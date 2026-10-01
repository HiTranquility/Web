package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Review;
import truyen.util.DBConnection;

/**
 * Quản lý bài đánh giá chi tiết cho truyện kèm điểm sao và nhãn spoiler (ISSUE-022).
 */
public class ReviewDAO {

    /**
     * Lấy danh sách bài đánh giá hiển thị (status = 'VISIBLE') của một truyện,
     * sắp xếp theo số lượt 'Có ích' giảm dần, rồi đến ngày mới nhất.
     */
    public List<Review> findByStory(int storyId, int currentUserId) throws SQLException {
        List<Review> list = new ArrayList<>();
        if (storyId <= 0 || !DBConnection.isReady()) return list;

        String sql = "SELECT r.id, r.user_id, r.story_id, r.title, r.content, r.has_spoiler, "
                   + "       r.helpful_count, r.status, r.created_at, r.updated_at, "
                   + "       u.username, u.display_name, u.avatar_url AS avatar, "
                   + "       COALESCE(rt.score, 5) AS score, "
                   + "       (SELECT COUNT(*) FROM review_votes rv WHERE rv.review_id = r.id AND rv.user_id = ?) > 0 AS voted_by_me "
                   + "FROM reviews r "
                   + "JOIN users u ON u.id = r.user_id "
                   + "LEFT JOIN ratings rt ON rt.user_id = r.user_id AND rt.story_id = r.story_id "
                   + "WHERE r.story_id = ? AND r.status = 'VISIBLE' "
                   + "ORDER BY r.helpful_count DESC, r.created_at DESC";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, currentUserId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * Lấy bài đánh giá của chính người dùng đối với một truyện (nếu có).
     */
    public Review findByUserAndStory(int userId, int storyId) throws SQLException {
        if (userId <= 0 || storyId <= 0 || !DBConnection.isReady()) return null;

        String sql = "SELECT r.id, r.user_id, r.story_id, r.title, r.content, r.has_spoiler, "
                   + "       r.helpful_count, r.status, r.created_at, r.updated_at, "
                   + "       u.username, u.display_name, u.avatar_url AS avatar, "
                   + "       COALESCE(rt.score, 5) AS score, "
                   + "       0 AS voted_by_me "
                   + "FROM reviews r "
                   + "JOIN users u ON u.id = r.user_id "
                   + "LEFT JOIN ratings rt ON rt.user_id = r.user_id AND rt.story_id = r.story_id "
                   + "WHERE r.user_id = ? AND r.story_id = ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /**
     * Thêm mới hoặc cập nhật bài đánh giá (Upsert: Một người chỉ có 1 bài / truyện).
     */
    public boolean upsert(Review review) throws SQLException {
        if (review.getUserId() <= 0 || review.getStoryId() <= 0) return false;
        if (!DBConnection.isReady()) return true;

        String sql = "INSERT INTO reviews (user_id, story_id, title, content, has_spoiler) "
                   + "VALUES (?, ?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE title = VALUES(title), content = VALUES(content), "
                   + "has_spoiler = VALUES(has_spoiler), status = 'VISIBLE', updated_at = CURRENT_TIMESTAMP";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, review.getUserId());
            ps.setInt(2, review.getStoryId());
            ps.setString(3, review.getTitle());
            ps.setString(4, review.getContent());
            ps.setBoolean(5, review.isHasSpoiler());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bấm hoặc bỏ bấm "Có ích" (Toggle helpful vote).
     * Atomic transaction: cập nhật review_votes và helpful_count cùng lúc.
     * @return true nếu trạng thái mới là ĐÃ VOTE, false nếu là HỦY VOTE.
     */
    public boolean toggleVote(int reviewId, int userId) throws SQLException {
        if (reviewId <= 0 || userId <= 0 || !DBConnection.isReady()) return false;

        String checkSql = "SELECT 1 FROM review_votes WHERE review_id = ? AND user_id = ?";
        String insertSql = "INSERT INTO review_votes (review_id, user_id) VALUES (?, ?)";
        String deleteSql = "DELETE FROM review_votes WHERE review_id = ? AND user_id = ?";
        String incSql = "UPDATE reviews SET helpful_count = helpful_count + 1 WHERE id = ?";
        String decSql = "UPDATE reviews SET helpful_count = GREATEST(0, helpful_count - 1) WHERE id = ?";

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                boolean alreadyVoted = false;
                try (PreparedStatement ps = con.prepareStatement(checkSql)) {
                    ps.setInt(1, reviewId);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        alreadyVoted = rs.next();
                    }
                }

                if (alreadyVoted) {
                    // Hủy vote
                    try (PreparedStatement ps = con.prepareStatement(deleteSql)) {
                        ps.setInt(1, reviewId);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(decSql)) {
                        ps.setInt(1, reviewId);
                        ps.executeUpdate();
                    }
                    con.commit();
                    return false;
                } else {
                    // Thêm vote
                    try (PreparedStatement ps = con.prepareStatement(insertSql)) {
                        ps.setInt(1, reviewId);
                        ps.setInt(2, userId);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = con.prepareStatement(incSql)) {
                        ps.setInt(1, reviewId);
                        ps.executeUpdate();
                    }
                    con.commit();
                    return true;
                }
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    /**
     * Ẩn bài đánh giá vi phạm (Admin moderation).
     */
    public boolean hide(int reviewId) throws SQLException {
        if (reviewId <= 0 || !DBConnection.isReady()) return false;

        String sql = "UPDATE reviews SET status = 'HIDDEN' WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reviewId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Đếm tổng số bài đánh giá hiển thị của truyện.
     */
    public int countByStory(int storyId) throws SQLException {
        if (storyId <= 0 || !DBConnection.isReady()) return 0;

        String sql = "SELECT COUNT(*) FROM reviews WHERE story_id = ? AND status = 'VISIBLE'";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Review mapRow(ResultSet rs) throws SQLException {
        Review r = new Review();
        r.setId(rs.getInt("id"));
        r.setUserId(rs.getInt("user_id"));
        r.setStoryId(rs.getInt("story_id"));
        r.setTitle(rs.getString("title"));
        r.setContent(rs.getString("content"));
        r.setHasSpoiler(rs.getBoolean("has_spoiler"));
        r.setHelpfulCount(rs.getInt("helpful_count"));
        r.setStatus(rs.getString("status"));
        if (rs.getTimestamp("created_at") != null) {
            r.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            r.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        r.setUsername(rs.getString("username"));
        r.setUserDisplayName(rs.getString("display_name"));
        r.setUserAvatar(rs.getString("avatar"));
        r.setScore(rs.getInt("score"));
        try {
            r.setVotedByMe(rs.getBoolean("voted_by_me"));
        } catch (SQLException ignore) {}
        return r;
    }
}
