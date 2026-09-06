package api.v2.travel_social_network_server.services.oauth2;

import api.v2.travel_social_network_server.dtos.auth.FacebookAuthDto;
import api.v2.travel_social_network_server.dtos.auth.FacebookUserInfoDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.UserCredential;
import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.security.TokenProvider;
import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import api.v2.travel_social_network_server.utilities.observer.UserPublisherManagement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FacebookAuthService implements IFacebookAuthService {

    @Value("${facebook.oauth.app-id}")
    private String facebookAppId;

    @Value("${facebook.oauth.app-secret}")
    private String facebookAppSecret;

    @Value("${facebook.oauth.userinfo.url}")
    private String facebookUserInfoUrl;


    private final UserRepository userRepository;
    private final TokenProvider jwtGenerator;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserPublisherManagement userPublisherManagement;

    @Override
    @Transactional
    public LoginResponse authenticateWithFacebook(FacebookAuthDto facebookAuthDto) {
        try {
            FacebookUserInfoDto facebookUserInfo = getUserInfoFromFacebook(facebookAuthDto.getAccessToken());
            
            Optional<User> existingUserOpt = userRepository.findByEmail(facebookUserInfo.getEmail());
            
            User user;
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                // Kiểm tra xem user đã có Facebook credential chưa
                boolean hasFacebookCredential = user.getCredentials().stream()
                        .anyMatch(c -> c.getProvider() == ProviderTypeEnum.FACEBOOK);
                
                if (!hasFacebookCredential) {
                    // Thêm Facebook credential cho user hiện tại
                    UserCredential facebookCredential = UserCredential.builder()
                            .provider(ProviderTypeEnum.FACEBOOK)
                            .providerUserId(facebookUserInfo.getId())
                            .password(null) // Facebook OAuth không cần password
                            .user(user)
                            .build();
                    user.getCredentials().add(facebookCredential);
                    userRepository.save(user);
                }
            } else {
                // Tạo user mới
                user = createNewFacebookUser(facebookUserInfo);
            }

            // Tạo JWT token
            String jwtToken = jwtGenerator.generateToken(user, ProviderTypeEnum.FACEBOOK);

            return LoginResponse.builder()
                    .userId(user.getUserId())
                    .userName(user.getUsername())
                    .avatarImg(user.getAvatarImg())
                    .coverImg(user.getCoverImg())
                    .email(user.getEmail())
                    .token(jwtToken)
                    .role(user.getRole().name())
                    .userProfile(user.getUserProfile())
                    .provider(ProviderTypeEnum.FACEBOOK)
                    .build();

        } catch (Exception e) {
            throw new ResourceNotFoundException("Facebook authentication failed: " + e.getMessage());
        }
    }


    @Override
    public FacebookUserInfoDto getUserInfoFromFacebook(String accessToken) {
        System.out.println("Facebook Access Token: " + accessToken);
        System.out.println(facebookUserInfoUrl);
        // Bước 1: Verify access token với debug_token endpoint
        verifyFacebookAccessToken(accessToken);
        
        // Bước 2: Nếu token hợp lệ, lấy user info
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    facebookUserInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                System.out.println("Facebook User Info: " + jsonNode);
                String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
                
                System.out.println("Facebook Email: " + email);

                // Nếu không có email, tạo email tạm thời từ Facebook ID
                if (email == null || email.trim().isEmpty()) {
                    throw new ResourceNotFoundException("User email cannot be null or empty. Please ensure the Facebook app has email permission and user has granted email access.");
                }
                
                return FacebookUserInfoDto.builder()
                        .id(jsonNode.get("id").asText())
                        .name(jsonNode.get("name").asText())
                        .email(email)
                        .picture(parsePictureFromJson(jsonNode))
                        .build();
            } else {
                throw new ResourceNotFoundException("Failed to get user info from Facebook");
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error getting Facebook user info: " + e.getMessage());
        }
    }
    
    private void verifyFacebookAccessToken(String accessToken) {
        try {
            // Tạo app access token từ app_id và app_secret
            String appAccessToken = facebookAppId + "|" + facebookAppSecret;
            
            // Gọi debug_token endpoint để verify user access token
            String debugTokenUrl = "https://graph.facebook.com/debug_token?" +
                    "input_token=" + accessToken + 
                    "&access_token=" + appAccessToken;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    debugTokenUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                JsonNode dataNode = jsonNode.get("data");
                
                if (dataNode != null) {
                    boolean isValid = dataNode.get("is_valid").asBoolean();
                    String appId = dataNode.get("app_id").asText();
                    
                    System.out.println("Token validation - is_valid: " + isValid + ", app_id: " + appId);
                    
                    // Kiểm tra token có hợp lệ và app_id có khớp không
                    if (!isValid || !facebookAppId.equals(appId)) {
                        throw new ResourceNotFoundException("Invalid Facebook access token or app_id mismatch");
                    }
                } else {
                    throw new ResourceNotFoundException("Invalid response format from Facebook debug_token");
                }
            } else {
                throw new ResourceNotFoundException("Failed to verify Facebook access token");
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error verifying Facebook access token: " + e.getMessage());
        }
    }

    @Override
    public User createNewFacebookUser(FacebookUserInfoDto facebookUserInfo) {
        // Tạo UserProfile
        UserProfile userProfile = UserProfile.builder()
                .fullName(facebookUserInfo.getName())
                .build();

        // Tạo UserCredential cho Facebook
        UserCredential facebookCredential = UserCredential.builder()
                .provider(ProviderTypeEnum.FACEBOOK)
                .providerUserId(facebookUserInfo.getId())
                .password(null)
                .build();

        // Tạo User
        User user = User.builder()
                .userName(facebookUserInfo.getEmail() != null ? 
                    facebookUserInfo.getEmail().split("@")[0] : 
                    "fb_" + facebookUserInfo.getId()) // Fallback nếu không có email
                .email(facebookUserInfo.getEmail())
                .avatarImg(facebookUserInfo.getPictureUrl())
                .userProfile(userProfile)
                .credentials(List.of(facebookCredential))
                .build();

        // Set relationships
        userProfile.setUser(user);
        facebookCredential.setUser(user);

        return userRepository.save(user);
    }


    private FacebookUserInfoDto.FacebookPictureDto parsePictureFromJson(JsonNode jsonNode) {
        if (jsonNode.has("picture")) {
            JsonNode pictureNode = jsonNode.get("picture");
            if (pictureNode.has("data")) {
                JsonNode dataNode = pictureNode.get("data");
                FacebookUserInfoDto.FacebookPictureDto.FacebookPictureDataDto data = 
                    FacebookUserInfoDto.FacebookPictureDto.FacebookPictureDataDto.builder()
                        .url(dataNode.has("url") ? dataNode.get("url").asText() : null)
                        .is_silhouette(dataNode.has("is_silhouette") ? dataNode.get("is_silhouette").asBoolean() : false)
                        .build();
                
                return FacebookUserInfoDto.FacebookPictureDto.builder()
                        .data(data)
                        .build();
            }
        }
        return null;
    }
}
