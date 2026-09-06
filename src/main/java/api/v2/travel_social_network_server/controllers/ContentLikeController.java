package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.like.ContentLikeResponse;
import api.v2.travel_social_network_server.services.like.IContentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/like")
@RequiredArgsConstructor
@Tag(name = "Content Like APIs", description = "Endpoints for liking posts and watches")
public class ContentLikeController {

    private final IContentLikeService contentLikeService;

    @Operation(
            summary = "Toggle like on post",
            description = "Like or unlike a specific post by its ID. "
                    + "If already liked, the like will be removed; otherwise, a new like will be added."
    )
    @PutMapping("/post/{postId}")
    public ResponseEntity<Response<ContentLikeResponse>> toggleLikeOnPost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        ContentLikeResponse response = contentLikeService.toggleLikeOnPost(postId, user);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Post like toggled successfully."));
    }

    @Operation(
            summary = "Toggle like on watch",
            description = "Like or unlike a specific watch/video by its ID. "
                    + "If already liked, the like will be removed; otherwise, a new like will be added."
    )
    @PutMapping("/watch/{watchId}")
    public ResponseEntity<Response<ContentLikeResponse>> toggleLikeOnWatch(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        ContentLikeResponse response = contentLikeService.toggleLikeOnWatch(watchId, user);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Watch like toggled successfully."));
    }
}
