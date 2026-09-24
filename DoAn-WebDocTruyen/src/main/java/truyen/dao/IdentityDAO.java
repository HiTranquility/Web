package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import truyen.model.UserIdentity;
import truyen.util.DBConnection;

/**
 * Truy vấn bảng user_identities.
 * Quản lý liên kết các phương thức đăng nhập bên thứ ba (Google OAuth / Firebase).
 *
 * Theo quy ước DAO:
 * - Không chạm HttpServletRequest / Session.
 * - Sử dụng PreparedStatement cho toàn bộ tham số.
 * - Ném SQLException lên tầng servlet xử lý.
 */
public class IdentityDAO {

    private static final String SELECT_BASE =
        "SELECT id, user_id, provider, provider_uid, email, created_at "
      + "FROM user_identities ";

    /**
     * Tìm liên kết theo nhà cung cấp và mã định danh duy nhất (sub).
     * Dùng khi đăng nhập Google: tìm xem tài khoản Google này đã gắn với user nào chưa.
     */
    public UserIdentity findByProviderUid(String provider, String providerUid) throws SQLException {
        if (!DBConnection.isReady()) return null;
        String sql = SELECT_BASE + "WHERE provider = ? AND provider_uid = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, provider);
            ps.setString(2, providerUid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Lấy toàn bộ các liên kết tài khoản của một người dùng.
     */
    public List<UserIdentity> findByUserId(int userId) throws SQLException {
        List<UserIdentity> list = new ArrayList<>();
        if (!DBConnection.isReady()) return list;
        String sql = SELECT_BASE + "WHERE user_id = ? ORDER BY id ASC";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Tìm liên kết của một user với nhà cung cấp cụ thể (vd: kiểm tra user này đã gắn Google chưa).
     */
    public UserIdentity findByUserAndProvider(int userId, String provider) throws SQLException {
        if (!DBConnection.isReady()) return null;
        String sql = SELECT_BASE + "WHERE user_id = ? AND provider = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, provider);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Kiểm tra nhanh xem user đã gắn nhà cung cấp này chưa.
     */
    public boolean hasProvider(int userId, String provider) throws SQLException {
        if (!DBConnection.isReady()) return false;
        String sql = "SELECT 1 FROM user_identities WHERE user_id = ? AND provider = ? LIMIT 1";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, provider);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm liên kết mới, gán ID tự sinh vào object.
     * Ném SQLIntegrityConstraintViolationException nếu provider_uid hoặc (user_id, provider) đã tồn tại.
     */
    public void insert(UserIdentity identity) throws SQLException {
        if (!DBConnection.isReady()) return;
        String sql = "INSERT INTO user_identities (user_id, provider, provider_uid, email) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, identity.getUserId());
            ps.setString(2, identity.getProvider());
            ps.setString(3, identity.getProviderUid());
            ps.setString(4, identity.getEmail());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    identity.setId(keys.getInt(1));
                }
            }
        }
    }

    /**
     * Gỡ liên kết của người dùng với nhà cung cấp.
     */
    public boolean deleteByUserAndProvider(int userId, String provider) throws SQLException {
        if (!DBConnection.isReady()) return false;
        String sql = "DELETE FROM user_identities WHERE user_id = ? AND provider = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, provider);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa liên kết theo ID bản ghi.
     */
    public boolean deleteById(int id) throws SQLException {
        if (!DBConnection.isReady()) return false;
        String sql = "DELETE FROM user_identities WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private UserIdentity mapRow(ResultSet rs) throws SQLException {
        UserIdentity item = new UserIdentity();
        item.setId(rs.getInt("id"));
        item.setUserId(rs.getInt("user_id"));
        item.setProvider(rs.getString("provider"));
        item.setProviderUid(rs.getString("provider_uid"));
        item.setEmail(rs.getString("email"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) {
            item.setCreatedAt(ts.toLocalDateTime());
        }
        return item;
    }
}
