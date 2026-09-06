package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ContentComment;
import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ContentCommentRepository extends JpaRepository<ContentComment, UUID> {
    // Get all top-level comments for Post (comments without parent)
    @Query("SELECT c FROM ContentComment c WHERE c.post = :post AND c.parentComment IS NULL")
    Page<ContentComment> findAllByPost(@Param("post") Post post, Pageable pageable);
    
    // Get all top-level comments for Watch (comments without parent)
    @Query("SELECT c FROM ContentComment c WHERE c.watch = :watch AND c.parentComment IS NULL")
    Page<ContentComment> findAllByWatch(@Param("watch") Watch watch, Pageable pageable);
    
    // Get all replies to a specific comment
    @Query("SELECT c FROM ContentComment c WHERE c.parentComment.commentId = :parentCommentId")
    Page<ContentComment> findAllByParentCommentId(@Param("parentCommentId") UUID parentCommentId, Pageable pageable);
    
    // Count replies for a comment
    @Query("SELECT COUNT(c) FROM ContentComment c WHERE c.parentComment.commentId = :parentCommentId")
    long countByParentCommentId(@Param("parentCommentId") UUID parentCommentId);
}
