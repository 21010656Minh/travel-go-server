package api.v2.travel_social_network_server.dtos.group;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JoinGroupResponse {
    private String status; // APPROVED or PENDING
    private Boolean isMember; // true if APPROVED, false if PENDING
}
