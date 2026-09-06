package api.v2.travel_social_network_server.responses.chat;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Response for Conversation Message (MongoDB)
 */
@Data
@SuperBuilder
public class ConversationMessageResponse {
    
    // MongoDB ID
    private String id;
    
    // PostgreSQL UUID (compatibility)
    private UUID conversationMessageId;
    
    // Conversation info
    private UUID conversationId;
    
    // Sender info
    private UUID senderId;
    private String senderName;
    private String senderAvatar;
    
    // Message content
    private String content;
    private String mediaUrl;
    private String type;
    private String status;
    
    // Timestamps
    private Instant createdAt;
    private Instant updatedAt;
    
    // Edit tracking
    private boolean isEdited;
    private Instant editedAt;
    
    // Reply support
    private String replyToMessageId;
    private String repliedMessageContent;
}
