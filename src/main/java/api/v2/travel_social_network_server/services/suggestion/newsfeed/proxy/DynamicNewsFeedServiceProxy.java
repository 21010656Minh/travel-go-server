package api.v2.travel_social_network_server.services.suggestion.newsfeed.proxy;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.services.redis.IRedisService;
import api.v2.travel_social_network_server.services.suggestion.newsfeed.INewsFeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class DynamicNewsFeedServiceProxy implements InvocationHandler {

    private final INewsFeedService target;
    private final IRedisService redisService;
    private final IPostService postService;

    public DynamicNewsFeedServiceProxy(@Qualifier("newsFeedServiceDecorator") INewsFeedService target, 
                                       IRedisService redisService,
                                       IPostService postService) {
        this.target = target;
        this.redisService = redisService;
        this.postService = postService;
    }

    private static final String NEWS_FEED_CACHE_KEY_PREFIX = "newsfeed:cache:user:";
    private static final Duration CACHE_EXPIRY_DURATION = Duration.ofDays(1);

    @SuppressWarnings("unchecked")
    public static INewsFeedService createProxy(INewsFeedService target, IRedisService redisService, IPostService postService) {
        return (INewsFeedService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{INewsFeedService.class},
                new DynamicNewsFeedServiceProxy(target, redisService, postService)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // Only intercept getNewsFeed(User, int, int)
        if ("getNewsFeed".equals(method.getName()) && args.length == 3 
                && args[0] instanceof User && args[1] instanceof Integer && args[2] instanceof Integer) {

            User user = (User) args[0];
            int page = (Integer) args[1];
            int pageSize = (Integer) args[2];
            UUID userId = user.getUserId();
            String redisKey = NEWS_FEED_CACHE_KEY_PREFIX + userId;
            String searchKeywords = redisService.getSearchKeywordsFromRedis("search:keywords:user:", userId);

            // 1. Try get ALL postIds from cache (page 0 with large size)
            List<UUID> allCachedPostIds = redisService.getNewsFeedIdsFromCache(redisKey, 0, 1000, userId);
            
            // Early return if cache can satisfy the request
            if (!allCachedPostIds.isEmpty()) {
                int start = page * pageSize;
                
                if (start < allCachedPostIds.size()) {
                    // Cache HIT - serve from cache
                    log.info("Cache HIT for user {} - found {} total postIds in cache", 
                            userId, allCachedPostIds.size());
                    
                    int end = Math.min(start + pageSize, allCachedPostIds.size());
                    List<UUID> pagePostIds = allCachedPostIds.subList(start, end);
                    
                    // Fetch fresh posts by IDs (batch query)
                    List<PostResponse> posts = postService.getPostsByIds(pagePostIds, user);
                    
                    log.info("Returned {} fresh posts for user {} page {} from {} cached IDs", 
                            posts.size(), userId, page, pagePostIds.size());
                    
                    // Build PageableResponse
                    int totalPages = (int) Math.ceil((double) allCachedPostIds.size() / pageSize);
                    return PageableResponse.<PostResponse>builder()
                            .content(posts)
                            .totalElements(allCachedPostIds.size())
                            .totalPages(totalPages)
                            .pageNumber(page)
                            .pageSize(pageSize)
                            .last(page + 1 >= totalPages)
                            .first(page == 0)
                            .build();
                }
                
                // Cache exists but insufficient for requested page
                log.warn("Cache INSUFFICIENT for user {} - requested page {} (start={}) exceeds cache size {}. Recomputing...", 
                        userId, page, start, allCachedPostIds.size());
            } else {
                log.info("Cache MISS for user {} - computing full newsfeed", userId);
            }

            // 2. Cache MISS or INSUFFICIENT → compute ALL postIds (heavy operation)
            List<UUID> allPostIds = target.computeNewsFeedPostIds(user, searchKeywords);
            
            // 3. Cache ALL postIds for future requests
            if (allPostIds != null && !allPostIds.isEmpty()) {
                redisService.cacheNewsFeedIds(redisKey, allPostIds, userId, CACHE_EXPIRY_DURATION);
                log.info("✅ Cached {} postIds for user {}", allPostIds.size(), userId);
            }
            
            // 4. After recompute, ALWAYS return page 0 to reset client state
            // Client should detect pageNumber=0 when they requested page>0 and refresh
            int returnPage = 0;
            int start = returnPage * pageSize;
            int end = Math.min(start + pageSize, allPostIds.size());
            
            if (allPostIds.isEmpty()) {
                return PageableResponse.<PostResponse>builder()
                        .content(List.of())
                        .totalElements(0)
                        .totalPages(0)
                        .pageNumber(0)
                        .pageSize(pageSize)
                        .last(true)
                        .first(true)
                        .build();
            }
            
            List<UUID> pagePostIds = allPostIds.subList(start, end);
            List<PostResponse> posts = postService.getPostsByIds(pagePostIds, user);
            
            log.info("Recomputed newsfeed for user {} - returning page 0 with {} posts (requested page was {})", 
                    userId, posts.size(), page);
            
            int totalPages = (int) Math.ceil((double) allPostIds.size() / pageSize);
            return PageableResponse.<PostResponse>builder()
                    .content(posts)
                    .totalElements(allPostIds.size())
                    .totalPages(totalPages)
                    .pageNumber(returnPage)  // Always 0 after recompute
                    .pageSize(pageSize)
                    .last(returnPage + 1 >= totalPages)
                    .first(page == 0)
                    .build();
        }

        // Other methods call target directly
        return method.invoke(target, args);
    }
}
