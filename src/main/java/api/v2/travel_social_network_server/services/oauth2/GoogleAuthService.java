package api.v2.travel_social_network_server.services.oauth2;

import api.v2.travel_social_network_server.dtos.auth.GoogleAuthDto;
import api.v2.travel_social_network_server.dtos.auth.GoogleUserInfoDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.UserCredential;
import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.security.TokenProvider;
import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import api.v2.travel_social_network_server.utilities.observer.UserPublisherManagement;
import api.v2.travel_social_network_server.utilities.observer.Subscriber;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class GoogleAuthService implements IGoogleAuthService {

    @Value("${google.oauth.userinfo.url}")
    private String googleUserInfoUrl;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    private final UserRepository userRepository;
    private final TokenProvider jwtGenerator;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserPublisherManagement userPublisherManagement;

    @Transactional
    public LoginResponse authenticateWithGoogle(GoogleAuthDto googleAuthDto) {
        try {
            GoogleUserInfoDto googleUserInfo = getUserInfoFromGoogle(googleAuthDto.getAccessToken());
            
            Optional<User> existingUserOpt = userRepository.findByEmail(googleUserInfo.getEmail());
            
            User user;
            if (existingUserOpt.isPresent()) {
                user = existingUserOpt.get();
                // Kiểm tra xem user đã có Google credential chưa
                boolean hasGoogleCredential = user.getCredentials().stream()
                        .anyMatch(c -> c.getProvider() == ProviderTypeEnum.GOOGLE);
                
                if (!hasGoogleCredential) {
                    // Thêm Google credential cho user hiện tại
                    UserCredential googleCredential = UserCredential.builder()
                            .provider(ProviderTypeEnum.GOOGLE)
                            .providerUserId(googleUserInfo.getId())
                            .password(null) // Google OAuth không cần password
                            .user(user)
                            .build();
                    user.getCredentials().add(googleCredential);
                    userRepository.save(user);
                }
            } else {
                // Tạo user mới
                user = createNewGoogleUser(googleUserInfo);
            }

            // Tạo JWT token
            String jwtToken = jwtGenerator.generateToken(user, ProviderTypeEnum.GOOGLE);

            return LoginResponse.builder()
                    .userId(user.getUserId())
                    .userName(user.getUsername())
                    .avatarImg(user.getAvatarImg())
                    .coverImg(user.getCoverImg())
                    .email(user.getEmail())
                    .token(jwtToken)
                    .role(user.getRole().name())
                    .userProfile(user.getUserProfile())
                    .provider(ProviderTypeEnum.GOOGLE)
                    .build();

        } catch (Exception e) {
            throw new ResourceNotFoundException("Google authentication failed: " + e.getMessage());
        }
    }

    public GoogleUserInfoDto getUserInfoFromGoogle(String accessToken) {
        System.out.println("Google Access Token: " + accessToken);
        
        // Bước 1: Verify access token với tokeninfo endpoint
        verifyGoogleAccessToken(accessToken);
        
        // Bước 2: Nếu token hợp lệ, lấy user info
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    googleUserInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                return GoogleUserInfoDto.builder()
                        .id(jsonNode.get("id").asText())
                        .email(jsonNode.get("email").asText())
                        .name(jsonNode.get("name").asText())
                        .givenName(jsonNode.has("given_name") ? jsonNode.get("given_name").asText() : "")
                        .familyName(jsonNode.has("family_name") ? jsonNode.get("family_name").asText() : "")
                        .picture(jsonNode.has("picture") ? jsonNode.get("picture").asText() : "")
                        .build();
            } else {
                throw new RuntimeException("Failed to get user info from Google");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling Google API: " + e.getMessage());
        }
    }
    
    private void verifyGoogleAccessToken(String accessToken) {
        try {
            // Gọi Google tokeninfo endpoint để verify access token
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?access_token=" + accessToken;
            
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    tokenInfoUrl,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                
                // Kiểm tra các trường quan trọng
                String aud = jsonNode.has("aud") ? jsonNode.get("aud").asText() : null;
                String email = jsonNode.has("email") ? jsonNode.get("email").asText() : null;
                String expiresIn = jsonNode.has("expires_in") ? jsonNode.get("expires_in").asText() : null;
                
                System.out.println("Token validation - aud: " + aud + ", email: " + email + ", expires_in: " + expiresIn);
                
                // Kiểm tra client_id (aud) có khớp không
                if (aud == null || !googleClientId.equals(aud)) {
                    throw new ResourceNotFoundException("Invalid Google access token or client_id mismatch");
                }
                
                // Kiểm tra token có hết hạn không
                if (expiresIn != null) {
                    try {
                        long expiresInSeconds = Long.parseLong(expiresIn);
                        if (expiresInSeconds <= 0) {
                            throw new ResourceNotFoundException("Google access token has expired");
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Warning: Could not parse expires_in: " + expiresIn);
                    }
                }
                
                // Kiểm tra có email không (required cho user info)
                if (email == null || email.trim().isEmpty()) {
                    throw new ResourceNotFoundException("Google access token does not contain email scope");
                }
                
            } else {
                throw new ResourceNotFoundException("Failed to verify Google access token");
            }
        } catch (Exception e) {
            throw new ResourceNotFoundException("Error verifying Google access token: " + e.getMessage());
        }
    }

    public User createNewGoogleUser(GoogleUserInfoDto googleUserInfo) {
        System.out.println(googleUserInfo);
        // Tạo UserProfile
        UserProfile userProfile = UserProfile.builder()
                .fullName(googleUserInfo.getName())
                .build();

        // Tạo UserCredential cho Google
        UserCredential googleCredential = UserCredential.builder()
                .provider(ProviderTypeEnum.GOOGLE)
                .providerUserId(googleUserInfo.getId())
                .password(null)
                .build();

        // Tạo User
        User user = User.builder()
                .userName(googleUserInfo.getEmail().split("@")[0]) // Sử dụng email prefix làm username
                .email(googleUserInfo.getEmail())
                .avatarImg(googleUserInfo.getPicture())
                .userProfile(userProfile)
                .credentials(List.of(googleCredential))
                .build();

        // Set relationships
        userProfile.setUser(user);
        googleCredential.setUser(user);

        return userRepository.save(user);
    }

}
