package api.v2.travel_social_network_server.controllers.admin;

import api.v2.travel_social_network_server.repositories.ContentLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("${api.base-url}/admin/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheController {

    private final RedisTemplate<String, String> redisTemplate;
    private final ContentLikeRepository postLikeRepository;

    /**
     * Clear all newsfeed cache (all users)
     */
    @DeleteMapping("/newsfeed/all")
    public ResponseEntity<String> clearAllNewsfeedCache() {
        try {
            // Get all newsfeed cache keys
            Set<String> keys = redisTemplate.keys("newsfeed:cache:user:*");
            
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);                return ResponseEntity.ok("Deleted " + deleted + " cache keys");
            }
            
            return ResponseEntity.ok("No cache keys found");
        } catch (Exception e) {
            log.error("Failed to clear newsfeed cache: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    /**
     * Clear cache for specific user
     */
    @DeleteMapping("/newsfeed/user/{userId}")
    public ResponseEntity<String> clearUserNewsfeedCache(@PathVariable String userId) {
        try {
            String pattern = "newsfeed:cache:user:" + userId + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys != null && !keys.isEmpty()) {
                Long deleted = redisTemplate.delete(keys);                return ResponseEntity.ok("Deleted " + deleted + " cache keys for user " + userId);
            }
            
            return ResponseEntity.ok("No cache found for user " + userId);
        } catch (Exception e) {
            log.error("Failed to clear cache for user {}: {}", userId, e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    /**
     * DANGER: Flush entire Redis database
     */
    @DeleteMapping("/flush-db")
    public ResponseEntity<String> flushDatabase() {
        try {
            redisTemplate.getConnectionFactory().getConnection().flushDb();            return ResponseEntity.ok("Database flushed successfully");
        } catch (Exception e) {
            log.error("Failed to flush database: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<String> getCacheStats() {
        try {
            Set<String> newsfeedKeys = redisTemplate.keys("newsfeed:cache:user:*");
            int totalKeys = newsfeedKeys != null ? newsfeedKeys.size() : 0;
            
            // Count index vs data keys
            int indexKeys = 0;
            int dataKeys = 0;
            if (newsfeedKeys != null) {
                for (String key : newsfeedKeys) {
                    if (key.contains(":index")) indexKeys++;
                    else if (key.contains(":data:")) dataKeys++;
                }
            }
            
            String stats = String.format(
                "Total Keys: %d\nIndex Keys: %d\nData Keys: %d\nPosts Cached: %d",
                totalKeys, indexKeys, dataKeys, dataKeys
            );
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }

    /**
     * DANGER: Delete all likes from database (for testing)
     */
    @DeleteMapping("/likes/all")
    public ResponseEntity<String> deleteAllLikes() {
        try {
            long count = postLikeRepository.count();
            postLikeRepository.deleteAll();            return ResponseEntity.ok("Deleted " + count + " likes");
        } catch (Exception e) {
            log.error("Failed to delete likes: {}", e.getMessage());
            return ResponseEntity.internalServerError().body("Failed: " + e.getMessage());
        }
    }
}
