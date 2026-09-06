package api.v2.travel_social_network_server.services.chat.conversation;

import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.entities.ConversationMember;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.FriendshipRepository;
import api.v2.travel_social_network_server.repositories.ConversationMemberRepository;
import api.v2.travel_social_network_server.repositories.ConversationRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.chat.ConversationResponse;
import api.v2.travel_social_network_server.responses.chat.SearchConversationResponse;
import api.v2.travel_social_network_server.utilities.enums.ConversationTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationService implements IConversationService {

        private final ConversationRepository conversationRepository;
        private final ConversationMemberRepository conversationMemberRepository;
        private final UserRepository userRepository;
        private final FriendshipRepository friendshipRepository;

        @Transactional(readOnly = true)
        public ConversationResponse getConversationByConversationId(UUID conversationId, UUID currentUserId) {
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                return convertToConversationResponse(conversation, currentUserId);
        }

        @Transactional
        public Conversation createGroupConversation(String groupName, UUID createdBy, List<UUID> memberIds) {
                User creator = userRepository.findByUserId(createdBy)
                                .orElseThrow(() -> new ResourceNotFoundException("User not existed: " + createdBy));

                Conversation group = Conversation.builder()
                                .conversationName(groupName)
                                .type(ConversationTypeEnum.GROUP)
                                .build();

                List<ConversationMember> members = new ArrayList<>();

                // Thêm creator làm admin
                members.add(ConversationMember.builder()
                                .conversation(group)
                                .user(creator)
                                .role(MemberRoleTypeEnum.ADMIN)
                                .build());

                // Thêm các member còn lại
                for (UUID userId : memberIds) {
                        if (userId.equals(createdBy))
                                continue;

                        User user = userRepository.findByUserId(userId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "User not existed: " + userId));

                        members.add(ConversationMember.builder()
                                        .conversation(group)
                                        .user(user)
                                        .role(MemberRoleTypeEnum.MEMBER)
                                        .build());
                }

                group.setConversationMembers(members);

                return conversationRepository.save(group);
        }

        @Transactional
        public ConversationResponse createOrGetPrivateConversation(UUID currentUserId, UUID friendUserId) {
                User currentUser = userRepository.findByUserId(currentUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not existed: " + currentUserId));
                User friendUser = userRepository.findByUserId(friendUserId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not existed: " + friendUserId));

                // 1. Kiểm tra nếu đã có private conversation
                Optional<Conversation> existingConversation = conversationRepository
                                .findPrivateConversationBetweenUsers(
                                                currentUserId, friendUserId, ConversationTypeEnum.PRIVATE);
                if (existingConversation.isPresent()) {
                        return convertToConversationResponse(existingConversation.get(), currentUserId);
                }

                Conversation conversation = Conversation.builder()
                                .type(ConversationTypeEnum.PRIVATE)
                                .conversationName(friendUser.getUserProfile().getFullName())
                                .conversationAvatar(friendUser.getAvatarImg())
                                .build();

                List<ConversationMember> members = List.of(
                                ConversationMember.builder()
                                                .conversation(conversation)
                                                .user(currentUser)
                                                .role(MemberRoleTypeEnum.ADMIN)
                                                .build(),
                                ConversationMember.builder()
                                                .conversation(conversation)
                                                .user(friendUser)
                                                .role(MemberRoleTypeEnum.ADMIN)
                                                .build());

                conversation.setConversationMembers(members);

                Conversation newConversation = conversationRepository.save(conversation);

                return convertToConversationResponse(newConversation, currentUserId);
        }

        @Override
        @Transactional(readOnly = true)
        public PageableResponse<ConversationResponse> getUserConversations(UUID userId, int page, int size) {
                return getUserConversations(userId, page, size, null);
        }

        @Override
        @Transactional(readOnly = true)
        public PageableResponse<ConversationResponse> getUserConversations(UUID userId, int page, int size,
                        ConversationTypeEnum type) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("lastActiveAt").descending());
                Page<Conversation> conversations;

                if (type != null) {
                        conversations = conversationRepository.findConversationsByUserIdAndType(userId, type, pageable);
                } else {
                        conversations = conversationRepository.findConversationsByUserId(userId, pageable);
                }

                List<ConversationResponse> content = conversations.getContent().stream()
                                .map(conversation -> convertToConversationResponse(conversation, userId))
                                .toList();

                return PageableResponse.<ConversationResponse>builder()
                                .content(content)
                                .totalElements(conversations.getTotalElements())
                                .totalPages(conversations.getTotalPages())
                                .build();
        }

        @Transactional(readOnly = true)
        public PageableResponse<ConversationResponse> searchConversationsByUerIdAndKeyWord(UUID userId, String keyword,
                        int page, int size) {
                Pageable pageable = PageRequest.of(page, size, Sort.by("lastActiveAt").descending());
                Page<Conversation> conversations = conversationRepository.searchConversationsByUserAndKeyword(userId,
                                keyword, pageable);

                List<ConversationResponse> content = conversations.getContent().stream()
                                .map(conversation -> convertToConversationResponse(conversation, userId))
                                .toList();

                return PageableResponse.<ConversationResponse>builder()
                                .content(content)
                                .totalElements(conversations.getTotalElements())
                                .totalPages(conversations.getTotalPages())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public PageableResponse<SearchConversationResponse> searchConversationsIncludeFriends(UUID userId,
                        String keyword, int page, int size) {
                // 1. Lấy group conversations match keyword
                List<ConversationResponse> groupResponses = conversationRepository
                                .searchConversationsByUserAndKeywordWithType(userId, keyword,
                                                ConversationTypeEnum.GROUP.name())
                                .stream()
                                .map(conversation -> convertToConversationResponse(conversation, userId))
                                .toList();

                // 2. Lấy bạn bè match keyword
                List<User> friends = friendshipRepository.findActiveFriendsByKeyword(userId, keyword);

                List<ConversationResponse> friendResponses = friends.stream()
                                .map(f -> ConversationResponse.builder()
                                                .otherUserId(f.getUserId())
                                                .conversationName(f.getUserProfile().getFullName())
                                                .conversationAvatar(f.getAvatarImg())
                                                .type(ConversationTypeEnum.PRIVATE)
                                                .build())
                                .collect(Collectors.toList());

                // 3. Tạo SearchConversationResponse
                SearchConversationResponse searchResponse = SearchConversationResponse.builder()
                                .groups(groupResponses)
                                .friends(friendResponses)
                                .build();

                // 4. Trả về paging đơn giản (1 object duy nhất)
                List<SearchConversationResponse> pageContent = List.of(searchResponse);

                return PageableResponse.<SearchConversationResponse>builder()
                                .content(pageContent)
                                .totalElements(1)
                                .totalPages(1)
                                .build();
        }

        private ConversationResponse convertToConversationResponse(Conversation conversation, UUID currentUserId) {
                String displayName = conversation.getConversationName();
                String displayAvatar = conversation.getConversationAvatar();
                Boolean isGroupOwner = null;

                if (conversation.getType() == ConversationTypeEnum.PRIVATE) {
                        Optional<ConversationMember> otherMemberOpt = conversation.getConversationMembers().stream()
                                        .filter(member -> !member.getUser().getUserId().equals(currentUserId))
                                        .findFirst();

                        if (otherMemberOpt.isPresent()) {
                                var otherUser = otherMemberOpt.get().getUser();
                                displayName = otherUser.getUserProfile().getFullName();
                                displayAvatar = otherUser.getAvatarImg();
                        }
                } else if (conversation.getType() == ConversationTypeEnum.GROUP) {
                        // Kiểm tra xem currentUser có phải là admin của group không
                        Optional<ConversationMember> currentMemberOpt = conversation.getConversationMembers().stream()
                                        .filter(member -> member.getUser().getUserId().equals(currentUserId))
                                        .findFirst();

                        if (currentMemberOpt.isPresent()) {
                                isGroupOwner = currentMemberOpt.get().getRole() == MemberRoleTypeEnum.ADMIN;
                        }
                }

                return ConversationResponse.builder()
                                .conversationId(conversation.getConversationId())
                                .conversationName(displayName)
                                .conversationAvatar(displayAvatar)
                                .type(conversation.getType())
                                .lastMessage(conversation.getLastMessage())
                                .lastActiveAt(conversation.getLastActiveAt())
                                .groupOwner(isGroupOwner)
                                .build();
        }

        @Override
        @Transactional
        public void updateLastMessage(UUID conversationId, String lastMessage) {
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                conversation.setLastMessage(lastMessage);
                conversation.setLastActiveAt(java.time.Instant.now());
                conversationRepository.save(conversation);
        }

        @Override
        @Transactional
        public Conversation updateGroupAvatar(UUID conversationId, String avatarUrl, UUID userId) {
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                // Verify it's a group conversation
                if (!conversation.getType().equals(ConversationTypeEnum.GROUP)) {
                        throw new IllegalArgumentException("Can only update avatar for group conversations");
                }

                conversation.setConversationAvatar(avatarUrl);
                return conversationRepository.save(conversation);
        }

        @Override
        @Transactional
        public void removeMemberFromConversation(UUID conversationId, UUID memberUserId, UUID adminUserId) {
                // Kiểm tra conversation có tồn tại
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                // Verify it's a group conversation
                if (!conversation.getType().equals(ConversationTypeEnum.GROUP)) {
                        throw new IllegalArgumentException("Can only remove members from group conversations");
                }

                // Kiểm tra admin có quyền xóa không
                ConversationMember adminMember = conversationMemberRepository
                                .findByConversationConversationIdAndUserUserId(conversationId, adminUserId);

                if (adminMember == null) {
                        throw new ResourceNotFoundException("Admin not found in conversation");
                }

                if (!adminMember.getRole().equals(MemberRoleTypeEnum.ADMIN)) {
                        throw new IllegalArgumentException("Only admin can remove members");
                }

                // Không cho phép xóa chính mình
                if (memberUserId.equals(adminUserId)) {
                        throw new IllegalArgumentException("Admin cannot remove themselves");
                }

                // Kiểm tra member cần xóa có tồn tại không
                ConversationMember memberToRemove = conversationMemberRepository
                                .findByConversationConversationIdAndUserUserId(conversationId, memberUserId);

                if (memberToRemove == null) {
                        throw new ResourceNotFoundException("Member not found in conversation");
                }

                // Xóa member
                conversationMemberRepository.deleteByConversationConversationIdAndUserUserId(conversationId,
                                memberUserId);
        }

        @Override
        @Transactional
        public void addMembersToConversation(UUID conversationId, List<UUID> memberUserIds, UUID adminUserId) {
                // Kiểm tra conversation có tồn tại
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                // Verify it's a group conversation
                if (!conversation.getType().equals(ConversationTypeEnum.GROUP)) {
                        throw new IllegalArgumentException("Can only add members to group conversations");
                }

                // Kiểm tra admin có quyền thêm không
                ConversationMember adminMember = conversationMemberRepository
                                .findByConversationConversationIdAndUserUserId(conversationId, adminUserId);

                if (adminMember == null) {
                        throw new ResourceNotFoundException("Admin not found in conversation");
                }

                if (!adminMember.getRole().equals(MemberRoleTypeEnum.ADMIN)) {
                        throw new IllegalArgumentException("Only admin can add members");
                }

                for (UUID userId : memberUserIds) {
                        // Kiểm tra user đã là member chưa
                        boolean isAlreadyMember = conversationMemberRepository
                                        .existsByConversationConversationIdAndUserUserId(conversationId, userId);

                        if (isAlreadyMember) {
                                continue; 
                        }

                        User user = userRepository.findByUserId(userId)
                                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

                        ConversationMember newMember = ConversationMember.builder()
                                        .conversation(conversation)
                                        .user(user)
                                        .role(MemberRoleTypeEnum.MEMBER)
                                        .build();

                        conversationMemberRepository.save(newMember);
                }
        }

        @Override
        @Transactional
        public Conversation updateGroupName(UUID conversationId, String groupName, UUID userId) {
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                if (!conversation.getType().equals(ConversationTypeEnum.GROUP)) {
                        throw new IllegalArgumentException("Can only update name for group conversations");
                }

                ConversationMember member = conversationMemberRepository
                                .findByConversationConversationIdAndUserUserId(conversationId, userId);

                if (member == null) {
                        throw new ResourceNotFoundException("User not found in conversation");
                }

                if (!member.getRole().equals(MemberRoleTypeEnum.ADMIN)) {
                        throw new IllegalArgumentException("Only admin can update group name");
                }

                conversation.setConversationName(groupName);
                return conversationRepository.save(conversation);
        }

        @Override
        @Transactional
        public void deleteConversation(UUID conversationId, UUID userId) {
                Conversation conversation = conversationRepository.findConversationByConversationId(conversationId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Conversation not found: " + conversationId));

                ConversationMember member = conversationMemberRepository
                                .findByConversationConversationIdAndUserUserId(conversationId, userId);

                if (member == null) {
                        throw new ResourceNotFoundException("User not found in conversation");
                }

                if (conversation.getType().equals(ConversationTypeEnum.GROUP)) {
                        if (!member.getRole().equals(MemberRoleTypeEnum.ADMIN)) {
                                throw new IllegalArgumentException("Only admin can delete group conversation");
                        }
                }
                conversationRepository.delete(conversation);
        }
}
