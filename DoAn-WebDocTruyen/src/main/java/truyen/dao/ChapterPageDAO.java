package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import truyen.model.ChapterPage;
import truyen.util.DBConnection;

/** Trang ảnh của chương truyện tranh (bảng chapter_pages). */
public class ChapterPageDAO {

    /** Trần số trang một chương — chặn một lần tải lên vô hạn làm đầy đĩa. */
    public static final int MAX_PAGES_PER_CHAPTER = 200;

    /** Mọi trang của một chương, đúng thứ tự đọc. */
    public List<ChapterPage> findByChapter(int chapterId) throws SQLException {
        List<ChapterPage> list = new ArrayList<>();
        if (!DBConnection.isReady()) return list;
        String sql = "SELECT id, chapter_id, page_no, image_path, width, height "
                   + "FROM chapter_pages WHERE chapter_id = ? ORDER BY page_no";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChapterPage p = new ChapterPage();
                    p.setId(rs.getInt("id"));
                    p.setChapterId(rs.getInt("chapter_id"));
                    p.setPageNo(rs.getInt("page_no"));
                    p.setImagePath(rs.getString("image_path"));
                    p.setWidth(rs.getInt("width"));
                    p.setHeight(rs.getInt("height"));
                    list.add(p);
                }
            }
        }
        return list;
    }

    /**
     * Ghi lại TOÀN BỘ danh sách trang của một chương theo thứ tự mới:
     * xoá những trang không còn trong danh sách, đánh lại số 1..n cho trang
     * cũ, chèn trang mới (id = 0) vào đúng vị trí. MỘT transaction — dừng giữa
     * chừng thì chương không bao giờ bị nửa cũ nửa mới.
     *
     * VÌ SAO ĐÁNH SỐ HAI LƯỢT
     *   (chapter_id, page_no) là UNIQUE. Đổi trang 1 <-> trang 2 trực tiếp thì
     *   lệnh đầu tiên đã đụng khoá với dòng còn lại. Lượt 1 đẩy mọi trang ra
     *   vùng số âm (không ai dùng), lượt 2 mới gán số thật.
     *
     * @return đường dẫn ảnh của các trang vừa bị xoá — servlet xoá file SAU KHI
     *         commit thành công (xoá file trước rồi rollback là mất ảnh thật).
     */
    public List<String> replaceAll(int chapterId, List<ChapterPage> ordered) throws SQLException {
        List<String> removed = new ArrayList<>();
        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);
            try {
                java.util.Set<Integer> keep = new java.util.HashSet<>();
                for (ChapterPage p : ordered) if (p.getId() > 0) keep.add(p.getId());

                // 1. Trang bị bỏ: nhớ đường dẫn rồi xoá dòng
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT id, image_path FROM chapter_pages WHERE chapter_id = ?")) {
                    ps.setInt(1, chapterId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            if (!keep.contains(rs.getInt(1))) removed.add(rs.getString(2));
                        }
                    }
                }
                try (PreparedStatement del = con.prepareStatement(
                        "DELETE FROM chapter_pages WHERE chapter_id = ? AND image_path = ?")) {
                    for (String path : removed) {
                        del.setInt(1, chapterId);
                        del.setString(2, path);
                        del.addBatch();
                    }
                    del.executeBatch();
                }

                // 2. Lượt 1: đẩy số trang cũ ra vùng âm để khỏi đụng UNIQUE
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE chapter_pages SET page_no = -page_no WHERE chapter_id = ?")) {
                    ps.setInt(1, chapterId);
                    ps.executeUpdate();
                }

                // 3. Lượt 2: gán số thật theo thứ tự mới; trang mới thì chèn
                try (PreparedStatement upd = con.prepareStatement(
                        "UPDATE chapter_pages SET page_no = ? WHERE id = ? AND chapter_id = ?");
                     PreparedStatement ins = con.prepareStatement(
                        "INSERT INTO chapter_pages (chapter_id, page_no, image_path, width, height) "
                      + "VALUES (?, ?, ?, ?, ?)")) {
                    int no = 1;
                    for (ChapterPage p : ordered) {
                        if (p.getId() > 0) {
                            upd.setInt(1, no);
                            upd.setInt(2, p.getId());
                            upd.setInt(3, chapterId);
                            upd.addBatch();
                        } else {
                            ins.setInt(1, chapterId);
                            ins.setInt(2, no);
                            ins.setString(3, p.getImagePath());
                            if (p.isSized()) { ins.setInt(4, p.getWidth()); ins.setInt(5, p.getHeight()); }
                            else { ins.setNull(4, java.sql.Types.INTEGER); ins.setNull(5, java.sql.Types.INTEGER); }
                            ins.addBatch();
                        }
                        p.setPageNo(no++);
                    }
                    upd.executeBatch();
                    ins.executeBatch();
                }

                con.commit();
                return removed;
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public int countByChapter(int chapterId) throws SQLException {
        if (!DBConnection.isReady()) return 0;
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COUNT(*) FROM chapter_pages WHERE chapter_id = ?")) {
            ps.setInt(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Đường dẫn mọi ảnh của chương — gọi TRƯỚC khi xoá chương, để còn dọn file. */
    public List<String> findPaths(int chapterId) throws SQLException {
        List<String> list = new ArrayList<>();
        for (ChapterPage p : findByChapter(chapterId)) list.add(p.getImagePath());
        return list;
    }
}
