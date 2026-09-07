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

    /**
     * Danh sách tài khoản kèm số truyện mỗi người — trang quản trị (TRANG 27).
     *
     * VÌ SAO CÂU NÀY NẰM Ở ĐÂY CHỨ KHÔNG PHẢI TRONG SERVLET
     *   Trước đây nó nằm thẳng trong AdminUserServlet cho "gọn". Nhưng
     *   standards §2 đã chốt: controller KHÔNG viết SQL. Lý do không phải là
     *   sự sạch sẽ hình thức — mà là khi đổi tên cột `status`, người sửa chỉ
     *   đi lục thư mục dao/, không ai nghĩ tới việc mở servlet ra tìm SQL.
     *
     * VÌ SAO DÙNG SUBQUERY CHỨ KHÔNG PHẢI LEFT JOIN + GROUP BY
     *   JOIN rồi GROUP BY sẽ phải gom theo tất cả các cột của users. Subquery
     *   tương quan đọc thẳng ý định: "với mỗi user, đếm truyện của user đó".
     *   Với vài trăm tài khoản thì hai cách nhanh như nhau.
     *
     * LIMIT 200: trang quản trị chưa phân trang. Có giới hạn cứng để một ngày
     * nào đó 50.000 tài khoản không làm trang đứng hình.
     */
    public List<User> findAllWithStoryCount() throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.users();

        String sql =
            "SELECT u.*, "
          + "       (SELECT COUNT(*) FROM stories s "
          + "        WHERE s.author_id = u.id AND s.status != 'DELETED') AS story_count "
          + "FROM users u ORDER BY u.created_at DESC LIMIT 200";

        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = mapRow(rs);
                u.setStoryCount(rs.getInt("story_count"));

                // Chuoi bam KHONG duoc roi khoi tang dao khi khong can dung.
                // Trang quan tri chi hien thi, khong xac thuc.
                u.setPasswordHash(null);
                list.add(u);
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

    /**
     * Người dùng tự sửa hồ sơ của mình (TRANG 15).
     *
     * KHÔNG có username trong danh sách cột được sửa. Tên đăng nhập là danh
     * tính: đổi được thì mọi bình luận cũ, mọi đường dẫn hồ sơ đã chia sẻ đều
     * trỏ sai người. Muốn đổi cách hiển thị thì đổi display_name.
     *
     * KHÔNG có role và status. Người dùng tự nâng mình lên ADMIN được thì
     * toàn bộ khu quản trị vô nghĩa.
     */
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
