package api.v2.travel_social_network_server.services.media;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.media.MediaUploadResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface IMediaService {
    
    /**
     * Upload a single media file
     * @param file MultipartFile to upload
     * @param type Type of media (blog, post, etc.)
     * @param user Authenticated user
     * @return MediaUploadResponse with media details
     */
    MediaUploadResponse uploadMedia(MultipartFile file, String type, User user);
    
    /**
     * Upload a chat media file with conversation tracking
     * @param file MultipartFile to upload
     * @param conversationId Conversation UUID to link media
     * @param user Authenticated user
     * @return MediaUploadResponse with media details
     */
    MediaUploadResponse uploadChatMedia(MultipartFile file, UUID conversationId, User user);
    
    /**
     * Upload multiple media files
     * @param files List of MultipartFile to upload
     * @param type Type of media
     * @param user Authenticated user
     * @return List of MediaUploadResponse
     */
    List<MediaUploadResponse> uploadMultipleMedia(List<MultipartFile> files, String type, User user);
    
    /**
     * Delete a media file
     * @param mediaId Media UUID
     * @param user Authenticated user
     */
    void deleteMedia(UUID mediaId, User user);
    
    /**
     * Link media to a blog post
     * @param mediaIds List of media UUIDs
     * @param blogId Blog UUID
     * @param user Authenticated user
     */
    void linkMediaToBlog(List<UUID> mediaIds, UUID blogId, User user);
    
    /**
     * Clean up orphaned media (scheduled task)
     * Removes media files that are not linked to any blog or post after 24 hours
     */
    void cleanupOrphanedMedia();
}
