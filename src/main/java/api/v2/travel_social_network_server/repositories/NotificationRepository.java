package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Notification;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    // Find notifications by receiver
    Page<Notification> findByReceiver_UserIdOrderByCreatedAtDesc(UUID receiverId, Pageable pageable);
    
    // Find unread notifications by receiver
    List<Notification> findByReceiver_UserIdAndIsReadFalseOrderByCreatedAtDesc(UUID receiverId);
    
    // Count unread notifications by receiver
    long countByReceiver_UserIdAndIsReadFalse(UUID receiverId);
    
    // Find notifications by type
    Page<Notification> findByReceiver_UserIdAndTypeOrderByCreatedAtDesc(UUID receiverId, NotificationTypeEnum type, Pageable pageable);
    
    // Mark notifications as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.receiver.userId = :receiverId")
    void markAllAsReadByReceiverId(@Param("receiverId") UUID receiverId);
    
    // Mark specific notification as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.notificationId = :notificationId")
    void markAsReadById(@Param("notificationId") UUID notificationId);
    
    // Delete old notifications (older than specified days)
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoffDate")
    void deleteOldNotifications(@Param("cutoffDate") Instant cutoffDate);
}
