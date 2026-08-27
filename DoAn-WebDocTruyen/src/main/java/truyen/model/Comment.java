package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Bình luận.
 *
 * Gắn với TRUYỆN, không gắn với từng chương — quyết định thiết kế để giảm độ
 * phức tạp (xem README, mục "Quyết định thiết kế đáng chú ý").
 * Muốn nâng cấp sau: thêm cột chapterId cho phép NULL, NULL nghĩa là bình luận
 * ở cấp truyện.
 */
public class Comment implements Serializable {

    private int id;
    private int storyId;
    private int userId;
    private String content;

    /**
     * VISIBLE | HIDDEN.
     * Admin gỡ bình luận vi phạm nội quy thì đổi sang HIDDEN, KHÔNG xoá hẳn —
     * giữ lại làm bằng chứng khi xử lý tài khoản người viết.
     */
    private String status;

    private LocalDateTime createdAt;

    // Lấy qua JOIN sang bảng users, để hiện tên người viết mà khỏi truy vấn thêm
    private String username;
    private String displayName;

    public Comment() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    /** Tên để hiện. Chưa đặt displayName thì lấy username. */
    public String getName() {
        return (displayName == null || displayName.isEmpty()) ? username : displayName;
    }

    /** Chữ cái đầu, làm avatar thay thế. */
    public String getInitial() {
        String n = getName();
        return (n == null || n.isEmpty()) ? "?" : n.substring(0, 1).toUpperCase();
    }
}
