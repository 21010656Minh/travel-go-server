package api.v2.travel_social_network_server.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "password_reset_tokens")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reset_id", nullable = false)
    private UUID resetId;

    @NotNull
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "reset_token", nullable = false, unique = true)
    private String resetToken;

    @NotNull
    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private boolean used = false;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;
}
