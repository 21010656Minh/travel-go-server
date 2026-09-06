package api.v2.travel_social_network_server.services.suggestion.search;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.search.SearchSuggestionResponse;

public interface ISearchSuggestionService {
    SearchSuggestionResponse searchSuggestions(User user, String keyword, int page, int pageSize);
}