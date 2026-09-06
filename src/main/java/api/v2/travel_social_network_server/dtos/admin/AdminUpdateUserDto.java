package api.v2.travel_social_network_server.dtos.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for admin to update user information")
public class AdminUpdateUserDto {
    
    @Email(message = "Invalid email format")
    @Schema(description = "User email", example = "user@example.com")
    private String email;
    
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Username", example = "johndoe")
    private String userName;
    
    @Schema(description = "Full name", example = "John Doe")
    private String fullName;
    
    @Schema(description = "Location", example = "Ho Chi Minh City")
    private String location;
    
    @Schema(description = "About/Bio", example = "Travel enthusiast")
    private String about;
}
