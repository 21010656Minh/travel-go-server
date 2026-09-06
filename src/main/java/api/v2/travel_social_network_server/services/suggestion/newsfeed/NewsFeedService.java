package api.v2.travel_social_network_server.services.suggestion.newsfeed;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.services.redis.IRedisService;
import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("newsFeedService")
@RequiredArgsConstructor
@Slf4j
public class NewsFeedService implements INewsFeedService {

    private final IPostService postService;
    private final IRedisService redisService;
    private static final String SEARCH_KEYWORDS_KEY_PREFIX = "search:keywords:user:";

    @Override
    public PageableResponse<PostResponse> getNewsFeed(User user, int page, int pageSize) {
        String searchKeywords = redisService.getSearchKeywordsFromRedis(SEARCH_KEYWORDS_KEY_PREFIX, user.getUserId());
        
        // Step 1: Compute newsfeed algorithm (heavy operation)
        List<UUID> postIds = computeNewsFeedPostIds(user, searchKeywords);
        
        // Step 2: Paginate postIds
        int start = page * pageSize;
        int end = Math.min(start + pageSize, postIds.size());
        
        if (start >= postIds.size()) {
            return PageableResponse.<PostResponse>builder()
                    .content(List.of())
                    .totalElements(postIds.size())
                    .totalPages((int) Math.ceil((double) postIds.size() / pageSize))
                    .pageNumber(page)
                    .pageSize(pageSize)
                    .last(true)
                    .first(page == 0)
                    .build();
        }
        
        List<UUID> pagePostIds = postIds.subList(start, end);
        List<PostResponse> posts = postService.getPostsByIds(pagePostIds, user);
        
        log.info("Returning {} posts for user {} (page {}, from {} total postIds)", 
                posts.size(), user.getUserId(), page, postIds.size());
        
        return PageableResponse.<PostResponse>builder()
                .content(posts)
                .totalElements(postIds.size())
                .totalPages((int) Math.ceil((double) postIds.size() / pageSize))
                .pageNumber(page)
                .pageSize(pageSize)
                .last(page + 1 >= Math.ceil((double) postIds.size() / pageSize))
                .first(page == 0)
                .build();
    }

    @Override
    public List<UUID> computeNewsFeedPostIds(User user, String searchKeywords) {
        List<PostResponse> allPosts = new ArrayList<>();

        if (searchKeywords.isEmpty()) {
            log.info("No recent search keywords for user {}, getting public NORMAL posts", user.getUserId());
            // Use filtered query - only NORMAL posts from database
            PageableResponse<PostResponse> publicPosts = postService.getPostsByPrivacyAndPostType(
                user, PrivacyTypeEnum.PUBLIC, PostTypeEnum.NORMAL, 0, 50);
            allPosts.addAll(publicPosts.getContent());
        } else {
            log.info("Search keywords for user {}: {}", user.getUserId(), searchKeywords);
            // Use filtered query - only NORMAL posts from database
            PageableResponse<PostResponse> publicPosts = postService.getPostsByPrivacyAndPostType(
                user, PrivacyTypeEnum.PUBLIC, PostTypeEnum.NORMAL, 0, 25);
            allPosts.addAll(publicPosts.getContent());
            
            // Use filtered query - only NORMAL posts from database
            List<PostResponse> suggestedPosts = postService.searchPostsForSuggestionByPostType(
                searchKeywords, PostTypeEnum.NORMAL, 0, 25);
            allPosts.addAll(suggestedPosts);
        }

        // Remove duplicates (no need to filter by PostType anymore - already filtered at DB level)
        List<PostResponse> uniquePosts = allPosts.stream()
                .collect(Collectors.toMap(
                        PostResponse::getPostId,
                        post -> post,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .collect(Collectors.toList());

        // Shuffle danh sách để trộn lẫn các bài viết
        Collections.shuffle(uniquePosts);
        
        // Extract only postIds
        List<UUID> postIds = uniquePosts.stream()
                .map(PostResponse::getPostId)
                .collect(Collectors.toList());
        
        log.info("Computed {} unique NORMAL postIds for user {} newsfeed (from {} total posts)", 
                postIds.size(), user.getUserId(), allPosts.size());
        
        return postIds;
    }

}
