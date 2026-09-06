package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.group.JoinGroupResponse;
import api.v2.travel_social_network_server.dtos.group.UpdateGroupDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.group.GroupMemberResponse;
import api.v2.travel_social_network_server.responses.post.PostMediaResponse;
import api.v2.travel_social_network_server.services.group.IGroupService;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("${api.base-url}/group")
@Tag(name = "Group APIs", description = "Endpoints for creating and managing groups")
public class GroupController {

    private final IGroupService groupService;
    private final IPostService postService;

    public GroupController(
            @Qualifier("dynamicGroupServiceProxy") IGroupService groupService,
            @Qualifier("postService") IPostService postService) {
        this.groupService = groupService;
        this.postService = postService;
    }

    @Operation(summary = "Get group by ID", description = "Retrieve details of a group using its ID")
    @GetMapping("/{groupId}")
    public ResponseEntity<Response<GroupResponse>> getGroupById(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        GroupResponse group = groupService.getGroupById(groupId, user);
        return ResponseEntity.ok(Response.success(group, request.getRequestURI(), "Get group successfully"));
    }

    @Operation(summary = "Search groups", description = "Search groups by keyword with pagination")
    @GetMapping
    public ResponseEntity<Response<PageableResponse<GroupResponse>>> getAllGroupsByName(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String keyword,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<GroupResponse> groups = groupService.getAllGroupsByName(user, keyword, page, size);
        return ResponseEntity.ok(Response.success(groups, request.getRequestURI(), "Search groups successfully"));
    }

    @Operation(summary = "Search groups with fulltext", description = "Search groups using PostgreSQL fulltext search")
    @GetMapping("/search/fulltext")
    public ResponseEntity<Response<PageableResponse<GroupResponse>>> searchGroupsFulltext(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        PageableResponse<GroupResponse> groups = groupService.searchGroupsFulltext(keyword, page, size, user);
        return ResponseEntity.ok(Response.success(groups, request.getRequestURI(), "Fulltext search completed successfully"));
    }

    @Operation(summary = "Create group", description = "Create a new group with name, description, and optional image")
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<Response<GroupResponse>> createGroup(
            @AuthenticationPrincipal User user,
            @ModelAttribute UpdateGroupDto updateGroupDto,
            HttpServletRequest request
    ) throws IOException {
        GroupResponse group = groupService.createGroup(user, updateGroupDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(group, request.getRequestURI(), "Group successfully created"));
    }

