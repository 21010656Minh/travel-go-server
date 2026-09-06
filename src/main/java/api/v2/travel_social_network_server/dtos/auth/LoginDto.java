package api.v2.travel_social_network_server.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginDto {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min= 8, max = 15)
    private String password;

    private String role;
}
