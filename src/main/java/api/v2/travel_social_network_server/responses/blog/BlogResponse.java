package api.v2.travel_social_network_server.responses.blog;

import api.v2.travel_social_network_server.responses.tag.TagResponse;
import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BlogResponse {
    
    private UUID blogId;
    
    private UserSummaryResponse author;
    
    private String title;
    
    private String content;
    
    private String description;
    
    private String thumbnailUrl;
    
    private String location;
    
    private Long viewCount;
    
    private Integer likeCount;
    
    private Integer reviewCount;
    
    private Double averageRating;
    
    private Integer totalRatings;
    
    private String status;
    
    private Boolean isFeatured;
    
    private Integer readingTime;
    
    private List<TagResponse> tags;
    
    private List<String> mediaUrls;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant publishedAt;
    
    private Boolean hasReviewed; // True if current user has already reviewed this blog
}
