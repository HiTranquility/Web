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

    /** null = bình luận gốc; có giá trị = đang trả lời bình luận đó. */
    private Integer parentId;

    /*
     * Các trả lời của bình luận này.
     *
     * KHÔNG phải cột trong CSDL. CommentDAO đọc danh sách phẳng từ một câu
     * SQL rồi tự xếp thành cây trong bộ nhớ — xem findByStory().
     *
     * Khởi tạo sẵn danh sách rỗng chứ không để null: JSP viết
     * <c:forEach items="${cm.replies}"> mà gặp null thì JSTL bỏ qua êm, nhưng
     * mọi đoạn code Java đụng vào sẽ nổ NullPointerException.
     */
    private java.util.List<Comment> replies = new java.util.ArrayList<>();

    // Lấy qua JOIN sang bảng users, để hiện tên người viết mà khỏi truy vấn thêm
    private String username;
    private String displayName;

    /** Ten truyen — lay kem khi JOIN, chi trang quan tri dung. */
    private String storyTitle;

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

    public Integer getParentId() { return parentId; }
    public void setParentId(Integer parentId) { this.parentId = parentId; }

    public java.util.List<Comment> getReplies() { return replies; }
    public void setReplies(java.util.List<Comment> replies) { this.replies = replies; }

    /** Có phải trả lời không — để JSP khỏi so sánh null. */
    public boolean isReply() { return parentId != null; }

    public int getReplyCount() { return replies.size(); }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getStoryTitle() { return storyTitle; }
    public void setStoryTitle(String storyTitle) { this.storyTitle = storyTitle; }

    public boolean isHidden() { return "HIDDEN".equals(status); }

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
