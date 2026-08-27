package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Comment;
import truyen.util.DBConnection;

/** CASE 07 — Bình luận. */
public class CommentDAO {

    /**
     * Bình luận của một truyện.
     *
     * ĐIỀU KIỆN status = 'VISIBLE' LÀ BẮT BUỘC.
     * Bỏ quên một chỗ là bình luận admin đã gỡ hiện lại trên trang công khai.
     * Đây là cái giá của việc dùng xoá mềm — đổi lại được khả năng khôi phục
     * và giữ bằng chứng.
     */
    public List<Comment> findByStory(int storyId) throws SQLException {
        String sql =
            "SELECT c.id, c.story_id, c.user_id, c.content, c.status, c.created_at, "
          + "       u.username, u.display_name "
          + "FROM comments c JOIN users u ON u.id = c.user_id "
          + "WHERE c.story_id = ? AND c.status = 'VISIBLE' "
          + "ORDER BY c.created_at DESC";

        List<Comment> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Comment findById(int id) throws SQLException {
        String sql =
            "SELECT c.id, c.story_id, c.user_id, c.content, c.status, c.created_at, "
          + "       u.username, u.display_name "
          + "FROM comments c JOIN users u ON u.id = c.user_id WHERE c.id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public void insert(Comment c) throws SQLException {
        String sql = "INSERT INTO comments (story_id, user_id, content, status) "
                   + "VALUES (?, ?, ?, 'VISIBLE')";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getStoryId());
            ps.setInt(2, c.getUserId());
            ps.setString(3, c.getContent());
            ps.executeUpdate();
        }
    }

    /** Ẩn bình luận — XOÁ MỀM, giữ lại làm bằng chứng khi xử lý tài khoản. */
    public void hide(int id) throws SQLException {
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE comments SET status = 'HIDDEN' WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public int countByStory(int storyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM comments WHERE story_id = ? AND status = 'VISIBLE'";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Comment mapRow(ResultSet rs) throws SQLException {
        Comment c = new Comment();
        c.setId(rs.getInt("id"));
        c.setStoryId(rs.getInt("story_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setContent(rs.getString("content"));
        c.setStatus(rs.getString("status"));
        c.setUsername(rs.getString("username"));
        c.setDisplayName(rs.getString("display_name"));
        if (rs.getTimestamp("created_at") != null) {
            c.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        return c;
    }
}
