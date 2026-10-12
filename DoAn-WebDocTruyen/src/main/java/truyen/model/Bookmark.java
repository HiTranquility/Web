package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Truyện người dùng TỰ BẤM lưu (bảng bookmarks).
 *
 * Trước đây bảng bookmarks gộp luôn "đang đọc tới chương mấy", nên mở một
 * chương là truyện tự nhảy vào tủ sách và "Bỏ lưu" thì mất vị trí đọc. Giờ
 * vị trí đọc nằm ở reading_progress (xem migration-027); hai trường
 * lastChapterId / lastChapterNo dưới đây chỉ được LEFT JOIN sang lúc liệt kê.
 */
public class Bookmark implements Serializable {

    private int userId;
    private int storyId;

    /**
     * Chương đang đọc dở, LEFT JOIN từ reading_progress.
     * 0 = đã lưu nhưng chưa đọc chương nào.
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

    /**
     * Phần trăm đã đọc (0-100%) để vẽ thanh tiến độ trên giao diện Tủ sách.
     */
    public int getProgressPercent() {
        if (totalChapters <= 0 || lastChapterNo <= 0) {
            return 0;
        }
        int pct = lastChapterNo * 100 / totalChapters;
        return pct > 100 ? 100 : pct;
    }

    /**
     * Mã trạng thái đọc phục vụ bộ lọc: "unread" | "reading" | "completed".
     */
    public String getReadingStatus() {
        if (!isStarted()) return "unread";
        if (lastChapterNo >= totalChapters && totalChapters > 0) return "completed";
        return "reading";
    }

    /**
     * Nhãn hiển thị trạng thái đọc trực quan.
     */
    public String getStatusBadge() {
        if (!isStarted()) return "Chưa đọc";
        if (lastChapterNo >= totalChapters && totalChapters > 0) return "Đã đọc xong";
        return "Đang đọc";
    }

    public String getInitial() {
        return (storyTitle == null || storyTitle.isEmpty())
                ? "?" : storyTitle.substring(0, 1).toUpperCase();
    }
}

