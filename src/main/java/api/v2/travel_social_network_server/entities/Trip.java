package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.TripStatusEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "trips")
public class Trip {

    @Id
    @Column(name = "trip_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "trip_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String tripName;

    @Column(name = "trip_description", columnDefinition = "TEXT")
    private String tripDescription;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "destination", columnDefinition = "VARCHAR(255)")
    private String destination;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(name = "budget", precision = 15, scale = 2)
    private BigDecimal budget;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatusEnum status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<TripSchedule> tripSchedules;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = TripStatusEnum.PLANNING;
        }
    }
}
