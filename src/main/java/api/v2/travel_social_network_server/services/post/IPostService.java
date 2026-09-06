    package api.v2.travel_social_network_server.services.post;

    import api.v2.travel_social_network_server.dtos.post.UpdatePostDto;
    import api.v2.travel_social_network_server.entities.User;
    import api.v2.travel_social_network_server.responses.PageableResponse;
    import api.v2.travel_social_network_server.responses.post.PostMediaResponse;
    import api.v2.travel_social_network_server.responses.post.PostResponse;
    import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
    import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
    import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;

    import java.io.IOException;
    import java.util.List;
    import java.util.UUID;

public interface IPostService extends ICreatePostService {
    PostResponse getPostById(UUID postId, User user);
    List<PostResponse> getPostsByIds(List<UUID> postIds, User user);
    PageableResponse<PostResponse> getPostsByUser(User user, int page, int size);
    PageableResponse<PostResponse> getPostsByGroup(UUID groupId, User user, int page, int size, String sort);
    PageableResponse<PostResponse> getPostsByPrivacy(User user, PrivacyTypeEnum privacy, int page, int size);
    PageableResponse<PostResponse> getPostsByPrivacyAndUser(PrivacyTypeEnum privacy, User user, int page, int size);
    PageableResponse<PostResponse> getPostsByPrivacyAndPostType(User user, PrivacyTypeEnum privacy, PostTypeEnum postType, int page, int size);
    void deletePost(UUID postId, User user);
    PostResponse updatePost(UUID postId, UpdatePostDto updatePostDto, User user) throws IOException;
    PostResponse updatePostPrivacy(UUID postId, PrivacyTypeEnum privacy, User user);
    PageableResponse<PostResponse> searchPostsInGroup(User user,UUID groupId, String keyword, int page, int size);
    PageableResponse<PostResponse> searchPostsFulltext(String keyword, int page, int size);
    List<PostResponse> searchPostsForSuggestion(String keyword, int page, int pageSize);
    List<PostResponse> searchPostsForSuggestionByPostType(String keyword, PostTypeEnum postType, int page, int pageSize);
    PostResponse sharePost(UUID postId, String content, PrivacyTypeEnum privacy, User user);
    List<PostMediaResponse> getMediaByGroupIdAndType(UUID groupId, MediaTypeEnum mediaType);
}