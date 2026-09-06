package api.v2.travel_social_network_server.security.jwt;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.authentication.AuthenticationFilter;

public class JwtAuthenticationFilter extends AuthenticationFilter {

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager,
                                   AuthenticationConverter authenticationConverter) {
        super(authenticationManager, authenticationConverter);
        setSuccessHandler((request, response, authentication) -> {
            // Không cần làm gì khi xác thực thành công
        });
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") || path.startsWith("/ws");
    }
}
