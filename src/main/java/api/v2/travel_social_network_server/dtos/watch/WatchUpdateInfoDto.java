package api.v2.travel_social_network_server.dtos.watch;

import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatchUpdateInfoDto {
    @Size(min = 10, max = 500, message = "Title must be between 10 and 500 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    private MultipartFile thumbnail;

    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;

    private PrivacyTypeEnum privacy;

    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @Size(max = 5, message = "Maximum 5 tags allowed")
    private List<String> tags;
}
