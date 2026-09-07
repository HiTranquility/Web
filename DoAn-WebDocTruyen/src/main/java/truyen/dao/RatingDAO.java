package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import truyen.util.DBConnection;
import truyen.util.DemoData;

/**
 * Chấm sao truyện.
 *
 * TẦNG: dao/ — chỗ duy nhất trong dự án viết SQL cho bảng ratings.
 *
 * ĐÂY LÀ DAO PHỨC TẠP NHẤT DỰ ÁN, vì mỗi lần chấm phải sửa HAI nơi:
 *     1. bảng ratings           — dữ liệu gốc, ai chấm mấy điểm
 *     2. stories.rating_sum/_count — bản đếm sẵn để hiển thị cho nhanh
 *
 * Hai nơi đó phải luôn khớp nhau. Nếu chỉ một trong hai chạy xong rồi mất
 * điện, ngôi sao hiển thị sẽ sai vĩnh viễn mà không ai biết. Vì vậy mọi thao
 * tác ghi ở đây đều nằm trong MỘT transaction: hoặc cả hai cùng vào, hoặc
 * không cái nào vào cả.
 */
public class RatingDAO {

    /**
     * Điểm mà một người đã chấm cho một truyện. 0 = chưa chấm.
     *
     * Dùng để trang chi tiết tô sáng đúng số sao người đó đã chọn lần trước,
     * thay vì lúc nào cũng hiện năm sao rỗng.
     */
    public int findScore(int userId, int storyId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.myRating(userId, storyId);

        String sql = "SELECT score FROM ratings WHERE user_id = ? AND story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("score") : 0;
            }
        }
    }

    /**
     * Chấm hoặc chấm lại.
     *
     * VÌ SAO TỰ TẮT autoCommit THAY VÌ ĐỂ MẶC ĐỊNH
     *   Mặc định JDBC commit sau MỖI câu lệnh. Ở đây có ba câu lệnh phải đi
     *   cùng nhau, nên phải tự cầm lái: tắt autoCommit, chạy hết, commit một
     *   lần. Có lỗi giữa chừng thì rollback trả lại nguyên trạng.
     *
     * VÌ SAO ĐỌC ĐIỂM CŨ TRƯỚC
     *   Chấm lần đầu thì rating_sum += điểm mới, rating_count += 1.
     *   Chấm LẠI thì rating_sum += (điểm mới − điểm cũ), count GIỮ NGUYÊN.
     *   Không biết điểm cũ thì không tính nổi hiệu số.
     *
     * VÌ SAO KHÔNG DÙNG "ON DUPLICATE KEY UPDATE" CHO GỌN
     *   Câu đó gộp được bước 1 và 2, nhưng vẫn không cho biết điểm cũ là bao
     *   nhiêu để cập nhật bản đếm sẵn. Viết tường minh dễ đọc hơn.
     */
    public void rate(int userId, int storyId, int score) throws SQLException {
        if (score < 1 || score > 5) {
            throw new SQLException("Điểm phải từ 1 đến 5, nhận được: " + score);
        }
        if (!DBConnection.isReady()) return;   // chế độ xem giao diện

        Connection con = null;
        try {
            con = DBConnection.get();
            con.setAutoCommit(false);

            // 1. điểm cũ (nếu có)
            int oldScore = 0;
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT score FROM ratings WHERE user_id = ? AND story_id = ?")) {
                ps.setInt(1, userId);
                ps.setInt(2, storyId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) oldScore = rs.getInt("score");
                }
            }

            // 2. ghi điểm mới
            if (oldScore == 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO ratings (user_id, story_id, score) VALUES (?, ?, ?)")) {
                    ps.setInt(1, userId);
                    ps.setInt(2, storyId);
                    ps.setInt(3, score);
                    ps.executeUpdate();
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE ratings SET score = ? WHERE user_id = ? AND story_id = ?")) {
                    ps.setInt(1, score);
                    ps.setInt(2, userId);
                    ps.setInt(3, storyId);
                    ps.executeUpdate();
                }
            }

            // 3. cập nhật bản đếm sẵn
            String sql = (oldScore == 0)
                    ? "UPDATE stories SET rating_sum = rating_sum + ?, "
                      + "rating_count = rating_count + 1 WHERE id = ?"
                    : "UPDATE stories SET rating_sum = rating_sum + ? WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, score - oldScore);
                ps.setInt(2, storyId);
                ps.executeUpdate();
            }

            con.commit();

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ignore) {
                    // Rollback hỏng thì cũng không cứu được gì thêm — ném lỗi
                    // gốc ra ngoài mới là thông tin có ích cho người sửa.
                }
            }
            throw e;

        } finally {
            if (con != null) {
                try {
                    // Trả lại chế độ mặc định TRƯỚC khi đóng. Bỏ qua bước này
                    // thì khi nào đó chuyển sang connection pool, kết nối bị
                    // trả về hồ vẫn đang ở giữa một transaction dở dang.
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignore) { }
            }
        }
    }

    /**
     * Đếm lại toàn bộ điểm từ bảng gốc và ghi đè bản đếm sẵn.
     *
     * Không dùng trong luồng bình thường. Đây là nút "sửa chữa" cho trường hợp
     * hai nơi lệch nhau vì lý do nào đó — vẫn nên có, vì mọi thiết kế phi
     * chuẩn hoá đều cần một đường quay về sự thật.
     */
    public void recountAll() throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql =
            "UPDATE stories s SET "
          + "  s.rating_sum   = (SELECT COALESCE(SUM(r.score), 0) "
          + "                    FROM ratings r WHERE r.story_id = s.id), "
          + "  s.rating_count = (SELECT COUNT(*) "
          + "                    FROM ratings r WHERE r.story_id = s.id)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
