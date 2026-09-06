package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.TripSchedule;
import api.v2.travel_social_network_server.utilities.enums.ActivityTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripScheduleRepository extends JpaRepository<TripSchedule, UUID> {

    // Tìm schedule theo id
    Optional<TripSchedule> findByTripScheduleId(@Param("tripScheduleId") UUID tripScheduleId);

    // Lấy tất cả schedule của một trip
    @Query("""
           SELECT ts FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
           ORDER BY ts.scheduleDate ASC, ts.orderIndex ASC
           """)
    List<TripSchedule> findByTripId(@Param("tripId") UUID tripId);

    // Lấy schedule theo ngày
    @Query("""
           SELECT ts FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
             AND DATE(ts.scheduleDate) = DATE(:scheduleDate)
           ORDER BY ts.orderIndex ASC
           """)
    List<TripSchedule> findByTripIdAndScheduleDate(
            @Param("tripId") UUID tripId,
            @Param("scheduleDate") Instant scheduleDate
    );

    // Lấy schedule theo loại hoạt động
    @Query("""
           SELECT ts FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
             AND ts.activityType = :activityType
           ORDER BY ts.scheduleDate ASC
           """)
    List<TripSchedule> findByTripIdAndActivityType(
            @Param("tripId") UUID tripId,
            @Param("activityType") ActivityTypeEnum activityType
    );

    // Lấy schedule trong khoảng thời gian
    @Query("""
           SELECT ts FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
             AND ts.scheduleDate BETWEEN :startDate AND :endDate
           ORDER BY ts.scheduleDate ASC, ts.orderIndex ASC
           """)
    List<TripSchedule> findByTripIdAndDateRange(
            @Param("tripId") UUID tripId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );
    
    // Overload for calendar - get schedules by trip and date range
    @Query("""
           SELECT ts FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
             AND ts.scheduleDate BETWEEN :startDate AND :endDate
           ORDER BY ts.scheduleDate ASC, ts.orderIndex ASC
           """)
    List<TripSchedule> findByTripIdAndScheduleDateBetween(
            @Param("tripId") UUID tripId,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate
    );

    // Đếm số lượng schedule của trip
    @Query("""
           SELECT COUNT(ts) FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
           """)
    Long countByTripId(@Param("tripId") UUID tripId);

    // Tìm kiếm schedule theo keyword
    @Query("""
           SELECT ts FROM TripSchedule ts
           WHERE ts.trip.tripId = :tripId
             AND (LOWER(ts.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(ts.location) LIKE LOWER(CONCAT('%', :keyword, '%')))
           ORDER BY ts.scheduleDate ASC
           """)
    Page<TripSchedule> searchSchedulesByKeyword(
            @Param("tripId") UUID tripId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Xóa tất cả schedule của trip
    void deleteByTripTripId(@Param("tripId") UUID tripId);
}
