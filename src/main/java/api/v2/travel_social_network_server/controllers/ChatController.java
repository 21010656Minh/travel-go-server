package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.entities.ConversationMessage;
import api.v2.travel_social_network_server.entities.ConversationMember;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.chat.ConversationMemberResponse;
import api.v2.travel_social_network_server.services.chat.conversationmember.IConversationMemberService;
import api.v2.travel_social_network_server.services.chat.conversationmessage.IConversationMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/chat")
@Tag(name = "Chat APIs", description = "Endpoints for conversations, members, and messaging")
@RequiredArgsConstructor
public class ChatController {

    private final IConversationMemberService conversationMemberService;
    private final IConversationMessageService conversationMessageService;

    // ==================== MEMBERS ====================

    @Operation(summary = "Get members of a group with pagination")
    @GetMapping("/members")
    public ResponseEntity<Response<Page<?>>> getMembersByGroup(
            @RequestParam UUID conversationId,
            @RequestParam int page,
            @RequestParam int size,
            HttpServletRequest request
    ) {
        Page<ConversationMemberResponse> members = conversationMemberService.getConversationMembersByConversationId(conversationId, page, size);
        return ResponseEntity.ok(Response.success(members, request.getRequestURI(), "Group members retrieved successfully"));
    }

    @Operation(summary = "Get groups of a user with pagination")
    @GetMapping("/members/user-groups")
    public ResponseEntity<Response<Page<ConversationMember>>> getGroupsByUser(
            @RequestParam UUID userId,
            @RequestParam int page,
            @RequestParam int size,
            HttpServletRequest request
    ) {
        Page<ConversationMember> userGroups = conversationMemberService.getConversationsByUser(userId, page, size);
        return ResponseEntity.ok(Response.success(userGroups, request.getRequestURI(), "User groups retrieved successfully"));
    }

    // ==================== MESSAGES ====================

    @Operation(summary = "Send a message to a conversation")
    @PostMapping("/messages")
    public ResponseEntity<Response<ConversationMessage>> sendMessage(
            @RequestParam UUID conversationId,
            @RequestParam String content,
            @RequestParam String type,
            @RequestParam(required = false) String mediaUrl,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Conversation conversation = Conversation.builder().conversationId(conversationId).build();
        ConversationMessage message = conversationMessageService.sendMessage(conversation, user, content, type, mediaUrl);
        return ResponseEntity.ok(Response.success(message, request.getRequestURI(), "Message sent successfully"));
    }

    @Operation(summary = "Get messages of a conversation with pagination")
    @GetMapping("/messages")
    public ResponseEntity<Response<Page<ConversationMessage>>> getMessages(
            @RequestParam UUID conversationId,
            @RequestParam int page,
            @RequestParam int size,
            HttpServletRequest request
    ) {
        Page<ConversationMessage> messages = conversationMessageService.getMessages(conversationId, page, size);
        return ResponseEntity.ok(Response.success(messages, request.getRequestURI(), "Messages retrieved successfully"));
    }

    @Operation(summary = "Search messages in a conversation by keyword with pagination")
    @GetMapping("/messages/search")
    public ResponseEntity<Response<Page<ConversationMessage>>> searchMessages(
            @RequestParam UUID conversationId,
            @RequestParam String keyword,
            @RequestParam int page,
            @RequestParam int size,
            HttpServletRequest request
    ) {
        Page<ConversationMessage> messages = conversationMessageService.searchMessages(conversationId, keyword, page, size);
        return ResponseEntity.ok(Response.success(messages, request.getRequestURI(), "Messages search completed"));
    }
}
