package api.v2.travel_social_network_server.dtos.admin;

import api.v2.travel_social_network_server.utilities.enums.RoleTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.StatusTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecentUserDto {
    private UUID userId;
    private String userName;
    private String email;
    private String avatarImg;
    private RoleTypeEnum role;
    private StatusTypeEnum status;
    private LocalDateTime createdAt;
}
