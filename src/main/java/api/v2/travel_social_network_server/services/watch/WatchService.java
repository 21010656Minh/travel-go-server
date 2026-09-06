package api.v2.travel_social_network_server.services.watch;

import api.v2.travel_social_network_server.dtos.watch.WatchCreateDto;
import api.v2.travel_social_network_server.dtos.watch.WatchUpdateInfoDto;
import api.v2.travel_social_network_server.entities.Tag;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.ContentLikeRepository;
import api.v2.travel_social_network_server.repositories.SavedWatchRepository;
import api.v2.travel_social_network_server.repositories.TagRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.repositories.WatchRepository;
import api.v2.travel_social_network_server.responses.watch.WatchResponse;
import api.v2.travel_social_network_server.responses.watch.WatchStatisticsResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WatchService {

    private final WatchRepository watchRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final ContentLikeRepository contentLikeRepository;
    private final SavedWatchRepository savedWatchRepository;
    
    @Qualifier("minioStorageAdapter")
    private final MediaStorage mediaStorage;

    @Transactional
    public WatchResponse createWatch(WatchCreateDto dto, User user) {
        // User is passed from controller via @AuthenticationPrincipal

        try {
            // Upload video file
            byte[] videoData = dto.getVideo().getBytes();
            String videoContentType = dto.getVideo().getContentType();
            String videoUrl = mediaStorage.uploadFile(videoData, "watches/videos", videoContentType);

            // Upload thumbnail if provided, otherwise use default or generate
            String thumbnailUrl = null;
            if (dto.getThumbnail() != null) {
                byte[] thumbnailData = dto.getThumbnail().getBytes();
                String thumbnailContentType = dto.getThumbnail().getContentType();
                thumbnailUrl = mediaStorage.uploadFile(thumbnailData, "watches/thumbnails", thumbnailContentType);
            }

        // Create Watch entity
        Watch watch = Watch.builder()
                .user(user)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .videoUrl(videoUrl)
                .thumbnailUrl(thumbnailUrl)
                .duration(dto.getDuration())
                .location(dto.getLocation())
                .privacy(dto.getPrivacy())
                .likeCount(0)
                .commentCount(0)
                .shareCount(0)
                .viewCount(0)
                .build();

            // Handle tags
            if (dto.getTags() != null && !dto.getTags().isEmpty()) {
                List<Tag> tags = new ArrayList<>();
                for (String tagTitle : dto.getTags()) {
                    Tag tag = tagRepository.findByTitle(tagTitle)
                            .orElseGet(() -> {
                                Tag newTag = Tag.builder()
                                        .title(tagTitle)
                                        .slug(normalizeSlug(tagTitle))
                                        .build();
                                return tagRepository.save(newTag);
                            });
                    tags.add(tag);
                }
                watch.setTags(tags);
            }

            // Save watch
            Watch savedWatch = watchRepository.save(watch);

            // Return response
            return convertToResponse(savedWatch, user, false, false);
            
        } catch (IOException e) {
            throw new IllegalStateException("Error uploading watch files: " + e.getMessage(), e);
        }
    }

    // Get all public watches with pagination
    @Transactional(readOnly = true)
    public PageableResponse<WatchResponse> getAllPublicWatches(Pageable pageable, User currentUser) {
        Page<Watch> watchPage = watchRepository.findByPrivacy(PrivacyTypeEnum.PUBLIC, pageable);
        
        List<WatchResponse> watchResponses = watchPage.getContent().stream()
                .map(watch -> convertToResponse(watch, 
                        currentUser,
                        isWatchLikedByUser(watch, currentUser),
                        false))
                .collect(Collectors.toList());

        return PageableResponse.<WatchResponse>builder()
                .content(watchResponses)
                .totalElements(watchPage.getTotalElements())
                .totalPages(watchPage.getTotalPages())
                .build();
    }

    // Get watches by user ID
    @Transactional(readOnly = true)
    public PageableResponse<WatchResponse> getWatchesByUser(UUID userId, Pageable pageable, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Page<Watch> watchPage;
        
        // If viewing own profile, show all watches. Otherwise, show only public watches
        if (currentUser != null && user.getUserId().equals(currentUser.getUserId())) {
            watchPage = watchRepository.findAllByUser(user, pageable);
        } else {
            watchPage = watchRepository.findByPrivacyAndUser(PrivacyTypeEnum.PUBLIC, user, pageable);
        }

        List<WatchResponse> watchResponses = watchPage.getContent().stream()
                .map(watch -> convertToResponse(watch, 
                        currentUser,
                        isWatchLikedByUser(watch, currentUser),
                        false))
                .collect(Collectors.toList());

        return PageableResponse.<WatchResponse>builder()
                .content(watchResponses)
                .totalElements(watchPage.getTotalElements())
                .totalPages(watchPage.getTotalPages())
                .build();
    }

    // Get watch by ID
    @Transactional
    public WatchResponse getWatchById(UUID watchId, User currentUser) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));

        // Check privacy
        if (!watch.getPrivacy().equals(PrivacyTypeEnum.PUBLIC) 
                && (currentUser == null || !watch.getUser().getUserId().equals(currentUser.getUserId()))) {
            throw new AccessDeniedException("You don't have permission to view this watch");
        }

        // Increment view count
        watch.setViewCount(watch.getViewCount() + 1);
        watchRepository.save(watch);

        return convertToResponse(watch, 
                currentUser,
                isWatchLikedByUser(watch, currentUser),
                true);
    }

    // Update watch (cannot update video file)
    @Transactional
    public WatchResponse updateWatch(UUID watchId, WatchUpdateInfoDto dto, User currentUser) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));

        // Check ownership
        if (!watch.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You don't have permission to update this watch");
        }

        try {
            // Update fields if provided
            if (dto.getTitle() != null) {
                watch.setTitle(dto.getTitle());
            }

            if (dto.getDescription() != null) {
                watch.setDescription(dto.getDescription());
            }

            if (dto.getLocation() != null) {
                watch.setLocation(dto.getLocation());
            }

            if (dto.getPrivacy() != null) {
                watch.setPrivacy(dto.getPrivacy());
            }

            // Upload new thumbnail if provided
            if (dto.getThumbnail() != null) {
                byte[] thumbnailData = dto.getThumbnail().getBytes();
                String thumbnailContentType = dto.getThumbnail().getContentType();
                String thumbnailUrl = mediaStorage.uploadFile(thumbnailData, "watches/thumbnails", thumbnailContentType);
                watch.setThumbnailUrl(thumbnailUrl);
            }

            // Handle tags update
            if (dto.getTags() != null) {
                List<Tag> tags = new ArrayList<>();
                for (String tagTitle : dto.getTags()) {
                    Tag tag = tagRepository.findByTitle(tagTitle)
                            .orElseGet(() -> {
                                Tag newTag = Tag.builder()
                                        .title(tagTitle)
                                        .slug(normalizeSlug(tagTitle))
                                        .build();
                                return tagRepository.save(newTag);
                            });
                    tags.add(tag);
                }
                watch.setTags(tags);
            }

            // Save updated watch
            Watch updatedWatch = watchRepository.save(watch);

            // Return response
            return convertToResponse(updatedWatch, currentUser, 
                    isWatchLikedByUser(updatedWatch, currentUser), false);

        } catch (IOException e) {
            throw new IllegalStateException("Error uploading thumbnail: " + e.getMessage(), e);
        }
    }

    // Delete watch
    @Transactional
    public void deleteWatch(UUID watchId, User currentUser) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found with id: " + watchId));

        // Check ownership
        if (!watch.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new AccessDeniedException("You don't have permission to delete this watch");
        }

        watchRepository.delete(watch);
    }

    // Get user watch statistics
    @Transactional(readOnly = true)
    public WatchStatisticsResponse getUserWatchStatistics(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get all watches of user (unpaged to count all)
        List<Watch> userWatches = watchRepository.findAllByUser(user, Pageable.unpaged()).getContent();

        long totalVideos = userWatches.size();
        long totalViews = userWatches.stream()
                .mapToLong(w -> w.getViewCount() != null ? w.getViewCount() : 0)
                .sum();
        long totalLikes = userWatches.stream()
                .mapToLong(w -> w.getLikeCount() != null ? w.getLikeCount() : 0)
                .sum();
        long totalComments = userWatches.stream()
                .mapToLong(w -> w.getCommentCount() != null ? w.getCommentCount() : 0)
                .sum();
        long totalShares = userWatches.stream()
                .mapToLong(w -> w.getShareCount() != null ? w.getShareCount() : 0)
                .sum();

        return WatchStatisticsResponse.builder()
                .totalVideos(totalVideos)
                .totalViews(totalViews)
                .totalLikes(totalLikes)
                .totalComments(totalComments)
                .totalShares(totalShares)
                .build();
    }

    // Get trending watches (based on view count and like count within specified days)
    @Transactional(readOnly = true)
    public PageableResponse<WatchResponse> getTrendingWatches(Pageable pageable, int days, User currentUser) {
        java.time.Instant sinceDate = java.time.Instant.now().minus(days, java.time.temporal.ChronoUnit.DAYS);
        Page<Watch> watchPage = watchRepository.findTrendingWatches(sinceDate, pageable);
        
        List<WatchResponse> watchResponses = watchPage.getContent().stream()
                .map(watch -> convertToResponse(watch, 
                        currentUser,
                        isWatchLikedByUser(watch, currentUser),
                        false))
                .collect(Collectors.toList());

        return PageableResponse.<WatchResponse>builder()
                .content(watchResponses)
                .totalElements(watchPage.getTotalElements())
                .totalPages(watchPage.getTotalPages())
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

    private WatchResponse convertToResponse(Watch watch, User currentUser, Boolean liked, Boolean watched) {
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
                .saved(isWatchSavedByUser(watch, currentUser))
                .watched(watched)
                .build();
    }

    private String normalizeSlug(String text) {
        return text.toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-")
                .substring(0, Math.min(text.length(), 50));
    }
}
