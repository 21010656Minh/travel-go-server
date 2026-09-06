package api.v2.travel_social_network_server.services.chat.conversationmember;

import api.v2.travel_social_network_server.entities.ConversationMember;
import api.v2.travel_social_network_server.responses.chat.ConversationMemberResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IConversationMemberService {

    // Lấy danh sách thành viên theo group có phân trang (trả về DTO)
    Page<ConversationMemberResponse> getConversationMembersByConversationId(UUID conversationId, int page, int size);

    // Lấy danh sách group mà user tham gia có phân trang
    Page<ConversationMember> getConversationsByUser(UUID userId, int page, int size);
    
    // Lấy thông tin member đã được extract (tránh lazy initialization)
    List<MemberInfo> getMemberInfoByConversationId(UUID conversationId, UUID excludeUserId);
    
    // Record DTO để chứa member info (extracted trong transaction)
    record MemberInfo(UUID userId, String username) {}
}
