package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.chat.group.CreateGroupChatRequest;
import api.v2.travel_social_network_server.dtos.chat.group.GroupMemberRequest;
import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.repositories.MediaRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.chat.ConversationMemberResponse;
import api.v2.travel_social_network_server.responses.chat.ConversationResponse;
import api.v2.travel_social_network_server.responses.chat.SearchConversationResponse;
import api.v2.travel_social_network_server.services.chat.chat.ChatService;
import api.v2.travel_social_network_server.services.chat.conversation.IConversationService;
import api.v2.travel_social_network_server.services.chat.conversationmember.IConversationMemberService;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.ConversationTypeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversation APIs", description = "Endpoints for managing conversations and chats")
@Slf4j
public class ConversationController {

    private final IConversationService conversationService;
    private final ChatService chatService;
    private final IConversationMemberService conversationMemberService;
    private final MediaRepository mediaRepository;
    private final MediaStorage mediaStorage;

    @Operation(summary = "Get conversation by ID", description = "Retrieve details of a conversation using its ID")
    @GetMapping("/{conversationId}")
    public ResponseEntity<Response<ConversationResponse>> getConversationById(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        var conversation = conversationService.getConversationByConversationId(conversationId, user.getUserId());
        return ResponseEntity
                .ok(Response.success(conversation, request.getRequestURI(), "Get conversation successfully"));
    }

