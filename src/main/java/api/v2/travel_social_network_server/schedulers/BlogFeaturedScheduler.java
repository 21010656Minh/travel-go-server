package api.v2.travel_social_network_server.schedulers;

import api.v2.travel_social_network_server.entities.Blog;
import api.v2.travel_social_network_server.repositories.BlogRepository;
import api.v2.travel_social_network_server.utilities.enums.BlogStatusEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlogFeaturedScheduler {

    private final BlogRepository blogRepository;

    /**
     * Scheduled task to update featured blogs based on views and ratings
     * Runs every week on Sunday at 3:00 AM
     * Cron: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 3 * * SUN") // Every Sunday at 3:00 AM
    @Transactional
    public void updateFeaturedBlogs() {        try {
            // Step 1: Remove featured flag from all current featured blogs
            List<Blog> currentFeaturedBlogs = blogRepository.findAllByIsFeaturedTrue();
            int removedCount = 0;
            for (Blog blog : currentFeaturedBlogs) {
                blog.setIsFeatured(false);
                removedCount++;
            }
            blogRepository.saveAll(currentFeaturedBlogs);            // Step 2: Get top 10 popular blogs (by rating, total ratings, and views)
            Pageable topBlogs = PageRequest.of(0, 10);
            List<Blog> popularBlogs = blogRepository.findPopularBlogs(BlogStatusEnum.PUBLISHED, topBlogs).getContent();
            
            // Step 3: Set featured flag for top popular blogs
            int featuredCount = 0;
            for (Blog blog : popularBlogs) {
                blog.setIsFeatured(true);
                featuredCount++;
            }
            blogRepository.saveAll(popularBlogs);        } catch (Exception e) {
            log.error("Error during featured blogs update: {}", e.getMessage(), e);
        }
    }
}
