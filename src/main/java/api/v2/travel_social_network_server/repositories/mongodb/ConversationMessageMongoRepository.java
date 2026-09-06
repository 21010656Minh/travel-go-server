package api.v2.travel_social_network_server.repositories.mongodb;

import api.v2.travel_social_network_server.entities.mongodb.ConversationMessageDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * MongoDB Repository for Conversation Messages
 * Provides efficient queries for chat operations
 */
@Repository
public interface ConversationMessageMongoRepository extends MongoRepository<ConversationMessageDocument, String> {

        /**
         * Find messages by conversation ID
         * Sorted by creation time descending (newest first)
         */
        Page<ConversationMessageDocument> findByConversationIdOrderByCreatedAtDesc(
                        UUID conversationId,
                        Pageable pageable);

        /**
         * Search messages by content (case-insensitive)
         */
        @Query("{'conversationId': ?0, 'content': {$regex: ?1, $options: 'i'}}")
        Page<ConversationMessageDocument> searchMessagesInConversation(
                        UUID conversationId,
                        String keyword,
                        Pageable pageable);

        /**
         * Get the last message in a conversation
         */
        Optional<ConversationMessageDocument> findFirstByConversationIdOrderByCreatedAtDesc(
                        UUID conversationId);

        /**
         * Count unread messages for a user in a conversation
         * Messages sent after the lastReadAt timestamp and not sent by the user
         */
        @Query("{'conversationId': ?0, 'senderId': {$ne: ?1}, 'createdAt': {$gt: ?2}}")
        long countUnreadMessages(UUID conversationId, UUID userId, Instant lastReadAt);

        /**
         * Find unread messages for a user in a conversation
         */
        @Query("{'conversationId': ?0, 'senderId': {$ne: ?1}, 'createdAt': {$gt: ?2}}")
        List<ConversationMessageDocument> findUnreadMessages(
                        UUID conversationId,
                        UUID userId,
                        Instant lastReadAt);

        /**
         * Find messages in a thread (replies to a specific message)
         */
        List<ConversationMessageDocument> findByReplyToMessageIdOrderByCreatedAtAsc(
                        String messageId);

        /**
         * Find messages by sender
         */
        Page<ConversationMessageDocument> findBySenderIdOrderByCreatedAtDesc(
                        UUID senderId,
                        Pageable pageable);

        /**
         * Find messages by type (e.g., all images, videos)
         */
        Page<ConversationMessageDocument> findByConversationIdAndTypeOrderByCreatedAtDesc(
                        UUID conversationId,
                        String type,
                        Pageable pageable);

        /**
         * Find old messages for archival/cleanup
         */
        @Query("{'conversationId': ?0, 'createdAt': {$lt: ?1}}")
        List<ConversationMessageDocument> findOldMessages(UUID conversationId, Instant before);

        /**
         * Count total messages in a conversation
         */
        long countByConversationId(UUID conversationId);

        /**
         * Delete all messages in a conversation (hard delete)
         */
        void deleteByConversationId(UUID conversationId);

        /**
         * Find messages by multiple conversation IDs
         */
        @Query("{'conversationId': {$in: ?0}}")
        List<ConversationMessageDocument> findByConversationIdIn(List<UUID> conversationIds);
}
