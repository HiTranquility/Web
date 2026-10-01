package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import truyen.util.DBConnection;

/**
 * Quản lý ví xu ảo và giao dịch ủng hộ tác giả (ISSUE-008).
 * Không đụng tiền thật — chỉ dùng xu tặng thưởng trong cộng đồng.
 */
public class WalletDAO {

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
                    ps.setString(5, message != null && !message.trim().isEmpty() ? message.trim() : "Ủng hộ truyện");
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
        if (price <= 0 || readerId <= 0 || chapterId <= 0) return false;
        if (!DBConnection.isReady()) return true;

        String deductSql = "UPDATE wallets SET balance = balance - ? WHERE user_id = ? AND balance >= ?";
        String addSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                      + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String unlockSql = "INSERT INTO chapter_unlocks (user_id, chapter_id, price_paid) VALUES (?, ?, ?)";
        String logSql = "INSERT INTO transactions (from_user_id, to_user_id, story_id, amount, message) "
                      + "VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                // Đảm bảo ví người đọc tồn tại
                getBalance(readerId);

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
                   + "JOIN users u2 ON u2.id = t.to_user_id "
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
}
