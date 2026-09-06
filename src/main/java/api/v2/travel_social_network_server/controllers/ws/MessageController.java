package api.v2.travel_social_network_server.controllers.ws;

import api.v2.travel_social_network_server.dtos.chat.message.MessageDeliveryReceiptDto;
import api.v2.travel_social_network_server.dtos.chat.message.MessageDeliveryRequestDto;
import api.v2.travel_social_network_server.dtos.chat.message.MessageResponse;
import api.v2.travel_social_network_server.dtos.chat.message.SendMessageRequest;
import api.v2.travel_social_network_server.dtos.chat.message.TypingNotificationDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.mongodb.ConversationMessageDocument;
import api.v2.travel_social_network_server.responses.notification.NotificationResponse;
import api.v2.travel_social_network_server.responses.user.UserSummaryResponse;
import api.v2.travel_social_network_server.utilities.enums.NotificationTypeEnum;
import api.v2.travel_social_network_server.services.chat.mongodb.ConversationMessageMongoService;
import api.v2.travel_social_network_server.services.chat.conversation.IConversationService;
import api.v2.travel_social_network_server.services.chat.conversationmember.IConversationMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {
        private final SimpMessagingTemplate simpMessagingTemplate;
        private final ConversationMessageMongoService messageMongoService;
        private final IConversationService conversationService;
        private final IConversationMemberService conversationMemberService;

        @MessageMapping("/private-message")
        @SendToUser("/queue/private-message")
        public MessageResponse receivePrivateMessage(
                        SimpMessageHeaderAccessor headerAccessor,
                        @Payload SendMessageRequest request) {
                User sender = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("simpUser");
                
                if (sender == null) {
                        log.error("❌ WebSocket authentication failed: sender is null in handlePrivateMessage");
                        throw new IllegalStateException("User not authenticated for WebSocket");
                }

                // Get sender info from userProfile
                String senderName = sender.getUserProfile() != null && sender.getUserProfile().getFullName() != null
                                ? sender.getUserProfile().getFullName()
                                : sender.getUsername();
                String senderAvatar = sender.getAvatarImg();

                if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                        throw new IllegalArgumentException("Message content is required");
                }

                // Set default values for optional fields
                String messageType = request.getType() != null ? request.getType() : "text";
                String mediaUrl = request.getMediaUrl(); // Can be null for text messages

                // Save message to MongoDB
                ConversationMessageDocument savedMessage = messageMongoService.saveMessage(
                                request.getGroupChatId(),
                                sender.getUserId(),
                                senderName,
                                senderAvatar,
                                request.getContent(),
                                messageType,
                                mediaUrl);

                // Update conversation metadata in PostgreSQL
                conversationService.updateLastMessage(
                                request.getGroupChatId(),
                                request.getContent());

                // Create response with actual MongoDB message ID
                MessageResponse messageResponse = MessageResponse.builder()
                                .messageId(savedMessage.getConversationMessageId())
                                .mongoId(savedMessage.getId())
                                .groupChatId(request.getGroupChatId())
                                .senderId(sender.getUserId())
                                .senderName(senderName)
                                .senderAvatar(senderAvatar)
                                .content(savedMessage.getContent())
                                .type(savedMessage.getType())
                                .mediaUrl(savedMessage.getMediaUrl())
                                .status(savedMessage.getStatus())
                                .createdAt(savedMessage.getCreatedAt())
                                .build();

                String destination = "/group/" + request.getGroupChatId();
                simpMessagingTemplate.convertAndSend(destination, messageResponse);
                
                // Broadcast new message notification to all members (except sender)
                broadcastNewMessageNotification(request.getGroupChatId(), sender.getUserId(), messageResponse);
                
                return messageResponse;
        }

        @MessageMapping("/group-message")
        @SendToUser("/queue/group-message")
        public MessageResponse receiveGroupMessage(
                        SimpMessageHeaderAccessor headerAccessor,
                        @Payload SendMessageRequest request) {
                User sender = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("simpUser");
                
                if (sender == null) {
                        log.error("❌ WebSocket authentication failed: sender is null in handleGroupMessage");
                        throw new IllegalStateException("User not authenticated for WebSocket");
                }

                if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                        throw new IllegalArgumentException("Message content is required");
                }

                String messageType = request.getType() != null ? request.getType() : "text";
                String mediaUrl = request.getMediaUrl();
                String replyToMessageId = request.getReplyToMessageId();

                // Save message to MongoDB
                ConversationMessageDocument savedMessage = messageMongoService.saveMessage(
                                request.getGroupChatId(),
                                sender.getUserId(),
                                sender.getUsername(),
                                sender.getAvatarImg(),
                                request.getContent(),
                                messageType,
                                mediaUrl,
                                replyToMessageId);

                // Update conversation metadata in PostgreSQL
                conversationService.updateLastMessage(
                                request.getGroupChatId(),
                                request.getContent());

                String repliedMessageContent = null;
                if (replyToMessageId != null) {
                        try {
                                ConversationMessageDocument repliedMsg = messageMongoService.getMessageById(replyToMessageId);
                                if (repliedMsg != null) {
                                        repliedMessageContent = repliedMsg.getContent();
                                }
                        } catch (Exception e) {
                                // Silently handle
                        }
                }

                // Create response with actual MongoDB message ID
                MessageResponse messageResponse = MessageResponse.builder()
                                .messageId(savedMessage.getConversationMessageId())
                                .mongoId(savedMessage.getId())
                                .groupChatId(request.getGroupChatId())
                                .senderId(sender.getUserId())
                                .senderName(sender.getUsername())
                                .content(savedMessage.getContent())
                                .type(savedMessage.getType())
                                .mediaUrl(savedMessage.getMediaUrl())
                                .status(savedMessage.getStatus())
                                .createdAt(savedMessage.getCreatedAt())
                                .senderAvatar(sender.getAvatarImg())
                                .replyToMessageId(savedMessage.getReplyToMessageId())
                                .repliedMessageContent(repliedMessageContent)
                                .build();

                String destination = "/group/" + request.getGroupChatId();
                simpMessagingTemplate.convertAndSend(destination, messageResponse);
                
                // Broadcast new message notification to all members (except sender)
                broadcastNewMessageNotification(request.getGroupChatId(), sender.getUserId(), messageResponse);
                
                return messageResponse;
        }

        @MessageMapping("/typing")
        public void handleTyping(
                        SimpMessageHeaderAccessor headerAccessor,
                        @Payload TypingNotificationDto typingNotification) {
                User sender = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("simpUser");
                
                if (sender == null) {
                        log.error("❌ WebSocket authentication failed: sender is null in handleTyping");
                        return; // Silently ignore
                }

                TypingNotificationDto notification = TypingNotificationDto.builder()
                                .userId(sender.getUserId())
                                .username(sender.getUsername())
                                .typing(typingNotification.isTyping())
                                .groupChatId(typingNotification.getGroupChatId())
                                .build();

                String destination = "/group/" + typingNotification.getGroupChatId() + "/typing";
                simpMessagingTemplate.convertAndSend(destination, notification);
        }

        @MessageMapping("/message-delivered")
        public void handleMessageDelivered(
                        SimpMessageHeaderAccessor headerAccessor,
                        @Payload MessageDeliveryRequestDto request) {
                User user = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("simpUser");
                
                if (user == null) {
                        log.error("❌ WebSocket authentication failed: user is null in handleMessageDelivered");
                        return; // Silently ignore
                }

                try {
                        messageMongoService.markMessageAsRead(request.getMessageId());
                } catch (Exception e) {
                        // Silently handle
                }

                MessageDeliveryReceiptDto receipt = MessageDeliveryReceiptDto.builder()
                                .messageId(request.getMessageId())
                                .conversationId(request.getConversationId())
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .type("delivered")
                                .timestamp(Instant.now())
                                .build();

                simpMessagingTemplate.convertAndSend("/group/" + request.getConversationId() + "/receipt", receipt);
        }

        @MessageMapping("/message-read")
        public void handleMessageRead(
                        SimpMessageHeaderAccessor headerAccessor,
                        @Payload MessageDeliveryRequestDto request) {
                User user = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("simpUser");

                try {
                        messageMongoService.markMessageAsRead(request.getMessageId());
                } catch (Exception e) {
                        // Silently handle
                }

                MessageDeliveryReceiptDto receipt = MessageDeliveryReceiptDto.builder()
                                .messageId(request.getMessageId())
                                .conversationId(request.getConversationId())
                                .userId(user.getUserId())
                                .username(user.getUsername())
                                .type("read")
                                .timestamp(Instant.now())
                                .build();

                simpMessagingTemplate.convertAndSend("/group/" + request.getConversationId() + "/receipt", receipt);
        }

        /**
         * Broadcast new message notification to all conversation members (except sender)
         * Sends to dedicated /queue/unread-messages endpoint - no database persistence
         * Simple lightweight notification for real-time unread message indicator
         */
        private void broadcastNewMessageNotification(UUID conversationId, UUID senderId, MessageResponse messageResponse) {
                try {
                        // Get member info (extracted trong transaction để tránh lazy init exception)
                        List<IConversationMemberService.MemberInfo> memberInfos = 
                                conversationMemberService.getMemberInfoByConversationId(conversationId, senderId);
                        
                        // Create notification cho mỗi member (data đã được extracted, không còn lazy load)
                        memberInfos.forEach(memberInfo -> {
                                // Create minimal notification payload (no DB storage needed)
                                NotificationResponse notification = NotificationResponse.builder()
                                        .notificationId(UUID.randomUUID()) // Temporary ID - not saved
                                        .receiverId(memberInfo.userId())
                                        .receiverName(memberInfo.username())
                                        .sender(UserSummaryResponse.builder()
                                                .userId(senderId)
                                                .userName(messageResponse.getSenderName())
                                                .avatarImg(messageResponse.getSenderAvatar())
                                                .build())
                                        .type(NotificationTypeEnum.CHAT_MESSAGE)
                                        .content("New message")
                                        .relatedId(conversationId)
                                        .isRead(false)
                                        .createdAt(Instant.now())
                                        .updatedAt(Instant.now())
                                        .build();

                                // Send to dedicated unread messages queue (separate from general notifications)
                                String userQueue = "/user/" + memberInfo.userId() + "/queue/unread-messages";
                                simpMessagingTemplate.convertAndSend(userQueue, notification);
                                
                                log.info("✅ Sent unread message notification to user: {} via queue: {}", memberInfo.userId(), userQueue);
                        });
                } catch (Exception e) {
                        log.error("Error broadcasting unread message notification", e);
                }
        }
}
