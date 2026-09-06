package api.v2.travel_social_network_server.security;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.TokenTypeEnum;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import org.springframework.security.core.GrantedAuthority;

import java.util.Date;
import java.util.List;

public class JwtTokenProvider implements TokenProvider {
    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String issuer;
    private final long expirationSeconds;

    public JwtTokenProvider(String secret, String issuer, long expirationSeconds) {
        if (secret == null || secret.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT secret cannot be null or empty");
        }
        if (issuer == null || issuer.trim().isEmpty()) {
            throw new IllegalArgumentException("JWT issuer cannot be null or empty");
        }
        if (expirationSeconds <= 0) {
            throw new IllegalArgumentException("Expiration seconds must be positive");
        }

        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expirationSeconds = expirationSeconds;
        this.verifier = JWT.require(algorithm)
                .withIssuer(this.issuer)
                .build();
    }

    public String generateToken(User user, ProviderTypeEnum providerTypeEnum) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (providerTypeEnum == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        List<String> roles = user.getAuthorities() != null
                ? user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
                : List.of();

        long expirationTime = System.currentTimeMillis() + (expirationSeconds * 1000);

        System.out.println("Generating token for user: " + user.getEmail());
        System.out.println("Token will expire at: " + new Date(expirationTime));

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getEmail())
                .withClaim("userId", String.valueOf(user.getUserId()))
                .withClaim("roles", roles)
                .withClaim("provider", providerTypeEnum.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(expirationTime))
                .sign(algorithm);
    }

    @Override
    public String extractEmail(String token) throws SignatureVerificationException {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException("Token cannot be null or empty");
        }

        try {
            return verifier.verify(token).getSubject();
        } catch (JWTVerificationException e) {
            System.out.println("Failed to extract email from token: " + e.getMessage());
            throw new SignatureVerificationException(algorithm, e);
        }
    }

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            System.out.println("Token validation failed: token is null or empty");
            return false;
        }

        try {
            var decodedJWT = verifier.verify(token);
            System.out.println("Token validation successful for: " + decodedJWT.getSubject());
            return true;
        } catch (JWTVerificationException e) {
            System.out.println("JWT validation failed: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            return false;
        }
    }

    public String generateRefreshToken(User user, ProviderTypeEnum providerTypeEnum) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("User email cannot be null or empty");
        }
        if (providerTypeEnum == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        long refreshExpirationTime = System.currentTimeMillis() + (7L * 24 * 60 * 60 * 1000);

        System.out.println("Generating refresh token for user: " + user.getEmail());
        System.out.println("Refresh token will expire at: " + new Date(refreshExpirationTime));

        return JWT.create()
                .withIssuer(issuer)
                .withSubject(user.getEmail())
                .withClaim("userId", String.valueOf(user.getUserId()))
                .withClaim("provider", providerTypeEnum.toString())
                .withClaim("type", TokenTypeEnum.REFRESH.getValue())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(refreshExpirationTime))
                .sign(algorithm);
    }
}
