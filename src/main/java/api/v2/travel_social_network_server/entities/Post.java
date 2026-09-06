package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "posts")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "post_id")
    private UUID postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shared_post_id")
    @JsonBackReference
    private Post sharedPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    @JsonBackReference
    private Group group;

    @Column(name = "location", length = 255, columnDefinition = "VARCHAR(255)")
    private String location;

    @Column(name = "like_count")
    private Integer likeCount;

    @Column(name = "comment_count")
    private Integer commentCount;

    @Column(name = "share_count")
    private Integer shareCount;

    @Column(name = "is_share")
    @Builder.Default
    private Boolean isShare = false;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "post_type", nullable = false)
    private PostTypeEnum postType = PostTypeEnum.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy", nullable = false)
    private PrivacyTypeEnum privacy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderation_id")
    private ContentModeration contentModeration;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentMedia> mediaList;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentComment> contentComments;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentLike> contentLikes;

    @PrePersist
    public void prePersist() {
        if (likeCount == null) likeCount = 0;
        if (commentCount == null) commentCount = 0;
        if (shareCount == null) shareCount = 0;
        if (isShare == null) isShare = false;
        if (postType == null) postType = PostTypeEnum.NORMAL;
    }
}
