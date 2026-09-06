package api.v2.travel_social_network_server.dtos.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class AdminBlogDto {
    private UUID blogId;
    private String title;
    private String thumbnailUrl;
    private String authorName;
    private UUID authorId;
    private String status; // DRAFT, PUBLISHED, ARCHIVED, PENDING
    private Long viewCount;
    private Double averageRating;
    private Integer totalRatings;
    private String category; // Main tag
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant publishedAt;
}
