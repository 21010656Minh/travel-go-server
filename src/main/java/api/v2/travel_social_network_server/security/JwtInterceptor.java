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
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.wrap(message);
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
                jwtToken = (String) tokenObj;
            }
            
            // Double-fallback: check if token is in query parameter
            if (jwtToken == null) {
                Object queryToken = accessor.getSessionAttributes().get("token");
                if (queryToken instanceof String) {
                    jwtToken = (String) queryToken;
                }
            }
        }

        if (jwtToken != null && !jwtToken.isEmpty()) {
            try {
                String username = jwtTokenProvider.extractEmail(jwtToken);

                if (username != null && jwtTokenProvider.validateToken(jwtToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
                    User userDetails = (User) userDetailsService.loadUserByUsername(username);

                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authenticationToken.setDetails(userDetails);
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                    if (accessor.getSessionAttributes() != null) {
                        accessor.getSessionAttributes().put("simpUser", authenticationToken.getPrincipal());
                        accessor.getSessionAttributes().put("userId", userDetails.getUserId());
                    }

                    log.info("✅ User authenticated via WebSocket: {} (userId: {})", username, userDetails.getUserId());
                } else {
                    log.warn("⚠️ Token validation failed or user already authenticated");
                }
            } catch (Exception e) {
                log.error("❌ Error validating token in JwtInterceptor: {}", e.getMessage(), e);
            }
        } else {
            log.warn("⚠️ No valid token found in WebSocket message (header, session, or query)");
        }

        return message;
    }
}

