package api.v2.travel_social_network_server.services.chat.conversationmessage;

import api.v2.travel_social_network_server.entities.ConversationMessage;
import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.ConversationMessageRepository;
import api.v2.travel_social_network_server.repositories.ConversationRepository;
import api.v2.travel_social_network_server.repositories.ConversationMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationMessageService implements IConversationMessageService {

    private final ConversationMessageRepository conversationMessageRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationMemberRepository conversationMemberRepository;

    @Override
    @Transactional
    public ConversationMessage sendMessage(Conversation conversation, User user, String content, String type) {
        return sendMessage(conversation, user, content, type, null);
    }

    @Override
    @Transactional
    public ConversationMessage sendMessage(Conversation conversation, User user, String content, String type, String mediaUrl) {
        ConversationMessage message = ConversationMessage.builder()
                .conversation(conversation)
                .sender(user)
                .content(content)
                .type(type)
                .mediaUrl(mediaUrl)
                .status("sent")
                .build();
        
        ConversationMessage savedMessage = conversationMessageRepository.save(message);
        
        // Cập nhật last message và last active time của group
        conversation.setLastMessage(content);
        conversation.setLastActiveAt(Instant.now());
        conversationRepository.save(conversation);
        
        return savedMessage;
    }

    @Override
    @Transactional
    public ConversationMessage saveMessage(UUID groupChatId, User user, String content, String type) {
        return saveMessage(groupChatId, user, content, type, null);
    }

    @Override
    @Transactional
    public ConversationMessage saveMessage(UUID groupChatId, User user, String content, String type, String mediaUrl) {
        Conversation conversation = conversationRepository.findById(groupChatId)
                .orElseThrow(() -> new ResourceNotFoundException("Group chat not found: " + groupChatId));
        
        return sendMessage(conversation, user, content, type, mediaUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationMessage> getMessages(UUID groupId, int page, int size) {
        return conversationMessageRepository.findByConversationConversationIdOrderByCreatedAtAsc(groupId,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationMessage> searchMessages(UUID groupId, String keyword, int page, int size) {
        return conversationMessageRepository.searchMessagesInGroup(groupId, keyword,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    @Override
    @Transactional(readOnly = true)
    public ConversationMessage getLastMessage(UUID groupId) {
        return conversationMessageRepository.findFirstByConversationConversationIdOrderByCreatedAtDesc(groupId)
                .orElse(null);
    }

    @Override
    @Transactional
    public void markMessageAsRead(UUID messageId) {
        ConversationMessage message = conversationMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        message.setStatus("read");
        conversationMessageRepository.save(message);
    }

    @Override
    @Transactional
    public void markAllMessagesAsRead(UUID groupId, UUID userId) {
        // Kiểm tra user có phải là member của group không
        boolean isMember = conversationMemberRepository.existsByConversationConversationIdAndUserUserId(groupId, userId);
        if (!isMember) {
            throw new ResourceNotFoundException("User is not a member of this group");
        }
        
        conversationMessageRepository.markAllMessagesAsReadInGroup(groupId, userId);
    }

    @Override
    @Transactional
    public void deleteMessage(UUID messageId, UUID userId) {
        ConversationMessage message = conversationMessageRepository.findById(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message not found: " + messageId));
        
        // Kiểm tra quyền xóa (chỉ người gửi mới được xóa)
        if (!message.getSender().getUserId().equals(userId)) {
            throw new SecurityException("You can only delete your own messages");
        }
        
        conversationMessageRepository.delete(message);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadMessageCount(UUID groupId, UUID userId) {
        return conversationMessageRepository.countUnreadMessagesInGroup(groupId, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationMessage> getUnreadMessages(UUID groupId, UUID userId) {
        return conversationMessageRepository.findUnreadMessagesInGroup(groupId, userId);
    }
}
