package api.v2.travel_social_network_server.services.tag;

import api.v2.travel_social_network_server.entities.Tag;
import api.v2.travel_social_network_server.repositories.TagRepository;
import api.v2.travel_social_network_server.responses.tag.TagResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TagService {
    
    private final TagRepository tagRepository;
    
    /**
     * Get or create tag (idempotent operation)
     */
    @Transactional
    public Tag getOrCreateTag(String title) {
        String normalizedTitle = normalizeTitle(title);
        String slug = generateSlug(normalizedTitle);
        
        return tagRepository.findBySlug(slug)
            .orElseGet(() -> {
                Tag tag = Tag.builder()
                    .title(normalizedTitle)
                    .slug(slug)
                    .build();
                Tag saved = tagRepository.save(tag);
                log.info("Created new tag: {} (slug: {})", normalizedTitle, slug);
                return saved;
            });
    }
    
    /**
     * Process list of tag titles and return Tag entities
     */
    @Transactional
    public List<Tag> processTagTitles(List<String> tagTitles) {
        if (tagTitles == null || tagTitles.isEmpty()) {
            return new ArrayList<>();
        }
        
        return tagTitles.stream()
            .filter(title -> title != null && !title.trim().isEmpty())
            .limit(3) // Maximum 3 tags
            .map(this::getOrCreateTag)
            .collect(Collectors.toList());
    }
    
    /**
     * Search tags by query
     */
    public List<TagResponse> searchTags(String query, int limit) {
        List<Tag> tags;
        
        if (query == null || query.trim().isEmpty()) {
            tags = tagRepository.findRecentTags(Math.min(limit, 20));
        } else {
            tags = tagRepository.searchByTitle(query, Math.min(limit, 20));
        }
        
        return tags.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * Convert Tag entity to TagResponse
     */
    private TagResponse toResponse(Tag tag) {
        return TagResponse.builder()
            .tagId(tag.getTagId())
            .title(tag.getTitle())
            .slug(tag.getSlug())
            .createdAt(tag.getCreatedAt())
            .build();
    }
    
    /**
     * Normalize tag title
     */
    private String normalizeTitle(String title) {
        String trimmed = title.trim().replaceAll("\\s+", " ");
        return trimmed.substring(0, Math.min(trimmed.length(), 50));
    }
    
    /**
     * Generate slug from title
     */
    private String generateSlug(String title) {
        String processed = title.toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-");
        
        return processed.substring(0, Math.min(processed.length(), 50));
    }
}
