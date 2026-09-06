package api.v2.travel_social_network_server.utilities.builder;

import api.v2.travel_social_network_server.dtos.post.UpdatePostDto;
import api.v2.travel_social_network_server.entities.*;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.GroupRepository;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.services.tag.TagService;
import api.v2.travel_social_network_server.utilities.enums.PostTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.PrivacyTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public class PostBuilder {
    private final Post post;
    private static final String USERS_FOLDER = "users"; // For avatars and cover images

    public PostBuilder(User user, UpdatePostDto dto) {
        this.post = Post.builder()
                .user(user)
                .content(dto.getContent())
                .location(dto.getLocation())
                .privacy(PrivacyTypeEnum.fromString(dto.getPrivacy()))
                .postType(PostTypeEnum.fromString(dto.getPostType()))
                .isShare(false)
                .build();
    }

    public PostBuilder withTags(List<String> tagTitles, TagService tagService) {
        if (tagTitles != null && !tagTitles.isEmpty() && tagService != null) {
            List<Tag> tags = tagService.processTagTitles(tagTitles);
            post.setTags(tags);
        }
        return this;
    }

    public PostBuilder withMedia(List<MultipartFile> media, MediaStorage cloudinaryService, String folder) {
        if (media != null && !media.isEmpty()) {
            // Use users folder for AVATAR_UPDATE and COVER_UPDATE post types
            String targetFolder = (post.getPostType() == PostTypeEnum.AVATAR_UPDATE || 
                                   post.getPostType() == PostTypeEnum.COVER_UPDATE) 
                                   ? USERS_FOLDER 
                                   : folder;
            
            List<ContentMedia> mediaList = cloudinaryService.uploadMultipleFiles(media, targetFolder);
            mediaList.forEach(m -> m.setPost(post));
            post.setMediaList(mediaList);
        }
        return this;
    }

    public PostBuilder withGroup(UUID groupId, GroupRepository groupRepository) {
        if (groupId != null) {
            Group group = groupRepository.findByGroupId(groupId)
                    .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));
            post.setGroup(group);
        }
        return this;
    }

    public Post build() {
        return post;
    }
}

