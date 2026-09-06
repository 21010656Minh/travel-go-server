package api.v2.travel_social_network_server.services.suggestion.newsfeed;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;

import java.util.List;
import java.util.UUID;

public interface INewsFeedService {
    PageableResponse<PostResponse> getNewsFeed(User user, int page, int pageSize);
    List<UUID> computeNewsFeedPostIds(User user, String searchKeywords);
}
