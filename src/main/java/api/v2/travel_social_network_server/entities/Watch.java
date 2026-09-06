package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "watches")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Watch {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "watch_id")
    private UUID watchId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @NotNull
    @Column(name = "video_url", nullable = false, columnDefinition = "TEXT")
    private String videoUrl;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Column(name = "duration")
    private Integer duration; // Duration in seconds

    @Column(name = "location", length = 255)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    private PrivacyTypeEnum privacy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderation_id")
    private ContentModeration contentModeration;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "share_count")
    private Integer shareCount;

    @Column(name = "view_count")
    private Integer viewCount;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Instant updatedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "watch_tags",
        joinColumns = @JoinColumn(name = "watch_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "watch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentLike> contentLikes;

    @OneToMany(mappedBy = "watch", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentComment> contentComments;

    @PrePersist
    public void prePersist() {
        if (likeCount == null) likeCount = 0;
        if (commentCount == null) commentCount = 0;
        if (shareCount == null) shareCount = 0;
        if (viewCount == null) viewCount = 0;
        if (privacy == null) privacy = PrivacyTypeEnum.PUBLIC;
    }
}
