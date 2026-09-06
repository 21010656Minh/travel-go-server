package api.v2.travel_social_network_server.services.friendship;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.friendship.FriendshipResponse;
import api.v2.travel_social_network_server.responses.friendship.UserFriendshipListsResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IFriendshipService {
    void sendFriendRequest(UUID requesterId, UUID receiverId);
    void acceptFriendRequest(UUID friendshipId, UUID receiverId);
    void rejectFriendRequest(UUID friendshipId, UUID receiverId);
    List<UserResponse> getFriends(UUID userId);
    void unfriend(UUID userId, UUID friendId);
    PageableResponse<FriendshipResponse> suggestFriends(UUID userId, int page, int size);
    PageableResponse<FriendshipResponse> getSuggestedFriendsOfFriends(UUID userId, int page, int size);
    PageableResponse<FriendshipResponse> getPendingRequests(UUID userId, int page, int size);
    UserFriendshipListsResponse getUserFriendshipLists(UUID userId);
    List<UserResponse> getBlockedUsers(UUID userId);
    List<UserResponse> getFriendsBirthdays(UUID userId);
    void blockUser(UUID userId, UUID blockedUserId);
    void unblockUser(UUID userId, UUID unblockedUserId);
}
