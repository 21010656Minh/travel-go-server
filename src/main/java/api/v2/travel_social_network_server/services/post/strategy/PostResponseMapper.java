package api.v2.travel_social_network_server.services.post.strategy;

import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.post.PostResponse;

public interface PostResponseMapper {
    PostResponse toResponse(Post post, User user);
}
