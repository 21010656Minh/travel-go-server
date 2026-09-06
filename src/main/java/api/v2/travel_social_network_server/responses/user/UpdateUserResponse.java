package api.v2.travel_social_network_server.responses.user;

import api.v2.travel_social_network_server.entities.UserProfile;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateUserResponse {
    protected UUID userId;
    protected String userName;
    protected UserProfile userProfile;
}
