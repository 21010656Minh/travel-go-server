package api.v2.travel_social_network_server.dto.ai;

import api.v2.travel_social_network_server.enums.AiRequestType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSuggestRequestDto {
    private AiRequestType type; // Request type: GENERATE for itinerary generation, null for normal chat
    private String prompt; // User's request/question
    private String destination; // Trip destination
    private String startDate; // Trip start date (ISO format)
    private String endDate; // Trip end date (ISO format)
    private Integer numberOfDays; // Total days
    private Double budget; // Total budget in VND
    private String groupType; // SOLO, COUPLE, FAMILY, FRIENDS
    private String interests; // User interests/preferences (optional)
}
