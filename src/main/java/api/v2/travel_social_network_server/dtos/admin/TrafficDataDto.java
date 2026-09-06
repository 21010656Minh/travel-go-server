package api.v2.travel_social_network_server.dtos.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TrafficDataDto {
    private String name;
    private Long visits;
}
