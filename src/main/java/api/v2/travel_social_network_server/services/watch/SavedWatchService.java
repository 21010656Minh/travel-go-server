package api.v2.travel_social_network_server.services.watch;

import api.v2.travel_social_network_server.entities.SavedWatch;
import api.v2.travel_social_network_server.entities.Tag;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.exceptions.ResourceAlreadyExistedException;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.SavedWatchRepository;
import api.v2.travel_social_network_server.repositories.WatchRepository;
import api.v2.travel_social_network_server.repositories.ContentLikeRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.watch.WatchResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
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
public class SavedWatchService {

    private final SavedWatchRepository savedWatchRepository;
    private final WatchRepository watchRepository;
    private final ContentLikeRepository contentLikeRepository;

    @Transactional
    public void saveWatch(UUID watchId, User user) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));

        // Check if already saved
        if (savedWatchRepository.existsByUserAndWatch(user, watch)) {
            throw new ResourceAlreadyExistedException("Watch already saved");
        }

        SavedWatch savedWatch = SavedWatch.builder()
                .user(user)
                .watch(watch)
                .build();

        savedWatchRepository.save(savedWatch);
    }

    @Transactional
    public void unsaveWatch(UUID watchId, User user) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));

        SavedWatch savedWatch = savedWatchRepository.findByUserAndWatch(user, watch)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not saved"));

        savedWatchRepository.delete(savedWatch);
    }

    @Transactional(readOnly = true)
    public PageableResponse<WatchResponse> getSavedWatches(User user, Pageable pageable) {
        Page<SavedWatch> savedWatchPage = savedWatchRepository.findAllByUser(user, pageable);

        List<WatchResponse> watchResponses = savedWatchPage.getContent().stream()
                .map(savedWatch -> convertToResponse(
                        savedWatch.getWatch(),
                        user,
                        isWatchLikedByUser(savedWatch.getWatch(), user),
                        true // isSaved = true
                ))
                .collect(Collectors.toList());

        return PageableResponse.<WatchResponse>builder()
                .content(watchResponses)
                .totalElements(savedWatchPage.getTotalElements())
                .totalPages(savedWatchPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public boolean isWatchSaved(UUID watchId, User user) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));
        return savedWatchRepository.existsByUserAndWatch(user, watch);
    }

    // Helper methods
    private boolean isWatchLikedByUser(Watch watch, User user) {
        if (user == null) return false;
        return contentLikeRepository.existsByWatchAndUser(watch, user);
    }

    private WatchResponse convertToResponse(Watch watch, User currentUser, Boolean liked, Boolean saved) {
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
                .watched(false)
                .build();
    }

}
