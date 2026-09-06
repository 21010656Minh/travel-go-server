package api.v2.travel_social_network_server.responses.comment;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentLikeResponse {
    private UUID commentId;
    private Integer likeCount;
    private boolean liked;
}
