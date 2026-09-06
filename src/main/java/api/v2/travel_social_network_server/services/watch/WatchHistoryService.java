package api.v2.travel_social_network_server.services.watch;

import api.v2.travel_social_network_server.entities.Tag;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.entities.WatchHistory;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import api.v2.travel_social_network_server.repositories.ContentLikeRepository;
import api.v2.travel_social_network_server.repositories.SavedWatchRepository;
import api.v2.travel_social_network_server.repositories.WatchHistoryRepository;
import api.v2.travel_social_network_server.repositories.WatchRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.responses.watch.WatchResponse;
import api.v2.travel_social_network_server.responses.watch.WatchWithIdsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchHistoryService {

    private final WatchHistoryRepository watchHistoryRepository;
    private final WatchRepository watchRepository;
    private final ContentLikeRepository contentLikeRepository;
    private final SavedWatchRepository savedWatchRepository;

    @Transactional
    public void addToHistory(UUID watchId, User user) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));

        // Check if history exists, update or create new
        WatchHistory history = watchHistoryRepository.findByUserAndWatch(user, watch)
                .orElse(WatchHistory.builder()
                        .user(user)
                        .watch(watch)
                        .build());

        watchHistoryRepository.save(history);
    }

    @Transactional
    public void removeFromHistory(UUID watchHistoryId, User user) {
        WatchHistory history = watchHistoryRepository.findById(watchHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch history not found with id: " + watchHistoryId));

        // Check if the history belongs to the user
        if (!history.getUser().getUserId().equals(user.getUserId())) {
            throw new api.v2.travel_social_network_server.exceptions.AccessDeniedException("You don't have permission to delete this history");
        }

        watchHistoryRepository.delete(history);
    }

    @Transactional(readOnly = true)
    public PageableResponse<WatchWithIdsResponse> getWatchHistory(User user, Pageable pageable) {
        Page<WatchHistory> historyPage = watchHistoryRepository.findAllByUserOrderByUpdatedAtDesc(user, pageable);

        List<WatchWithIdsResponse> watchResponses = historyPage.getContent().stream()
                .map(history -> convertToResponseWithIds(
                        history.getWatch(),
                        history.getWatchHistoryId(),
                        user,
                        isWatchLikedByUser(history.getWatch(), user),
                        isWatchSavedByUser(history.getWatch(), user),
                        true // watched = true
                ))
                .collect(Collectors.toList());

        return PageableResponse.<WatchWithIdsResponse>builder()
                .content(watchResponses)
                .totalElements(historyPage.getTotalElements())
                .totalPages(historyPage.getTotalPages())
                .build();
    }

    // Helper methods
    private boolean isWatchLikedByUser(Watch watch, User user) {
        if (user == null) return false;
        return contentLikeRepository.existsByWatchAndUser(watch, user);
    }

    private boolean isWatchSavedByUser(Watch watch, User user) {
        if (user == null) return false;
        return savedWatchRepository.existsByUserAndWatch(user, watch);
    }

    private WatchResponse convertToResponse(Watch watch, User currentUser, Boolean liked, Boolean saved, Boolean watched) {
        String fullName = watch.getUser().getUserProfile() != null && watch.getUser().getUserProfile().getFullName() != null
                ? watch.getUser().getUserProfile().getFullName()
                : watch.getUser().getUsername();

        UserResponse userResponse = UserResponse.builder()
                .userId(watch.getUser().getUserId())
                .userName(fullName)
                .avatarImg(watch.getUser().getAvatarImg())
                .build();

        List<String> tagTitles = watch.getTags() != null
                ? watch.getTags().stream().map(Tag::getTitle).collect(Collectors.toList())
                : new ArrayList<>();

        return WatchResponse.builder()
                .watchId(watch.getWatchId())
                .user(userResponse)
                .title(watch.getTitle())
                .description(watch.getDescription())
                .videoUrl(watch.getVideoUrl())
                .thumbnailUrl(watch.getThumbnailUrl())
                .duration(watch.getDuration())
                .location(watch.getLocation())
                .privacy(watch.getPrivacy())
                .likeCount(watch.getLikeCount())
                .commentCount(watch.getCommentCount())
                .shareCount(watch.getShareCount())
                .viewCount(watch.getViewCount())
                .createdAt(watch.getCreatedAt())
                .updatedAt(watch.getUpdatedAt())
                .tags(tagTitles)
                .liked(liked)
                .saved(saved)
                .watched(watched)
                .build();
    }

    private WatchWithIdsResponse convertToResponseWithIds(Watch watch, UUID watchHistoryId, User currentUser, Boolean liked, Boolean saved, Boolean watched) {
        String fullName = watch.getUser().getUserProfile() != null && watch.getUser().getUserProfile().getFullName() != null
                ? watch.getUser().getUserProfile().getFullName()
                : watch.getUser().getUsername();

        UserResponse userResponse = UserResponse.builder()
                .userId(watch.getUser().getUserId())
                .userName(fullName)
                .avatarImg(watch.getUser().getAvatarImg())
                .build();

        List<String> tagTitles = watch.getTags() != null
                ? watch.getTags().stream().map(Tag::getTitle).collect(Collectors.toList())
                : new ArrayList<>();

        return WatchWithIdsResponse.builder()
                .watchId(watch.getWatchId())
                .user(userResponse)
                .title(watch.getTitle())
                .description(watch.getDescription())
                .videoUrl(watch.getVideoUrl())
                .thumbnailUrl(watch.getThumbnailUrl())
                .duration(watch.getDuration())
                .location(watch.getLocation())
                .privacy(watch.getPrivacy())
                .likeCount(watch.getLikeCount())
                .commentCount(watch.getCommentCount())
                .shareCount(watch.getShareCount())
                .viewCount(watch.getViewCount())
                .createdAt(watch.getCreatedAt())
                .updatedAt(watch.getUpdatedAt())
                .tags(tagTitles)
                .liked(liked)
                .saved(saved)
                .watched(watched)
                .watchHistoryId(watchHistoryId)
                .build();
    }
}
