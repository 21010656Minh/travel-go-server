package api.v2.travel_social_network_server.responses.chat;

import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import api.v2.travel_social_network_server.utilities.enums.ConversationTypeEnum;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
public class ConversationResponse {
    private UUID conversationId;
    private String conversationName;
    private String conversationAvatar;
    private ConversationTypeEnum type;
    private UUID otherUserId;
    private String lastMessage;
    private Instant lastActiveAt;

    // New fields
    private Boolean groupOwner; // true nếu current user là chủ nhóm, false/null nếu không
    private List<UserSummaryResponse> members; // Tối đa 3 thành viên
    private List<String> recentMedia; // Tối đa 3 ảnh gần nhất
}
