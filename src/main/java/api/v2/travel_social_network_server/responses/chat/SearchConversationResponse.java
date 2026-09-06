package api.v2.travel_social_network_server.responses.chat;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
public class SearchConversationResponse {
    private List<ConversationResponse> groups;
    private List<ConversationResponse> friends;
}
