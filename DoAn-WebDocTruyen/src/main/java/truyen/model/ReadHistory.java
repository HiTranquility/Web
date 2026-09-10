package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Một dòng trong lịch sử đọc — TRANG 31.
 *
 * KHÁC GÌ Bookmark?
 *   Bookmark là THỦ CÔNG: người đọc chủ động bấm "☆ Lưu truyện".
 *   ReadHistory là TỰ ĐỘNG: cứ mở truyện ra là có, không cần bấm gì.
 *
 *   Hai thứ trả lời hai câu khác nhau:
 *       bookmark  -> "truyện tôi ĐỊNH đọc"
 *       lịch sử   -> "truyện tôi ĐÃ đọc"
 *   Người quên tên truyện hôm qua vừa đọc thì bookmark không cứu được, vì
 *   họ có bấm lưu đâu.
 *
 * KHÔNG PHẢI BẢN SAO CỦA Story
 *   Lớp này giữ kết quả của một câu JOIN, không phải ảnh chụp bảng stories.
 *   Ngoài thông tin truyện còn có hai thứ chỉ có nghĩa với MỘT người đọc cụ
 *   thể: lần đọc gần nhất và số lần đã mở. Nhét chúng vào Story là sai chỗ —
 *   Story dùng chung cho mọi người xem, làm gì có "lần đọc gần nhất của tôi".
 */
public class ReadHistory implements Serializable {

    private int storyId;
    private String storyTitle;
    private String coverUrl;
    private String authorName;
    private int totalChapters;

    /** Lần gần nhất người này mở truyện — MAX(viewed_at). */
    private LocalDateTime lastViewed;

    /** Đã mở truyện này bao nhiêu lần — COUNT(*) trong view_logs. */
    private int viewTimes;

    /**
     * Chương đang đọc dở, lấy từ bookmarks qua LEFT JOIN.
     *
     * 0 = có đọc truyện này nhưng CHƯA lưu, nên hệ thống không biết dừng ở
     * chương nào. view_logs chỉ ghi "đã mở truyện", không ghi tới chương mấy —
     * vị trí đọc nằm ở bookmarks.last_chapter_id.
     */
    private int lastChapterId;
    private int lastChapterNo;

    public ReadHistory() { }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public String getStoryTitle() { return storyTitle; }
    public void setStoryTitle(String storyTitle) { this.storyTitle = storyTitle; }

    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getTotalChapters() { return totalChapters; }
    public void setTotalChapters(int totalChapters) { this.totalChapters = totalChapters; }

    public LocalDateTime getLastViewed() { return lastViewed; }
    public void setLastViewed(LocalDateTime lastViewed) { this.lastViewed = lastViewed; }

    public int getViewTimes() { return viewTimes; }
    public void setViewTimes(int viewTimes) { this.viewTimes = viewTimes; }

    public int getLastChapterId() { return lastChapterId; }
    public void setLastChapterId(int lastChapterId) { this.lastChapterId = lastChapterId; }

    public int getLastChapterNo() { return lastChapterNo; }
    public void setLastChapterNo(int lastChapterNo) { this.lastChapterNo = lastChapterNo; }

    /** Có chỗ để "đọc tiếp" không — JSP chọn nút dựa vào đây. */
    public boolean isResumable() {
        return lastChapterId > 0;
    }

    /**
     * Phần trăm đã đọc, làm tròn về SỐ NGUYÊN — cho thanh tiến độ ở trang chủ.
     *
     * VÌ SAO KHÔNG TÍNH THẲNG TRONG JSP
     *   ${r.lastChapterNo * 100 / r.totalChapters} chạy được, nhưng phép chia
     *   trong EL luôn trả về Double, nên HTML nhận
     *       style="width:42.857142857142854%"
     *   Trình duyệt vẽ đúng, chỉ là 15 chữ số thập phân cho một cái vạch rộng
     *   vài chục pixel thì thừa, và xem mã nguồn trang thấy rất bẩn.
     *
     *   Chặn chia cho 0 cũng gọn hơn ở đây: truyện chưa có chương nào thì
     *   totalChapters = 0, mà trong JSP phải viết thêm một toán tử ba ngôi
     *   ngay giữa thuộc tính style.
     */
    public int getProgressPercent() {
        if (totalChapters <= 0) {
            return 0;
        }
        int pct = lastChapterNo * 100 / totalChapters;
        return pct > 100 ? 100 : pct;   // chương bị xoá bớt -> có thể vượt 100
    }

    public String getInitial() {
        return (storyTitle == null || storyTitle.isEmpty())
                ? "?" : storyTitle.substring(0, 1).toUpperCase();
    }

    /**
     * "Hôm nay" · "Hôm qua" · "3 ngày trước" · "12/9".
     *
     * Tính Ở ĐÂY chứ không phải trong JSP: JSTL không có thẻ nào làm được
     * việc này, viết bằng EL thì thành một chuỗi c:choose lồng nhau dài
     * ngoằng ngay giữa phần trình bày. Model tính sẵn, JSP chỉ in ra.
     *
     * Đây là thứ khiến trang lịch sử đọc được: mắt người tìm "hôm qua" nhanh
     * hơn nhiều so với dò một cột ngày tháng.
     */
    public String getViewedLabel() {
        if (lastViewed == null) {
            return "";
        }
        java.time.LocalDate d = lastViewed.toLocalDate();
        long days = java.time.temporal.ChronoUnit.DAYS.between(d, java.time.LocalDate.now());

        if (days <= 0) return "Hôm nay";
        if (days == 1) return "Hôm qua";
        if (days < 7)  return days + " ngày trước";
        return d.getDayOfMonth() + "/" + d.getMonthValue();
    }
}
