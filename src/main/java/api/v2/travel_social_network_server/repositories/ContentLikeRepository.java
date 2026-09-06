package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.ContentLike;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Watch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContentLikeRepository extends JpaRepository<ContentLike, Long> {
    
    // For Post
    Optional<ContentLike> findByPostAndUser(Post post, User user);
    boolean existsByPostAndUser(Post post, User user);
    long countByPost(Post post);
    
    // For Watch
    Optional<ContentLike> findByWatchAndUser(Watch watch, User user);
    boolean existsByWatchAndUser(Watch watch, User user);
    long countByWatch(Watch watch);
}

