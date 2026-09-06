package api.v2.travel_social_network_server.responses.auth;

import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private UUID userId;
    private String userName;
    private String email;
    private String token;
    private String refreshToken;
    private String avatarImg;
    private String coverImg;
    private String role;
    private UserProfile userProfile;
    private ProviderTypeEnum provider;
}
