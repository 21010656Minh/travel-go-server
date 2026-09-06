package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notification_id", updatable = false, nullable = false)
    private UUID notificationId;

    // Người nhận thông báo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    // Người gây ra sự kiện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private NotificationTypeEnum type;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    // Id của đối tượng liên quan (post_id, comment_id, group_id...)
    @Column(name = "related_id")
    private UUID relatedId;

    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
