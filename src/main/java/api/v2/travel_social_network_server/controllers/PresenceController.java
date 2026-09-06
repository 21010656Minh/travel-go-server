package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.presence.OnlineUserResponse;
import api.v2.travel_social_network_server.services.presence.IPresenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * REST endpoints for presence (online/offline) tracking.
 * <p>
 * Authenticated users can fetch the list of currently online users,
 * optionally filtered to a specific subset of userIds (e.g. their friends).
 */
@RestController
@RequestMapping("${api.base-url}/presence")
@RequiredArgsConstructor
@Tag(name = "Presence APIs", description = "Endpoints for online/offline user presence")
public class PresenceController {

    private final IPresenceService presenceService;

    /**
     * Fetch all currently online users with profile metadata.
     */
    @Operation(summary = "List online users",
            description = "Get the list of userIds, userNames, fullNames and avatars of users currently online.")
    @GetMapping("/online")
    public ResponseEntity<Response<List<OnlineUserResponse>>> getOnlineUsers(
            HttpServletRequest request
    ) {
        List<OnlineUserResponse> online = presenceService.getOnlineUsers();
        return ResponseEntity.ok(Response.success(online, request.getRequestURI(), "Fetched online users"));
    }

    /**
     * Check whether a specific user is online.
     */
    @Operation(summary = "Check user online status")
    @GetMapping("/status")
    public ResponseEntity<Response<Boolean>> isOnline(
            @RequestParam UUID userId,
            HttpServletRequest request
    ) {
        boolean online = presenceService.isOnline(userId);
        return ResponseEntity.ok(Response.success(online, request.getRequestURI(), "Presence status"));
    }

    /**
     * Get the authenticated user's own session metadata (online). Useful for
     * the client to confirm its session is recognized.
     */
    @Operation(summary = "My presence status")
    @GetMapping("/me")
    public ResponseEntity<Response<Boolean>> myPresence(
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        boolean online = presenceService.isOnline(user.getUserId());
        return ResponseEntity.ok(Response.success(online, request.getRequestURI(), "My presence status"));
    }
}
