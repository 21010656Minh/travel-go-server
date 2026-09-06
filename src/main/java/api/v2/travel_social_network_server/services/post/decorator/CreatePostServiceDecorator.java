package api.v2.travel_social_network_server.services.post.decorator;

import api.v2.travel_social_network_server.controllers.ws.NotificationWebSocketController;
import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.dtos.post.UpdatePostDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.services.friendship.IFriendshipService;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import api.v2.travel_social_network_server.services.post.ICreatePostService;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

@Service("createPostServiceDecorator")
@Slf4j
public class CreatePostServiceDecorator implements ICreatePostService {

    private final ICreatePostService wrapped;
    private final INotificationService notificationService;
    private final IFriendshipService friendshipService;
    private final Executor notificationExecutor;
    private final NotificationWebSocketController notificationWebSocketController;

    public CreatePostServiceDecorator(@Qualifier("postService") ICreatePostService wrapped,
                                      INotificationService notificationService,
                                      IFriendshipService friendshipService,
                                      @Qualifier("taskExecutor") Executor notificationExecutor,
                                      NotificationWebSocketController notificationWebSocketController) {
        this.wrapped = wrapped;
        this.notificationService = notificationService;
        this.friendshipService = friendshipService;
        this.notificationExecutor = notificationExecutor;
        this.notificationWebSocketController = notificationWebSocketController;
    }


    @Override
    public PostResponse createPostMultiTask(User user, UpdatePostDto dto, UUID groupId) {
        PostResponse response = wrapped.createPostMultiTask(user, dto, groupId);

        // Gửi thông báo async cho tất cả bạn bè - Sử dụng proxy để @Async hoạt động
        try {
            ((CreatePostServiceDecorator) AopContext.currentProxy()).notifyFriendsAsync(user, response);
        } catch (IllegalStateException e) {
            // Fallback nếu không có proxy (trong test hoặc không enable expose-proxy)
            log.warn("AopContext not available, calling async method directly (may not be async)");
            notifyFriendsAsync(user, response);
        }

        return response;
    }

    @Async("taskExecutor")
    public void notifyFriendsAsync(User user, PostResponse postResponse) {
        List<UserResponse> friends = friendshipService.getFriends(user.getUserId());

        for (UserResponse friend : friends) {
            notificationExecutor.execute(() -> {
                log.info("notifyFriendsAsync: user={}, friend={}", user.getUsername(), friend.getUserName());

                // 1. Lưu thông báo vào DB
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(friend.getUserId())
                        .senderId(user.getUserId())
                        .type(String.valueOf(NotificationTypeEnum.NEW_POST))
                        .content(user.getUserProfile().getFullName() + " đã tạo một bài viết mới")
                        .relatedId(postResponse.getPostId())
                        .build();

                NotificationResponse notificationResponse = notificationService.createNotification(notificationDto);

                // 2. Gửi realtime qua WebSocket
                notificationWebSocketController.sendNotification(friend.getUserId(), notificationResponse);
            });
        }
    }
}
