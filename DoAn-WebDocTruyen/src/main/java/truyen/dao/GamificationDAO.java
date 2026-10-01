package truyen.dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import truyen.model.DailyCheckin;
import truyen.model.DailyQuest;
import truyen.util.DBConnection;

/**
 * Quản lý điểm danh nhận xu & nhiệm vụ hàng ngày (Gamification).
 */
public class GamificationDAO {

    private static final int[] STREAK_REWARDS = { 5, 10, 15, 20, 25, 30, 50 }; // Ngày 1 -> Ngày 7

    /**
     * Lấy điểm danh hôm nay của user (nếu đã điểm danh).
     */
    public DailyCheckin getTodayCheckin(int userId) throws SQLException {
        if (userId <= 0 || !DBConnection.isReady()) return null;

        String sql = "SELECT id, user_id, checkin_date, streak_days, reward_coins, created_at "
                   + "FROM daily_checkins WHERE user_id = ? AND checkin_date = CURRENT_DATE()";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DailyCheckin c = new DailyCheckin();
                    c.setId(rs.getInt("id"));
                    c.setUserId(rs.getInt("user_id"));
                    c.setCheckinDate(rs.getDate("checkin_date").toLocalDate());
                    c.setStreakDays(rs.getInt("streak_days"));
                    c.setRewardCoins(rs.getInt("reward_coins"));
                    c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    return c;
                }
            }
        }
        return null;
    }

    /**
     * Lấy chuỗi streak hiện tại của user.
     */
    public int getCurrentStreak(int userId) throws SQLException {
        if (userId <= 0 || !DBConnection.isReady()) return 0;

        String sql = "SELECT checkin_date, streak_days FROM daily_checkins "
                   + "WHERE user_id = ? ORDER BY checkin_date DESC LIMIT 1";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDate lastDate = rs.getDate("checkin_date").toLocalDate();
                    int lastStreak = rs.getInt("streak_days");
                    LocalDate today = LocalDate.now();

                    if (lastDate.equals(today)) {
                        return lastStreak;
                    } else if (lastDate.equals(today.minusDays(1))) {
                        return lastStreak;
                    } else {
                        return 0; // Ngắt chuỗi
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Thực hiện điểm danh hôm nay (Atomic Transaction).
     * Tự động cộng xu vào ví và ghi transactions.
     * @return DailyCheckin nếu thành công, null nếu đã điểm danh rồi hoặc lỗi.
     */
    public DailyCheckin doCheckin(int userId) throws SQLException {
        if (userId <= 0 || !DBConnection.isReady()) return null;

        // Kiểm tra xem hôm nay đã điểm danh chưa
        DailyCheckin existing = getTodayCheckin(userId);
        if (existing != null) return existing;

        LocalDate today = LocalDate.now();
        int prevStreak = 0;

        String lastSql = "SELECT checkin_date, streak_days FROM daily_checkins "
                       + "WHERE user_id = ? ORDER BY checkin_date DESC LIMIT 1";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(lastSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LocalDate lastDate = rs.getDate("checkin_date").toLocalDate();
                    if (lastDate.equals(today.minusDays(1))) {
                        prevStreak = rs.getInt("streak_days");
                    }
                }
            }
        }

        int newStreak = (prevStreak % 7) + 1;
        int rewardCoins = STREAK_REWARDS[newStreak - 1];

        String insertSql = "INSERT INTO daily_checkins (user_id, checkin_date, streak_days, reward_coins) "
                         + "VALUES (?, CURRENT_DATE(), ?, ?)";
        String addWalletSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String logTxSql = "INSERT INTO transactions (from_user_id, to_user_id, amount, message) "
                        + "VALUES (NULL, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                // 1. Ghi nhận điểm danh
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, newStreak);
                    ps.setInt(3, rewardCoins);
                    ps.executeUpdate();
                }

                // 2. Cộng xu vào ví
                try (PreparedStatement ps = con.prepareStatement(addWalletSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, rewardCoins);
                    ps.setInt(3, rewardCoins);
                    ps.executeUpdate();
                }

                // 3. Ghi transaction
                try (PreparedStatement ps = con.prepareStatement(logTxSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, rewardCoins);
                    ps.setString(3, "Thưởng điểm danh ngày " + newStreak + " (" + rewardCoins + " xu)");
                    ps.executeUpdate();
                }

                con.commit();

                DailyCheckin res = new DailyCheckin(userId, today, newStreak, rewardCoins);
                return res;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Lấy danh sách nhiệm vụ hàng ngày và trạng thái hoàn thành / đã nhận thưởng của user.
     */
    public List<DailyQuest> getDailyQuests(int userId) throws SQLException {
        List<DailyQuest> quests = new ArrayList<>();
        if (userId <= 0 || !DBConnection.isReady()) return quests;

        // 1. Nhiệm vụ: Điểm danh ngày mới
        DailyCheckin checkin = getTodayCheckin(userId);
        quests.add(new DailyQuest(
            "DAILY_CHECKIN",
            "Điểm danh ngày mới",
            "Đăng nhập và điểm danh hàng ngày nhận xu thưởng",
            10,
            1,
            checkin != null ? 1 : 0,
            checkin != null // Đã nhận cùng lúc bấm điểm danh
        ));

        // 2. Nhiệm vụ: Mọt sách siêng năng (Đọc ít nhất 1 chương truyện hôm nay)
        int readTodayCount = 0;
        String readSql = "SELECT COUNT(*) FROM view_logs WHERE user_id = ? AND DATE(viewed_at) = CURRENT_DATE()";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(readSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) readTodayCount = rs.getInt(1);
            }
        }
        boolean readClaimed = isQuestClaimed(userId, "READ_CHAPTER");
        quests.add(new DailyQuest(
            "READ_CHAPTER",
            "Mọt sách siêng năng",
            "Đọc ít nhất 1 chương truyện trong ngày",
            10,
            1,
            Math.min(1, readTodayCount),
            readClaimed
        ));

        // 3. Nhiệm vụ: Nhà phê bình nhiệt huyết (Bình luận hoặc đánh giá 1 truyện hôm nay)
        int reviewTodayCount = 0;
        String commentSql = "SELECT "
                          + "(SELECT COUNT(*) FROM comments WHERE user_id = ? AND DATE(created_at) = CURRENT_DATE()) + "
                          + "(SELECT COUNT(*) FROM reviews WHERE user_id = ? AND DATE(created_at) = CURRENT_DATE())";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(commentSql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) reviewTodayCount = rs.getInt(1);
            }
        }
        boolean reviewClaimed = isQuestClaimed(userId, "COMMENT_REVIEW");
        quests.add(new DailyQuest(
            "COMMENT_REVIEW",
            "Nhà phê bình nhiệt huyết",
            "Để lại 1 bình luận hoặc đánh giá cho truyện hôm nay",
            15,
            1,
            Math.min(1, reviewTodayCount),
            reviewClaimed
        ));

        return quests;
    }

    /**
     * Nhận thưởng nhiệm vụ hàng ngày (Atomic Transaction).
     */
    public boolean claimQuestReward(int userId, String questKey) throws SQLException {
        if (userId <= 0 || questKey == null || !DBConnection.isReady()) return false;

        // Kiểm tra xem đã nhận chưa
        if (isQuestClaimed(userId, questKey)) return false;

        // Kiểm tra điều kiện hoàn thành nhiệm vụ
        int rewardCoins = 0;
        String questName = "";

        if ("READ_CHAPTER".equals(questKey)) {
            rewardCoins = 10;
            questName = "Mọt sách siêng năng";
            String readSql = "SELECT COUNT(*) FROM view_logs WHERE user_id = ? AND DATE(viewed_at) = CURRENT_DATE()";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(readSql)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) < 1) return false; // Chưa xong
                }
            }
        } else if ("COMMENT_REVIEW".equals(questKey)) {
            rewardCoins = 15;
            questName = "Nhà phê bình nhiệt huyết";
            String commentSql = "SELECT "
                              + "(SELECT COUNT(*) FROM comments WHERE user_id = ? AND DATE(created_at) = CURRENT_DATE()) + "
                              + "(SELECT COUNT(*) FROM reviews WHERE user_id = ? AND DATE(created_at) = CURRENT_DATE())";
            try (Connection con = DBConnection.get();
                 PreparedStatement ps = con.prepareStatement(commentSql)) {
                ps.setInt(1, userId);
                ps.setInt(2, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next() || rs.getInt(1) < 1) return false; // Chưa xong
                }
            }
        } else {
            return false;
        }

        String claimSql = "INSERT INTO daily_quest_claims (user_id, quest_key, claim_date, reward_coins) "
                        + "VALUES (?, ?, CURRENT_DATE(), ?)";
        String addWalletSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String logTxSql = "INSERT INTO transactions (from_user_id, to_user_id, amount, message) "
                        + "VALUES (NULL, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(claimSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, questKey);
                    ps.setInt(3, rewardCoins);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(addWalletSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, rewardCoins);
                    ps.setInt(3, rewardCoins);
                    ps.executeUpdate();
                }

                try (PreparedStatement ps = con.prepareStatement(logTxSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, rewardCoins);
                    ps.setString(3, "Thưởng nhiệm vụ: " + questName + " (" + rewardCoins + " xu)");
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

    private boolean isQuestClaimed(int userId, String questKey) throws SQLException {
        String sql = "SELECT 1 FROM daily_quest_claims WHERE user_id = ? AND quest_key = ? AND claim_date = CURRENT_DATE()";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, questKey);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
