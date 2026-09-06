package api.v2.travel_social_network_server.responses.like;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContentLikeResponse {
    private UUID contentId; // Can be postId or watchId
    private Integer likeCount;
    private boolean liked;
}
