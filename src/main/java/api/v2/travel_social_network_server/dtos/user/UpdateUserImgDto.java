package api.v2.travel_social_network_server.dtos.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserImgDto {
    private MultipartFile coverImg;
    private MultipartFile avatarImg;
}

