package api.v2.travel_social_network_server.services.blog;

import api.v2.travel_social_network_server.dtos.blog.BlogReviewDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.blog.BlogReviewResponse;

import java.util.UUID;

public interface IBlogReviewService {
    PageableResponse<BlogReviewResponse> getReviewsByBlogId(UUID blogId, int page, int size, String sort, User currentUser);
    PageableResponse<BlogReviewResponse> getAllReviewsByBlogId(UUID blogId, int page, int size, User currentUser);
    PageableResponse<BlogReviewResponse> getReviewsByUserId(UUID userId, int page, int size, User currentUser);
    BlogReviewResponse createReviewForBlog(User user, BlogReviewDto reviewDto);
    BlogReviewResponse updateReview(UUID reviewId, User user, BlogReviewDto reviewDto);
    void deleteReview(UUID reviewId, User user);
}
