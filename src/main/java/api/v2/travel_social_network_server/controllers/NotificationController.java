package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.dtos.notification.UpdateNotificationDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification APIs", description = "Endpoints for managing user notifications")
public class NotificationController {

    private final INotificationService notificationService;

    @Operation(
            summary = "Create notification",
            description = "Create a new notification for a user."
    )
    @PostMapping
    public ResponseEntity<Response<NotificationResponse>> createNotification(
            @RequestBody CreateNotificationDto createNotificationDto,
            HttpServletRequest request) {
        
        NotificationResponse notification = notificationService.createNotification(createNotificationDto);
        return ResponseEntity.ok(Response.success(notification, request.getRequestURI(), "Notification created successfully"));
    }

    @Operation(
            summary = "Get notification by ID",
            description = "Retrieve a specific notification by its ID."
    )
    @GetMapping("/{notificationId}")
    public ResponseEntity<Response<NotificationResponse>> getNotificationById(
            @PathVariable UUID notificationId,
            HttpServletRequest request) {
        
        NotificationResponse notification = notificationService.getNotificationById(notificationId);
        return ResponseEntity.ok(Response.success(notification, request.getRequestURI(), "Notification retrieved successfully"));
    }

    @Operation(
            summary = "Get user's notifications",
            description = "Retrieve paginated notifications for the authenticated user (receiver)."
    )
    @GetMapping("/me")
    public ResponseEntity<Response<PageableResponse<NotificationResponse>>> getMyNotifications(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        
        PageableResponse<NotificationResponse> notifications = notificationService
                .getNotificationsByReceiver(user.getUserId(), page, pageSize);
        return ResponseEntity.ok(Response.success(notifications, request.getRequestURI(), "Notifications retrieved successfully"));
    }

    @Operation(
            summary = "Get user's unread notifications",
            description = "Retrieve all unread notifications for the authenticated user (receiver)."
    )
    @GetMapping("/me/unread")
    public ResponseEntity<Response<List<NotificationResponse>>> getMyUnreadNotifications(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        
        List<NotificationResponse> unreadNotifications = notificationService
                .getUnreadNotificationsByReceiver(user.getUserId());
        return ResponseEntity.ok(Response.success(unreadNotifications, request.getRequestURI(), "Unread notifications retrieved successfully"));
    }

    @Operation(
            summary = "Get notifications by type",
            description = "Retrieve paginated notifications of a specific type for the authenticated user (receiver)."
    )
    @GetMapping("/me/type/{type}")
    public ResponseEntity<Response<PageableResponse<NotificationResponse>>> getNotificationsByType(
            @AuthenticationPrincipal User user,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        
        PageableResponse<NotificationResponse> notifications = notificationService
                .getNotificationsByType(user.getUserId(), type, page, pageSize);
        return ResponseEntity.ok(Response.success(notifications, request.getRequestURI(), "Notifications by type retrieved successfully"));
    }

    @Operation(
            summary = "Update notification",
            description = "Update a notification's content or read status."
    )
    @PutMapping("/{notificationId}")
    public ResponseEntity<Response<NotificationResponse>> updateNotification(
            @PathVariable UUID notificationId,
            @RequestBody UpdateNotificationDto updateNotificationDto,
            HttpServletRequest request) {
        
        NotificationResponse updatedNotification = notificationService
                .updateNotification(notificationId, updateNotificationDto);
        return ResponseEntity.ok(Response.success(updatedNotification, request.getRequestURI(), "Notification updated successfully"));
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Mark a specific notification as read."
    )
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Response<NotificationResponse>> markNotificationAsRead(
            @PathVariable UUID notificationId,
            HttpServletRequest request) {
        
        NotificationResponse notification = notificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok(Response.success(notification, request.getRequestURI(), "Notification marked as read successfully"));
    }

    @Operation(
            summary = "Mark all notifications as read",
            description = "Mark all notifications as read for the authenticated user (receiver)."
    )
    @PatchMapping("/me/read-all")
    public ResponseEntity<Response<Void>> markAllNotificationsAsRead(@AuthenticationPrincipal User user, HttpServletRequest request) {
        notificationService.markAllNotificationsAsRead(user.getUserId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "All notifications marked as read successfully"));
    }

    @Operation(
            summary = "Get unread count",
            description = "Get the count of unread notifications for the authenticated user (receiver)."
    )
    @GetMapping("/me/unread-count")
    public ResponseEntity<Response<Long>> getUnreadCount(@AuthenticationPrincipal User user, HttpServletRequest request) {
        long unreadCount = notificationService.getUnreadCount(user.getUserId());
        return ResponseEntity.ok(Response.success(unreadCount, request.getRequestURI(), "Unread count retrieved successfully"));
    }

    @Operation(
            summary = "Delete notification",
            description = "Delete a specific notification."
    )
    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Response<Void>> deleteNotification(@PathVariable UUID notificationId, HttpServletRequest request) {
        notificationService.deleteNotification(notificationId);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Notification deleted successfully"));
    }

    @Operation(
            summary = "Delete old notifications",
            description = "Delete notifications older than specified days (admin only)."
    )
    @DeleteMapping("/admin/cleanup")
    public ResponseEntity<Response<Void>> deleteOldNotifications(
            @RequestParam(defaultValue = "30") int daysOld,
            HttpServletRequest request) {
        
        notificationService.deleteOldNotifications(daysOld);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Old notifications cleaned up successfully"));
    }
}
