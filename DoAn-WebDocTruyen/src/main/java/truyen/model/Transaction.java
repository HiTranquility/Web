package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Đại diện cho một giao dịch chuyển xu ảo ủng hộ tác giả (ISSUE-008).
 * Bảng: transactions.
 */
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private Integer fromUserId;
    private int toUserId;
    private Integer storyId;
    private int amount;
    private String message;
    private LocalDateTime createdAt;

    // Các trường kèm theo khi JOIN bảng
    private String fromUsername;
    private String toUsername;
    private String storyTitle;

    public Transaction() {
    }

    public Transaction(int id, Integer fromUserId, int toUserId, Integer storyId, int amount, String message) {
        this.id = id;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.storyId = storyId;
        this.amount = amount;
        this.message = message;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Integer fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getToUserId() {
        return toUserId;
    }

    public void setToUserId(int toUserId) {
        this.toUserId = toUserId;
    }

    public Integer getStoryId() {
        return storyId;
    }

    public void setStoryId(Integer storyId) {
        this.storyId = storyId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFromUsername() {
        return fromUsername;
    }

    public void setFromUsername(String fromUsername) {
        this.fromUsername = fromUsername;
    }

    public String getToUsername() {
        return toUsername;
    }

    public void setToUsername(String toUsername) {
        this.toUsername = toUsername;
    }

    public String getStoryTitle() {
        return storyTitle;
    }

    public void setStoryTitle(String storyTitle) {
        this.storyTitle = storyTitle;
    }
}
