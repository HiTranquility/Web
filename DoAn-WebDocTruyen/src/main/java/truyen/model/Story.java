package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Một truyện.
 *
 * JavaBean thuần: chỉ có dữ liệu và get/set, KHÔNG có logic truy vấn.
 * Việc đọc/ghi database là của StoryDAO. Trộn hai thứ vào một lớp là bước đầu
 * để dự án rối tung.
 *
 * Vì sao vẫn cần implements Serializable: xem ghi chú trong User.java.
 */
public class Story implements Serializable {

    private int id;
    private String title;
    private String slug;
    private String description;
    private String coverUrl;

    private int authorId;
    // Tên tác giả lấy từ JOIN sang bảng users. Để sẵn ở đây để JSP in
    // ${story.authorName} mà không phải truy vấn thêm lần nữa cho từng truyện.
    private String authorName;

    private String status;      // DRAFT | PUBLISHED | DELETED
    private String progress;    // ONGOING | COMPLETED
    private int viewCount;
    private int chapterCount;   // đếm sẵn, tránh COUNT() cho từng thẻ truyện

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Khởi tạo sẵn list rỗng thay vì để null: JSP duyệt qua nó mà gặp null là
    // lỗi, còn list rỗng thì lặp 0 vòng, an toàn tuyệt đối.
    private List<Tag> tags = new ArrayList<>();

    public Story() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public int getAuthorId() { return authorId; }
    public void setAuthorId(int authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProgress() { return progress; }
    public void setProgress(String progress) { this.progress = progress; }

    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }

    public int getChapterCount() { return chapterCount; }
    public void setChapterCount(int chapterCount) { this.chapterCount = chapterCount; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<Tag> getTags() { return tags; }
    public void setTags(List<Tag> tags) { this.tags = tags; }

    /**
     * Chữ cái đầu của tiêu đề, dùng làm bìa thay thế khi truyện chưa có ảnh.
     *
     * Đặt ở model chứ không viết trong JSP: JSP không nên chứa logic, kể cả
     * logic một dòng. EL gọi được vì đây đúng chuẩn get method -> ${story.initial}
     */
    public String getInitial() {
        return (title == null || title.isEmpty())
                ? "?"
                : title.substring(0, 1).toUpperCase();
    }

    /** true nếu truyện đã hoàn thành — để JSP chọn màu nhãn. */
    public boolean isCompleted() {
        return "COMPLETED".equals(progress);
    }
}
