package truyen.dao;

import truyen.util.DemoData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.userByUsername(username);
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.user(id);
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

    /** Thêm tài khoản mới, gán luôn id vừa sinh vào object user. */
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

    /**
     * Danh sách tài khoản kèm số truyện mỗi người — trang quản trị (TRANG 27).
     */
    public List<User> findAllWithStoryCount() throws SQLException {
        return searchWithStoryCount(null, null);
    }

    /**
     * Như trên nhưng có LỌC — trang 27.
     *
     * @param keyword tìm trong tên đăng nhập, tên hiển thị, email. null/rỗng = mọi người
     * @param status  ACTIVE hoặc BANNED. null/rỗng = mọi trạng thái
     *
     * VÌ SAO PHẢI CÓ Ô TÌM
     *   Câu cũ là "lấy 200 tài khoản mới nhất". Với vài chục người thì cuộn
     *   được, nhưng admin cần tìm MỘT người cụ thể — thường là người vừa bị
     *   báo cáo. Không có ô tìm thì phải Ctrl+F trên trang, mà cách đó chỉ
     *   thấy được trong 200 dòng đã tải; người thứ 201 là không tìm ra.
     *
     * DỰNG CÂU SQL BẰNG CÁCH NỐI CHUỖI — VẪN AN TOÀN
     *   Phần nối thêm là các mảnh CỐ ĐỊNH viết sẵn trong code, còn giá trị
     *   người dùng gõ luôn đi qua dấu ? và ps.setString(). Không có mẩu chữ
     *   nào của người dùng lọt vào câu lệnh, nên không có đường tiêm SQL.
     *   Sai lầm cần tránh là "... LIKE '%" + keyword + "%'" — chỗ đó mới chết.
     *
     * DẤU % ĐẶT Ở THAM SỐ, KHÔNG PHẢI TRONG CÂU SQL
     *   Viết LIKE ? rồi truyền "%kiem%" chứ không viết LIKE '%?%'. Cách sau
     *   không chạy: dấu ? nằm trong chuỗi nháy nên JDBC coi nó là ký tự thường,
     *   không phải chỗ điền tham số.
     */
    public List<User> searchWithStoryCount(String keyword, String status) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.users();

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        boolean hasStatus  = "ACTIVE".equals(status) || "BANNED".equals(status);

        StringBuilder sql = new StringBuilder(
            "SELECT u.*, "
          + "       (SELECT COUNT(*) FROM stories s "
          + "        WHERE s.author_id = u.id AND s.status != 'DELETED') AS story_count "
          + "FROM users u WHERE 1 = 1 ");

        /* "WHERE 1 = 1" ở trên để mọi điều kiện sau đều bắt đầu bằng AND.
           Không có nó thì phải nhớ điều kiện nào là cái đầu tiên để viết
           WHERE, các cái sau mới AND — đúng chỗ hay quên nhất khi thêm bộ lọc
           thứ ba. MySQL loại bỏ 1 = 1 khi tối ưu, không tốn gì. */
        if (hasKeyword) {
            sql.append("AND (u.username LIKE ? OR u.display_name LIKE ? OR u.email LIKE ?) ");
        }
        if (hasStatus) {
            sql.append("AND u.status = ? ");
        }
        sql.append("ORDER BY u.created_at DESC LIMIT 200");

        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            int i = 1;
            if (hasKeyword) {
                String like = "%" + keyword.trim() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            if (hasStatus) {
                ps.setString(i++, status);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User u = mapRow(rs);
                    u.setStoryCount(rs.getInt("story_count"));

                    // Chuoi bam KHONG duoc roi khoi tang dao khi khong can dung.
                    // Trang quan tri chi hien thi, khong xac thuc.
                    u.setPasswordHash(null);
                    list.add(u);
                }
            }
        }
        return list;
    }

    /**
     * Đổi vai trò USER <-> ADMIN (TRANG 27).
     *
     * Chỉ nhận đúng hai giá trị. Không kiểm thì một request tay chân có thể
     * đặt role = "SUPERADMIN", và mọi câu lệnh so sánh role trong dự án sẽ
     * lặng lẽ trả về false — tài khoản đó mất quyền mà không ai hiểu vì sao.
     */
    public void updateRole(int userId, String role) throws SQLException {
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            throw new SQLException("Vai trò không hợp lệ: " + role);
        }
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, role);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Người dùng tự sửa hồ sơ của mình (TRANG 15). */
    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET display_name = ?, email = ?, "
                   + "avatar_url = ?, bio = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getDisplayName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getAvatarUrl());
            ps.setString(4, user.getBio());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        }
    }

    /** Đổi mật khẩu. Nhận chuỗi ĐÃ BĂM — DAO không băm hộ, không kiểm hộ. */
    public void updatePassword(int userId, String passwordHash) throws SQLException {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    /** Tìm theo email — dùng cho luồng quên mật khẩu (TRANG 21). */
    public User findByEmail(String email) throws SQLException {
        if (!DBConnection.isReady()) {
            for (User u : DemoData.users()) {
                if (u.getEmail().equalsIgnoreCase(email)) return u;
            }
            return null;
        }
        String sql = "SELECT * FROM users WHERE email = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
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
