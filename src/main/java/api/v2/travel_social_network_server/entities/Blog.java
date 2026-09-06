package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.BlogStatusEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "blogs")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Blog {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "blog_id")
    private UUID blogId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    @NotNull
    @Size(max = 500)
    @Column(name = "title", nullable = false, length = 500)
    private String title;

    @NotNull
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "thumbnail_url", columnDefinition = "TEXT")
    private String thumbnailUrl;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "location", length = 255)
    private String location;

    @Column(name = "view_count")
    private Long viewCount;

    @Column(name = "average_rating")
    private Double averageRating;

    @Column(name = "total_ratings")
    private Integer totalRatings;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BlogStatusEnum status;

    @Column(name = "is_featured")
    private Boolean isFeatured;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "moderation_id")
    private ContentModeration contentModeration;

    @Column(name = "reading_time")
    private Integer readingTime;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant updatedAt;

    @Column(name = "published_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX", timezone = "UTC")
    private Instant publishedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "blog_tags",
        joinColumns = @JoinColumn(name = "blog_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonManagedReference
    private List<Tag> tags;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<BlogReview> reviews;

    @OneToMany(mappedBy = "blog", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ContentMedia> mediaList;

    @PrePersist
    public void prePersist() {
        if (viewCount == null) viewCount = 0L;
        if (averageRating == null) averageRating = 0.0;
        if (totalRatings == null) totalRatings = 0;
        if (status == null) status = BlogStatusEnum.PUBLISHED;
        if (isFeatured == null) isFeatured = false;
        if (status == BlogStatusEnum.PUBLISHED && publishedAt == null) {
            publishedAt = Instant.now();
        }
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}
