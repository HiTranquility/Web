package truyen.model;

import java.time.LocalDateTime;

/**
 * Một thông báo gửi tới một người dùng.
 *
 * TẦNG: model/
 *
 * message được sinh SẴN lúc tạo thông báo, không ghép lúc hiển thị.
 * Lý do: tên truyện lúc gửi thông báo có thể khác tên truyện hôm nay. Thông
 * báo là ảnh chụp một thời điểm, nó phải giữ nguyên câu chữ của thời điểm đó.
 */
public class Notification {

    private int id;
    private int userId;
    private Integer storyId;      // Integer để nhận NULL từ CSDL
    private Integer chapterId;
    private String type;          // NEW_CHAPTER | SYSTEM
    private String message;
    private boolean read;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public Integer getStoryId() { return storyId; }
    public void setStoryId(Integer storyId) { this.storyId = storyId; }

    public Integer getChapterId() { return chapterId; }
    public void setChapterId(Integer chapterId) { this.chapterId = chapterId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Biểu tượng theo loại — để JSP không phải viết chuỗi if. */
    public String getIcon() {
        return "NEW_CHAPTER".equals(type) ? "📖" : "🔔";
    }
}
