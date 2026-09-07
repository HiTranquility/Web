package truyen.model;

import java.time.LocalDateTime;

/**
 * Quan hệ theo dõi: followerId theo dõi authorId.
 *
 * TẦNG: model/
 *
 * Các trường author* KHÔNG có trong bảng follows. Chúng do câu JOIN sang users
 * đổ sang, để trang "Đang theo dõi" hiện được tên tác giả mà không phải hỏi
 * thêm một câu SQL cho từng dòng — đúng bài toán N+1 query.
 */
public class Follow {

    private int followerId;
    private int authorId;
    private LocalDateTime createdAt;

    // Lấy kèm từ users khi JOIN
    private String authorName;
    private String authorUsername;
    private int authorStoryCount;

    public int getFollowerId() { return followerId; }
    public void setFollowerId(int followerId) { this.followerId = followerId; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public int getAuthorStoryCount() { return authorStoryCount; }
    public void setAuthorStoryCount(int authorStoryCount) { this.authorStoryCount = authorStoryCount; }

    /** Chữ cái đầu cho ảnh đại diện chữ. */
    public String getInitial() {
        return (authorName == null || authorName.isEmpty())
                ? "?" : authorName.substring(0, 1).toUpperCase();
    }
}
