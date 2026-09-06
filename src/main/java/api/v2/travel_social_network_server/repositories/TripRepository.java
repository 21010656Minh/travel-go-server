package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Trip;
import api.v2.travel_social_network_server.utilities.enums.TripStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

    // Tìm trip theo tripId
    Optional<Trip> findByTripId(@Param("tripId") UUID tripId);

    // Lấy tất cả trip của một conversation
    @Query("""
           SELECT t FROM Trip t
           WHERE t.conversation.conversationId = :conversationId
           ORDER BY t.startDate DESC
           """)
    Page<Trip> findByConversationId(@Param("conversationId") UUID conversationId, Pageable pageable);

    // Lấy trip theo status
    @Query("""
           SELECT t FROM Trip t
           WHERE t.conversation.conversationId = :conversationId
             AND t.status = :status
           ORDER BY t.startDate DESC
           """)
    List<Trip> findByConversationIdAndStatus(
            @Param("conversationId") UUID conversationId,
            @Param("status") TripStatusEnum status
    );

    // Lấy tất cả trip mà user tham gia (thông qua conversation)
    @Query("""
           SELECT t FROM Trip t
           JOIN t.conversation.conversationMembers m
           WHERE m.user.userId = :userId
           ORDER BY t.startDate DESC
           """)
    Page<Trip> findTripsByUserId(@Param("userId") UUID userId, Pageable pageable);
    
    // Lấy tất cả trip mà user tham gia (không phân trang - cho calendar)
    @Query("""
           SELECT t FROM Trip t
           JOIN t.conversation.conversationMembers m
           WHERE m.user.userId = :userId
           ORDER BY t.startDate DESC
           """)
    List<Trip> findTripsByUserId(@Param("userId") UUID userId);

    // Tìm kiếm trip theo keyword
    @Query("""
           SELECT t FROM Trip t
           WHERE t.conversation.conversationId = :conversationId
             AND (LOWER(t.tripName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(t.destination) LIKE LOWER(CONCAT('%', :keyword, '%')))
           ORDER BY t.startDate DESC
           """)
    Page<Trip> searchTripsByKeyword(
            @Param("conversationId") UUID conversationId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Đếm số lượng trip của conversation
    @Query("""
           SELECT COUNT(t) FROM Trip t
           WHERE t.conversation.conversationId = :conversationId
           """)
    Long countByConversationId(@Param("conversationId") UUID conversationId);

    // Lấy trip được tạo bởi user
    @Query("""
           SELECT t FROM Trip t
           WHERE t.createdBy.userId = :userId
           ORDER BY t.createdAt DESC
           """)
    List<Trip> findByCreatedBy(@Param("userId") UUID userId);
}
