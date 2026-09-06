package api.v2.travel_social_network_server.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentActivityDto {
    private String title;
    private String description;
    private Instant timestamp;
    private String activityType; // USER_REGISTERED, POST_CREATED, REPORT_SUBMITTED, GROUP_CREATED
}
