package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.util.DBConnection;

/**
 * Quản lý mở khoá chương VIP bằng xu ảo (ISSUE-020).
 */
public class UnlockDAO {

    /**
     * Kiểm tra xem người dùng đã mở khoá chương này chưa.
     */
    public boolean hasUnlocked(int userId, int chapterId) throws SQLException {
        if (userId <= 0 || chapterId <= 0) return false;
        if (!DBConnection.isReady()) return false;

        String sql = "SELECT 1 FROM chapter_unlocks WHERE user_id = ? AND chapter_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Ghi nhận mở khoá chương (thường được gọi trong transaction của WalletDAO).
     */
    public void insert(Connection con, int userId, int chapterId, int pricePaid) throws SQLException {
        String sql = "INSERT INTO chapter_unlocks (user_id, chapter_id, price_paid) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            ps.setInt(3, pricePaid);
            ps.executeUpdate();
        }
    }

    /**
     * Danh sách id các chương mà người dùng đã mở khoá trong một truyện.
     */
    public List<Integer> findUnlockedChapterIds(int userId, int storyId) throws SQLException {
        List<Integer> list = new ArrayList<>();
        if (userId <= 0 || storyId <= 0 || !DBConnection.isReady()) return list;

        String sql = "SELECT u.chapter_id FROM chapter_unlocks u "
                   + "JOIN chapters c ON c.id = u.chapter_id "
                   + "WHERE u.user_id = ? AND c.story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getInt(1));
                }
            }
        }
        return list;
    }

    /**
     * Đếm tổng số chương đã mở khoá của một người dùng.
     */
    public int countByUser(int userId) throws SQLException {
        if (userId <= 0 || !DBConnection.isReady()) return 0;

        String sql = "SELECT COUNT(*) FROM chapter_unlocks WHERE user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Đếm số lượt mở khoá của một chương (cho tác giả thống kê).
     */
    public int countByChapter(int chapterId) throws SQLException {
        if (chapterId <= 0 || !DBConnection.isReady()) return 0;

        String sql = "SELECT COUNT(*) FROM chapter_unlocks WHERE chapter_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
