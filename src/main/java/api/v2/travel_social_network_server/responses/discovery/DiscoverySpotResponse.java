package api.v2.travel_social_network_server.responses.discovery;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscoverySpotResponse implements Serializable {
    /**
     * Unique slug-like key for client navigation (lowercased, diacritic-stripped
     * location prefix). Used as React `id` and as filter param when the user
     * clicks the card.
     */
    private String id;

    /**
     * Human-readable title, derived from the canonical location
     * (e.g. "Sa Pa", "Hội An"). For the featured card this may be enriched
     * with a short caption (e.g. "Sa Pa cuối thu — Ruộng bậc thang vàng").
     */
    private String title;

    /**
     * Optional short caption shown on the small Bento cards.
     */
    private String subtitle;

    /**
     * Cover image URL. Pulled from the first media of the top-ranked post for
     * this destination; falls back to the post's own thumbnail if available.
     */
    private String coverUrl;

    /**
     * Optional badge tag shown on the featured card (e.g. "Trending", "Mùa này").
     */
    private String tag;

    /**
     * Raw, un-normalized location string as stored on the post (e.g.
     * "Sa Pa, Lào Cai"). May contain additional context like province.
     */
    private String location;

    /**
     * Number of public posts that share this normalized location. Useful for
     * client-side ranking and "X bài viết" hints.
     */
    private Integer postCount;

    /**
     * Sum of like counts across all posts for this location, used for ranking
     * and to decide which destination becomes "featured".
     */
    private Integer totalLikes;
}
