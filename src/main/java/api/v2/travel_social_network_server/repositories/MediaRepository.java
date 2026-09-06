package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ContentMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface MediaRepository extends JpaRepository<ContentMedia, UUID> {
    
    /**
     * Find all orphaned media (not linked to any blog or post) that are older than specified time
     * @param cutoffTime The time before which media are considered orphaned
     * @return List of orphaned media
     */
    @Query("SELECT m FROM ContentMedia m WHERE m.blog IS NULL AND m.post IS NULL AND m.createdAt < :cutoffTime")
    List<ContentMedia> findOrphanedMedia(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    /**
     * Find all media by blog ID
     * @param blogId Blog UUID
     * @return List of media
     */
    @Query("SELECT m FROM ContentMedia m WHERE m.blog.blogId = :blogId")
    List<ContentMedia> findByBlogId(@Param("blogId") UUID blogId);
    
    /**
     * Find all media by post ID
     * @param postId Post UUID
     * @return List of media
     */
    @Query("SELECT m FROM ContentMedia m WHERE m.post.postId = :postId")
    List<ContentMedia> findByPostId(@Param("postId") UUID postId);
    
    /**
     * Find all media by conversation ID (chat images/videos)
     * @param conversationId Conversation UUID
     * @return List of media ordered by newest first
     */
    @Query("SELECT m FROM ContentMedia m WHERE m.conversation.conversationId = :conversationId ORDER BY m.createdAt DESC")
    List<ContentMedia> findByConversationId(@Param("conversationId") UUID conversationId);
}
