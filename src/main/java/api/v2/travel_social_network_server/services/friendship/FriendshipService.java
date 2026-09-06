package api.v2.travel_social_network_server.services.friendship;

import api.v2.travel_social_network_server.controllers.ws.NotificationWebSocketController;
import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.entities.Friendship;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceAlreadyExistedException;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.FriendshipRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.friendship.FriendshipResponse;
import api.v2.travel_social_network_server.responses.friendship.UserFriendshipListsResponse;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import api.v2.travel_social_network_server.utilities.enums.FriendShipTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FriendshipService implements IFriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final INotificationService notificationService;
    private final NotificationWebSocketController notificationWebSocketController;
    private final Executor notificationExecutor;

    public FriendshipService(UserRepository userRepository,
                            FriendshipRepository friendshipRepository,
                            INotificationService notificationService,
                            NotificationWebSocketController notificationWebSocketController,
                            @Qualifier("taskExecutor") Executor notificationExecutor) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.notificationService = notificationService;
        this.notificationWebSocketController = notificationWebSocketController;
        this.notificationExecutor = notificationExecutor;
    }

    @Transactional
    public void sendFriendRequest(UUID requesterId, UUID receiverId) {
        if (requesterId.equals(receiverId)) {
            throw new IllegalArgumentException("You cannot send a friend request to yourself!");
        }

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("Requester does not exist"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Receiver does not exist"));

        // Check if a friendship already exists
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(requesterId, receiverId);
        if (existing.isPresent()) {
            throw new ResourceAlreadyExistedException("A request or friendship already exists!");
        }

        Friendship friendship = Friendship.builder()
                .requester(requester)
                .receiver(receiver)
                .status(FriendShipTypeEnum.PENDING)
                .build();

        friendshipRepository.save(friendship);

        // Send notification to receiver asynchronously
        notificationExecutor.execute(() -> {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(receiverId)
                        .senderId(requesterId)
                        .type(String.valueOf(NotificationTypeEnum.FRIEND_REQUEST))
                        .content(requester.getUserProfile().getFullName() + " đã gửi lời mời kết bạn")
                        .relatedId(requesterId)
                        .build();

                NotificationResponse notificationResponse = notificationService.createNotification(notificationDto);
                notificationWebSocketController.sendNotification(receiverId, notificationResponse);            } catch (Exception e) {
                log.error("Failed to send friend request notification", e);
            }
        });
    }

    @Transactional
    public void acceptFriendRequest(UUID friendshipId, UUID receiverId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request does not exist"));

        if (!friendship.getReceiver().getUserId().equals(receiverId)) {
            throw new IllegalStateException("You are not authorized to accept this request!");
        }

        friendship.setStatus(FriendShipTypeEnum.ACCEPTED);
        friendshipRepository.save(friendship);

        // Send notification to requester that their request was accepted asynchronously
        User receiver = friendship.getReceiver();
        User requester = friendship.getRequester();
        
        notificationExecutor.execute(() -> {
            try {
                CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                        .receiverId(requester.getUserId())
                        .senderId(receiver.getUserId())
                        .type(String.valueOf(NotificationTypeEnum.FRIEND_ACCEPTED))
                        .content(receiver.getUserProfile().getFullName() + " đã chấp nhận lời mời kết bạn của bạn")
                        .relatedId(receiver.getUserId())
                        .build();

                NotificationResponse notificationResponse = notificationService.createNotification(notificationDto);
                notificationWebSocketController.sendNotification(requester.getUserId(), notificationResponse);
                
                log.info("Sent friend accepted notification from {} to {}", receiver.getUserId(), requester.getUserId());
            } catch (Exception e) {
                log.error("Failed to send friend accepted notification", e);
            }
        });
    }

    @Transactional
    public void rejectFriendRequest(UUID friendshipId, UUID receiverId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResourceNotFoundException("Friend request does not exist"));

        if (!friendship.getReceiver().getUserId().equals(receiverId)) {
            throw new IllegalStateException("You are not authorized to reject this request!");
        }

        // Xóa friendship record thay vì set status
        friendshipRepository.delete(friendship);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getFriends(UUID userId) {
        List<User> asRequester = friendshipRepository.findFriendsAsRequester(userId);
        List<User> asReceiver = friendshipRepository.findFriendsAsReceiver(userId);

        List<User> allFriends = new ArrayList<>();
        allFriends.addAll(asRequester);
        allFriends.addAll(asReceiver);

        return allFriends.stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }


    @Transactional
    public void unfriend(UUID userId, UUID friendId) {
        friendshipRepository.deleteFriendship(userId, friendId);
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<FriendshipResponse> suggestFriends(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> suggestedUsers = friendshipRepository.findSuggestedUsers(userId, pageable);

        List<FriendshipResponse> content = suggestedUsers.getContent()
                .stream()
                .map(u -> FriendshipResponse.builder()
                        .friendshipId(null)
//                        .requesterId(userId)
//                        .receiverId(u.getUserId())
                        .friendProfile(UserResponse.builder()
                                .userId(u.getUserId())
                                .userName(u.getUserProfile().getFullName())
                                .userProfile(u.getUserProfile())
                                .avatarImg(u.getAvatarImg())
                                .coverImg(u.getCoverImg())
                                .build())
                        .status(null) // Chưa có quan hệ
                        .createdAt(null)
                        .build())
                .toList();

        return PageableResponse.<FriendshipResponse>builder()
                .content(content)
                .totalPages(suggestedUsers.getTotalPages())
                .totalElements(suggestedUsers.getTotalElements())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<FriendshipResponse> getSuggestedFriendsOfFriends(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> friendsOfFriends = friendshipRepository.findFriendsOfFriends(userId, pageable);

        List<FriendshipResponse> content = friendsOfFriends.getContent()
                .stream()
                .map(u -> FriendshipResponse.builder()
                        .friendshipId(null)
                        .friendProfile(UserResponse.builder()
                                .userId(u.getUserId())
                                .userName(u.getUserProfile().getFullName())
                                .userProfile(u.getUserProfile())
                                .avatarImg(u.getAvatarImg())
                                .coverImg(u.getCoverImg())
                                .build())
                        .status(null) // Chưa có quan hệ
                        .createdAt(null)
                        .build())
                .toList();

        return PageableResponse.<FriendshipResponse>builder()
                .content(content)
                .totalPages(friendsOfFriends.getTotalPages())
                .totalElements(friendsOfFriends.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public PageableResponse<FriendshipResponse> getPendingRequests(UUID userId, int page, int size) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Friendship> friendships = friendshipRepository.findAllByReceiverAndStatus(
                receiver, FriendShipTypeEnum.PENDING, pageable
        );

        List<FriendshipResponse> content = friendships.getContent().stream()
                .map(f -> FriendshipResponse.builder()
                        .friendshipId(f.getFriendshipId())
                        .requesterId(f.getRequester().getUserId())
                        .receiverId(f.getReceiver().getUserId())
                        .friendProfile(UserResponse.builder()
                                .userId(f.getRequester().getUserId())
                                .userName(f.getRequester().getUserProfile().getFullName())
                                .userProfile(f.getRequester().getUserProfile())
                                .avatarImg(f.getRequester().getAvatarImg())
                                .coverImg(f.getRequester().getCoverImg())
                                .build())
                        .status(f.getStatus())
                        .createdAt(f.getCreatedAt())
                        .build())
                .toList();

        return PageableResponse.<FriendshipResponse>builder()
                .content(content)
                .totalElements(friendships.getTotalElements())
                .totalPages(friendships.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserFriendshipListsResponse getUserFriendshipLists(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        // 1. Pending Requests (lời mời kết bạn nhận được)
        List<Friendship> pendingFriendships = friendshipRepository
                .findAllByReceiverAndStatus(user, FriendShipTypeEnum.PENDING, Pageable.unpaged())
                .getContent();
        
        List<FriendshipResponse> pendingRequests = pendingFriendships.stream()
                .map(f -> FriendshipResponse.builder()
                        .friendshipId(f.getFriendshipId())
                        .requesterId(f.getRequester().getUserId())
                        .receiverId(f.getReceiver().getUserId())
                        .friendProfile(toUserResponse(f.getRequester()))
                        .status(f.getStatus())
                        .createdAt(f.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        // 2. Friends (danh sách bạn bè)
        List<UserResponse> friends = getFriends(userId);

        // 3. Blocked Users (danh sách người đã chặn)
        List<UserResponse> blockedUsers = getBlockedUsers(userId);

        return UserFriendshipListsResponse.builder()
                .pendingRequests(pendingRequests)
                .friends(friends)
                .blockedUsers(blockedUsers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getBlockedUsers(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        // Lấy những người mà user đã chặn (user là requester)
        List<Friendship> blockedByUser = friendshipRepository
                .findAllByRequesterAndStatus(user, FriendShipTypeEnum.BLOCKED, Pageable.unpaged())
                .getContent();

        return blockedByUser.stream()
                .map(f -> toUserResponse(f.getReceiver()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getFriendsBirthdays(UUID userId) {
        List<User> friends = getFriendsList(userId);
        
        // Filter friends who have birthday information and sort by upcoming birthdays
        return friends.stream()
                .filter(f -> f.getUserProfile() != null && f.getUserProfile().getDateOfBirth() != null)
                .sorted((a, b) -> {
                    // Sort by month and day for upcoming birthdays
                    if (a.getUserProfile().getDateOfBirth() == null) return 1;
                    if (b.getUserProfile().getDateOfBirth() == null) return -1;
                    
                    int monthA = a.getUserProfile().getDateOfBirth().getMonthValue();
                    int dayA = a.getUserProfile().getDateOfBirth().getDayOfMonth();
                    int monthB = b.getUserProfile().getDateOfBirth().getMonthValue();
                    int dayB = b.getUserProfile().getDateOfBirth().getDayOfMonth();
                    
                    if (monthA != monthB) return Integer.compare(monthA, monthB);
                    return Integer.compare(dayA, dayB);
                })
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void blockUser(UUID userId, UUID blockedUserId) {
        if (userId.equals(blockedUserId)) {
            throw new IllegalArgumentException("You cannot block yourself!");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocked user does not exist"));

        // Check if a friendship exists and remove it
        Optional<Friendship> existing = friendshipRepository.findFriendshipBetween(userId, blockedUserId);
        existing.ifPresent(friendshipRepository::delete);

        // Create a blocked relationship
        Friendship blockedRelationship = Friendship.builder()
                .requester(user)
                .receiver(blockedUser)
                .status(FriendShipTypeEnum.BLOCKED)
                .build();

        friendshipRepository.save(blockedRelationship);
    }

    @Override
    @Transactional
    public void unblockUser(UUID userId, UUID unblockedUserId) {
        // Find and delete the blocked relationship
        Optional<Friendship> blocked = friendshipRepository.findFriendshipBetween(userId, unblockedUserId);
        
        if (blocked.isPresent() && blocked.get().getStatus() == FriendShipTypeEnum.BLOCKED) {
            friendshipRepository.delete(blocked.get());
        } else {
            throw new ResourceNotFoundException("No blocked relationship found");
        }
    }

    private List<User> getFriendsList(UUID userId) {
        List<User> asRequester = friendshipRepository.findFriendsAsRequester(userId);
        List<User> asReceiver = friendshipRepository.findFriendsAsReceiver(userId);
        
        List<User> allFriends = new ArrayList<>();
        allFriends.addAll(asRequester);
        allFriends.addAll(asReceiver);
        
        return allFriends;
    }

    public UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .userProfile(user.getUserProfile())
                .avatarImg(user.getAvatarImg())
                .coverImg(user.getCoverImg())
                .postsCount(0L)
                .friendsCount(0L)
                .build();
    }
}
