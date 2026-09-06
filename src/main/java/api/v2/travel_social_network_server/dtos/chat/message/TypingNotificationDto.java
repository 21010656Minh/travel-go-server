package api.v2.travel_social_network_server.dtos.chat.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Typing Notification DTO
 * Used for real-time typing indicators in chat
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypingNotificationDto {

    /**
     * User ID who is typing
     */
    private UUID userId;

    /**
     * Username who is typing
     */
    private String username;

    /**
     * Whether user is currently typing
     */
    private boolean typing;

    /**
     * Conversation/Group ID where typing is happening
     */
    private UUID groupChatId;
}
