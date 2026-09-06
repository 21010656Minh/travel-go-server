package api.v2.travel_social_network_server.services.chat.conversationmessage;

import api.v2.travel_social_network_server.entities.Conversation;
import api.v2.travel_social_network_server.entities.ConversationMessage;
import api.v2.travel_social_network_server.entities.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface IConversationMessageService {

    // Gửi tin nhắn
    ConversationMessage sendMessage(Conversation conversation, User user, String content, String type);

    // Gửi tin nhắn với media URL
    ConversationMessage sendMessage(Conversation conversation, User user, String content, String type, String mediaUrl);

    // Lưu tin nhắn mới
    ConversationMessage saveMessage(UUID groupChatId, User user, String content, String type);

    // Lưu tin nhắn mới với media URL
    ConversationMessage saveMessage(UUID groupChatId, User user, String content, String type, String mediaUrl);

    // Lấy tin nhắn theo group (có phân trang)
    Page<ConversationMessage> getMessages(UUID groupId, int page, int size);

    // Tìm kiếm tin nhắn theo keyword trong group (có phân trang)
    Page<ConversationMessage> searchMessages(UUID groupId, String keyword, int page, int size);

    // Lấy tin nhắn cuối cùng của group
    ConversationMessage getLastMessage(UUID groupId);

    // Đánh dấu tin nhắn đã đọc
    void markMessageAsRead(UUID messageId);

    // Đánh dấu tất cả tin nhắn trong group đã đọc bởi user
    void markAllMessagesAsRead(UUID groupId, UUID userId);

    // Xóa tin nhắn
    void deleteMessage(UUID messageId, UUID userId);

    // Lấy số tin nhắn chưa đọc trong group
    long getUnreadMessageCount(UUID groupId, UUID userId);

    // Lấy danh sách tin nhắn chưa đọc
    List<ConversationMessage> getUnreadMessages(UUID groupId, UUID userId);
}
