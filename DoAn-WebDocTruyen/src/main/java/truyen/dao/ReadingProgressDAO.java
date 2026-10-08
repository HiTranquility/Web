package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import truyen.model.ReadingProgress;
import truyen.util.DBConnection;

/**
 * Vị trí đọc — chương + đoạn văn (bảng reading_progress).
 *
 * HAI ĐƯỜNG GHI
 *   touch() — ChapterServlet gọi lúc MỞ chương. Không cần JavaScript, nên tắt
 *             JS vẫn nhớ được chương.
 *   save()  — trang đọc gửi lên (navigator.sendBeacon) khi người đọc dừng
 *             cuộn hoặc rời trang: biết chính xác đang ở ĐOẠN nào, và ở
 *             CHƯƠNG nào khi đọc liên tục nối nhiều chương vào một trang.
 */
public class ReadingProgressDAO {

    /** Vị trí đọc của một người trong một truyện, null nếu chưa đọc chương nào. */
    public ReadingProgress find(int userId, int storyId) throws SQLException {
        if (!DBConnection.isReady()) return null;
        String sql =
            "SELECT rp.user_id, rp.story_id, rp.chapter_id, rp.para_no, c.chapter_no "
          + "FROM reading_progress rp "
          + "JOIN chapters c ON c.id = rp.chapter_id "
          + "WHERE rp.user_id = ? AND rp.story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ReadingProgress p = new ReadingProgress();
                p.setUserId(rs.getInt("user_id"));
                p.setStoryId(rs.getInt("story_id"));
                p.setChapterId(rs.getInt("chapter_id"));
                p.setParaNo(rs.getInt("para_no"));
                p.setChapterNo(rs.getInt("chapter_no"));
                return p;
            }
        }
    }

    /**
     * Ghi "đang đọc chương này" lúc mở trang.
     *
     * Mở lại ĐÚNG chương đang dở thì GIỮ số đoạn cũ — không thì bấm "Đọc tiếp"
     * xong, chính lần mở trang đó đã xoá mất vị trí cần quay về. Sang chương
     * khác thì về đầu chương (para_no = 0).
     *
     * Thứ tự hai vế trong ON DUPLICATE KEY UPDATE là CỐ Ý: MySQL gán từ trái
     * sang phải, vế sau thấy giá trị vế trước vừa gán. para_no phải so với
     * chapter_id CŨ, nên phải đứng trước.
     */
    public void touch(int userId, int storyId, int chapterId) throws SQLException {
        // CHE DO XEM GIAO DIEN: bo qua, ly do giong increaseView().
        if (!DBConnection.isReady()) return;

        String sql = "INSERT INTO reading_progress (user_id, story_id, chapter_id, para_no) "
                   + "VALUES (?, ?, ?, 0) "
                   + "ON DUPLICATE KEY UPDATE "
                   + "  para_no = IF(chapter_id <=> VALUES(chapter_id), para_no, 0), "
                   + "  chapter_id = VALUES(chapter_id)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            ps.setInt(3, chapterId);
            ps.executeUpdate();
        }
    }

    /** Ghi chính xác chương + đoạn đang đọc — từ beacon của trang đọc. */
    public void save(int userId, int storyId, int chapterId, int paraNo) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "INSERT INTO reading_progress (user_id, story_id, chapter_id, para_no) "
                   + "VALUES (?, ?, ?, ?) "
                   + "ON DUPLICATE KEY UPDATE chapter_id = VALUES(chapter_id), "
                   + "                        para_no = VALUES(para_no)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, storyId);
            ps.setInt(3, chapterId);
            ps.setInt(4, paraNo);
            ps.executeUpdate();
        }
    }
}
