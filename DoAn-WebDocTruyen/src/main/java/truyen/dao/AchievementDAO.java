package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import truyen.model.Achievement;
import truyen.model.User;
import truyen.util.AchievementPolicy;
import truyen.util.DBConnection;

/** Ghi nhận thành tích vĩnh viễn và nhận thưởng đúng một lần. */
public class AchievementDAO {
    public boolean canPersistRewards() { return DBConnection.isReady(); }
    private static volatile boolean tablesReady;
    private static final String QUALIFIED_CHAPTER = "(CHAR_LENGTH(TRIM(COALESCE(c.content, ''))) >= 200 "
            + "OR (SELECT COUNT(*) FROM chapter_pages cp WHERE cp.chapter_id = c.id) >= 3)";

    public static synchronized void ensureTables() throws SQLException {
        if (tablesReady || !DBConnection.isReady()) return;
        try (Connection con = DBConnection.get(); Statement st = con.createStatement()) {
            st.executeUpdate("CREATE TABLE IF NOT EXISTS user_achievements ("
                    + "user_id INT NOT NULL, achievement_key VARCHAR(50) NOT NULL, "
                    + "unlocked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "claimed_at DATETIME NULL, reward_coins INT NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (user_id, achievement_key), "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            tablesReady = true;
        }
    }

    public List<Achievement> getAchievements(int userId, User user) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        if (userId > 0 && DBConnection.isReady()) {
            ReadingActivityDAO.ensureTable();
            stats = readStats(userId);
        }
        List<Achievement> list = AchievementPolicy.catalog(stats, user != null && user.isVip());
        if (userId <= 0 || !DBConnection.isReady()) return list;
        ensureTables();
        try (Connection con = DBConnection.get()) {
            syncUnlocked(con, userId, list);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT achievement_key, claimed_at, reward_coins FROM user_achievements WHERE user_id = ?")) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    Map<String, Integer> earned = new HashMap<>();
                    while (rs.next()) earned.put(rs.getString("achievement_key"), rs.getTimestamp("claimed_at") != null ? rs.getInt("reward_coins") : -1);
                    for (Achievement achievement : list) {
                        // VIP theo thời hạn, không ghi thành tích vĩnh viễn.
                        if (earned.containsKey(achievement.getId()) && achievement.getRewardCoins() > 0) {
                            achievement.setUnlocked(true);
                            achievement.setCurrentProgress(achievement.getTargetProgress());
                            achievement.setProgressPercent(100);
                            achievement.setProgressText("Đã đạt — được lưu vĩnh viễn");
                            achievement.setClaimed(earned.get(achievement.getId()) >= 0);
                            if (achievement.isClaimed()) achievement.setRewardCoins(earned.get(achievement.getId()));
                        }
                    }
                }
            }
        }
        return list;
    }

    private Map<String, Integer> readStats(int userId) throws SQLException {
        String sql = "SELECT "
                + "(SELECT COUNT(*) FROM user_chapter_reads WHERE user_id = ?) AS read_count, "
                + "(SELECT COUNT(*) FROM user_chapter_reads WHERE user_id = ? AND night_read = TRUE) AS night_reads, "
                + "(SELECT COUNT(*) FROM transactions WHERE from_user_id = ? AND to_user_id <> from_user_id AND amount > 0 AND message LIKE '[TIP] %') AS tip_count, "
                + "(SELECT COUNT(*) FROM reviews r JOIN stories s ON s.id = r.story_id WHERE r.user_id = ? AND s.author_id <> r.user_id AND s.status = 'PUBLISHED' AND CHAR_LENGTH(TRIM(r.content)) >= 80) AS review_count, "
                + "(SELECT COALESCE(MAX(streak_days), 0) FROM daily_checkins WHERE user_id = ?) AS max_streak, "
                + "(SELECT COUNT(DISTINCT story_id) FROM bookmarks WHERE user_id = ?) AS bookmark_count, "
                + "(SELECT COUNT(*) FROM stories s WHERE s.author_id = ? AND s.status = 'PUBLISHED' AND EXISTS (SELECT 1 FROM chapters c WHERE c.story_id = s.id AND " + QUALIFIED_CHAPTER + ")) AS published_count, "
                + "(SELECT COUNT(*) FROM chapters c JOIN stories s ON s.id = c.story_id WHERE s.author_id = ? AND s.status = 'PUBLISHED' AND " + QUALIFIED_CHAPTER + ") AS published_chapters, "
                + "(SELECT COUNT(*) FROM stories s WHERE s.author_id = ? AND s.status = 'PUBLISHED' AND s.progress = 'COMPLETED' AND (SELECT COUNT(*) FROM chapters c WHERE c.story_id = s.id AND " + QUALIFIED_CHAPTER + ") >= 10) AS completed_count";
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 1; i <= 9; i++) ps.setInt(i, userId);
            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Integer> result = new HashMap<>();
                if (rs.next()) for (String key : new String[] { "read_count", "night_reads", "tip_count", "review_count",
                        "max_streak", "bookmark_count", "published_count", "published_chapters", "completed_count" }) {
                    result.put(key, rs.getInt(key));
                }
                return result;
            }
        }
    }

    private void syncUnlocked(Connection con, int userId, List<Achievement> list) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT IGNORE INTO user_achievements (user_id, achievement_key) VALUES (?, ?)")) {
            for (Achievement value : list) if (value.isUnlocked() && value.getRewardCoins() > 0) {
                ps.setInt(1, userId); ps.setString(2, value.getId()); ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public int dailyBonus(int userId) throws SQLException {
        return AchievementPolicy.dailyBonus(getAchievements(userId, null));
    }

    public boolean claim(int userId, String key) throws SQLException {
        if (userId <= 0 || key == null || !DBConnection.isReady()) return false;
        Achievement value = getAchievements(userId, null).stream()
                .filter(a -> key.equals(a.getId())).findFirst().orElse(null);
        if (value == null || !value.isClaimable()) return false;
        try (Connection con = DBConnection.get()) {
            return claim(con, userId, value);
        }
    }

    boolean claim(Connection con, int userId, Achievement value) throws SQLException {
        con.setAutoCommit(false);
        try {
            // UPDATE có điều kiện khóa hàng: hai yêu cầu đồng thời chỉ một yêu cầu thắng.
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE user_achievements a JOIN users u ON u.id = a.user_id "
                    + "SET a.claimed_at = NOW(), a.reward_coins = ? "
                    + "WHERE a.user_id = ? AND a.achievement_key = ? AND a.claimed_at IS NULL AND u.status = 'ACTIVE'")) {
                ps.setInt(1, value.getRewardCoins()); ps.setInt(2, userId); ps.setString(3, value.getId());
                if (ps.executeUpdate() != 1) { con.rollback(); return false; }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT IGNORE INTO wallets (user_id, balance) VALUES (?, 100)")) {
                ps.setInt(1, userId); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("UPDATE wallets SET balance = balance + ? WHERE user_id = ?")) {
                ps.setInt(1, value.getRewardCoins()); ps.setInt(2, userId);
                if (ps.executeUpdate() != 1) throw new SQLException("Không cộng được thưởng thành tựu.");
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO transactions (from_user_id, to_user_id, amount, message) VALUES (NULL, ?, ?, ?)")) {
                ps.setInt(1, userId); ps.setInt(2, value.getRewardCoins());
                ps.setString(3, "Thưởng thành tựu: " + value.getTitle()); ps.executeUpdate();
            }
            con.commit(); return true;
        } catch (SQLException ex) {
            con.rollback(); throw ex;
        } finally { con.setAutoCommit(true); }
    }

    /** Nạp theo lô; không truy vấn lại từng tác giả khi xếp đề xuất. */
    public Map<Integer, Integer> recommendationBoosts(Collection<Integer> authorIds) throws SQLException {
        if (authorIds.isEmpty() || !DBConnection.isReady()) return Collections.emptyMap();
        ensureTables();
        Map<String, Integer> weights = new HashMap<>();
        for (Achievement a : AchievementPolicy.catalog(Collections.emptyMap(), false)) weights.put(a.getId(), a.getRecommendationWeight());
        String placeholders = String.join(",", Collections.nCopies(authorIds.size(), "?"));
        Map<Integer, Integer> result = new LinkedHashMap<>();
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(
                "SELECT u.id AS user_id, a.achievement_key FROM users u LEFT JOIN user_achievements a ON u.id = a.user_id "
                + "WHERE u.status = 'ACTIVE' AND u.id IN (" + placeholders + ")")) {
            int i = 1; for (Integer id : authorIds) ps.setInt(i++, id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.merge(rs.getInt("user_id"), weights.getOrDefault(rs.getString("achievement_key"), 0), Integer::sum);
            }
        }
        result.replaceAll((id, weight) -> Math.min(AchievementPolicy.MAX_RECOMMENDATION_BOOST, weight));
        return result;
    }
}
