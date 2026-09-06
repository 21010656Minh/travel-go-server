package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.entities.WatchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WatchHistoryRepository extends JpaRepository<WatchHistory, UUID> {

    // Find watch history by user and watch
    Optional<WatchHistory> findByUserAndWatch(User user, Watch watch);

    // Check if user has watched a video
    boolean existsByUserAndWatch(User user, Watch watch);

    // Get all watch histories by user with pagination (ordered by updated_at DESC)
    @Query("SELECT wh FROM WatchHistory wh JOIN FETCH wh.watch WHERE wh.user = :user ORDER BY wh.updatedAt DESC")
    Page<WatchHistory> findAllByUserOrderByUpdatedAtDesc(@Param("user") User user, Pageable pageable);

    // Count watch histories by user
    long countByUser(User user);

    // Delete watch history by user and watch
    void deleteByUserAndWatch(User user, Watch watch);

    // Delete all watch histories by user
    void deleteAllByUser(User user);

    // Delete watch histories older than specified date
    @Query("DELETE FROM WatchHistory wh WHERE wh.updatedAt < :cutoffDate")
    void deleteOlderThan(@Param("cutoffDate") java.time.Instant cutoffDate);
}
