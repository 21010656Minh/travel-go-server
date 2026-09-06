package api.v2.travel_social_network_server.services.oauth2;

import api.v2.travel_social_network_server.dtos.auth.GoogleAuthDto;
import api.v2.travel_social_network_server.dtos.auth.GoogleUserInfoDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;

public interface IGoogleAuthService {
    LoginResponse authenticateWithGoogle(GoogleAuthDto googleAuthDto);
    GoogleUserInfoDto getUserInfoFromGoogle(String accessToken);
    User createNewGoogleUser(GoogleUserInfoDto googleUserInfo);
}