    @Operation(summary = "Update group", description = "Update group information including name, description, privacy, and cover image (owner/admin only)")
    @PutMapping(value = "/{groupId}", consumes = "multipart/form-data")
    public ResponseEntity<Response<GroupResponse>> updateGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            @ModelAttribute UpdateGroupDto updateGroupDto,
            HttpServletRequest request
    ) throws IOException {
        GroupResponse group = groupService.updateGroup(groupId, user, updateGroupDto);
        return ResponseEntity.ok(Response.success(group, request.getRequestURI(), "Group updated successfully"));
    }

    @Operation(summary = "Join group", description = "Send a join request to a group")
    @PostMapping("/{groupId}/join")
    public ResponseEntity<Response<JoinGroupResponse>> joinGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        JoinGroupResponse response = groupService.joinGroup(groupId, user);
        String message = response.getIsMember() 
            ? "You have joined the group successfully" 
            : "Join group request sent successfully";
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), message));
    }

    @Operation(summary = "Leave group", description = "Leave a group you are a member of")
    @PostMapping("/{groupId}/leave")
    public ResponseEntity<Response<Void>> leaveGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        groupService.leaveGroup(groupId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "You have left the group"));
    }

    @Operation(summary = "Approve join request", description = "Approve a user's join request (admin only)")
    @PostMapping("/{groupId}/members/{targetUserId}/approve")
    public ResponseEntity<Response<GroupMemberResponse>> approveJoinRequest(
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal User admin,
            HttpServletRequest request
    ) {
        GroupMemberResponse member = groupService.approveJoinRequest(groupId, targetUserId, admin.getUserId());
        return ResponseEntity.ok(Response.success(member, request.getRequestURI(), "Join request approved successfully"));
    }

    @Operation(summary = "Reject join request", description = "Reject a user's join request (admin only)")
    @PostMapping("/{groupId}/members/{targetUserId}/reject")
    public ResponseEntity<Response<Void>> rejectJoinRequest(
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal User admin,
            HttpServletRequest request
    ) {
        groupService.rejectJoinRequest(groupId, targetUserId, admin.getUserId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Join request rejected successfully"));
    }

    @Operation(summary = "Remove member", description = "Remove a user from the group (admin only)")
    @DeleteMapping("/{groupId}/members/{targetUserId}")
    public ResponseEntity<Response<Void>> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal User admin,
            HttpServletRequest request
    ) {
        groupService.removeMemberFromGroup(groupId, targetUserId, admin.getUserId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Member removed successfully"));
    }

    @Operation(summary = "Change member role", description = "Update the role of a group member (admin only)")
    @PutMapping("/{groupId}/members/{targetUserId}/role")
    public ResponseEntity<Response<Void>> changeMemberRole(
            @PathVariable UUID groupId,
            @PathVariable UUID targetUserId,
            @RequestParam String newRole,
            @AuthenticationPrincipal User admin,
            HttpServletRequest request
    ) {
        groupService.changeMemberRole(groupId, targetUserId, admin.getUserId(), newRole);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Member role updated successfully"));
    }

    @Operation(summary = "Get user's groups", description = "Retrieve all groups the current user is a member of, with pagination")
    @GetMapping("/my-groups")
    public ResponseEntity<Response<PageableResponse<GroupResponse>>> getGroupsOfUser(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpServletRequest request
    ) {
        PageableResponse<GroupResponse> groups = groupService.getGroupsOfUser(user.getUserId(), page, size);
        return ResponseEntity.ok(Response.success(groups, request.getRequestURI(), "Fetched user's groups successfully"));
    }

    @Operation(summary = "Get pending groups", description = "Retrieve groups where user has pending join requests")
    @GetMapping("/pending-groups")
    public ResponseEntity<Response<PageableResponse<GroupResponse>>> getPendingGroups(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            HttpServletRequest request
    ) {
        PageableResponse<GroupResponse> groups = groupService.getPendingGroupsOfUser(user.getUserId(), page, size);
        return ResponseEntity.ok(Response.success(groups, request.getRequestURI(), "Fetched pending groups successfully"));
    }

    @Operation(summary = "Get group members", description = "Retrieve all members of a group with pagination and filtering")
    @GetMapping("/{groupId}/members")
    public ResponseEntity<Response<PageableResponse<GroupMemberResponse>>> getGroupMembers(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "all") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request
    ) {
        try {
            log.info(">>> Controller: getGroupMembers called - groupId={}, userId={}, keyword={}, filter={}, page={}, size={}", 
                groupId, user.getUserId(), keyword, filter, page, size);
            
            PageableResponse<GroupMemberResponse> members = groupService.getGroupMembers(groupId, user, keyword, filter, page, size);
            
            log.info("<<< Controller: getGroupMembers success - returned {} members", members.getContent().size());
            return ResponseEntity.ok(Response.success(members, request.getRequestURI(), "Fetched group members successfully"));
        } catch (Exception e) {
            log.error("XXX Controller: getGroupMembers ERROR", e);
            log.error("Error type: {}", e.getClass().getName());
            log.error("Error message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }
            throw e;
        }
    }

    @Operation(summary = "Get group media", description = "Retrieve photos or videos from a group")
    @GetMapping("/{groupId}/media")
    public ResponseEntity<Response<List<PostMediaResponse>>> getGroupMedia(
            @PathVariable UUID groupId,
            @RequestParam(defaultValue = "IMAGE") MediaTypeEnum mediaType,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        List<PostMediaResponse> media = postService.getMediaByGroupIdAndType(groupId, mediaType);
        
        log.info("<<< Controller: getGroupMedia success - returned {} media items", media.size());
        return ResponseEntity.ok(Response.success(media, request.getRequestURI(), "Fetched group media successfully"));
    }

    @Operation(summary = "Lock group", description = "Lock a group (admin only)")
    @PostMapping("/{groupId}/lock")
    public ResponseEntity<Response<Void>> lockGroup(
            @PathVariable UUID groupId,
            @RequestParam String reason,
            @AuthenticationPrincipal User admin,
            HttpServletRequest request
    ) {
        groupService.lockGroup(groupId, admin.getUserId(), reason);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Group locked successfully"));
    }

    @Operation(summary = "Unlock group", description = "Unlock a group (admin only)")
    @PostMapping("/{groupId}/unlock")
    public ResponseEntity<Response<Void>> unlockGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User admin,
            HttpServletRequest request
    ) {
        groupService.unlockGroup(groupId, admin.getUserId());
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Group unlocked successfully"));
    }
}