    @Operation(summary = "Create group conversation", description = "Create a new group chat")
    @PostMapping
    public ResponseEntity<Response<Conversation>> createGroupConversation(
            @RequestBody CreateGroupChatRequest req,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        var conversation = conversationService.createGroupConversation(req.getGroupName(), user.getUserId(),
                req.getMemberIds());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(conversation, request.getRequestURI(), "Group created successfully"));
    }

    @Operation(summary = "Create or get private conversation", description = "Create a private chat between two users or get existing one")
    @PostMapping("/private/{friendUserId}")
    public ResponseEntity<Response<ConversationResponse>> createOrGetPrivateConversation(
            @PathVariable UUID friendUserId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        var privateChat = conversationService.createOrGetPrivateConversation(user.getUserId(), friendUserId);
        return ResponseEntity.ok(
                Response.success(privateChat, request.getRequestURI(), "Private chat retrieved/created successfully"));
    }

    @Operation(summary = "Get user's conversations", description = "Get all conversations for the current user")
    @GetMapping("/my")
    public ResponseEntity<Response<PageableResponse<ConversationResponse>>> getUserConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) ConversationTypeEnum type,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        var conversations = conversationService.getUserConversations(user.getUserId(), page, size, type);
        return ResponseEntity
                .ok(Response.success(conversations, request.getRequestURI(), "Get user conversations successfully"));
    }

    @Operation(summary = "Search conversations", description = "Search group conversations by keyword")
    @GetMapping("/search")
    public ResponseEntity<Response<PageableResponse<ConversationResponse>>> searchConversations(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        var results = conversationService.searchConversationsByUerIdAndKeyWord(user.getUserId(), keyword, page, size);
        return ResponseEntity
                .ok(Response.success(results, request.getRequestURI(), "Search conversations successfully"));
    }

    @Operation(summary = "Search all groups and friends", description = "Search both groups and friends by keyword")
    @GetMapping("/search-all")
    public ResponseEntity<Response<PageableResponse<SearchConversationResponse>>> searchAllGroupsAndFriends(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        var result = conversationService.searchConversationsIncludeFriends(user.getUserId(), keyword, page, size);
        return ResponseEntity.ok(Response.success(result, request.getRequestURI(), "Search completed successfully"));
    }

    @Operation(summary = "Add members to conversation", description = "Add new members to an existing conversation")
    @PostMapping("/{conversationId}/members")
    public ResponseEntity<Response<Void>> addMembers(
            @PathVariable UUID conversationId,
            @RequestBody GroupMemberRequest req,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        conversationService.addMembersToConversation(conversationId, req.getUserIds(), user.getUserId());
        
        return ResponseEntity.ok(
            Response.success(null, request.getRequestURI(), "Members added successfully"));
    }

    @Operation(summary = "Remove members from conversation", description = "Remove members from a conversation")
    @DeleteMapping("/{conversationId}/members")
    public ResponseEntity<Response<Void>> removeMembers(
            @PathVariable UUID conversationId,
            @RequestBody GroupMemberRequest req,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        // Xóa từng member một
        for (UUID memberUserId : req.getUserIds()) {
            conversationService.removeMemberFromConversation(conversationId, memberUserId, user.getUserId());
        }
        
        return ResponseEntity.ok(
            Response.success(null, request.getRequestURI(), "Members removed successfully"));
    }

    @Operation(summary = "Leave conversation", description = "Leave a conversation")
    @PostMapping("/{conversationId}/leave")
    public ResponseEntity<Response<Void>> leaveConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        throw new UnsupportedOperationException("leaveConversation not implemented yet");
    }

    /**
     * Lấy danh sách user online
     */
    @GetMapping("/online-users")
    public ResponseEntity<List<String>> getOnlineUsers() {
        List<String> onlineUsers = chatService.getOnlineUsers().stream().toList();
        return ResponseEntity.ok(onlineUsers);
    }

    /**
     * Kiểm tra user có online không
     */
    @GetMapping("/users/{userId}/online-status")
    public ResponseEntity<Boolean> isUserOnline(@PathVariable UUID userId) {
        boolean isOnline = chatService.isUserOnline(userId);
        return ResponseEntity.ok(isOnline);
    }
    
    @Operation(summary = "Get conversation members", description = "Get all members of a conversation")
    @GetMapping("/{conversationId}/members")
    public ResponseEntity<Response<Page<ConversationMemberResponse>>> getConversationMembers(
            @PathVariable UUID conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        Page<ConversationMemberResponse> members = conversationMemberService.getConversationMembersByConversationId(
                conversationId, page, size);
        return ResponseEntity.ok(
                Response.success(members, request.getRequestURI(), "Get conversation members successfully"));
    }
    
    @Operation(summary = "Get conversation media", description = "Get all media (images/videos) from a conversation")
    @GetMapping("/{conversationId}/media")
    public ResponseEntity<Response<List<ContentMedia>>> getConversationMedia(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        List<ContentMedia> media = mediaRepository.findByConversationId(conversationId);
        return ResponseEntity.ok(
                Response.success(media, request.getRequestURI(), "Get conversation media successfully"));
    }
    
    @Operation(summary = "Update group avatar", description = "Update avatar for a group conversation")
    @PutMapping("/{conversationId}/avatar")
    public ResponseEntity<Response<ConversationResponse>> updateGroupAvatar(
            @PathVariable UUID conversationId,
            @RequestParam("avatar") MultipartFile avatarFile,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        try {
            // Validate file
            if (avatarFile.isEmpty()) {
                throw new IllegalArgumentException("Avatar file is empty");
            }
            
            // Validate file size (max 5MB)
            if (avatarFile.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("Avatar file size exceeds 5MB limit");
            }
            
            // Validate file type
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed for avatar");
            }
            
            // Upload avatar to storage
            String avatarUrl = mediaStorage.uploadFile(avatarFile.getBytes(), "avatars", contentType);
            
            // Update conversation avatar
            conversationService.updateGroupAvatar(conversationId, avatarUrl, user.getUserId());
            
            // Convert to response
            ConversationResponse response = conversationService.getConversationByConversationId(
                    conversationId, user.getUserId());
            
            return ResponseEntity.ok(
                    Response.success(response, request.getRequestURI(), "Group avatar updated successfully"));
        } catch (Exception e) {
            log.error("Error updating group avatar: {}", e.getMessage());
            throw new RuntimeException("Failed to update group avatar: " + e.getMessage());
        }
    }
    
    @Operation(summary = "Update group name", description = "Update name for a group conversation")
    @PutMapping("/{conversationId}/name")
    public ResponseEntity<Response<ConversationResponse>> updateGroupName(
            @PathVariable UUID conversationId,
            @RequestBody java.util.Map<String, String> body,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        String groupName = body.get("groupName");
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }
        
        conversationService.updateGroupName(conversationId, groupName.trim(), user.getUserId());
        ConversationResponse response = conversationService.getConversationByConversationId(
                conversationId, user.getUserId());
        
        return ResponseEntity.ok(
                Response.success(response, request.getRequestURI(), "Group name updated successfully"));
    }
    
    @Operation(summary = "Delete conversation", description = "Delete a conversation (admin only for groups)")
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Response<Void>> deleteConversation(
            @PathVariable UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {
        conversationService.deleteConversation(conversationId, user.getUserId());
        return ResponseEntity.ok(
                Response.success(null, request.getRequestURI(), "Conversation deleted successfully"));
    }
}
