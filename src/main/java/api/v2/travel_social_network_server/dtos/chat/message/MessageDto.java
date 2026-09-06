package api.v2.travel_social_network_server.dtos.chat.message;

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
public class MessageDto {
    private UUID messageId;
    private UUID groupChatId;
    private UUID senderId;
    private String senderName;
    private String content;
    private String type; // text, image, video, file
    private String status; // sent, delivered, read
    private Instant createdAt;
    private UUID receiverId; // cho private message
    private String receiverName; // cho private message
}