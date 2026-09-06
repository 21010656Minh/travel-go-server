package api.v2.travel_social_network_server.services.comment;

import api.v2.travel_social_network_server.dtos.comment.CommentDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.comment.CommentResponse;

import java.util.UUID;

public interface ICommentService {
    PageableResponse<CommentResponse> getCommentsByPostId(UUID postId, int page, int size, String sort, User currentUser);
    PageableResponse<CommentResponse> getCommentsByWatchId(UUID watchId, int page, int size, String sort, User currentUser);
    PageableResponse<CommentResponse> getRepliesByCommentId(UUID commentId, int page, int size, User currentUser);
    CommentResponse createCommentForContent(User user, CommentDto commentDto);
    CommentResponse updateCommentContent(UUID commentId, User user, CommentDto commentDto);
    void deleteCommentById(UUID commentId, User user);
}