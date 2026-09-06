package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.GroupMember;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.Group;
import api.v2.travel_social_network_server.utilities.enums.GroupMemberStatusTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    boolean existsByGroupGroupIdAndUserUserId(UUID groupId, UUID userId);

    Optional<GroupMember> findByGroupGroupIdAndUserUserId(UUID groupId, UUID userId);

    Page<GroupMember> findAllByGroupGroupId(UUID groupId, Pageable pageable);

    // Query with JOIN FETCH to avoid lazy loading issues
    @Query("SELECT gm FROM GroupMember gm " +
           "LEFT JOIN FETCH gm.user u " +
           "LEFT JOIN FETCH u.userProfile " +
           "WHERE gm.group.groupId = :groupId")
    List<GroupMember> findAllByGroupGroupIdWithUser(@Param("groupId") UUID groupId);

    Page<GroupMember> findAllByUserUserId(UUID userId, Pageable pageable);

    List<GroupMember> findAllByGroupGroupIdAndStatus(UUID groupId, GroupMemberStatusTypeEnum status);

    // Lấy danh sách groups mà user có status PENDING hoặc APPROVED
    Page<GroupMember> findAllByUserUserIdAndStatus(UUID userId, GroupMemberStatusTypeEnum status, Pageable pageable);

    void deleteByGroupAndUser(Group group, User user);

    // Đếm số thành viên mới trong khoảng thời gian
    long countByGroupGroupIdAndJoinedAtAfter(UUID groupId, Instant joinedAtAfter);

    // Lấy danh sách thành viên theo role
    List<GroupMember> findByGroupGroupIdAndRole(UUID groupId, MemberRoleTypeEnum role);

    // Lấy danh sách thành viên theo nhiều roles
    List<GroupMember> findByGroupGroupIdAndRoleIn(UUID groupId, List<MemberRoleTypeEnum> roles);

    // Lấy danh sách bạn bè của user hiện tại đang là thành viên của một group cụ thể
    @Query("SELECT gm.user FROM GroupMember gm " +
           "WHERE gm.group.groupId = :groupId " +
           "AND gm.user.userId IN (" +
           "    SELECT CASE " +
           "        WHEN f.requester.userId = :currentUserId THEN f.receiver.userId " +
           "        ELSE f.requester.userId " +
           "    END " +
           "    FROM Friendship f " +
           "    WHERE (f.requester.userId = :currentUserId OR f.receiver.userId = :currentUserId) " +
           "    AND f.status = 'ACCEPTED'" +
           ")")
    List<User> findFriendMembersInGroup(@Param("groupId") UUID groupId, @Param("currentUserId") UUID currentUserId);
}
