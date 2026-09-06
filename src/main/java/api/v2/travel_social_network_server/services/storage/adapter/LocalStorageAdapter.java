package api.v2.travel_social_network_server.services.storage.adapter;

import api.v2.travel_social_network_server.entities.ContentMedia;
import api.v2.travel_social_network_server.services.storage.MediaStorage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

@Service("localStorageAdapter")
public class LocalStorageAdapter implements MediaStorage {

    private final String baseUploadPath = "uploads"; // thư mục gốc lưu file
    private final String baseUrl = "http://localhost:8080/uploads"; // base URL public

    // --- Map contentType sang extension ---
    private String getFileExtension(String contentType) {
        return switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/gif" -> ".gif";
            case "video/mp4" -> ".mp4";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }

    // --- IO blocking method ---
    @Override
    public String uploadFile(byte[] fileData, String folderName, String contentType) {
        try {
            File folder = new File(baseUploadPath + "/" + folderName);
            if (!folder.exists()) folder.mkdirs();

            String extension = getFileExtension(contentType);
            String fileName = UUID.randomUUID() + extension;
            File file = new File(folder, fileName);

            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(fileData);
            }

            // trả về URL public
            return baseUrl + "/" + folderName + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Error saving file locally", e);
        }
    }

    // --- NIO async method ---
    public String uploadFileAsync(byte[] fileData, String folderName, String contentType) {
        try {
            File folder = new File(baseUploadPath + "/" + folderName);
            if (!folder.exists()) folder.mkdirs();

            String extension = getFileExtension(contentType);
            String fileName = UUID.randomUUID() + extension;
            File file = new File(folder, fileName);

            try (AsynchronousFileChannel channel = AsynchronousFileChannel.open(
                    file.toPath(),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {

                ByteBuffer buffer = ByteBuffer.wrap(fileData);
                Future<Integer> result = channel.write(buffer, 0);
                // result.get(); // uncomment nếu muốn đồng bộ
            }

            return baseUrl + "/" + folderName + "/" + fileName;

        } catch (IOException e) {
            throw new RuntimeException("Error saving file asynchronously", e);
        }
    }

    @Override
    public List<ContentMedia> uploadMultipleFiles(List<MultipartFile> files, String folderName) {
        List<ContentMedia> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                byte[] fileData = file.getBytes();
                MediaTypeEnum type = MediaTypeEnum.fromString(file.getContentType());

                // --- Chọn IO hoặc NIO ---
                String url = uploadFileAsync(fileData, folderName, file.getContentType());
//                String url = uploadFile(fileData, folderName, file.getContentType());

                ContentMedia contentMedia = ContentMedia.builder()
                        .url(url)
                        .type(MediaTypeEnum.fromString(file.getContentType()))
                        .build();

                uploadedFiles.add(contentMedia);

            } catch (IOException e) {
                throw new RuntimeException("Error saving file locally: " + file.getOriginalFilename(), e);
            }
        }
        return uploadedFiles;
    }
}
