package api.v2.travel_social_network_server.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;

public class JwtAuthenticationConverter implements AuthenticationConverter {
    private static final String BEARER_TOKEN_PREFIX = "Bearer ";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    @Override
    public Authentication convert(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_TOKEN_PREFIX)) {
            return null;
        }
        String token = authorizationHeader.substring(BEARER_TOKEN_PREFIX.length());

        System.out.println(token);

        return new JwtAuthenticationToken(token);
    }
}

