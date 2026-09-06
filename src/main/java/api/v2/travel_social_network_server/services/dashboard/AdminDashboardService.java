package api.v2.travel_social_network_server.services.dashboard;

import api.v2.travel_social_network_server.dtos.admin.AdminBlogDto;
import api.v2.travel_social_network_server.dtos.admin.AdminGroupDto;
import api.v2.travel_social_network_server.dtos.admin.DashboardStatsDto;
import api.v2.travel_social_network_server.dtos.admin.RecentActivityDto;
import api.v2.travel_social_network_server.dtos.admin.RecentUserDto;
import api.v2.travel_social_network_server.dtos.admin.TrafficDataDto;
import api.v2.travel_social_network_server.entities.*;
import api.v2.travel_social_network_server.exceptions.ResourceNotFoundException;
import api.v2.travel_social_network_server.repositories.*;
import api.v2.travel_social_network_server.utilities.enums.BlogStatusEnum;
import api.v2.travel_social_network_server.utilities.enums.StatusTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService implements IAdminDashboardService {

    private final UserRepository userRepository;
    private final TripRepository tripRepository;
    private final PostRepository postRepository;
    private final ContentModerationRepository contentModerationRepository;
    private final GroupRepository groupRepository;
    private final BlogRepository blogRepository;

    @Override
    public DashboardStatsDto getDashboardStats() {
        log.info("Fetching dashboard statistics");

        // Get current counts
        long totalUsers = userRepository.count();
        long totalTrips = tripRepository.count();
        long totalPosts = postRepository.count();
        long totalReports = contentModerationRepository.count();

        // Calculate growth rates (last 30 days vs previous 30 days)
        Instant now = Instant.now();
        Instant thirtyDaysAgo = now.minus(30, ChronoUnit.DAYS);
        Instant sixtyDaysAgo = now.minus(60, ChronoUnit.DAYS);

        // Users growth
        long recentUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .count();
        long previousUsers = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(sixtyDaysAgo.atZone(ZoneId.systemDefault()).toLocalDateTime()) 
                        && u.getCreatedAt().isBefore(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .count();
        double userGrowthRate = previousUsers > 0 ? ((recentUsers - previousUsers) * 100.0 / previousUsers) : 0;

        // Trips growth
        long recentTrips = tripRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
        long previousTrips = tripRepository.findAll().stream()
                .filter(t -> t.getCreatedAt().isAfter(sixtyDaysAgo) && t.getCreatedAt().isBefore(thirtyDaysAgo))
                .count();
        double tripGrowthRate = previousTrips > 0 ? ((recentTrips - previousTrips) * 100.0 / previousTrips) : 0;

        // Posts growth
        long recentPosts = postRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().isAfter(thirtyDaysAgo))
                .count();
        long previousPosts = postRepository.findAll().stream()
                .filter(p -> p.getCreatedAt().isAfter(sixtyDaysAgo) && p.getCreatedAt().isBefore(thirtyDaysAgo))
                .count();
        double postGrowthRate = previousPosts > 0 ? ((recentPosts - previousPosts) * 100.0 / previousPosts) : 0;

        // Reports growth
        long recentReports = contentModerationRepository.findAll().stream()
                .filter(r -> r.getModeratedAt().isAfter(thirtyDaysAgo))
                .count();
        long previousReports = contentModerationRepository.findAll().stream()
                .filter(r -> r.getModeratedAt().isAfter(sixtyDaysAgo) && r.getModeratedAt().isBefore(thirtyDaysAgo))
                .count();
        double reportGrowthRate = previousReports > 0 ? ((recentReports - previousReports) * 100.0 / previousReports) : 0;

        return DashboardStatsDto.builder()
                .totalUsers(totalUsers)
                .totalTrips(totalTrips)
                .totalPosts(totalPosts)
                .totalReports(totalReports)
                .userGrowthRate(Math.round(userGrowthRate * 100.0) / 100.0)
                .tripGrowthRate(Math.round(tripGrowthRate * 100.0) / 100.0)
                .postGrowthRate(Math.round(postGrowthRate * 100.0) / 100.0)
                .reportGrowthRate(Math.round(reportGrowthRate * 100.0) / 100.0)
                .build();
    }

    @Override
    public List<TrafficDataDto> getTrafficData(int days) {
        log.info("Fetching traffic data for {} days", days);
        
        List<TrafficDataDto> trafficData = new ArrayList<>();
        Instant now = Instant.now();
        
        // Get all users created in the last 'days' days
        List<User> users = userRepository.findAll().stream()
                .filter(u -> u.getCreatedAt().isAfter(now.minus(days, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toLocalDateTime()))
                .collect(Collectors.toList());

        // Group by day and count
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            long count = users.stream()
                    .filter(u -> {
                        LocalDate userDate = u.getCreatedAt().toLocalDate();
                        return userDate.equals(date);
                    })
                    .count();
            
            String dayName;
            if (i == 0) {
                dayName = "Hôm nay";
            } else if (i == 1) {
                dayName = "Hôm qua";
            } else {
                String[] daysOfWeek = {"Chủ Nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"};
                dayName = daysOfWeek[date.getDayOfWeek().getValue() % 7];
            }
            
            trafficData.add(TrafficDataDto.builder()
                    .name(dayName)
                    .visits(count)
                    .build());
        }
        
        return trafficData;
    }

    @Override
    public List<RecentUserDto> getRecentUsers(int limit) {
        log.info("Fetching {} recent users", limit);
        
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        return userRepository.findAll(pageRequest).getContent().stream()
                .map(user -> {
                    RecentUserDto dto = new RecentUserDto();
                    dto.setUserId(user.getUserId());
                    dto.setUserName(user.getUsername());
                    dto.setEmail(user.getEmail());
                    dto.setAvatarImg(user.getAvatarImg());                    dto.setRole(user.getRole());                    dto.setStatus(user.getStatus());
                    dto.setCreatedAt(user.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<RecentActivityDto> getRecentActivities(int limit) {
        log.info("Fetching {} recent activities", limit);
        
        List<RecentActivityDto> activities = new ArrayList<>();
        
        // Get recent users
        List<User> recentUsers = userRepository.findAll(
                PageRequest.of(0, limit / 4, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        
        for (User user : recentUsers) {
            String displayName = user.getUserProfile() != null && user.getUserProfile().getFullName() != null 
                ? user.getUserProfile().getFullName() 
                : "User";
            activities.add(RecentActivityDto.builder()
                    .title("Người dùng mới đăng ký")
                    .description(displayName + " đã tạo tài khoản mới.")
                    .timestamp(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                    .activityType("USER_REGISTERED")
                    .build());
        }
        
        // Get recent posts
        List<Post> recentPosts = postRepository.findAll(
                PageRequest.of(0, limit / 4, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        
        for (Post post : recentPosts) {
            String displayName = post.getUser().getUserProfile() != null && post.getUser().getUserProfile().getFullName() != null 
                ? post.getUser().getUserProfile().getFullName() 
                : "User";
            activities.add(RecentActivityDto.builder()
                    .title("Bài viết mới được đăng")
                    .description(displayName + " vừa chia sẻ một bài viết mới.")
                    .timestamp(post.getCreatedAt())
                    .activityType("POST_CREATED")
                    .build());
        }
        
        // Get recent reports
        List<ContentModeration> recentReports = contentModerationRepository.findAll(
                PageRequest.of(0, limit / 4, Sort.by(Sort.Direction.DESC, "moderatedAt"))
        ).getContent();
        
        for (ContentModeration report : recentReports) {
            activities.add(RecentActivityDto.builder()
                    .title("Báo cáo vi phạm")
                    .description("Có báo cáo mới về nội dung vi phạm.")
                    .timestamp(report.getModeratedAt())
                    .activityType("REPORT_SUBMITTED")
                    .build());
        }
        
        // Get recent groups
        List<Group> recentGroups = groupRepository.findAll(
                PageRequest.of(0, limit / 4, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        
        for (Group group : recentGroups) {
            // Find the creator (the first ADMIN member)
            String creatorName = "Người dùng";
            if (group.getGroupMembers() != null && !group.getGroupMembers().isEmpty()) {
                creatorName = group.getGroupMembers().stream()
                    .filter(m -> m.getRole() == api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum.ADMIN)
                    .findFirst()
                    .map(m -> {
                        User user = m.getUser();
                        return user.getUserProfile() != null && user.getUserProfile().getFullName() != null
                            ? user.getUserProfile().getFullName()
                            : "User";
                    })
                    .orElse("Người dùng");
            }
            activities.add(RecentActivityDto.builder()
                    .title("Nhóm mới được tạo")
                    .description(creatorName + " đã tạo nhóm \"" + group.getGroupName() + "\".")
                    .timestamp(group.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                    .activityType("GROUP_CREATED")
                    .build());
        }
        
        // Sort all activities by timestamp and limit
        return activities.stream()
                .sorted((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    public List<AdminGroupDto> getAllGroups(int limit) {
        log.info("Fetching all groups for admin, limit: {}", limit);
        
        List<Group> groups = groupRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        
        List<AdminGroupDto> groupDtos = new ArrayList<>();
        
        for (Group group : groups) {
            // Find creator (first ADMIN member)
            String creatorName = "Người dùng";
            if (group.getGroupMembers() != null && !group.getGroupMembers().isEmpty()) {
                creatorName = group.getGroupMembers().stream()
                    .filter(m -> m.getRole() == api.v2.travel_social_network_server.utilities.enums.MemberRoleTypeEnum.ADMIN)
                    .findFirst()
                    .map(m -> {
                        User user = m.getUser();
                        String fullName = user.getUserProfile() != null ? user.getUserProfile().getFullName() : null;
                        return fullName != null && !fullName.isEmpty() ? fullName : user.getEmail();
                    })
                    .orElse("Người dùng");
            }
            
            // Determine status based on ContentModeration
            String status = "ACTIVE";
            Boolean isLocked = false;
            String moderationReason = null;
            
            if (group.getContentModeration() != null) {
                // isActive = false means group is locked
                if (!group.getContentModeration().getIsActive()) {
                    status = "BANNED";
                    isLocked = true;
                    moderationReason = group.getContentModeration().getModerationReason();
                }
            }
            
            // Calculate posts per day (simple estimate based on recent activity)
            Integer postsPerDay = 0;
            if (group.getPosts() != null && !group.getPosts().isEmpty()) {
                LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
                long recentPosts = group.getPosts().stream()
                    .filter(p -> p.getCreatedAt().isAfter(thirtyDaysAgo.atZone(ZoneId.systemDefault()).toInstant()))
                    .count();
                postsPerDay = (int) (recentPosts / 30);
            }
            
            AdminGroupDto dto = AdminGroupDto.builder()
                    .groupId(group.getGroupId())
                    .groupName(group.getGroupName())
                    .coverImageUrl(group.getCoverImageUrl())
                    .memberCount(group.getMemberCount())
                    .createdBy(creatorName)
                    .status(status)
                    .postsPerDay(postsPerDay)
                    .tags(group.getTags())
                    .isLocked(isLocked)
                    .moderationReason(moderationReason)
                    .createdAt(group.getCreatedAt())
                    .build();
            
            groupDtos.add(dto);
        }
        
        return groupDtos;
    }

    @Override
    public List<AdminBlogDto> getAllBlogs(int limit) {
        log.info("Fetching all blogs for admin with limit: {}", limit);
        
        List<Blog> blogs = blogRepository.findAll(
            PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
        
        List<AdminBlogDto> blogDtos = new ArrayList<>();
        
        for (Blog blog : blogs) {
            // Get author name
            String authorName = "Unknown";
            if (blog.getUser() != null) {
                User author = blog.getUser();
                if (author.getUserProfile() != null && author.getUserProfile().getFullName() != null 
                    && !author.getUserProfile().getFullName().isEmpty()) {
                    authorName = author.getUserProfile().getFullName();
                } else {
                    authorName = author.getEmail();
                }
            }
            
            // Get main category from tags
            String category = null;
            if (blog.getTags() != null && !blog.getTags().isEmpty()) {
                category = blog.getTags().get(0).getTitle();
            }
            
            AdminBlogDto dto = AdminBlogDto.builder()
                    .blogId(blog.getBlogId())
                    .title(blog.getTitle())
                    .thumbnailUrl(blog.getThumbnailUrl())
                    .authorName(authorName)
                    .authorId(blog.getUser() != null ? blog.getUser().getUserId() : null)
                    .status(blog.getStatus().name())
                    .viewCount(blog.getViewCount())
                    .averageRating(blog.getAverageRating())
                    .totalRatings(blog.getTotalRatings())
                    .category(category)
                    .createdAt(blog.getCreatedAt())
                    .publishedAt(blog.getPublishedAt())
                    .build();
            
            blogDtos.add(dto);
        }
        
        return blogDtos;
    }

    @Override
    @Transactional
    public AdminBlogDto approveBlog(UUID blogId) {
        log.info("Approving blog with ID: {}", blogId);
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + blogId));
        
        blog.setStatus(BlogStatusEnum.PUBLISHED);
        blog.setPublishedAt(Instant.now());
        blog = blogRepository.save(blog);
        
        // Get author name
        String authorName = "Unknown";
        if (blog.getUser() != null) {
            User author = blog.getUser();
            if (author.getUserProfile() != null && author.getUserProfile().getFullName() != null 
                && !author.getUserProfile().getFullName().isEmpty()) {
                authorName = author.getUserProfile().getFullName();
            } else {
                authorName = author.getEmail();
            }
        }
        
        // Get main category
        String category = null;
        if (blog.getTags() != null && !blog.getTags().isEmpty()) {
            category = blog.getTags().get(0).getTitle();
        }
        
        return AdminBlogDto.builder()
                .blogId(blog.getBlogId())
                .title(blog.getTitle())
                .thumbnailUrl(blog.getThumbnailUrl())
                .authorName(authorName)
                .authorId(blog.getUser() != null ? blog.getUser().getUserId() : null)
                .status(blog.getStatus().name())
                .viewCount(blog.getViewCount())
                .averageRating(blog.getAverageRating())
                .totalRatings(blog.getTotalRatings())
                .category(category)
                .createdAt(blog.getCreatedAt())
                .publishedAt(blog.getPublishedAt())
                .build();
    }

    @Override
    @Transactional
    public AdminBlogDto rejectBlog(UUID blogId) {
        log.info("Rejecting blog with ID: {}", blogId);
        
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog not found with id: " + blogId));
        
        blog.setStatus(BlogStatusEnum.ARCHIVED);
        blog = blogRepository.save(blog);
        
        // Get author name
        String authorName = "Unknown";
        if (blog.getUser() != null) {
            User author = blog.getUser();
            if (author.getUserProfile() != null && author.getUserProfile().getFullName() != null 
                && !author.getUserProfile().getFullName().isEmpty()) {
                authorName = author.getUserProfile().getFullName();
            } else {
                authorName = author.getEmail();
            }
        }
        
        // Get main category
        String category = null;
        if (blog.getTags() != null && !blog.getTags().isEmpty()) {
            category = blog.getTags().get(0).getTitle();
        }
        
        return AdminBlogDto.builder()
                .blogId(blog.getBlogId())
                .title(blog.getTitle())
                .thumbnailUrl(blog.getThumbnailUrl())
                .authorName(authorName)
                .authorId(blog.getUser() != null ? blog.getUser().getUserId() : null)
                .status(blog.getStatus().name())
                .viewCount(blog.getViewCount())
                .averageRating(blog.getAverageRating())
                .totalRatings(blog.getTotalRatings())
                .category(category)
                .createdAt(blog.getCreatedAt())
                .publishedAt(blog.getPublishedAt())
                .build();
    }
}
