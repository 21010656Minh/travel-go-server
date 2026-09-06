package api.v2.travel_social_network_server.responses.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GroupResponse {
    private UUID groupId;
    private String groupName;
    private String groupDescription;
    private String coverImageUrl;
    private Integer memberCount;
    private Boolean privacy;
    private Boolean isMember;
    private String currentUserRole; // OWNER, ADMIN, MODERATOR, MEMBER, null if not a member
    private Boolean isLocked; // true if group is locked by admin
    private String moderationReason; // reason for locking
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastActivityAt;
    
    // Số bài viết trung bình mỗi ngày (tính trong 30 ngày gần nhất)
    private Integer postsPerDay;
    
    // Thống kê hoạt động
    private Integer postsToday;           // Số bài viết hôm nay
    private Integer postsLastMonth;       // Số bài viết tháng trước
    private Integer newMembersThisWeek;   // Số thành viên mới tuần này
    
    // Thông tin bổ sung
    private String tags;                  // Tags của nhóm (ví dụ: "Tính yêu & sự lãng mạn")
    private String location;              // Vị trí (ví dụ: "Việt Nam")
    
    // Danh sách bạn bè của user hiện tại đang là thành viên của group
    private List<FriendMemberDto> friendMembers;
    
    // Danh sách admin và moderator
    private List<FriendMemberDto> adminMembers;
    private List<FriendMemberDto> moderatorMembers;
}
