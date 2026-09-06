package api.v2.travel_social_network_server.security;

import api.v2.travel_social_network_server.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtInterceptor implements ChannelInterceptor {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
        String jwtToken = null;

        // Try to get token from Authorization header first
        String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwtToken = authorizationHeader.substring(7);
        } 
        // Fallback: get token from session attributes (from handshake)
        else if (accessor.getSessionAttributes() != null) {
            Object tokenObj = accessor.getSessionAttributes().get("token");
            if (tokenObj instanceof String) {
                jwtToken = (String) tokenObj;            }
        }

        if (jwtToken != null) {
            String username = jwtTokenProvider.extractEmail(jwtToken);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails = (User) userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                authenticationToken.setDetails(userDetails);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                accessor.getSessionAttributes().put("simpUser", authenticationToken.getPrincipal());
                accessor.getSessionAttributes().put("userId", userDetails.getUserId());

                log.info("✅ User authenticated via WebSocket: {} (userId: {})", username, userDetails.getUserId());
            }
        } else {
            log.warn("⚠️ No token found in WebSocket message (header or session)");
        }

        return message;
    }
}

