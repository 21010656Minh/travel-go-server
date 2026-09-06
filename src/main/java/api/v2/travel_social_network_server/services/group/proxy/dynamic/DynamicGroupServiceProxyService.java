package api.v2.travel_social_network_server.services.group.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;
import java.util.UUID;

import api.v2.travel_social_network_server.entities.GroupMember;
import api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.GroupMemberRepository;
import api.v2.travel_social_network_server.services.group.IGroupService;

public class DynamicGroupServiceProxyService implements InvocationHandler {
    private final IGroupService target;
    private final GroupMemberRepository groupMemberRepository;

    public DynamicGroupServiceProxyService(IGroupService target, GroupMemberRepository groupMemberRepository) {
        this.target = target;
        this.groupMemberRepository = groupMemberRepository;
    }

    @SuppressWarnings("unchecked")
    public static IGroupService createProxy(IGroupService target, GroupMemberRepository repo) {
        return (IGroupService) Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class[]{IGroupService.class},
                new DynamicGroupServiceProxyService(target, repo)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // chỉ check cho 4 method đặc biệt
        if (Set.of("approveJoinRequest", "rejectJoinRequest", "removeMemberFromGroup", "changeMemberRole")
                .contains(method.getName())) {

            UUID groupId = (UUID) args[0];
            UUID adminUserId = (UUID) args[2];

            GroupMember admin = groupMemberRepository
                    .findByGroupGroupIdAndUserUserId(groupId, adminUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin is not part of this group"));

            if (!(admin.getRole() == MemberRoleTypeEnum.ADMIN || admin.getRole() == MemberRoleTypeEnum.OWNER)) {
                throw new IllegalStateException("You do not have permission to perform this action");
            }
        }

        // gọi về service gốc
        return method.invoke(target, args);
    }
}


