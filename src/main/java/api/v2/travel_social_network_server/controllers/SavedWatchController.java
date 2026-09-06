package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.watch.WatchResponse;
import api.v2.travel_social_network_server.services.watch.SavedWatchService;
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
@RequestMapping("/api/v2/watches/saved")
@RequiredArgsConstructor
public class SavedWatchController {

    private final SavedWatchService savedWatchService;

    @PostMapping("/{watchId}")
    public ResponseEntity<Response<Void>> saveWatch(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        savedWatchService.saveWatch(watchId, user);

        Response<Void> response = Response.<Void>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch saved successfully")
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{watchId}")
    public ResponseEntity<Response<Void>> unsaveWatch(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        savedWatchService.unsaveWatch(watchId, user);

        Response<Void> response = Response.<Void>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Watch unsaved successfully")
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Response<PageableResponse<WatchResponse>>> getSavedWatches(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        Pageable pageable = PageRequest.of(page, size);
        PageableResponse<WatchResponse> savedWatches = savedWatchService.getSavedWatches(user, pageable);

        Response<PageableResponse<WatchResponse>> response = Response.<PageableResponse<WatchResponse>>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Saved watches retrieved successfully")
                .data(savedWatches)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{watchId}/check")
    public ResponseEntity<Response<Boolean>> checkWatchSaved(
            @PathVariable UUID watchId,
            @AuthenticationPrincipal User user,
            HttpServletRequest request
    ) {
        boolean isSaved = savedWatchService.isWatchSaved(watchId, user);

        Response<Boolean> response = Response.<Boolean>builder()
                .success(true)
                .path(request.getRequestURI())
                .message("Check completed")
                .data(isSaved)
                .timestamp(java.time.Instant.now())
                .build();

        return ResponseEntity.ok(response);
    }
}
