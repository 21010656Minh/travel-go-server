package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.User;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUserName(String username);

    Optional<User> findByUserId(UUID id);
    //
    boolean existsByEmail(String email);

    boolean existsByUserName(String userName);

    Optional<User> findByEmail(String email);

    Optional<User> findByPasswordResetToken_ResetToken(String resetToken);

    @Query("SELECT u FROM User u JOIN u.userProfile up WHERE " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(up.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> findByUserNameOrFullNameContainingIgnoreCase(String keyword, Pageable pageable);

    // Search users for suggestion
    @Query("SELECT u FROM User u LEFT JOIN u.userProfile up WHERE " +
            "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(up.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchUsersForSuggestion(@Param("keyword") String keyword, Pageable pageable);

    // Fulltext search using PostgreSQL to_tsvector
    @Query(value = "SELECT u.* FROM users u " +
            "LEFT JOIN user_profiles up ON u.user_id = up.user_id " +
            "WHERE to_tsvector('simple', COALESCE(u.user_name, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(up.full_name, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(u.email, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(up.location, '')) @@ plainto_tsquery('simple', :keyword) " +
            "ORDER BY " +
            "CASE " +
            "  WHEN to_tsvector('simple', COALESCE(u.user_name, '')) @@ plainto_tsquery('simple', :keyword) THEN 1 " +
            "  WHEN to_tsvector('simple', COALESCE(up.full_name, '')) @@ plainto_tsquery('simple', :keyword) THEN 2 " +
            "  ELSE 3 " +
            "END, u.created_at DESC",
            countQuery = "SELECT COUNT(*) FROM users u " +
            "LEFT JOIN user_profiles up ON u.user_id = up.user_id " +
            "WHERE to_tsvector('simple', COALESCE(u.user_name, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(up.full_name, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(u.email, '')) @@ plainto_tsquery('simple', :keyword) " +
            "OR to_tsvector('simple', COALESCE(up.location, '')) @@ plainto_tsquery('simple', :keyword)",
            nativeQuery = true)
    Page<User> searchUsersFulltext(@Param("keyword") String keyword, Pageable pageable);
}