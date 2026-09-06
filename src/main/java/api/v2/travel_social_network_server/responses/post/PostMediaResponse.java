package api.v2.travel_social_network_server.responses.post;

import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMediaResponse {
    private UUID mediaId;
    private UUID postId;
    private String url;
    private MediaTypeEnum type;
}