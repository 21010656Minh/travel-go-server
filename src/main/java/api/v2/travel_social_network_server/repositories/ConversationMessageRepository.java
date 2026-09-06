package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ConversationMessage;
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

@Repository
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, UUID> {

    // Lấy tất cả tin nhắn trong group theo phân trang, sắp xếp theo thời gian gửi
    Page<ConversationMessage> findByConversationConversationIdOrderByCreatedAtAsc(UUID groupChatId, Pageable pageable);

    // Tìm tin nhắn trong group theo từ khóa, phân trang
    @Query("""
           SELECT m FROM ConversationMessage m
           WHERE m.conversation.conversationId = :groupId
             AND m.content LIKE CONCAT('%', :keyword, '%')
           ORDER BY m.createdAt ASC
           """)
    Page<ConversationMessage> searchMessagesInGroup(@Param("groupId") UUID groupId,
                                                    @Param("keyword") String keyword,
                                                    Pageable pageable);

    // Lấy tin nhắn cuối cùng của group
    Optional<ConversationMessage> findFirstByConversationConversationIdOrderByCreatedAtDesc(UUID groupChatId);

    // Đánh dấu tất cả tin nhắn trong group đã đọc bởi user
    @Modifying
    @Query("""
           UPDATE ConversationMessage m 
           SET m.status = 'read' 
           WHERE m.conversation.conversationId = :groupId 
             AND m.sender.userId != :userId 
             AND m.status != 'read'
           """)
    void markAllMessagesAsReadInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    // Đếm số tin nhắn chưa đọc trong group
    @Query("""
           SELECT COUNT(m) FROM ConversationMessage m
           WHERE m.conversation.conversationId = :groupId
             AND m.sender.userId != :userId
             AND m.status != 'read'
           """)
    long countUnreadMessagesInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    // Lấy danh sách tin nhắn chưa đọc trong group
    @Query("""
           SELECT m FROM ConversationMessage m
           WHERE m.conversation.conversationId = :groupId
             AND m.sender.userId != :userId
             AND m.status != 'read'
           ORDER BY m.createdAt ASC
           """)
    List<ConversationMessage> findUnreadMessagesInGroup(@Param("groupId") UUID groupId, @Param("userId") UUID userId);

    // Lấy tin nhắn theo sender
    List<ConversationMessage> findBySenderUserIdOrderByCreatedAtDesc(UUID senderId);

    // Lấy tin nhắn trong khoảng thời gian
    @Query("""
           SELECT m FROM ConversationMessage m
           WHERE m.conversation.conversationId = :groupId
             AND m.createdAt BETWEEN :startTime AND :endTime
           ORDER BY m.createdAt ASC
           """)
    List<ConversationMessage> findMessagesInTimeRange(@Param("groupId") UUID groupId,
                                                      @Param("startTime") java.time.Instant startTime,
                                                      @Param("endTime") java.time.Instant endTime);
}
