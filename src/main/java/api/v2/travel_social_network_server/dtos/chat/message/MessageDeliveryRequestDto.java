package api.v2.travel_social_network_server.dtos.chat.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Message Delivery Request DTO
 * Used when client sends delivery/read confirmation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDeliveryRequestDto {

    /**
     * Message ID (MongoDB ID)
     */
    private String messageId;

    /**
     * Conversation ID
     */
    private UUID conversationId;
}
