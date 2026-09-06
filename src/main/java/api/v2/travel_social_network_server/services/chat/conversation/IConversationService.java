package api.v2.travel_social_network_server.services.chat.conversation;

import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.chat.ConversationResponse;
import api.v2.travel_social_network_server.responses.chat.SearchConversationResponse;
import api.v2.travel_social_network_server.utilities.enums.ConversationTypeEnum;

import java.util.List;
import java.util.UUID;

public interface IConversationService {

    // Lấy thông tin hội thoại theo Id
    ConversationResponse getConversationByConversationId(UUID conversationId, UUID currentUserId);

    // Tạo conversation (conversation group)
    Conversation createGroupConversation(String groupName, UUID createdBy, List<UUID> memberIds);

    // Tạo conversation 1-1 (private conversation)
    ConversationResponse createOrGetPrivateConversation(UUID user1, UUID user2);

    // Lấy tất cả conversation của user có phân trang
    PageableResponse<ConversationResponse> getUserConversations(UUID userId, int page, int size);

    // Lấy tất cả conversation của user có phân trang và lọc theo loại
    PageableResponse<ConversationResponse> getUserConversations(UUID userId, int page, int size, ConversationTypeEnum type);

    // Tìm kiếm conversation theo keyword có phân trang
    PageableResponse<ConversationResponse> searchConversationsByUerIdAndKeyWord(UUID userId, String keyword, int page,
            int size);

    // Tìm kiếm tất cả conversation bao gồm bạn bè
    PageableResponse<SearchConversationResponse> searchConversationsIncludeFriends(UUID userId, String keyword,
            int page, int size);

    // Cập nhật last message và last active time của conversation
    void updateLastMessage(UUID conversationId, String lastMessage);
    
    // Cập nhật avatar của group conversation
    Conversation updateGroupAvatar(UUID conversationId, String avatarUrl, UUID userId);
    
    // Cập nhật tên group conversation
    Conversation updateGroupName(UUID conversationId, String groupName, UUID userId);
    
    // Thêm members vào group conversation (chỉ admin mới được thêm)
    void addMembersToConversation(UUID conversationId, List<UUID> memberUserIds, UUID adminUserId);
    
    // Xóa member khỏi group conversation (chỉ admin mới được xóa)
    void removeMemberFromConversation(UUID conversationId, UUID memberUserId, UUID adminUserId);
    
    // Xóa conversation (chỉ admin mới được xóa)
    void deleteConversation(UUID conversationId, UUID userId);
}
