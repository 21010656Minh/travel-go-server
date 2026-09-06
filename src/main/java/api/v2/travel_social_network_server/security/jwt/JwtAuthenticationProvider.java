package api.v2.travel_social_network_server.security.jwt;

import api.v2.travel_social_network_server.security.TokenProvider;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JwtAuthenticationProvider implements AuthenticationProvider {
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationProvider(TokenProvider tokenProvider, UserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String token = (String) authentication.getCredentials();

        if (!tokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid JWT token");
        }

        String email = tokenProvider.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        return new JwtAuthenticationToken(userDetails.getAuthorities(), userDetails, token);
    }


    @Override
    public boolean supports(Class<?> authentication) {
        return JwtAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
