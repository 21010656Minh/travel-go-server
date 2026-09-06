package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Blog;
import api.v2.travel_social_network_server.entities.BlogReview;
import api.v2.travel_social_network_server.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlogReviewRepository extends JpaRepository<BlogReview, UUID> {
    
    // Get all reviews for Blog
    @Query("SELECT r FROM BlogReview r WHERE r.blog = :blog ORDER BY r.createdAt DESC")
    Page<BlogReview> findAllByBlog(@Param("blog") Blog blog, Pageable pageable);
    
    // Get all reviews for Blog (reviews with rating)
    @Query("SELECT r FROM BlogReview r WHERE r.blog = :blog AND r.rating IS NOT NULL ORDER BY r.createdAt DESC")
    Page<BlogReview> findAllReviewsByBlog(@Param("blog") Blog blog, Pageable pageable);
    
    // Check if user has already reviewed this blog
    @Query("SELECT COUNT(r) > 0 FROM BlogReview r WHERE r.blog = :blog AND r.user = :user AND r.rating IS NOT NULL")
    boolean existsByBlogAndUserAndRatingIsNotNull(@Param("blog") Blog blog, @Param("user") User user);
    
    // Count reviews for a blog
    long countByBlog(Blog blog);
    
    // Count reviews for a blog with rating
    @Query("SELECT COUNT(r) FROM BlogReview r WHERE r.blog = :blog AND r.rating IS NOT NULL")
    long countReviewsByBlog(@Param("blog") Blog blog);
    
    // Get all reviews by user (reviews with rating)
    @Query("SELECT r FROM BlogReview r WHERE r.user = :user AND r.rating IS NOT NULL ORDER BY r.createdAt DESC")
    Page<BlogReview> findAllReviewsByUser(@Param("user") User user, Pageable pageable);
}
