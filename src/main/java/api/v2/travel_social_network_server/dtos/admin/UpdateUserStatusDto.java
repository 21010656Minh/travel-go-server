package api.v2.travel_social_network_server.dtos.admin;

import api.v2.travel_social_network_server.utilities.enums.StatusTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO for updating user status")
public class UpdateUserStatusDto {
    
    @NotNull(message = "Status is required")
    @Schema(description = "New user status", example = "INACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private StatusTypeEnum status;
}
