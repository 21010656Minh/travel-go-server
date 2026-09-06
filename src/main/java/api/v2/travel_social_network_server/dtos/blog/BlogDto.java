package api.v2.travel_social_network_server.dtos.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogDto {
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String thumbnailUrl;
    
    // Thumbnail file for multipart upload (optional - use either thumbnailUrl or thumbnail file)
    private MultipartFile thumbnail;
    
    @Size(max = 255, message = "Location must not exceed 255 characters")
    private String location;
    
    private List<String> tagTitles;
    
    private List<String> mediaUrls;
    
    // Media IDs from content editor (uploaded separately via /media/upload)
    private List<UUID> mediaIds;
    
    private String status; // DRAFT, PUBLISHED, ARCHIVED, PENDING
    
    private Boolean isFeatured;
    
    private Integer readingTime;
}
