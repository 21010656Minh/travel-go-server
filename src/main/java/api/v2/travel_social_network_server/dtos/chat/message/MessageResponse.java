package api.v2.travel_social_network_server.dtos.chat.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private UUID messageId; // UUID for compatibility
    private String mongoId; // MongoDB document ID
    private UUID groupChatId;
    private UUID senderId;
    private String senderName;
    private String content;
    private String type;
    private String mediaUrl; // URL for image/video/file attachments
    private String status;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    private String senderAvatar;
    private String replyToMessageId; // MongoDB ID of message being replied to
    private String repliedMessageContent; // Content of replied message for display
}
