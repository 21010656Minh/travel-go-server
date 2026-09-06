package api.v2.travel_social_network_server.responses.blog;

import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogReviewResponse {
    
    private UUID reviewId;
    
    private UUID blogId;
    
    private String blogTitle; // For user reviews page
    
    private String blogThumbnailUrl; // For user reviews page
    
    private UserSummaryResponse blogAuthor; // Blog author info
    
    private UserSummaryResponse user; // Review author info
    
    private String content;
    
    private Integer rating; // 1-5 stars (always required)
    
    private Boolean isEdited;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant updatedAt;
}
