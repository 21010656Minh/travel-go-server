package api.v2.travel_social_network_server.responses.user;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSummaryResponse {
    private UUID userId;
    private String userName;
    private String email;
    private String avatarImg;
}
