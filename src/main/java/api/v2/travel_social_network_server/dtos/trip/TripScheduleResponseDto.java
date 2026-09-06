package api.v2.travel_social_network_server.dtos.trip;

import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripScheduleResponseDto {

    private UUID tripScheduleId;
    private UUID tripId;
    private String title;
    private String description;
    private String location;
    private Instant scheduleDate;
    private Instant startTime;
    private Instant endTime;
    private String activityType;
    private BigDecimal estimatedCost;
    private String notes;
    private Integer orderIndex;
    private UUID createdBy;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
}
