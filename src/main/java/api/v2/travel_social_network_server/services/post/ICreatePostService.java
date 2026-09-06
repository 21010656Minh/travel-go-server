package api.v2.travel_social_network_server.services.post;

import api.v2.travel_social_network_server.dtos.post.UpdatePostDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.post.PostResponse;

import java.util.UUID;

public interface ICreatePostService {
    PostResponse createPostMultiTask(User user, UpdatePostDto dto, UUID groupId);
}
