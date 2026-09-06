package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.friendship.FriendShipRequestDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.friendship.FriendshipResponse;
import api.v2.travel_social_network_server.responses.friendship.UserFriendshipListsResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.services.friendship.IFriendshipService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/friendship")
@RequiredArgsConstructor
@Tag(name = "Friendship APIs", description = "Endpoints for managing friendships and friend requests")
public class FriendshipController {

    private final IFriendshipService friendshipService;

    @Operation(summary = "Suggest friends", description = "Get list of users who are not friends with the authenticated user")
    @GetMapping("/suggestions")
    public ResponseEntity<Response<PageableResponse<FriendshipResponse>>> suggestFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<FriendshipResponse> suggestions = friendshipService.suggestFriends(user.getUserId(), page, size);
        return ResponseEntity.ok(Response.success(suggestions, request.getRequestURI(), "Get friend suggestions successfully"));
    }

    @Operation(summary = "Suggest friends of friends", description = "Get list of 20 friends of friends who are not yet connected with the authenticated user")
    @GetMapping("/suggestions/friends-of-friends")
    public ResponseEntity<Response<PageableResponse<FriendshipResponse>>> suggestFriendsOfFriends(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<FriendshipResponse> suggestions = friendshipService.getSuggestedFriendsOfFriends(user.getUserId(), page, size);
        return ResponseEntity.ok(Response.success(suggestions, request.getRequestURI(), "Get friends of friends suggestions successfully"));
    }

    @Operation(summary = "Send friend request", description = "Send a friend request from authenticated user to another user")
    @PostMapping("/request")
    public ResponseEntity<Response<String>> sendFriendRequest(
            @Valid @RequestBody FriendShipRequestDto dto,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        friendshipService.sendFriendRequest(user.getUserId(), dto.getReceiverId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Friend request sent successfully"));
    }

    @Operation(summary = "Accept friend request", description = "Accept a pending friend request by friendship ID")
    @PostMapping("/{friendshipId}/accept")
    public ResponseEntity<Response<String>> acceptFriendRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        friendshipService.acceptFriendRequest(friendshipId, user.getUserId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Friend request accepted successfully"));
    }

    @Operation(summary = "Reject friend request", description = "Reject a pending friend request by friendship ID")
    @PostMapping("/{friendshipId}/reject")
    public ResponseEntity<Response<String>> rejectFriendRequest(
            @PathVariable UUID friendshipId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        friendshipService.rejectFriendRequest(friendshipId, user.getUserId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Friend request rejected successfully"));
    }

    @Operation(summary = "Get my friends", description = "Retrieve a list of friends for the authenticated user")
    @GetMapping("/friends")
    public ResponseEntity<Response<List<UserResponse>>> getMyFriends(
            @AuthenticationPrincipal User user, 
            HttpServletRequest request
    ) {
        List<UserResponse> friends = friendshipService.getFriends(user.getUserId());
        return ResponseEntity.ok(Response.success(friends, request.getRequestURI(), "Get friends successfully"));
    }

    @Operation(summary = "Unfriend a user", description = "Remove a friendship between authenticated user and another user")
    @DeleteMapping("/unfriend/{friendId}")
    public ResponseEntity<Response<String>> unfriend(
            @PathVariable UUID friendId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        friendshipService.unfriend(user.getUserId(), friendId);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Unfriend successfully"));
    }

    @Operation(summary = "Get pending friend requests", description = "Retrieve all pending friend requests for the authenticated user with pagination")
    @GetMapping("/requests/pending")
    public ResponseEntity<Response<PageableResponse<FriendshipResponse>>> getPendingRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<FriendshipResponse> requests = friendshipService.getPendingRequests(user.getUserId(), page, size);
        return ResponseEntity.ok(Response.success(requests, request.getRequestURI(), "Get pending requests successfully"));
    }

    @Operation(summary = "Get user friendship lists", description = "Retrieve pending requests, friends, and blocked users for a specific user")
    @GetMapping("/users/{userId}/lists")
    public ResponseEntity<Response<UserFriendshipListsResponse>> getUserFriendshipLists(
            @PathVariable UUID userId,
            HttpServletRequest request
    ) {
        UserFriendshipListsResponse lists = friendshipService.getUserFriendshipLists(userId);
        return ResponseEntity.ok(Response.success(lists, request.getRequestURI(), "Get user friendship lists successfully"));
    }

    @Operation(summary = "Get friends birthdays", description = "Retrieve friends with upcoming birthdays sorted by date")
    @GetMapping("/birthdays")
    public ResponseEntity<Response<List<UserResponse>>> getFriendsBirthdays(
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        List<UserResponse> birthdays = friendshipService.getFriendsBirthdays(user.getUserId());
        return ResponseEntity.ok(Response.success(birthdays, request.getRequestURI(), "Get friends birthdays successfully"));
    }

    @Operation(summary = "Block a user", description = "Block a user to prevent them from viewing your content")
    @PostMapping("/block/{userId}")
    public ResponseEntity<Response<Object>> blockUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        friendshipService.blockUser(user.getUserId(), userId);
        return ResponseEntity.ok(Response.success(
                new java.util.HashMap<String, String>() {{
                    put("friendshipStatus", "BLOCKED");
                }},
                request.getRequestURI(),
                "User blocked successfully"
        ));
    }

    @Operation(summary = "Unblock a user", description = "Unblock a user to allow them to view your content again")
    @DeleteMapping("/unblock/{userId}")
    public ResponseEntity<Response<Object>> unblockUser(
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        friendshipService.unblockUser(user.getUserId(), userId);
        return ResponseEntity.ok(Response.success(
                new java.util.HashMap<String, String>() {{
                    put("friendshipStatus", null);
                }},
                request.getRequestURI(),
                "User unblocked successfully"
        ));
    }

}
