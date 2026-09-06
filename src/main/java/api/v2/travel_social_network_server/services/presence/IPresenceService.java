package api.v2.travel_social_network_server.services.presence;

import api.v2.travel_social_network_server.dtos.presence.PresenceEventDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.presence.OnlineUserResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface IPresenceService {

    /**
     * Mark a user as online, store session metadata, and broadcast an "online" event.
     */
    void markOnline(User user, String sessionId);

    /**
     * Mark a user as offline (one of possibly many sessions) and broadcast "offline"
     * only if the user has no other active sessions.
     */
    void markOffline(UUID userId, String sessionId);

    /**
     * Mark a user as offline for the given session without broadcasting.
     * Used by scheduled cleanup of stale sessions.
     */
    void markOfflineSilent(UUID userId, String sessionId);

    /**
     * Returns true if the user has at least one active session.
     */
    boolean isOnline(UUID userId);

    /**
     * Returns the set of currently online userIds.
     */
    Set<UUID> getOnlineUserIds();

    /**
     * Returns online users enriched with profile info (id, userName, fullName, avatarImg).
     */
    List<OnlineUserResponse> getOnlineUsers();

    /**
     * Broadcast a presence change to all connected clients via /topic/presence.
     */
    void broadcast(PresenceEventDto event);
}
