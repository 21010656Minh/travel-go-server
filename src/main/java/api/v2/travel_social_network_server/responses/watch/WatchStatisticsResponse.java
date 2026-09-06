package api.v2.travel_social_network_server.responses.watch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchStatisticsResponse {
    private Long totalVideos;
    private Long totalViews;
    private Long totalLikes;
    private Long totalComments;
    private Long totalShares;
}
