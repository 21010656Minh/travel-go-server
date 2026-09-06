package api.v2.travel_social_network_server.services.discovery;

import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.repositories.PostRepository;
import api.v2.travel_social_network_server.responses.discovery.DiscoveryFeaturedResponse;
import api.v2.travel_social_network_server.responses.discovery.DiscoverySpotResponse;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DiscoveryService implements IDiscoveryService {

    /** Hard upper bound on how many candidate posts we scan to aggregate. */
    private static final int POST_CANDIDATE_LIMIT = 200;

    /** Final size of `miniSpots` for the Bento grid (4 small cards). */
    private static final int MINI_SPOTS_LIMIT = 4;

    /** Weights for the trending score used to rank destinations. */
    private static final int W_LIKE = 1;
    private static final int W_COMMENT = 2;
    private static final int W_SHARE = 3;

    private final PostRepository postRepository;

    @Override
    @Transactional(readOnly = true)
    public DiscoveryFeaturedResponse getFeaturedDestinations(User currentUser) {
        // currentUser is reserved for future per-user personalization (e.g.
        // boosting destinations that match the user's recent searches). For
        // now the response is global, so the parameter is intentionally
        // unused.
        Pageable pageable = PageRequest.of(0, POST_CANDIDATE_LIMIT,
                // Ordering is fully expressed by the @Query's ORDER BY
                // clause; do NOT add a Sort to the Pageable or Spring Data will
                // append it again and produce a duplicate ORDER BY.
                Sort.unsorted());

        List<Post> posts = postRepository.findTopPublicPostsForDiscovery(PrivacyTypeEnum.PUBLIC, pageable);
        if (posts == null || posts.isEmpty()) {
            return DiscoveryFeaturedResponse.builder()
                    .featured(null)
                    .miniSpots(List.of())
                    .build();
        }

        // Aggregate posts by their normalized location key. Preserve insertion
        // order so the rank by trending score is deterministic: the first post
        // we encounter for a given key wins as the "representative" post.
        Map<String, AggregatedSpot> spots = new LinkedHashMap<>();
        for (Post post : posts) {
            String rawLocation = post.getLocation();
            if (rawLocation == null || rawLocation.isBlank()) {
                continue;
            }
            String key = normalizeLocation(rawLocation);
            if (key == null) {
                continue;
            }
            AggregatedSpot existing = spots.get(key);
            if (existing != null) {
                existing.totalLikeCount += nullableInt(post.getLikeCount());
                existing.totalCommentCount += nullableInt(post.getCommentCount());
                existing.totalShareCount += nullableInt(post.getShareCount());
                existing.postCount += 1;
                // Keep the first (highest-ranked) post as the cover source.
                continue;
            }
            spots.put(key, new AggregatedSpot(rawLocation, post));
        }

        if (spots.isEmpty()) {
            return DiscoveryFeaturedResponse.builder()
                    .featured(null)
                    .miniSpots(List.of())
                    .build();
        }

        List<AggregatedSpot> ranked = new ArrayList<>(spots.values());
        Comparator<AggregatedSpot> byScoreDesc =
                Comparator.comparingInt((AggregatedSpot s) -> score(s)).reversed();
        Comparator<AggregatedSpot> byLikesDesc =
                Comparator.comparingInt((AggregatedSpot s) -> nullableInt(s.representative.getLikeCount())).reversed();
        Comparator<AggregatedSpot> byPostCountDesc =
                Comparator.comparingInt((AggregatedSpot s) -> s.postCount).reversed();
        Comparator<AggregatedSpot> byCreatedAtDesc =
                Comparator.comparing(
                        (AggregatedSpot s) -> s.representative.getCreatedAt(),
                        Comparator.nullsLast(Instant::compareTo).reversed());
        Comparator<AggregatedSpot> chained = byScoreDesc
                .thenComparing(byLikesDesc)
                .thenComparing(byPostCountDesc)
                .thenComparing(byCreatedAtDesc);
        ranked.sort(chained);

        AggregatedSpot featured = ranked.get(0);
        DiscoverySpotResponse featuredSpot = toSpotResponse(featured, /*featuredCard=*/ true);

        List<DiscoverySpotResponse> minis = new ArrayList<>();
        for (int i = 1; i < ranked.size() && minis.size() < MINI_SPOTS_LIMIT; i++) {
            minis.add(toSpotResponse(ranked.get(i), /*featuredCard=*/ false));
        }

        return DiscoveryFeaturedResponse.builder()
                .featured(featuredSpot)
                .miniSpots(minis)
                .build();
    }

    // ----- Helpers -----

    private DiscoverySpotResponse toSpotResponse(AggregatedSpot spot, boolean featuredCard) {
        String coverUrl = resolveCoverUrl(spot.representative);
        Integer totalLikes = spot.totalLikeCount;

        String title;
        String subtitle;
        String tag;
        if (featuredCard) {
            // For the single featured card we send the bare location plus a
            // small "Trending" tag so the UI can render the star badge. The
            // client decides how to compose a richer caption from the
            // destination's representative post.
            title = prettifyLocation(spot.location);
            subtitle = buildFeaturedSubtitle(spot);
            tag = "Trending";
        } else {
            title = prettifyLocation(spot.location);
            subtitle = buildMiniSubtitle(spot);
            tag = null;
        }

        return DiscoverySpotResponse.builder()
                .id(spot.locationKey)
                .title(title)
                .subtitle(subtitle)
                .coverUrl(coverUrl)
                .tag(tag)
                .location(spot.location)
                .postCount(spot.postCount)
                .totalLikes(totalLikes)
                .build();
    }

    private static String resolveCoverUrl(Post post) {
        if (post.getMediaList() != null) {
            for (var m : post.getMediaList()) {
                if (m != null && m.getUrl() != null && !m.getUrl().isBlank()) {
                    return m.getUrl();
                }
            }
        }
        return null;
    }

    /**
     * Slug-friendly key: lowercase, diacritic-stripped, only [a-z0-9]+.
     * Two locations that differ only by accents/whitespace/commas map to the
     * same key so we don't double-count the same destination.
     *
     * <p>The strategy is:
     * <ol>
     *   <li>Strip diacritics and lowercase.</li>
     *   <li>Keep everything <em>before</em> the first comma. This collapses
     *       "Sa Pa, Lào Cai" and "Sa Pa, Lai Châu" into the same key but
     *       keeps distinct multi-word destinations like "Mù Cang Chải" or
     *       "TP. Hồ Chí Minh" intact.</li>
     *   <li>Replace remaining non-alphanumeric characters with nothing.</li>
     * </ol>
     */
    private static String normalizeLocation(String raw) {
        if (raw == null) return null;
        String trimmed = raw.trim().toLowerCase(Locale.ROOT);
        if (trimmed.isEmpty()) return null;

        // Strip diacritics: "đà lạt" -> "da lat", "hội an" -> "hoi an"
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.replace('đ', 'd').replace('Đ', 'd');

        // Drop everything after the first comma — including the comma itself.
        // "Sa Pa, Lào Cai" -> "Sa Pa"; "TP. Hồ Chí Minh" stays put (no comma).
        int commaIdx = normalized.indexOf(',');
        if (commaIdx >= 0) {
            normalized = normalized.substring(0, commaIdx);
        }

        // Collapse internal whitespace, trim, then strip non-alphanumerics.
        normalized = normalized.replaceAll("\\s+", " ").trim();
        normalized = normalized.replaceAll("[^a-z0-9 ]", "");
        normalized = normalized.replaceAll("\\s+", "");
        return normalized.isEmpty() ? null : normalized;
    }

    private static String prettifyLocation(String raw) {
        if (raw == null) return "";
        // Use only the first segment of the original location for the title
        // ("Sa Pa, Lào Cai" -> "Sa Pa"). For known multi-word cities we don't
        // shorten the comma-less form ("TP. Hồ Chí Minh" stays intact, minus
        // the trailing period where it makes the title cleaner).
        int idx = raw.indexOf(',');
        String trimmed = (idx >= 0 ? raw.substring(0, idx) : raw).trim();
        if (trimmed.isEmpty()) return raw.trim();
        // Capitalize the FIRST character only — lowercase the rest of the
        // title is intentionally NOT done here so diacritics stay intact.
        return Character.toTitleCase(trimmed.charAt(0)) + trimmed.substring(1);
    }

    private static String buildFeaturedSubtitle(AggregatedSpot spot) {
        int totalLikes = Objects.requireNonNullElse(spot.totalLikeCount, 0);
        int postCount = Math.max(spot.postCount, 1);
        if (spot.postCount > 1) {
            return String.format(Locale.ROOT, "%d bài viết · %d lượt thích",
                    postCount, totalLikes);
        }
        return String.format(Locale.ROOT, "%d lượt thích", totalLikes);
    }

    private static String buildMiniSubtitle(AggregatedSpot spot) {
        return String.format(Locale.ROOT, "%d bài viết",
                Math.max(spot.postCount, 1));
    }

    private static int score(AggregatedSpot spot) {
        return W_LIKE * nullableInt(spot.totalLikeCount)
                + W_COMMENT * nullableInt(spot.totalCommentCount)
                + W_SHARE * nullableInt(spot.totalShareCount);
    }

    private static int nullableInt(Integer v) {
        return v == null ? 0 : v;
    }

    // ----- Aggregation record -----

    private static final class AggregatedSpot {
        final String locationKey;
        final String location;
        final Post representative;
        int totalLikeCount;
        int totalCommentCount;
        int totalShareCount;
        int postCount;

        AggregatedSpot(String location, Post representative) {
            this.locationKey = normalizeLocation(location);
            this.location = location;
            this.representative = representative;
            this.totalLikeCount = nullableInt(representative.getLikeCount());
            this.totalCommentCount = nullableInt(representative.getCommentCount());
            this.totalShareCount = nullableInt(representative.getShareCount());
            this.postCount = 1;
        }
    }
}
