package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.user.UpdateUserDto;
import api.v2.travel_social_network_server.dtos.user.UpdateUserImgDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.user.UpdateUserResponse;
import api.v2.travel_social_network_server.responses.user.UserPhotosResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.responses.user.UserVideosResponse;
import api.v2.travel_social_network_server.services.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/users")
@RequiredArgsConstructor
@Tag(name = "User APIs", description = "Endpoints for managing user profiles")
public class UserController {

    private final IUserService userService;

    @Operation(
            summary = "Search users by keyword",
            description = "Search for users by username, email, or other related fields."
    )
    @GetMapping("/search")
    public ResponseEntity<Response<PageableResponse<UserResponse>>> searchUsersByKeyword(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {

        UUID currentUserId = currentUser != null ? currentUser.getUserId() : null;
        PageableResponse<UserResponse> userResponse = userService.searchUsersByKeyword(keyword, page, pageSize, currentUserId);
        return ResponseEntity.ok(Response.success(userResponse, request.getRequestURI(), "Get users successfully"));
    }

    @Operation(
            summary = "Search users with fulltext search",
            description = "Search for users using PostgreSQL fulltext search for better performance and relevance."
    )
    @GetMapping("/search/fulltext")
    public ResponseEntity<Response<PageableResponse<UserResponse>>> searchUsersFulltext(
            @RequestParam("q") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) {

        UUID currentUserId = currentUser != null ? currentUser.getUserId() : null;
        PageableResponse<UserResponse> userResponse = userService.searchUsersFulltext(keyword, page, pageSize, currentUserId);
        return ResponseEntity.ok(Response.success(userResponse, request.getRequestURI(), "Fulltext search completed successfully"));
    }

    @Operation(
            summary = "Get user profile",
            description = "Retrieve the profile information of a specific user by their ID."
    )
    @GetMapping("/{userId}")
    public ResponseEntity<Response<UserResponse>> getUserProfile(
            @PathVariable UUID userId, 
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request) throws IOException {
        UUID currentUserId = currentUser != null ? currentUser.getUserId() : null;
        UserResponse userResponse = userService.getUserProfile(userId, currentUserId);
        return ResponseEntity.ok(Response.success(userResponse, request.getRequestURI(), "Get user profile successfully"));
    }

    @Operation(
            summary = "Update authenticated user's profile",
            description = "Edit profile information of the currently authenticated user."
    )
    @PutMapping("/me")
    public ResponseEntity<Response<UpdateUserResponse>> updateUserProfile(
            @RequestBody UpdateUserDto updateUserDto,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) throws IOException {

        UpdateUserResponse updateUserResponse = userService.updateUserProfile(updateUserDto, user);
        return ResponseEntity.ok(Response.success(updateUserResponse, request.getRequestURI(), "Update profile successfully"));
    }

    @Operation(
            summary = "Get user photos",
            description = "Retrieve all photos of a specific user including avatar, cover, and post images."
    )
    @GetMapping("/{userId}/photos")
    public ResponseEntity<Response<UserPhotosResponse>> getUserPhotos(
            @PathVariable UUID userId,
            HttpServletRequest request) {
        UserPhotosResponse photos = userService.getUserPhotos(userId);
        return ResponseEntity.ok(Response.success(photos, request.getRequestURI(), "Get user photos successfully"));
    }

    @Operation(
            summary = "Get user videos",
            description = "Retrieve all videos of a specific user from their normal posts."
    )
    @GetMapping("/{userId}/videos")
    public ResponseEntity<Response<UserVideosResponse>> getUserVideos(
            @PathVariable UUID userId,
            HttpServletRequest request) {
        UserVideosResponse videos = userService.getUserVideos(userId);
        return ResponseEntity.ok(Response.success(videos, request.getRequestURI(), "Get user videos successfully"));
    }
}
