package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.mongodb.ConversationMessageDocument;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.chat.ConversationMessageResponse;
import api.v2.travel_social_network_server.services.chat.mongodb.ConversationMessageMongoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * MongoDB-based Message Controller
 * Handles all message operations using MongoDB for better performance
 */
@RestController
@RequestMapping("${api.base-url}/mongo/messages")
@Tag(name = "MongoDB Message APIs", description = "Endpoints for MongoDB-based message operations")
@RequiredArgsConstructor
@Slf4j
public class MongoMessageController {

    private final ConversationMessageMongoService messageMongoService;

    /**
     * Get messages from a conversation (MongoDB)
     */
    @Operation(summary = "Get messages from conversation (MongoDB)")
    @GetMapping("/conversation/{conversationId}")
    public ResponseEntity<Response<Page<ConversationMessageResponse>>> getMessages(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📥 Getting messages for conversation: {} by user: {}", conversationId, user.getUsername());

        Page<ConversationMessageResponse> messages = messageMongoService.getMessagesResponse(conversationId, page, size);

        log.info("✅ Retrieved {} messages from MongoDB", messages.getContent().size());
        return ResponseEntity.ok(Response.success(
                messages,
                request.getRequestURI(),
                "Messages retrieved successfully from MongoDB"));
    }

    /**
     * Search messages in a conversation
     */
    @Operation(summary = "Search messages in conversation")
    @GetMapping("/conversation/{conversationId}/search")
    public ResponseEntity<Response<Page<ConversationMessageDocument>>> searchMessages(
            @PathVariable UUID conversationId,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("🔍 Searching messages in conversation: {} with keyword: '{}' by user: {}",
                conversationId, keyword, user.getUsername());

        Page<ConversationMessageDocument> messages = messageMongoService.searchMessages(conversationId, keyword, page,
                size);

        log.info("✅ Found {} messages matching keyword", messages.getContent().size());
        return ResponseEntity.ok(Response.success(
                messages,
                request.getRequestURI(),
                "Messages search completed successfully"));
    }

    /**
     * Get last message in a conversation
     */
    @Operation(summary = "Get last message in conversation")
    @GetMapping("/conversation/{conversationId}/last")
    public ResponseEntity<Response<ConversationMessageDocument>> getLastMessage(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📥 Getting last message for conversation: {} by user: {}", conversationId, user.getUsername());

        ConversationMessageDocument lastMessage = messageMongoService.getLastMessage(conversationId);

        if (lastMessage == null) {
            return ResponseEntity.ok(Response.success(
                    null,
                    request.getRequestURI(),
                    "No messages found in conversation"));
        }

        return ResponseEntity.ok(Response.success(
                lastMessage,
                request.getRequestURI(),
                "Last message retrieved successfully"));
    }

    /**
     * Update message content
     */
    @Operation(summary = "Update message content")
    @PutMapping("/{messageId}")
    public ResponseEntity<Response<ConversationMessageDocument>> updateMessage(
            @PathVariable String messageId,
            @RequestParam String content,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("✏️ Updating message: {} by user: {}", messageId, user.getUsername());

        ConversationMessageDocument updatedMessage = messageMongoService.updateMessage(messageId, content);        return ResponseEntity.ok(Response.success(
                updatedMessage,
                request.getRequestURI(),
                "Message updated successfully"));
    }

    /**
     * Delete message (soft delete)
     */
    @Operation(summary = "Delete message (soft delete)")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Response<Void>> deleteMessage(
            @PathVariable String messageId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("🗑️ Deleting message: {} by user: {}", messageId, user.getUsername());

        messageMongoService.deleteMessage(messageId, user.getUserId());        return ResponseEntity.ok(Response.success(
                null,
                request.getRequestURI(),
                "Message deleted successfully"));
    }

