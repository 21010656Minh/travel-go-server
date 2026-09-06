package api.v2.travel_social_network_server.configurations;

import api.v2.travel_social_network_server.security.JwtTokenProvider;
import api.v2.travel_social_network_server.security.TokenProvider;
import api.v2.travel_social_network_server.security.jwt.JwtAuthenticationConverter;
import api.v2.travel_social_network_server.security.jwt.JwtAuthenticationFilter;
import api.v2.travel_social_network_server.security.jwt.JwtAuthenticationProvider;
import api.v2.travel_social_network_server.services.user.UserService;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserService userService;

    @PostConstruct
    public void init() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return userService::getUserByEmail;
    }

    @Bean
    public TokenProvider tokenProvider(@Value("${jwt.secret.key}") String secret,
                                       @Value("${spring.application.name}") String issuer,
                                       @Value("${jwt.expiration-seconds}") long expirationSeconds) {
        return new JwtTokenProvider(secret, issuer, expirationSeconds);
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(TokenProvider tokenProvider, UserDetailsService userDetailsService) {
        return new JwtAuthenticationProvider(tokenProvider, userDetailsService);
    }

    @Bean
    public AuthenticationManager authenticationManager(JwtAuthenticationProvider jwtAuthenticationProvider) throws Exception {
        var daoProvider = new org.springframework.security.authentication.dao.DaoAuthenticationProvider();
        daoProvider.setUserDetailsService(userDetailsService());
        daoProvider.setPasswordEncoder(passwordEncoder());

        return new ProviderManager(daoProvider, jwtAuthenticationProvider);
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new JwtAuthenticationFilter(authenticationManager, new JwtAuthenticationConverter());
    }

}
