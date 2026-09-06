package api.v2.travel_social_network_server.dtos.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommentDto {
    private UUID postId; // Required when creating comment for post, optional when updating
    
    private UUID watchId; // Required when creating comment for watch, optional when updating

    @NotBlank
    private String content;
    
    private UUID parentCommentId; // Optional: for nested replies
}
