package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Một đoạn văn người dùng tự đánh dấu để đọc lại (bảng chapter_bookmarks).
 *
 * paraNo = 0 nghĩa là đánh dấu CẢ CHƯƠNG; từ 1 trở lên là đoạn thứ mấy.
 */
public class ChapterBookmark implements Serializable {

    /** Độ dài tối đa của đoạn trích — khớp VARCHAR(200) trong CSDL, chừa chỗ cho dấu "…". */
    public static final int SNIPPET_MAX = 140;

    private int id;
    private int userId;
    private int chapterId;
    private int paraNo;
    private String snippet;
    private LocalDateTime createdAt;

    // Lấy qua JOIN — để danh sách đánh dấu hiện đủ thông tin trong MỘT truy vấn
    private int chapterNo;
    private String chapterTitle;
    private int storyId;
    private String storyTitle;
    private String coverUrl;

    public ChapterBookmark() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getChapterId() { return chapterId; }
    public void setChapterId(int chapterId) { this.chapterId = chapterId; }

    public int getParaNo() { return paraNo; }
    public void setParaNo(int paraNo) { this.paraNo = paraNo; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getChapterNo() { return chapterNo; }
    public void setChapterNo(int chapterNo) { this.chapterNo = chapterNo; }

    public String getChapterTitle() { return chapterTitle; }
    public void setChapterTitle(String chapterTitle) { this.chapterTitle = chapterTitle; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public String getStoryTitle() { return storyTitle; }
    public void setStoryTitle(String storyTitle) { this.storyTitle = storyTitle; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    /** TEXT / COMIC — JSP ghi "đoạn" hay "trang". */
    private String storyType = Story.TYPE_TEXT;
    /** Truyện tranh: đường dẫn ảnh của trang được đánh dấu (pages/...). */
    private String pageImage;

    public String getStoryType() { return storyType; }
    public void setStoryType(String storyType) { this.storyType = Story.normalizeType(storyType); }
    public boolean isComic() { return Story.TYPE_COMIC.equals(storyType); }
    public String getUnitLabel() { return isComic() ? "trang" : "đoạn"; }

    public String getPageImage() { return pageImage; }
    public void setPageImage(String pageImage) { this.pageImage = pageImage; }
    /** URL ảnh thu nhỏ để ghép sau contextPath, hoặc null nếu không có. */
    public String getPageUrl() { return pageImage == null ? null : "/uploads/" + pageImage; }

    public String getInitial() {
        return (storyTitle == null || storyTitle.isEmpty())
                ? "?" : storyTitle.substring(0, 1).toUpperCase();
    }

    /**
     * Cắt một đoạn văn thành đoạn trích ngắn để hiện trong danh sách.
     *
     * Gộp mọi khoảng trắng liên tiếp thành một dấu cách, cắt ở ranh giới TỪ
     * gần nhất (không cắt đôi một chữ tiếng Việt), rồi thêm "…" nếu bị cắt.
     */
    public static String snippetOf(String paragraph) {
        if (paragraph == null) return "";
        String t = paragraph.replaceAll("\\s+", " ").trim();
        if (t.length() <= SNIPPET_MAX) return t;

        int cut = t.lastIndexOf(' ', SNIPPET_MAX);
        if (cut < SNIPPET_MAX / 2) cut = SNIPPET_MAX;   // một "từ" dài bất thường
        return t.substring(0, cut).trim() + "…";
    }
}
