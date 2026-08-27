package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Đánh dấu truyện.
 *
 * Gộp HAI nhu cầu vào một bảng:
 *   1. "lưu truyện để đọc sau"       -> chỉ cần (userId, storyId)
 *   2. "nhớ đang đọc tới chương mấy" -> cột lastChapterId
 * Tách làm hai bảng cũng được, nhưng gộp thì ít việc hơn mà vẫn đủ dùng.
 */
public class Bookmark implements Serializable {

    private int userId;
    private int storyId;

    /**
     * 0 = đã lưu nhưng chưa đọc chương nào.
     * Cột trong database cho phép NULL, và khoá ngoại dùng ON DELETE SET NULL —
     * tác giả xoá chương đang đọc dở thì chỉ mất vị trí đọc, KHÔNG mất bookmark.
     */
    private int lastChapterId;

    private LocalDateTime createdAt;

    // Lấy qua JOIN — để trang "Truyện đã lưu" hiện đủ thông tin trong MỘT truy vấn
    private String storyTitle;
    private String storySlug;
    private String coverUrl;
    private int lastChapterNo;
    private int totalChapters;

    public Bookmark() { }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getLastChapterId() { return lastChapterId; }
    public void setLastChapterId(int lastChapterId) { this.lastChapterId = lastChapterId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getStoryTitle() { return storyTitle; }
    public void setStoryTitle(String storyTitle) { this.storyTitle = storyTitle; }

    public String getStorySlug() { return storySlug; }
    public void setStorySlug(String storySlug) { this.storySlug = storySlug; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getLastChapterNo() { return lastChapterNo; }
    public void setLastChapterNo(int lastChapterNo) { this.lastChapterNo = lastChapterNo; }

    public int getTotalChapters() { return totalChapters; }
    public void setTotalChapters(int totalChapters) { this.totalChapters = totalChapters; }

    /** Đã bắt đầu đọc chưa — để JSP chọn hiện "Đọc tiếp" hay "Bắt đầu đọc". */
    public boolean isStarted() {
        return lastChapterId > 0;
    }

    public String getInitial() {
        return (storyTitle == null || storyTitle.isEmpty())
                ? "?" : storyTitle.substring(0, 1).toUpperCase();
    }
}
