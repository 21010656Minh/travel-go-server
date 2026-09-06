package api.v2.travel_social_network_server.services.comment;

import api.v2.travel_social_network_server.entities.ContentCommentLike;
import api.v2.travel_social_network_server.entities.ContentComment;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.ContentCommentLikeRepository;
import api.v2.travel_social_network_server.repositories.ContentCommentRepository;
import api.v2.travel_social_network_server.responses.comment.CommentLikeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentLikeService implements ICommentLikeService {

    private final ContentCommentLikeRepository commentLikeRepository;
    private final ContentCommentRepository commentRepository;

    @Transactional
    @Override
    public CommentLikeResponse toggleLikeOnComment(UUID commentId, User user) {
        ContentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        // Use ID-based query instead of object comparison
        ContentCommentLike liked = commentLikeRepository.findByCommentIdAndUserId(commentId, user.getUserId()).orElse(null);
        boolean isLiked;

        log.info("Like comment action - CommentId: {}, UserId: {}, Already liked: {}",
                commentId, user.getUserId(), liked != null);

        if (liked != null) {
            // Unlike: remove the like            commentLikeRepository.delete(liked);
            int likeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
            comment.setLikeCount(Math.max(0, likeCount - 1));
            isLiked = false;
        } else {
            // Like: add a new like            ContentCommentLike like = ContentCommentLike.builder()
                    .user(user)
                    .comment(comment)
                    .build();
            commentLikeRepository.save(like);

            int likeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
            comment.setLikeCount(likeCount + 1);
            isLiked = true;
        }

        ContentComment updatedComment = commentRepository.save(comment);

        log.info("✅ Comment like toggled successfully - CommentId: {}, LikeCount: {}, isLiked: {}",
                commentId, updatedComment.getLikeCount(), isLiked);

        return CommentLikeResponse.builder()
                .commentId(comment.getCommentId())
                .likeCount(updatedComment.getLikeCount())
                .liked(isLiked)
                .build();
    }
}
