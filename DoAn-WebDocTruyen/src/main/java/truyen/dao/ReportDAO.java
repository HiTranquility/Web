package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import truyen.model.Report;
import truyen.util.DBConnection;
import truyen.util.DemoData;

/**
 * Báo cáo vi phạm.
 *
 * TẦNG: dao/
 */
public class ReportDAO {

    /** Chỉ hai loại đích. Danh sách trắng, không nhận gì khác. */
    private static final List<String> TYPES = Arrays.asList("STORY", "COMMENT");

    /** Ba trạng thái hợp lệ. */
    private static final List<String> STATUSES =
            Arrays.asList("PENDING", "RESOLVED", "DISMISSED");

    /** Người dùng gửi báo cáo. */
    public void insert(Report r) throws SQLException {
        if (!TYPES.contains(r.getTargetType())) {
            throw new SQLException("Loại báo cáo không hợp lệ: " + r.getTargetType());
        }
        if (!DBConnection.isReady()) return;

        String sql = "INSERT INTO reports (reporter_id, target_type, target_id, reason) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getReporterId());
            ps.setString(2, r.getTargetType());
            ps.setInt(3, r.getTargetId());
            ps.setString(4, r.getReason());
            ps.executeUpdate();
        }
    }

    /** Danh sách báo cáo cho trang quản trị (TRANG 30). */
    public List<Report> findAll(String status) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.reports(status);

        StringBuilder sql = new StringBuilder(
            "SELECT r.*, u.display_name, u.username, "
          + "       COALESCE(s.title, LEFT(c.content, 80)) AS target_title "
          + "FROM reports r "
          + "JOIN users u ON u.id = r.reporter_id "
          + "LEFT JOIN stories  s ON r.target_type = 'STORY'   AND s.id = r.target_id "
          + "LEFT JOIN comments c ON r.target_type = 'COMMENT' AND c.id = r.target_id ");

        boolean filtered = status != null && STATUSES.contains(status);
        if (filtered) sql.append("WHERE r.status = ? ");
        sql.append("ORDER BY r.status = 'PENDING' DESC, r.created_at DESC LIMIT 200");
        // Sắp xếp: việc chưa xử lý luôn nổi lên đầu, rồi mới tới mới/cũ.

        List<Report> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            if (filtered) ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Report r = new Report();
                    r.setId(rs.getInt("id"));
                    r.setReporterId(rs.getInt("reporter_id"));
                    r.setTargetType(rs.getString("target_type"));
                    r.setTargetId(rs.getInt("target_id"));
                    r.setReason(rs.getString("reason"));
                    r.setStatus(rs.getString("status"));
                    String name = rs.getString("display_name");
                    r.setReporterName(name != null ? name : rs.getString("username"));
                    r.setTargetTitle(rs.getString("target_title"));
                    if (rs.getTimestamp("created_at") != null) {
                        r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    if (rs.getTimestamp("handled_at") != null) {
                        r.setHandledAt(rs.getTimestamp("handled_at").toLocalDateTime());
                    }
                    list.add(r);
                }
            }
        }
        return list;
    }

    /**
     * Admin đánh dấu đã xử lý hoặc bỏ qua.
     *
     * Ghi luôn thời điểm xử lý. Không ghi thì sau này không trả lời được câu
     * "báo cáo này nằm chờ bao lâu mới có người xem".
     */
    public void updateStatus(int id, String status) throws SQLException {
        if (!STATUSES.contains(status)) {
            throw new SQLException("Trạng thái không hợp lệ: " + status);
        }
        if (!DBConnection.isReady()) return;

        String sql = "UPDATE reports SET status = ?, handled_at = NOW() WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Số báo cáo còn chờ — hiện trên bảng điều khiển. */
    public int countPending() throws SQLException {
        if (!DBConnection.isReady()) return DemoData.reports("PENDING").size();

        String sql = "SELECT COUNT(*) FROM reports WHERE status = 'PENDING'";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
