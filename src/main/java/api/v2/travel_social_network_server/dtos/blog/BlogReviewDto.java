package api.v2.travel_social_network_server.dtos.blog;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogReviewDto {
    
    private UUID blogId;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    @jakarta.validation.constraints.NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be between 1 and 5")
    @Max(value = 5, message = "Rating must be between 1 and 5")
    private Integer rating; // Required: 1-5 stars (each user can only review once)
}
