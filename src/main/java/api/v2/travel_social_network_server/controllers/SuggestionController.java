package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.responses.search.SearchSuggestionResponse;
import api.v2.travel_social_network_server.services.suggestion.newsfeed.INewsFeedService;
import api.v2.travel_social_network_server.services.suggestion.search.ISearchSuggestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("${api.base-url}/suggestions")
@Tag(name = "Suggestion APIs", description = "Endpoints for AI-powered suggestions and personalized content")
@RequiredArgsConstructor
public class SuggestionController {

    private final ISearchSuggestionService searchSuggestionService;
    private final INewsFeedService dynamicNewsFeedServiceProxy;

    @Operation(
            summary = "Get AI-powered search suggestions",
            description = "Get intelligent search suggestions based on user behavior and preferences"
    )
    @GetMapping
    public ResponseEntity<Response<SearchSuggestionResponse>> getSearchSuggestions(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam("q") String keyword,

            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        SearchSuggestionResponse suggestions = searchSuggestionService.searchSuggestions(user, keyword, page, pageSize);

        return ResponseEntity.ok(
                Response.success(suggestions, request.getRequestURI(), "Search suggestions retrieved successfully")
        );
    }

    @Operation(
            summary = "Get personalized news feed",
            description = "Get personalized news feed based on user's search history and preferences with Redis caching"
    )
    @GetMapping("/newsfeed")
    public ResponseEntity<Response<PageableResponse<PostResponse>>> getNewsFeed(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "10")
            @RequestParam(defaultValue = "10") int pageSize,

            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        PageableResponse<PostResponse> newsFeed = dynamicNewsFeedServiceProxy.getNewsFeed(user, page, pageSize);

        return ResponseEntity.ok(
                Response.success(newsFeed, request.getRequestURI(), "News feed retrieved successfully")
        );
    }
}
