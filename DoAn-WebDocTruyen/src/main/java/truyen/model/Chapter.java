package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/** Một chương của truyện. JavaBean thuần — chỉ dữ liệu, không SQL. */
public class Chapter implements Serializable {

    private int id;
    private int storyId;

    /**
     * Số thứ tự trong truyện (1, 2, 3...).
     * Sắp xếp theo cột này, KHÔNG theo id — vì tác giả có thể chèn chương vào
     * giữa, lúc đó id mới nhưng số chương lại nhỏ.
     */
    private int chapterNo;

    private String title;

    /**
     * MEDIUMTEXT trong database, không phải TEXT.
     * TEXT chỉ chứa 64 KB; một chương dài tiếng Việt có dấu rất dễ vượt, và
     * MySQL sẽ CẮT CỤT âm thầm — không báo lỗi, chỉ mất chữ.
     */
    private String content;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** Tên truyện, lấy qua JOIN — để trang đọc hiện được mà khỏi truy vấn thêm. */
    private String storyTitle;

    public Chapter() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public int getChapterNo() { return chapterNo; }
    public void setChapterNo(int chapterNo) { this.chapterNo = chapterNo; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getStoryTitle() { return storyTitle; }
    public void setStoryTitle(String storyTitle) { this.storyTitle = storyTitle; }

    /**
     * Ước lượng số phút đọc, hiện ở đầu trang đọc.
     * Tốc độ đọc trung bình khoảng 200 từ/phút.
     *
     * Tính ở model chứ không viết trong JSP — JSP không nên chứa logic.
     * EL gọi được vì đúng chuẩn get: ${chapter.readMinutes}
     */
    public int getReadMinutes() {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        int words = content.split("\\s+").length;
        return Math.max(1, words / 200);
    }
}
