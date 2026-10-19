package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tài khoản. JavaBean thuần — chỉ dữ liệu, không SQL.
 *
 * MỘT BẢNG DUY NHẤT CHO CẢ ĐỘC GIẢ LẪN TÁC GIẢ.
 * "Tác giả" không phải loại người, nó là quan hệ: ai đăng truyện thì là tác giả
 * của truyện đó (stories.author_id). Nên role chỉ có USER và ADMIN.
 */
public class User implements Serializable {

    private int id;
    private String username;
    private String email;

    /**
     * Chuỗi BĂM, không phải mật khẩu gốc.
     *
     * Đặt tên là passwordHash chứ không phải password — để khi đọc code là biết
     * ngay đây không phải chuỗi gõ vào. Đặt tên "password" rồi lỡ in ra log hay
     * hiện lên JSP thì đỡ nguy hiểm hơn nhiều so với việc tưởng nhầm.
     */
    private String passwordHash;

    private String displayName;
    private String avatarUrl;
    private String bio;

    private String role;      // USER | ADMIN
    private String status;    // ACTIVE | BANNED
    private String banReason;

    private LocalDateTime createdAt;

    /* Số truyện người này đã đăng. */
    private int storyCount;

    /** Số người đang theo dõi. Cũng là dữ liệu đi kèm, không phải cột users. */
    private int followerCount;

