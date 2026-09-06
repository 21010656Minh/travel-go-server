package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.ConversationTypeEnum;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "conversations") // đổi từ group_chats -> conversations
public class Conversation {

    @Id
    @Column(name = "conversation_id") // đổi từ group_chat_id -> conversation_id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID conversationId;

    @Column(name = "conversation_name", columnDefinition = "VARCHAR(255)")
    private String conversationName; // null nếu là 1-1 chat

    @Column(name = "conversation_avatar", columnDefinition = "TEXT")
    private String conversationAvatar;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConversationTypeEnum type; // 'private' hoặc 'group'

    @Column(name = "last_message", columnDefinition = "TEXT")
    private String lastMessage;

    @Column(name = "last_active_at")
    private Instant lastActiveAt;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ConversationMember> conversationMembers;

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ConversationMessage> messages;

    @CreatedDate
    @Column(name = "created_at")
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
