package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.watch.WatchResponse;
import api.v2.travel_social_network_server.responses.watch.WatchWithIdsResponse;
import api.v2.travel_social_network_server.services.watch.WatchHistoryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/watches/history")
@RequiredArgsConstructor
public class WatchHistoryController {

    private final WatchHistoryService watchHistoryService;

    @PostMapping("/{watchId}")
    public ResponseEntity<Response<Void>> addToHistory(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        watchHistoryService.addToHistory(watchId, user);

        Response<Void> response = Response.<Void>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch history added successfully")
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Response<PageableResponse<WatchWithIdsResponse>>> getWatchHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageableResponse<WatchWithIdsResponse> watchHistory = watchHistoryService.getWatchHistory(user, pageable);

        Response<PageableResponse<WatchWithIdsResponse>> response = Response.<PageableResponse<WatchWithIdsResponse>>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch history retrieved successfully")
                .data(watchHistory)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{watchHistoryId}")
    public ResponseEntity<Response<Void>> removeFromHistory(
            @PathVariable UUID watchHistoryId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        watchHistoryService.removeFromHistory(watchHistoryId, user);

        Response<Void> response = Response.<Void>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch history removed successfully")
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
