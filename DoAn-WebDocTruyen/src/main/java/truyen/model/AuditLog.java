package truyen.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bản ghi nhật ký thao tác quản trị (ISSUE-023).
 * Lưu trữ bất biến các hành động kiểm duyệt và quản lý tài nguyên của Admin.
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    private long id;
    private Integer adminId;
    private String adminUsername;
    private String adminName;
    private String action;
    private String targetType;
    private int targetId;
    private String targetLabel;
    private String reason;
    private LocalDateTime createdAt;

    public AuditLog() {
    }

    public AuditLog(Integer adminId, String action, String targetType, int targetId, String targetLabel, String reason) {
        this.adminId = adminId;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.targetLabel = targetLabel;
        this.reason = reason;
        this.createdAt = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getAdminId() {
        return adminId;
    }

    public void setAdminId(Integer adminId) {
        this.adminId = adminId;
    }

    public String getAdminUsername() {
        return adminUsername;
    }

    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public String getTargetLabel() {
        return targetLabel;
    }

    public void setTargetLabel(String targetLabel) {
        this.targetLabel = targetLabel;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getFormattedTime() {
        return createdAt != null ? createdAt.format(FORMATTER) : "";
    }

    public String getActionLabel() {
        if (action == null) return "Không xác định";
        switch (action) {
            case "DELETE_STORY":  return "Gỡ truyện";
            case "RESTORE_STORY": return "Khôi phục truyện";
            case "BAN_USER":      return "Khoá tài khoản";
            case "UNBAN_USER":    return "Mở khoá tài khoản";
            case "HIDE_COMMENT":  return "Ẩn bình luận";
            case "SHOW_COMMENT":  return "Hiện bình luận";
            case "GRANT_COIN":    return "Cấp xu";
            case "REVOKE_COIN":   return "Thu hồi xu";
            default:              return action;
        }
    }

    public String getActionBadgeClass() {
        if (action == null) return "badge-secondary";
        switch (action) {
            case "DELETE_STORY":
            case "BAN_USER":
            case "HIDE_COMMENT":
            case "REVOKE_COIN":
                return "badge-danger";
            case "RESTORE_STORY":
            case "UNBAN_USER":
            case "SHOW_COMMENT":
            case "GRANT_COIN":
                return "badge-success";
            default:
                return "badge-primary";
        }
    }
}
