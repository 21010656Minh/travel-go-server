package api.v2.travel_social_network_server.services.group;

import api.v2.travel_social_network_server.controllers.ws.NotificationWebSocketController;
import api.v2.travel_social_network_server.dtos.group.JoinGroupResponse;
import api.v2.travel_social_network_server.dtos.group.UpdateGroupDto;
import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.entities.*;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.repositories.PostRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.group.FriendMemberDto;
import api.v2.travel_social_network_server.responses.group.GroupMemberResponse;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.repositories.GroupRepository;
import api.v2.travel_social_network_server.repositories.GroupMemberRepository;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.GroupMemberStatusTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service("groupService")
@Slf4j
public class GroupService implements IGroupService {

    private static final String GROUP_FOLDER = "groups";

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final PostRepository postRepository;
    private final MediaStorage cloudinaryService;
    private final UserRepository userRepository;
    private final INotificationService notificationService;
    private final NotificationWebSocketController notificationWebSocketController;
    private final Executor notificationExecutor;
    private final api.v2.travel_social_network_server.repositories.ContentModerationRepository contentModerationRepository;

    public GroupService(GroupRepository groupRepository,
                       GroupMemberRepository groupMemberRepository,
                       PostRepository postRepository,
                       @Qualifier("minioStorageAdapter") MediaStorage cloudinaryService,
                       UserRepository userRepository,
                       INotificationService notificationService,
                       NotificationWebSocketController notificationWebSocketController,
                       @Qualifier("taskExecutor") Executor notificationExecutor,
                       api.v2.travel_social_network_server.repositories.ContentModerationRepository contentModerationRepository) {
        this.groupRepository = groupRepository;
        this.groupMemberRepository = groupMemberRepository;
        this.postRepository = postRepository;
        this.cloudinaryService = cloudinaryService;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.notificationWebSocketController = notificationWebSocketController;
        this.notificationExecutor = notificationExecutor;
        this.contentModerationRepository = contentModerationRepository;
    }

    @Transactional(readOnly = true)
    public PageableResponse<GroupResponse> getAllGroupsByName(User user, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Group> groups;

        if (keyword == null || keyword.trim().isEmpty()) {
            groups = groupRepository.findAll(pageable);
        } else {
            groups = groupRepository.findAllByGroupNameContaining(keyword.trim(), pageable);
        }

        System.out.println("Found groups for keyword '{}': {}" + keyword + groups.getContent());

        List<GroupResponse> content = groups.getContent().stream()
                .map(group -> convertToGroupResponse(group, user))
                .toList();

        return PageableResponse.<GroupResponse>builder()
                .content(content)
                .totalElements(groups.getTotalElements())
                .totalPages(groups.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<GroupResponse> searchGroupsFulltext(String keyword, int page, int size, User user) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Group> groups = groupRepository.searchGroupsFulltext(keyword, pageable);

        List<GroupResponse> content = groups.getContent().stream()
                .map(group -> convertToGroupResponse(group, user))
                .toList();

        return PageableResponse.<GroupResponse>builder()
                .content(content)
                .pageNumber(groups.getNumber())
                .pageSize(groups.getSize())
                .totalElements(groups.getTotalElements())
                .totalPages(groups.getTotalPages())
                .last(groups.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PageableResponse<GroupResponse> getGroupsOfUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("group.lastActivityAt").descending());

        // Chỉ lấy groups với status APPROVED
        Page<GroupMember> memberships = groupMemberRepository.findAllByUserUserIdAndStatus(
                userId, 
                GroupMemberStatusTypeEnum.APPROVED, 
                pageable
        );

        List<GroupResponse> content = memberships.getContent().stream()
                .map(gm -> convertToGroupResponse(gm.getGroup(), gm.getUser()))
                .toList();

        return PageableResponse.<GroupResponse>builder()
                .content(content)
                .pageNumber(memberships.getNumber())
                .pageSize(memberships.getSize())
                .totalElements(memberships.getTotalElements())
                .totalPages(memberships.getTotalPages())
                .last(memberships.isLast())
                .first(memberships.isFirst())
                .build();
    }

    @Transactional(readOnly = true)
    public PageableResponse<GroupResponse> getPendingGroupsOfUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("joinedAt").descending());

