package api.v2.travel_social_network_server.responses.group;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendMemberDto {
    private UUID userId;
    private String name;
    private String avatar;
}
