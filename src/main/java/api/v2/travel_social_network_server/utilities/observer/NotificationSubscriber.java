package api.v2.travel_social_network_server.utilities.observer;

import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import io.swagger.v3.oas.annotations.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component("notificationSubscriber")
@RequiredArgsConstructor
@Service
@Slf4j
public class NotificationSubscriber implements Subscriber<User> {
    
    private final INotificationService notificationService;
    
    @Override
    @Async("taskExecutor")
    public void onMessage(User user) {
        log.info("Sending password change notification to user: {}", user.getUserId());
        
        CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                .receiverId(user.getUserId())
                .senderId(null) // System notification
                .type(String.valueOf(NotificationTypeEnum.SYSTEM))
                .content("Mật khẩu của bạn đã được thay đổi thành công. Nếu bạn không thực hiện thay đổi này, vui lòng liên hệ hỗ trợ ngay lập tức.")
                .relatedId(null)
                .build();
        
        notificationService.createNotification(notificationDto);
        
        log.info("Password change notification sent to user: {}", user.getUserId());
    }
}
