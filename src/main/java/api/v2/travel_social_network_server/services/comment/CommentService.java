package api.v2.travel_social_network_server.services.comment;

import api.v2.travel_social_network_server.controllers.ws.NotificationWebSocketController;
import api.v2.travel_social_network_server.dtos.comment.CommentDto;
import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.entities.ContentComment;
import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.comment.CommentResponse;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.repositories.ContentCommentLikeRepository;
import api.v2.travel_social_network_server.repositories.ContentCommentRepository;
import api.v2.travel_social_network_server.repositories.PostRepository;
import api.v2.travel_social_network_server.repositories.WatchRepository;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class CommentService implements ICommentService {

    private final ContentCommentRepository commentRepository;
    private final PostRepository postRepository;
    private final WatchRepository watchRepository;
    private final ContentCommentLikeRepository commentLikeRepository;
    private final INotificationService notificationService;
    private final NotificationWebSocketController notificationWebSocketController;
    private final Executor notificationExecutor;

    public CommentService(ContentCommentRepository commentRepository,
                         PostRepository postRepository,
                         WatchRepository watchRepository,
                         ContentCommentLikeRepository commentLikeRepository,
                         INotificationService notificationService,
                         NotificationWebSocketController notificationWebSocketController,
                         @Qualifier("taskExecutor") Executor notificationExecutor) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.watchRepository = watchRepository;
        this.commentLikeRepository = commentLikeRepository;
        this.notificationService = notificationService;
        this.notificationWebSocketController = notificationWebSocketController;
        this.notificationExecutor = notificationExecutor;
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<CommentResponse> getCommentsByPostId(UUID postId, int page, int size, String sort, User currentUser) {
        Sort sortStrategy;
        if ("newest".equalsIgnoreCase(sort)) {
            sortStrategy = Sort.by("createdAt").descending();
        } else if ("oldest".equalsIgnoreCase(sort)) {
            sortStrategy = Sort.by("createdAt").ascending();
        } else {
            sortStrategy = Sort.by("likeCount").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortStrategy);
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Page<ContentComment> comments = commentRepository.findAllByPost(post, pageable);

        List<CommentResponse> content = comments.getContent().stream()
                .map(comment -> convertToGenericCommentResponse(comment, currentUser))
                .toList();

        return PageableResponse.<CommentResponse>builder()
                .content(content)
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<CommentResponse> getCommentsByWatchId(UUID watchId, int page, int size, String sort, User currentUser) {
        Sort sortStrategy;
        if ("newest".equalsIgnoreCase(sort)) {
            sortStrategy = Sort.by("createdAt").descending();
        } else if ("oldest".equalsIgnoreCase(sort)) {
            sortStrategy = Sort.by("createdAt").ascending();
        } else {
            sortStrategy = Sort.by("likeCount").descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortStrategy);
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found"));

        Page<ContentComment> comments = commentRepository.findAllByWatch(watch, pageable);

        List<CommentResponse> content = comments.getContent().stream()
                .map(comment -> convertToGenericCommentResponse(comment, currentUser))
                .toList();

        return PageableResponse.<CommentResponse>builder()
                .content(content)
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<CommentResponse> getRepliesByCommentId(UUID commentId, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

        commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        Page<ContentComment> replies = commentRepository.findAllByParentCommentId(commentId, pageable);

        List<CommentResponse> content = replies.getContent().stream()
                .map(comment -> convertToGenericCommentResponse(comment, currentUser))
                .toList();

        return PageableResponse.<CommentResponse>builder()
                .content(content)
                .totalElements(replies.getTotalElements())
                .totalPages(replies.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public CommentResponse createCommentForContent(User user, CommentDto commentDto) {
        if (commentDto.getPostId() == null && commentDto.getWatchId() == null) {
            throw new IllegalArgumentException("Post ID or Watch ID is required when creating a comment");
        }

        ContentComment.ContentCommentBuilder commentBuilder = ContentComment.builder()
                .user(user)
                .content(commentDto.getContent())
                .replyCount(0);

        Post post = null;
        Watch watch = null;

        if (commentDto.getPostId() != null) {
            post = postRepository.findById(commentDto.getPostId())
                    .orElseThrow(() -> new ResourceNotFoundException("Post not found"));
            commentBuilder.post(post);
        } else if (commentDto.getWatchId() != null) {
            watch = watchRepository.findById(commentDto.getWatchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Watch not found"));
            commentBuilder.watch(watch);
        }

        ContentComment parentComment = null;
        if (commentDto.getParentCommentId() != null) {
            parentComment = commentRepository.findById(commentDto.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            commentBuilder.parentComment(parentComment);

            parentComment.setReplyCount((parentComment.getReplyCount() != null ? parentComment.getReplyCount() : 0) + 1);
            commentRepository.save(parentComment);
        }

        ContentComment comment = commentBuilder.build();
        ContentComment savedComment = commentRepository.save(comment);

        // Determine contentId for notification
        UUID contentId = (post != null) ? post.getPostId() : (watch != null) ? watch.getWatchId() : null;

        if (post != null) {
            post.setCommentCount((post.getCommentCount() != null ? post.getCommentCount() : 0) + 1);
            postRepository.save(post);

            // Send notification to post owner if commenter is not the owner
            if (!post.getUser().getUserId().equals(user.getUserId())) {
                sendCommentNotification(user, post.getUser(), post.getPostId(), "bài viết");
            }
        } else if (watch != null) {
            watch.setCommentCount((watch.getCommentCount() != null ? watch.getCommentCount() : 0) + 1);
            watchRepository.save(watch);

            // Send notification to watch owner if commenter is not the owner
            if (!watch.getUser().getUserId().equals(user.getUserId())) {
                sendCommentNotification(user, watch.getUser(), watch.getWatchId(), "video");
            }
        }

        // Send notification to parent comment owner if this is a reply
        if (parentComment != null && contentId != null) {
            // Don't send notification if replying to own comment
            if (!parentComment.getUser().getUserId().equals(user.getUserId())) {
                sendReplyNotification(user, parentComment.getUser(), contentId);
            }
        }

        return convertToGenericCommentResponse(savedComment, user);
    }

    private void sendCommentNotification(User commenter, User contentOwner, UUID contentId, String contentType) {
        notificationExecutor.execute(() -> {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(contentOwner.getUserId())
                        .senderId(commenter.getUserId())
                        .type(String.valueOf(NotificationTypeEnum.POST_COMMENT))
                        .content(commenter.getUserProfile().getFullName() + " đã bình luận về " + contentType + " của bạn")
                        .relatedId(contentId)
                        .build();

                NotificationResponse notificationResponse = notificationService.createNotification(notificationDto);
                notificationWebSocketController.sendNotification(contentOwner.getUserId(), notificationResponse);

                log.info("Sent comment notification from {} to {}", commenter.getUserId(), contentOwner.getUserId());
            } catch (Exception e) {
                log.error("Failed to send comment notification", e);
            }
        });
    }

    private void sendReplyNotification(User replier, User commentOwner, UUID contentId) {
        notificationExecutor.execute(() -> {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(commentOwner.getUserId())
                        .senderId(replier.getUserId())
                        .type(String.valueOf(NotificationTypeEnum.POST_COMMENT))
                        .content(replier.getUserProfile().getFullName() + " đã phản hồi bình luận của bạn")
                        .relatedId(contentId)
                        .build();

                NotificationResponse notificationResponse = notificationService.createNotification(notificationDto);
                notificationWebSocketController.sendNotification(commentOwner.getUserId(), notificationResponse);

                log.info("Sent reply notification from {} to {}", replier.getUserId(), commentOwner.getUserId());
            } catch (Exception e) {
                log.error("Failed to send reply notification", e);
            }
        });
    }

    @Override
    @Transactional
    public CommentResponse updateCommentContent(UUID commentId, User user, CommentDto commentDto) {
        ContentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("You are not allowed to edit this comment");
        }

        comment.setContent(commentDto.getContent());
        ContentComment updatedComment = commentRepository.save(comment);

        return convertToGenericCommentResponse(updatedComment, user);
    }

    @Override
    @Transactional
    public void deleteCommentById(UUID commentId, User user) {
        ContentComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isCommentOwner = comment.getUser().getUserId().equals(user.getUserId());
        boolean isPostOwner = comment.getPost() != null &&
                comment.getPost().getUser().getUserId().equals(user.getUserId());
        boolean isWatchOwner = comment.getWatch() != null &&
                comment.getWatch().getUser().getUserId().equals(user.getUserId());

        if (!isCommentOwner && !isPostOwner && !isWatchOwner) {
            throw new IllegalStateException("You are not allowed to delete this comment");
        }

        Post post = comment.getPost();
        Watch watch = comment.getWatch();

        if (comment.getParentComment() != null) {
            ContentComment parent = comment.getParentComment();
            parent.setReplyCount(Math.max(0, (parent.getReplyCount() != null ? parent.getReplyCount() : 0) - 1));
            commentRepository.save(parent);
        }

        commentRepository.delete(comment);

        if (post != null) {
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
            postRepository.save(post);
        } else if (watch != null) {
            watch.setCommentCount(Math.max(0, watch.getCommentCount() - 1));
            watchRepository.save(watch);
        }
    }

    private CommentResponse convertToGenericCommentResponse(ContentComment comment, User currentUser) {
        boolean isLiked = currentUser != null &&
                commentLikeRepository.findByCommentIdAndUserId(comment.getCommentId(), currentUser.getUserId()).isPresent();

        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getUserId())
                .fullName(comment.getUser().getUserProfile().getFullName())
                .avatarImg(comment.getUser().getAvatarImg())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null)
                .replyCount(comment.getReplyCount() != null ? comment.getReplyCount() : 0)
                .likeCount(comment.getLikeCount() != null ? comment.getLikeCount() : 0)
                .liked(isLiked)
                .build();
    }
}
