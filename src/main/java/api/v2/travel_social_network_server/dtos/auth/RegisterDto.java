package api.v2.travel_social_network_server.dtos.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RegisterDto {
    @NotBlank
    private String userName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 15)
    private String password;

    @Size(max = 50)
    private String firstName;

    @Size(max = 50)
    private String lastName;

    private LocalDate dateOfBirth;

    @Size(max = 10)
    private String gender;
}