        Page<GroupMember> pendingMemberships = groupMemberRepository.findAllByUserUserIdAndStatus(
                userId, 
                GroupMemberStatusTypeEnum.PENDING, 
                pageable
        );

        List<GroupResponse> content = pendingMemberships.getContent().stream()
                .map(gm -> convertToGroupResponse(gm.getGroup(), gm.getUser()))
                .toList();

        return PageableResponse.<GroupResponse>builder()
                .content(content)
                .pageNumber(pendingMemberships.getNumber())
                .pageSize(pendingMemberships.getSize())
                .totalElements(pendingMemberships.getTotalElements())
                .totalPages(pendingMemberships.getTotalPages())
                .last(pendingMemberships.isLast())
                .first(pendingMemberships.isFirst())
                .build();
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroupById(UUID groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        return convertToGroupResponse(group, user);
    }

    @Transactional
    public GroupResponse createGroup(User user, UpdateGroupDto updateGroupDto) throws IOException {
        if (updateGroupDto.getName() == null || updateGroupDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }

        Group group = Group.builder()
                .groupName(updateGroupDto.getName().trim())
                .groupDescription(updateGroupDto.getDescription() != null ? updateGroupDto.getDescription().trim() : null)
                .privacy(updateGroupDto.getPrivacy() ? PrivacyTypeEnum.PRIVATE : PrivacyTypeEnum.PUBLIC)
                .tags(updateGroupDto.getTags() != null ? updateGroupDto.getTags().trim() : null)
                .groupMembers(new ArrayList<>())
                .memberCount(1)
                .build();

        if (updateGroupDto.getCover() != null && !updateGroupDto.getCover().isEmpty()) {
            try {
                String contentType = updateGroupDto.getCover().getContentType();
                String coverUrl = cloudinaryService.uploadFile(updateGroupDto.getCover().getBytes(), GROUP_FOLDER, contentType);
                group.setCoverImageUrl(coverUrl);
            } catch (IOException e) {
                log.warn("Failed to upload cover for group '{}': {}", updateGroupDto.getName(), e.getMessage());
            }
        }

        group = groupRepository.save(group);

        GroupMember creatorMember = GroupMember.builder()
                .group(group)
                .user(user)
                .role(MemberRoleTypeEnum.OWNER)
                .build();

        groupMemberRepository.save(creatorMember);
        group.getGroupMembers().add(creatorMember);
        group = groupRepository.save(group);

        log.info("Created group '{}' with ID: {}", group.getGroupName(), group.getGroupId());
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .coverImageUrl(group.getCoverImageUrl())
                .memberCount(group.getMemberCount() != null ? group.getMemberCount() : 0)
                .groupDescription(group.getGroupDescription())
                .privacy(group.getPrivacy().equals(PrivacyTypeEnum.PRIVATE))
                .isMember(true)
                .build();
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(UUID groupId, User user, UpdateGroupDto updateGroupDto) throws IOException {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        // Check if user is OWNER or ADMIN
        GroupMember member = groupMemberRepository.findByGroupGroupIdAndUserUserId(groupId, user.getUserId())
                .orElseThrow(() -> new IllegalStateException("You are not a member of this group"));

        if (member.getRole() != MemberRoleTypeEnum.OWNER && member.getRole() != MemberRoleTypeEnum.ADMIN) {
            throw new IllegalStateException("Only group owner or admin can update group information");
        }

        // Update group information
        if (updateGroupDto.getName() != null && !updateGroupDto.getName().trim().isEmpty()) {
            group.setGroupName(updateGroupDto.getName().trim());
        }

        if (updateGroupDto.getDescription() != null) {
            group.setGroupDescription(updateGroupDto.getDescription().trim());
        }

        if (updateGroupDto.getPrivacy() != null) {
            group.setPrivacy(updateGroupDto.getPrivacy() ? PrivacyTypeEnum.PRIVATE : PrivacyTypeEnum.PUBLIC);
        }

        // Update cover image if provided
        if (updateGroupDto.getCover() != null && !updateGroupDto.getCover().isEmpty()) {
            try {
                String contentType = updateGroupDto.getCover().getContentType();
                String coverUrl = cloudinaryService.uploadFile(updateGroupDto.getCover().getBytes(), GROUP_FOLDER, contentType);
                group.setCoverImageUrl(coverUrl);
            } catch (IOException e) {
                log.warn("Failed to upload cover for group '{}': {}", updateGroupDto.getName(), e.getMessage());
                throw e;
            }
        }

        group = groupRepository.save(group);

        log.info("Updated group '{}' with ID: {}", group.getGroupName(), group.getGroupId());
        return convertToGroupResponse(group, user);
    }

    @Override
    @Transactional
    public JoinGroupResponse joinGroup(UUID groupId, User user) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group does not exist"));

        boolean alreadyJoined = groupMemberRepository.existsByGroupGroupIdAndUserUserId(groupId, user.getUserId());
        if (alreadyJoined) {
            throw new IllegalStateException("You have already joined or requested to join this group");
        }

        GroupMember member = GroupMember.builder()
                .group(group)
                .user(user)
                .role(MemberRoleTypeEnum.MEMBER)
                .joinedAt(Instant.now())
                .build();

        GroupMemberStatusTypeEnum status;
        if (group.getPrivacy() == PrivacyTypeEnum.PUBLIC) {
            member.setStatus(GroupMemberStatusTypeEnum.APPROVED);
            status = GroupMemberStatusTypeEnum.APPROVED;
            // Increment member count for public groups
            group.setMemberCount(group.getMemberCount() + 1);
            groupRepository.save(group);
        } else {
            member.setStatus(GroupMemberStatusTypeEnum.PENDING);
            status = GroupMemberStatusTypeEnum.PENDING;
        }

        groupMemberRepository.save(member);
        
        return JoinGroupResponse.builder()
                .status(status.getValue())
                .isMember(status == GroupMemberStatusTypeEnum.APPROVED)
                .build();
    }

