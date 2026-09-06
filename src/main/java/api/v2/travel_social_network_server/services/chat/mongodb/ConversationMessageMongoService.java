package api.v2.travel_social_network_server.services.chat.mongodb;

import api.v2.travel_social_network_server.entities.mongodb.ConversationMessageDocument;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.mongodb.ConversationMessageMongoRepository;
import api.v2.travel_social_network_server.responses.chat.ConversationMessageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MongoDB-based Conversation Message Service Implementation
 * Handles all chat message operations using MongoDB for better performance
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationMessageMongoService implements IConversationMessageMongoService {

    private final ConversationMessageMongoRepository messageRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public ConversationMessageDocument saveMessage(
            UUID conversationId,
            UUID senderId,
            String senderName,
            String senderAvatar,
            String content,
            String type,
            String mediaUrl) {
        return saveMessage(conversationId, senderId, senderName, senderAvatar, content, type, mediaUrl, null);
    }

    public ConversationMessageDocument saveMessage(
            UUID conversationId,
            UUID senderId,
            String senderName,
            String senderAvatar,
            String content,
            String type,
            String mediaUrl,
            String replyToMessageId) {

        ConversationMessageDocument message = new ConversationMessageDocument();
        message.setConversationMessageId(UUID.randomUUID());
        message.setConversationId(conversationId);
        message.setSenderId(senderId);
        message.setSenderName(senderName);
        message.setSenderAvatar(senderAvatar);
        message.setContent(content);
        message.setType(type);
        message.setMediaUrl(mediaUrl);
        message.setStatus("sent");
        message.setCreatedAt(Instant.now());
        message.setUpdatedAt(Instant.now());
        message.setEdited(false);
        message.setReplyToMessageId(replyToMessageId);

        ConversationMessageDocument savedMessage = mongoTemplate.save(message, "conversation_messages");
        log.debug("Message saved with ID: {}, replyTo: {}", savedMessage.getId(), replyToMessageId);

        return savedMessage;
    }

    @Override
    public Page<ConversationMessageDocument> getMessages(UUID conversationId, int page, int size) {        // Use MongoDB aggregation with $lookup to join replied messages
        LookupOperation lookupOperation = LookupOperation.newLookup()
                .from("conversation_messages")
                .localField("replyToMessageId")
                .foreignField("_id")
                .as("repliedMessage");

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("conversationId").is(conversationId)),
                Aggregation.sort(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"),
                Aggregation.skip((long) page * size),
                Aggregation.limit(size),
                lookupOperation,
                Aggregation.addFields()
                        .addFieldWithValue("repliedMessageContent", 
                                org.springframework.data.mongodb.core.aggregation.ArrayOperators.ArrayElemAt
                                        .arrayOf("repliedMessage.content").elementAt(0))
                        .build()
        );

        AggregationResults<ConversationMessageDocument> results = mongoTemplate.aggregate(
                aggregation,
                "conversation_messages",
                ConversationMessageDocument.class
        );

        // Create mutable list and reverse for chat display (old -> new)
        List<ConversationMessageDocument> messages = new java.util.ArrayList<>(results.getMappedResults());
        java.util.Collections.reverse(messages);
        
        long total = messageRepository.countByConversationId(conversationId);

        return new PageImpl<>(messages, PageRequest.of(page, size), total);
    }

    @Override
    public Page<ConversationMessageResponse> getMessagesResponse(UUID conversationId, int page, int size) {        Page<ConversationMessageDocument> messagesPage = getMessages(conversationId, page, size);
        
        List<ConversationMessageResponse> responseList = messagesPage.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        
        return new PageImpl<>(responseList, messagesPage.getPageable(), messagesPage.getTotalElements());
    }

    /**
     * Map ConversationMessageDocument to ConversationMessageResponse
     */
    private ConversationMessageResponse mapToResponse(ConversationMessageDocument doc) {
        return ConversationMessageResponse.builder()
                .id(doc.getId())
                .conversationMessageId(doc.getConversationMessageId())
                .conversationId(doc.getConversationId())
                .senderId(doc.getSenderId())
                .senderName(doc.getSenderName())
                .senderAvatar(doc.getSenderAvatar())
                .content(doc.getContent())
                .mediaUrl(doc.getMediaUrl())
                .type(doc.getType())
                .status(doc.getStatus())
                .createdAt(doc.getCreatedAt())
                .updatedAt(doc.getUpdatedAt())
                .isEdited(doc.isEdited())
                .editedAt(doc.getEditedAt())
                .replyToMessageId(doc.getReplyToMessageId())
                .repliedMessageContent(doc.getRepliedMessageContent())
                .build();
    }

    @Override
    public Page<ConversationMessageDocument> searchMessages(UUID conversationId, String keyword, int page, int size) {        return messageRepository.searchMessagesInConversation(
                conversationId,
                keyword,
                PageRequest.of(page, size));
    }

    @Override
    public ConversationMessageDocument getLastMessage(UUID conversationId) {        return messageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conversationId)
                .orElse(null);
    }

    @Override
    public ConversationMessageDocument updateMessage(String messageId, String newContent) {        ConversationMessageDocument message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));

        message.setContent(newContent);
        message.setEdited(true);
        message.setEditedAt(Instant.now());
        message.setUpdatedAt(Instant.now());

        return messageRepository.save(message);
    }

    @Override
    public void deleteMessage(String messageId, UUID userId) {        ConversationMessageDocument message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));

        // Verify the user is the sender
        if (!message.getSenderId().equals(userId)) {
            throw new SecurityException("You can only delete your own messages");
        }

        // Hard delete the message
        messageRepository.deleteById(messageId);    }

    @Override
    public void hardDeleteMessage(String messageId) {        messageRepository.deleteById(messageId);
    }

    @Override
    public void markMessageAsRead(String messageId) {        ConversationMessageDocument message = messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));

        // Idempotency check - only update if not already read
        if ("read".equals(message.getStatus())) {            return;
        }

        message.setStatus("read");
        message.setUpdatedAt(Instant.now());

        messageRepository.save(message);    }

    @Override
    public long countUnreadMessages(UUID conversationId, UUID userId, Instant lastReadAt) {        return messageRepository.countUnreadMessages(conversationId, userId, lastReadAt);
    }

    @Override
    public List<ConversationMessageDocument> getUnreadMessages(UUID conversationId, UUID userId, Instant lastReadAt) {        return messageRepository.findUnreadMessages(conversationId, userId, lastReadAt);
    }

    @Override
    public Page<ConversationMessageDocument> getMessagesByType(UUID conversationId, String type, int page, int size) {        return messageRepository.findByConversationIdAndTypeOrderByCreatedAtDesc(
                conversationId,
                type,
                PageRequest.of(page, size));
    }

    @Override
    public List<ConversationMessageDocument> getThreadMessages(String messageId) {        return messageRepository.findByReplyToMessageIdOrderByCreatedAtAsc(messageId);
    }

    @Override
    public long countMessages(UUID conversationId) {        return messageRepository.countByConversationId(conversationId);
    }

    @Override
    public ConversationMessageDocument getMessageById(String messageId) {        return messageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found with ID: " + messageId));
    }

    @Override
    public void deleteAllMessagesInConversation(UUID conversationId) {        messageRepository.deleteByConversationId(conversationId);
    }
}
