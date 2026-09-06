package api.v2.travel_social_network_server.responses.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupMemberResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String avatar;
    private String role;  // ADMIN, MODERATOR, MEMBER
    private String status; // PENDING, APPROVED
    private Boolean isFriend;
    private Integer postsCount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant joinedAt;
}
