package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WatchRepository extends JpaRepository<Watch, UUID> {

    // Find all public watches
    Page<Watch> findByPrivacy(PrivacyTypeEnum privacy, Pageable pageable);

    // Find all watches by user
    Page<Watch> findAllByUser(User user, Pageable pageable);

    // Find public watches by user
    Page<Watch> findByPrivacyAndUser(PrivacyTypeEnum privacy, User user, Pageable pageable);

    // Count watches by user
    long countByUser(User user);

    // Search watches by keyword (title, description, location)
    @Query("SELECT w FROM Watch w WHERE " +
            "w.title LIKE CONCAT('%', :keyword, '%') OR " +
            "w.description LIKE CONCAT('%', :keyword, '%') OR " +
            "LOWER(w.location) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Watch> searchWatches(@Param("keyword") String keyword, Pageable pageable);

    // Get featured watches (most viewed or most liked)
    @Query("SELECT w FROM Watch w WHERE w.privacy = 'PUBLIC' ORDER BY w.viewCount DESC, w.likeCount DESC")
    Page<Watch> findFeaturedWatches(Pageable pageable);

    // Get trending watches (recent with high engagement)
    @Query("SELECT w FROM Watch w WHERE w.privacy = 'PUBLIC' AND w.createdAt >= :sinceDate " +
            "ORDER BY (w.viewCount + w.likeCount * 2 + w.commentCount * 3) DESC")
    Page<Watch> findTrendingWatches(@Param("sinceDate") java.time.Instant sinceDate, Pageable pageable);

    // Find watches by tag
    @Query("SELECT w FROM Watch w JOIN w.tags t WHERE t.title = :tagName AND w.privacy = 'PUBLIC'")
    Page<Watch> findByTagName(@Param("tagName") String tagName, Pageable pageable);
}
