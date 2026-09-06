package api.v2.travel_social_network_server.responses.media;

import api.v2.travel_social_network_server.utilities.enums.MediaTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MediaUploadResponse {
    private UUID mediaId;
    private String url;
    private MediaTypeEnum type;
    private Long size;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;
}
