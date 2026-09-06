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
public class TripDto {

    @NotNull(message = "Conversation ID is required")
    private UUID conversationId;

    @NotBlank(message = "Trip name is required")
    @Size(max = 255, message = "Trip name must not exceed 255 characters")
    private String tripName;

    @Size(max = 5000, message = "Trip description must not exceed 5000 characters")
    private String tripDescription;

    private String coverImageUrl;

    @Size(max = 255, message = "Destination must not exceed 255 characters")
    private String destination;

    @NotNull(message = "Start date is required")
    private Instant startDate;

    @NotNull(message = "End date is required")
    private Instant endDate;

    private BigDecimal budget;

    private String status; // PLANNING, CONFIRMED, ONGOING, COMPLETED, CANCELLED
}
