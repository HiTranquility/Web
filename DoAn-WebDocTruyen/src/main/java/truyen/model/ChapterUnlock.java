package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Ghi nhận độc giả mở khoá một chương VIP (ISSUE-020).
 * Mở một lần, đọc vĩnh viễn.
 */
public class ChapterUnlock implements Serializable {

    private int userId;
    private int chapterId;
    private int pricePaid;
    private LocalDateTime unlockedAt;

    public ChapterUnlock() {}

    public ChapterUnlock(int userId, int chapterId, int pricePaid) {
        this.userId = userId;
        this.chapterId = chapterId;
        this.pricePaid = pricePaid;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getPricePaid() { return pricePaid; }
    public void setPricePaid(int pricePaid) { this.pricePaid = pricePaid; }

    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
}
