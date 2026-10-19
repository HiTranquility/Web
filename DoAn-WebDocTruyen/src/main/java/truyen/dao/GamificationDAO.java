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
import truyen.util.AchievementPolicy;
import truyen.model.User;

/**
 * Quản lý điểm danh nhận xu & nhiệm vụ hàng ngày (Gamification).
 */
public class GamificationDAO {

    private static final int[] STREAK_REWARDS = { 5, 10, 15, 20, 25, 30, 50 }; // Ngày 1 -> Ngày 7

    public truyen.model.RewardSummary getRewardSummary(int userId) throws SQLException {
        if (userId <= 0 || !DBConnection.isReady())
            return new truyen.model.RewardSummary(LocalDate.now(), 0, 0);
        try (Connection con = DBConnection.get()) { return getRewardSummary(con, userId); }
    }

    truyen.model.RewardSummary getRewardSummary(Connection con, int userId) throws SQLException {
        String sql = "SELECT CURRENT_DATE() AS reward_date, COALESCE(SUM(reward_coins), 0) AS total_earned, "
                + "COALESCE(SUM(CASE WHEN earned_date = CURRENT_DATE() THEN reward_coins ELSE 0 END), 0) AS today_earned "
                + "FROM (SELECT checkin_date AS earned_date, reward_coins FROM daily_checkins WHERE user_id = ? "
                + "UNION ALL SELECT claim_date, reward_coins FROM daily_quest_claims WHERE user_id = ? "
                + "UNION ALL SELECT DATE(claimed_at), reward_coins FROM user_achievements WHERE user_id = ? AND claimed_at IS NOT NULL) earned";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 3; i++) ps.setInt(i, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Missing reward summary");
                return new truyen.model.RewardSummary(rs.getDate("reward_date").toLocalDate(),
                        rs.getLong("today_earned"), rs.getLong("total_earned"));
            }
        }
    }

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
        return doCheckin(userId, 1.0);
    }

    /**
     * Thực hiện điểm danh hôm nay có áp dụng hệ số nhân cấp bậc / VIP độc giả.
     */
    public DailyCheckin doCheckin(int userId, double multiplier) throws SQLException {
        if (userId <= 0 || !DBConnection.isReady()) return null;

        // Kiểm tra xem hôm nay đã điểm danh chưa
        DailyCheckin existing = getTodayCheckin(userId);
        if (existing != null) return null;

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
        int baseReward = STREAK_REWARDS[newStreak - 1];
        if (multiplier < 1.0) multiplier = 1.0;
        int badgeBonus = new AchievementDAO().dailyBonus(userId);
        int rewardCoins = AchievementPolicy.dailyReward(baseReward, multiplier, badgeBonus);

        String insertSql = "INSERT INTO daily_checkins (user_id, checkin_date, streak_days, reward_coins) "
                         + "VALUES (?, CURRENT_DATE(), ?, ?)";
        String addWalletSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String logTxSql = "INSERT INTO transactions (from_user_id, to_user_id, amount, message) "
                        + "VALUES (NULL, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                if (!lockUser(con, userId) || exists(con, "SELECT 1 FROM daily_checkins WHERE user_id = ? AND checkin_date = CURRENT_DATE()", userId, null)) {
                    con.rollback(); return null;
                }
                // 1. Ghi nhận điểm danh
                try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, newStreak);
                    ps.setInt(3, rewardCoins);
                    ps.executeUpdate();
                }

                ensureWallet(con, userId);
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
                    String bonusNote = " (cấp bậc/VIP x" + multiplier + ", huy hiệu +" + badgeBonus + "% trên gốc)";
                    ps.setString(3, "Thưởng điểm danh ngày " + newStreak + ": " + rewardCoins + " xu" + bonusNote);
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

        int badgeBonus = new AchievementDAO().dailyBonus(userId);
        User user = new UserDAO().findById(userId);
        if (user != null) user.setReadChapters(new UserDAO().countReadChapters(userId));
        double multiplier = user == null ? 1 : user.getCheckinMultiplier();
        java.util.Map<String, Integer> paid = claimedRewards(userId);

        // 1. Nhiệm vụ: Điểm danh ngày mới
        DailyCheckin checkin = getTodayCheckin(userId);
        quests.add(new DailyQuest(
            "DAILY_CHECKIN",
            "Điểm danh ngày mới",
            "Đăng nhập và điểm danh hàng ngày nhận xu thưởng",
            checkin != null ? checkin.getRewardCoins()
                    : AchievementPolicy.dailyReward(STREAK_REWARDS[getCurrentStreak(userId) % 7], multiplier, badgeBonus),
            1,
            checkin != null ? 1 : 0,
            checkin != null // Đã nhận cùng lúc bấm điểm danh
        ));

        // 2. Nhiệm vụ: Mọt sách siêng năng (Đọc ít nhất 1 chương truyện hôm nay)
        int readTodayCount = 0;
        String readSql = "SELECT COUNT(*) FROM user_chapter_reads WHERE user_id = ? AND DATE(last_read_at) = CURRENT_DATE()";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(readSql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) readTodayCount = rs.getInt(1);
            }
        }
        boolean readClaimed = paid.containsKey("READ_CHAPTER");
        quests.add(new DailyQuest(
            "READ_CHAPTER",
            "Mọt sách siêng năng",
            "Đọc ít nhất 1 chương truyện trong ngày",
            paid.getOrDefault("READ_CHAPTER", AchievementPolicy.dailyReward(10, 1, badgeBonus)),
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
        boolean reviewClaimed = paid.containsKey("COMMENT_REVIEW");
        quests.add(new DailyQuest(
            "COMMENT_REVIEW",
            "Nhà phê bình nhiệt huyết",
            "Để lại 1 bình luận hoặc đánh giá cho truyện hôm nay",
            paid.getOrDefault("COMMENT_REVIEW", AchievementPolicy.dailyReward(15, 1, badgeBonus)),
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
            String readSql = "SELECT COUNT(*) FROM user_chapter_reads WHERE user_id = ? AND DATE(last_read_at) = CURRENT_DATE()";
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

        rewardCoins = AchievementPolicy.dailyReward(rewardCoins, 1, new AchievementDAO().dailyBonus(userId));
        String claimSql = "INSERT INTO daily_quest_claims (user_id, quest_key, claim_date, reward_coins) "
                        + "VALUES (?, ?, CURRENT_DATE(), ?)";
        String addWalletSql = "INSERT INTO wallets (user_id, balance) VALUES (?, ?) "
                            + "ON DUPLICATE KEY UPDATE balance = balance + ?";
        String logTxSql = "INSERT INTO transactions (from_user_id, to_user_id, amount, message) "
                        + "VALUES (NULL, ?, ?, ?)";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                if (!lockUser(con, userId) || exists(con,
                        "SELECT 1 FROM daily_quest_claims WHERE user_id = ? AND quest_key = ? AND claim_date = CURRENT_DATE()", userId, questKey)) {
                    con.rollback(); return false;
                }
                try (PreparedStatement ps = con.prepareStatement(claimSql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, questKey);
                    ps.setInt(3, rewardCoins);
                    ps.executeUpdate();
                }

                ensureWallet(con, userId);
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
        try (Connection con = DBConnection.get()) {
            return exists(con, "SELECT 1 FROM daily_quest_claims WHERE user_id = ? AND quest_key = ? AND claim_date = CURRENT_DATE()", userId, questKey);
        }
    }

    private void ensureWallet(Connection con, int userId) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO wallets (user_id, balance) VALUES (?, 100)")) {
            ps.setInt(1, userId); ps.executeUpdate();
        }
    }

    private boolean lockUser(Connection con, int userId) throws SQLException {
        return exists(con, "SELECT id FROM users WHERE id = ? AND status = 'ACTIVE' FOR UPDATE", userId, null);
    }

    private boolean exists(Connection con, String sql, int userId, String key) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId); if (key != null) ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    private java.util.Map<String, Integer> claimedRewards(int userId) throws SQLException {
        java.util.Map<String, Integer> paid = new java.util.HashMap<>();
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(
                "SELECT quest_key, reward_coins FROM daily_quest_claims WHERE user_id = ? AND claim_date = CURRENT_DATE()")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) paid.put(rs.getString("quest_key"), rs.getInt("reward_coins"));
            }
        }
        return paid;
    }
}
