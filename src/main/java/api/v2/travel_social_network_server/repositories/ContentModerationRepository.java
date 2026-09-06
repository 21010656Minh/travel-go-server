package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ContentModeration;
import api.v2.travel_social_network_server.utilities.enums.ContentTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentModerationRepository extends JpaRepository<ContentModeration, UUID> {
    
    /**
     * Find active moderation for specific content
     */
    Optional<ContentModeration> findByContentTypeAndContentIdAndIsActiveTrue(ContentTypeEnum contentType, UUID contentId);
    
    /**
     * Check if content is moderated
     */
    boolean existsByContentTypeAndContentIdAndIsActiveTrue(ContentTypeEnum contentType, UUID contentId);
}
