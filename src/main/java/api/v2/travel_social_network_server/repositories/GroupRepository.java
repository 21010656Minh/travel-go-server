package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Group;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Query("SELECT g FROM Group g WHERE g.privacy = :privacy")
    Page<Group> findAllByPrivacy(@Param("privacy") PrivacyTypeEnum privacyTypeEnum, Pageable pageable);

    @Query("SELECT g FROM Group g WHERE LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Group> findAllByGroupNameContaining(@Param("keyword") String keyword, Pageable pageable);

    Optional<Group> findByGroupId(UUID groupId);

    @Query("SELECT g FROM Group g JOIN g.groupMembers m WHERE m.user.userId = :userId AND m.status = 'JOINED'")
    Page<Group> findAllGroupsByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Search groups for suggestion
    @Query("SELECT g FROM Group g WHERE " +
            "LOWER(g.groupName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(g.groupDescription) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Group> searchGroupsForSuggestion(@Param("keyword") String keyword, Pageable pageable);

    // Fulltext search using PostgreSQL to_tsvector
    @Query(value = "SELECT g.* FROM groups g " +
            "WHERE to_tsvector('simple', COALESCE(g.group_name, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(g.group_description, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(g.location, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(g.tags, '')) @@ plainto_tsquery('simple', :keyword) " +
            "ORDER BY " +
            "CASE " +
            "  WHEN to_tsvector('simple', COALESCE(g.group_name, '')) @@ plainto_tsquery('simple', :keyword) THEN 1 " +
            "  WHEN to_tsvector('simple', COALESCE(g.group_description, '')) @@ plainto_tsquery('simple', :keyword) THEN 2 " +
            "  ELSE 3 " +
            "END, g.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM groups g " +
            "WHERE to_tsvector('simple', COALESCE(g.group_name, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(g.group_description, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(g.location, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(g.tags, '')) @@ plainto_tsquery('simple', :keyword)",
            nativeQuery = true)
    Page<Group> searchGroupsFulltext(@Param("keyword") String keyword, Pageable pageable);
}
