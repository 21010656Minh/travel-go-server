package api.v2.travel_social_network_server.responses.comment;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostCommentResponse {
    private UUID commentId;
    private UUID userId; // ID of the user who created the comment
    private String content;
    private String fullName;
    private String avatarImg;
    private Instant createdAt;
    private UUID parentCommentId; // ID of parent comment if this is a reply
    private Integer replyCount; // Number of replies to this comment
    private Integer likeCount; // Number of likes on this comment
    private Boolean liked; // Whether current user has liked this comment
}
