package api.v2.travel_social_network_server.dtos.admin;

import api.v2.travel_social_network_server.utilities.enums.RoleTypeEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminRegisterDto {
    
    @NotBlank(message = "Username is required")
    private String userName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 15, message = "Password must be between 8 and 15 characters")
    private String password;
    
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;
    
    private LocalDate dateOfBirth;
    
    @Size(max = 10, message = "Gender must not exceed 10 characters")
    private String gender;
    
    @NotNull(message = "Role is required")
    private RoleTypeEnum role;
}
