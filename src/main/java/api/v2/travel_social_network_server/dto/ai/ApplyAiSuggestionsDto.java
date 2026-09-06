package api.v2.travel_social_network_server.dto.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplyAiSuggestionsDto {
    private List<DayScheduleDto> schedules;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayScheduleDto {
        private Integer day;
        private String title;
        private List<ActivityDto> activities;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityDto {
        private String time; // HH:mm format
        private String title;
        private String description;
        private String type; // VISIT, MEAL, TRANSPORT, ACCOMMODATION
        private String location;
        private Double estimatedCost;
    }
}
