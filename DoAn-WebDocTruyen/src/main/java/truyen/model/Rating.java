package truyen.model;

import java.time.LocalDateTime;

/**
 * Một lượt chấm sao: ai chấm truyện nào mấy điểm.
 *
 * TẦNG: model/ — chỉ chứa dữ liệu, không biết SQL, không biết HTTP.
 *
 * Không có trường id: khoá chính là cặp (userId, storyId). Mỗi người chấm một
 * truyện đúng một lần, chấm lại thì sửa dòng cũ.
 */
public class Rating {

    private int userId;
    private int storyId;
    private int score;          // 1..5
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
