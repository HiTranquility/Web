package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Một ảnh bằng chứng đính kèm báo cáo vi phạm (ISSUE-025).
 *
 * TẦNG: model/ — JavaBean thuần, không SQL, không đụng request/response.
 */
public class ReportEvidence implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private int reportId;

    /**
     * Đường dẫn tương đối với thư mục uploads, ví dụ
     * {@code evidence/2026/09/a1b2c3d4.jpg}.
     *
     * <p>LUÔN bắt đầu bằng {@code evidence/} — đó không phải quy ước cho đẹp
     * mà là hàng rào: {@code UploadedFileServlet} nhìn tiền tố này để chặn
     * người không phải admin. Ảnh tố cáo có thể chứa ảnh chụp tin nhắn riêng
     * hoặc thông tin cá nhân của người bị tố, khác hẳn ảnh bìa truyện.
     */
    private String filePath;

    private int fileSize;
    private LocalDateTime createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getReportId() { return reportId; }
    public void setReportId(int reportId) { this.reportId = reportId; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public int getFileSize() { return fileSize; }
    public void setFileSize(int fileSize) { this.fileSize = fileSize; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** Cỡ file dạng đọc được, cho JSP khỏi phải tính. */
    public String getSizeLabel() {
        if (fileSize < 1024) return fileSize + " B";
        if (fileSize < 1024 * 1024) return (fileSize / 1024) + " KB";
        return String.format("%.1f MB", fileSize / 1024.0 / 1024.0);
    }
}
