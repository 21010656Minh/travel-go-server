package api.v2.travel_social_network_server.services.like;

import api.v2.travel_social_network_server.entities.Post;
import api.v2.travel_social_network_server.entities.Watch;
import api.v2.travel_social_network_server.entities.ContentLike;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.responses.like.ContentLikeResponse;
import api.v2.travel_social_network_server.repositories.ContentLikeRepository;
import api.v2.travel_social_network_server.repositories.PostRepository;
import api.v2.travel_social_network_server.repositories.WatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentLikeService implements IContentLikeService {

    private final ContentLikeRepository likeRepository;
    private final PostRepository postRepository;
    private final WatchRepository watchRepository;

    @Override
    @Transactional
    public ContentLikeResponse toggleLikeOnPost(UUID postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        // Use ID-based query instead of object comparison
        ContentLike liked = likeRepository.findByPostAndUser(post, user).orElse(null);
        boolean isLiked; // Final state after the operation

        log.info("Like action - PostId: {}, UserId: {}, Already liked: {}", 
                 postId, user.getUserId(), liked != null);

        if (liked != null) {
            // Unlike: remove the like            likeRepository.delete(liked);
            int likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
            post.setLikeCount(Math.max(0, likeCount - 1));
            isLiked = false; // After unliking, the post is not liked
        } else {
            // Like: add a new like            ContentLike like = ContentLike.builder()
                    .user(user)
                    .post(post)
                    .build();
            likeRepository.save(like);

            int likeCount = post.getLikeCount() != null ? post.getLikeCount() : 0;
            post.setLikeCount(likeCount + 1);
            isLiked = true; // After liking, the post is liked
        }

        Post updatedPost = postRepository.save(post);

        log.info("✅ Post like toggled successfully - PostId: {}, LikeCount: {}, isLiked: {}", 
                 postId, updatedPost.getLikeCount(), isLiked);

        return ContentLikeResponse.builder()
                .contentId(post.getPostId())
                .likeCount(updatedPost.getLikeCount())
                .liked(isLiked)
                .build();
    }

    @Override
    @Transactional
    public ContentLikeResponse toggleLikeOnWatch(UUID watchId, User user) {
        Watch watch = watchRepository.findById(watchId)
                .orElseThrow(() -> new ResourceNotFoundException("Watch not found"));

        // Use ID-based query instead of object comparison
        ContentLike liked = likeRepository.findByWatchAndUser(watch, user).orElse(null);
        boolean isLiked; // Final state after the operation

        log.info("Like action - WatchId: {}, UserId: {}, Already liked: {}", 
                 watchId, user.getUserId(), liked != null);

        if (liked != null) {
            // Unlike: remove the like            likeRepository.delete(liked);
            int likeCount = watch.getLikeCount() != null ? watch.getLikeCount() : 0;
            watch.setLikeCount(Math.max(0, likeCount - 1));
            isLiked = false; // After unliking, the watch is not liked
        } else {
            // Like: add a new like            ContentLike like = ContentLike.builder()
                    .user(user)
                    .watch(watch)
                    .build();
            likeRepository.save(like);

            int likeCount = watch.getLikeCount() != null ? watch.getLikeCount() : 0;
            watch.setLikeCount(likeCount + 1);
            isLiked = true; // After liking, the watch is liked
        }

        Watch updatedWatch = watchRepository.save(watch);

        log.info("✅ Watch like toggled successfully - WatchId: {}, LikeCount: {}, isLiked: {}", 
                 watchId, updatedWatch.getLikeCount(), isLiked);

        return ContentLikeResponse.builder()
                .contentId(watch.getWatchId())
                .likeCount(updatedWatch.getLikeCount())
                .liked(isLiked)
                .build();
    }
}
