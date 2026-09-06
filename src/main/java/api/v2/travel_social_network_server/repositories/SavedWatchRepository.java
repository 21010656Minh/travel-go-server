    package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.SavedWatch;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Watch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedWatchRepository extends JpaRepository<SavedWatch, UUID> {

    // Check if user has saved a watch
    boolean existsByUserAndWatch(User user, Watch watch);

    // Find saved watch by user and watch
    Optional<SavedWatch> findByUserAndWatch(User user, Watch watch);

    // Get all saved watches by user with pagination
    @Query("SELECT sw FROM SavedWatch sw JOIN FETCH sw.watch WHERE sw.user = :user ORDER BY sw.createdAt DESC")
    Page<SavedWatch> findAllByUser(@Param("user") User user, Pageable pageable);

    // Count saved watches by user
    long countByUser(User user);

    // Delete saved watch by user and watch
    void deleteByUserAndWatch(User user, Watch watch);
}
