package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import truyen.util.DBConnection;

/**
 * Vé đặt lại mật khẩu.
 *
 * CSDL KHÔNG GIỮ VÉ, CHỈ GIỮ DẤU VÂN TAY CỦA VÉ
 *   Vé đặt lại mật khẩu CHÍNH LÀ một mật khẩu tạm: ai cầm được nó là đổi được
 *   mật khẩu tài khoản kia. Mật khẩu thì dự án đã băm rất cẩn thận rồi, mà vé
 *   lại nằm nguyên chữ trong bảng thì công sức kia mất một nửa — lộ database
 *   là chiếm được mọi tài khoản còn vé chưa hết hạn.
 *
 *   Nay chỉ lưu SHA-256 của vé. Vé thật chỉ tồn tại trong đường link gửi cho
 *   người dùng, không nơi nào khác. Có đọc được cả bảng cũng không dựng ngược
 *   lại được vé.
 *
 * VÌ SAO SHA-256 CHỨ KHÔNG PHẢI PBKDF2 NHƯ MẬT KHẨU
 *   PBKDF2 chạy chậm CÓ CHỦ Ý (120.000 vòng) để kẻ tấn công không thể thử
 *   hàng triệu mật khẩu mỗi giây. Nó cần thiết vì mật khẩu do người nghĩ ra,
 *   entropy thấp, đoán được.
 *
 *   Vé thì khác hẳn: 32 byte từ SecureRandom, tức 256 bit ngẫu nhiên thật.
 *   Không có "danh sách vé thường gặp" để mà dò. Làm chậm phép băm ở đây
 *   không thêm an toàn, chỉ làm mỗi lần bấm link chờ lâu hơn.
 *
 *   Quy tắc: bí mật do NGƯỜI đặt -> băm chậm. Bí mật do MÁY sinh ngẫu nhiên
 *   đủ dài -> băm nhanh là đủ.
 *
 * VÀ VÌ SAO KHÔNG CẦN SỬA SCHEMA
 *   SHA-256 ra 32 byte; mã base64url không đệm của 32 byte là đúng 43 ký tự —
 *   trùng khít độ dài vé cũ, nên cột CHAR(43) giữ nguyên. Cột vẫn tên "token"
 *   nhưng thứ nằm trong đó là DẤU VÂN TAY, không phải vé.
 */
public class PasswordResetDAO {

    /** Vé sống 30 phút. Đủ để mở hộp thư, không đủ để quên rồi lộ. */
    public static final int VALID_MINUTES = 30;

    /**
     * Dấu vân tay của vé — thứ THẬT SỰ được lưu và đem ra so.
     *
     * Cùng một vé luôn cho cùng một kết quả (không có muối ngẫu nhiên), nên
     * tra cứu bằng WHERE token = ? vẫn chạy. Đây chính là chỗ khác PBKDF2:
     * PBKDF2 mỗi lần băm ra một chuỗi khác nhau nên không tra cứu kiểu này
     * được, phải quét từng dòng mà so.
     */
    private static String fingerprint(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            /* SHA-256 là thuật toán BẮT BUỘC có trong mọi bản Java. Tới được
               đây nghĩa là môi trường chạy hỏng nặng, không phải lỗi có thể
               xử lý tử tế — để nó nổ còn hơn lặng lẽ lưu vé dạng thô. */
            throw new IllegalStateException("JVM thiếu SHA-256", e);
        }
    }

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
                /* Lưu VÂN TAY, không lưu vé. Vé thật chỉ nằm trong đường
                   link gửi cho người dùng và không tồn tại ở đâu khác. */
                ps.setString(1, fingerprint(token));
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
            ps.setString(1, fingerprint(token));
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
            ps.setString(1, fingerprint(token));
            ps.executeUpdate();
        }
    }
}
