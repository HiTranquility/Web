package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import truyen.model.ReadHistory;
import truyen.util.DBConnection;
import truyen.util.DemoData;

/**
 * TRANG 31 — Lịch sử đọc, đọc từ bảng view_logs.
 *
 * VÌ SAO TÁCH RA DAO RIÊNG, KHÔNG NHÉT VÀO StoryDAO
 *   StoryDAO cũng có đụng view_logs, nhưng ở đó nó chỉ là bảng phụ để xếp
 *   hạng: "truyện nào nhiều lượt xem trong 7 ngày". Câu hỏi ở đây ngược lại
 *   hẳn — "NGƯỜI NÀY đã đọc những truyện nào" — chủ thể là người dùng chứ
 *   không phải truyện. Hai câu hỏi khác chủ thể thì nên ở hai lớp khác nhau.
 */
public class ViewLogDAO {

    /**
     * Lịch sử đọc của một người, mới nhất trước, mỗi truyện CHỈ MỘT DÒNG.
     *
     * VÌ SAO PHẢI GOM NHÓM
     *   view_logs ghi MỘT DÒNG MỖI LẦN MỞ. Đọc một truyện 40 chương trong ba
     *   ngày là mấy chục dòng cùng một truyện. Liệt kê thô ra thì lịch sử chỉ
     *   toàn một cái tên lặp lại — vô dụng.
     *   Gom theo story_id, lấy MAX(viewed_at) làm mốc "lần gần nhất", và
     *   COUNT(*) thành thông tin có ích: "đã mở 12 lần".
     *
     * LEFT JOIN bookmarks — KHÔNG PHẢI JOIN
     *   Vị trí đọc nằm ở bookmarks.last_chapter_id, mà view_logs thì ghi cả
     *   truyện người ta chỉ ngó qua rồi thôi, không lưu. Dùng JOIN thường là
     *   những truyện đó biến mất khỏi lịch sử — đúng cái loại truyện người
     *   dùng cần lịch sử để tìm lại nhất.
     *
     * WHERE s.status = 'PUBLISHED'
     *   Truyện bị gỡ hoặc chuyển về nháp sau khi đã đọc thì không hiện nữa.
     *   Không thì bấm vào chỉ nhận 404.
     */
    public List<ReadHistory> findByUser(int userId, int limit) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.history(userId, limit);

        String sql =
            "SELECT s.id, s.title, s.cover_url, "
          + "       COALESCE(u.display_name, u.username) AS author_name, "
          + "       (SELECT COUNT(*) FROM chapters c WHERE c.story_id = s.id) AS total_chapters, "
          + "       v.last_viewed, v.times, "
          + "       COALESCE(b.last_chapter_id, 0) AS last_chapter_id, "
          + "       COALESCE(ch.chapter_no, 0)     AS last_chapter_no "
          + "FROM ( "
          + "   SELECT story_id, MAX(viewed_at) AS last_viewed, COUNT(*) AS times "
          + "   FROM view_logs "
          + "   WHERE user_id = ? "
          + "   GROUP BY story_id "
          + ") v "
          + "JOIN stories s ON s.id = v.story_id "
          + "JOIN users   u ON u.id = s.author_id "
          + "LEFT JOIN bookmarks b ON b.story_id = s.id AND b.user_id = ? "
          + "LEFT JOIN chapters  ch ON ch.id = b.last_chapter_id "
          + "WHERE s.status = 'PUBLISHED' "
          + "ORDER BY v.last_viewed DESC "
          + "LIMIT ?";

        List<ReadHistory> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ReadHistory h = new ReadHistory();
                    h.setStoryId(rs.getInt("id"));
                    h.setStoryTitle(rs.getString("title"));
                    h.setCoverUrl(rs.getString("cover_url"));
                    h.setAuthorName(rs.getString("author_name"));
                    h.setTotalChapters(rs.getInt("total_chapters"));

                    Timestamp ts = rs.getTimestamp("last_viewed");
                    if (ts != null) {
                        h.setLastViewed(ts.toLocalDateTime());
                    }
                    h.setViewTimes(rs.getInt("times"));
                    h.setLastChapterId(rs.getInt("last_chapter_id"));
                    h.setLastChapterNo(rs.getInt("last_chapter_no"));
                    list.add(h);
                }
            }
        }
        return list;
    }

    /**
     * XOÁ LỊCH SỬ — nhưng bằng cách GỠ TÊN, không phải xoá dòng.
     *
     *   UPDATE view_logs SET user_id = NULL   <- đang dùng
     *   DELETE FROM view_logs                 <- KHÔNG dùng
     *
     * VÌ SAO
     *   Mỗi dòng view_logs mang HAI thông tin gộp làm một:
     *       "truyện X được đọc lúc T"   -> của cả hệ thống, để xếp hạng tuần
     *       "người Y là người đọc"      -> của riêng người đó
     *   Người dùng muốn xoá cái thứ hai. DELETE thì xoá luôn cả cái thứ nhất:
     *   một người xoá lịch sử là bảng xếp hạng tuần tụt theo, và truyện của
     *   tác giả khác tự dưng mất lượt xem mà không ai hiểu vì sao.
     *
     *   Đặt user_id = NULL gỡ đúng phần cần gỡ. Cột này vốn đã cho phép NULL
     *   sẵn — đó là cách ghi lượt xem của khách chưa đăng nhập. Nói cách khác,
     *   xoá lịch sử = biến các lượt đọc cũ thành lượt đọc của một người vô
     *   danh. Không khôi phục được, và đó là điều người dùng muốn.
     *
     * @return số lượt đọc đã gỡ tên
     */
    public int clearForUser(int userId) throws SQLException {
        if (!DBConnection.isReady()) return 0;

        String sql = "UPDATE view_logs SET user_id = NULL WHERE user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate();
        }
    }

    /** Đã đọc bao nhiêu truyện khác nhau — hiện ở đầu trang. */
    public int countStories(int userId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.history(userId, 100).size();

        String sql = "SELECT COUNT(DISTINCT story_id) FROM view_logs WHERE user_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
