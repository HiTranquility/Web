package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import truyen.model.Report;
import truyen.model.ReportEvidence;
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

    /** Tối đa bao nhiêu ảnh bằng chứng cho một báo cáo (ISSUE-025). */
    public static final int MAX_EVIDENCE = 3;

    /**
     * Người dùng gửi báo cáo, kèm ảnh bằng chứng nếu có.
     *
     * <p>CẢ HAI TRONG MỘT TRANSACTION. Ghi báo cáo xong mới ghi ảnh ở hai lần
     * commit rời nhau thì rớt giữa chừng là có ảnh nằm trên ổ đĩa mà không
     * dòng nào trỏ tới — không ai dọn được nữa, vì không biết file nào là rác.
     *
     * @param evidences đường dẫn + cỡ của ảnh đã lưu; rỗng hoặc null nếu không có
     * @return id của báo cáo vừa tạo, hoặc 0 khi chưa có CSDL
     */
    public int insert(Report r, List<ReportEvidence> evidences) throws SQLException {
        if (!TYPES.contains(r.getTargetType())) {
            throw new SQLException("Loại báo cáo không hợp lệ: " + r.getTargetType());
        }
        if (!Report.isValidCategory(r.getCategory(), r.getTargetType())) {
            throw new SQLException("Loại vi phạm không hợp lệ: " + r.getCategory());
        }
        List<ReportEvidence> files =
                evidences != null ? evidences : Collections.<ReportEvidence>emptyList();
        if (files.size() > MAX_EVIDENCE) {
            // Người gọi phải đếm TRƯỚC khi lưu file. Tới đây mới phát hiện thì
            // file đã nằm trên đĩa rồi — chặn ở đây chỉ là hàng rào cuối.
            throw new SQLException("Tối đa " + MAX_EVIDENCE + " ảnh bằng chứng.");
        }
        if (!DBConnection.isReady()) return 0;

        String sqlReport =
                "INSERT INTO reports (reporter_id, target_type, target_id, category, reason) "
              + "VALUES (?, ?, ?, ?, ?)";
        String sqlEvidence =
                "INSERT INTO report_evidence (report_id, file_path, file_size) "
              + "VALUES (?, ?, ?)";

        Connection con = null;
        try {
            con = DBConnection.get();
            con.setAutoCommit(false);

            int reportId;
            try (PreparedStatement ps = con.prepareStatement(
                         sqlReport, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, r.getReporterId());
                ps.setString(2, r.getTargetType());
                ps.setInt(3, r.getTargetId());
                ps.setString(4, r.getCategory());
                ps.setString(5, r.getReason());
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    reportId = keys.next() ? keys.getInt(1) : 0;
                }
            }

            if (reportId > 0 && !files.isEmpty()) {
                try (PreparedStatement ps = con.prepareStatement(sqlEvidence)) {
                    for (ReportEvidence ev : files) {
                        ps.setInt(1, reportId);
                        ps.setString(2, ev.getFilePath());
                        ps.setInt(3, ev.getFileSize());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            con.commit();
            r.setId(reportId);
            return reportId;
        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); } catch (SQLException ignored) {}
                try { con.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /** Báo cáo không kèm ảnh — giữ chữ ký cũ cho chỗ nào chưa cần ảnh. */
    public void insert(Report r) throws SQLException {
        if (r.getCategory() == null) r.setCategory("OTHER");
        insert(r, null);
    }

    /** Danh sách báo cáo cho trang quản trị (TRANG 30). */
    public List<Report> findAll(String status) throws SQLException {
        return findAll(status, null);
    }

    /**
     * Danh sách báo cáo, lọc theo trạng thái và/hoặc loại vi phạm.
     *
     * @param status   PENDING | RESOLVED | DISMISSED, hoặc null = tất cả
     * @param category một trong 8 loại, hoặc null = tất cả
     */
    public List<Report> findAll(String status, String category) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.reports(status);

        /*
         * link_story_id LÀ MẢNH GHÉP ĐỂ DỰNG LINK (ISSUE-025).
         *
         * COALESCE(s.id, c.story_id) trả về id truyện trong CẢ HAI trường hợp:
         * báo cáo truyện thì là chính nó, báo cáo bình luận thì là truyện chứa
         * bình luận đó. Trước đây câu này không lấy c.story_id, nên trang quản
         * trị không có gì để dựng link tới bình luận — admin phải tự đi mò
         * từng truyện.
         *
         * NULL khi nội dung đã bị xoá hẳn (cả hai LEFT JOIN đều hụt).
         * rs.getInt() đọc NULL ra 0, không ném lỗi; 0 là tín hiệu để JSP hiện
         * "Nội dung không còn tồn tại" thay vì một link chết.
         */
        StringBuilder sql = new StringBuilder(
            "SELECT r.*, u.display_name, u.username, "
          + "       COALESCE(s.title, LEFT(c.content, 80)) AS target_title, "
          + "       COALESCE(s.id, c.story_id)             AS link_story_id "
          + "FROM reports r "
          + "JOIN users u ON u.id = r.reporter_id "
          + "LEFT JOIN stories  s ON r.target_type = 'STORY'   AND s.id = r.target_id "
          + "LEFT JOIN comments c ON r.target_type = 'COMMENT' AND c.id = r.target_id ");

        boolean byStatus   = status   != null && STATUSES.contains(status);
        boolean byCategory = category != null && Report.CATEGORIES.contains(category);

        List<String> where = new ArrayList<>();
        if (byStatus)   where.add("r.status = ?");
        if (byCategory) where.add("r.category = ?");
        if (!where.isEmpty()) sql.append("WHERE ").append(String.join(" AND ", where)).append(' ');

        /*
         * Thứ tự ưu tiên, ba bậc:
         *   1. Việc chưa xử lý nổi lên trước.
         *   2. Trong số đó, BA LOẠI NẶNG (nội dung người lớn · bạo lực/thù
         *      ghét · lộ thông tin cá nhân) nổi tiếp. Không phải cho đẹp:
         *      mỗi giờ chậm là thêm người nhìn thấy. Spam thì để chiều xử
         *      cũng không sao.
         *   3. Còn lại thì mới trước cũ sau.
         */
        sql.append("ORDER BY r.status = 'PENDING' DESC, ")
           .append("         r.category IN ('ADULT','VIOLENCE','PRIVACY') DESC, ")
           .append("         r.created_at DESC LIMIT 200");

        List<Report> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (byStatus)   ps.setString(i++, status);
            if (byCategory) ps.setString(i++, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Report r = new Report();
                    r.setId(rs.getInt("id"));
                    r.setReporterId(rs.getInt("reporter_id"));
                    r.setTargetType(rs.getString("target_type"));
                    r.setTargetId(rs.getInt("target_id"));
                    r.setCategory(rs.getString("category"));
                    r.setReason(rs.getString("reason"));
                    r.setStatus(rs.getString("status"));
                    r.setStoryId(rs.getInt("link_story_id"));   // NULL -> 0
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

        attachEvidence(list);
        return list;
    }

    /**
     * Nạp ảnh bằng chứng cho cả danh sách bằng MỘT câu truy vấn.
     *
     * <p>Cách ngây thơ là lặp qua 200 báo cáo rồi mỗi cái một câu SELECT —
     * đúng bài toán N+1 truy vấn. Một câu {@code IN (...)} rồi gom theo
     * report_id ở tầng Java thì chỉ tốn một vòng đi về CSDL.
     */
    private void attachEvidence(List<Report> reports) throws SQLException {
        if (reports.isEmpty()) return;

        StringBuilder in = new StringBuilder();
        for (int i = 0; i < reports.size(); i++) in.append(i == 0 ? "?" : ",?");

        String sql = "SELECT id, report_id, file_path, file_size, created_at "
                   + "FROM report_evidence WHERE report_id IN (" + in + ") "
                   + "ORDER BY id";

        Map<Integer, List<ReportEvidence>> byReport = new HashMap<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < reports.size(); i++) {
                ps.setInt(i + 1, reports.get(i).getId());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReportEvidence ev = new ReportEvidence();
                    ev.setId(rs.getInt("id"));
                    ev.setReportId(rs.getInt("report_id"));
                    ev.setFilePath(rs.getString("file_path"));
                    ev.setFileSize(rs.getInt("file_size"));
                    if (rs.getTimestamp("created_at") != null) {
                        ev.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    byReport.computeIfAbsent(ev.getReportId(), k -> new ArrayList<>()).add(ev);
                }
            }
        }

        for (Report r : reports) {
            List<ReportEvidence> evs = byReport.get(r.getId());
            if (evs != null) r.setEvidences(evs);
        }
    }

    /** Đường dẫn mọi ảnh của một báo cáo — để xoá file trên đĩa trước khi xoá dòng. */
    public List<String> findEvidencePaths(int reportId) throws SQLException {
        List<String> paths = new ArrayList<>();
        if (!DBConnection.isReady()) return paths;

        String sql = "SELECT file_path FROM report_evidence WHERE report_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) paths.add(rs.getString("file_path"));
            }
        }
        return paths;
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
