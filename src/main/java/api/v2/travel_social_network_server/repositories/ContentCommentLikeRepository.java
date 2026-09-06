package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ContentCommentLike;
import api.v2.travel_social_network_server.entities.ContentComment;
import api.v2.travel_social_network_server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface ContentCommentLikeRepository extends JpaRepository<ContentCommentLike, Long> {
    
    @Query("SELECT cl FROM ContentCommentLike cl WHERE cl.comment.commentId = :commentId AND cl.user.userId = :userId")
    Optional<ContentCommentLike> findByCommentIdAndUserId(@Param("commentId") UUID commentId, @Param("userId") UUID userId);
    
    boolean existsByCommentAndUser(ContentComment comment, User user);
}
