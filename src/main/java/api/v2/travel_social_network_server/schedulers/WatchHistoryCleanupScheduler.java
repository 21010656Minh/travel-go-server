package api.v2.travel_social_network_server.schedulers;

import api.v2.travel_social_network_server.repositories.WatchHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class WatchHistoryCleanupScheduler {

    private final WatchHistoryRepository watchHistoryRepository;

    /**
     * Scheduled task to clean up watch history older than 3 days
     * Runs every 7 days at 2:00 AM
     * Cron: second, minute, hour, day of month, month, day of week
     */
    @Scheduled(cron = "0 0 2 */7 * *") // Every 7 days at 2:00 AM
    @Transactional
    public void cleanupOldWatchHistory() {        try {
            // Calculate cutoff date (3 days ago)
            Instant cutoffDate = Instant.now().minus(3, ChronoUnit.DAYS);            // Delete old watch histories
            watchHistoryRepository.deleteOlderThan(cutoffDate);        } catch (Exception e) {
            log.error("Error during watch history cleanup: {}", e.getMessage(), e);
        }
    }
}
