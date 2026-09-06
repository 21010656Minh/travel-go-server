package api.v2.travel_social_network_server.dtos.notification;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateNotificationDto {
    private String content;
    private boolean isRead;
}
