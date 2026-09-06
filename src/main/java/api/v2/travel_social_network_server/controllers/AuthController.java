package api.v2.travel_social_network_server.controllers;

import api.v2.travel_social_network_server.dtos.auth.ChangePasswordDto;
import api.v2.travel_social_network_server.dtos.auth.FacebookAuthDto;
import api.v2.travel_social_network_server.dtos.auth.GoogleAuthDto;
import api.v2.travel_social_network_server.dtos.auth.LoginDto;
import api.v2.travel_social_network_server.dtos.auth.RegisterDto;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;
import api.v2.travel_social_network_server.responses.auth.RegisterResponse;
import api.v2.travel_social_network_server.services.oauth2.FacebookAuthService;
import api.v2.travel_social_network_server.services.oauth2.GoogleAuthService;
import api.v2.travel_social_network_server.services.auth.IAuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;

import java.util.Map;

@RestController
@RequestMapping("${api.base-url}/auth")
@Tag(name = "Authentication APIs", description = "Endpoints for user authentication and authorization")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;
    private final GoogleAuthService googleAuthService;
    private final FacebookAuthService facebookAuthService;

    @Operation(summary = "Register a new user", description = "Create a new account with username, email and password")
    @PostMapping("/local/register")
    public ResponseEntity<Response<RegisterResponse>> register(@Valid @RequestBody RegisterDto registerDto, HttpServletRequest request) {
        RegisterResponse response = authService.registerService(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Response.success(response, request.getRequestURI(), "Register successfully"));
    }

    @Operation(summary = "Login user", description = "Authenticate user and return access token")
    @PostMapping("/local/login")
    public ResponseEntity<Response<LoginResponse>> login(@Valid @RequestBody LoginDto loginDto, HttpServletRequest request) {
        LoginResponse response = authService.loginService(loginDto);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Login successfully"));
    }

    @Operation(summary = "Forgot password", description = "Send reset password link to user email")
    @PostMapping("/local/forgot-password")
    public ResponseEntity<Response<String>> forgotPassword(@RequestParam String email, HttpServletRequest request) {
        authService.forgotPasswordService(email);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Reset password link sent successfully"));
    }

    @Operation(summary = "Reset password", description = "Reset user password using token from email")
    @PostMapping("/local/reset-password")
    public ResponseEntity<Response<String>> resetPassword(@RequestParam String token,
                                                          @Valid @RequestBody ChangePasswordDto changePasswordDto,
                                                          HttpServletRequest request) {
        authService.resetPasswordService(token, changePasswordDto);
        return ResponseEntity.ok(Response.success(null, request.getRequestURI(), "Password reset successfully"));
    }

    @Operation(summary = "Google OAuth Login", description = "Authenticate user with Google OAuth access token")
    @PostMapping("/google/login")
    public ResponseEntity<Response<LoginResponse>> googleLogin(@Valid @RequestBody GoogleAuthDto googleAuthDto, HttpServletRequest request) {
        LoginResponse response = googleAuthService.authenticateWithGoogle(googleAuthDto);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Google login successfully"));
    }

    @Operation(summary = "Facebook OAuth Login", description = "Authenticate user with Facebook OAuth access token")
    @PostMapping("/facebook/login")
    public ResponseEntity<Response<LoginResponse>> facebookLogin(@Valid @RequestBody FacebookAuthDto facebookAuthDto, HttpServletRequest request) {
        LoginResponse response = facebookAuthService.authenticateWithFacebook(facebookAuthDto);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Facebook login successfully"));
    }

    @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
    @PostMapping("/refresh-token")
    public ResponseEntity<Response<LoginResponse>> refreshToken(@RequestBody Map<String, String> body, HttpServletRequest request) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new ResourceNotFoundException("Refresh token is required");
        }
        LoginResponse response = authService.refreshTokenService(refreshToken);
        return ResponseEntity.ok(Response.success(response, request.getRequestURI(), "Token refreshed successfully"));
    }

}
