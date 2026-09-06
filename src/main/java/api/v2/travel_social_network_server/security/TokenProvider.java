package api.v2.travel_social_network_server.security;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import com.auth0.jwt.exceptions.SignatureVerificationException;

public interface TokenProvider {
    String generateToken(User user, ProviderTypeEnum providerTypeEnum);
    boolean validateToken(String token);
    String extractEmail(String token) throws SignatureVerificationException;
}
