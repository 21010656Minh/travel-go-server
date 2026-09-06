package api.v2.travel_social_network_server.responses.user;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserMediaResponse {
    private String mediaId;
    private String url;
    private LocalDateTime createdAt;
    private String postId;
}
