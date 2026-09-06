package api.v2.travel_social_network_server.dtos.notification;

import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateNotificationDto {
    private UUID receiverId;
    private UUID senderId;
    private String type;
    private String content;
    private UUID relatedId;
}
