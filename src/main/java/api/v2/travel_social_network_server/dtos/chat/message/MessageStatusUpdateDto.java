package api.v2.travel_social_network_server.dtos.chat.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Message Status Update DTO
 * Used for updating message status (sent, delivered, read)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageStatusUpdateDto {

    /**
     * Message ID (MongoDB ID)
     */
    private String messageId;

    /**
     * Conversation ID
     */
    private UUID conversationId;

    /**
     * New status: 'sent', 'delivered', 'read'
     */
    private String status;

    /**
     * Timestamp of the status update
     */
    private Instant timestamp;

    /**
     * User ID who triggered the status update
     */
    private UUID userId;
}
