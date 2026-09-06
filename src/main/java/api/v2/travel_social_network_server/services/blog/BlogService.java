package api.v2.travel_social_network_server.services.blog;

import api.v2.travel_social_network_server.dtos.blog.BlogDto;
import api.v2.travel_social_network_server.entities.*;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.BlogReviewRepository;
import api.v2.travel_social_network_server.repositories.BlogRepository;
import api.v2.travel_social_network_server.repositories.MediaRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.blog.BlogResponse;
import api.v2.travel_social_network_server.responses.tag.TagResponse;
import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.services.tag.TagService;
import api.v2.travel_social_network_server.utilities.enums.BlogStatusEnum;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.RoleTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogService implements IBlogService {
    
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final TagService tagService;
    private final BlogReviewRepository blogReviewRepository;
    
    @Qualifier("minioMediaStorage")
    private final MediaStorage mediaStorage;
    
    @Override
    @Transactional
    public BlogResponse createBlog(User user, BlogDto blogDto) {
        log.info("Creating blog for user: {}", user.getUserId());
        
        // Process thumbnail file upload if provided
        String thumbnailUrl = blogDto.getThumbnailUrl();
        MultipartFile thumbnailFile = blogDto.getThumbnail();
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                byte[] fileData = thumbnailFile.getBytes();
                thumbnailUrl = mediaStorage.uploadFile(fileData, "thumbnails", thumbnailFile.getContentType());
            } catch (Exception e) {
                log.error("Error uploading thumbnail", e);
                throw new RuntimeException("Failed to upload thumbnail: " + e.getMessage());
            }
        }
        
        // Process tags
        List<Tag> tags = tagService.processTagTitles(blogDto.getTagTitles());
        
        // Process media URLs (legacy support)
        List<ContentMedia> mediaList = processMediaUrls(blogDto.getMediaUrls());
        
        // Determine status
        BlogStatusEnum status = determineStatus(blogDto.getStatus());
        
        // Validate user can set this status
        validateUserBlogStatus(user, status);
        
        // Build blog
        Blog blog = Blog.builder()
                .user(user)
                .title(blogDto.getTitle())
                .content(blogDto.getContent())
                .description(blogDto.getDescription())
                .thumbnailUrl(thumbnailUrl)
                .location(blogDto.getLocation())
                .status(status)
                .isFeatured(blogDto.getIsFeatured() != null ? blogDto.getIsFeatured() : false)
                .readingTime(blogDto.getReadingTime())
                .tags(tags)
                .mediaList(mediaList)
                .build();
        
        // Set blog reference for media
        if (mediaList != null) {
            mediaList.forEach(media -> media.setBlog(blog));
        }
        
        // Set publishedAt if status is PUBLISHED
        if (status == BlogStatusEnum.PUBLISHED) {
            blog.setPublishedAt(Instant.now());
        }
        
        Blog savedBlog = blogRepository.save(blog);
        
        // Link media IDs from content editor to blog
        if (blogDto.getMediaIds() != null && !blogDto.getMediaIds().isEmpty()) {
            for (UUID mediaId : blogDto.getMediaIds()) {
                try {
                    ContentMedia media = mediaRepository.findById(mediaId)
                            .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));
                    media.setBlog(savedBlog);
                    mediaRepository.save(media);
                } catch (Exception e) {
                    log.error("Error linking media {} to blog {}", mediaId, savedBlog.getBlogId(), e);
                }
            }
        }
        
        log.info("Blog created successfully with ID: {}", savedBlog.getBlogId());
        
        return toBlogResponse(savedBlog, user);
    }
    
    @Override
    @Transactional
    public BlogResponse updateBlog(UUID blogId, User user, BlogDto blogDto) {
        log.info("Updating blog: {} by user: {}", blogId, user.getUserId());
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + blogId));
        
        // Check ownership
        if (!blog.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("You are not authorized to update this blog");
        }
        
        // Handle thumbnail upload
        MultipartFile thumbnailFile = blogDto.getThumbnail();
        String thumbnailUrl = blog.getThumbnailUrl(); // Keep existing if no new file
        
        if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
            try {
                byte[] fileData = thumbnailFile.getBytes();
                thumbnailUrl = mediaStorage.uploadFile(fileData, "thumbnails", thumbnailFile.getContentType());
            } catch (Exception e) {
                log.error("Error uploading thumbnail", e);
                throw new RuntimeException("Failed to upload thumbnail: " + e.getMessage());
            }
        } else if (blogDto.getThumbnailUrl() != null) {
            // If no file but URL provided, use URL (for cases where thumbnail wasn't changed)
            thumbnailUrl = blogDto.getThumbnailUrl();
        }
        
        // Update fields
        blog.setTitle(blogDto.getTitle());
        blog.setContent(blogDto.getContent());
        blog.setDescription(blogDto.getDescription());
        blog.setThumbnailUrl(thumbnailUrl);
        blog.setLocation(blogDto.getLocation());
        blog.setReadingTime(blogDto.getReadingTime());
        
        // Update status
        BlogStatusEnum newStatus = determineStatus(blogDto.getStatus());
        
        // Validate user can set this status
        validateUserBlogStatus(user, newStatus);
        
        if (newStatus != blog.getStatus() && newStatus == BlogStatusEnum.PUBLISHED) {
            blog.setPublishedAt(Instant.now());
        }
        blog.setStatus(newStatus);
        
        // Update featured flag
        if (blogDto.getIsFeatured() != null) {
            blog.setIsFeatured(blogDto.getIsFeatured());
        }
        
        // Update tags
        if (blogDto.getTagTitles() != null) {
            List<Tag> tags = tagService.processTagTitles(blogDto.getTagTitles());
            blog.setTags(tags);
        }
        
        // Update media
        if (blogDto.getMediaUrls() != null) {
            blog.getMediaList().clear();
            List<ContentMedia> newMedia = processMediaUrls(blogDto.getMediaUrls());
            if (newMedia != null) {
                newMedia.forEach(media -> media.setBlog(blog));
                blog.getMediaList().addAll(newMedia);
            }
        }
        
        // Save blog first
        Blog updatedBlog = blogRepository.save(blog);
        
        // Link media IDs to blog (for uploaded content images)
        if (blogDto.getMediaIds() != null && !blogDto.getMediaIds().isEmpty()) {
            log.info("Linking {} media files to blog: {}", blogDto.getMediaIds().size(), blogId);
            for (UUID mediaId : blogDto.getMediaIds()) {
                Optional<ContentMedia> mediaOpt = mediaRepository.findById(mediaId);
                if (mediaOpt.isPresent()) {
                    ContentMedia media = mediaOpt.get();
                    media.setBlog(updatedBlog);
                    mediaRepository.save(media);
                }
            }
        }
        return toBlogResponse(updatedBlog, user);
    }
    
    @Override
    @Transactional
    public void deleteBlog(UUID blogId, User user) {
        log.info("Deleting blog: {} by user: {}", blogId, user.getUserId());
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + blogId));
        
        // Check ownership
        if (!blog.getUser().getUserId().equals(user.getUserId())) {
            throw new SecurityException("You are not authorized to delete this blog");
        }
        
        blogRepository.delete(blog);
    }
    
    @Override
    @Transactional
    public BlogResponse getBlogById(UUID blogId, User currentUser) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + blogId));
        
        // Check if blog is published or user is owner or user is admin
        if (blog.getStatus() != BlogStatusEnum.PUBLISHED) {
            boolean isOwner = currentUser != null && blog.getUser().getUserId().equals(currentUser.getUserId());
            boolean isAdmin = currentUser != null && currentUser.getRole() != null && "ADMIN".equals(currentUser.getRole().name());
            
            if (!isOwner && !isAdmin) {
                throw new ResourceNotFoundException("Blog not found with ID: " + blogId);
            }
        }
        
        return toBlogResponse(blog, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> getAllBlogs(int page, int size, String sort, User currentUser) {
        Pageable pageable = createPageable(page, size, sort);
        Page<Blog> blogPage = blogRepository.findAllByStatusOrderByCreatedAtDesc(BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> getBlogsByUser(UUID userId, int page, int size, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        Pageable pageable = PageRequest.of(page, size);
        
        // If viewing own blogs, show all statuses; otherwise only published
        Page<Blog> blogPage;
        if (currentUser != null && currentUser.getUserId().equals(userId)) {
            blogPage = blogRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);
        } else {
            blogPage = blogRepository.findAllByUserAndStatusOrderByCreatedAtDesc(user, BlogStatusEnum.PUBLISHED, pageable);
        }
        
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> getMyBlogs(User user, int page, int size, String status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage;
        
        if (status != null && !status.isEmpty()) {
            BlogStatusEnum blogStatus = BlogStatusEnum.fromValue(status.toUpperCase());
            blogPage = blogRepository.findAllByUserAndStatusOrderByCreatedAtDesc(user, blogStatus, pageable);
        } else {
            blogPage = blogRepository.findAllByUserOrderByCreatedAtDesc(user, pageable);
        }
        
        return toPageableResponse(blogPage, user);
    }
    
    @Override
    public PageableResponse<BlogResponse> getFeaturedBlogs(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.findAllByIsFeaturedTrueAndStatusOrderByCreatedAtDesc(BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> searchBlogs(String query, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.searchBlogs(query, BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> searchBlogsFulltext(String query, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.searchBlogsFulltext(query, BlogStatusEnum.PUBLISHED.name(), pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> searchBlogsByLocation(String location, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.searchBlogsByLocation(location, BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> getBlogsByTag(UUID tagId, int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.findAllByTagId(tagId, BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> getTrendingBlogs(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.findTrendingBlogs(BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    public PageableResponse<BlogResponse> getPopularBlogs(int page, int size, User currentUser) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Blog> blogPage = blogRepository.findPopularBlogs(BlogStatusEnum.PUBLISHED, pageable);
        return toPageableResponse(blogPage, currentUser);
    }
    
    @Override
    @Transactional
    public void incrementViewCount(UUID blogId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with ID: " + blogId));
        blog.incrementViewCount();
        blogRepository.save(blog);
    }
    
    // Helper methods
    
    private BlogStatusEnum determineStatus(String statusStr) {
        if (statusStr == null || statusStr.isEmpty()) {
            return BlogStatusEnum.DRAFT; // Default to DRAFT for user safety
        }
        return BlogStatusEnum.fromValue(statusStr.toUpperCase());
    }
    
    private void validateUserBlogStatus(User user, BlogStatusEnum status) {
        // Only allow DRAFT and PENDING for regular users
        // PUBLISHED can only be set by admin through moderation/approval
        if (status == BlogStatusEnum.PUBLISHED && !isAdmin(user)) {
            throw new SecurityException("Only administrators can publish blogs directly. Please submit for review (PENDING status).");
        }
    }
    
    private boolean isAdmin(User user) {
        return user.getRole() == RoleTypeEnum.ADMIN;
    }
    
    private List<ContentMedia> processMediaUrls(List<String> mediaUrls) {
        if (mediaUrls == null || mediaUrls.isEmpty()) {
            return new ArrayList<>();
        }
        
        return mediaUrls.stream()
                .map(url -> ContentMedia.builder()
                        .url(url)
                        .type(determineMediaType(url))
                        .build())
                .collect(Collectors.toList());
    }
    
    private MediaTypeEnum determineMediaType(String url) {
        String lowerUrl = url.toLowerCase();
        if (lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mov") || lowerUrl.endsWith(".avi")) {
            return MediaTypeEnum.VIDEO;
        }
        return MediaTypeEnum.IMAGE;
    }
    
    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.isEmpty()) {
            // Support multiple sort fields separated by semicolon
            // Example: "averageRating,desc;totalRatings,desc;createdAt,desc"
            if (sort.contains(";")) {
                String[] sortFields = sort.split(";");
                Sort combinedSort = null;
                for (String sortField : sortFields) {
                    String[] sortParams = sortField.trim().split(",");
                    String field = sortParams[0];
                    Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                            ? Sort.Direction.ASC : Sort.Direction.DESC;
                    Sort currentSort = Sort.by(direction, field);
                    combinedSort = combinedSort == null ? currentSort : combinedSort.and(currentSort);
                }
                return PageRequest.of(page, size, combinedSort);
            } else {
                // Single sort field
                String[] sortParams = sort.split(",");
                String field = sortParams[0];
                Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") 
                        ? Sort.Direction.ASC : Sort.Direction.DESC;
                return PageRequest.of(page, size, Sort.by(direction, field));
            }
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    
    private PageableResponse<BlogResponse> toPageableResponse(Page<Blog> blogPage, User currentUser) {
        List<BlogResponse> responses = blogPage.getContent().stream()
                .map(blog -> toBlogResponse(blog, currentUser))
                .collect(Collectors.toList());
        
        return PageableResponse.<BlogResponse>builder()
                .content(responses)
                .totalPages(blogPage.getTotalPages())
                .totalElements(blogPage.getTotalElements())
                .build();
    }
    
    private BlogResponse toBlogResponse(Blog blog, User currentUser) {
        // Check if current user has already reviewed this blog
        boolean hasReviewed = false;
        if (currentUser != null) {
            hasReviewed = blogReviewRepository.existsByBlogAndUserAndRatingIsNotNull(blog, currentUser);
        }
        
        return BlogResponse.builder()
                .blogId(blog.getBlogId())
                .author(toUserSummary(blog.getUser()))
                .title(blog.getTitle())
                .content(blog.getContent())
                .description(blog.getDescription())
                .thumbnailUrl(blog.getThumbnailUrl())
                .location(blog.getLocation())
                .viewCount(blog.getViewCount())
                .averageRating(blog.getAverageRating())
                .totalRatings(blog.getTotalRatings())
                .status(blog.getStatus().getValue())
                .isFeatured(blog.getIsFeatured())
                .readingTime(blog.getReadingTime())
                .tags(blog.getTags() != null ? blog.getTags().stream()
                        .map(this::toTagResponse)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .mediaUrls(blog.getMediaList() != null ? blog.getMediaList().stream()
                        .map(ContentMedia::getUrl)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(blog.getCreatedAt())
                .updatedAt(blog.getUpdatedAt())
                .publishedAt(blog.getPublishedAt())
                .hasReviewed(hasReviewed)
                .build();
    }
    
    private UserSummaryResponse toUserSummary(User user) {
        String displayName = user.getUsername();
        if (user.getUserProfile() != null && user.getUserProfile().getFullName() != null) {
            displayName = user.getUserProfile().getFullName();
        }
        
        return UserSummaryResponse.builder()
                .userId(user.getUserId())
                .userName(displayName)
                .email(user.getEmail())
                .avatarImg(user.getAvatarImg())
                .build();
    }
    
    private TagResponse toTagResponse(Tag tag) {
        return TagResponse.builder()
                .tagId(tag.getTagId())
                .title(tag.getTitle())
                .slug(tag.getSlug())
                .createdAt(tag.getCreatedAt())
                .build();
    }
}
