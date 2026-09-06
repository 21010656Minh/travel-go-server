package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Blog;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.BlogStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BlogRepository extends JpaRepository<Blog, UUID> {
    
    // Find all published blogs
    Page<Blog> findAllByStatusOrderByCreatedAtDesc(BlogStatusEnum status, Pageable pageable);
    
    // Find blogs by user
    Page<Blog> findAllByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find blogs by user and status
    Page<Blog> findAllByUserAndStatusOrderByCreatedAtDesc(User user, BlogStatusEnum status, Pageable pageable);
    
    // Find featured blogs
    Page<Blog> findAllByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(BlogStatusEnum status, Pageable pageable);
    
    // Find all featured blogs (for scheduler)
    List<Blog> findAllByIsFeaturedTrue();
    
    // Search blogs by title or description
    @Query("SELECT b FROM Blog b WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND b.status = :status ORDER BY b.createdAt DESC")
    Page<Blog> searchBlogs(@Param("query") String query, @Param("status") BlogStatusEnum status, Pageable pageable);
    
    // Search blogs by location
    @Query("SELECT b FROM Blog b WHERE LOWER(b.location) LIKE LOWER(CONCAT('%', :location, '%')) " +
           "AND b.status = :status ORDER BY b.createdAt DESC")
    Page<Blog> searchBlogsByLocation(@Param("location") String location, @Param("status") BlogStatusEnum status, Pageable pageable);
    
    // Find blogs by tag
    @Query("SELECT b FROM Blog b JOIN b.tags t WHERE t.tagId = :tagId AND b.status = :status ORDER BY b.createdAt DESC")
    Page<Blog> findAllByTagId(@Param("tagId") UUID tagId, @Param("status") BlogStatusEnum status, Pageable pageable);
    
    // Find trending blogs (by view count)
    @Query("SELECT b FROM Blog b WHERE b.status = :status ORDER BY b.viewCount DESC, b.createdAt DESC")
    Page<Blog> findTrendingBlogs(@Param("status") BlogStatusEnum status, Pageable pageable);
    
    // Find popular blogs (by average rating, total ratings, then creation date)
    @Query("SELECT b FROM Blog b WHERE b.status = :status ORDER BY b.averageRating DESC, b.totalRatings DESC, b.createdAt DESC")
    Page<Blog> findPopularBlogs(@Param("status") BlogStatusEnum status, Pageable pageable);
    
    // Find blog by ID and status
    Optional<Blog> findByBlogIdAndStatus(UUID blogId, BlogStatusEnum status);
    
    // Fulltext search using PostgreSQL to_tsvector
    @Query(value = "SELECT b.* FROM blogs b " +
            "WHERE (to_tsvector('simple', COALESCE(b.title, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(b.content, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(b.description, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(b.location, '')) @@ plainto_tsquery('simple', :keyword)) " +
            "AND b.status = :status " +
            "ORDER BY " +
            "CASE " +
            "  WHEN to_tsvector('simple', COALESCE(b.title, '')) @@ plainto_tsquery('simple', :keyword) THEN 1 " +
            "  WHEN to_tsvector('simple', COALESCE(b.description, '')) @@ plainto_tsquery('simple', :keyword) THEN 2 " +
            "  WHEN to_tsvector('simple', COALESCE(b.content, '')) @@ plainto_tsquery('simple', :keyword) THEN 3 " +
            "  ELSE 4 " +
            "END, b.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM blogs b " +
            "WHERE (to_tsvector('simple', COALESCE(b.title, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(b.content, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(b.description, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(b.location, '')) @@ plainto_tsquery('simple', :keyword)) " +
            "AND b.status = :status",
            nativeQuery = true)
    Page<Blog> searchBlogsFulltext(@Param("keyword") String keyword, @Param("status") String status, Pageable pageable);
    
    // Count blogs by user
    long countByUser(User user);
    
    // Count published blogs by user
    long countByUserAndStatus(User user, BlogStatusEnum status);
}
