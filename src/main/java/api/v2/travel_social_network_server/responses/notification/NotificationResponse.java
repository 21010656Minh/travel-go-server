package api.v2.travel_social_network_server.responses.notification;

import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private UUID notificationId;
    private UUID receiverId;
    private String receiverName;
    private UserSummaryResponse sender;
    private NotificationTypeEnum type;
    private String content;
    private UUID relatedId;
    private boolean isRead;
    private Instant createdAt;
    private Instant updatedAt;
}
