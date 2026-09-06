package api.v2.travel_social_network_server.responses.friendship;

import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.utilities.enums.FriendShipTypeEnum;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FriendshipResponse {
    private UUID friendshipId;
    private UUID requesterId;
    private UUID receiverId;
    private UserResponse friendProfile;
    private FriendShipTypeEnum status;
    private Instant createdAt;
}