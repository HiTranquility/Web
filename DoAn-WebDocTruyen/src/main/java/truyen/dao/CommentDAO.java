package truyen.dao;

import truyen.util.DemoData;

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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.comments(storyId);
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

    /**
     * Toan bo binh luan cho trang quan tri — trang 29.
     *
     * Lay ca binh luan DA AN. Trang quan tri phai thay duoc thu minh da an,
     * neu khong thi bam an xong la no bien mat luon, khong con duong hien lai.
     *
     * @param onlyHidden true = chi xem cac binh luan da an
     */
    public List<Comment> findAllForAdmin(boolean onlyHidden) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.allComments(onlyHidden);

        String sql =
            "SELECT c.*, u.username, u.display_name, s.title AS story_title "
          + "FROM comments c "
          + "JOIN users   u ON u.id = c.user_id "
          + "JOIN stories s ON s.id = c.story_id "
          + (onlyHidden ? "WHERE c.status = 'HIDDEN' " : "")
          + "ORDER BY c.created_at DESC LIMIT 200";

        List<Comment> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Comment c = mapRow(rs);
                // story_title khong phai cot cua comments — muon truong
                // content de cho sang JSP thi se de nham. Dat han mot truong
                // rieng trong model Comment moi la cach dung.
                c.setStoryTitle(rs.getString("story_title"));
                list.add(c);
            }
        }
        return list;
    }

    /** Hien lai mot binh luan da an. */
    public void unhide(int id) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "UPDATE comments SET status = 'VISIBLE' WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.comments(storyId).size();
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
