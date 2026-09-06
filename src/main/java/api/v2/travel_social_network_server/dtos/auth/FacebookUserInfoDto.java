package api.v2.travel_social_network_server.dtos.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacebookUserInfoDto {
    private String id;
    private String name;
    private String email;
    
    @JsonProperty("picture")
    private FacebookPictureDto picture;
    
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class FacebookPictureDto {
        private FacebookPictureDataDto data;
        
        @Getter
        @Setter
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public static class FacebookPictureDataDto {
            private String url;
            private boolean is_silhouette;
        }
    }
    
    public String getPictureUrl() {
        return picture != null && picture.getData() != null ? picture.getData().getUrl() : null;
    }
}
