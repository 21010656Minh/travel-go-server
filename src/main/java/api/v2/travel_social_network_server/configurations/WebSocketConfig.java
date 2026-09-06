package api.v2.travel_social_network_server.configurations;

import api.v2.travel_social_network_server.security.JwtInterceptor;
import api.v2.travel_social_network_server.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.messaging.context.AuthenticationPrincipalArgumentResolver;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${api.base-url}")
    private String apiBaseUrl;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthenticationPrincipalArgumentResolver());
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // ✅ Endpoint chính cho WebSocket (không cần thêm apiBaseUrl)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Dev: cho phép tất cả, Production nên chỉ định domain cụ thể
                .addInterceptors(new HandshakeInterceptor() {
                    @Override
                    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
                        // Extract token from query parameter for SockJS handshake
                        if (request instanceof ServletServerHttpRequest) {
                            HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
                            String token = servletRequest.getParameter("token");
                            
                            if (token != null && !token.isEmpty()) {
                                try {
                                    // Validate token
                                    String username = jwtTokenProvider.extractEmail(token);
                                    if (username != null && jwtTokenProvider.validateToken(token)) {
                                        attributes.put("token", token);
                                        attributes.put("username", username);
                                        return true;
                                    } else {
                                    }
                                } catch (Exception e) {
                                    log.error("❌ Error validating token in WebSocket handshake: {}", e.getMessage());
                                }
                            } else {
                            }
                        }
                        return true; // Allow connection, authentication will be checked in JwtInterceptor
                    }

                    @Override
                    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                               WebSocketHandler wsHandler, Exception exception) {
                        if (exception != null) {
                            log.error("❌ WebSocket handshake failed: {}", exception.getMessage());
                        }
                    }
                })
                .withSockJS(); // Hỗ trợ fallback cho browser không support WebSocket

        // Optional: Endpoint không dùng SockJS (cho native WebSocket client)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Client gửi đến server qua /app/*
        config.setApplicationDestinationPrefixes("/app");

        // Server gửi về client qua /topic/* (broadcast), /user/* (point-to-point), /group/* (group chat)
        config.enableSimpleBroker("/topic", "/user", "/queue", "/group");

        // Prefix riêng cho user-specific messages
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public boolean configureMessageConverters(List<MessageConverter> converters) {
        converters.add(messageConverter());
        return false; // giữ thêm converter mặc định của Spring
    }

    @Bean
    public MappingJackson2MessageConverter messageConverter() {
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // hỗ trợ LocalDateTime, Instant
        mapper.findAndRegisterModules();

        converter.setObjectMapper(mapper);
        return converter;
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtInterceptor);
    }
}
