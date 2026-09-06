package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Friendship;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.FriendShipTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    // 1. Kiểm tra đã tồn tại lời mời kết bạn chưa
    @Query("SELECT f FROM Friendship f " +
            "WHERE (f.requester.userId = :userId AND f.receiver.userId = :otherId) " +
            "   OR (f.requester.userId = :otherId AND f.receiver.userId = :userId)")
    Optional<Friendship> findFriendshipBetween(@Param("userId") UUID userId, @Param("otherId") UUID otherId);

    // 2. Lấy tất cả lời mời đến user (chưa xử lý)
    Page<Friendship> findAllByReceiverAndStatus(User receiver, FriendShipTypeEnum status, Pageable pageable);

    // 3. Lấy tất cả lời mời mà user đã gửi
    Page<Friendship> findAllByRequesterAndStatus(User requester, FriendShipTypeEnum status, Pageable pageable);

    // 4. Lấy danh sách bạn bè đã ACCEPTED (người này là requester)
    @Query("SELECT f.receiver FROM Friendship f WHERE f.requester.userId = :userId AND f.status = 'ACCEPTED'")
    List<User> findFriendsAsRequester(@Param("userId") UUID userId);

    // 5. Lấy danh sách bạn bè đã ACCEPTED (người này là receiver)
    @Query("SELECT f.requester FROM Friendship f WHERE f.receiver.userId = :userId AND f.status = 'ACCEPTED'")
    List<User> findFriendsAsReceiver(@Param("userId") UUID userId);

    // 6. Đếm số lượng bạn bè đã ACCEPTED
    @Query("SELECT COUNT(f) FROM Friendship f WHERE (f.requester.userId = :userId OR f.receiver.userId = :userId) AND f.status = 'ACCEPTED'")
    long countAcceptedFriends(@Param("userId") UUID userId);

    // 7. Huỷ kết bạn (tìm record bất kỳ trong 2 chiều)
    @Modifying
    @Query("DELETE FROM Friendship f WHERE (f.requester.userId = :userId AND f.receiver.userId = :friendId) " +
            "   OR (f.requester.userId = :friendId AND f.receiver.userId = :userId)")
    void deleteFriendship(@Param("userId") UUID userId, @Param("friendId") UUID friendId);

    @Query("""
        SELECT u FROM User u
        WHERE u.userId <> :userId
          AND u.userId NOT IN (
              SELECT CASE
                       WHEN f.requester.userId = :userId THEN f.receiver.userId
                       WHEN f.receiver.userId= :userId THEN f.requester.userId
                     END
              FROM Friendship f
              WHERE f.status = 'ACCEPTED' OR f.status = 'PENDING'
          )
    """)
    Page<User> findSuggestedUsers(@Param("userId") UUID userId, Pageable pageable);

    // 8. Lấy gợi ý bạn bè dựa trên bạn bè của bạn bè
    @Query("""
        SELECT DISTINCT u FROM User u
        WHERE u.userId <> :userId
          AND u.userId NOT IN (
              SELECT CASE
                       WHEN f.requester.userId = :userId THEN f.receiver.userId
                       WHEN f.receiver.userId = :userId THEN f.requester.userId
                     END
              FROM Friendship f
              WHERE (f.requester.userId = :userId OR f.receiver.userId = :userId)
          )
          AND u.userId IN (
              SELECT CASE
                       WHEN f2.requester.userId IN (
                           SELECT CASE
                                    WHEN f1.requester.userId = :userId THEN f1.receiver.userId
                                    WHEN f1.receiver.userId = :userId THEN f1.requester.userId
                                  END
                           FROM Friendship f1
                           WHERE (f1.requester.userId = :userId OR f1.receiver.userId = :userId)
                             AND f1.status = 'ACCEPTED'
                       ) THEN f2.receiver.userId
                       WHEN f2.receiver.userId IN (
                           SELECT CASE
                                    WHEN f1.requester.userId = :userId THEN f1.receiver.userId
                                    WHEN f1.receiver.userId = :userId THEN f1.requester.userId
                                  END
                           FROM Friendship f1
                           WHERE (f1.requester.userId = :userId OR f1.receiver.userId = :userId)
                             AND f1.status = 'ACCEPTED'
                       ) THEN f2.requester.userId
                     END
              FROM Friendship f2
              WHERE f2.status = 'ACCEPTED'
          )
    """)
    Page<User> findFriendsOfFriends(@Param("userId") UUID userId, Pageable pageable);

    default List<User> findActiveFriendsByKeyword(UUID userId, String keyword) {
        String lowerKeyword = keyword.toLowerCase();

        List<User> requesterFriends = findFriendsAsRequester(userId).stream()
                .filter(u -> u.getUserProfile() != null && u.getUserProfile().getFullName().toLowerCase().contains(lowerKeyword))
                .toList();

        List<User> receiverFriends = findFriendsAsReceiver(userId).stream()
                .filter(u -> u.getUserProfile() != null && u.getUserProfile().getFullName().toLowerCase().contains(lowerKeyword))
                .toList();

        // Gộp 2 list và loại trùng nếu cần
        return Stream.concat(requesterFriends.stream(), receiverFriends.stream())
                .distinct()
                .toList();
    }
}

