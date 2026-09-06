package api.v2.travel_social_network_server.configurations;

import api.v2.travel_social_network_server.repositories.GroupMemberRepository;
import api.v2.travel_social_network_server.services.group.IGroupService;
import api.v2.travel_social_network_server.services.group.proxy.dynamic.DynamicGroupServiceProxyService;
import api.v2.travel_social_network_server.services.post.IPostService;
import api.v2.travel_social_network_server.services.redis.IRedisService;
import api.v2.travel_social_network_server.services.suggestion.newsfeed.INewsFeedService;
import api.v2.travel_social_network_server.services.suggestion.newsfeed.proxy.DynamicNewsFeedServiceProxy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ProxyConfig {
    @Bean("dynamicGroupServiceProxy")
    public IGroupService dynamicGroupServiceProxy(@Qualifier("groupService") IGroupService groupService,
                                                  GroupMemberRepository groupMemberRepository) {
        return DynamicGroupServiceProxyService.createProxy(groupService, groupMemberRepository);
    }

    @Primary
    @Bean("dynamicNewsFeedServiceProxy")
    public INewsFeedService dynamicNewsFeedServiceProxy(
            @Qualifier("newsFeedServiceDecorator") INewsFeedService newsFeedService,
            IRedisService redisService,
            @Qualifier("postService") IPostService postService
    ) {
        return DynamicNewsFeedServiceProxy.createProxy(newsFeedService, redisService, postService);
    }
}

