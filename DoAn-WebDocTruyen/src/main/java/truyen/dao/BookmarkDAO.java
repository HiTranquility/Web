package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Bookmark;
import truyen.util.DBConnection;

/** CASE 08 — Đánh dấu truyện. */
public class BookmarkDAO {

    /**
     * Danh sách truyện người dùng đã lưu.
     *
     * LEFT JOIN sang chapters vì last_chapter_id CÓ THỂ NULL (đã lưu nhưng
     * chưa đọc chương nào). JOIN thường sẽ loại mất những dòng đó — bookmark
     * chưa đọc sẽ biến mất khỏi danh sách một cách khó hiểu.
     */
    public List<Bookmark> findByUser(int userId) throws SQLException {
        String sql =
            "SELECT b.user_id, b.story_id, b.last_chapter_id, b.created_at, "
          + "       s.title AS story_title, s.slug AS story_slug, s.cover_url, "
          + "       COALESCE(c.chapter_no, 0) AS last_chapter_no, "
          + "       (SELECT COUNT(*) FROM chapters ch WHERE ch.story_id = s.id) AS total_chapters "
          + "FROM bookmarks b "
          + "JOIN stories s ON s.id = b.story_id "
          + "LEFT JOIN chapters c ON c.id = b.last_chapter_id "
          + "WHERE b.user_id = ? AND s.status = 'PUBLISHED' "
          + "ORDER BY b.created_at DESC";

        List<Bookmark> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Bookmark b = new Bookmark();
                    b.setUserId(rs.getInt("user_id"));
                    b.setStoryId(rs.getInt("story_id"));
                    b.setLastChapterId(rs.getInt("last_chapter_id"));
                    b.setStoryTitle(rs.getString("story_title"));
                    b.setStorySlug(rs.getString("story_slug"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setLastChapterNo(rs.getInt("last_chapter_no"));
                    b.setTotalChapters(rs.getInt("total_chapters"));
                    if (rs.getTimestamp("created_at") != null) {
                        b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    list.add(b);
                }
            }
        }
        return list;
    }

    /** Người này đã lưu truyện này chưa — để nút hiện "Đã lưu" hay "Lưu truyện". */
    public boolean exists(int userId, int storyId) throws SQLException {
        String sql = "SELECT 1 FROM bookmarks WHERE user_id = ? AND story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Thêm bookmark. Đã có rồi thì bỏ qua, KHÔNG báo lỗi.
     *
     * INSERT IGNORE của MySQL: gặp vi phạm khoá chính thì lặng lẽ không làm gì.
     * Hợp ở đây vì bấm nút "Lưu" hai lần không phải là lỗi của người dùng —
     * kết quả mong muốn (truyện đã được lưu) vẫn đạt được.
     */
    public void add(int userId, int storyId) throws SQLException {
        String sql = "INSERT IGNORE INTO bookmarks (user_id, story_id) VALUES (?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            ps.executeUpdate();
        }
    }

    public void remove(int userId, int storyId) throws SQLException {
        String sql = "DELETE FROM bookmarks WHERE user_id = ? AND story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            ps.executeUpdate();
        }
    }

    /**
     * Ghi lại vị trí đọc. Gọi mỗi khi người dùng mở một chương.
     *
     * ON DUPLICATE KEY UPDATE: chưa có bookmark thì tạo mới, có rồi thì cập
     * nhật vị trí. Một câu lệnh thay cho "SELECT xem có chưa, rồi INSERT hoặc
     * UPDATE" — vừa ngắn hơn, vừa tránh được trường hợp hai request cùng lúc
     * cùng thấy "chưa có" rồi cùng INSERT.
     */
    public void updateProgress(int userId, int storyId, int chapterId) throws SQLException {
        String sql = "INSERT INTO bookmarks (user_id, story_id, last_chapter_id) "
                   + "VALUES (?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE last_chapter_id = VALUES(last_chapter_id)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            ps.setInt(3, chapterId);
            ps.executeUpdate();
        }
    }
}
