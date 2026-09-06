package api.v2.travel_social_network_server.responses.post;

import lombok.*;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostUserResponse implements Serializable {
    private UUID userId;
    private String fullName;
    private String avatarImg;
}
