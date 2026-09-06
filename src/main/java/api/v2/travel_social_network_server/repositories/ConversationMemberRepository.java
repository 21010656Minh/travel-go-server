package api.v2.travel_social_network_server.repositories;

import api.v2.travel_social_network_server.entities.ConversationMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConversationMemberRepository extends JpaRepository<ConversationMember, UUID> {

    // Lấy tất cả thành viên trong group theo phân trang
    Page<ConversationMember> findByConversationConversationId(UUID groupChatId, Pageable pageable);

    // ConversationMemberRepository
    @Query("""
       SELECT m FROM ConversationMember m
       WHERE m.user.userId = :userId
       """)
    Page<ConversationMember> findByUserId(@Param("userId") UUID userId, Pageable pageable);

    // Kiểm tra user có phải là member của group không
    boolean existsByConversationConversationIdAndUserUserId(UUID groupChatId, UUID userId);

    // Lấy tất cả member của group
    List<ConversationMember> findByConversationConversationId(UUID groupChatId);

    // Lấy member theo group và user
    ConversationMember findByConversationConversationIdAndUserUserId(UUID groupChatId, UUID userId);

    // Xóa member khỏi group
    void deleteByConversationConversationIdAndUserUserId(UUID groupChatId, UUID userId);
    
    // Xóa tất cả members của conversation
    void deleteByConversation(api.v2.travel_social_network_server.entities.Conversation conversation);
}
