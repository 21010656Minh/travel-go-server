package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.media.MediaUploadResponse;
import api.v2.travel_social_network_server.services.media.IMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/media")
@Tag(name = "Media APIs", description = "Endpoints for managing media uploads")
@RequiredArgsConstructor
public class MediaController {
    
    private final IMediaService mediaService;
    
    @Operation(summary = "Upload single media file", description = "Upload a single image for blog content")
    @PostMapping("/upload")
    public ResponseEntity<Response<MediaUploadResponse>> uploadMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", defaultValue = "blog") String type,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        MediaUploadResponse response = mediaService.uploadMedia(file, type, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(response, request.getRequestURI(), "Media uploaded successfully."));
    }
    
    @Operation(summary = "Upload chat media file", description = "Upload a single image for chat messages with conversation tracking")
    @PostMapping("/upload/chat")
    public ResponseEntity<Response<MediaUploadResponse>> uploadChatMedia(
            @RequestParam("file") MultipartFile file,
            @RequestParam("conversationId") UUID conversationId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        MediaUploadResponse response = mediaService.uploadChatMedia(file, conversationId, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(response, request.getRequestURI(), "Chat media uploaded successfully."));
    }
    
    @Operation(summary = "Upload multiple media files", description = "Upload multiple images at once")
    @PostMapping("/upload/batch")
    public ResponseEntity<Response<List<MediaUploadResponse>>> uploadMultipleMedia(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "type", defaultValue = "blog") String type,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        List<MediaUploadResponse> responses = mediaService.uploadMultipleMedia(files, type, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(responses, request.getRequestURI(), "Media uploaded successfully."));
    }
    
    @Operation(summary = "Delete media", description = "Delete a media file by ID")
    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Response<Void>> deleteMedia(
            @PathVariable UUID mediaId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        mediaService.deleteMedia(mediaId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Media deleted successfully."));
    }
    
    @Operation(summary = "Link media to blog", description = "Associate uploaded media with a blog post")
    @PostMapping("/link")
    public ResponseEntity<Response<Void>> linkMediaToBlog(
            @RequestParam("mediaIds") List<UUID> mediaIds,
            @RequestParam("blogId") UUID blogId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        mediaService.linkMediaToBlog(mediaIds, blogId, user);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Media linked successfully."));
    }
}
