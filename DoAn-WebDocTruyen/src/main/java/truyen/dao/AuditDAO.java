package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import truyen.model.AuditLog;
import truyen.util.DBConnection;

/**
 * DAO quản lý nhật ký thao tác quản trị (ISSUE-023).
 *
 * NGUYÊN TẮC BẢO MẬT BẤT BIẾN:
 * 1. CHỈ CÓ INSERT VÀ SELECT — KHÔNG có phương thức UPDATE hoặc DELETE.
 * 2. Tự động liên kết thông tin Admin thực hiện thông qua LEFT JOIN.
 * 3. Hỗ trợ dự phòng an toàn khi chạy chế độ xem giao diện (Demo Data).
 */
public class AuditDAO {

    private static volatile boolean tableChecked = false;

    /**
     * Tự động khởi tạo bảng audit_logs nếu CSDL chưa có (Fail-safe).
     */
    public static void ensureTable() {
        if (tableChecked || !DBConnection.isReady()) return;
        tableChecked = true;
        String sql = "CREATE TABLE IF NOT EXISTS audit_logs ("
                   + "    id           BIGINT AUTO_INCREMENT PRIMARY KEY,"
                   + "    admin_id     INT NULL,"
                   + "    action       VARCHAR(50) NOT NULL,"
                   + "    target_type  VARCHAR(50) NOT NULL,"
                   + "    target_id    INT NOT NULL,"
                   + "    target_label VARCHAR(200) NULL,"
                   + "    reason       VARCHAR(500) NULL,"
                   + "    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                   + "    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL,"
                   + "    INDEX idx_audit_time (created_at DESC),"
                   + "    INDEX idx_audit_admin (admin_id, created_at DESC)"
                   + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        try (Connection con = DBConnection.get(); Statement st = con.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception ignored) {
        }
    }

    /**
     * Ghi một dòng nhật ký quản trị mới.
     */
    public boolean log(Integer adminId, String action, String targetType, int targetId, String targetLabel, String reason) {
        if (!DBConnection.isReady()) return true;
        ensureTable();

        String sql = "INSERT INTO audit_logs (admin_id, action, target_type, target_id, target_label, reason, created_at) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (adminId != null && adminId > 0) {
                ps.setInt(1, adminId);
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setString(2, action);
            ps.setString(3, targetType);
            ps.setInt(4, targetId);
            ps.setString(5, targetLabel != null && targetLabel.length() > 200 ? targetLabel.substring(0, 197) + "..." : targetLabel);
            ps.setString(6, reason != null && reason.length() > 500 ? reason.substring(0, 497) + "..." : reason);
            ps.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            // Không ngắt luồng hệ thống nếu ghi log thất bại
            return false;
        }
    }

    /**
     * Lấy danh sách nhật ký thao tác gần nhất kèm phân trang và lọc action.
     */
    public List<AuditLog> findLatest(int limit, int offset, String actionFilter) throws SQLException {
        if (!DBConnection.isReady()) {
            return Collections.emptyList();
        }
        ensureTable();

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT a.id, a.admin_id, a.action, a.target_type, a.target_id, a.target_label, ")
           .append("       a.reason, a.created_at, u.username, u.display_name ")
           .append("FROM audit_logs a ")
           .append("LEFT JOIN users u ON u.id = a.admin_id ");

        boolean hasFilter = actionFilter != null && !actionFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(actionFilter);
        if (hasFilter) {
            sql.append("WHERE a.action = ? ");
        }
        sql.append("ORDER BY a.created_at DESC, a.id DESC ")
           .append("LIMIT ? OFFSET ?");

        List<AuditLog> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int idx = 1;
            if (hasFilter) {
                ps.setString(idx++, actionFilter.trim());
            }
            ps.setInt(idx++, limit > 0 ? limit : 20);
            ps.setInt(idx, Math.max(0, offset));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    AuditLog log = new AuditLog();
                    log.setId(rs.getLong("id"));
                    int adminId = rs.getInt("admin_id");
                    if (!rs.wasNull()) {
                        log.setAdminId(adminId);
                    }
                    log.setAction(rs.getString("action"));
                    log.setTargetType(rs.getString("target_type"));
                    log.setTargetId(rs.getInt("target_id"));
                    log.setTargetLabel(rs.getString("target_label"));
                    log.setReason(rs.getString("reason"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) {
                        log.setCreatedAt(ts.toLocalDateTime());
                    }
                    log.setAdminUsername(rs.getString("username"));
                    String dname = rs.getString("display_name");
                    log.setAdminName(dname != null && !dname.isEmpty() ? dname : rs.getString("username"));

                    list.add(log);
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số dòng nhật ký (hỗ trợ phân trang).
     */
    public int count(String actionFilter) throws SQLException {
        if (!DBConnection.isReady()) return 0;
        ensureTable();

        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM audit_logs ");
        boolean hasFilter = actionFilter != null && !actionFilter.trim().isEmpty() && !"ALL".equalsIgnoreCase(actionFilter);
        if (hasFilter) {
            sql.append("WHERE action = ?");
        }

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (hasFilter) {
                ps.setString(1, actionFilter.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
