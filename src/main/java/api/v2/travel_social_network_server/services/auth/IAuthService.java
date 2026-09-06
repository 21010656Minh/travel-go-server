package api.v2.travel_social_network_server.services.auth;

import api.v2.travel_social_network_server.dtos.admin.AdminRegisterDto;
import api.v2.travel_social_network_server.dtos.auth.ChangePasswordDto;
import api.v2.travel_social_network_server.dtos.auth.LoginDto;
import api.v2.travel_social_network_server.dtos.auth.RegisterDto;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;
import api.v2.travel_social_network_server.responses.auth.RegisterResponse;

public interface IAuthService {
    RegisterResponse registerService(RegisterDto registerDto);
    RegisterResponse adminRegisterService(AdminRegisterDto adminRegisterDto);
    LoginResponse loginService(LoginDto loginDto);
    LoginResponse refreshTokenService(String refreshToken);
    void forgotPasswordService(String email);
    void resetPasswordService(String token, ChangePasswordDto changePasswordDto);
}