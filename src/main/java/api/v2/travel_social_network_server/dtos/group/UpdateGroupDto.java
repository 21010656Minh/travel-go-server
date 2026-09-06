package api.v2.travel_social_network_server.dtos.group;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateGroupDto {
    @NotBlank
    private String name;
    private String description;
    private MultipartFile avatar;
    private MultipartFile cover;
    @NotBlank
    private Boolean privacy;
    private String tags;
}
