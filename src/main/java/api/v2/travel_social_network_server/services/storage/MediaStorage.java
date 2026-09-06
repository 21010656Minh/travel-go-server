package api.v2.travel_social_network_server.services.storage;

import api.v2.travel_social_network_server.entities.ContentMedia;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MediaStorage {
    String uploadFile(byte[] fileData, String folderName, String resourceType);
    List<ContentMedia> uploadMultipleFiles(List<MultipartFile> files, String folderName);
}
