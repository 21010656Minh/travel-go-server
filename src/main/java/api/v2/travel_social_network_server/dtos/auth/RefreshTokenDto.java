package api.v2.travel_social_network_server.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshTokenDto {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;
}
