package api.v2.travel_social_network_server.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "saved_watches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "watch_id"})
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SavedWatch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "saved_watch_id")
    private UUID savedWatchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "watch_id", nullable = false)
    private Watch watch;

    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
