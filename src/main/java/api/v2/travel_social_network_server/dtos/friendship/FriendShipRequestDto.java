package api.v2.travel_social_network_server.dtos.friendship;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FriendShipRequestDto {
    @NotNull
    private UUID receiverId;
}