    @Override
    @Transactional
    public void leaveGroup(UUID groupId, User user) {
        GroupMember member = groupMemberRepository
                .findByGroupGroupIdAndUserUserId(groupId, user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("You are not a member of this group"));

        // OWNER cannot leave the group
        if (member.getRole() == MemberRoleTypeEnum.OWNER) {
            throw new IllegalStateException("Group owner cannot leave the group. Please transfer ownership first or delete the group.");
        }

        groupMemberRepository.delete(member);

        Group group = member.getGroup();
        if (group.getMemberCount() > 0) {
            group.setMemberCount(group.getMemberCount() - 1);
            groupRepository.save(group);
        }
    }

    @Override
    @Transactional
    public GroupMemberResponse approveJoinRequest(UUID groupId, UUID targetUserId, UUID adminUserId) {
        GroupMember target = groupMemberRepository
                .findByGroupGroupIdAndUserUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target member does not exist in the group"));

        if (target.getStatus() != GroupMemberStatusTypeEnum.PENDING) {
            throw new IllegalStateException("Join request has already been processed");
        }

        target.setStatus(GroupMemberStatusTypeEnum.APPROVED);
        GroupMember savedMember = groupMemberRepository.save(target);

        Group group = target.getGroup();
        group.setMemberCount(group.getMemberCount() + 1);
        groupRepository.save(group);

        // Send notification to user that their join request was accepted asynchronously
        User approvedUser = savedMember.getUser();
        User admin = userRepository.findById(adminUserId)
                .orElse(null);
        
        notificationExecutor.execute(() -> {
            try {
                String adminName = admin != null && admin.getUserProfile() != null 
                    ? admin.getUserProfile().getFullName() 
                    : "Admin";
                    
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(approvedUser.getUserId())
                        .senderId(adminUserId)
                        .type(String.valueOf(NotificationTypeEnum.GROUP_JOIN_ACCEPTED))
                        .content(adminName + " đã chấp nhận yêu cầu tham gia nhóm " + group.getGroupName())
                        .relatedId(groupId)
                        .build();

                NotificationResponse notificationResponse = notificationService.createNotification(notificationDto);
                notificationWebSocketController.sendNotification(approvedUser.getUserId(), notificationResponse);
                
                log.info("Sent group join accepted notification from {} to {} for group {}", adminUserId, approvedUser.getUserId(), groupId);
            } catch (Exception e) {
                log.error("Failed to send group join accepted notification", e);
            }
        });

        // Build and return GroupMemberResponse
        User user = savedMember.getUser();
        String fullName = user.getUserProfile() != null 
            ? user.getUserProfile().getFullName() 
            : user.getUsername();
        
        return GroupMemberResponse.builder()
                .userId(user.getUserId())
                .firstName(fullName.split(" ")[0])
                .lastName(fullName.contains(" ") ? fullName.substring(fullName.indexOf(" ") + 1) : "")
                .fullName(fullName)
                .avatar(user.getAvatarImg())
                .role(savedMember.getRole().getValue())
                .status(savedMember.getStatus().getValue())
                .isFriend(false)
                .postsCount(0)
                .joinedAt(savedMember.getJoinedAt())
                .build();
    }

