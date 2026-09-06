package api.v2.travel_social_network_server.responses.presence;

import lombok.*;

import java.time.Instant;

/**
 * Lightweight summary of a user currently online.
 * Returned by GET /presence/online.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnlineUserResponse {
    private String userId;
    private String userName;
    private String fullName;
    private String avatarImg;
    private Instant lastSeenAt;
}
