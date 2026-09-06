package api.v2.travel_social_network_server.controllers.admin;

import api.v2.travel_social_network_server.dtos.admin.AdminRegisterDto;
import api.v2.travel_social_network_server.dtos.admin.AdminUpdateUserDto;
import api.v2.travel_social_network_server.dtos.admin.UpdateUserStatusDto;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.responses.auth.RegisterResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.services.auth.IAuthService;
import api.v2.travel_social_network_server.services.user.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/admin/users")
@RequiredArgsConstructor
@Slf4j
//@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - User Management", description = "Admin endpoints for user management")
@SecurityRequirement(name = "bearerAuth")
public class AdminUserController {

    private final IAuthService authService;
    private final IUserService userService;

    @PostMapping("/account")
    @Operation(
        summary = "Create user account (Admin only)",
        description = "Allows admin to create user accounts with any role (USER, ADMIN, etc.)"
    )
    public ResponseEntity<Response<RegisterResponse>> createUser(
            @Valid @RequestBody AdminRegisterDto adminRegisterDto,
            HttpServletRequest request) {
        
        log.info("Admin creating user: {} with role: {}", 
                adminRegisterDto.getEmail(), 
                adminRegisterDto.getRole());
        
        RegisterResponse response = authService.adminRegisterService(adminRegisterDto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Response.success(
                        response, 
                        request.getRequestURI(), 
                        "User created successfully with role: " + adminRegisterDto.getRole()
                ));
    }

    @PutMapping("/{userId}")
    @Operation(
        summary = "Update user information (Admin only)",
        description = "Allows admin to update user information including email, username, full name, etc."
    )
    public ResponseEntity<Response<UserResponse>> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody AdminUpdateUserDto adminUpdateUserDto,
            HttpServletRequest request) throws IOException {
        
        log.info("Admin updating user: {}", userId);
        
        UserResponse response = userService.adminUpdateUser(userId, adminUpdateUserDto);
        
        return ResponseEntity.ok(Response.success(
                response, 
                request.getRequestURI(), 
                "User updated successfully"
        ));
    }

    @PatchMapping("/{userId}/status")
    @Operation(
        summary = "Update user status (Admin only)",
        description = "Allows admin to change user status (ACTIVE, INACTIVE, BANNED)"
    )
    public ResponseEntity<Response<UserResponse>> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusDto updateUserStatusDto,
            HttpServletRequest request) {
        
        log.info("Admin updating user status: {} to {}", userId, updateUserStatusDto.getStatus());
        
        UserResponse response = userService.updateUserStatus(userId, updateUserStatusDto.getStatus());
        
        return ResponseEntity.ok(Response.success(
                response, 
                request.getRequestURI(), 
                "User status updated successfully"
        ));
    }

    @DeleteMapping("/{userId}")
    @Operation(
        summary = "Delete user (Admin only)",
        description = "Allows admin to delete a user account"
    )
    public ResponseEntity<Response<Void>> deleteUser(
            @PathVariable UUID userId,
            HttpServletRequest request) {
        
        log.info("Admin deleting user: {}", userId);
        
        userService.deleteUser(userId);
        
        return ResponseEntity.ok(Response.success(
                null, 
                request.getRequestURI(), 
                "User deleted successfully"
        ));
    }
}
