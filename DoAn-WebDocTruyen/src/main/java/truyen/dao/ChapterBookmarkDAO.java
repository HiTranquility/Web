package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.ChapterBookmark;
import truyen.util.DBConnection;

/** Đoạn văn người dùng tự đánh dấu để đọc lại (bảng chapter_bookmarks). */
public class ChapterBookmarkDAO {

    /** Mỗi người tối đa chừng này đánh dấu — chặn một script bấm vô hạn làm phình bảng. */
    public static final int MAX_PER_USER = 500;

    /**
     * Đánh dấu một đoạn. Trả về false nếu đã có sẵn (INSERT IGNORE gặp khoá
     * UNIQUE) — bấm hai lần không phải lỗi, chỉ là không có gì mới.
     */
    public boolean add(int userId, int chapterId, int paraNo, String snippet) throws SQLException {
        String sql = "INSERT IGNORE INTO chapter_bookmarks (user_id, chapter_id, para_no, snippet) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            ps.setInt(3, paraNo);
            ps.setString(4, snippet);
            return ps.executeUpdate() > 0;
        }
    }

    /** Bỏ đánh dấu theo vị trí — dùng ở trang đọc, nơi chỉ biết chương + đoạn. */
    public void remove(int userId, int chapterId, int paraNo) throws SQLException {
        String sql = "DELETE FROM chapter_bookmarks WHERE user_id = ? AND chapter_id = ? AND para_no = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            ps.setInt(3, paraNo);
            ps.executeUpdate();
        }
    }

    /**
     * Bỏ đánh dấu theo id — dùng ở trang danh sách.
     * Luôn kèm user_id trong WHERE: đoán được id của người khác cũng không xoá được.
     */
    public void removeById(int userId, int id) throws SQLException {
        String sql = "DELETE FROM chapter_bookmarks WHERE id = ? AND user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public int countByUser(int userId) throws SQLException {
        if (!DBConnection.isReady()) return 0;
        String sql = "SELECT COUNT(*) FROM chapter_bookmarks WHERE user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Những đoạn đã đánh dấu trong một chương — để trang đọc tô sẵn khi mở. */
    public List<Integer> findParaNos(int userId, int chapterId) throws SQLException {
        List<Integer> list = new ArrayList<>();
        if (!DBConnection.isReady()) return list;
        String sql = "SELECT para_no FROM chapter_bookmarks WHERE user_id = ? AND chapter_id = ? "
                   + "ORDER BY para_no";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt(1));
            }
        }
        return list;
    }

    /**
     * Mọi đoạn đã đánh dấu của một người, mới nhất trước.
     * Bỏ qua truyện đã gỡ / về nháp — bấm vào chỉ nhận 404.
     */
    public List<ChapterBookmark> findByUser(int userId) throws SQLException {
        List<ChapterBookmark> list = new ArrayList<>();
        if (!DBConnection.isReady()) return list;
        String sql =
            "SELECT cb.id, cb.user_id, cb.chapter_id, cb.para_no, cb.snippet, cb.created_at, "
          + "       c.chapter_no, c.title AS chapter_title, "
          + "       s.id AS story_id, s.title AS story_title, s.cover_url, s.story_type, "
          // Truyện tranh: ảnh của đúng trang được đánh dấu, làm ảnh thu nhỏ
          + "       cp.image_path AS page_image "
          + "FROM chapter_bookmarks cb "
          + "JOIN chapters c ON c.id = cb.chapter_id "
          + "JOIN stories  s ON s.id = c.story_id "
          + "LEFT JOIN chapter_pages cp ON cp.chapter_id = cb.chapter_id AND cp.page_no = cb.para_no "
          + "WHERE cb.user_id = ? AND s.status = 'PUBLISHED' "
          + "ORDER BY cb.created_at DESC, cb.id DESC "
          + "LIMIT " + MAX_PER_USER;
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChapterBookmark b = new ChapterBookmark();
                    b.setId(rs.getInt("id"));
                    b.setUserId(rs.getInt("user_id"));
                    b.setChapterId(rs.getInt("chapter_id"));
                    b.setParaNo(rs.getInt("para_no"));
                    b.setSnippet(rs.getString("snippet"));
                    if (rs.getTimestamp("created_at") != null) {
                        b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    }
                    b.setChapterNo(rs.getInt("chapter_no"));
                    b.setChapterTitle(rs.getString("chapter_title"));
                    b.setStoryId(rs.getInt("story_id"));
                    b.setStoryTitle(rs.getString("story_title"));
                    b.setCoverUrl(rs.getString("cover_url"));
                    b.setStoryType(rs.getString("story_type"));
                    b.setPageImage(rs.getString("page_image"));
                    list.add(b);
                }
            }
        }
        return list;
    }
}
