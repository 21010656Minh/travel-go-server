package api.v2.travel_social_network_server.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    private Long totalUsers;
    private Long totalTrips;
    private Long totalPosts;
    private Long totalReports;
    private Double userGrowthRate;
    private Double tripGrowthRate;
    private Double postGrowthRate;
    private Double reportGrowthRate;
}
