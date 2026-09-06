package api.v2.travel_social_network_server.controllers.admin;

import api.v2.travel_social_network_server.dtos.admin.AdminBlogDto;
import api.v2.travel_social_network_server.dtos.admin.AdminGroupDto;
import api.v2.travel_social_network_server.dtos.admin.DashboardStatsDto;
import api.v2.travel_social_network_server.dtos.admin.RecentActivityDto;
import api.v2.travel_social_network_server.dtos.admin.RecentUserDto;
import api.v2.travel_social_network_server.dtos.admin.TrafficDataDto;
import api.v2.travel_social_network_server.responses.Response;
import api.v2.travel_social_network_server.services.dashboard.IAdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("${api.base-url}/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin - Dashboard", description = "Admin dashboard statistics and data")
@SecurityRequirement(name = "bearerAuth")
public class AdminDashboardController {

    private final IAdminDashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(
        summary = "Get dashboard statistics",
        description = "Returns overall statistics including total users, trips, posts, and reports with growth rates"
    )
    public ResponseEntity<Response<DashboardStatsDto>> getDashboardStats(HttpServletRequest request) {
        log.info("Fetching dashboard statistics");
        
        DashboardStatsDto stats = dashboardService.getDashboardStats();
        
        return ResponseEntity.ok(
            Response.success(stats, request.getRequestURI(), "Dashboard statistics retrieved successfully")
        );
    }

    @GetMapping("/traffic")
    @Operation(
        summary = "Get traffic data",
        description = "Returns traffic data (new user registrations) for the specified number of days"
    )
    public ResponseEntity<Response<List<TrafficDataDto>>> getTrafficData(
            @RequestParam(defaultValue = "7") int days,
            HttpServletRequest request) {
        log.info("Fetching traffic data for {} days", days);
        
        List<TrafficDataDto> trafficData = dashboardService.getTrafficData(days);
        
        return ResponseEntity.ok(
            Response.success(trafficData, request.getRequestURI(), "Traffic data retrieved successfully")
        );
    }

    @GetMapping("/recent-users")
    @Operation(
        summary = "Get recent users",
        description = "Returns a list of recently registered users"
    )
    public ResponseEntity<Response<List<RecentUserDto>>> getRecentUsers(
            @RequestParam(defaultValue = "5") int limit,
            HttpServletRequest request) {
        log.info("Fetching {} recent users", limit);
        
        List<RecentUserDto> recentUsers = dashboardService.getRecentUsers(limit);
        
        return ResponseEntity.ok(
            Response.success(recentUsers, request.getRequestURI(), "Recent users retrieved successfully")
        );
    }

    @GetMapping("/recent-activities")
    @Operation(
        summary = "Get recent activities",
        description = "Returns a list of recent activities including user registrations, posts, reports, and group creations"
    )
    public ResponseEntity<Response<List<RecentActivityDto>>> getRecentActivities(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletRequest request) {
        log.info("Fetching {} recent activities", limit);
        
        List<RecentActivityDto> activities = dashboardService.getRecentActivities(limit);
        
        return ResponseEntity.ok(
            Response.success(activities, request.getRequestURI(), "Recent activities retrieved successfully")
        );
    }

    @GetMapping("/groups")
    @Operation(
        summary = "Get all groups",
        description = "Returns a list of all groups with basic information for admin management"
    )
    public ResponseEntity<Response<List<AdminGroupDto>>> getAllGroups(
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request) {
        log.info("Fetching all groups for admin, limit: {}", limit);
        
        List<AdminGroupDto> groups = dashboardService.getAllGroups(limit);
        
        return ResponseEntity.ok(
            Response.success(groups, request.getRequestURI(), "Groups retrieved successfully")
        );
    }

    @GetMapping("/blogs")
    @Operation(
        summary = "Get all blogs",
        description = "Returns a list of all blogs with basic information for admin management"
    )
    public ResponseEntity<Response<List<AdminBlogDto>>> getAllBlogs(
            @RequestParam(defaultValue = "100") int limit,
            HttpServletRequest request) {
        log.info("Fetching all blogs for admin, limit: {}", limit);
        
        List<AdminBlogDto> blogs = dashboardService.getAllBlogs(limit);
        
        return ResponseEntity.ok(
            Response.success(blogs, request.getRequestURI(), "Blogs retrieved successfully")
        );
    }

    @PatchMapping("/blogs/{blogId}/approve")
    @Operation(
        summary = "Approve blog",
        description = "Approve a pending blog and change its status to PUBLISHED"
    )
    public ResponseEntity<Response<AdminBlogDto>> approveBlog(
            @PathVariable UUID blogId,
            HttpServletRequest request) {
        log.info("Approving blog with ID: {}", blogId);
        
        AdminBlogDto blog = dashboardService.approveBlog(blogId);
        
        return ResponseEntity.ok(
            Response.success(blog, request.getRequestURI(), "Blog approved successfully")
        );
    }

    @PatchMapping("/blogs/{blogId}/reject")
    @Operation(
        summary = "Reject blog",
        description = "Reject a pending blog and change its status to ARCHIVED"
    )
    public ResponseEntity<Response<AdminBlogDto>> rejectBlog(
            @PathVariable UUID blogId,
            HttpServletRequest request) {
        log.info("Rejecting blog with ID: {}", blogId);
        
        AdminBlogDto blog = dashboardService.rejectBlog(blogId);
        
        return ResponseEntity.ok(
            Response.success(blog, request.getRequestURI(), "Blog rejected successfully")
        );
    }
}
