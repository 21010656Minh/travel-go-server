package api.v2.travel_social_network_server.dtos.user;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UpdateUserDto {
    @NotBlank
    private String userName;
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String location;
    @NotBlank
    private String gender;
    @NotBlank
    private LocalDate dateOfBirth;
    private String about;
}
