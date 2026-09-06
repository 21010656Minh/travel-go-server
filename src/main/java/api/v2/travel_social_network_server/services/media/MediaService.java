package api.v2.travel_social_network_server.services.media;

import api.v2.travel_social_network_server.entities.Blog;
import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.repositories.BlogRepository;
import api.v2.travel_social_network_server.repositories.MediaRepository;
import api.v2.travel_social_network_server.responses.media.MediaUploadResponse;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaService implements IMediaService {
    
    private final MediaRepository mediaRepository;
    private final BlogRepository blogRepository;
    private final MediaStorage mediaStorage;
    private final String folderName = "blogs";

    @Override
    @Transactional
    public MediaUploadResponse uploadMedia(MultipartFile file, String type, User user) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds 5MB limit");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            
            // Determine media type
            MediaTypeEnum mediaType = determineMediaType(contentType);
            
            // Upload to storage (MinIO) - pass actual contentType, not hardcoded "image"
            String url = mediaStorage.uploadFile(file.getBytes(), folderName, contentType);
            

            
            // For blog/post content, save media record (without blog reference - will be linked later)
            ContentMedia media = ContentMedia.builder()
                    .url(url)
                    .type(mediaType)
                    .build();
            
            media = mediaRepository.save(media);
            
            log.info("Media uploaded successfully: {}", media.getMediaId());
            
            return MediaUploadResponse.builder()
                    .mediaId(media.getMediaId())
                    .url(media.getUrl())
                    .type(media.getType())
                    .size(file.getSize())
                    .uploadedAt(media.getCreatedAt())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error uploading media: {}", e.getMessage());
            throw new RuntimeException("Failed to upload media: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public MediaUploadResponse uploadChatMedia(MultipartFile file, UUID conversationId, User user) {
        try {
            // Validate file
            if (file.isEmpty()) {
                throw new IllegalArgumentException("File is empty");
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("File size exceeds 5MB limit");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Only image files are allowed");
            }
            
            // Validate conversationId
            if (conversationId == null) {
                throw new IllegalArgumentException("Conversation ID is required for chat media");
            }
            
            // Determine media type
            MediaTypeEnum mediaType = determineMediaType(contentType);
            
            // Upload to storage (MinIO)
            String url = mediaStorage.uploadFile(file.getBytes(), folderName, contentType);
            
            // Save media record with conversation reference
            Conversation conversation = new Conversation();
            conversation.setConversationId(conversationId);
            
            ContentMedia media = ContentMedia.builder()
                    .url(url)
                    .type(mediaType)
                    .conversation(conversation)
                    .build();
            
            media = mediaRepository.save(media);            return MediaUploadResponse.builder()
                    .mediaId(media.getMediaId())
                    .url(media.getUrl())
                    .type(media.getType())
                    .size(file.getSize())
                    .uploadedAt(media.getCreatedAt())
                    .build();
                    
        } catch (Exception e) {
            log.error("Error uploading chat media: {}", e.getMessage());
            throw new RuntimeException("Failed to upload chat media: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public List<MediaUploadResponse> uploadMultipleMedia(List<MultipartFile> files, String type, User user) {
        List<MediaUploadResponse> responses = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                MediaUploadResponse response = uploadMedia(file, type, user);
                responses.add(response);
            } catch (Exception e) {
                log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
                // Continue with other files
            }
        }
        
        return responses;
    }
    
    @Override
    @Transactional
    public void deleteMedia(UUID mediaId, User user) {
        ContentMedia media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
        
        // Check if media belongs to user's blog/post
        if (media.getBlog() != null && !media.getBlog().getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("You don't have permission to delete this media");
        }
        
        // TODO: Delete from Cloudinary storage
        // cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        
        mediaRepository.delete(media);    }
    
    @Override
    @Transactional
    public void linkMediaToBlog(List<UUID> mediaIds, UUID blogId, User user) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("Blog not found"));
        
        // Check ownership
        if (!blog.getUser().getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("You don't have permission to modify this blog");
        }
        
        // Link media to blog
        for (UUID mediaId : mediaIds) {
            ContentMedia media = mediaRepository.findById(mediaId)
                    .orElseThrow(() -> new IllegalArgumentException("Media not found: " + mediaId));
            
            media.setBlog(blog);
            mediaRepository.save(media);
        }
        
        log.info("Linked {} media to blog {}", mediaIds.size(), blogId);
    }
    
    @Override
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
    public void cleanupOrphanedMedia() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(24);
        List<ContentMedia> orphanedMedia = mediaRepository.findOrphanedMedia(cutoffTime);
        
        log.info("Found {} orphaned media files to clean up", orphanedMedia.size());
        
        for (ContentMedia media : orphanedMedia) {
            try {
                // TODO: Delete from Cloudinary storage
                // Extract public ID from URL and delete
                
                mediaRepository.delete(media);
                log.info("Cleaned up orphaned media: {}", media.getMediaId());
            } catch (Exception e) {
                log.error("Error cleaning up media {}: {}", media.getMediaId(), e.getMessage());
            }
        }
        
        log.info("Cleanup completed. Removed {} orphaned media files", orphanedMedia.size());
    }
    
    private MediaTypeEnum determineMediaType(String contentType) {
        if (contentType.startsWith("image/")) {
            return MediaTypeEnum.IMAGE;
        } else if (contentType.startsWith("video/")) {
            return MediaTypeEnum.VIDEO;
        }
        return MediaTypeEnum.IMAGE; // Default
    }
}
