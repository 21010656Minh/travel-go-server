package api.v2.travel_social_network_server.dtos.presence;

import lombok.*;

import java.time.Instant;

/**
 * Represents an online/offline presence event broadcast to all connected clients
 * via STOMP topic /topic/presence.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PresenceEventDto {
    /** UserId of the user whose status changed. */
    private String userId;

    /** Username (login name) for display purposes. */
    private String userName;

    /** Full display name from the user profile (may be null). */
    private String fullName;

    /** Avatar URL for display purposes. */
    private String avatarImg;

    /** New status: online or offline. */
    private String status;

    /** Event timestamp (ISO-8601). */
    private Instant timestamp;
}
