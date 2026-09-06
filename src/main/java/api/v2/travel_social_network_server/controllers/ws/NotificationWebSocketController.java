package api.v2.travel_social_network_server.controllers.ws;

import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class NotificationWebSocketController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Gửi thông báo đến user qua WebSocket
     */
    public void sendNotification(UUID userId, NotificationResponse notification) {
        simpMessagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
        );    }

}
