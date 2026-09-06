package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.comment.CommentDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.comment.CommentResponse;
import api.v2.travel_social_network_server.responses.comment.CommentLikeResponse;
import api.v2.travel_social_network_server.services.comment.ICommentLikeService;
import api.v2.travel_social_network_server.services.comment.ICommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/comment")
@Tag(name = "Comment APIs", description = "Endpoints for managing comments on posts and watches")
@RequiredArgsConstructor
public class CommentController {

    private final ICommentService commentService;
    private final ICommentLikeService commentLikeService;

    // ========== COMMENT RETRIEVAL ENDPOINTS ==========
    
    @Operation(summary = "Get comments by post ID", description = "Retrieve a paginated list of comments for a specific post")
    @GetMapping("/post/{postId}")
    public ResponseEntity<Response<PageableResponse<CommentResponse>>> getCommentsByPostId(
            @PathVariable UUID postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<CommentResponse> comments = commentService.getCommentsByPostId(postId, page, size, sort, currentUser);
        return ResponseEntity.ok(Response.success(comments, request.getRequestURI(), "Retrieved post comments successfully."));
    }

    @Operation(summary = "Get comments by watch ID", description = "Retrieve a paginated list of comments for a specific watch/video")
    @GetMapping("/watch/{watchId}")
    public ResponseEntity<Response<PageableResponse<CommentResponse>>> getCommentsByWatchId(
            @PathVariable UUID watchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<CommentResponse> comments = commentService.getCommentsByWatchId(watchId, page, size, sort, currentUser);
        return ResponseEntity.ok(Response.success(comments, request.getRequestURI(), "Retrieved watch comments successfully."));
    }

    @Operation(summary = "Get replies by comment ID", description = "Retrieve a paginated list of replies for a given comment")
    @GetMapping("/replies/{commentId}")
    public ResponseEntity<Response<PageableResponse<CommentResponse>>> getRepliesByCommentId(
            @PathVariable UUID commentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<CommentResponse> replies = commentService.getRepliesByCommentId(commentId, page, size, currentUser);
        return ResponseEntity.ok(Response.success(replies, request.getRequestURI(), "Retrieved comment replies successfully."));
    }

    // ========== COMMENT INTERACTION ENDPOINTS ==========
    
    @Operation(summary = "Toggle like on comment", description = "Like or unlike a specific comment by its ID")
    @PutMapping("/like/{commentId}")
    public ResponseEntity<Response<CommentLikeResponse>> toggleLikeOnComment(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        CommentLikeResponse response = commentLikeService.toggleLikeOnComment(commentId, user);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Comment like toggled successfully."));
    }

    // ========== COMMENT MANAGEMENT ENDPOINTS ==========
    
    @Operation(summary = "Create a new comment", description = "Add a new comment to a post or watch")
    @PostMapping
    public ResponseEntity<Response<CommentResponse>> createCommentForContent(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody CommentDto commentDto,
            HttpServletRequest request
    ) throws IOException {
        CommentResponse comment = commentService.createCommentForContent(user, commentDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(comment, request.getRequestURI(), "Comment created successfully."));
    }

    @Operation(summary = "Update a comment", description = "Update an existing comment by its ID")
    @PatchMapping("/{commentId}")
    public ResponseEntity<Response<CommentResponse>> updateCommentContent(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User user,
            @RequestBody CommentDto commentDto,
            HttpServletRequest request
    ) {
        CommentResponse updated = commentService.updateCommentContent(commentId, user, commentDto);
        return ResponseEntity.ok(Response.success(updated, request.getRequestURI(), "Comment updated successfully."));
    }

    @Operation(summary = "Delete a comment", description = "Delete a comment by its ID")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Response<String>> deleteCommentById(
            @PathVariable UUID commentId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        commentService.deleteCommentById(commentId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Comment deleted successfully."));
    }
}
