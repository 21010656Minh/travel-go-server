package api.v2.travel_social_network_server.dtos.chat.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {
    @NotNull(message = "Conversation ID is required")
    private UUID groupChatId;
    
    @NotBlank(message = "Message content is required")
    private String content;
    
    private String type; // text, image, video, file (default: "text")
    private String mediaUrl; // URL for media attachments
    private String replyToMessageId; // MongoDB ID of message being replied to
}
