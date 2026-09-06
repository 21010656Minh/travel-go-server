package api.v2.travel_social_network_server.responses.friendship;

import api.v2.travel_social_network_server.responses.user.UserResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFriendshipListsResponse {
    private List<FriendshipResponse> pendingRequests;  // Lời mời kết bạn nhận được
    private List<UserResponse> friends;                // Danh sách bạn bè
    private List<UserResponse> blockedUsers;           // Danh sách người đã chặn
}
