package api.v2.travel_social_network_server.responses.auth;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class RegisterResponse {
    private String userName;
    private String email;
}
