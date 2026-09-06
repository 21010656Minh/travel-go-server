package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.utilities.enums.ConversationTypeEnum;
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
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    // Tìm group chat theo groupChatId
    Optional<Conversation> findConversationByConversationId(@Param("conversationId") UUID conversationId);

    // Lấy tất cả group chat mà user tham gia (có phân trang)
    @Query("""
           SELECT g FROM Conversation g
           JOIN g.conversationMembers m
           WHERE m.user.userId = :userId
           """)
    Page<Conversation> findConversationsByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Lấy tất cả group chat mà user tham gia theo loại (có phân trang)
    @Query("""
           SELECT g FROM Conversation g
           JOIN g.conversationMembers m
           WHERE m.user.userId = :userId
             AND g.type = :type
           """)
    Page<Conversation> findConversationsByUserIdAndType(
            @Param("userId") UUID userId,
            @Param("type") ConversationTypeEnum type,
            Pageable pageable
    );

    // Tìm group chat theo keyword với PostgreSQL fulltext search (có phân trang)
    @Query(value = """
           SELECT DISTINCT g.* 
           FROM conversations g
           JOIN conversation_members m ON g.conversation_id = m.conversation_id
           WHERE m.user_id = :userId
             AND (
               to_tsvector('simple', COALESCE(g.conversation_name, '')) @@ plainto_tsquery('simple', :keyword)
               OR LOWER(g.conversation_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR to_tsvector('simple', COALESCE(g.last_message, '')) @@ plainto_tsquery('simple', :keyword)
             )
           ORDER BY 
             CASE 
               WHEN LOWER(g.conversation_name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
               WHEN to_tsvector('simple', COALESCE(g.conversation_name, '')) @@ plainto_tsquery('simple', :keyword) THEN 2
               WHEN LOWER(g.conversation_name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3
               ELSE 4
             END,
             g.last_active_at DESC NULLS LAST
           """, 
           countQuery = """
           SELECT COUNT(DISTINCT g.conversation_id)
           FROM conversations g
           JOIN conversation_members m ON g.conversation_id = m.conversation_id
           WHERE m.user_id = :userId
             AND (
               to_tsvector('simple', COALESCE(g.conversation_name, '')) @@ plainto_tsquery('simple', :keyword)
               OR LOWER(g.conversation_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR to_tsvector('simple', COALESCE(g.last_message, '')) @@ plainto_tsquery('simple', :keyword)
             )
           """,
           nativeQuery = true)
    Page<Conversation> searchConversationsByUserAndKeyword(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // Tìm group chat theo keyword + type với fulltext search
    @Query(value = """
           SELECT DISTINCT g.* 
           FROM conversations g
           JOIN conversation_members m ON g.conversation_id = m.conversation_id
           WHERE m.user_id = :userId
             AND g.type = CAST(:conversationType AS text)
             AND (
               to_tsvector('simple', COALESCE(g.conversation_name, '')) @@ plainto_tsquery('simple', :keyword)
               OR LOWER(g.conversation_name) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR to_tsvector('simple', COALESCE(g.last_message, '')) @@ plainto_tsquery('simple', :keyword)
             )
           ORDER BY 
             CASE 
               WHEN LOWER(g.conversation_name) LIKE LOWER(CONCAT(:keyword, '%')) THEN 1
               WHEN to_tsvector('simple', COALESCE(g.conversation_name, '')) @@ plainto_tsquery('simple', :keyword) THEN 2
               WHEN LOWER(g.conversation_name) LIKE LOWER(CONCAT('%', :keyword, '%')) THEN 3
               ELSE 4
             END,
             g.last_active_at DESC NULLS LAST
           """,
           nativeQuery = true)
    List<Conversation> searchConversationsByUserAndKeywordWithType(
            @Param("userId") UUID userId,
            @Param("keyword") String keyword,
            @Param("conversationType") String conversationType
    );

    // Tìm private chat giữa 2 user
    @Query("""
           SELECT gc FROM Conversation gc
           WHERE gc.type = :conversationTypeEnum
             AND EXISTS (
                 SELECT 1 FROM gc.conversationMembers m1 WHERE m1.user.userId = :userId1
             )
             AND EXISTS (
                 SELECT 1 FROM gc.conversationMembers m2 WHERE m2.user.userId = :userId2
             )
           """)
    Optional<Conversation> findPrivateConversationBetweenUsers(
            @Param("userId1") UUID userId1,
            @Param("userId2") UUID userId2,
            @Param("conversationTypeEnum") ConversationTypeEnum conversationTypeEnum
    );
}
