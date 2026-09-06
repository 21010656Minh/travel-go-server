package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.watch.WatchCreateDto;
import api.v2.travel_social_network_server.dtos.watch.WatchUpdateInfoDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.watch.WatchResponse;
import api.v2.travel_social_network_server.responses.watch.WatchStatisticsResponse;
import api.v2.travel_social_network_server.services.watch.WatchService;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/watches")
@RequiredArgsConstructor
public class WatchController {

    private final WatchService watchService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<WatchResponse>> createWatch(
            @RequestParam("video") MultipartFile video,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "duration", required = false) Integer duration,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam("privacy") String privacy,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tagsJson,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) throws Exception {
        // Parse tags from JSON string
        List<String> tags = null;
        if (tagsJson != null && !tagsJson.isEmpty()) {
            tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        }

        // Parse privacy enum
        PrivacyTypeEnum privacyEnum = PrivacyTypeEnum.fromString(privacy);

        // Build DTO
        WatchCreateDto dto = WatchCreateDto.builder()
                .video(video)
                .title(title)
                .description(description)
                .thumbnail(thumbnail)
                .duration(duration)
                .location(location)
                .privacy(privacyEnum)
                .category(category)
                .tags(tags)
                .build();

        // Create watch
        WatchResponse watchResponse = watchService.createWatch(dto, user);

        Response<WatchResponse> response = Response.<WatchResponse>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch created successfully")
                .data(watchResponse)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<PageableResponse<WatchResponse>>> getAllPublicWatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("ASC") 
                ? Sort.Direction.ASC 
                : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        PageableResponse<WatchResponse> watches = watchService.getAllPublicWatches(pageable, user);

        Response<PageableResponse<WatchResponse>> response = Response.<PageableResponse<WatchResponse>>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watches retrieved successfully")
                .data(watches)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Response<PageableResponse<WatchResponse>>> getWatchesByUser(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageableResponse<WatchResponse> watches = watchService.getWatchesByUser(userId, pageable, user);

        Response<PageableResponse<WatchResponse>> response = Response.<PageableResponse<WatchResponse>>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("User watches retrieved successfully")
                .data(watches)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<Response<PageableResponse<WatchResponse>>> getMyWatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        PageableResponse<WatchResponse> watches = watchService.getWatchesByUser(user.getUserId(), pageable, user);

        Response<PageableResponse<WatchResponse>> response = Response.<PageableResponse<WatchResponse>>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("My watches retrieved successfully")
                .data(watches)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/statistics")
    public ResponseEntity<Response<WatchStatisticsResponse>> getMyWatchStatistics(
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        WatchStatisticsResponse statistics = watchService.getUserWatchStatistics(user.getUserId());

        Response<WatchStatisticsResponse> response = Response.<WatchStatisticsResponse>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Statistics retrieved successfully")
                .data(statistics)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending")
    public ResponseEntity<Response<PageableResponse<WatchResponse>>> getTrendingWatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "7") int days,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageableResponse<WatchResponse> watches = watchService.getTrendingWatches(pageable, days, user);

        Response<PageableResponse<WatchResponse>> response = Response.<PageableResponse<WatchResponse>>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Trending watches retrieved successfully")
                .data(watches)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{watchId}")
    public ResponseEntity<Response<WatchResponse>> getWatchById(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        WatchResponse watch = watchService.getWatchById(watchId, user);

        Response<WatchResponse> response = Response.<WatchResponse>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch retrieved successfully")
                .data(watch)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{watchId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<WatchResponse>> updateWatch(
            @PathVariable UUID watchId,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "privacy", required = false) String privacy,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "tags", required = false) String tagsJson,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) throws Exception {
        // Parse tags from JSON string
        List<String> tags = null;
        if (tagsJson != null && !tagsJson.isEmpty()) {
            tags = objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        }

        // Parse privacy enum
        PrivacyTypeEnum privacyEnum = null;
        if (privacy != null && !privacy.isEmpty()) {
            privacyEnum = PrivacyTypeEnum.fromString(privacy);
        }

        // Build DTO
        WatchUpdateInfoDto dto = WatchUpdateInfoDto.builder()
                .title(title)
                .description(description)
                .thumbnail(thumbnail)
                .location(location)
                .privacy(privacyEnum)
                .category(category)
                .tags(tags)
                .build();

        // Update watch
        WatchResponse watchResponse = watchService.updateWatch(watchId, dto, user);

        Response<WatchResponse> response = Response.<WatchResponse>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch updated successfully")
                .data(watchResponse)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{watchId}")
    public ResponseEntity<Response<Void>> deleteWatch(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        watchService.deleteWatch(watchId, user);

        Response<Void> response = Response.<Void>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch deleted successfully")
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
