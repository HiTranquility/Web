package truyen.dao;

import truyen.util.DemoData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Bookmark;
import truyen.util.DBConnection;

/**
 * CASE 08 — Truyện đã lưu (bảng bookmarks).
 *
 * CHỈ những truyện người dùng tự bấm "Lưu truyện". Vị trí đọc nằm ở
 * ReadingProgressDAO, đoạn văn đánh dấu nằm ở ChapterBookmarkDAO.
 */
public class BookmarkDAO {

    /**
     * Danh sách truyện người dùng đã lưu.
     *
     * LEFT JOIN sang reading_progress vì truyện đã lưu CÓ THỂ chưa đọc chương
     * nào. JOIN thường sẽ loại mất những dòng đó — truyện lưu để dành đọc sau
     * sẽ biến mất khỏi danh sách một cách khó hiểu.
     */
    public List<Bookmark> findByUser(int userId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.bookmarks(userId);
        String sql =
            "SELECT b.user_id, b.story_id, b.created_at, "
          + "       COALESCE(rp.chapter_id, 0) AS last_chapter_id, "
          + "       s.title AS story_title, s.slug AS story_slug, s.cover_url, "
          + "       COALESCE(c.chapter_no, 0) AS last_chapter_no, "
          + "       (SELECT COUNT(*) FROM chapters ch WHERE ch.story_id = s.id) AS total_chapters "
          + "FROM bookmarks b "
          + "JOIN stories s ON s.id = b.story_id "
          + "LEFT JOIN reading_progress rp ON rp.user_id = b.user_id AND rp.story_id = b.story_id "
          + "LEFT JOIN chapters c ON c.id = rp.chapter_id "
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.bookmarked(storyId);
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

    /** Bỏ lưu. KHÔNG đụng tới vị trí đọc — bỏ lưu không có nghĩa là quên mình đang đọc tới đâu. */
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
     * Những người cần báo khi truyện có chương mới: đã LƯU truyện, hoặc đang
     * ĐỌC DỞ truyện.
     *
     * Trước khi tách bảng, ai mở một chương cũng tự có bookmark nên tự được
     * báo. UNION với reading_progress giữ nguyên hành vi đó cho người đang
     * đọc; UNION (không phải UNION ALL) để một người không nhận hai thông báo.
     */
    public List<Integer> findBookmarkedUserIds(int storyId) throws SQLException {
        if (!DBConnection.isReady()) return java.util.Collections.emptyList();
        String sql = "SELECT user_id FROM bookmarks WHERE story_id = ? "
                   + "UNION "
                   + "SELECT user_id FROM reading_progress WHERE story_id = ?";
        List<Integer> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getInt("user_id"));
            }
        }
        return list;
    }
}
