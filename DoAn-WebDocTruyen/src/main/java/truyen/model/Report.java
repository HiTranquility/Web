package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Một báo cáo vi phạm do người dùng gửi. */
public class Report implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Tám loại vi phạm (ISSUE-025). Thứ tự ở đây là thứ tự hiện trong form.
     *
     * <p>Dừng ở tám, không nhiều hơn: danh sách dài thì người ta chọn bừa cái
     * đầu tiên, mà số liệu bị chọn bừa còn tệ hơn không phân loại — nó nhìn
     * như số liệu thật.
     */
    public static final List<String> CATEGORIES = Arrays.asList(
            "SPAM", "ADULT", "VIOLENCE", "PRIVACY",
            "PLAGIARISM", "WRONG_INFO", "HARASSMENT", "OTHER");

    /**
     * Ba loại nặng — được trang quản trị đẩy lên đầu hàng đợi.
     *
     * <p>Không phải để cho đẹp: nội dung người lớn, kích động thù ghét và lộ
     * thông tin cá nhân là loại mà <b>mỗi giờ chậm là thêm người nhìn thấy</b>.
     * Spam thì để tới chiều xử cũng không sao.
     */
    public static final List<String> SEVERE =
            Arrays.asList("ADULT", "VIOLENCE", "PRIVACY");

    /**
     * Hai loại chỉ có nghĩa với TRUYỆN, form báo cáo bình luận không hiện.
     * Lọc danh sách theo ngữ cảnh là cách rẻ nhất để người ta chọn đúng.
     */
    private static final List<String> STORY_ONLY =
            Arrays.asList("PLAGIARISM", "WRONG_INFO");

    /** Loại chỉ có nghĩa với BÌNH LUẬN. */
    private static final List<String> COMMENT_ONLY =
            Arrays.asList("HARASSMENT");

    private int id;
    private int reporterId;
    private String targetType;    // STORY | COMMENT
    private int targetId;
    private String category;      // xem CATEGORIES
    private String reason;
    private String status;        // PENDING | RESOLVED | DISMISSED
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

    // Lấy kèm khi JOIN, để trang quản trị hiện được nội dung bị báo cáo
    private String reporterName;
    private String targetTitle;

    /**
     * Id truyện để dựng link — {@code COALESCE(s.id, c.story_id)}.
     *
     * <p>Báo cáo truyện thì là chính nó; báo cáo bình luận thì là truyện chứa
     * bình luận đó. Bằng 0 khi nội dung bị báo cáo đã bị xoá hẳn (cả hai
     * LEFT JOIN đều hụt) — trang quản trị lấy 0 làm tín hiệu để hiện
     * "Nội dung không còn tồn tại" thay vì một link chết.
     */
    private int storyId;

    /** Ảnh bằng chứng. Rỗng chứ không null, để JSP khỏi phải kiểm. */
    private List<ReportEvidence> evidences = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReporterId() { return reporterId; }
    public void setReporterId(int reporterId) { this.reporterId = reporterId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public int getTargetId() { return targetId; }
    public void setTargetId(int targetId) { this.targetId = targetId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getHandledAt() { return handledAt; }
    public void setHandledAt(LocalDateTime handledAt) { this.handledAt = handledAt; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public String getTargetTitle() { return targetTitle; }
    public void setTargetTitle(String targetTitle) { this.targetTitle = targetTitle; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public int getStoryId() { return storyId; }
    public void setStoryId(int storyId) { this.storyId = storyId; }

    public List<ReportEvidence> getEvidences() { return evidences; }
    public void setEvidences(List<ReportEvidence> evidences) {
        this.evidences = evidences != null ? evidences : new ArrayList<>();
    }

    public boolean isPending()  { return "PENDING".equals(status); }
    public boolean isStory()    { return "STORY".equals(targetType); }

    /** Nội dung bị báo cáo đã bị xoá hẳn — không dựng được link tới nó nữa. */
    public boolean isTargetGone() { return storyId <= 0; }

    /** Loại nặng, cần xử trước. Dùng ở trang quản trị để tô đỏ và xếp lên đầu. */
    public boolean isSevere() { return SEVERE.contains(category); }

    public boolean isHasEvidence() { return !evidences.isEmpty(); }

    /** Nhãn tiếng Việt cho JSP, khỏi viết chuỗi c:choose. */
    public String getStatusLabel() {
        if ("RESOLVED".equals(status))  return "Đã xử lý";
        if ("DISMISSED".equals(status)) return "Bỏ qua";
        return "Chờ xử lý";
    }

    /** Nhãn tiếng Việt của loại vi phạm. */
    public String getCategoryLabel() { return categoryLabel(category); }

    /**
     * Nhãn tiếng Việt cho một mã loại. Static để JSP dựng được ô chọn mà không
     * cần một đối tượng Report.
     */
    public static String categoryLabel(String code) {
        if (code == null) return "Khác";
        switch (code) {
            case "SPAM":        return "Spam, quảng cáo, rác";
            case "ADULT":       return "Nội dung người lớn, khiêu dâm";
            case "VIOLENCE":    return "Bạo lực, thù ghét, kích động";
            case "PRIVACY":     return "Lộ thông tin cá nhân người khác";
            case "PLAGIARISM":  return "Đạo văn, đăng lại không xin phép";
            case "WRONG_INFO":  return "Sai thể loại, tiêu đề gây hiểu nhầm";
            case "HARASSMENT":  return "Quấy rối, xúc phạm người dùng khác";
            default:            return "Khác";
        }
    }

    /**
     * Danh sách loại hợp lệ cho một kiểu đích.
     *
     * <p>Form báo cáo bình luận không hiện "Đạo văn" và "Sai thể loại" — hai
     * loại đó vô nghĩa với một dòng bình luận. Ngược lại "Quấy rối" chỉ có
     * nghĩa khi đích là bình luận.
     */
    public static List<String> categoriesFor(String targetType) {
        List<String> out = new ArrayList<>();
        boolean story = "STORY".equals(targetType);
        for (String c : CATEGORIES) {
            if (story  && COMMENT_ONLY.contains(c)) continue;
            if (!story && STORY_ONLY.contains(c))   continue;
            out.add(c);
        }
        return out;
    }

    /** Mã loại có hợp lệ với kiểu đích này không — dùng để chặn ở máy chủ. */
    public static boolean isValidCategory(String code, String targetType) {
        return code != null && categoriesFor(targetType).contains(code);
    }
}
