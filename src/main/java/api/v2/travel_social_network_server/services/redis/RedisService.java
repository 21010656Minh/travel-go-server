package api.v2.travel_social_network_server.services.redis;

import api.v2.travel_social_network_server.responses.post.PostResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis Service using Sorted Set + Hash architecture for newsfeed caching
 * 
 * Architecture:
 * - Sorted Set (index): Stores postIds with timestamp as score for ordering
 * - String keys (data): Stores actual post JSON data
 * 
 * Benefits:
 * - O(1) update for individual posts (like/comment)
 * - O(log(N) + M) pagination
 * - Native sorting by timestamp
 * - Easy to remove specific posts
 */
@Slf4j
@Service
@AllArgsConstructor
public class RedisService implements IRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Lấy recent keywords của user từ Redis và nối thành chuỗi
     */
    public String getSearchKeywordsFromRedis(String redisKey, UUID userId) {
        try {
            List<String> keywords = redisTemplate.opsForList().range(redisKey, 0, -1);

            if (keywords == null || keywords.isEmpty()) {
                return "";
            }

            return keywords.stream()
                    .map(String::trim)
                    .filter(k -> !k.isEmpty())
                    .collect(Collectors.joining(" "));

        } catch (Exception e) {
            log.error("Failed to get search keywords for user {}: {}", userId, e.getMessage());
            return "";
        }
    }

    /**
     * Get page of postIds from cache (Sorted Set for ordering)
     * Returns empty list if cache miss
     */
    @Override
    public List<UUID> getNewsFeedIdsFromCache(String redisKey, int page, int pageSize, UUID userId) {
        log.debug("Getting page {} (size {}) of postIds from cache for user {}", page, pageSize, userId);

        String indexKey = redisKey + ":ids";
        
        int start = page * pageSize;
        int end = start + pageSize - 1;

        // Get postIds from sorted set (ordered by score desc)
        Set<String> postIdStrings = redisTemplate.opsForZSet().range(indexKey, start, end);

        if (postIdStrings == null || postIdStrings.isEmpty()) {
            log.debug("Cache MISS for user {} page {}", userId, page);
            return List.of();
        }

        // Convert String to UUID
        List<UUID> postIds = postIdStrings.stream()
                .map(UUID::fromString)
                .collect(Collectors.toList());

        log.debug("Cache HIT: Returned {} postIds for user {} page {}", postIds.size(), userId, page);
        return postIds;
    }

    /**
     * Cache newsfeed postIds using Sorted Set (clear old cache first)
     * Only stores ordering, not post data
     */
    @Override
    public void cacheNewsFeedIds(String redisKey, List<UUID> postIds, UUID userId, Duration duration) {
        try {
            String indexKey = redisKey + ":ids";
            
            // Clear old cache completely
            clearNewsFeedCache(redisKey, userId);

            log.info("Caching {} postIds for user {}", postIds.size(), userId);

            long timestamp = System.currentTimeMillis();
            
            for (int i = 0; i < postIds.size(); i++) {
                UUID postId = postIds.get(i);
                
                // Add to sorted set (negative score for DESC order)
                double score = -(timestamp - i); // Maintain order
                redisTemplate.opsForZSet().add(indexKey, postId.toString(), score);
            }

            // Set TTL for index
            redisTemplate.expire(indexKey, duration);
            
            log.info("✅ Cached {} postIds for user {}", postIds.size(), userId);

        } catch (Exception e) {
            log.error("Failed to cache newsfeed IDs for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Append new postIds to cache (O(log(N)) per insert)
     */
    @Override
    public void appendToNewsFeedIdsCache(String redisKey, List<UUID> newPostIds, UUID userId, Duration duration) {
        try {
            String indexKey = redisKey + ":ids";
            
            log.info("Appending {} postIds for user {}", newPostIds.size(), userId);

            long timestamp = System.currentTimeMillis();
            
            for (int i = 0; i < newPostIds.size(); i++) {
                UUID postId = newPostIds.get(i);
                
                // Add to sorted set with unique score
                double score = -(timestamp - i);
                redisTemplate.opsForZSet().add(indexKey, postId.toString(), score);
            }

            // Refresh TTL
            redisTemplate.expire(indexKey, duration);
            
            Long newSize = redisTemplate.opsForZSet().size(indexKey);
            log.info("✅ Appended {} postIds. New cache size: {}", newPostIds.size(), newSize);

        } catch (Exception e) {
            log.error("Failed to append postIds for user {}: {}", userId, e.getMessage());
        }
    }

    /**
     * Get cache size from sorted set (O(1))
     */
    @Override
    public Long getCacheSize(String redisKey) {
        try {
            String indexKey = redisKey + ":ids";
            return redisTemplate.opsForZSet().size(indexKey);
        } catch (Exception e) {
            log.error("Failed to get cache size for {}: {}", redisKey, e.getMessage());
            return null;
        }
    }

    /**
     * Clear newsfeed cache (only postIds index)
     */
    @Override
    public void clearNewsFeedCache(String redisKey, UUID userId) {
        try {
            String indexKey = redisKey + ":ids";
            
            // Delete index
            Boolean deleted = redisTemplate.delete(indexKey);
            
            if (Boolean.TRUE.equals(deleted)) {
                log.info("✅ Cleared newsfeed cache for user {}", userId);
            }
        } catch (Exception e) {
            log.error("Failed to clear cache for user {}: {}", userId, e.getMessage());
        }
    }
}
