package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.ConversationMessage;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.services.chat.conversationmessage.ConversationMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
@Slf4j
public class ConversationMessageController {

    private final ConversationMessageService messageService;

    /**
     * Lấy tin nhắn trong group
     */
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<Page<ConversationMessage>> getGroupMessages(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User user
    ) {
        log.info("Getting messages for group: {} by user: {}", groupId, user.getUsername());
        
        Page<ConversationMessage> messages = messageService.getMessages(groupId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Tìm kiếm tin nhắn trong group
     */
    @GetMapping("/groups/{groupId}/messages/search")
    public ResponseEntity<Page<ConversationMessage>> searchMessages(
            @PathVariable UUID groupId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user
    ) {
        log.info("Searching messages in group: {} with keyword: {} by user: {}", 
                groupId, keyword, user.getUsername());
        
        Page<ConversationMessage> messages = messageService.searchMessages(groupId, keyword, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * Đánh dấu tin nhắn đã đọc
     */
    @PutMapping("/messages/{messageId}/read")
    public ResponseEntity<Void> markMessageAsRead(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Marking message: {} as read by user: {}", messageId, user.getUsername());
        
        messageService.markMessageAsRead(messageId);
        return ResponseEntity.ok().build();
    }

    /**
     * Đánh dấu tất cả tin nhắn trong group đã đọc
     */
    @PutMapping("/groups/{groupId}/messages/read-all")
    public ResponseEntity<Void> markAllMessagesAsRead(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Marking all messages in group: {} as read by user: {}", 
                groupId, user.getUsername());
        
        messageService.markAllMessagesAsRead(groupId, user.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * Lấy số tin nhắn chưa đọc trong group
     */
    @GetMapping("/groups/{groupId}/unread-count")
    public ResponseEntity<Long> getUnreadMessageCount(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Getting unread message count for group: {} by user: {}", 
                groupId, user.getUsername());
        
        long count = messageService.getUnreadMessageCount(groupId, user.getUserId());
        return ResponseEntity.ok(count);
    }

    /**
     * Lấy tin nhắn chưa đọc trong group
     */
    @GetMapping("/groups/{groupId}/unread-messages")
    public ResponseEntity<List<ConversationMessage>> getUnreadMessages(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Getting unread messages for group: {} by user: {}", 
                groupId, user.getUsername());
        
        List<ConversationMessage> messages = messageService.getUnreadMessages(groupId, user.getUserId());
        return ResponseEntity.ok(messages);
    }

    /**
     * Xóa tin nhắn
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable UUID messageId,
            @AuthenticationPrincipal User user
    ) {
        log.info("Deleting message: {} by user: {}", messageId, user.getUsername());
        
        messageService.deleteMessage(messageId, user.getUserId());
        return ResponseEntity.ok().build();
    }
}
