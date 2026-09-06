package api.v2.travel_social_network_server.services.oauth2;

import api.v2.travel_social_network_server.dtos.auth.FacebookAuthDto;
import api.v2.travel_social_network_server.dtos.auth.FacebookUserInfoDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;

public interface IFacebookAuthService {
    LoginResponse authenticateWithFacebook(FacebookAuthDto facebookAuthDto);
    FacebookUserInfoDto getUserInfoFromFacebook(String accessToken);
    User createNewFacebookUser(FacebookUserInfoDto facebookUserInfo);
}
