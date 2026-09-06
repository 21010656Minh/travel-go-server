package api.v2.travel_social_network_server.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GoogleAuthDto {
    @NotBlank
    private String accessToken;
    
    private String idToken;
}
