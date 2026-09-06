package api.v2.travel_social_network_server.dto.ai;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiSuggestResponseDto {
    private String message; // AI's response message
    private List<DayScheduleDto> schedules; // List of day schedules
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayScheduleDto {
        private Integer day; // Day number
        private String title; // Day theme/title
        private List<ActivityDto> activities; // List of activities
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityDto {
        private String time; // HH:mm format
        private String title; // Activity title
        private String description; // Activity description
        private String type; // VISIT, MEAL, TRANSPORT, ACCOMMODATION
        private String location; // Specific location (optional)
        private Double estimatedCost; // Estimated cost in VND (optional)
    }
}
