package api.v2.travel_social_network_server.responses.watch;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class WatchWithIdsResponse extends WatchResponse {
    private UUID watchHistoryId;
}
