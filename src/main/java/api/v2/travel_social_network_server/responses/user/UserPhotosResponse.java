package api.v2.travel_social_network_server.responses.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPhotosResponse {
    private List<UserMediaResponse> avatars;
    private List<UserMediaResponse> coverImages;
    private List<UserMediaResponse> postPhotos;
}
