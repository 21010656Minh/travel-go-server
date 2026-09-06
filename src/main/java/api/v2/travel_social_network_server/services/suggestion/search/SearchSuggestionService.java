package api.v2.travel_social_network_server.services.suggestion.search;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.search.SearchSuggestionResponse;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.services.group.IGroupService;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.services.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SearchSuggestionService implements ISearchSuggestionService {

    private final IUserService userService;
    private final IGroupService groupService;
    private final IPostService postService;

    @Override
    @Transactional(readOnly = true)
    public SearchSuggestionResponse searchSuggestions(User user, String keyword, int page, int pageSize) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return SearchSuggestionResponse.builder()
                    .users(List.of())
                    .groups(List.of())
                    .posts(List.of())
                    .build();
        }

        // Tìm kiếm users
        List<UserResponse> users = userService.searchUsersForSuggestion(keyword, page, pageSize);
        
        // Tìm kiếm groups
        List<GroupResponse> groups = groupService.searchGroupsForSuggestion(keyword, page, pageSize);
        
        // Tìm kiếm posts
        List<PostResponse> posts = postService.searchPostsForSuggestion(keyword, page, pageSize);

        return SearchSuggestionResponse.builder()
                .users(users)
                .groups(groups)
                .posts(posts)
                .build();
    }
}