package api.v2.travel_social_network_server.services.dashboard;

import api.v2.travel_social_network_server.dtos.admin.AdminBlogDto;
import api.v2.travel_social_network_server.dtos.admin.AdminGroupDto;
import api.v2.travel_social_network_server.dtos.admin.DashboardStatsDto;
import api.v2.travel_social_network_server.dtos.admin.RecentActivityDto;
import api.v2.travel_social_network_server.dtos.admin.RecentUserDto;
import api.v2.travel_social_network_server.dtos.admin.TrafficDataDto;

import java.util.List;
import java.util.UUID;

public interface IAdminDashboardService {
    DashboardStatsDto getDashboardStats();
    List<TrafficDataDto> getTrafficData(int days);
    List<RecentUserDto> getRecentUsers(int limit);
    List<RecentActivityDto> getRecentActivities(int limit);
    List<AdminGroupDto> getAllGroups(int limit);
    List<AdminBlogDto> getAllBlogs(int limit);
    AdminBlogDto approveBlog(UUID blogId);
    AdminBlogDto rejectBlog(UUID blogId);
}
