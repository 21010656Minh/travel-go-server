package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.RoleTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.StatusTypeEnum;
import com.fasterxml.jackson.annotation.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Size(max = 255)
    @Column(name = "user_name", nullable = false)
    private String userName;

    @NotNull
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @JsonIgnore
    private RoleTypeEnum role;

    @Column(name = "avatar_img", columnDefinition = "TEXT")
    private String avatarImg;

    @Column(name = "cover_img")
    private String coverImg;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @JsonIgnore
    private StatusTypeEnum status;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<UserCredential> credentials;

    @JsonManagedReference
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private PasswordResetToken passwordResetToken;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Post> posts;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Blog> blogs;

    @OneToMany(mappedBy = "requester", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Friendship> sentFriendRequests;

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Friendship> receivedFriendRequests;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ConversationMember> conversationMembers;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<ContentLike> contentLikes;

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<GroupMember> groupMembers;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name().toUpperCase()));
    }

    @Override
    public String getUsername() {
        return this.userName;
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return credentials.stream()
                .filter(c -> c.getProvider() == ProviderTypeEnum.LOCAL)
                .map(UserCredential::getPassword)
                .findFirst()
                .orElse(null);
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return this.status == StatusTypeEnum.ACTIVE;
    }

    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return this.status != StatusTypeEnum.INACTIVE;
    }

    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return this.status == StatusTypeEnum.ACTIVE;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                '}';
    }


    @PrePersist
    public void prePersist() {
        if (role == null) role = RoleTypeEnum.USER;
        if (status == null) status = StatusTypeEnum.ACTIVE;
    }
}
