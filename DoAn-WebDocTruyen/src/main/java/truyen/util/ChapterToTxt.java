package truyen.util;

import java.util.List;
import truyen.model.Chapter;
import truyen.model.Story;

/**
 * Tiện ích chuyển đổi và định dạng chương truyện / toàn bộ truyện sang định dạng .txt.
 *
 * Dùng chung giữa:
 * 1. DownloadServlet: người đọc tải nguyên bộ truyện về máy.
 * 2. DriveBackupServlet: tác giả sao lưu từng chương lên Google Drive (DocTruyen/<truyện>/001 - <chương>.txt).
 *
 * TÁCH RIÊNG theo nguyên tắc DRY (Don't Repeat Yourself) để bảo đảm cả hai nơi
 * cùng sinh ra một định dạng chuẩn, không vỡ dấu tiếng Việt.
 */
public class ChapterToTxt {

    /**
     * Ghép toàn bộ truyện thành một chuỗi văn bản .txt (dùng cho tải trọn bộ).
     */
    public static String formatStory(Story story, List<Chapter> chapters) {
        StringBuilder sb = new StringBuilder();
        if (story != null) {
            sb.append(story.getTitle() != null ? story.getTitle() : "").append("\n");
            sb.append("Tác giả: ").append(story.getAuthorName() != null ? story.getAuthorName() : "").append("\n");
            if (story.getDescription() != null && !story.getDescription().trim().isEmpty()) {
                sb.append("\n").append(story.getDescription().trim()).append("\n");
            }
        }
        sb.append("\n").append(repeat('=', 60)).append("\n\n");

        if (chapters == null || chapters.isEmpty()) {
            sb.append("(Truyện chưa có chương nào.)\n");
        } else {
            for (Chapter c : chapters) {
                sb.append("\n");
                sb.append("Chương ").append(c.getChapterNo()).append(": ")
                  .append(c.getTitle() != null ? c.getTitle().trim() : "").append("\n");
                sb.append(repeat('-', 60)).append("\n\n");
                sb.append(c.getContent() != null ? c.getContent() : "").append("\n\n");
            }
        }

        sb.append(repeat('=', 60)).append("\n");
        sb.append("Tải từ web Đọc Truyện — đồ án môn Lập trình Web\n");
        return sb.toString();
    }

    /**
     * Định dạng nội dung của một chương đơn lẻ (dùng cho sao lưu Google Drive).
     */
    public static String formatChapter(Chapter c) {
        if (c == null) return "";
        return formatChapter(c.getChapterNo(), c.getTitle(), c.getContent());
    }

    /**
     * Định dạng nội dung một chương theo số thứ tự, tiêu đề và nội dung.
     */
    public static String formatChapter(int chapterNo, String title, String content) {
        StringBuilder sb = new StringBuilder();
        sb.append("Chương ").append(chapterNo).append(": ")
          .append(title != null ? title.trim() : "").append("\n");
        sb.append(repeat('-', 60)).append("\n\n");
        sb.append(content != null ? content : "").append("\n");
        return sb.toString();
    }

    /**
     * Tạo tên file chuẩn 3 chữ số cho chương truyện khi lưu lên Google Drive:
     * Ví dụ:
     *   chapterNo = 1,  title = "Mở đầu"    -> "001 - Mở đầu.txt"
     *   chapterNo = 29, title = "Hồi kết"   -> "029 - Hồi kết.txt"
     *   chapterNo = 105, title = "Ngoại truyện" -> "105 - Ngoại truyện.txt"
     */
    public static String formatChapterFileName(int chapterNo, String title) {
        String safeTitle = sanitizeFileName(title);
        if (safeTitle.isEmpty()) {
            return String.format("%03d.txt", chapterNo);
        }
        return String.format("%03d - %s.txt", chapterNo, safeTitle);
    }

    /**
     * Khử các ký tự cấm trong tên file của hệ điều hành và Google Drive:
     *  \ / : * ? " < > |
     */
    public static String sanitizeFileName(String name) {
        if (name == null) return "";
        String clean = name.replaceAll("[\\\\/:*?\"<>|]", " ").trim();
        clean = clean.replaceAll("\\s+", " ");
        return clean;
    }

    /**
     * Lặp lại ký tự c n lần (tương thích Java 8/11/17).
     */
    public static String repeat(char c, int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }
}
