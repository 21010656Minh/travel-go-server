package api.v2.travel_social_network_server.configurations;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import api.v2.travel_social_network_server.security.jwt.JwtAuthenticationFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Value("${api.base-url}")
    private String apiBaseURL;

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                                // WebSocket endpoints - MUST be first
                                .requestMatchers("/ws", "/ws/**", "/ws**").permitAll()
                                // Actuator health endpoints for Docker health checks
                                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                                // public endpoints
                                .requestMatchers("/error").permitAll()
                                .requestMatchers("/uploads/**").permitAll()
                                .requestMatchers(apiBaseURL + "/auth/**").permitAll()
                                // admin endpoints - no security
                                .requestMatchers(apiBaseURL + "/admin/**").permitAll()
// presence endpoints - require authentication (default) but allow JWT-filtered access
                .requestMatchers(apiBaseURL + "/presence/**").authenticated()
                // discovery endpoints - require authentication (default) but allow JWT-filtered access
                .requestMatchers(apiBaseURL + "/discovery/**").authenticated()
                                // swagger
                                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                .anyRequest().authenticated()
                )
                .userDetailsService(userDetailsService)
                .addFilterAfter(jwtAuthenticationFilter, LogoutFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint(authenticationEntryPoint()))
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);

        System.out.println("CORS Configuration: " + configuration.toString());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            System.out.println("Authentication failed: " + authException.getMessage());
            System.out.println("Request URI: " + request.getRequestURI());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String jsonResponse = """
                   {
                        "error": "Unauthorized",
                        "message": "Authentication required",
                        "path": "%s"
                   }
                   """.formatted(request.getRequestURI());
            response.getWriter().write(jsonResponse);
        };
    }

}
