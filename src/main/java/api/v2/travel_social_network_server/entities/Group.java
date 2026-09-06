package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "groups")
public class Group {
    @Id
    @Column(name = "group_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID groupId;

    @Column(name = "group_name", nullable = false, columnDefinition = "VARCHAR(255)")
    private String groupName;

    @Column(name = "group_description", columnDefinition = "TEXT")
    private String groupDescription;

    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;

    @Column(name = "member_count")
    private Integer memberCount;

    @Column(name = "privacy")
    @Enumerated(EnumType.STRING)
    private PrivacyTypeEnum privacy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderation_id")
    private ContentModeration contentModeration;

    @Column(name = "tags", columnDefinition = "VARCHAR(255)")
    private String tags;

    @Column(name = "location", columnDefinition = "VARCHAR(255)")
    private String location;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<GroupMember> groupMembers;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Post> posts;

    @CreatedDate
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @LastModifiedBy
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    @Column(name = "last_activity_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityAt;

    @PrePersist
    public void prePersist() {
        if (memberCount == null) memberCount = 0;
        if (lastActivityAt == null) lastActivityAt = LocalDateTime.now();
    }
}
