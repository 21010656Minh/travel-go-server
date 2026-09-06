package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.ContentTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "content_moderations", indexes = {
    @Index(name = "idx_content_type_id", columnList = "content_type,content_id")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ContentModeration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "moderation_id")
    private UUID moderationId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 50)
    private ContentTypeEnum contentType;

    @NotNull
    @Column(name = "content_id", nullable = false)
    private UUID contentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderated_by_user_id", nullable = false)
    private User moderatedByUser; 

    @NotNull
    @Column(name = "moderation_reason", nullable = false, columnDefinition = "TEXT")
    private String moderationReason;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "moderated_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant moderatedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant updatedAt;

    @Column(name = "unlocked_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant unlockedAt;

    @PrePersist
    public void prePersist() {
        if (isActive == null) isActive = true;
    }
}