    /**
     * Mark message as read
     */
    @Operation(summary = "Mark message as read")
    @PutMapping("/{messageId}/read")
    public ResponseEntity<Response<Void>> markMessageAsRead(
            @PathVariable String messageId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("✅ Marking message: {} as read by user: {}", messageId, user.getUsername());

        messageMongoService.markMessageAsRead(messageId);

        return ResponseEntity.ok(Response.success(
                null,
                request.getRequestURI(),
                "Message marked as read"));
    }

    /**
     * Count unread messages in a conversation
     */
    @Operation(summary = "Count unread messages")
    @GetMapping("/conversation/{conversationId}/unread-count")
    public ResponseEntity<Response<Long>> countUnreadMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) Long lastReadTimestamp,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📊 Counting unread messages for conversation: {} by user: {}",
                conversationId, user.getUsername());

        Instant lastReadAt = lastReadTimestamp != null
                ? Instant.ofEpochMilli(lastReadTimestamp)
                : Instant.now().minusSeconds(86400); // Default: last 24 hours

        long count = messageMongoService.countUnreadMessages(conversationId, user.getUserId(), lastReadAt);        return ResponseEntity.ok(Response.success(
                count,
                request.getRequestURI(),
                "Unread message count retrieved successfully"));
    }

    /**
     * Get unread messages in a conversation
     */
    @Operation(summary = "Get unread messages")
    @GetMapping("/conversation/{conversationId}/unread")
    public ResponseEntity<Response<List<ConversationMessageDocument>>> getUnreadMessages(
            @PathVariable UUID conversationId,
            @RequestParam(required = false) Long lastReadTimestamp,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📥 Getting unread messages for conversation: {} by user: {}",
                conversationId, user.getUsername());

        Instant lastReadAt = lastReadTimestamp != null
                ? Instant.ofEpochMilli(lastReadTimestamp)
                : Instant.now().minusSeconds(86400); // Default: last 24 hours

        List<ConversationMessageDocument> messages = messageMongoService.getUnreadMessages(
                conversationId, user.getUserId(), lastReadAt);

        log.info("✅ Found {} unread messages", messages.size());
        return ResponseEntity.ok(Response.success(
                messages,
                request.getRequestURI(),
                "Unread messages retrieved successfully"));
    }

    /**
     * Get messages by type (e.g., images, videos)
     */
    @Operation(summary = "Get messages by type")
    @GetMapping("/conversation/{conversationId}/type/{type}")
    public ResponseEntity<Response<Page<ConversationMessageDocument>>> getMessagesByType(
            @PathVariable UUID conversationId,
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📥 Getting {} messages for conversation: {} by user: {}",
                type, conversationId, user.getUsername());

        Page<ConversationMessageDocument> messages = messageMongoService.getMessagesByType(
                conversationId, type, page, size);

        log.info("✅ Found {} {} messages", messages.getContent().size(), type);
        return ResponseEntity.ok(Response.success(
                messages,
                request.getRequestURI(),
                String.format("%s messages retrieved successfully", type)));
    }

    /**
     * Get thread messages (replies to a message)
     */
    @Operation(summary = "Get thread messages")
    @GetMapping("/{messageId}/thread")
    public ResponseEntity<Response<List<ConversationMessageDocument>>> getThreadMessages(
            @PathVariable String messageId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📥 Getting thread messages for message: {} by user: {}", messageId, user.getUsername());

        List<ConversationMessageDocument> messages = messageMongoService.getThreadMessages(messageId);

        log.info("✅ Found {} thread messages", messages.size());
        return ResponseEntity.ok(Response.success(
                messages,
                request.getRequestURI(),
                "Thread messages retrieved successfully"));
    }

    /**
     * Count total messages in a conversation
     */
    @Operation(summary = "Count total messages in conversation")
    @GetMapping("/conversation/{conversationId}/count")
    public ResponseEntity<Response<Long>> countMessages(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        log.info("📊 Counting total messages for conversation: {} by user: {}",
                conversationId, user.getUsername());

        long count = messageMongoService.countMessages(conversationId);        return ResponseEntity.ok(Response.success(
                count,
                request.getRequestURI(),
                "Message count retrieved successfully"));
    }
}
