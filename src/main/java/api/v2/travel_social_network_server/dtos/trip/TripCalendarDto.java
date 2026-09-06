package api.v2.travel_social_network_server.dtos.trip;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TripCalendarDto {

    private UUID tripId;
    private String tripName;
    private String destination;
    private Instant scheduleDate; // The specific date this trip appears on the calendar
    private Instant startDate;
    private Instant endDate;
    private String status;
    
    // Conversation/Group info
    private UUID conversationId;
    private String conversationName;
    private String conversationAvatar;
    
    // Optional: number of schedules on this specific date
    private Long schedulesOnDate;
}
