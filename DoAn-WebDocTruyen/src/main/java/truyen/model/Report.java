package truyen.model;

import java.time.LocalDateTime;

/**
 * Một báo cáo vi phạm do người dùng gửi.
 *
 * TẦNG: model/
 *
 * targetType + targetId là cặp "trỏ tới đâu": STORY thì targetId là id truyện,
 * COMMENT thì là id bình luận. Cách này gọi là quan hệ đa hình — tiện cho việc
 * mở rộng, nhưng đổi lại CSDL không kiểm hộ được targetId có thật hay không.
 * Xem ghi chú trong schema.sql.
 */
public class Report {

    private int id;
    private int reporterId;
    private String targetType;    // STORY | COMMENT
    private int targetId;
    private String reason;
    private String status;        // PENDING | RESOLVED | DISMISSED
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

    // Lấy kèm khi JOIN, để trang quản trị hiện được nội dung bị báo cáo
    private String reporterName;
    private String targetTitle;

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

    public boolean isPending()  { return "PENDING".equals(status); }
    public boolean isStory()    { return "STORY".equals(targetType); }

    /** Nhãn tiếng Việt cho JSP, khỏi viết chuỗi c:choose. */
    public String getStatusLabel() {
        if ("RESOLVED".equals(status))  return "Đã xử lý";
        if ("DISMISSED".equals(status)) return "Bỏ qua";
        return "Chờ xử lý";
    }
}
