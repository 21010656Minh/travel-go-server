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
public class TripResponseDto {

    private UUID tripId;
    private ConversationInfoDto conversation;
    private String tripName;
    private String tripDescription;
    private String coverImageUrl;
    private String destination;
    private Instant startDate;
    private Instant endDate;
    private BigDecimal budget;
    private String status;
    private UUID createdBy;
    private String createdByName;
    private Instant createdAt;
    private Instant updatedAt;
    private Long scheduleCount;
}
