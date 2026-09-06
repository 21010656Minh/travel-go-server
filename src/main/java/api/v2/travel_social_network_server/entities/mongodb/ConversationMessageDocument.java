package api.v2.travel_social_network_server.entities.mongodb;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

/**
 * MongoDB Document for Conversation Messages
 * Converted from JPA ConversationMessage entity to MongoDB for better chat
 * performance
 */
@Document(collection = "conversation_messages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "conversation_created_idx", def = "{'conversationId': 1, 'createdAt': -1}")
})
public class ConversationMessageDocument {

    /**
     * MongoDB auto-generated ID
     */
    @Id
    private String id;

    /**
     * Original UUID from PostgreSQL (for migration compatibility)
     */
    private UUID conversationMessageId;

    /**
     * Reference to conversation in PostgreSQL
     */
    @Indexed
    private UUID conversationId;

    /**
     * Sender information (denormalized for better read performance)
     */
    @Indexed
    private UUID senderId;

    private String senderName;

    private String senderAvatar;

    /**
     * Message content
     */
    private String content;

    /**
     * Media URL (for images, videos, files)
     */
    private String mediaUrl;

    /**
     * Message type: text, image, video, file, audio
     */
    private String type;

    /**
     * Message status: sent, delivered, read
     */
    private String status;

    /**
     * Timestamps
     */
    @Indexed
    private Instant createdAt;
    private Instant updatedAt;

    /**
     * Edit tracking
     */
    private boolean isEdited;
    private Instant editedAt;

    /**
     * Reply/Thread support (optional enhancement)
     */
    private String replyToMessageId;

    /**
     * Content of the replied message (not stored in DB, populated on read)
     */
    @org.springframework.data.annotation.Transient
    private String repliedMessageContent;
}
