package api.v2.travel_social_network_server.services.storage.adapter;

import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("cloudinaryStorageAdapter")
@RequiredArgsConstructor
public class CloudinaryStorageAdapter implements MediaStorage {

    private final Cloudinary cloudinary;

    @Override
    public String uploadFile(byte[] fileData, String folderName, String resourceType) {
        try {
            Map uploadOptions = ObjectUtils.asMap(
                    "folder", folderName,
                    "resource_type", resourceType  // "image", "video", "auto"
            );

            Map uploadResult = cloudinary.uploader().upload(fileData, uploadOptions);

            return uploadResult.get("url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Error uploading to Cloudinary: " + e.getMessage());
        }
    }

    @Override
    public List<ContentMedia> uploadMultipleFiles(List<MultipartFile> files, String folderName) {
        List<ContentMedia> contentMedias = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                byte[] fileData = file.getBytes();

                MediaTypeEnum type = MediaTypeEnum.fromString(file.getContentType());

                String url = uploadFile(fileData, folderName, type.getValue().toLowerCase());

                ContentMedia contentMedia = ContentMedia.builder()
                        .url(url)
                        .type(type)
                        .build();

                contentMedias.add(contentMedia);

            } catch (IOException e) {
                throw new RuntimeException("Failed to upload one of the files: " + e.getMessage());
            }
        }

        return contentMedias;
    }
}
