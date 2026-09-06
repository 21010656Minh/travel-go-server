package api.v2.travel_social_network_server.responses.post;

//import api.v2.travel_social_network_server.entities.Group;
//import api.v2.travel_social_network_server.reponses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostResponse implements Serializable {
    private UUID postId;
    private String content;
    private String location;
    private PostUserResponse user;
    private Integer likeCount;
    private Integer commentCount;
    private Integer shareCount;
    private List<PostMediaResponse> mediaList;
    private List<String> tags;
    private Boolean isShare;
    private PostTypeEnum postType;
    private PostResponse sharedPost;
    private PrivacyTypeEnum privacy;
    private GroupResponse group;
    private Instant createdAt;
    private boolean liked;
}
