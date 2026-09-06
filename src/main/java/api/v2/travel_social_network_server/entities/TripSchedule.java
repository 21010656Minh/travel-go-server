package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.ActivityTypeEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "trip_schedules")
public class TripSchedule {

    @Id
    @Column(name = "trip_schedule_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID tripScheduleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    @JsonBackReference
    private Trip trip;

    @Column(name = "title", nullable = false, columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location", columnDefinition = "VARCHAR(500)")
    private String location;

    @Column(name = "schedule_date", nullable = false)
    private Instant scheduleDate;

    @Column(name = "start_time")
    private Instant startTime;

    @Column(name = "end_time")
    private Instant endTime;

    @Column(name = "activity_type")
    @Enumerated(EnumType.STRING)
    private ActivityTypeEnum activityType;

    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private BigDecimal estimatedCost;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void prePersist() {
        if (orderIndex == null) {
            orderIndex = 0;
        }
    }
}
