package api.v2.travel_social_network_server.services.auth;

import api.v2.travel_social_network_server.dtos.admin.AdminRegisterDto;
import api.v2.travel_social_network_server.dtos.auth.ChangePasswordDto;
import api.v2.travel_social_network_server.dtos.auth.LoginDto;
import api.v2.travel_social_network_server.dtos.auth.RegisterDto;
import api.v2.travel_social_network_server.dtos.mail.MailDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.entities.UserCredential;
import api.v2.travel_social_network_server.entities.UserProfile;
import api.v2.travel_social_network_server.entities.PasswordResetToken;
import api.v2.travel_social_network_server.exceptions.ResourceAlreadyExistedException;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.responses.auth.LoginResponse;
import api.v2.travel_social_network_server.responses.auth.RegisterResponse;
import api.v2.travel_social_network_server.repositories.UserRepository;
import api.v2.travel_social_network_server.security.TokenProvider;
import api.v2.travel_social_network_server.security.JwtTokenProvider;
import api.v2.travel_social_network_server.services.mail.IMailService;
import api.v2.travel_social_network_server.utilities.enums.GenderTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.ProviderTypeEnum;
import api.v2.travel_social_network_server.utilities.enums.TokenTypeEnum;
import api.v2.travel_social_network_server.utilities.observer.UserPublisherManagement;
import api.v2.travel_social_network_server.utilities.observer.Subscriber;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    @Value("${api.client.url}")
    private String clientUrl;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider jwtGenerator;
    private final IMailService mailService;
    private final UserPublisherManagement userPublisherManagement;

    @Qualifier("emailSubscriber")
    private final Subscriber<User> emailSubscriber;

    @Qualifier("notificationSubscriber")
    private final Subscriber<User> notificationSubscriber;

    @PostConstruct
    public void init() {
        userPublisherManagement.subscribe(emailSubscriber);
        userPublisherManagement.subscribe(notificationSubscriber);
    }

    @Override
    @Transactional
    public RegisterResponse registerService(RegisterDto registerDto) {
        Optional<User> existingUserOpt = userRepository.findByEmail(registerDto.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();

            boolean hasLocalCredential = existingUser.getCredentials().stream()
                    .anyMatch(c -> c.getProvider() == ProviderTypeEnum.LOCAL);
            if (hasLocalCredential) {
                throw new ResourceAlreadyExistedException("User already exists with this email");
            }

            UserCredential localCredential = UserCredential.builder()
                    .provider(ProviderTypeEnum.LOCAL)
                    .password(passwordEncoder.encode(registerDto.getPassword()))
                    .user(existingUser)
                    .build();

            existingUser.getCredentials().add(localCredential);
            userRepository.save(existingUser);

            return RegisterResponse.builder()
                    .userName(existingUser.getUsername())
                    .email(existingUser.getEmail())
                    .build();
        }

        UserProfile userProfile = UserProfile.builder()
                .fullName(registerDto.getFirstName().trim() + " " + registerDto.getLastName().trim())
                .dateOfBirth(registerDto.getDateOfBirth())
                .gender(GenderTypeEnum.fromString(registerDto.getGender()))
                .build();

        UserCredential userCredential = UserCredential.builder()
                .provider(ProviderTypeEnum.LOCAL)
                .password(passwordEncoder.encode(registerDto.getPassword()))
                .build();

        User user = User.builder()
                .userName(registerDto.getUserName())
                .email(registerDto.getEmail())
                .userProfile(userProfile)
                .credentials(List.of(userCredential))
                .build();

        userProfile.setUser(user);
        userCredential.setUser(user);

        userRepository.save(user);

        return RegisterResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public RegisterResponse adminRegisterService(AdminRegisterDto adminRegisterDto) {
        // Check if user already exists
        Optional<User> existingUserOpt = userRepository.findByEmail(adminRegisterDto.getEmail());

        if (existingUserOpt.isPresent()) {
            throw new ResourceAlreadyExistedException("User already exists with this email");
        }

        // Create user profile
        UserProfile userProfile = UserProfile.builder()
                .fullName((adminRegisterDto.getFirstName() != null ? adminRegisterDto.getFirstName().trim() : "") 
                        + " " 
                        + (adminRegisterDto.getLastName() != null ? adminRegisterDto.getLastName().trim() : ""))
                .dateOfBirth(adminRegisterDto.getDateOfBirth())
                .gender(adminRegisterDto.getGender() != null ? 
                        GenderTypeEnum.fromString(adminRegisterDto.getGender()) : null)
                .build();

        // Create user credential
        UserCredential userCredential = UserCredential.builder()
                .provider(ProviderTypeEnum.LOCAL)
                .password(passwordEncoder.encode(adminRegisterDto.getPassword()))
                .build();

        // Create user with specified role
        User user = User.builder()
                .userName(adminRegisterDto.getUserName())
                .email(adminRegisterDto.getEmail())
                .role(adminRegisterDto.getRole()) // Set role from admin input
                .userProfile(userProfile)
                .credentials(List.of(userCredential))
                .build();

        userProfile.setUser(user);
        userCredential.setUser(user);

        userRepository.save(user);

        return RegisterResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .build();
    }

    @Override
    @Transactional
    public LoginResponse loginService(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not existed"));

        String jwtToken = authenticateUser(loginDto, user);
        String refreshToken = ((JwtTokenProvider) jwtGenerator).generateRefreshToken(user, ProviderTypeEnum.LOCAL);

        return LoginResponse.builder()  
                .userId(user.getUserId())
                .userName(user.getUsername())
                .avatarImg(user.getAvatarImg())
                .coverImg(user.getCoverImg())
                .email(user.getEmail())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .role(user.getRole().name())
                .userProfile(user.getUserProfile())
                .provider(ProviderTypeEnum.LOCAL)
                .build();
    }

    @Override
    @Transactional
    public void forgotPasswordService(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String token = UUID.randomUUID().toString();
        Instant expiredAt = Instant.now().plusSeconds(30 * 60);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .resetToken(token)
                .expiredAt(expiredAt)
                .build();

        user.setPasswordResetToken(resetToken);
        userRepository.save(user);

        String resetLink = String.format("%s/reset-password?token=%s", clientUrl, token);

        mailService.sendMail(MailDto.builder()
                .to(email)
                .subject("Request to reset password")
                .placeholders(Map.of("resetPasswordLink", resetLink, "linkExpirationTime", "30 minutes"))
                .templateName("change_password_template")
                .build()).join();
    }

    @Override
    @Transactional
    public void resetPasswordService(String token, ChangePasswordDto changePasswordDto) {
        if (!changePasswordDto.getNewPassword().equals(changePasswordDto.getNewPasswordConfirm())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        User user = userRepository.findByPasswordResetToken_ResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid reset token"));

        PasswordResetToken resetToken = user.getPasswordResetToken();
        if (resetToken.isUsed() || resetToken.getExpiredAt().isBefore(Instant.now())) {
            throw new ResourceNotFoundException("Reset token is expired or already used");
        }

        UserCredential localCredential = user.getCredentials().stream()
                .filter(c -> c.getProvider() == ProviderTypeEnum.LOCAL)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No LOCAL credentials found for this user"));

        localCredential.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        resetToken.setUsed(true);
        userRepository.save(user);

        // noti
        userPublisherManagement.publish(user);
    }

    private String authenticateUser(LoginDto loginDto, User user) {
        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPassword())) {
            throw new ResourceNotFoundException("Invalid password");
        }
        return jwtGenerator.generateToken(user, ProviderTypeEnum.LOCAL);
    }

    @Override
    public LoginResponse refreshTokenService(String refreshToken) {
        try {
            // Validate refresh token
            if (!jwtGenerator.validateToken(refreshToken)) {
                throw new ResourceNotFoundException("Invalid refresh token");
            }

            // Extract email from refresh token
            String email = jwtGenerator.extractEmail(refreshToken);
            
            // Decode token to check type claim
            DecodedJWT decodedJWT = JWT.decode(refreshToken);
            String tokenType = decodedJWT.getClaim("type").asString();
            
            if (!TokenTypeEnum.REFRESH.getValue().equals(tokenType)) {
                throw new ResourceNotFoundException("Token is not a refresh token");
            }

            // Get user from database
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            // Generate new tokens
            String newAccessToken = jwtGenerator.generateToken(user, ProviderTypeEnum.LOCAL);
            String newRefreshToken = ((JwtTokenProvider) jwtGenerator).generateRefreshToken(user, ProviderTypeEnum.LOCAL);

            // Return new tokens with user info
            return LoginResponse.builder()
                    .userId(user.getUserId())
                    .userName(user.getUsername())
                    .avatarImg(user.getAvatarImg())
                    .coverImg(user.getCoverImg())
                    .email(user.getEmail())
                    .token(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .role(user.getRole().name())
                    .userProfile(user.getUserProfile())
                    .provider(ProviderTypeEnum.LOCAL)
                    .build();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Failed to refresh token: " + e.getMessage());
        }
    }
}
