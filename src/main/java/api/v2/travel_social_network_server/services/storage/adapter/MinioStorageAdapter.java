package api.v2.travel_social_network_server.services.storage.adapter;

import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service("minioStorageAdapter")
@Primary
@RequiredArgsConstructor
public class MinioStorageAdapter implements MediaStorage {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name:images}")
    private String bucketName;

    @Value("${minio.endpoint:http://localhost:9000}")
    private String endpoint;
    
    @Value("${minio.public-url:#{null}}")
    private String publicUrl;
    
    @Value("${minio.bucket.watches:watches}")
    private String watchesBucket;
    
    @Value("${minio.bucket.posts:posts}")
    private String postsBucket;
    
    @Value("${minio.bucket.groups:groups}")
    private String groupsBucket;
    
    @Value("${minio.bucket.users:users}")
    private String usersBucket;

    // --- Map contentType sang extension ---
    private String getFileExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "video/mp4" -> ".mp4";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    @Override
    public String uploadFile(byte[] fileData, String folderName, String contentType) {
        try {
            // Determine bucket based on folder name
            String targetBucket = determineBucket(folderName);
            
            // Ensure bucket exists
            ensureBucketExists(targetBucket);
            
            String extension = getFileExtension(contentType);
            String fileName = UUID.randomUUID() + extension;
            
            // Extract subfolder path from folderName
            // Example: "watches/videos" -> bucket="watches", subfolder="videos"
            //          "users" -> bucket="users", subfolder=""
            String subfolder = extractSubfolder(folderName, targetBucket);
            String objectName = subfolder.isEmpty() ? fileName : subfolder + "/" + fileName;

            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(targetBucket)
                            .object(objectName)
                            .stream(new ByteArrayInputStream(fileData), fileData.length, -1)
                            .contentType(contentType)
                            .build()
            );

            // Return public URL (use publicUrl if configured, otherwise use endpoint)
            String baseUrl = (publicUrl != null && !publicUrl.isEmpty()) ? publicUrl : endpoint;
            return baseUrl + "/" + targetBucket + "/" + objectName;

        } catch (Exception e) {
            throw new RuntimeException("Error uploading file to MinIO: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ContentMedia> uploadMultipleFiles(List<MultipartFile> files, String folderName) {
        List<ContentMedia> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                byte[] fileData = file.getBytes();
                MediaTypeEnum type = MediaTypeEnum.fromString(file.getContentType());

                String url = uploadFile(fileData, folderName, file.getContentType());

                ContentMedia contentMedia = ContentMedia.builder()
                        .url(url)
                        .type(type)
                        .build();

                uploadedFiles.add(contentMedia);

            } catch (IOException e) {
                throw new RuntimeException("Error uploading file to MinIO: " + file.getOriginalFilename(), e);
            }
        }
        return uploadedFiles;
    }
    
    /**
     * Determine which bucket to use based on folder name
     */
    private String determineBucket(String folderName) {
        if (folderName == null) {
            return bucketName;
        }
        
        String folder = folderName.toLowerCase();
        
        if (folder.startsWith("watches")) {
            return watchesBucket;
        } else if (folder.startsWith("posts")) {
            return postsBucket;
        } else if (folder.startsWith("groups")) {
            return groupsBucket;
        } else if (folder.startsWith("users") || folder.startsWith("avatars") || folder.startsWith("profiles")) {
            return usersBucket;
        }
        
        return bucketName; // default bucket
    }
    
    /**
     * Extract subfolder path from folderName after removing bucket prefix
     * Example: "watches/videos" with bucket "watches" -> "videos"
     *          "users" with bucket "users" -> ""
     *          "posts/images" with bucket "posts" -> "images"
     */
    private String extractSubfolder(String folderName, String bucket) {
        if (folderName == null || folderName.isEmpty()) {
            return "";
        }
        
        String folder = folderName.toLowerCase();
        
        // If folderName starts with bucket name, remove it
        if (folder.equals(bucket)) {
            return ""; // No subfolder, just bucket root
        } else if (folder.startsWith(bucket + "/")) {
            // Remove "bucket/" prefix
            return folderName.substring(bucket.length() + 1);
        }
        
        // Otherwise keep the full folder path
        return folderName;
    }
    
    /**
     * Ensure bucket exists, create if not
     */
    private void ensureBucketExists(String bucket) {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucket)
                            .build()
            );
            
            if (!exists) {
                // Create bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucket)
                                .build()
                );
                
                // Set bucket policy to public read
                String policy = """
                    {
                        "Version": "2012-10-17",
                        "Statement": [
                            {
                                "Effect": "Allow",
                                "Principal": {"AWS": ["*"]},
                                "Action": ["s3:GetObject"],
                                "Resource": ["arn:aws:s3:::%s/*"]
                            }
                        ]
                    }
                    """.formatted(bucket);
                
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucket)
                                .config(policy)
                                .build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking/creating bucket: " + bucket, e);
        }
    }
}
