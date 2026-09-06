package api.v2.travel_social_network_server.services.presence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Periodically reconciles the global online_users set with individual session keys.
 * Redis TTL handles individual session expiry, but in some edge cases (e.g.
 * network partitions, Redis restarts) the set may retain stale entries.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PresenceCleanupScheduler {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String ONLINE_USERS_KEY = "presence:online_users";
    private static final String USER_SESSIONS_KEY = "presence:user_sessions:";

    /**
     * Runs every 5 minutes. Iterates online users and removes any whose
     * session set is empty or missing.
     */
    @Scheduled(fixedDelay = 5 * 60 * 1000L, initialDelay = 60 * 1000L)
    public void reconcileOnlineUsers() {
        Set<String> online;
        try {
            online = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        } catch (Exception ex) {
            log.warn("⚠️ Failed to read online users during cleanup: {}", ex.getMessage());
            return;
        }
        if (online == null || online.isEmpty()) return;

        Set<String> stale = new HashSet<>();
        for (String userIdStr : online) {
            UUID userId;
            try {
                userId = UUID.fromString(userIdStr);
            } catch (IllegalArgumentException ignored) {
                stale.add(userIdStr);
                continue;
            }
            Set<String> sessions = redisTemplate.opsForSet().members(USER_SESSIONS_KEY + userId);
            if (sessions == null || sessions.isEmpty()) {
                stale.add(userIdStr);
            }
        }

        if (!stale.isEmpty()) {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, stale.toArray());
            log.info("🧹 Reconciled presence: removed {} stale entries", stale.size());
        }
    }
}
