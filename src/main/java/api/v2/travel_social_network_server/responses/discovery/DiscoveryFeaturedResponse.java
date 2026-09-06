package api.v2.travel_social_network_server.responses.discovery;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoveryFeaturedResponse implements Serializable {
    /**
     * The single, large "featured" destination shown at the top of the Bento
     * grid. Picked as the highest-ranked destination across the recent public
     * feed.
     */
    private DiscoverySpotResponse featured;

    /**
     * Up to 4 additional destinations shown as small cards in the Bento grid,
     * sorted by trending score (descending). Excludes the `featured` one.
     */
    private List<DiscoverySpotResponse> miniSpots;
}
