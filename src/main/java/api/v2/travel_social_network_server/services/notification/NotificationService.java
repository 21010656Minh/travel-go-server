package api.v2.travel_social_network_server.services.notification;

import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.dtos.notification.UpdateNotificationDto;
import api.v2.travel_social_network_server.entities.Notification;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.NotificationRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public NotificationResponse createNotification(CreateNotificationDto dto) {
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Receiver user not found"));

        User sender = null;
        if (dto.getSenderId() != null) {
            sender = userRepository.findById(dto.getSenderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sender user not found"));
        }

        Notification notification = Notification.builder()
                .receiver(receiver)
                .sender(sender)
                .type( NotificationTypeEnum.fromString(dto.getType()))
                .content(dto.getContent())
                .relatedId(dto.getRelatedId())
                .isRead(false)
                .build();

        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification: {} for user: {}", savedNotification.getNotificationId(), receiver.getUserId());

        return mapToNotificationResponse(savedNotification);
    }

    @Override
    @Transactional(readOnly = true)
    public NotificationResponse getNotificationById(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        return mapToNotificationResponse(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<NotificationResponse> getNotificationsByReceiver(UUID receiverId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Notification> notificationPage = notificationRepository
                .findByReceiver_UserIdOrderByCreatedAtDesc(receiverId, pageable);

        List<NotificationResponse> notifications = notificationPage.getContent()
                .stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());

        return PageableResponse.<NotificationResponse>builder()
                .content(notifications)
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotificationsByReceiver(UUID receiverId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByReceiver_UserIdAndIsReadFalseOrderByCreatedAtDesc(receiverId);

        return unreadNotifications.stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<NotificationResponse> getNotificationsByType(UUID receiverId, String type, int page, int pageSize) {
        NotificationTypeEnum notificationType = NotificationTypeEnum.fromString(type);
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Notification> notificationPage = notificationRepository
                .findByReceiver_UserIdAndTypeOrderByCreatedAtDesc(receiverId, notificationType, pageable);

        List<NotificationResponse> notifications = notificationPage.getContent()
                .stream()
                .map(this::mapToNotificationResponse)
                .collect(Collectors.toList());

        return PageableResponse.<NotificationResponse>builder()
                .content(notifications)
                .totalElements(notificationPage.getTotalElements())
                .totalPages(notificationPage.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public NotificationResponse updateNotification(UUID notificationId, UpdateNotificationDto dto) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (dto.getContent() != null) {
            notification.setContent(dto.getContent());
        }
        notification.setRead(dto.isRead());

        Notification updatedNotification = notificationRepository.save(notification);        return mapToNotificationResponse(updatedNotification);
    }

    @Override
    @Transactional
    public NotificationResponse markNotificationAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);        return mapToNotificationResponse(updatedNotification);
    }

    @Override
    @Transactional
    public void markAllNotificationsAsRead(UUID receiverId) {
        notificationRepository.markAllAsReadByReceiverId(receiverId);    }

    @Override
    @Transactional
    public void deleteNotification(UUID notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            throw new ResourceNotFoundException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(UUID receiverId) {
        return notificationRepository.countByReceiver_UserIdAndIsReadFalse(receiverId);
    }

    @Override
    @Transactional
    public void deleteOldNotifications(int daysOld) {
        Instant cutoffDate = Instant.now().minus(daysOld, ChronoUnit.DAYS);
        notificationRepository.deleteOldNotifications(cutoffDate);    }

    private NotificationResponse mapToNotificationResponse(Notification notification) {
        UserSummaryResponse sender = null;
        if (notification.getSender() != null) {
            sender = UserSummaryResponse.builder()
                    .userId(notification.getSender().getUserId())
                    .userName(notification.getSender().getUserProfile().getFullName())
                    .email(notification.getSender().getEmail())
                    .avatarImg(notification.getSender().getAvatarImg())
                    .build();
        }

        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .receiverId(notification.getReceiver().getUserId())
                .receiverName(notification.getReceiver().getUserProfile().getFullName())
                .sender(sender)
                .type(notification.getType())
                .content(notification.getContent())
                .relatedId(notification.getRelatedId())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
