package api.v2.travel_social_network_server.services.blog;

import api.v2.travel_social_network_server.dtos.blog.BlogDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.blog.BlogResponse;

import java.util.UUID;

public interface IBlogService {
    
    // Create blog
    BlogResponse createBlog(User user, BlogDto blogDto);
    
    // Update blog
    BlogResponse updateBlog(UUID blogId, User user, BlogDto blogDto);
    
    // Delete blog
    void deleteBlog(UUID blogId, User user);
    
    // Get blog by ID
    BlogResponse getBlogById(UUID blogId, User currentUser);
    
    // Get all published blogs
    PageableResponse<BlogResponse> getAllBlogs(int page, int size, String sort, User currentUser);
    
    // Get blogs by user
    PageableResponse<BlogResponse> getBlogsByUser(UUID userId, int page, int size, User currentUser);
    
    // Get my blogs
    PageableResponse<BlogResponse> getMyBlogs(User user, int page, int size, String status);
    
    // Get featured blogs
    PageableResponse<BlogResponse> getFeaturedBlogs(int page, int size, User currentUser);
    
    // Search blogs
    PageableResponse<BlogResponse> searchBlogs(String query, int page, int size, User currentUser);
    
    // Search blogs with fulltext
    PageableResponse<BlogResponse> searchBlogsFulltext(String query, int page, int size, User currentUser);
    
    // Search blogs by location
    PageableResponse<BlogResponse> searchBlogsByLocation(String location, int page, int size, User currentUser);
    
    // Get blogs by tag
    PageableResponse<BlogResponse> getBlogsByTag(UUID tagId, int page, int size, User currentUser);
    
    // Get trending blogs
    PageableResponse<BlogResponse> getTrendingBlogs(int page, int size, User currentUser);
    
    // Get popular blogs
    PageableResponse<BlogResponse> getPopularBlogs(int page, int size, User currentUser);
    
    // Increment view count
    void incrementViewCount(UUID blogId);
}
