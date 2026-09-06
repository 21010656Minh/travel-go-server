package api.v2.travel_social_network_server.services.group;

import api.v2.travel_social_network_server.dtos.group.UpdateGroupDto;
import api.v2.travel_social_network_server.dtos.group.JoinGroupResponse;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.group.GroupMemberResponse;

import java.util.List;
import java.util.UUID;
import java.io.IOException;

public interface IGroupService {
    PageableResponse<GroupResponse> getAllGroupsByName(User user, String keyword, int page, int size);

    GroupResponse getGroupById(UUID groupId, User user);

    GroupResponse createGroup(User user, UpdateGroupDto updateGroupDto) throws IOException;

    GroupResponse updateGroup(UUID groupId, User user, UpdateGroupDto updateGroupDto) throws IOException;

    PageableResponse<GroupResponse> getGroupsOfUser(UUID userId, int page, int size);

    PageableResponse<GroupResponse> getPendingGroupsOfUser(UUID userId, int page, int size);

    JoinGroupResponse joinGroup(UUID groupId, User user);

    void leaveGroup(UUID groupId, User user);

    GroupMemberResponse approveJoinRequest(UUID groupId, UUID targetUserId, UUID adminUserId);

    void rejectJoinRequest(UUID groupId, UUID targetUserId, UUID adminUserId);

    void removeMemberFromGroup(UUID groupId, UUID targetUserId, UUID adminUserId);

    void changeMemberRole(UUID groupId, UUID targetUserId, UUID adminUserId, String newRole);
    
    List<GroupResponse> searchGroupsForSuggestion(String keyword, int page, int pageSize);
    
    PageableResponse<GroupResponse> searchGroupsFulltext(String keyword, int page, int size, User user);
    
    PageableResponse<GroupMemberResponse> getGroupMembers(UUID groupId, User currentUser, String keyword, String filter, int page, int size);
    
    void lockGroup(UUID groupId, UUID adminUserId, String reason);
    
    void unlockGroup(UUID groupId, UUID adminUserId);
}
