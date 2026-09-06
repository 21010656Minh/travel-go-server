package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "user_credentials",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"provider", "provider_user_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class UserCredential {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "credential_id", nullable = false, updatable = false)
    private UUID credentialId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 50)
    private ProviderTypeEnum provider; // LOCAL, GOOGLE, FACEBOOK...

    @Column(name = "provider_user_id", length = 255)
    private String providerUserId; // Google sub ID, Facebook ID...

    @Column(name = "password")
    private String password;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}
