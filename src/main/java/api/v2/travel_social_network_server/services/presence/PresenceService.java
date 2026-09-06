package api.v2.travel_social_network_server.services.presence;

import api.v2.travel_social_network_server.dtos.presence.PresenceEventDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.presence.OnlineUserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Redis-backed presence tracking with STOMP broadcast.
 * <p>
 * Data model in Redis:
 * <ul>
 *   <li>{@code online_users} — Set of userIds currently online (at least one active session)</li>
 *   <li>{@code user_sessions:{userId}} — Set of sessionIds belonging to that user</li>
 *   <li>{@code session_user:{sessionId}} — String value with userId (for reverse lookup)</li>
 *   <li>{@code session_meta:{sessionId}} — Hash with userName, fullName, avatarImg (for broadcast)</li>
 * </ul>
 * All session keys are written with a TTL so missed disconnects auto-expire.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService implements IPresenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserRepository userRepository;

    private static final String ONLINE_USERS_KEY = "presence:online_users";
    private static final String USER_SESSIONS_KEY = "presence:user_sessions:";
    private static final String SESSION_USER_KEY = "presence:session_user:";
    private static final String SESSION_META_KEY = "presence:session_meta:";

    private static final Duration SESSION_TTL = Duration.ofMinutes(30);
    private static final String PRESENCE_TOPIC = "/topic/presence";

    private static final String META_USER_ID = "userId";
    private static final String META_USER_NAME = "userName";
    private static final String META_FULL_NAME = "fullName";
    private static final String META_AVATAR = "avatarImg";

    @Override
    public void markOnline(User user, String sessionId) {
        if (user == null || sessionId == null) return;
        UUID userId = user.getUserId();
        String userIdStr = userId.toString();

        // Store reverse lookup so we can resolve sessionId -> userId on disconnect
        redisTemplate.opsForValue().set(SESSION_USER_KEY + sessionId, userIdStr, SESSION_TTL);

        // Store profile metadata for fast broadcast without DB lookup
        Map<String, String> meta = new HashMap<>();
        meta.put(META_USER_ID, userIdStr);
        meta.put(META_USER_NAME, user.getUsername());
        UserProfile profile = user.getUserProfile();
        if (profile != null) {
            meta.put(META_FULL_NAME, profile.getFullName());
        }
        if (user.getAvatarImg() != null) {
            meta.put(META_AVATAR, user.getAvatarImg());
        }
        redisTemplate.opsForHash().putAll(SESSION_META_KEY + sessionId, meta);
        redisTemplate.expire(SESSION_META_KEY + sessionId, SESSION_TTL);

        // Add session to user-sessions set with TTL on the set itself
        String userSessionsKey = USER_SESSIONS_KEY + userIdStr;
        redisTemplate.opsForSet().add(userSessionsKey, sessionId);
        redisTemplate.expire(userSessionsKey, SESSION_TTL);

        // Mark user as online (set of userIds)
        boolean wasOnline = Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userIdStr));
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userIdStr);

        log.info("✅ User online: userId={}, sessionId={}", userIdStr, sessionId);

        // Only broadcast first session to avoid noisy duplicate events
        if (!wasOnline) {
            broadcast(buildEvent(user, "online"));
        }
    }

    @Override
    public void markOffline(UUID userId, String sessionId) {
        if (userId == null || sessionId == null) return;
        String userIdStr = userId.toString();

        // Remove session
        redisTemplate.opsForSet().remove(USER_SESSIONS_KEY + userIdStr, sessionId);
        redisTemplate.delete(SESSION_USER_KEY + sessionId);
        redisTemplate.delete(SESSION_META_KEY + sessionId);

        boolean stillOnline = hasAnyActiveSession(userId);

        if (!stillOnline) {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userIdStr);
            log.info("🔴 User offline: userId={}, sessionId={}", userIdStr, sessionId);
            broadcast(PresenceEventDto.builder()
                    .userId(userIdStr)
                    .status("offline")
                    .timestamp(Instant.now())
                    .build());
        } else {
            log.info("⏹️ Session closed but user still online: userId={}, sessionId={}", userIdStr, sessionId);
        }
    }

    @Override
    public void markOfflineSilent(UUID userId, String sessionId) {
        if (userId == null || sessionId == null) return;
        String userIdStr = userId.toString();

        redisTemplate.opsForSet().remove(USER_SESSIONS_KEY + userIdStr, sessionId);
        redisTemplate.delete(SESSION_USER_KEY + sessionId);
        redisTemplate.delete(SESSION_META_KEY + sessionId);

        if (!hasAnyActiveSession(userId)) {
            redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userIdStr);
        }
    }

    @Override
    public boolean isOnline(UUID userId) {
        if (userId == null) return false;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId.toString()));
    }

    @Override
    public Set<UUID> getOnlineUserIds() {
        Set<String> raw = redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
        if (raw == null) return Collections.emptySet();
        Set<UUID> result = new HashSet<>(raw.size());
        for (String s : raw) {
            try {
                result.add(UUID.fromString(s));
            } catch (IllegalArgumentException ignored) {
                // skip malformed entries
            }
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OnlineUserResponse> getOnlineUsers() {
        Set<UUID> ids = getOnlineUserIds();
        if (ids.isEmpty()) return Collections.emptyList();

        // Fetch profile metadata from DB to enrich the response
        Map<UUID, User> usersById = userRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(User::getUserId, u -> u));

        List<OnlineUserResponse> result = new ArrayList<>(ids.size());
        Instant now = Instant.now();
        for (UUID id : ids) {
            User user = usersById.get(id);
            if (user == null) {
                // Redis may have stale entries; skip
                continue;
            }
            UserProfile profile = user.getUserProfile();
            result.add(OnlineUserResponse.builder()
                    .userId(id.toString())
                    .userName(user.getUsername())
                    .fullName(profile != null ? profile.getFullName() : null)
                    .avatarImg(user.getAvatarImg())
                    .lastSeenAt(now)
                    .build());
        }
        return result;
    }

    @Override
    public void broadcast(PresenceEventDto event) {
        if (event == null) return;
        if (event.getTimestamp() == null) {
            event.setTimestamp(Instant.now());
        }
        try {
            simpMessagingTemplate.convertAndSend(PRESENCE_TOPIC, event);
        } catch (Exception ex) {
            log.warn("⚠️ Failed to broadcast presence event for userId={}: {}", event.getUserId(), ex.getMessage());
        }
    }

    private boolean hasAnyActiveSession(UUID userId) {
        if (userId == null) return false;
        Set<String> sessions = redisTemplate.opsForSet().members(USER_SESSIONS_KEY + userId);
        return sessions != null && !sessions.isEmpty();
    }

    private PresenceEventDto buildEvent(User user, String status) {
        UserProfile profile = user.getUserProfile();
        return PresenceEventDto.builder()
                .userId(user.getUserId().toString())
                .userName(user.getUsername())
                .fullName(profile != null ? profile.getFullName() : null)
                .avatarImg(user.getAvatarImg())
                .status(status)
                .timestamp(Instant.now())
                .build();
    }
}
