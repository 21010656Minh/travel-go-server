package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "conversation_members")
public class ConversationMember {

    @Id
    @Column(name = "conversation_member_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID conversationMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonBackReference
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private MemberRoleTypeEnum role; // OWNER, ADMIN, MEMBER

    @CreatedDate
    @Column(name = "joined_at", updatable = false)
    private Instant joinedAt;
}
