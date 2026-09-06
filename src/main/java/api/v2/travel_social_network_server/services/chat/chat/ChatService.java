package api.v2.travel_social_network_server.services.chat.chat;

import api.v2.travel_social_network_server.entities.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String ONLINE_USERS_KEY = "online_users";
    private static final String USER_SESSION_KEY = "user_session:";
    private static final String GROUP_MEMBERS_KEY = "group_members:";
    private static final Duration SESSION_TIMEOUT = Duration.ofMinutes(30);

    /**
     * Đánh dấu user online
     */
    public void markUserOnline(UUID userId, String sessionId) {
        String userKey = USER_SESSION_KEY + userId;
        String sessionKey = "session:" + sessionId;
        
        // Lưu session của user
        redisTemplate.opsForValue().set(userKey, sessionId, SESSION_TIMEOUT);
        
        // Thêm user vào danh sách online
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId.toString());    }

    /**
     * Đánh dấu user offline
     */
    public void markUserOffline(UUID userId) {
        String userKey = USER_SESSION_KEY + userId;
        
        // Xóa session của user
        redisTemplate.delete(userKey);
        
        // Xóa user khỏi danh sách online
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId.toString());    }

    /**
     * Kiểm tra user có online không
     */
    public boolean isUserOnline(UUID userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(USER_SESSION_KEY + userId));
    }

    /**
     * Lấy danh sách user online
     */
    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }

    /**
     * Lưu user vào group chat
     */
    public void addUserToGroup(UUID groupId, UUID userId) {
        String groupKey = GROUP_MEMBERS_KEY + groupId;
        redisTemplate.opsForSet().add(groupKey, userId.toString());
    }

    /**
     * Xóa user khỏi group chat
     */
    public void removeUserFromGroup(UUID groupId, UUID userId) {
        String groupKey = GROUP_MEMBERS_KEY + groupId;
        redisTemplate.opsForSet().remove(groupKey, userId.toString());
    }

    /**
     * Lấy danh sách member trong group
     */
    public Set<String> getGroupMembers(UUID groupId) {
        String groupKey = GROUP_MEMBERS_KEY + groupId;
        return redisTemplate.opsForSet().members(groupKey);
    }

    /**
     * Lưu tin nhắn vào cache
     */
    public void cacheMessage(UUID groupId, String messageKey, String messageData) {
        String cacheKey = "group_messages:" + groupId + ":" + messageKey;
        redisTemplate.opsForValue().set(cacheKey, messageData, Duration.ofHours(24));
    }

    /**
     * Lấy tin nhắn từ cache
     */
    public String getCachedMessage(UUID groupId, String messageKey) {
        String cacheKey = "group_messages:" + groupId + ":" + messageKey;
        return redisTemplate.opsForValue().get(cacheKey);
    }

    /**
     * Xóa cache tin nhắn
     */
    public void clearMessageCache(UUID groupId) {
        String pattern = "group_messages:" + groupId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
