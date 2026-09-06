package api.v2.travel_social_network_server.dtos.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePasswordDto {
    @NotBlank
    @Size(min = 8, max = 15)
    private String newPassword;

    @NotBlank
    @Size(min = 8, max = 15)
    private String newPasswordConfirm;
}
