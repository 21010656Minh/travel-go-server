package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.blog.BlogReviewDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.blog.BlogReviewResponse;
import api.v2.travel_social_network_server.services.blog.IBlogReviewService;
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
@RequestMapping("${api.base-url}/blogs/reviews")
@Tag(name = "Blog Review APIs", description = "Endpoints for managing blog reviews")
@RequiredArgsConstructor
public class BlogReviewController {
    
    private final IBlogReviewService blogReviewService;
    
    // ========== REVIEW RETRIEVAL ENDPOINTS ==========
    
    @Operation(summary = "Get reviews by blog ID", description = "Retrieve a paginated list of reviews for a specific blog")
    @GetMapping("/blog/{blogId}")
    public ResponseEntity<Response<PageableResponse<BlogReviewResponse>>> getReviewsByBlogId(
            @PathVariable UUID blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sort,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogReviewResponse> reviews = blogReviewService.getReviewsByBlogId(blogId, page, size, sort, currentUser);
        return ResponseEntity.ok(Response.success(reviews, request.getRequestURI(), "Retrieved blog reviews successfully."));
    }
    
    @Operation(summary = "Get all reviews by blog ID", description = "Retrieve a paginated list of reviews (with ratings) for a specific blog")
    @GetMapping("/blog/{blogId}/all")
    public ResponseEntity<Response<PageableResponse<BlogReviewResponse>>> getAllReviewsByBlogId(
            @PathVariable UUID blogId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogReviewResponse> reviews = blogReviewService.getAllReviewsByBlogId(blogId, page, size, currentUser);
        return ResponseEntity.ok(Response.success(reviews, request.getRequestURI(), "Retrieved blog reviews successfully."));
    }
    
    @Operation(summary = "Get reviews by user ID", description = "Retrieve a paginated list of reviews created by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Response<PageableResponse<BlogReviewResponse>>> getReviewsByUserId(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        PageableResponse<BlogReviewResponse> reviews = blogReviewService.getReviewsByUserId(userId, page, size, currentUser);
        return ResponseEntity.ok(Response.success(reviews, request.getRequestURI(), "Retrieved user reviews successfully."));
    }
    
    // ========== REVIEW MANAGEMENT ENDPOINTS ==========
    
    @Operation(summary = "Create a new review", description = "Add a new review to a blog. Include rating (1-5).")
    @PostMapping
    public ResponseEntity<Response<BlogReviewResponse>> createReview(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BlogReviewDto reviewDto,
            HttpServletRequest request
    ) {
        BlogReviewResponse review = blogReviewService.createReviewForBlog(user, reviewDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(review, request.getRequestURI(), "Review created successfully."));
    }
    
    @Operation(summary = "Update review", description = "Update an existing review. Note: Rating cannot be changed after creation.")
    @PutMapping("/{reviewId}")
    public ResponseEntity<Response<BlogReviewResponse>> updateReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal User user,
            @Valid @RequestBody BlogReviewDto reviewDto,
            HttpServletRequest request
    ) {
        BlogReviewResponse review = blogReviewService.updateReview(reviewId, user, reviewDto);
        return ResponseEntity.ok(Response.success(review, request.getRequestURI(), "Review updated successfully."));
    }
    
    @Operation(summary = "Delete review", description = "Delete a review. Can be deleted by review owner or blog owner.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<Response<Void>> deleteReview(
            @PathVariable UUID reviewId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        blogReviewService.deleteReview(reviewId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Review deleted successfully."));
    }
}
