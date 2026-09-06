package api.v2.travel_social_network_server.services.chat.mongodb;

import api.v2.travel_social_network_server.entities.mongodb.ConversationMessageDocument;
import api.v2.travel_social_network_server.responses.chat.ConversationMessageResponse;
import org.springframework.data.domain.Page;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Interface for MongoDB-based Conversation Message Service
 */
public interface IConversationMessageMongoService {

    /**
     * Save a new message to MongoDB
     */
    ConversationMessageDocument saveMessage(
            UUID conversationId,
            UUID senderId,
            String senderName,
            String senderAvatar,
            String content,
            String type,
            String mediaUrl);

    /**
     * Get messages for a conversation with pagination
     */
    Page<ConversationMessageDocument> getMessages(UUID conversationId, int page, int size);

    /**
     * Get messages for a conversation with pagination (Response DTO)
     */
    Page<ConversationMessageResponse> getMessagesResponse(UUID conversationId, int page, int size);

    /**
     * Search messages in a conversation
     */
    Page<ConversationMessageDocument> searchMessages(UUID conversationId, String keyword, int page, int size);

    /**
     * Get the last message in a conversation
     */
    ConversationMessageDocument getLastMessage(UUID conversationId);

    /**
     * Update message content (edit)
     */
    ConversationMessageDocument updateMessage(String messageId, String newContent);

    /**
     * Delete message (soft delete)
     */
    void deleteMessage(String messageId, UUID userId);

    /**
     * Hard delete message
     */
    void hardDeleteMessage(String messageId);

    /**
     * Mark message as read
     */
    void markMessageAsRead(String messageId);

    /**
     * Count unread messages for a user in a conversation
     */
    long countUnreadMessages(UUID conversationId, UUID userId, Instant lastReadAt);

    /**
     * Get unread messages for a user in a conversation
     */
    List<ConversationMessageDocument> getUnreadMessages(UUID conversationId, UUID userId, Instant lastReadAt);

    /**
     * Get messages by type (e.g., images, videos)
     */
    Page<ConversationMessageDocument> getMessagesByType(UUID conversationId, String type, int page, int size);

    /**
     * Get thread messages (replies to a message)
     */
    List<ConversationMessageDocument> getThreadMessages(String messageId);

    /**
     * Count total messages in a conversation
     */
    long countMessages(UUID conversationId);

    /**
     * Get message by ID
     */
    ConversationMessageDocument getMessageById(String messageId);

    /**
     * Delete all messages in a conversation
     */
    void deleteAllMessagesInConversation(UUID conversationId);
}
