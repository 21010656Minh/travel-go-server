package api.v2.travel_social_network_server.services.user;

//import api.v2.travel_social_network_server.dtos.user.UpdateUserDto;
//import api.v2.travel_social_network_server.dtos.user.UpdateUserImgDto;
import api.v2.travel_social_network_server.dtos.admin.AdminUpdateUserDto;
import api.v2.travel_social_network_server.dtos.user.UpdateUserDto;
import api.v2.travel_social_network_server.entities.Friendship;
import api.v2.travel_social_network_server.entities.User;
//import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.utilities.enums.StatusTypeEnum;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
//import api.v2.travel_social_network_server.reponses.PageableResponse;
//import api.v2.travel_social_network_server.reponses.post.PostResponse;
//import api.v2.travel_social_network_server.reponses.user.UpdateUserResponse;
//import api.v2.travel_social_network_server.reponses.user.UserResponse;
import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.repositories.FriendshipRepository;
import api.v2.travel_social_network_server.repositories.ContentMediaRepository;
import api.v2.travel_social_network_server.repositories.PostRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;

//import api.v2.travel_social_network_server.utilities.enums.GenderEnum;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.user.UpdateUserResponse;
import api.v2.travel_social_network_server.responses.user.UserMediaResponse;
import api.v2.travel_social_network_server.responses.user.UserPhotosResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.responses.user.UserVideosResponse;
import api.v2.travel_social_network_server.utilities.enums.FriendShipTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.GenderTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements IUserService{

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final PostRepository postRepository;
    private final ContentMediaRepository contentMediaRepository;

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUserName(username).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(UUID userId, UUID currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        FriendShipTypeEnum friendshipStatus = getFriendshipStatus(currentUserId, userId);
        
        // Count posts and friends
        long postsCount = postRepository.countByUser(user);
        long friendsCount = friendshipRepository.countAcceptedFriends(userId);

        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .userProfile(user.getUserProfile())
                .avatarImg(user.getAvatarImg())
                .coverImg(user.getCoverImg())
                .friendshipStatus(friendshipStatus)
                .postsCount(postsCount)
                .friendsCount(friendsCount)
                .build();
    }

    private FriendShipTypeEnum getFriendshipStatus(UUID currentUserId, UUID targetUserId) {
        if (currentUserId == null || currentUserId.equals(targetUserId)) {
            return null;
        }

        Optional<Friendship> friendship = friendshipRepository.findFriendshipBetween(currentUserId, targetUserId);
        
        // Nếu không có friendship record nào thì chưa có quan hệ (trả về null)
        if (friendship.isEmpty()) {
            return null;
        }

        return friendship.get().getStatus();
    }

    @Transactional
    public UpdateUserResponse updateUserProfile(UpdateUserDto updateUserDto, User user) throws IOException {
        User exUser = userRepository.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        exUser.setUserName(updateUserDto.getUserName());

        UserProfile profile = exUser.getUserProfile();
        
        // Combine firstName and lastName into fullName
        String fullName = "";
        if (updateUserDto.getFirstName() != null && !updateUserDto.getFirstName().trim().isEmpty()) {
            fullName = updateUserDto.getFirstName().trim();
        }
        if (updateUserDto.getLastName() != null && !updateUserDto.getLastName().trim().isEmpty()) {
            if (!fullName.isEmpty()) {
                fullName += " " + updateUserDto.getLastName().trim();
            } else {
                fullName = updateUserDto.getLastName().trim();
            }
        }
        profile.setFullName(fullName);
        
        profile.setDateOfBirth(updateUserDto.getDateOfBirth());
        profile.setLocation(updateUserDto.getLocation());
        profile.setAbout(updateUserDto.getAbout());

        profile.setGender(GenderTypeEnum.fromString(updateUserDto.getGender()));

        exUser.setUserProfile(profile);
        User userUpdate = userRepository.save(exUser);

        return UserResponse.builder()
                .userName(userUpdate.getUsername())
                .userProfile(userUpdate.getUserProfile())
                .postsCount(0L)
                .friendsCount(0L)
                .build();
    }


    @Transactional(readOnly = true)
    public PageableResponse<UserResponse> searchUsersByKeyword(String keyword, int page, int pageSize, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> userPage = userRepository.findByUserNameOrFullNameContainingIgnoreCase(keyword, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> mapToUserResponseWithFriendship(user, currentUserId))
                .toList();

        return PageableResponse.<UserResponse>builder()
                .content(userResponses)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<UserResponse> searchUsersFulltext(String keyword, int page, int pageSize, UUID currentUserId) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> userPage = userRepository.searchUsersFulltext(keyword, pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(user -> mapToUserResponseWithFriendship(user, currentUserId))
                .toList();

        return PageableResponse.<UserResponse>builder()
                .content(userResponses)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .last(userPage.isLast())
                .first(userPage.isFirst())
                .build();
    }

    private UserResponse mapToUserResponseWithFriendship(User user, UUID currentUserId) {
        FriendShipTypeEnum friendshipStatus = getFriendshipStatus(currentUserId, user.getUserId());
        
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .avatarImg(user.getAvatarImg())
                .coverImg(user.getCoverImg())
                .userProfile(user.getUserProfile())
                .friendshipStatus(friendshipStatus)
                .postsCount(0L)
                .friendsCount(0L)
                .build();
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUsername())
                .avatarImg(user.getAvatarImg())
                .coverImg(user.getCoverImg())
                .userProfile(user.getUserProfile())
                .friendshipStatus(null)
                .postsCount(0L)
                .friendsCount(0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchUsersForSuggestion(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> userPage = userRepository.searchUsersForSuggestion(keyword, pageable);

        return userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserPhotosResponse getUserPhotos(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get all avatars from AVATAR_UPDATE posts
        List<ContentMedia> avatars = contentMediaRepository.findAllAvatarsByUserId(userId);
        List<UserMediaResponse> avatarList = avatars.stream()
                .map(media -> UserMediaResponse.builder()
                        .mediaId(media.getMediaId().toString())
                        .url(media.getUrl())
                        .createdAt(media.getCreatedAt())
                        .postId(media.getPost().getPostId().toString())
                        .build())
                .toList();

        // Get all covers from COVER_UPDATE posts
        List<ContentMedia> covers = contentMediaRepository.findAllCoversByUserId(userId);
        List<UserMediaResponse> coverList = covers.stream()
                .map(media -> UserMediaResponse.builder()
                        .mediaId(media.getMediaId().toString())
                        .url(media.getUrl())
                        .createdAt(media.getCreatedAt())
                        .postId(media.getPost().getPostId().toString())
                        .build())
                .toList();

        // Get photos from normal posts
        List<ContentMedia> postMediaList = contentMediaRepository.findAllMediaByUserIdAndType(userId, MediaTypeEnum.IMAGE);
        List<UserMediaResponse> postPhotos = postMediaList.stream()
                .map(media -> UserMediaResponse.builder()
                        .mediaId(media.getMediaId().toString())
                        .url(media.getUrl())
                        .createdAt(media.getCreatedAt())
                        .postId(media.getPost().getPostId().toString())
                        .build())
                .toList();

        return UserPhotosResponse.builder()
                .avatars(avatarList)
                .coverImages(coverList)
                .postPhotos(postPhotos)
                .build();
    }

    @Transactional(readOnly = true)
    public UserVideosResponse getUserVideos(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get all videos from normal posts
        List<ContentMedia> videoMediaList = contentMediaRepository.findAllMediaByUserIdAndType(userId, MediaTypeEnum.VIDEO);
        List<UserMediaResponse> videos = videoMediaList.stream()
                .map(media -> UserMediaResponse.builder()
                        .mediaId(media.getMediaId().toString())
                        .url(media.getUrl())
                        .createdAt(media.getCreatedAt())
                        .postId(media.getPost().getPostId().toString())
                        .build())
                .toList();

        return UserVideosResponse.builder()
                .videos(videos)
                .build();
    }

    @Override
    @Transactional
    public UserResponse adminUpdateUser(UUID userId, AdminUpdateUserDto adminUpdateUserDto) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Update user fields
        if (adminUpdateUserDto.getEmail() != null) {
            user.setEmail(adminUpdateUserDto.getEmail());
        }
        if (adminUpdateUserDto.getUserName() != null) {
            user.setUserName(adminUpdateUserDto.getUserName());
        }

        // Update user profile fields
        UserProfile profile = user.getUserProfile();
        if (profile == null) {
            profile = new UserProfile();
            user.setUserProfile(profile);
        }

        if (adminUpdateUserDto.getFullName() != null) {
            profile.setFullName(adminUpdateUserDto.getFullName());
        }
        if (adminUpdateUserDto.getLocation() != null) {
            profile.setLocation(adminUpdateUserDto.getLocation());
        }
        if (adminUpdateUserDto.getAbout() != null) {
            profile.setAbout(adminUpdateUserDto.getAbout());
        }

        User savedUser = userRepository.save(user);

        // Return user response
        long postsCount = postRepository.countByUser(savedUser);
        long friendsCount = friendshipRepository.countAcceptedFriends(userId);

        return UserResponse.builder()
                .userId(savedUser.getUserId())
                .userName(savedUser.getUsername())
                .userProfile(savedUser.getUserProfile())
                .avatarImg(savedUser.getAvatarImg())
                .coverImg(savedUser.getCoverImg())
                .postsCount(postsCount)
                .friendsCount(friendsCount)
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateUserStatus(UUID userId, StatusTypeEnum status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setStatus(status);
        User savedUser = userRepository.save(user);

        long postsCount = postRepository.countByUser(savedUser);
        long friendsCount = friendshipRepository.countAcceptedFriends(userId);

        return UserResponse.builder()
                .userId(savedUser.getUserId())
                .userName(savedUser.getUsername())
                .userProfile(savedUser.getUserProfile())
                .avatarImg(savedUser.getAvatarImg())
                .coverImg(savedUser.getCoverImg())
                .postsCount(postsCount)
                .friendsCount(friendsCount)
                .build();
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        userRepository.delete(user);
    }
}
