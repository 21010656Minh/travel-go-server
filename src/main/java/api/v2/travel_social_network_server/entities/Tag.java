package api.v2.travel_social_network_server.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "tags", indexes = {
    @Index(name = "idx_tag_slug", columnList = "slug"),
    @Index(name = "idx_tag_title", columnList = "title")
})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @NotNull
    @Column(name = "title", nullable = false, length = 50, unique = true)
    private String title;

    @NotNull
    @Column(name = "slug", nullable = false, length = 50, unique = true)
    private String slug;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();

    @ManyToMany(mappedBy = "tags")
    private Set<Watch> watches = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void generateSlug() {
        if (this.slug == null && this.title != null) {
            this.slug = normalizeSlug(this.title);
        }
    }

    private String normalizeSlug(String text) {
        return text.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .substring(0, Math.min(text.length(), 50));
    }
}

