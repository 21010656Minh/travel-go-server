package api.v2.travel_social_network_server.services.suggestion.search.decorator;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.search.SearchSuggestionResponse;
import api.v2.travel_social_network_server.services.suggestion.search.ISearchSuggestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
@Primary
public class SearchSuggestionServiceDecorator implements ISearchSuggestionService {

    private final ISearchSuggestionService searchSuggestionService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String SEARCH_KEYWORDS_KEY_PREFIX = "search:keywords:user:";
    private static final int MAX_KEYWORDS = 3;
    private static final Duration EXPIRY_DURATION = Duration.ofDays(7);

    @Override
    public SearchSuggestionResponse searchSuggestions(User user, String keyword, int page, int pageSize) {
        // Thực hiện search
        SearchSuggestionResponse response = searchSuggestionService.searchSuggestions(user, keyword, page, pageSize);

        // Lưu keyword nếu hợp lệ
        if (response != null && !keyword.trim().isEmpty()) {
            System.out.println(keyword);
            saveKeywordToRedis(user.getUserId(), keyword.trim());
        }

        return response;
    }

    private void saveKeywordToRedis(UUID userId, String keyword) {
        try {
            String redisKey = SEARCH_KEYWORDS_KEY_PREFIX + userId;
            redisTemplate.opsForList().remove(redisKey, 0, keyword);

            // Thêm keyword mới nhất vào đầu danh sách
            redisTemplate.opsForList().leftPush(redisKey, keyword);

            // Giữ tối đa MAX_KEYWORDS phần tử
            redisTemplate.opsForList().trim(redisKey, 0, MAX_KEYWORDS - 1);

            // Đặt TTL cho key
            redisTemplate.expire(redisKey, EXPIRY_DURATION);
        } catch (Exception e) {
            log.error("Failed to save keyword '{}' for user {}: {}", keyword, userId, e.getMessage());
        }
    }

//    /**
//     * Lấy danh sách keywords gần đây của user
//     */
//    public List<String> getRecentKeywords(UUID userId) {
//        try {
//            String redisKey = SEARCH_KEYWORDS_KEY_PREFIX + userId;
//            return redisTemplate.opsForList().range(redisKey, 0, -1);
//        } catch (Exception e) {
//            log.error("Failed to get keywords for user {}: {}", userId, e.getMessage());
//            return List.of();
//        }
//    }
//
//    /**
//     * Mock method: sau này bạn có thể lấy userId từ SecurityContext / JWT
//     */
//    private UUID getCurrentUserIdMock() {
//        return UUID.fromString("00000000-0000-0000-0000-000000000001");
//    }
}
