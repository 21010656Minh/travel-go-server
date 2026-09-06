package api.v2.travel_social_network_server.services.post.strategy;

import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.Tag;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.group.GroupResponse;
import api.v2.travel_social_network_server.responses.post.PostMediaResponse;
import api.v2.travel_social_network_server.responses.post.PostResponse;
import api.v2.travel_social_network_server.responses.post.PostUserResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DefaultPostResponseMapper implements PostResponseMapper {
    @Override
    public PostResponse toResponse(Post post, User user) {
        return PostResponse.builder()
                .postId(post.getPostId())
                .content(post.getContent())
                .location(post.getLocation())
                .createdAt(post.getCreatedAt())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .shareCount(post.getShareCount())
                .user(PostUserResponse.builder()
                        .userId(post.getUser().getUserId())
                        .fullName(post.getUser().getUserProfile().getFullName())
                        .avatarImg(post.getUser().getAvatarImg())
                        .build())
                .mediaList(post.getMediaList() != null ? post.getMediaList().stream()
                        .map(media -> PostMediaResponse.builder()
                                .mediaId(media.getMediaId())
                                .postId(post.getPostId())
                                .url(media.getUrl())
                                .type(media.getType())
                                .build())
                        .toList() : List.of())
                .tags(post.getTags() != null
                        ? post.getTags().stream().map(Tag::getTitle).toList()
                        : List.of())
                .isShare(post.getIsShare() != null ? post.getIsShare() : false)
                .postType(post.getPostType())
                .sharedPost(post.getSharedPost() != null ? toSharedPostResponse(post.getSharedPost(), user) : null)
                .privacy(post.getPrivacy())
                .group(post.getGroup() != null ? GroupResponse.builder()
                        .groupId(post.getGroup().getGroupId())
                        .groupName(post.getGroup().getGroupName())
                        .coverImageUrl(post.getGroup().getCoverImageUrl())
                        .build() : null)
                .liked(user != null && post.getContentLikes() != null &&
                        post.getContentLikes().stream().anyMatch(like -> like.getUser().getUserId().equals(user.getUserId())))
                .build();
    }

    private PostResponse toSharedPostResponse(Post sharedPost, User user) {
        return PostResponse.builder()
                .postId(sharedPost.getPostId())
                .content(sharedPost.getContent())
                .location(sharedPost.getLocation())
                .createdAt(sharedPost.getCreatedAt())
                .likeCount(sharedPost.getLikeCount())
                .commentCount(sharedPost.getCommentCount())
                .shareCount(sharedPost.getShareCount())
                .user(PostUserResponse.builder()
                        .userId(sharedPost.getUser().getUserId())
                        .fullName(sharedPost.getUser().getUserProfile().getFullName())
                        .avatarImg(sharedPost.getUser().getAvatarImg())
                        .build())
                .mediaList(sharedPost.getMediaList() != null ? sharedPost.getMediaList().stream()
                        .map(media -> PostMediaResponse.builder()
                                .mediaId(media.getMediaId())
                                .postId(sharedPost.getPostId())
                                .url(media.getUrl())
                                .type(media.getType())
                                .build())
                        .toList() : List.of())
                .tags(sharedPost.getTags() != null
                        ? sharedPost.getTags().stream().map(Tag::getTitle).toList()
                        : List.of())
                .isShare(sharedPost.getIsShare() != null ? sharedPost.getIsShare() : false)
                .postType(sharedPost.getPostType())
                .privacy(sharedPost.getPrivacy())
                .group(sharedPost.getGroup() != null ? GroupResponse.builder()
                        .groupId(sharedPost.getGroup().getGroupId())
                        .groupName(sharedPost.getGroup().getGroupName())
                        .coverImageUrl(sharedPost.getGroup().getCoverImageUrl())
                        .build() : null)
                .liked(user != null && sharedPost.getContentLikes() != null &&
                        sharedPost.getContentLikes().stream().anyMatch(like -> like.getUser().getUserId().equals(user.getUserId())))
                .build();
    }
}
