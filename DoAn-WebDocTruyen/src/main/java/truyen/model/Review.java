package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Bài đánh giá chi tiết cho truyện kèm điểm sao và nhãn spoiler (ISSUE-022).
 */
public class Review implements Serializable {

    private int id;
    private int userId;
    private int storyId;
    private String title;
    private String content;
    private boolean hasSpoiler;
    private int helpfulCount;
    private String status; // VISIBLE, HIDDEN
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Các trường lấy qua JOIN để hiển thị
    private String username;
    private String userDisplayName;
    private String userAvatar;
    private int score; // Điểm sao từ bảng ratings
    private boolean votedByMe; // Người dùng hiện tại đã bấm 'Có ích' chưa

    public Review() {
        this.status = "VISIBLE";
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isHasSpoiler() { return hasSpoiler; }
    public void setHasSpoiler(boolean hasSpoiler) { this.hasSpoiler = hasSpoiler; }

    public int getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(int helpfulCount) { this.helpfulCount = helpfulCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }

    public String getUserAvatar() { return userAvatar; }
    public void setUserAvatar(String userAvatar) { this.userAvatar = userAvatar; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public boolean isVotedByMe() { return votedByMe; }
    public void setVotedByMe(boolean votedByMe) { this.votedByMe = votedByMe; }
}