    public User() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    /**
     * Tài khoản này đã đặt mật khẩu chưa.
     * Tài khoản đăng ký qua Google có passwordHash là null (chưa có mật khẩu).
     */
    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.trim().isEmpty();
    }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBanReason() { return banReason; }
    public void setBanReason(String banReason) { this.banReason = banReason; }

    public int getStoryCount() { return storyCount; }
    public void setStoryCount(int storyCount) { this.storyCount = storyCount; }

    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int followerCount) { this.followerCount = followerCount; }

    /** Tổng lượt xem của tất cả truyện do tác giả này sáng tác. */
    private long totalViews;

    public long getTotalViews() { return totalViews; }
    public void setTotalViews(long totalViews) { this.totalViews = totalViews; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /**
     * Tên để hiển thị. Chưa đặt displayName thì lấy username.
     *
     * Tính ở model chứ không viết <c:if> trong JSP — JSP không nên chứa logic,
     * kể cả logic một dòng. EL gọi được vì đúng chuẩn get: ${user.name}
     */
    public String getName() {
        return (displayName == null || displayName.isEmpty()) ? username : displayName;
    }

    private List<String> roles = new ArrayList<>();
    private Set<String> permissions = new HashSet<>();

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles != null ? roles : new ArrayList<>(); }

    public Set<String> getPermissions() { return permissions; }
    public void setPermissions(Set<String> permissions) { this.permissions = permissions != null ? permissions : new HashSet<>(); }

    public boolean hasRole(String roleCode) {
        if (roleCode == null) return false;
        if (roleCode.equalsIgnoreCase(this.role)) return true;
        for (String r : roles) {
            if (roleCode.equalsIgnoreCase(r)) return true;
        }
        return false;
    }

    public boolean hasPermission(String permCode) {
        if (isAdmin() || hasRole("SUPER_ADMIN")) return true;
        return permissions.contains(permCode);
    }

    public boolean isModerator() {
        return hasRole("MODERATOR") || isAdmin();
    }

    public boolean isEditor() {
        return hasRole("EDITOR") || isAdmin();
    }

    /** Cho JSP: ${currentUser.admin} — EL thử isAdmin() cho kiểu boolean. */
    public boolean isAdmin() {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role) || hasRole("ADMIN") || hasRole("SUPER_ADMIN");
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equalsIgnoreCase(role) || hasRole("SUPER_ADMIN");
    }

    /** Bị ban thì chặn đăng nhập, nhưng truyện vẫn giữ nguyên. */
    public boolean isBanned() {
        return "BANNED".equals(status);
    }

    /** Thời hạn Hội viên VIP Premium (null hoặc quá khứ = không có VIP). */
    private LocalDateTime vipExpireAt;

    /** Tổng số chương người dùng đã đọc (được tổng hợp từ lịch sử đọc). */
    private int readChapters;

    public LocalDateTime getVipExpireAt() { return vipExpireAt; }
    public void setVipExpireAt(LocalDateTime vipExpireAt) { this.vipExpireAt = vipExpireAt; }

    public int getReadChapters() { return readChapters; }
    public void setReadChapters(int readChapters) { this.readChapters = readChapters; }

    /** Kiểm tra xem tài khoản có đang sở hữu gói Hội Viên VIP còn hiệu lực hay không. */
    public boolean isVip() {
        return vipExpireAt != null && vipExpireAt.isAfter(LocalDateTime.now());
    }

    /** Số ngày người dùng đã đồng hành cùng website kể từ ngày tạo tài khoản. */
    public int getAccountAgeDays() {
        if (createdAt == null) return 1;
        long days = java.time.temporal.ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        return Math.max(1, (int) days);
    }

    /**
     * Xác định Cấp bậc Độc giả Lâu năm (Seniority & Loyalty Tier).
     * Phân cấp:
     * - VIP: Đang sở hữu thẻ Hội Viên VIP
     * - LEGEND (Đại Tông Sư): Tuổi tài khoản >= 180 ngày HOẶC đã đọc >= 500 chương
     * - LOYAL (Tri Kỷ): Tuổi tài khoản >= 60 ngày HOẶC đã đọc >= 150 chương
     * - COMPANION (Đồng Hành): Tuổi tài khoản >= 15 ngày HOẶC đã đọc >= 30 chương
     * - NEWBIE (Tân Thủ): Người dùng mới gia nhập
     */
    public String getSeniorityTier() {
        if (isVip()) return "vip";
        int days = getAccountAgeDays();
        if (days >= 180 || readChapters >= 500) return "grandmaster";
        if (days >= 60 || readChapters >= 150)  return "confidant";
        if (days >= 15 || readChapters >= 30)   return "companion";
        return "newbie";
    }

    /** Tên hiển thị thân thiện của cấp bậc độc giả. */
    public String getTierName() {
        switch (getSeniorityTier().toLowerCase()) {
            case "vip":         return "Hội Viên VIP";
            case "grandmaster":
            case "legend":      return "Đại Tông Sư";
            case "confidant":
            case "loyal":       return "Tri Kỷ";
            case "companion":   return "Đồng Hành";
            default:            return "Tân Thủ";
        }
    }

    /** Biểu tượng đại diện của cấp bậc. */
    public String getTierIcon() {
        switch (getSeniorityTier().toLowerCase()) {
            case "vip":         return "⭐";
            case "grandmaster":
            case "legend":      return "🔥";
            case "confidant":
            case "loyal":       return "👑";
            case "companion":   return "⚔️";
            default:            return "🌱";
        }
    }

    /** Phần trăm giảm giá khi mở khóa chương VIP (% chiết khấu). */
    public int getDiscountPercent() {
        switch (getSeniorityTier().toLowerCase()) {
            case "vip":         return 25; // Giảm 25% cho VIP
            case "grandmaster":
            case "legend":      return 15; // Giảm 15% cho Đại Tông Sư
            case "confidant":
            case "loyal":       return 10; // Giảm 10% cho Tri Kỷ
            case "companion":   return 5;  // Giảm 5% cho Đồng Hành
            default:            return 0;  // Tân Thủ nguyên giá
        }
    }

    /** Hệ số nhân thưởng xu khi điểm danh hàng ngày. */
    public double getCheckinMultiplier() {
        switch (getSeniorityTier().toLowerCase()) {
            case "vip":         return 2.0; // Nhân đôi xu
            case "grandmaster":
            case "legend":      return 1.8; // Tăng 80% xu
            case "confidant":
            case "loyal":       return 1.5; // Tăng 50% xu
            case "companion":   return 1.2; // Tăng 20% xu
            default:            return 1.0;
        }
    }

    /** Tiến độ phần trăm thăng hạng tiếp theo (0 - 100%). */
    public int getNextTierProgressPercent() {
        String tier = getSeniorityTier().toLowerCase();
        if (isVip() || "grandmaster".equals(tier) || "legend".equals(tier)) return 100;
        int days = getAccountAgeDays();

        if ("newbie".equals(tier)) {
            int p1 = (int) Math.min(100, (days * 100.0) / 15);
            int p2 = (int) Math.min(100, (readChapters * 100.0) / 30);
            return Math.max(p1, p2);
        } else if ("companion".equals(tier)) {
            int p1 = (int) Math.min(100, ((days - 15) * 100.0) / (60 - 15));
            int p2 = (int) Math.min(100, ((readChapters - 30) * 100.0) / (150 - 30));
            return Math.max(p1, p2);
        } else if ("confidant".equals(tier) || "loyal".equals(tier)) {
            int p1 = (int) Math.min(100, ((days - 60) * 100.0) / (180 - 60));
            int p2 = (int) Math.min(100, ((readChapters - 150) * 100.0) / (500 - 150));
            return Math.max(p1, p2);
        }
        return 100;
    }

    /** Mục tiêu thăng hạng tiếp theo để hiển thị gợi ý. */
    public String getNextTierTargetText() {
        if (isVip()) return "Đặc quyền VIP tối thượng đang kích hoạt!";
        String tier = getSeniorityTier().toLowerCase();
        if ("grandmaster".equals(tier) || "legend".equals(tier)) return "Đã đạt Đẳng Cấp Huyền Thoại cao nhất!";
        if ("confidant".equals(tier) || "loyal".equals(tier)) return "Cần 180 ngày hoặc 500 chương đọc để thăng cấp 🔥 Đại Tông Sư";
        if ("companion".equals(tier)) return "Cần 60 ngày hoặc 150 chương đọc để thăng cấp 👑 Tri Kỷ";
        return "Cần 15 ngày hoặc 30 chương đọc để thăng cấp ⚔️ Đồng Hành";
    }

    public String getInitial() {
        String n = getName();
        return (n == null || n.isEmpty()) ? "?" : n.substring(0, 1).toUpperCase();
    }
}

