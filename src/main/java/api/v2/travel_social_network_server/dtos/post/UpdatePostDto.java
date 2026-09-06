package api.v2.travel_social_network_server.dtos.post;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostDto {
    @NotBlank
    private String content;
    private String location;
    private String privacy;
    private String postType;  // NORMAL, AVATAR_UPDATE, COVER_UPDATE
    private String mediaType;
    private List<String> tags;
    private List<MultipartFile> mediaFiles;
}
