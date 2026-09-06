package api.v2.travel_social_network_server.dtos.trip;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConversationInfoDto {
    private UUID conversationId;
    private String conversationName;
    private String conversationAvatar;
}
