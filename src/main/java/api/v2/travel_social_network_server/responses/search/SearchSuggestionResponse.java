package api.v2.travel_social_network_server.responses.search;

import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSuggestionResponse {
    private List<UserResponse> users;
    private List<GroupResponse> groups;
    private List<PostResponse> posts;
}