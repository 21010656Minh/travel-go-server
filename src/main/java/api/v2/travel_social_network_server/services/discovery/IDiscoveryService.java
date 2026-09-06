package api.v2.travel_social_network_server.services.discovery;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.discovery.DiscoveryFeaturedResponse;

public interface IDiscoveryService {
    /**
     * Aggregates the top public posts on the platform, groups them by their
     * normalized location, ranks the resulting destinations by a trending
     * score (likes + comments + shares, weighted), and returns one featured
     * destination plus up to {@code miniSpotLimit} secondary spots for the
     * Home page Bento grid.
     *
     * @param currentUser the authenticated user (may be {@code null} when the
     *                    endpoint is hit anonymously). Currently only used for
     *                    logging and to scope to public posts.
     * @return the response payload for the Home page.
     */
    DiscoveryFeaturedResponse getFeaturedDestinations(User currentUser);
}
