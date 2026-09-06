package api.v2.travel_social_network_server.services.user;

import api.v2.travel_social_network_server.dtos.admin.AdminUpdateUserDto;
import api.v2.travel_social_network_server.dtos.user.UpdateUserDto;
import api.v2.travel_social_network_server.entities.User;
import api.v2.travel_social_network_server.utilities.enums.StatusTypeEnum;
import api.v2.travel_social_network_server.responses.PageableResponse;
import api.v2.travel_social_network_server.responses.user.UpdateUserResponse;
import api.v2.travel_social_network_server.responses.user.UserPhotosResponse;
import api.v2.travel_social_network_server.responses.user.UserResponse;
import api.v2.travel_social_network_server.responses.user.UserVideosResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IUserService {
    User getUserByUsername(String username);
    User getUserByEmail(String email);
    User getUserById(UUID userId);
    PageableResponse<UserResponse> searchUsersByKeyword(String keyword, int page, int pageSize, UUID currentUserId);
    UserResponse getUserProfile(UUID userId, UUID currentUserId) throws IOException;
    UpdateUserResponse updateUserProfile(UpdateUserDto updateUserDto, User user) throws IOException;
    List<UserResponse> searchUsersForSuggestion(String keyword, int page, int pageSize);
    PageableResponse<UserResponse> searchUsersFulltext(String keyword, int page, int pageSize, UUID currentUserId);
    UserPhotosResponse getUserPhotos(UUID userId);
    UserVideosResponse getUserVideos(UUID userId);
    
    // Admin methods
    UserResponse adminUpdateUser(UUID userId, AdminUpdateUserDto adminUpdateUserDto) throws IOException;
    UserResponse updateUserStatus(UUID userId, StatusTypeEnum status);
    void deleteUser(UUID userId);
}