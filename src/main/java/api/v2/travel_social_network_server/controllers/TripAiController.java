package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dto.ai.AiSuggestResponseDto;
import api.v2.travel_social_network_server.dto.ai.ApplyAiSuggestionsDto;
import api.v2.travel_social_network_server.entities.TripSchedule;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.enums.AiRequestType;
import api.v2.travel_social_network_server.services.ai.TripAiService;
import api.v2.travel_social_network_server.responses.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/ai/trip")
@RequiredArgsConstructor
@Tag(name = "AI Trip Suggestions", description = "AI-powered trip itinerary suggestions")
public class TripAiController {

    private final TripAiService tripAiService;

    @Operation(summary = "Generate AI trip suggestions", description = "Generate detailed trip itinerary using Google Gemini AI based on trip context and user prompt")
    @PostMapping("/{tripId}/suggest")
    public ResponseEntity<Response<AiSuggestResponseDto>> generateSuggestions(
            @PathVariable UUID tripId,
            @RequestParam String prompt,
            @RequestParam(required = false) String type,
            HttpServletRequest request
    ) {
        AiRequestType requestType = AiRequestType.fromValue(type);
        AiSuggestResponseDto suggestions = tripAiService.generateSuggestions(tripId, prompt, requestType);
        return ResponseEntity.ok(Response.success(
                suggestions,
                request.getRequestURI(),
                "AI suggestions generated successfully"
        ));
    }

    @Operation(summary = "Apply AI suggestions", description = "Create trip schedules from AI-generated suggestions")
    @PostMapping("/{tripId}/apply")
    public ResponseEntity<Response<List<TripSchedule>>> applyAiSuggestions(
            @PathVariable UUID tripId,
            @RequestBody ApplyAiSuggestionsDto dto,
            @AuthenticationPrincipal User currentUser,
            HttpServletRequest request
    ) {
        List<TripSchedule> schedules = tripAiService.applyAiSuggestions(tripId, dto, currentUser);
        return ResponseEntity.ok(Response.success(
                schedules,
                request.getRequestURI(),
                "AI suggestions applied successfully. Created " + schedules.size() + " schedules."
        ));
    }
}
