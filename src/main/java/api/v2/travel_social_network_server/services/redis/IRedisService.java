package api.v2.travel_social_network_server.services.redis;

import api.v2.travel_social_network_server.responses.post.PostResponse;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public interface IRedisService {
    // search suggest
    String getSearchKeywordsFromRedis(String redisKey, UUID userId);

    // newsfeed - cache only postIds (ordering/pagination result)
    List<UUID> getNewsFeedIdsFromCache(String redisKey, int page, int pageSize, UUID userId);
    void cacheNewsFeedIds(String redisKey, List<UUID> postIds, UUID userId, Duration duration);
    void appendToNewsFeedIdsCache(String redisKey, List<UUID> newPostIds, UUID userId, Duration duration);
    Long getCacheSize(String redisKey);
    void clearNewsFeedCache(String redisKey, UUID userId);
}