package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "content_media")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ContentMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "media_id")
    private UUID mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    @JsonBackReference
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = true)
    @JsonBackReference
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = true)
    @JsonBackReference
    private Conversation conversation;

    @Column(name = "url", nullable = false)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MediaTypeEnum type;

    @CreatedDate
    @Column(name = "uploaded_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}
