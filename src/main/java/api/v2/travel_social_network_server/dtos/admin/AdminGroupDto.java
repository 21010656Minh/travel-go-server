package api.v2.travel_social_network_server.dtos.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminGroupDto {
    private UUID groupId;
    private String groupName;
    private String coverImageUrl;
    private Integer memberCount;
    private String createdBy;  // Name of the creator
    private String status;     // ACTIVE, WARNING, BANNED
    private Integer postsPerDay;
    private String tags;       // Category/tags
    private Boolean isLocked;  // Whether group is locked
    private String moderationReason;  // Reason for lock/moderation
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
