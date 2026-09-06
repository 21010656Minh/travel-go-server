package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.post.UpdatePostDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.services.post.ICreatePostService;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/post")
@Tag(name = "Post APIs", description = "Endpoints for creating, retrieving, updating and deleting posts")
public class PostController {

    private final ICreatePostService createPostService;
    private final IPostService postService;

    public PostController(
            @Qualifier("createPostServiceDecorator") ICreatePostService createPostService,
            @Qualifier("postService") IPostService postService
    ) {
        this.createPostService = createPostService;
        this.postService = postService;
    }

    @Operation(summary = "Get posts by user", description = "Retrieve posts created by a specific user. If the authenticated user matches the requested user, all posts are returned; otherwise only public posts are returned.")
    @GetMapping("/{userId}")
    public ResponseEntity<Response<PageableResponse<PostResponse>>> getPostsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<PostResponse> postPage;
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            postPage = postService.getPostsByUser(currentUser, page, size);
        } else {
            User user = new User();
            user.setUserId(userId);
            postPage = postService.getPostsByPrivacyAndUser(PrivacyTypeEnum.PUBLIC, user, page, size);
        }
        return ResponseEntity.ok(Response.success(postPage, request.getRequestURI(), "Search posts successfully."));
    }

    @Operation(summary = "Get post by ID", description = "Retrieve a single post by its ID. Returns the post with all its details including media, comments count, likes count, and privacy settings.")
    @GetMapping("/detail/{postId}")
    public ResponseEntity<Response<PostResponse>> getPostById(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PostResponse post = postService.getPostById(postId, user);
        return ResponseEntity.ok(Response.success(post, request.getRequestURI(), "Post retrieved successfully."));
    }

    @Operation(summary = "Get public posts", description = "Retrieve all public posts with pagination.")
    @GetMapping
    public ResponseEntity<Response<PageableResponse<PostResponse>>> getPostsByStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<PostResponse> postPage = postService.getPostsByPrivacy(user, PrivacyTypeEnum.PUBLIC, page, size);
        return ResponseEntity.ok(Response.success(postPage, request.getRequestURI(), "Search posts successfully."));
    }

    @Operation(summary = "Get posts by group", description = "Retrieve all posts within a specific group.")
    @GetMapping("/group/{groupId}")
    public ResponseEntity<Response<PageableResponse<PostResponse>>> getPostsByGroup(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "new_post") String sort,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<PostResponse> postPage = postService.getPostsByGroup(groupId, user, page, size, sort);
        return ResponseEntity.ok(Response.success(postPage, request.getRequestURI(), "Search group posts successfully."));
    }

    @Operation(summary = "Create post on user profile", description = "Create a new post on the authenticated user's own profile. Supports text and media (multipart/form-data).")
    @PostMapping(value ="/me", consumes = "multipart/form-data")
    public ResponseEntity<Response<PostResponse>> createPost(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute UpdatePostDto updatePostDto,
            HttpServletRequest request
    ) throws IOException {
        PostResponse post = createPostService.createPostMultiTask(user, updatePostDto, null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(post, request.getRequestURI(), "Post successfully created."));
    }

    @Operation(summary = "Create post in group", description = "Create a new post inside a group. Supports text and media (multipart/form-data).")
    @PostMapping(value = "/group/{groupId}", consumes = "multipart/form-data")
    public ResponseEntity<Response<PostResponse>> createPostOnGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            @ModelAttribute UpdatePostDto updatePostDto,
            HttpServletRequest request
    ) throws IOException {
        System.out.println(user);
        PostResponse post = postService.createPostMultiTask(user, updatePostDto, groupId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(post, request.getRequestURI(), "Post successfully created on group."));
    }

    @Operation(summary = "Delete post", description = "Delete a post by its ID. Only the owner of the post or admins can perform this action.")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Response<String>> deletePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        postService.deletePost(postId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Post deleted successfully"));
    }

    @Operation(summary = "Update post", description = "Update post content, location, privacy, tags, or add new media. Only the owner can update the post.")
    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public ResponseEntity<Response<PostResponse>> updatePost(
            @PathVariable UUID postId,
            @AuthenticationPrincipal User user,
            @ModelAttribute UpdatePostDto updatePostDto,
            HttpServletRequest request
    ) throws IOException {
        PostResponse post = postService.updatePost(postId, updatePostDto, user);
        return ResponseEntity.ok(Response.success(post, request.getRequestURI(), "Post updated successfully"));
    }

    @Operation(summary = "Update post privacy", description = "Update the privacy setting (PUBLIC, FRIENDS_ONLY, PRIVATE) of a specific post.")
    @PatchMapping("/{postId}/privacy")
    public ResponseEntity<Response<PostResponse>> updatePostPrivacy(
            @PathVariable UUID postId,
            @RequestParam PrivacyTypeEnum privacy,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PostResponse post = postService.updatePostPrivacy(postId, privacy, user);
        return ResponseEntity.ok(Response.success(post, request.getRequestURI(), "Privacy updated successfully"));
    }

    @Operation(summary = "Search posts in group", description = "Search posts inside a group by keyword with pagination.")
    @GetMapping("/group/{groupId}/search")
    public ResponseEntity<Response<PageableResponse<PostResponse>>> searchPostsInGroup(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<PostResponse> posts = postService.searchPostsInGroup(user, groupId, keyword, page, size);
        return ResponseEntity.ok(Response.success(posts, request.getRequestURI(), "Search posts successfully"));
    }

    @Operation(summary = "Share a post", description = "Share an existing post to your timeline with optional additional content and privacy settings.")
    @PostMapping("/{postId}/share")
    public ResponseEntity<Response<PostResponse>> sharePost(
            @PathVariable UUID postId,
            @RequestParam(required = false) String content,
            @RequestParam PrivacyTypeEnum privacy,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PostResponse sharedPost = postService.sharePost(postId, content, privacy, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(sharedPost, request.getRequestURI(), "Post shared successfully"));
    }
}
