package api.v2.travel_social_network_server.services.like;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.like.ContentLikeResponse;

import java.util.UUID;

public interface IContentLikeService {
    ContentLikeResponse toggleLikeOnPost(UUID postId, User user);
    ContentLikeResponse toggleLikeOnWatch(UUID watchId, User user);
}
