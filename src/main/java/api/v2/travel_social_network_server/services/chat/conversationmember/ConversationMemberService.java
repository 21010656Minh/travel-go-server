package api.v2.travel_social_network_server.services.chat.conversationmember;

import api.v2.travel_social_network_server.entities.ConversationMember;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.repositories.ConversationMemberRepository;
import api.v2.travel_social_network_server.responses.chat.ConversationMemberResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ConversationMemberService implements IConversationMemberService {

    private final ConversationMemberRepository conversationMemberRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationMemberResponse> getConversationMembersByConversationId(UUID conversationId, int page, int size) {
        Page<ConversationMember> members = conversationMemberRepository.findByConversationConversationId(
                conversationId, PageRequest.of(page, size));
        
        // Map entity sang DTO trong transaction scope để tránh lazy loading exception
        return members.map(member -> {
            User user = member.getUser();
            return ConversationMemberResponse.builder()
                    .conversationMemberId(member.getConversationMemberId())
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .fullName(user.getUserProfile().getFullName())
                    .avatarUrl(user.getAvatarImg())
                    .role(member.getRole())
                    .joinedAt(member.getJoinedAt())
                    .build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationMember> getConversationsByUser(UUID userId, int page, int size) {
        return conversationMemberRepository.findByUserId(userId, PageRequest.of(page, size));
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<IConversationMemberService.MemberInfo> getMemberInfoByConversationId(UUID conversationId, UUID excludeUserId) {
        Page<ConversationMember> members = conversationMemberRepository.findByConversationConversationId(
            conversationId, PageRequest.of(0, 100)
        );
        
        // Extract data TRONG transaction scope để tránh lazy init exception
        return members.getContent().stream()
            .filter(member -> !member.getUser().getUserId().equals(excludeUserId))
            .map(member -> new IConversationMemberService.MemberInfo(
                member.getUser().getUserId(),
                member.getUser().getUsername()
            ))
            .collect(Collectors.toList());
    }
}
