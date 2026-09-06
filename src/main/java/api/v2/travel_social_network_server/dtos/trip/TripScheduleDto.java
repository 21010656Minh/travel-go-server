package api.v2.travel_social_network_server.dtos.trip;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripScheduleDto {

    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    @Size(max = 5000, message = "Description must not exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Location must not exceed 500 characters")
    private String location;

    @NotNull(message = "Schedule date is required")
    private Instant scheduleDate;

    private Instant startTime;

    private Instant endTime;

    private String activityType; // VISIT, MEAL, ACCOMMODATION, TRANSPORT, OTHER

    private BigDecimal estimatedCost;

    @Size(max = 5000, message = "Notes must not exceed 5000 characters")
    private String notes;

    private Integer orderIndex;
}