    @Override
    @Transactional
    public void rejectJoinRequest(UUID groupId, UUID targetUserId, UUID adminUserId) {
        GroupMember target = groupMemberRepository
                .findByGroupGroupIdAndUserUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target member does not exist in the group"));

        if (target.getStatus() != GroupMemberStatusTypeEnum.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }

        groupMemberRepository.delete(target);
    }

    @Override
    @Transactional
    public void removeMemberFromGroup(UUID groupId, UUID targetUserId, UUID adminUserId) {
        GroupMember target = groupMemberRepository
                .findByGroupGroupIdAndUserUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target member does not exist in the group"));

        if (target.getRole() == MemberRoleTypeEnum.OWNER) {
            throw new IllegalStateException("Cannot remove the group OWNER");
        }

        groupMemberRepository.delete(target);
    }

    @Override
    @Transactional
    public void changeMemberRole(UUID groupId, UUID targetUserId, UUID adminUserId, String newRole) {
        GroupMember target = groupMemberRepository
                .findByGroupGroupIdAndUserUserId(groupId, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Target member does not exist in the group"));

        if (target.getRole() == MemberRoleTypeEnum.OWNER) {
            throw new IllegalStateException("Cannot change the role of the group OWNER");
        }

        switch (newRole.toLowerCase()) {
            case "admin" -> target.setRole(MemberRoleTypeEnum.ADMIN);
            case "member" -> target.setRole(MemberRoleTypeEnum.MEMBER);
        }

        groupMemberRepository.save(target);
    }

    public GroupResponse convertToGroupResponse(Group group, User user) {
        boolean isMember = groupMemberRepository.existsByGroupGroupIdAndUserUserId(group.getGroupId(), user.getUserId());
        boolean isAdmin = user != null && user.getRole() != null && "ADMIN".equals(user.getRole().name());
        
        // Get current user's role in the group
        String currentUserRole = null;
        if (isMember) {
            GroupMember currentMember = groupMemberRepository
                    .findByGroupGroupIdAndUserUserId(group.getGroupId(), user.getUserId())
                    .orElse(null);
            if (currentMember != null && currentMember.getRole() != null) {
                currentUserRole = currentMember.getRole().name();
            }
        } else if (isAdmin) {
            // Admin can view group details even if not a member
            currentUserRole = "ADMIN";
        }

        // Chuyển đổi LocalDateTime sang Instant cho việc tính toán
        Instant now = Instant.now();
        
        // Tính số bài viết trung bình mỗi ngày (30 ngày gần nhất)
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        long postCount = postRepository.countPostsInGroupSinceDate(group.getGroupId(), thirtyDaysAgo);
        int postsPerDay = (int) Math.ceil(postCount / 30.0);

        // Tính số bài viết hôm nay (từ đầu ngày hôm nay)
        LocalDateTime startOfTodayLocal = LocalDateTime.now().truncatedTo(ChronoUnit.DAYS);
        Instant startOfToday = startOfTodayLocal.atZone(ZoneId.systemDefault()).toInstant();
        long postsToday = postRepository.countPostsInGroupSinceDate(group.getGroupId(), startOfToday);

        // Tính số bài viết tháng trước (30 ngày trước đến hôm qua)
        Instant startOfLastMonth = now.minus(30, ChronoUnit.DAYS);
        long postsLastMonth = postRepository.countPostsInGroupSinceDate(group.getGroupId(), startOfLastMonth);

        // Tính số thành viên mới tuần này (7 ngày gần nhất)
        Instant startOfWeek = now.minus(7, ChronoUnit.DAYS);
        long newMembersThisWeek = groupMemberRepository.countByGroupGroupIdAndJoinedAtAfter(group.getGroupId(), startOfWeek);

        // Lấy danh sách bạn bè của user hiện tại đang là thành viên của group
        List<User> friendMembers = groupMemberRepository.findFriendMembersInGroup(group.getGroupId(), user.getUserId());
        List<FriendMemberDto> friendMemberDtos = friendMembers.stream()
                .limit(5) // Giới hạn tối đa 5 bạn bè để hiển thị
                .map(friend -> FriendMemberDto.builder()
                        .userId(friend.getUserId())
                        .name(friend.getUserProfile().getFullName())
                        .avatar(friend.getAvatarImg())
                        .build())
                .collect(Collectors.toList());

        // Lấy danh sách admin (role = ADMIN hoặc OWNER)
        List<GroupMember> adminGroupMembers = groupMemberRepository.findByGroupGroupIdAndRoleIn(
                group.getGroupId(), 
                List.of(MemberRoleTypeEnum.ADMIN, MemberRoleTypeEnum.OWNER)
        );
        List<FriendMemberDto> adminMemberDtos = adminGroupMembers.stream()
                .limit(5) // Giới hạn tối đa 5 admin để hiển thị
                .map(gm -> FriendMemberDto.builder()
                        .userId(gm.getUser().getUserId())
                        .name(gm.getUser().getUserProfile().getFullName())
                        .avatar(gm.getUser().getAvatarImg())
                        .build())
                .collect(Collectors.toList());

        // Lấy danh sách moderator (role = MODERATOR)
        List<GroupMember> moderatorGroupMembers = groupMemberRepository.findByGroupGroupIdAndRole(
                group.getGroupId(), 
                MemberRoleTypeEnum.MODERATOR
        );
        List<FriendMemberDto> moderatorMemberDtos = moderatorGroupMembers.stream()
                .limit(5) // Giới hạn tối đa 5 moderator để hiển thị
                .map(gm -> FriendMemberDto.builder()
                        .userId(gm.getUser().getUserId())
                        .name(gm.getUser().getUserProfile().getFullName())
                        .avatar(gm.getUser().getAvatarImg())
                        .build())
                .collect(Collectors.toList());

        // Check if group is locked by admin
        ContentModeration moderation = group.getContentModeration();
        boolean isLocked = moderation != null && Boolean.FALSE.equals(moderation.getIsActive());
        String moderationReason = isLocked ? moderation.getModerationReason() : null;

        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupDescription(group.getGroupDescription())
                .coverImageUrl(group.getCoverImageUrl())
                .memberCount(group.getMemberCount() != null ? group.getMemberCount() : 0)
                .privacy(group.getPrivacy().equals(PrivacyTypeEnum.PRIVATE))
                .isMember(isMember)
                .currentUserRole(currentUserRole)
                .isLocked(isLocked)
                .moderationReason(moderationReason)
                .createdAt(group.getCreatedAt())
                .lastActivityAt(group.getLastActivityAt())
                .postsPerDay(postsPerDay)
                .postsToday((int) postsToday)
                .postsLastMonth((int) postsLastMonth)
                .newMembersThisWeek((int) newMembersThisWeek)
                .tags(group.getTags())
                .location(group.getLocation())
                .friendMembers(friendMemberDtos)
                .adminMembers(adminMemberDtos)
                .moderatorMembers(moderatorMemberDtos)
                .build();
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> searchGroupsForSuggestion(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Group> groupPage = groupRepository.searchGroupsForSuggestion(keyword, pageable);

        return groupPage.getContent().stream()
                .map(this::mapToGroupResponseForSuggestion)
                .toList();
    }

    private GroupResponse mapToGroupResponseForSuggestion(Group group) {
        return GroupResponse.builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .groupDescription(group.getGroupDescription())
                .coverImageUrl(group.getCoverImageUrl())
                .memberCount(group.getMemberCount() != null ? group.getMemberCount() : 0)
                .privacy(group.getPrivacy().equals(PrivacyTypeEnum.PRIVATE))
                .isMember(false) // Mặc định false cho search suggestion
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<GroupMemberResponse> getGroupMembers(UUID groupId, User currentUser, String keyword, String filter, int page, int size) {
        log.info("GroupId: {}, UserId: {}, Keyword: {}, Filter: {}, Page: {}, Size: {}", 
            groupId, currentUser.getUserId(), keyword, filter, page, size);
        
        try {
            // Check if user is member of the group
            if (!groupMemberRepository.existsByGroupGroupIdAndUserUserId(groupId, currentUser.getUserId())) {
                log.warn("User {} is not a member of group {}", currentUser.getUserId(), groupId);
                throw new ResourceNotFoundException("User is not a member of group with id: " + groupId);
            }
            // Fetch all members with eager loading to avoid lazy loading issues
            List<GroupMember> allMembers = groupMemberRepository.findAllByGroupGroupIdWithUser(groupId);
            log.info("Fetched {} members from database", allMembers.size());

            // Filter and map to response
            List<GroupMemberResponse> filteredMembers = allMembers.stream()
                    .map(groupMember -> {
                        try {
                            User user = groupMember.getUser();
                            log.debug("Processing member: userId={}, role={}", user.getUserId(), groupMember.getRole());
                            
                            String fullName = user.getUserProfile() != null 
                                ? user.getUserProfile().getFullName() 
                                : user.getUsername();
                            
                            return GroupMemberResponse.builder()
                                    .userId(user.getUserId())
                                    .firstName(fullName.split(" ")[0])
                                    .lastName(fullName.contains(" ") ? fullName.substring(fullName.indexOf(" ") + 1) : "")
                                    .fullName(fullName)
                                    .avatar(user.getAvatarImg())
                                    .role(groupMember.getRole().getValue())
                                    .status(groupMember.getStatus().getValue())
                                    .isFriend(false)
                                    .postsCount(0)
                                    .joinedAt(groupMember.getJoinedAt())
                                    .build();
                        } catch (Exception e) {
                            log.error("Error mapping member to response: {}", e.getMessage(), e);
                            throw new RuntimeException("Error mapping member: " + e.getMessage(), e);
                        }
                    })
                    .filter(member -> {
                        // Apply keyword filter
                        if (keyword != null && !keyword.trim().isEmpty()) {
                            String lowerKeyword = keyword.toLowerCase();
                            if (!member.getFullName().toLowerCase().contains(lowerKeyword)) {
                                return false;
                            }
                        }
                        
                        // Apply role/friend filter
                        if (filter != null && !filter.equals("all")) {
                            if (filter.equals("admins")) {
                                return !member.getRole().equals("MEMBER");
                            }
                        }
                        
                        return true;
                    })
                    .sorted((a, b) -> b.getJoinedAt().compareTo(a.getJoinedAt())) // Sort by joinedAt DESC
                    .toList();
            
            log.info("Filtered to {} members after applying filters", filteredMembers.size());

            // Manual pagination
            int start = page * size;
            int end = Math.min(start + size, filteredMembers.size());
            List<GroupMemberResponse> paginatedMembers = start < filteredMembers.size() 
                ? filteredMembers.subList(start, end)
                : List.of();

            int totalElements = filteredMembers.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            log.info("Pagination: page={}, size={}, totalElements={}, totalPages={}, returning {} members", 
                page, size, totalElements, totalPages, paginatedMembers.size());
            return PageableResponse.<GroupMemberResponse>builder()
                    .content(paginatedMembers)
                    .pageNumber(page)
                    .pageSize(size)
                    .totalElements((long) totalElements)
                    .totalPages(totalPages)
                    .last(page >= totalPages - 1)
                    .first(page == 0)
                    .build();
                    
        } catch (ResourceNotFoundException e) {
            log.error("=== getGroupMembers END - RESOURCE NOT FOUND: {} ===", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("=== getGroupMembers END - ERROR ===", e);
            log.error("Error details: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching group members: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void lockGroup(UUID groupId, UUID adminUserId, String reason) {
        // Verify admin role
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        if (!"ADMIN".equals(admin.getRole().name())) {
            throw new IllegalArgumentException("Only admin can lock groups");
        }

        // Get group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        // Check if already has moderation record
        ContentModeration moderation = group.getContentModeration();
        
        if (moderation == null) {
            // Create new moderation record
            moderation = ContentModeration.builder()
                    .contentType(api.v2.travel_social_network_server.utilities.enums.ContentTypeEnum.GROUP)
                    .contentId(groupId)
                    .moderatedByUser(admin)
                    .moderationReason(reason)
                    .isActive(false) // false means locked
                    .build();
            moderation = contentModerationRepository.save(moderation);
            
            // Update group
            group.setContentModeration(moderation);
            groupRepository.save(group);
        } else {
            // Update existing moderation
            moderation.setIsActive(false); // false means locked
            moderation.setModerationReason(reason);
            moderation.setModeratedByUser(admin);
            moderation.setUnlockedAt(null);
            contentModerationRepository.save(moderation);
        }

        // Send notification to group owner
        GroupMember owner = groupMemberRepository.findByGroupGroupIdAndRole(groupId, MemberRoleTypeEnum.OWNER)
                .stream()
                .findFirst()
                .orElse(null);
                
        if (owner != null && owner.getUser() != null) {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(owner.getUser().getUserId())
                        .senderId(adminUserId)
                        .type(String.valueOf(NotificationTypeEnum.SYSTEM))
                        .content(String.format("Nhóm '%s' đã bị khóa bởi quản trị viên. Lý do: %s", 
                                group.getGroupName(), reason))
                        .relatedId(groupId)
                        .build();
                        
                NotificationResponse notification = notificationService.createNotification(notificationDto);
                
                // Send via WebSocket
                notificationExecutor.execute(() -> {
                    try {
                        notificationWebSocketController.sendNotification(
                                owner.getUser().getUserId(), 
                                notification
                        );
                    } catch (Exception e) {
                        log.error("Failed to send WebSocket notification for locked group", e);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to create notification for locked group", e);
            }
        }

        log.info("Group {} has been locked by admin {}", groupId, adminUserId);
    }

    @Override
    @Transactional
    public void unlockGroup(UUID groupId, UUID adminUserId) {
        // Verify admin role
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
        
        if (!"ADMIN".equals(admin.getRole().name())) {
            throw new IllegalArgumentException("Only admin can unlock groups");
        }

        // Get group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        // Update moderation record
        ContentModeration moderation = group.getContentModeration();
        if (moderation != null) {
            moderation.setIsActive(true); // true means unlocked
            moderation.setUnlockedAt(Instant.now());
            contentModerationRepository.save(moderation);
        }

        // Send notification to group owner
        GroupMember owner = groupMemberRepository.findByGroupGroupIdAndRole(groupId, MemberRoleTypeEnum.OWNER)
                .stream()
                .findFirst()
                .orElse(null);
                
        if (owner != null && owner.getUser() != null) {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(owner.getUser().getUserId())
                        .senderId(adminUserId)
                        .type(String.valueOf(NotificationTypeEnum.SYSTEM))
                        .content(String.format("Nhóm '%s' đã được mở khóa bởi quản trị viên.", 
                                group.getGroupName()))
                        .relatedId(groupId)
                        .build();
                        
                NotificationResponse notification = notificationService.createNotification(notificationDto);
                
                // Send via WebSocket
                notificationExecutor.execute(() -> {
                    try {
                        notificationWebSocketController.sendNotification(
                                owner.getUser().getUserId(), 
                                notification
                        );
                    } catch (Exception e) {
                        log.error("Failed to send WebSocket notification for unlocked group", e);
                    }
                });
            } catch (Exception e) {
                log.error("Failed to create notification for unlocked group", e);
            }
        }

        log.info("Group {} has been unlocked by admin {}", groupId, adminUserId);
    }

}
