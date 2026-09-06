package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.FriendShipTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Friendship {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "friendship_id")
    private UUID friendshipId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FriendShipTypeEnum status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

