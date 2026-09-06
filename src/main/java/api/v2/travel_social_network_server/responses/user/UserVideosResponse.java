package api.v2.travel_social_network_server.responses.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVideosResponse {
    private List<UserMediaResponse> videos;
}
