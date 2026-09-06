package api.v2.travel_social_network_server.responses.watch;

import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class WatchResponse {
    private UUID watchId;
    private UserResponse user;
    private String title;
    private String description;
    private String videoUrl;
    private String thumbnailUrl;
    private Integer duration; // Duration in seconds
    private String location;
    private PrivacyTypeEnum privacy;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private Integer viewCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
    
    private List<String> tags;
    
    // Additional fields for current user interaction
    private Boolean liked;
    private Boolean saved;
    private Boolean watched;
}
