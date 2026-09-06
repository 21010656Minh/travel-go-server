package api.v2.travel_social_network_server.responses.chat;

import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMemberResponse {
    private UUID conversationMemberId;
    private UUID userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private MemberRoleTypeEnum role;
    private Instant joinedAt;
}
