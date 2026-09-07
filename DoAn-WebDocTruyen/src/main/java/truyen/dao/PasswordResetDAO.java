package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import truyen.util.DBConnection;

/**
 * Vé đặt lại mật khẩu.
 *
 * TẦNG: dao/
 *
 * VỀ VIỆC KHÔNG GỬI EMAIL
 *   Luồng thật là: người dùng nhập email → hệ thống gửi một đường dẫn chứa
 *   token → bấm vào đó mới đổi được mật khẩu. Đồ án không có máy chủ gửi thư,
 *   nên AuthServlet hiện thẳng đường dẫn đó ra màn hình.
 *
 *   Phần CÓ Ý NGHĨA của bài học vẫn còn nguyên: token ngẫu nhiên đủ dài, có
 *   hạn dùng, dùng một lần. Chỉ mỗi khâu vận chuyển là bị lược. Trong hệ
 *   thống thật, hiện token ra màn hình như vậy là lỗ hổng nghiêm trọng —
 *   ai mở trang cũng đổi được mật khẩu của người khác.
 */
public class PasswordResetDAO {

    /** Vé sống 30 phút. Đủ để mở hộp thư, không đủ để quên rồi lộ. */
    public static final int VALID_MINUTES = 30;

    /**
     * Tạo vé mới.
     *
     * Huỷ mọi vé cũ chưa dùng của người này trước. Nếu không, xin đặt lại năm
     * lần thì có năm vé cùng hiệu lực — chỉ cần một cái lọt ra ngoài là đủ.
     */
    public void create(String token, int userId) throws SQLException {
        if (!DBConnection.isReady()) return;

        try (Connection con = DBConnection.get()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE password_resets SET used_at = NOW() "
                  + "WHERE user_id = ? AND used_at IS NULL")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO password_resets (token, user_id, expires_at) "
                  + "VALUES (?, ?, ?)")) {
                ps.setString(1, token);
                ps.setInt(2, userId);
                ps.setTimestamp(3, Timestamp.valueOf(
                        LocalDateTime.now().plusMinutes(VALID_MINUTES)));
                ps.executeUpdate();
            }
        }
    }

    /**
     * Đổi vé lấy id người dùng. Trả 0 nếu vé sai, hết hạn, hoặc đã dùng.
     *
     * Ba điều kiện kiểm CÙNG MỘT LÚC trong câu SQL, không tách ra để báo lỗi
     * chi tiết hơn. "Token này đã dùng rồi" là một câu trả lời hữu ích cho
     * người quên mật khẩu, nhưng cũng hữu ích y hệt cho người đang dò token.
     */
    public int findValidUserId(String token) throws SQLException {
        if (!DBConnection.isReady()) return 0;

        String sql = "SELECT user_id FROM password_resets "
                   + "WHERE token = ? AND used_at IS NULL AND expires_at > NOW()";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("user_id") : 0;
            }
        }
    }

    /** Đánh dấu đã dùng. Gọi NGAY sau khi đổi mật khẩu thành công. */
    public void markUsed(String token) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "UPDATE password_resets SET used_at = NOW() WHERE token = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }
}
