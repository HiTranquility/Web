package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import truyen.model.User;
import truyen.util.DBConnection;

/**
 * Truy vấn bảng users.
 *
 * Theo đúng luật của tầng DAO (xem docs/standards/01-CODING_CONVENTIONS.md §2):
 * chỉ nói chuyện với database, không đụng request/response, ném SQLException
 * lên cho servlet xử lý.
 */
public class UserDAO {

    private static final String SELECT_BASE =
        "SELECT id, username, email, password_hash, display_name, avatar_url, "
      + "       bio, role, status, ban_reason, created_at "
      + "FROM users ";

    /**
     * Tìm theo username — dùng lúc đăng nhập.
     * Trả null nếu không có. Servlet phải kiểm null trước khi dùng.
     */
    public User findByUsername(String username) throws SQLException {
        String sql = SELECT_BASE + "WHERE username = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public User findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Username hoặc email đã có người dùng chưa — kiểm lúc đăng ký.
     *
     * Database đã có ràng buộc UNIQUE nên dù bỏ qua bước này cũng không tạo
     * được trùng. Nhưng kiểm trước thì báo lỗi tử tế được ("Tên này đã có
     * người dùng"), thay vì quăng SQLException khó hiểu cho người dùng xem.
     */
    public boolean exists(String username, String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE username = ? OR email = ? LIMIT 1";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm tài khoản mới, gán luôn id vừa sinh vào object user.
     *
     * RETURN_GENERATED_KEYS: bảo MySQL trả về id AUTO_INCREMENT vừa tạo.
     * Không có nó thì user.getId() vẫn là 0 và mọi thứ dựa vào id sẽ sai.
     *
     * Chỉ nhận passwordHash — DAO KHÔNG băm mật khẩu. Băm là việc của
     * PasswordUtil, servlet gọi trước rồi mới đưa xuống đây. Tách vậy để DAO
     * chỉ làm đúng một việc là đọc ghi database.
     */
    public void insert(User user) throws SQLException {
        String sql = "INSERT INTO users "
                   + "(username, email, password_hash, display_name, role, status) "
                   + "VALUES (?, ?, ?, ?, 'USER', 'ACTIVE')";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getDisplayName());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                }
            }
        }
    }

    /** Admin ban / bỏ ban một tài khoản (CASE 10). */
    public void updateStatus(int userId, String status, String reason) throws SQLException {
        String sql = "UPDATE users SET status = ?, ban_reason = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, reason);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    /* Đổi MỘT dòng ResultSet thành MỘT object User. Thêm cột mới thì sửa
       đúng chỗ này, không phải đi sửa từng method truy vấn. */
    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setDisplayName(rs.getString("display_name"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setBio(rs.getString("bio"));
        u.setRole(rs.getString("role"));
        u.setStatus(rs.getString("status"));
        u.setBanReason(rs.getString("ban_reason"));
        if (rs.getTimestamp("created_at") != null) {
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return u;
    }
}
