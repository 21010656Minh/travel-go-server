package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.blog.BlogDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.blog.BlogResponse;
import api.v2.travel_social_network_server.services.blog.IBlogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/blogs")
@Tag(name = "Blog APIs", description = "Endpoints for managing travel blogs")
@RequiredArgsConstructor
public class BlogController {
    
    private final IBlogService blogService;
    
    // ========== BLOG RETRIEVAL ENDPOINTS ==========
    
    @Operation(summary = "Get all published blogs", description = "Retrieve a paginated list of all published blogs")
    @GetMapping
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getAllBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getAllBlogs(page, size, sort, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved blogs successfully."));
    }
    
    @Operation(summary = "Get blog by ID", description = "Retrieve a specific blog by its ID and increment view count")
    @GetMapping("/{blogId}")
    public ResponseEntity<Response<BlogResponse>> getBlogById(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        BlogResponse blog = blogService.getBlogById(blogId, currentUser);
        blogService.incrementViewCount(blogId);
        return ResponseEntity.ok(Response.success(blog, request.getRequestURI(), "Retrieved blog successfully."));
    }
    
    @Operation(summary = "Get blogs by user", description = "Retrieve all blogs created by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getBlogsByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getBlogsByUser(userId, page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved user blogs successfully."));
    }
    
    @Operation(summary = "Get my blogs", description = "Retrieve all blogs created by the authenticated user")
    @GetMapping("/me")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getMyBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getMyBlogs(user, page, size, status);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved your blogs successfully."));
    }
    
    @Operation(summary = "Get featured blogs", description = "Retrieve featured blogs")
    @GetMapping("/featured")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getFeaturedBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getFeaturedBlogs(page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved featured blogs successfully."));
    }
    
    @Operation(summary = "Get trending blogs", description = "Retrieve trending blogs sorted by view count")
    @GetMapping("/trending")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getTrendingBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getTrendingBlogs(page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved trending blogs successfully."));
    }
    
    @Operation(summary = "Get popular blogs", description = "Retrieve popular blogs sorted by rating")
    @GetMapping("/popular")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getPopularBlogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getPopularBlogs(page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved popular blogs successfully."));
    }
    
    @Operation(summary = "Search blogs", description = "Search blogs by title or content")
    @GetMapping("/search")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> searchBlogs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.searchBlogs(query, page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Search completed successfully."));
    }
    
    @Operation(summary = "Search blogs with fulltext", description = "Search blogs using PostgreSQL fulltext search")
    @GetMapping("/search/fulltext")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> searchBlogsFulltext(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.searchBlogsFulltext(query, page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Fulltext search completed successfully."));
    }
    
    @Operation(summary = "Search blogs by location", description = "Search blogs by location")
    @GetMapping("/search/location")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> searchBlogsByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.searchBlogsByLocation(location, page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Location search completed successfully."));
    }
    
    @Operation(summary = "Get blogs by tag", description = "Retrieve blogs filtered by tag")
    @GetMapping("/tag/{tagId}")
    public ResponseEntity<Response<PageableResponse<BlogResponse>>> getBlogsByTag(
            @PathVariable UUID tagId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogResponse> blogs = blogService.getBlogsByTag(tagId, page, size, currentUser);
        return ResponseEntity.ok(Response.success(blogs, request.getRequestURI(), "Retrieved blogs by tag successfully."));
    }
    
    // ========== BLOG MANAGEMENT ENDPOINTS ==========
    
    @Operation(summary = "Create a new blog with file upload", description = "Create a new travel blog with multipart form data")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Response<BlogResponse>> createBlogWithFiles(
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid BlogDto blogDto,
            HttpServletRequest request
    ) {
        BlogResponse blog = blogService.createBlog(user, blogDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(blog, request.getRequestURI(), "Blog created successfully."));
    }
    
    @Operation(summary = "Create a new blog", description = "Create a new travel blog with JSON")
    @PostMapping(consumes = "application/json")
    public ResponseEntity<Response<BlogResponse>> createBlog(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BlogDto blogDto,
            HttpServletRequest request
    ) {
        BlogResponse blog = blogService.createBlog(user, blogDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(blog, request.getRequestURI(), "Blog created successfully."));
    }
    
    @Operation(summary = "Update blog with file upload", description = "Update an existing blog with multipart form data")
    @PutMapping(value = "/{blogId}", consumes = "multipart/form-data")
    public ResponseEntity<Response<BlogResponse>> updateBlogWithFiles(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal User user,
            @ModelAttribute @Valid BlogDto blogDto,
            HttpServletRequest request
    ) {
        BlogResponse blog = blogService.updateBlog(blogId, user, blogDto);
        return ResponseEntity.ok(Response.success(blog, request.getRequestURI(), "Blog updated successfully."));
    }
    
    @Operation(summary = "Update blog", description = "Update an existing blog with JSON")
    @PutMapping(value = "/{blogId}", consumes = "application/json")
    public ResponseEntity<Response<BlogResponse>> updateBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BlogDto blogDto,
            HttpServletRequest request
    ) {
        BlogResponse blog = blogService.updateBlog(blogId, user, blogDto);
        return ResponseEntity.ok(Response.success(blog, request.getRequestURI(), "Blog updated successfully."));
    }
    
    @Operation(summary = "Delete blog", description = "Delete a blog")
    @DeleteMapping("/{blogId}")
    public ResponseEntity<Response<Void>> deleteBlog(
            @PathVariable UUID blogId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        blogService.deleteBlog(blogId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Blog deleted successfully."));
    }
}
