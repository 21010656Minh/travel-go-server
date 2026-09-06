package api.v2.travel_social_network_server.services.comment;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.comment.CommentLikeResponse;

import java.util.UUID;

public interface ICommentLikeService {
    CommentLikeResponse toggleLikeOnComment(UUID commentId, User user);
}
