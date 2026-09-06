package api.v2.travel_social_network_server.services.chat.chat;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.services.presence.IPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {

    private final ChatService chatService;
    private final IPresenceService presenceService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Lấy user từ session attributes (with null safety)
        if (headerAccessor.getSessionAttributes() != null) {
            User user = (User) headerAccessor.getSessionAttributes().get("simpUser");
            if (user != null) {
                chatService.markUserOnline(user.getUserId(), sessionId);
                // Broadcast online presence event to /topic/presence
                presenceService.markOnline(user, sessionId);
                log.info("User {} connected with session {}", user.getUsername(), sessionId);
            } else {
                log.warn("⚠️ Session connected but no simpUser in attributes: sessionId={}", sessionId);
            }
        } else {
            log.warn("⚠️ Session connected but no session attributes: sessionId={}", sessionId);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Lấy user từ session attributes (with null safety)
        if (headerAccessor.getSessionAttributes() != null) {
            User user = (User) headerAccessor.getSessionAttributes().get("simpUser");
            if (user != null) {
                chatService.markUserOffline(user.getUserId());
                // Broadcast offline presence event only if no other active sessions remain
                presenceService.markOffline(user.getUserId(), sessionId);
                log.info("User {} disconnected from session {}", user.getUsername(), sessionId);
            } else {
                log.warn("⚠️ Session disconnected but no simpUser in attributes: sessionId={}", sessionId);
            }
        } else {
            log.warn("⚠️ Session disconnected but no session attributes: sessionId={}", sessionId);
        }
    }
}
