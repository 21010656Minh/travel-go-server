package api.v2.travel_social_network_server.services.blog;

import api.v2.travel_social_network_server.dtos.blog.BlogReviewDto;
import api.v2.travel_social_network_server.entities.Blog;
import api.v2.travel_social_network_server.entities.BlogReview;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.BlogReviewRepository;
import api.v2.travel_social_network_server.repositories.BlogRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.blog.BlogReviewResponse;
import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogReviewService implements IBlogReviewService {
    
    private final BlogReviewRepository reviewRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public PageableResponse<BlogReviewResponse> getReviewsByBlogId(UUID blogId, int page, int size, String sort, User currentUser) {
        Sort sortStrategy = determineSortStrategy(sort);
        Pageable pageable = PageRequest.of(page, size, sortStrategy);
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + blogId));
        
        Page<BlogReview> reviews = reviewRepository.findAllByBlog(blog, pageable);
        
        List<BlogReviewResponse> content = reviews.getContent().stream()
                .map(review -> toReviewResponse(review, currentUser))
                .toList();
        
        return PageableResponse.<BlogReviewResponse>builder()
                .content(content)
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageableResponse<BlogReviewResponse> getAllReviewsByBlogId(UUID blogId, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + blogId));
        
        Page<BlogReview> reviews = reviewRepository.findAllReviewsByBlog(blog, pageable);
        
        List<BlogReviewResponse> content = reviews.getContent().stream()
                .map(review -> toReviewResponse(review, currentUser))
                .toList();
        
        return PageableResponse.<BlogReviewResponse>builder()
                .content(content)
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional(readOnly = true)
    public PageableResponse<BlogReviewResponse> getReviewsByUserId(UUID userId, int page, int size, User currentUser) {        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Page<BlogReview> reviews = reviewRepository.findAllReviewsByUser(user, pageable);
        
        List<BlogReviewResponse> content = reviews.getContent().stream()
                .map(review -> toReviewResponseWithBlogInfo(review, currentUser))
                .toList();
        
        return PageableResponse.<BlogReviewResponse>builder()
                .content(content)
                .totalElements(reviews.getTotalElements())
                .totalPages(reviews.getTotalPages())
                .build();
    }
    
    @Override
    @Transactional
    public BlogReviewResponse createReviewForBlog(User user, BlogReviewDto reviewDto) {
        log.info("Creating review for blog: {} by user: {}", reviewDto.getBlogId(), user.getUserId());
        
        if (reviewDto.getBlogId() == null) {
            throw new IllegalArgumentException("Blog ID is required");
        }
        
        Blog blog = blogRepository.findById(reviewDto.getBlogId())
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + reviewDto.getBlogId()));
        
        // Check if user already has a review (rating is now always required)
        boolean hasReview = reviewRepository.existsByBlogAndUserAndRatingIsNotNull(blog, user);
        if (hasReview) {
            throw new IllegalStateException("You have already reviewed this blog. You can only submit one review per blog.");
        }
        
        BlogReview review = BlogReview.builder()
                .blog(blog)
                .user(user)
                .content(reviewDto.getContent())
                .rating(reviewDto.getRating())
                .build();
        
        BlogReview savedReview = reviewRepository.save(review);
        
        // Update rating (rating is always provided for reviews)
        Double currentAvgRating = blog.getAverageRating();
        Integer currentTotalRatings = blog.getTotalRatings();
        
        double avgRating = currentAvgRating != null ? currentAvgRating : 0.0;
        int totalRatings = currentTotalRatings != null ? currentTotalRatings : 0;
        
        double currentTotal = avgRating * totalRatings;
        int newTotalRatings = totalRatings + 1;
        double newAverageRating = (currentTotal + reviewDto.getRating()) / newTotalRatings;
        
        blog.setTotalRatings(newTotalRatings);
        blog.setAverageRating(newAverageRating);        blogRepository.save(blog);
        
        log.info("Review created successfully with ID: {}", savedReview.getReviewId());
        return toReviewResponse(savedReview, user);
    }
    
    @Override
    @Transactional
    public BlogReviewResponse updateReview(UUID reviewId, User user, BlogReviewDto reviewDto) {
        log.info("Updating review: {} by user: {}", reviewId, user.getUserId());
        
        BlogReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // Check ownership
        if (!review.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("You are not authorized to update this review");
        }
        
        // Update content
        review.setContent(reviewDto.getContent());
        review.setIsEdited(true);
        
        // Note: Rating cannot be updated after creation to maintain review integrity
        
        BlogReview updatedReview = reviewRepository.save(review);        return toReviewResponse(updatedReview, user);
    }
    
    @Override
    @Transactional
    public void deleteReview(UUID reviewId, User user) {
        log.info("Deleting review: {} by user: {}", reviewId, user.getUserId());
        
        BlogReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with ID: " + reviewId));
        
        // Check permissions: owner of review or owner of blog can delete
        boolean isReviewOwner = review.getUser().getUserId().equals(user.getUserId());
        boolean isBlogOwner = review.getBlog().getUser().getUserId().equals(user.getUserId());
        
        if (!isReviewOwner && !isBlogOwner) {
            throw new SecurityException("You are not authorized to delete this review");
        }
        
        Blog blog = review.getBlog();
        Integer deletedRating = review.getRating();
        
        log.info("Deleting review - ID: {}, Rating: {}, Current blog ratings - Total: {}, Average: {}", 
                reviewId, deletedRating, blog.getTotalRatings(), blog.getAverageRating());
        
        // Delete review
        reviewRepository.delete(review);
        
        // Update rating - Handle null values (rating is always present now)
        Double currentAvgRating = blog.getAverageRating();
        Integer currentTotalRatings = blog.getTotalRatings();
        
        double avgRating = currentAvgRating != null ? currentAvgRating : 0.0;
        int totalRatings = currentTotalRatings != null ? currentTotalRatings : 0;
        
        if (totalRatings > 0) {
            double currentTotal = avgRating * totalRatings;
            int newTotalRatings = totalRatings - 1;
            
            if (newTotalRatings > 0) {
                double newAverageRating = (currentTotal - deletedRating) / newTotalRatings;
                blog.setTotalRatings(newTotalRatings);
                blog.setAverageRating(newAverageRating);            } else {
                blog.setTotalRatings(0);
                blog.setAverageRating(0.0);            }
        }
        
        blogRepository.save(blog);    }
    
    // Helper methods
    
    private Sort determineSortStrategy(String sort) {
        if ("newest".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").descending();
        } else if ("oldest".equalsIgnoreCase(sort)) {
            return Sort.by("createdAt").ascending();
        } else if ("rating".equalsIgnoreCase(sort)) {
            return Sort.by("rating").descending().and(Sort.by("createdAt").descending());
        } else {
            // most_relevant - sort by rating first, then by createdAt
            return Sort.by("rating").descending().and(Sort.by("createdAt").descending());
        }
    }
    
    private BlogReviewResponse toReviewResponse(BlogReview review, User currentUser) {
        return BlogReviewResponse.builder()
                .reviewId(review.getReviewId())
                .blogId(review.getBlog().getBlogId())
                .blogAuthor(toUserSummary(review.getBlog().getUser()))
                .user(toUserSummary(review.getUser()))
                .content(review.getContent())
                .rating(review.getRating())
                .isEdited(review.getIsEdited())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private BlogReviewResponse toReviewResponseWithBlogInfo(BlogReview review, User currentUser) {
        return BlogReviewResponse.builder()
                .reviewId(review.getReviewId())
                .blogId(review.getBlog().getBlogId())
                .blogTitle(review.getBlog().getTitle())
                .blogThumbnailUrl(review.getBlog().getThumbnailUrl())
                .blogAuthor(toUserSummary(review.getBlog().getUser()))
                .user(toUserSummary(review.getUser()))
                .content(review.getContent())
                .rating(review.getRating())
                .isEdited(review.getIsEdited())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
    
    private UserSummaryResponse toUserSummary(User user) {
        String displayName = user.getUsername();
        if (user.getUserProfile() != null && user.getUserProfile().getFullName() != null) {
            displayName = user.getUserProfile().getFullName();
        }
        
        return UserSummaryResponse.builder()
                .userId(user.getUserId())
                .userName(displayName)
                .email(user.getEmail())
                .avatarImg(user.getAvatarImg())
                .build();
    }
}
