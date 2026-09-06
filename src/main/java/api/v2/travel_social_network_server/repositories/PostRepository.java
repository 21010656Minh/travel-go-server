package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;


@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findAllByUser(User user, Pageable pageable);

    Page<Post> findAllByGroupGroupId(UUID groupId, Pageable pageable);

    Page<Post> findByPrivacy(PrivacyTypeEnum privacy, Pageable pageable);

    /**
     * Fetch top public posts for the Discovery grid. Ordered by likeCount
     * descending, then commentCount, then shareCount, then most recent, so the
     * aggregated destinations stay stable and trending. `Pageable` is used to
     * bound the result set.
     */
    @Query("SELECT p FROM Post p " +
           "WHERE p.privacy = :privacy " +
           "ORDER BY p.likeCount DESC, p.commentCount DESC, p.shareCount DESC, p.createdAt DESC")
    List<Post> findTopPublicPostsForDiscovery(@Param("privacy") PrivacyTypeEnum privacy, Pageable pageable);

    Page<Post> findByPrivacyAndUser(PrivacyTypeEnum privacy, User user, Pageable pageable);

    // With PostType filter
    Page<Post> findByPrivacyAndPostType(PrivacyTypeEnum privacy, PostTypeEnum postType, Pageable pageable);

    Page<Post> findByPrivacyAndUserAndPostType(PrivacyTypeEnum privacy, User user, PostTypeEnum postType, Pageable pageable);

    // Count posts by user
    long countByUser(User user);

    // Count posts by user in a specific group
    long countByUserAndGroup_GroupId(User user, UUID groupId);

    // Search bài post trong group theo keyword
    @Query("SELECT p FROM Post p WHERE p.group.groupId = :groupId AND p.content LIKE %:keyword%")
    Page<Post> searchPostsInGroup(@Param("groupId") UUID groupId, @Param("keyword") String keyword, Pageable pageable);

    // Search posts for suggestion
    @Query("SELECT p FROM Post p LEFT JOIN p.user u LEFT JOIN p.group g WHERE " +
            "p.content LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(p.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Post> searchPostsForSuggestion(@Param("keyword") String keyword, Pageable pageable);

    // Search posts for suggestion with PostType filter
    @Query("SELECT p FROM Post p LEFT JOIN p.user u LEFT JOIN p.group g WHERE " +
            "(p.content LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(p.location) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND p.postType = :postType")
    Page<Post> searchPostsForSuggestionByPostType(@Param("keyword") String keyword, @Param("postType") PostTypeEnum postType, Pageable pageable);

    // Đếm số bài viết trong group trong N ngày gần nhất
    @Query("SELECT COUNT(p) FROM Post p WHERE p.group.groupId = :groupId AND p.createdAt >= :sinceDate")
    long countPostsInGroupSinceDate(@Param("groupId") UUID groupId, @Param("sinceDate") java.time.Instant sinceDate);

}
