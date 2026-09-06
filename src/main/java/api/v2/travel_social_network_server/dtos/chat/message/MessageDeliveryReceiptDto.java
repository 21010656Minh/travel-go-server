package api.v2.travel_social_network_server.dtos.chat.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Message Delivery Receipt DTO
 * Used for tracking message delivery and read status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDeliveryReceiptDto {

    /**
     * Message ID (MongoDB ID)
     */
    private String messageId;

    /**
     * Conversation ID
     */
    private UUID conversationId;

    /**
     * User ID who received/read the message
     */
    private UUID userId;

    /**
     * Username
     */
    private String username;

    /**
     * Receipt type: 'delivered' or 'read'
     */
    private String type;

    /**
     * Timestamp when the action occurred
     */
    private Instant timestamp;
}
