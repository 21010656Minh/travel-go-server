package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.tag.TagResponse;
import api.v2.travel_social_network_server.services.tag.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.base-url}/tags")
@Tag(name = "Tag APIs", description = "Endpoints for tag search and suggestions")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    @Operation(summary = "Search tags", description = "Search tags by title or get recent tags if query is empty")
    @GetMapping("/search")
    public ResponseEntity<Response<List<TagResponse>>> searchTags(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request
    ) {
        List<TagResponse> tags = tagService.searchTags(query, limit);
        return ResponseEntity.ok(Response.success(tags, request.getRequestURI(), "Tags retrieved successfully"));
    }
}
