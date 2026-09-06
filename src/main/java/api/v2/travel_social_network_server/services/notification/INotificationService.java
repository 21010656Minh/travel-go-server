package api.v2.travel_social_network_server.services.notification;

import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.dtos.notification.UpdateNotificationDto;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    
    // Create notification
    NotificationResponse createNotification(CreateNotificationDto createNotificationDto);
    
    // Get notification by ID
    NotificationResponse getNotificationById(UUID notificationId);
    
    // Get notifications by receiver with pagination
    PageableResponse<NotificationResponse> getNotificationsByReceiver(UUID receiverId, int page, int pageSize);
    
    // Get unread notifications by receiver
    List<NotificationResponse> getUnreadNotificationsByReceiver(UUID receiverId);
    
    // Get notifications by type
    PageableResponse<NotificationResponse> getNotificationsByType(UUID receiverId, String type, int page, int pageSize);
    
    // Update notification
    NotificationResponse updateNotification(UUID notificationId, UpdateNotificationDto updateNotificationDto);
    
    // Mark notification as read
    NotificationResponse markNotificationAsRead(UUID notificationId);
    
    // Mark all notifications as read for a user
    void markAllNotificationsAsRead(UUID receiverId);
    
    // Delete notification
    void deleteNotification(UUID notificationId);
    
    // Get unread count for a user
    long getUnreadCount(UUID receiverId);
    
    // Delete old notifications
    void deleteOldNotifications(int daysOld);
}
