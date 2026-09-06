package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.blog.BlogResponse;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.services.blog.IBlogService;
import api.v2.travel_social_network_server.services.group.IGroupService;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.services.user.IUserService;
import api.v2.travel_social_network_server.services.suggestion.search.ISearchSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Global search API for users, groups, posts, and blogs")
public class SearchController {

        private final IUserService userService;
        private final IGroupService groupService;
        private final IPostService postService;
        private final IBlogService blogService;
        private final ISearchSuggestionService searchSuggestionService;

        @Operation(summary = "Global search", description = "Search across users, groups, and posts using fulltext search. Returns combined results with limited items per category.")
        @GetMapping
        public ResponseEntity<Response<Map<String, Object>>> globalSearch(
                        @RequestParam("q") String keyword,
                        @RequestParam(defaultValue = "5") int limit,
                        @AuthenticationPrincipal User currentUser,
                        HttpServletRequest request) {
                UUID currentUserId = currentUser != null ? currentUser.getUserId() : null;

                // Search với limit cho mỗi loại
                PageableResponse<UserResponse> users = userService.searchUsersFulltext(keyword, 0, limit,
                                currentUserId);
                PageableResponse<GroupResponse> groups = groupService.searchGroupsFulltext(keyword, 0, limit,
                                currentUser);
                PageableResponse<PostResponse> posts = postService.searchPostsFulltext(keyword, 0, limit);

                Map<String, Object> result = new HashMap<>();
                result.put("users", users);
                result.put("groups", groups);
                result.put("posts", posts);

                return ResponseEntity
                                .ok(Response.success(result, request.getRequestURI(), "Search completed successfully"));
        }

        @Operation(summary = "Search users only", description = "Search users with pagination support")
        @GetMapping("/users")
        public ResponseEntity<Response<PageableResponse<UserResponse>>> searchUsers(
                        @RequestParam("q") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @AuthenticationPrincipal User currentUser,
                        HttpServletRequest request) {
                UUID currentUserId = currentUser != null ? currentUser.getUserId() : null;
                PageableResponse<UserResponse> users = userService.searchUsersFulltext(keyword, page, size,
                                currentUserId);

                return ResponseEntity.ok(Response.success(users, request.getRequestURI(), "Users search completed"));
        }

        @Operation(summary = "Search groups only", description = "Search groups with pagination support")
        @GetMapping("/groups")
        public ResponseEntity<Response<PageableResponse<GroupResponse>>> searchGroups(
                        @RequestParam("q") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @AuthenticationPrincipal User currentUser,
                        HttpServletRequest request) {
                PageableResponse<GroupResponse> groups = groupService.searchGroupsFulltext(keyword, page, size,
                                currentUser);

                return ResponseEntity.ok(Response.success(groups, request.getRequestURI(), "Groups search completed"));
        }

        @Operation(summary = "Search posts only", description = "Search posts with pagination support")
        @GetMapping("/posts")
        public ResponseEntity<Response<PageableResponse<PostResponse>>> searchPosts(
                        @RequestParam("q") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @AuthenticationPrincipal User currentUser,
                        HttpServletRequest request) {
                PageableResponse<PostResponse> posts = postService.searchPostsFulltext(keyword, page, size);

                return ResponseEntity.ok(Response.success(posts, request.getRequestURI(), "Posts search completed"));
        }

        @Operation(summary = "Search blogs only", description = "Search blogs with pagination support using fulltext search")
        @GetMapping("/blogs")
        public ResponseEntity<Response<PageableResponse<BlogResponse>>> searchBlogs(
                        @RequestParam("q") String keyword,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @AuthenticationPrincipal User currentUser,
                        HttpServletRequest request) {
                PageableResponse<BlogResponse> blogs = blogService.searchBlogsFulltext(keyword, page, size, currentUser);

                return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Blogs search completed"));
        }

        @Operation(summary = "Search suggestions", description = "Get quick search suggestions for autocomplete (limited results)")
        @GetMapping("/suggestions")
        public ResponseEntity<Response<Map<String, Object>>> searchSuggestions(
                        @RequestParam("q") String keyword,
                        @AuthenticationPrincipal User currentUser,
                        HttpServletRequest request) {
                // Lưu keyword vào Redis thông qua decorator
                searchSuggestionService.searchSuggestions(currentUser, keyword, 0, 1);

                UUID currentUserId = currentUser != null ? currentUser.getUserId() : null;

                PageableResponse<UserResponse> users = userService.searchUsersFulltext(keyword, 0, 3, currentUserId);
                PageableResponse<GroupResponse> groups = groupService.searchGroupsFulltext(keyword, 0, 3, currentUser);
                PageableResponse<PostResponse> posts = postService.searchPostsFulltext(keyword, 0, 3);

                Map<String, Object> result = new HashMap<>();
                result.put("users", users.getContent());
                result.put("groups", groups.getContent());
                result.put("posts", posts.getContent());

                return ResponseEntity.ok(Response.success(result, request.getRequestURI(),
                                "Suggestions retrieved successfully"));
        }
}
