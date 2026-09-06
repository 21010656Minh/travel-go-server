package api.v2.travel_social_network_server.services.suggestion.newsfeed.decorator;

import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.services.suggestion.newsfeed.INewsFeedService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service("newsFeedServiceDecorator")
@Slf4j
public class NewsFeedServiceDecorator implements INewsFeedService {

    private final INewsFeedService newsFeedService;

    public NewsFeedServiceDecorator(@Qualifier("newsFeedService") INewsFeedService newsFeedService) {
        this.newsFeedService = newsFeedService;
    }

    @Override
    public PageableResponse<PostResponse> getNewsFeed(User user, int page, int pageSize) {
        log.debug("Decorator: getNewsFeed called for user {} (page {}, size {})", 
                user.getUserId(), page, pageSize);
        
        // Validate input
        if (page < 0) {
            log.warn("Invalid page number {} for user {}, using 0", page, user.getUserId());
            page = 0;
        }
        
        if (pageSize <= 0 || pageSize > 100) {
            log.warn("Invalid page size {} for user {}, using 20", pageSize, user.getUserId());
            pageSize = 20;
        }
        
        // Pass through to base service
        return newsFeedService.getNewsFeed(user, page, pageSize);
    }

    @Override
    public List<UUID> computeNewsFeedPostIds(User user, String searchKeywords) {
        log.debug("Decorator: computeNewsFeedPostIds called for user {}", user.getUserId());
        
        // Pass through to base service
        return newsFeedService.computeNewsFeedPostIds(user, searchKeywords);
    }
}
