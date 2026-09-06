package api.v2.travel_social_network_server.responses.user;

import api.v2.travel_social_network_server.utilities.enums.FriendShipTypeEnum;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UserResponse extends UpdateUserResponse {
    private String avatarImg;
    private String coverImg;
    private FriendShipTypeEnum friendshipStatus;
    private Long postsCount;
    private Long friendsCount;
}