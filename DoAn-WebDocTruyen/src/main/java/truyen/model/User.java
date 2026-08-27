package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

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

    public User() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

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

    /** Cho JSP: ${currentUser.admin} — EL thử isAdmin() cho kiểu boolean. */
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    /** Bị ban thì chặn đăng nhập, nhưng truyện vẫn giữ nguyên. */
    public boolean isBanned() {
        return "BANNED".equals(status);
    }

    /** Chữ cái đầu, dùng làm avatar thay thế khi chưa có ảnh. */
    public String getInitial() {
        String n = getName();
        return (n == null || n.isEmpty()) ? "?" : n.substring(0, 1).toUpperCase();
    }
}
