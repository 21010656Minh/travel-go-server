package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.GroupMemberStatusTypeEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "group_members")
@EntityListeners(AuditingEntityListener.class)
public class GroupMember {
    @Id
    @Column(name = "group_member_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID GroupIdMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    @JsonBackReference
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @Enumerated(EnumType.STRING)
    private GroupMemberStatusTypeEnum status;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private MemberRoleTypeEnum role;

    @CreatedDate
    @Column(name = "joinedAt", updatable = false)
    private Instant joinedAt;

    @PrePersist
    public void prePersist() {
        if (status == null) status = GroupMemberStatusTypeEnum.APPROVED;
    }
}
