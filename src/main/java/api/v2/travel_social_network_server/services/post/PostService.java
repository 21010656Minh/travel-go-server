package api.v2.travel_social_network_server.services.post;

import api.v2.travel_social_network_server.dtos.notification.CreateNotificationDto;
import api.v2.travel_social_network_server.dtos.post.UpdatePostDto;
import api.v2.travel_social_network_server.entities.*;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.GroupMemberRepository;
import api.v2.travel_social_network_server.repositories.GroupRepository;
import api.v2.travel_social_network_server.repositories.ContentMediaRepository;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.post.PostMediaResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.repositories.PostRepository;
import api.v2.travel_social_network_server.services.notification.INotificationService;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.services.post.strategy.PostResponseMapper;
import api.v2.travel_social_network_server.services.tag.TagService;
import api.v2.travel_social_network_server.utilities.builder.PostBuilder;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service("postService")
@RequiredArgsConstructor
public class PostService implements IPostService {

    private static final String POST_FOLDER = "posts";

    private final PostRepository postRepository;
    
    @Qualifier("minioStorageAdapter")
    private final MediaStorage cloudinaryStorageAdapter;
    
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ContentMediaRepository postMediaRepository;
    private final PostResponseMapper postResponseMapper;
    private final TagService tagService;
    private final INotificationService notificationService;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PostResponse getPostById(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));
        
        // Check privacy: if post is not PUBLIC and user is not the owner
        if (post.getPrivacy() != PrivacyTypeEnum.PUBLIC) {
            if (user == null || !post.getUser().getUserId().equals(user.getUserId())) {
                throw new ResourceNotFoundException("Post not found or you don't have permission to view this post");
            }
        }
        
        return postResponseMapper.toResponse(post, user);
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getPostsByIds(List<UUID> postIds, User user) {
        if (postIds == null || postIds.isEmpty()) {
            return List.of();
        }

        // Batch query all posts by IDs (single DB query with WHERE IN clause)
        List<Post> posts = postRepository.findAllById(postIds);
        
        // Filter by privacy and convert to response, preserving order
        Map<UUID, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getPostId, post -> post));
        
        return postIds.stream()
                .map(postMap::get)
                .filter(post -> post != null)
                .filter(post -> {
                    // Filter out non-public posts that user doesn't own
                    if (post.getPrivacy() != PrivacyTypeEnum.PUBLIC) {
                        return user != null && post.getUser().getUserId().equals(user.getUserId());
                    }
                    return true;
                })
                .map(post -> postResponseMapper.toResponse(post, user))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> getPostsByUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findAllByUser(user, pageable);

        List<PostResponse> content = posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, user))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> getPostsByGroup(UUID groupId, User user, int page, int size, String sort) {

        if(!groupMemberRepository.existsByGroupGroupIdAndUserUserId(groupId, user.getUserId()) ) {
            throw new ResourceNotFoundException("User not member of group with id: " + groupId);
        }

        // Determine sort order based on sort parameter
        Sort sortOrder;
        switch (sort.toLowerCase()) {
            case "new_activity":
                // Sort by last comment/activity time (for now, use updatedAt or createdAt)
                // TODO: Add lastActivityAt field to Post entity for accurate activity sorting
                sortOrder = Sort.by("createdAt").descending();
                break;
            case "relevant":
                // Sort by relevance (for now, use likeCount * 2 + commentCount)
                // TODO: Implement proper relevance algorithm
                sortOrder = Sort.by("createdAt").descending();
                break;
            case "new_post":
            default:
                // Sort by newest posts (createdAt descending)
                sortOrder = Sort.by("createdAt").descending();
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Post> posts = postRepository.findAllByGroupGroupId(groupId, pageable);
        System.out.println(posts.getContent());

        List<PostResponse> content = posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, user))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> getPostsByPrivacy(User user, PrivacyTypeEnum privacy, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByPrivacy(privacy, pageable);

        List<PostResponse> content = posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, user))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    // With PostType filter for newsfeed optimization
    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> getPostsByPrivacyAndPostType(User user, PrivacyTypeEnum privacy, PostTypeEnum postType, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByPrivacyAndPostType(privacy, postType, pageable);

        List<PostResponse> content = posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, user))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> getPostsByPrivacyAndUser(PrivacyTypeEnum privacy, User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.findByPrivacyAndUser(privacy, user, pageable);

        List<PostResponse> content = posts.getContent().stream()
                .map(post ->postResponseMapper.toResponse(post, user))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> searchPostsInGroup(User user, UUID groupId, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.searchPostsInGroup(groupId, keyword, pageable);

        List<PostResponse> content = posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, user))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(content)
                .totalElements(posts.getTotalElements())
                .totalPages(posts.getTotalPages())
                .build();
    }

    @Override
    @Transactional
    public PostResponse createPostMultiTask(User user, UpdatePostDto dto, UUID groupId) {
        Post post = new PostBuilder(user, dto)
                .withTags(dto.getTags(), tagService)
                .withMedia(dto.getMediaFiles(), cloudinaryStorageAdapter, POST_FOLDER)
                .withGroup(groupId, groupRepository)
                .build();

        Post saved = postRepository.save(post);
        
        // Update group's lastActivityAt when post is created in a group
        if (groupId != null && saved.getGroup() != null) {
            Group group = saved.getGroup();
            group.setLastActivityAt(java.time.LocalDateTime.now());
            groupRepository.save(group);
        }
        
        // Handle AVATAR_UPDATE and COVER_UPDATE post types
        if (saved.getPostType() == PostTypeEnum.AVATAR_UPDATE || saved.getPostType() == PostTypeEnum.COVER_UPDATE) {
            if (saved.getMediaList() != null && !saved.getMediaList().isEmpty()) {
                String imageUrl = saved.getMediaList().get(0).getUrl();
                User exUser = userRepository.findById(user.getUserId())
                        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                
                if (saved.getPostType() == PostTypeEnum.AVATAR_UPDATE) {
                    exUser.setAvatarImg(imageUrl);
                } else if (saved.getPostType() == PostTypeEnum.COVER_UPDATE) {
                    exUser.setCoverImg(imageUrl);
                }
                
                userRepository.save(exUser);
            }
        }
        
        System.out.println(saved);
        return postResponseMapper.toResponse(saved, user);
    }

    @Override
    public void deletePost(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Chỉ chủ post mới có quyền xoá
        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("You are not allowed to delete this post");
        }

        postRepository.delete(post);
    }


    @Override
    public PostResponse updatePostPrivacy(UUID postId, PrivacyTypeEnum privacy, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("You are not allowed to update privacy of this post");
        }

        post.setPrivacy(privacy);
        postRepository.save(post);

        return postResponseMapper.toResponse(post, user);
    }

    @Override
    @Transactional
    public PostResponse updatePost(UUID postId, UpdatePostDto updatePostDto, User user) throws java.io.IOException {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        if (!post.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalStateException("You are not allowed to update this post");
        }

        // Update content
        if (updatePostDto.getContent() != null) {
            post.setContent(updatePostDto.getContent());
        }

        // Update location
        if (updatePostDto.getLocation() != null) {
            post.setLocation(updatePostDto.getLocation());
        }

        // Update privacy
        if (updatePostDto.getPrivacy() != null) {
            post.setPrivacy(PrivacyTypeEnum.fromString(updatePostDto.getPrivacy()));
        }

        // Update tags if provided
        if (updatePostDto.getTags() != null && !updatePostDto.getTags().isEmpty()) {
            post.getTags().clear();
            List<Tag> tags = tagService.processTagTitles(updatePostDto.getTags());
            post.getTags().addAll(tags);
        }

        // Handle media files if provided (add new media)
        if (updatePostDto.getMediaFiles() != null && !updatePostDto.getMediaFiles().isEmpty()) {
            MediaTypeEnum mediaType = MediaTypeEnum.valueOf(updatePostDto.getMediaType());
            
            updatePostDto.getMediaFiles().forEach(file -> {
                try {
                    byte[] fileData = file.getBytes();
                    String contentType = file.getContentType();
                    String url = cloudinaryStorageAdapter.uploadFile(fileData, POST_FOLDER, contentType);
                    ContentMedia media = ContentMedia.builder()
                            .url(url)
                            .post(post)
                            .type(mediaType)
                            .build();
                    post.getMediaList().add(media);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to upload media file", e);
                }
            });
        }

        postRepository.save(post);
        return postResponseMapper.toResponse(post, user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PostResponse> searchPostsForSuggestion(String keyword, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.searchPostsForSuggestion(keyword, pageable);

        return posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, null)) // null user for suggestion
                .toList();
    }

    // With PostType filter for newsfeed optimization
    @Transactional(readOnly = true)
    public List<PostResponse> searchPostsForSuggestionByPostType(String keyword, PostTypeEnum postType, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        Page<Post> posts = postRepository.searchPostsForSuggestionByPostType(keyword, postType, pageable);

        return posts.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, null)) // null user for suggestion
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageableResponse<PostResponse> searchPostsFulltext(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Post> postsPage = postRepository.searchPostsForSuggestion(keyword, pageable);

        List<PostResponse> postResponses = postsPage.getContent().stream()
                .map(post -> postResponseMapper.toResponse(post, null))
                .toList();

        return PageableResponse.<PostResponse>builder()
                .content(postResponses)
                .pageNumber(postsPage.getNumber())
                .pageSize(postsPage.getSize())
                .totalElements(postsPage.getTotalElements())
                .totalPages(postsPage.getTotalPages())
                .last(postsPage.isLast())
                .first(postsPage.isFirst())
                .build();
    }

    @Override
    @Transactional
    public PostResponse sharePost(UUID postId, String content, PrivacyTypeEnum privacy, User user) {
        // Find the original post
        Post originalPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + postId));

        // Create a new post that shares the original
        Post sharedPost = Post.builder()
                .user(user)
                .content(content != null ? content : "") // Optional share text
                .privacy(privacy)
                .isShare(true)
                .sharedPost(originalPost)
                .shareCount(0)
                .commentCount(0)
                .likeCount(0)
                .build();

        // Increment share count on original post
        originalPost.setShareCount((originalPost.getShareCount() != null ? originalPost.getShareCount() : 0) + 1);
        postRepository.save(originalPost);

        // Save the shared post
        Post saved = postRepository.save(sharedPost);

        // Create notification for the original post owner (if not sharing own post)
        if (!originalPost.getUser().getUserId().equals(user.getUserId())) {
            String sharerName = user.getUserProfile() != null && user.getUserProfile().getFullName() != null
                    ? user.getUserProfile().getFullName()
                    : user.getUsername();
            
            CreateNotificationDto notificationDto = CreateNotificationDto.builder()
                    .receiverId(originalPost.getUser().getUserId())
                    .senderId(user.getUserId())
                    .type(NotificationTypeEnum.POST_SHARE.name())
                    .content(sharerName + " đã chia sẻ bài viết của bạn")
                    .relatedId(saved.getPostId())
                    .build();
            
            notificationService.createNotification(notificationDto);
        }

        return postResponseMapper.toResponse(saved, user);
    }

    @Transactional(readOnly = true)
    public List<PostMediaResponse> getMediaByGroupIdAndType(UUID groupId, MediaTypeEnum mediaType) {
        List<ContentMedia> mediaList = postMediaRepository.findAllMediaByGroupIdAndType(groupId, mediaType);
        
        return mediaList.stream()
                .map(media -> PostMediaResponse.builder()
                        .mediaId(media.getMediaId())
                        .postId(media.getPost().getPostId())
                        .url(media.getUrl())
                        .type(media.getType())
                        .build())
                .toList();
    }
}
