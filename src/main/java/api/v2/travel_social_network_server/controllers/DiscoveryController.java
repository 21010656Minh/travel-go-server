package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.discovery.DiscoveryFeaturedResponse;
import api.v2.travel_social_network_server.services.discovery.IDiscoveryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Aggregated, read-only feeds for the Home page "Điểm đến nổi bật" grid.
 *
 * <p>The endpoint ranks public posts by engagement, groups them by
 * destination and returns one featured spot plus up to 4 secondary spots.
 * Authenticated users still receive the same payload; the current user is
 * passed to the service for future personalization but is not required right
 * now.
 */
@RestController
@RequestMapping("${api.base-url}/discovery")
@Tag(name = "Discovery APIs", description = "Aggregated discovery feeds for the Home page")
@RequiredArgsConstructor
public class DiscoveryController {

    private final IDiscoveryService discoveryService;

    @Operation(
            summary = "Get featured destinations",
            description = "Returns one featured destination plus up to 4 mini spots, ranked by " +
                    "likes + comments + shares across public posts on the platform."
    )
    @GetMapping("/featured")
    public ResponseEntity<Response<DiscoveryFeaturedResponse>> getFeaturedDestinations(
            @AuthenticationPrincipal User user,
            HttpServletRequest request) {

        DiscoveryFeaturedResponse payload = discoveryService.getFeaturedDestinations(user);

        return ResponseEntity.ok(
                Response.success(payload, request.getRequestURI(),
                        "Featured destinations retrieved successfully")
        );
    }
}
