package api.v2.travel_social_network_server.entities;

import api.v2.travel_social_network_server.utilities.enums.GenderTypeEnum;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @JsonIgnore
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_profile_id")
    private UUID userProfileId;

    @Column(name = "full_name", columnDefinition = "VARCHAR(255)")
    private String fullName;

    @Column(name = "location", length = 255, columnDefinition = "VARCHAR(255)")
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", columnDefinition = "VARCHAR(10)")
    private GenderTypeEnum gender;

    @Column(name = "dob")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    @Column(name = "about", columnDefinition = "TEXT")
    private String about;

    @CreatedDate
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

}
