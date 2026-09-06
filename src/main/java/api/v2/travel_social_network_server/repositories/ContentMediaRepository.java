package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ContentMediaRepository extends JpaRepository<ContentMedia, UUID> {
    
    // Lấy tất cả media (IMAGE hoặc VIDEO) từ posts thường của user theo type
    @Query("SELECT pm FROM ContentMedia pm " +
           "JOIN pm.post p " +
           "WHERE p.user.userId = :userId " +
           "AND pm.type = :mediaType " +
           "AND p.postType = 'NORMAL' " +
           "ORDER BY pm.createdAt DESC")
    List<ContentMedia> findAllMediaByUserIdAndType(@Param("userId") UUID userId, @Param("mediaType") MediaTypeEnum mediaType);
    
    // Lấy tất cả ảnh đại diện từ AVATAR_UPDATE posts
    @Query("SELECT pm FROM ContentMedia pm " +
           "JOIN pm.post p " +
           "WHERE p.user.userId = :userId " +
           "AND p.postType = 'AVATAR_UPDATE' " +
           "ORDER BY pm.createdAt DESC")
    List<ContentMedia> findAllAvatarsByUserId(@Param("userId") UUID userId);
    
    // Lấy tất cả ảnh bìa từ COVER_UPDATE posts
    @Query("SELECT pm FROM ContentMedia pm " +
           "JOIN pm.post p " +
           "WHERE p.user.userId = :userId " +
           "AND p.postType = 'COVER_UPDATE' " +
           "ORDER BY pm.createdAt DESC")
    List<ContentMedia> findAllCoversByUserId(@Param("userId") UUID userId);
    
    // Lấy tất cả media theo groupId và type (IMAGE hoặc VIDEO)
    @Query("SELECT pm FROM ContentMedia pm " +
           "JOIN pm.post p " +
           "WHERE p.group.groupId = :groupId " +
           "AND pm.type = :mediaType " +
           "ORDER BY pm.createdAt DESC")
    List<ContentMedia> findAllMediaByGroupIdAndType(@Param("groupId") UUID groupId, @Param("mediaType") MediaTypeEnum mediaType);
}
