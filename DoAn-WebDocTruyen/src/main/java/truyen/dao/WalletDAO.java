package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Transaction;
import truyen.model.Wallet;
import truyen.util.DBConnection;

/**
 * Quản lý ví xu ảo và giao dịch ủng hộ tác giả (ISSUE-008).
 * Không đụng tiền thật — chỉ dùng xu tặng thưởng trong cộng đồng.
 */
public class WalletDAO {

    public boolean canPersistUnlocks() { return DBConnection.isReady(); }

    /**
     * Lấy số dư xu của người dùng. Nếu chưa có ví, tự động tạo mới với 100 xu tặng làm quen.
     */
    public int getBalance(int userId) throws SQLException {
        if (!DBConnection.isReady()) return 100;

        String selectSql = "SELECT balance FROM wallets WHERE user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("balance");
                }
            }
        }

        // Chưa có ví -> Tạo mới với 100 xu khởi đầu
        String insertSql = "INSERT IGNORE INTO wallets (user_id, balance) VALUES (?, 100)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(insertSql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
        return 100;
    }

    /**
     * Chuyển xu ủng hộ tác giả (Atomic Transaction).
     * @return true nếu thành công, false nếu không đủ số dư hoặc lỗi
     */
    public boolean transfer(int fromUserId, int toUserId, Integer storyId, int amount, String message)
            throws SQLException {
        if (amount <= 0 || fromUserId == toUserId) return false;
        if (!DBConnection.isReady()) return true;

        String deductSql = "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?";
        String addSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                      + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String logSql = "INSERT INTO transactions (from_user_id, to_user_id, story_id, amount, message) "
                      + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                // Đảm bảo ví người gửi tồn tại
                getBalance(fromUserId);

                // 1. Trừ tiền người gửi (kiểm tra balance >= amount)
                try (PreparedStatement ps = con.prepareStatement(deductSql)) {
                    ps.setInt(1, amount);
                    ps.setInt(2, fromUserId);
                    ps.setInt(3, amount);
                    int affected = ps.executeUpdate();
                    if (affected == 0) {
                        con.rollback();
                        return false; // Không đủ số dư
                    }
                }

                // 2. Cộng tiền người nhận
                try (PreparedStatement ps = con.prepareStatement(addSql)) {
                    ps.setInt(1, toUserId);
                    ps.setInt(2, amount);
                    ps.setInt(3, amount);
                    ps.executeUpdate();
                }

                // 3. Ghi log giao dịch
                try (PreparedStatement ps = con.prepareStatement(logSql)) {
                    ps.setInt(1, fromUserId);
                    ps.setInt(2, toUserId);
                    if (storyId != null && storyId > 0) {
                        ps.setInt(3, storyId);
                    } else {
                        ps.setNull(3, Types.INTEGER);
                    }
                    ps.setInt(4, amount);
                    String tipMessage = message != null && !message.trim().isEmpty() ? message.trim() : "Ủng hộ truyện";
                    ps.setString(5, "[TIP] " + tipMessage.substring(0, Math.min(230, tipMessage.length())));
                    ps.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Mở khoá chương VIP bằng xu ảo (ISSUE-020).
     * Atomic transaction:
     * 1. Trừ xu người đọc (nếu balance >= price).
     * 2. Cộng xu tác giả.
     * 3. Ghi vào chapter_unlocks.
     * 4. Ghi vào transactions với type/message mở khoá chương.
     */
    public boolean unlockChapter(int readerId, int chapterId, int authorId, int storyId, int price)
            throws SQLException {
        if (price <= 0 || readerId <= 0 || chapterId <= 0 || authorId <= 0 || readerId == authorId) return false;
        if (!DBConnection.isReady()) return false;

        try (Connection con = DBConnection.get()) {
            return unlockChapter(con, readerId, chapterId, authorId, storyId, price);
        }
    }

    boolean unlockChapter(Connection con, int readerId, int chapterId, int authorId, int storyId, int price)
            throws SQLException {
        String deductSql = "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?";
        String addSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                      + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String unlockSql = "INSERT INTO chapter_unlocks (user_id, chapter_id, price_paid) VALUES (?, ?, ?)";
        String logSql = "INSERT INTO transactions (from_user_id, to_user_id, story_id, amount, message) "
                      + "VALUES (?, ?, ?, ?, ?)";

        con.setAutoCommit(false);
        try {
            // Khóa người mua để hai POST cùng lúc không trừ xu hai lần.
            try (PreparedStatement ps = con.prepareStatement("SELECT id FROM users WHERE id = ? AND status = 'ACTIVE' FOR UPDATE")) {
                ps.setInt(1, readerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) { con.rollback(); return false; }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("SELECT 1 FROM chapter_unlocks WHERE user_id = ? AND chapter_id = ?")) {
                ps.setInt(1, readerId); ps.setInt(2, chapterId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { con.commit(); return true; }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO wallets (user_id, balance) VALUES (?, 100)")) {
                ps.setInt(1, readerId); ps.executeUpdate();
            }

            // 1. Trừ tiền người đọc
            try (PreparedStatement ps = con.prepareStatement(deductSql)) {
                ps.setInt(1, price);
                ps.setInt(2, readerId);
                ps.setInt(3, price);
                int affected = ps.executeUpdate();
                if (affected == 0) {
                    con.rollback();
                    return false; // Không đủ số dư
                }
            }

            // 2. Cộng tiền tác giả (nếu khác tác giả)
            if (authorId > 0 && authorId != readerId) {
                try (PreparedStatement ps = con.prepareStatement(addSql)) {
                    ps.setInt(1, authorId);
                    ps.setInt(2, price);
                    ps.setInt(3, price);
                    ps.executeUpdate();
                }
            }

            // 3. Ghi vé mở khoá
            try (PreparedStatement ps = con.prepareStatement(unlockSql)) {
                ps.setInt(1, readerId);
                ps.setInt(2, chapterId);
                ps.setInt(3, price);
                ps.executeUpdate();
            }

            // 4. Ghi log giao dịch
            try (PreparedStatement ps = con.prepareStatement(logSql)) {
                ps.setInt(1, readerId);
                ps.setInt(2, authorId > 0 ? authorId : readerId);
                if (storyId > 0) {
                    ps.setInt(3, storyId);
                } else {
                    ps.setNull(3, Types.INTEGER);
                }
                ps.setInt(4, price);
                ps.setString(5, "Mở khoá chương #" + chapterId);
                ps.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**
     * Nạp xu vào ví người dùng (Thanh toán / Khuyến mãi / Thưởng).
     * Atomic transaction:
     * 1. Cộng balance vào wallets.
     * 2. Ghi log vào transactions (from_user_id = NULL đại diện hệ thống nạp).
     * @return true nếu thành công
     */
    public boolean addCoins(int userId, int amount, String message) throws SQLException {
        if (amount <= 0 || userId <= 0) return false;
        if (!DBConnection.isReady()) return true;

        String addSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                      + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String logSql = "INSERT INTO transactions (from_user_id, to_user_id, story_id, amount, message) "
                      + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                // Đảm bảo ví tồn tại trước khi cộng
                getBalance(userId);

                // 1. Cộng balance
                try (PreparedStatement ps = con.prepareStatement(addSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, amount);
                    ps.setInt(3, amount);
                    ps.executeUpdate();
                }

                // 2. Ghi log giao dịch nạp xu (from_user_id = NULL)
                try (PreparedStatement ps = con.prepareStatement(logSql)) {
                    ps.setNull(1, Types.INTEGER);
                    ps.setInt(2, userId);
                    ps.setNull(3, Types.INTEGER);
                    ps.setInt(4, amount);
                    ps.setString(5, message != null && !message.trim().isEmpty() ? message.trim() : "Nạp xu vào ví");
                    ps.executeUpdate();
                }

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Trừ xu khi mua gói dịch vụ, nâng cấp VIP.
     */
    public boolean deductCoins(int userId, int amount, String message) throws SQLException {
        if (amount <= 0 || userId <= 0) return false;
        if (!DBConnection.isReady()) return true;

        String deductSql = "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?";
        String logSql = "INSERT INTO transactions (from_user_id, to_user_id, story_id, amount, message) "
                      + "VALUES (?, NULL, NULL, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                getBalance(userId);
                try (PreparedStatement ps = con.prepareStatement(deductSql)) {
                    ps.setInt(1, amount);
                    ps.setInt(2, userId);
                    ps.setInt(3, amount);
                    if (ps.executeUpdate() == 0) {
                        con.rollback();
                        return false;
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(logSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, amount);
                    ps.setString(3, message != null ? message : "Chi tiêu xu");
                    ps.executeUpdate();
                }
                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /** Trừ xu, gia hạn VIP và ghi giao dịch cùng thành công hoặc cùng rollback. */
    public boolean purchaseVip(int userId, int days) throws SQLException {
        if (userId <= 0 || (days != 30 && days != 90 && days != 365)) return false;
        if (!DBConnection.isReady()) return false;
        int cost = days == 365 ? 500 : (days == 90 ? 150 : 60);
        UserDAO.ensureVipColumn();
        getBalance(userId);
        try (Connection con = DBConnection.get()) {
            return purchaseVip(con, userId, days, cost);
        }
    }

    /** Tách giao dịch để kiểm chứng rollback bằng kết nối cục bộ giả lập. */
    boolean purchaseVip(Connection con, int userId, int days, int cost) throws SQLException {
        con.setAutoCommit(false);
        try {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?")) {
                ps.setInt(1, cost);
                ps.setInt(2, userId);
                ps.setInt(3, cost);
                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE users SET vip_expire_at = DATE_ADD("
                    + "CASE WHEN vip_expire_at > NOW() THEN vip_expire_at ELSE NOW() END, INTERVAL ? DAY) WHERE id = ?")) {
                ps.setInt(1, days);
                ps.setInt(2, userId);
                if (ps.executeUpdate() != 1) {
                    con.rollback();
                    return false;
                }
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transactions (from_user_id, to_user_id, story_id, amount, message) VALUES (?, NULL, NULL, ?, ?)")) {
                ps.setInt(1, userId);
                ps.setInt(2, cost);
                ps.setString(3, "Nâng cấp Hội viên VIP (" + days + " ngày)");
                ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException ex) {
            con.rollback();
            throw ex;
        } finally {
            con.setAutoCommit(true);
        }
    }

    /**

     * Số lần và tổng số xu đã NẠP (mô phỏng) trong hôm nay — để chặn trần.
     *
     * Nhận diện giao dịch nạp: from_user_id NULL (hệ thống cộng), không gắn
     * truyện, và message bắt đầu bằng "Nạp " — đúng chuỗi addCoins() được gọi
     * từ WalletServlet. Thưởng điểm danh / nhiệm vụ cũng có from_user_id NULL
     * nhưng message khác, nên không bị đếm nhầm.
     *
     * @return {số lần, tổng xu}
     */
    public int[] topupToday(int userId) throws SQLException {
        if (!DBConnection.isReady()) return new int[] { 0, 0 };
        String sql = "SELECT COUNT(*), COALESCE(SUM(amount), 0) FROM transactions "
                   + "WHERE to_user_id = ? AND from_user_id IS NULL AND story_id IS NULL "
                   + "  AND message LIKE 'Nạp %' AND created_at >= CURDATE()";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new int[] { rs.getInt(1), rs.getInt(2) } : new int[] { 0, 0 };
            }
        }
    }

    /**
     * Tìm thông tin ví xu theo userId.
     */
    public truyen.model.Wallet findByUserId(int userId) throws SQLException {
        int balance = getBalance(userId);
        return new truyen.model.Wallet(userId, balance);
    }

    /**
     * Lấy lịch sử giao dịch xu của người dùng (cả gửi và nhận).
     */
    public java.util.List<truyen.model.Transaction> findHistoryByUserId(int userId, int limit) throws SQLException {
        java.util.List<truyen.model.Transaction> list = new java.util.ArrayList<>();
        if (!DBConnection.isReady()) return list;

        String sql = "SELECT t.id, t.from_user_id, t.to_user_id, t.story_id, t.amount, t.message, t.created_at, "
                   + "       u1.display_name AS from_name, u2.display_name AS to_name, s.title AS story_title "
                   + "FROM transactions t "
                   + "LEFT JOIN users u1 ON u1.id = t.from_user_id "
                   + "LEFT JOIN users u2 ON u2.id = t.to_user_id "
                   + "LEFT JOIN stories s ON s.id = t.story_id "
                   + "WHERE t.from_user_id = ? OR t.to_user_id = ? "
                   + "ORDER BY t.created_at DESC LIMIT ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, limit > 0 ? limit : 20);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    truyen.model.Transaction tx = new truyen.model.Transaction();
                    tx.setId(rs.getInt("id"));
                    int fId = rs.getInt("from_user_id");
                    tx.setFromUserId(rs.wasNull() ? null : fId);
                    int tId = rs.getInt("to_user_id");
                    tx.setToUserId(rs.wasNull() ? null : tId);
                    int sId = rs.getInt("story_id");
                    tx.setStoryId(rs.wasNull() ? null : sId);
                    tx.setAmount(rs.getInt("amount"));
                    tx.setMessage(rs.getString("message"));
                    if (rs.getTimestamp("created_at") != null) {
                        tx.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    tx.setFromUsername(rs.getString("from_name"));
                    tx.setToUsername(rs.getString("to_name"));
                    tx.setStoryTitle(rs.getString("story_title"));
                    list.add(tx);
                }
            }
        }
        return list;
    }

    /**
     * Tổng số xu tác giả kiếm được từ một truyện (từ mở khoá VIP và tặng xu).
     */
    public int getStoryIncome(int authorId, int storyId) throws SQLException {
        if (authorId <= 0 || storyId <= 0 || !DBConnection.isReady()) return 0;
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE to_user_id = ? AND story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Tổng số xu tác giả nhận được từ độc giả (cả mở khoá chương và tặng xu).
     */
    public int getTotalAuthorIncome(int authorId) throws SQLException {
        if (authorId <= 0 || !DBConnection.isReady()) return 0;
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions "
                   + "WHERE to_user_id = ? AND from_user_id IS NOT NULL AND from_user_id <> ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Danh sách các khoản xu tác giả nhận được từ độc giả gần nhất (ủng hộ & mở khoá chương).
     */
    public java.util.List<truyen.model.Transaction> findEarningsByAuthorId(int authorId, int limit) throws SQLException {
        java.util.List<truyen.model.Transaction> list = new java.util.ArrayList<>();
        if (authorId <= 0 || !DBConnection.isReady()) return list;

        String sql = "SELECT t.id, t.from_user_id, t.to_user_id, t.story_id, t.amount, t.message, t.created_at, "
                   + "       u1.display_name AS from_name, u2.display_name AS to_name, s.title AS story_title "
                   + "FROM transactions t "
                   + "LEFT JOIN users u1 ON u1.id = t.from_user_id "
                   + "JOIN users u2 ON u2.id = t.to_user_id "
                   + "LEFT JOIN stories s ON s.id = t.story_id "
                   + "WHERE t.to_user_id = ? AND t.from_user_id IS NOT NULL AND t.from_user_id <> ? "
                   + "ORDER BY t.created_at DESC LIMIT ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, authorId);
            ps.setInt(3, limit > 0 ? limit : 15);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    truyen.model.Transaction tx = new truyen.model.Transaction();
                    tx.setId(rs.getInt("id"));
                    int fId = rs.getInt("from_user_id");
                    tx.setFromUserId(rs.wasNull() ? null : fId);
                    tx.setToUserId(rs.getInt("to_user_id"));
                    int sId = rs.getInt("story_id");
                    tx.setStoryId(rs.wasNull() ? null : sId);
                    tx.setAmount(rs.getInt("amount"));
                    tx.setMessage(rs.getString("message"));
                    if (rs.getTimestamp("created_at") != null) {
                        tx.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    tx.setFromUsername(rs.getString("from_name"));
                    tx.setToUsername(rs.getString("to_name"));
                    tx.setStoryTitle(rs.getString("story_title"));
                    list.add(tx);
                }
            }
        }
        return list;
    }

    /**
     * Bảng Vàng Fan Cứng: Top những độc giả ủng hộ / mở khoá xu nhiều nhất cho tác giả.
     */
    public java.util.List<truyen.model.AuthorPatron> findTopPatronsByAuthor(int authorId, int limit) throws SQLException {
        java.util.List<truyen.model.AuthorPatron> list = new java.util.ArrayList<>();
        if (authorId <= 0 || !DBConnection.isReady()) return list;

        String sql = "SELECT t.from_user_id, "
                   + "       u.username, u.display_name, u.avatar_url, "
                   + "       SUM(t.amount) AS total_amount, "
                   + "       COUNT(t.id) AS donation_count, "
                   + "       MAX(t.created_at) AS last_donated_at "
                   + "FROM transactions t "
                   + "JOIN users u ON u.id = t.from_user_id "
                   + "WHERE t.to_user_id = ? AND t.from_user_id IS NOT NULL AND t.from_user_id <> ? "
                   + "GROUP BY t.from_user_id, u.username, u.display_name, u.avatar_url "
                   + "ORDER BY total_amount DESC, last_donated_at DESC "
                   + "LIMIT ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, authorId);
            ps.setInt(3, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    truyen.model.AuthorPatron ap = new truyen.model.AuthorPatron();
                    ap.setUserId(rs.getInt("from_user_id"));
                    ap.setUsername(rs.getString("username"));
                    ap.setDisplayName(rs.getString("display_name"));
                    ap.setAvatarUrl(rs.getString("avatar_url"));
                    ap.setTotalAmount(rs.getInt("total_amount"));
                    ap.setDonationCount(rs.getInt("donation_count"));
                    if (rs.getTimestamp("last_donated_at") != null) {
                        ap.setLastDonatedAt(rs.getTimestamp("last_donated_at").toLocalDateTime());
                    }
                    list.add(ap);
                }
            }
        }
        return list;
    }

    // =========================================================================
    //  PHẦN DÀNH CHO QUẢN TRỊ VIÊN (ISSUE-023)
    // =========================================================================

    /**
     * Lấy danh sách ví của tất cả người dùng kèm thống kê đã nhận/đã tiêu (cho Admin).
     */
    public List<Wallet> findAllWallets(String q, int offset, int limit) throws SQLException {
        List<Wallet> list = new ArrayList<>();
        if (!DBConnection.isReady()) return list;

        boolean hasQ = q != null && !q.trim().isEmpty();
        String pattern = hasQ ? "%" + q.trim() + "%" : "%";

        String sql = "SELECT u.id AS user_id, u.username, u.display_name, u.email, u.avatar_url, "
                   + "       COALESCE(w.balance, 100) AS balance, w.updated_at, "
                   + "       (SELECT COALESCE(SUM(t.amount), 0) FROM transactions t WHERE t.to_user_id = u.id AND t.from_user_id IS NOT NULL AND t.from_user_id <> u.id) AS total_received, "
                   + "       (SELECT COALESCE(SUM(t.amount), 0) FROM transactions t WHERE t.from_user_id = u.id) AS total_spent "
                   + "FROM users u "
                   + "LEFT JOIN wallets w ON w.user_id = u.id "
                   + "WHERE (u.username LIKE ? OR u.display_name LIKE ? OR u.email LIKE ?) "
                   + "ORDER BY balance DESC, u.id ASC "
                   + "LIMIT ? OFFSET ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setInt(4, limit > 0 ? limit : 20);
            ps.setInt(5, Math.max(0, offset));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Wallet w = new Wallet();
                    w.setUserId(rs.getInt("user_id"));
                    w.setUsername(rs.getString("username"));
                    w.setDisplayName(rs.getString("display_name"));
                    w.setEmail(rs.getString("email"));
                    w.setAvatarUrl(rs.getString("avatar_url"));
                    w.setBalance(rs.getInt("balance"));
                    if (rs.getTimestamp("updated_at") != null) {
                        w.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    }
                    w.setTotalReceived(rs.getInt("total_received"));
                    w.setTotalSpent(rs.getInt("total_spent"));
                    list.add(w);
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số ví thoả điều kiện tìm kiếm.
     */
    public int countWallets(String q) throws SQLException {
        if (!DBConnection.isReady()) return 0;
        boolean hasQ = q != null && !q.trim().isEmpty();
        String pattern = hasQ ? "%" + q.trim() + "%" : "%";

        String sql = "SELECT COUNT(*) FROM users u "
                   + "WHERE (u.username LIKE ? OR u.display_name LIKE ? OR u.email LIKE ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Tổng số xu đang lưu hành trên toàn bộ hệ thống.
     */
    public long getTotalCirculatingCoins() throws SQLException {
        if (!DBConnection.isReady()) return 10000;
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM wallets";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    /**
     * Tổng số lượt giao dịch toàn hệ thống.
     */
    public long getTotalTransactions() throws SQLException {
        if (!DBConnection.isReady()) return 0;
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    /**
     * Tổng số xu đã được giao dịch qua lại.
     */
    public long getTotalCoinsTipped() throws SQLException {
        if (!DBConnection.isReady()) return 0;
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE from_user_id IS NOT NULL";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getLong(1) : 0;
        }
    }

    /**
     * Danh sách giao dịch toàn hệ thống gần nhất (cho Admin).
     */
    public List<Transaction> findRecentTransactions(int limit) throws SQLException {
        List<Transaction> list = new ArrayList<>();
        if (!DBConnection.isReady()) return list;

        String sql = "SELECT t.id, t.from_user_id, t.to_user_id, t.story_id, t.amount, t.message, t.created_at, "
                   + "       u1.username AS from_username, u1.display_name AS from_name, "
                   + "       u2.username AS to_username, u2.display_name AS to_name, "
                   + "       s.title AS story_title "
                   + "FROM transactions t "
                   + "LEFT JOIN users u1 ON u1.id = t.from_user_id "
                   + "JOIN users u2 ON u2.id = t.to_user_id "
                   + "LEFT JOIN stories s ON s.id = t.story_id "
                   + "ORDER BY t.created_at DESC, t.id DESC "
                   + "LIMIT ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 20);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = new Transaction();
                    tx.setId(rs.getInt("id"));
                    int fId = rs.getInt("from_user_id");
                    tx.setFromUserId(rs.wasNull() ? null : fId);
                    tx.setToUserId(rs.getInt("to_user_id"));
                    int sId = rs.getInt("story_id");
                    tx.setStoryId(rs.wasNull() ? null : sId);
                    tx.setAmount(rs.getInt("amount"));
                    tx.setMessage(rs.getString("message"));
                    if (rs.getTimestamp("created_at") != null) {
                        tx.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    String fName = rs.getString("from_name");
                    tx.setFromUsername(fName != null && !fName.isEmpty() ? fName : rs.getString("from_username"));
                    String tName = rs.getString("to_name");
                    tx.setToUsername(tName != null && !tName.isEmpty() ? tName : rs.getString("to_username"));
                    tx.setStoryTitle(rs.getString("story_title"));
                    list.add(tx);
                }
            }
        }
        return list;
    }

    /**
     * Admin cấp xu hoặc thu hồi xu của người dùng.
     * Bắt buộc thực hiện trong 1 Transaction và ghi nhận đồng thời vào audit_logs.
     */
    public boolean adminAdjust(int adminId, int targetUserId, int amount, String reason) throws SQLException {
        if (!DBConnection.isReady() || targetUserId <= 0 || amount == 0) return true;

        String selectSql = "SELECT balance FROM wallets WHERE user_id = ? FOR UPDATE";
        String updateSql = "UPDATE wallets SET balance = balance + ? WHERE user_id = ?";
        String insertTxSql = "INSERT INTO transactions (from_user_id, to_user_id, amount, message, created_at) VALUES (?, ?, ?, ?, NOW())";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                int currentBalance = 100;
                boolean walletExists = false;
                try (PreparedStatement ps = con.prepareStatement(selectSql)) {
                    ps.setInt(1, targetUserId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            currentBalance = rs.getInt("balance");
                            walletExists = true;
                        }
                    }
                }

                if (!walletExists) {
                    try (PreparedStatement ps = con.prepareStatement("INSERT INTO wallets (user_id, balance) VALUES (?, 100)")) {
                        ps.setInt(1, targetUserId);
                        ps.executeUpdate();
                    }
                    currentBalance = 100;
                }

                // Nếu thu hồi xu, không cho số dư âm
                if (amount < 0 && (currentBalance + amount < 0)) {
                    con.rollback();
                    return false;
                }

                try (PreparedStatement ps = con.prepareStatement(updateSql)) {
                    ps.setInt(1, amount);
                    ps.setInt(2, targetUserId);
                    ps.executeUpdate();
                }

                // Ghi transaction
                try (PreparedStatement ps = con.prepareStatement(insertTxSql)) {
                    if (amount > 0) {
                        if (adminId > 0) ps.setInt(1, adminId);
                        else ps.setNull(1, java.sql.Types.INTEGER);
                        ps.setInt(2, targetUserId);
                    } else {
                        ps.setInt(1, targetUserId);
                        if (adminId > 0) ps.setInt(2, adminId);
                        else ps.setNull(2, java.sql.Types.INTEGER);
                    }
                    ps.setInt(3, Math.abs(amount));
                    ps.setString(4, (amount > 0 ? "Admin cấp xu: " : "Admin thu hồi xu: ") + (reason != null ? reason : ""));
                    ps.executeUpdate();
                }

                // Ghi nhật ký audit
                AuditDAO auditDAO = new AuditDAO();
                auditDAO.log(adminId, amount > 0 ? "GRANT_COIN" : "REVOKE_COIN", "WALLET", targetUserId,
                        "Ví User #" + targetUserId + " (" + (amount > 0 ? "+" : "") + amount + " xu)", reason);

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Tổng số xu đang lưu hành trong toàn bộ hệ thống.
     */
    public int totalCirculatingCoins() throws SQLException {
        if (!DBConnection.isReady()) return 0;
        String sql = "SELECT COALESCE(SUM(balance), 0) FROM wallets";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    /**
     * Tổng số lượt giao dịch xu trong hệ thống.
     */
    public int countTotalTransactions() throws SQLException {
        if (!DBConnection.isReady()) return 0;
        String sql = "SELECT COUNT(*) FROM transactions";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
}
