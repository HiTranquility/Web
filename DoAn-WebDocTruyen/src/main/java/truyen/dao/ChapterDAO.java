package truyen.dao;

import truyen.util.DemoData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Chapter;
import truyen.util.DBConnection;

/** CASE 06 / CASE 09 — Chương truyện. */
public class ChapterDAO {

    /**
     * Mục lục chương — CỐ Ý không lấy cột content.
     *
     * Mỗi chương có thể vài chục KB. Truyện 200 chương mà lấy hết nội dung là
     * kéo vài MB từ database về chỉ để in ra một danh sách tiêu đề. Chỉ lấy
     * cột nào thật sự cần.
     */
    public List<Chapter> findByStory(int storyId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.chapters(storyId);
        String sql =
            "SELECT id, story_id, chapter_no, title, created_at, updated_at "
          + "FROM chapters WHERE story_id = ? ORDER BY chapter_no";

        List<Chapter> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, false));
                }
            }
        }
        return list;
    }

    /** Một chương KÈM nội dung — cho trang đọc. */
    public Chapter findById(int id) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.chapter(id);
        String sql =
            "SELECT c.id, c.story_id, c.chapter_no, c.title, c.content, "
          + "       c.created_at, c.updated_at, s.title AS story_title "
          + "FROM chapters c JOIN stories s ON s.id = c.story_id "
          + "WHERE c.id = ?";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Chapter c = mapRow(rs, true);
                c.setStoryTitle(rs.getString("story_title"));
                return c;
            }
        }
    }

    /** Chương liền trước / liền sau — cho nút điều hướng ở trang đọc. */
    public Chapter findNeighbour(int storyId, int chapterNo, int direction) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.neighbour(storyId, chapterNo, direction);
        String sql = direction < 0
            ? "SELECT id, story_id, chapter_no, title FROM chapters "
            + "WHERE story_id = ? AND chapter_no < ? ORDER BY chapter_no DESC LIMIT 1"
            : "SELECT id, story_id, chapter_no, title FROM chapters "
            + "WHERE story_id = ? AND chapter_no > ? ORDER BY chapter_no ASC LIMIT 1";

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, chapterNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs, false) : null;
            }
        }
    }

    /** Số chương kế tiếp, để form thêm chương điền sẵn. */
    public int nextChapterNo(int storyId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.chapters(storyId).size() + 1;
        String sql = "SELECT COALESCE(MAX(chapter_no), 0) + 1 FROM chapters WHERE story_id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 1;
            }
        }
    }

    public void insert(Chapter c) throws SQLException {
        String sql = "INSERT INTO chapters (story_id, chapter_no, title, content) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getStoryId());
            ps.setInt(2, c.getChapterNo());
            ps.setString(3, c.getTitle());
            ps.setString(4, c.getContent());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    c.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Chapter c) throws SQLException {
        String sql = "UPDATE chapters SET chapter_no = ?, title = ?, content = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, c.getChapterNo());
            ps.setString(2, c.getTitle());
            ps.setString(3, c.getContent());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
        }
    }

    /** Xoá THẬT — khác truyện. Chương không có gì trỏ tới ngoài bookmark, mà
        khoá ngoại của bookmark dùng SET NULL nên không mồ côi. */
    public void delete(int id) throws SQLException {
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement("DELETE FROM chapters WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Tu khoa ngan hon so nay thi FULLTEXT bo qua — xem ghi chu ben duoi. */
    private static final int MIN_FT_LEN = 3;

    /** So ky tu trich dan quanh cho khop, moi ben. */
    private static final int SNIPPET_PAD = 60;

    /** TIM KIEM TRONG NOI DUNG CHUONG. */
    public List<Chapter> searchContent(String keyword, int limit) throws SQLException {
        String kw = keyword == null ? "" : keyword.trim();
        if (kw.isEmpty()) return new ArrayList<>();

        if (!DBConnection.isReady()) return DemoData.searchChapters(kw, limit);

        boolean useFulltext = kw.length() >= MIN_FT_LEN;

        String sql;
        if (useFulltext) {
            sql = "SELECT c.id, c.story_id, c.chapter_no, c.title, c.content, "
                + "       c.created_at, c.updated_at, s.title AS story_title "
                + "FROM chapters c JOIN stories s ON s.id = c.story_id "
                + "WHERE s.status = 'PUBLISHED' "
                + "  AND MATCH(c.title, c.content) AGAINST (? IN BOOLEAN MODE) "
                + "ORDER BY MATCH(c.title, c.content) AGAINST (? IN BOOLEAN MODE) DESC "
                + "LIMIT ?";
        } else {
            sql = "SELECT c.id, c.story_id, c.chapter_no, c.title, c.content, "
                + "       c.created_at, c.updated_at, s.title AS story_title "
                + "FROM chapters c JOIN stories s ON s.id = c.story_id "
                + "WHERE s.status = 'PUBLISHED' "
                + "  AND (c.content LIKE ? OR c.title LIKE ?) "
                + "ORDER BY c.story_id, c.chapter_no "
                + "LIMIT ?";
        }

        List<Chapter> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (useFulltext) {
                // Dau + = tu nay BAT BUOC phai co mat.
                String bool = "+" + removeOperators(kw);
                ps.setString(1, bool);
                ps.setString(2, bool);   // lap lai cho menh de ORDER BY
                ps.setInt(3, limit);
            } else {
                String like = "%" + kw + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setInt(3, limit);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Chapter c = mapRow(rs, true);
                    c.setStoryTitle(rs.getString("story_title"));

                    /*
                     * Thay noi dung day du bang mot doan trich quanh cho khop.
                     *
                     * Ket qua tim kiem 20 chuong ma moi chuong keo theo vai
                     * nghin chu la vai tram KB HTML cho mot trang danh sach.
                     * Cat ngay o day, truoc khi du lieu roi khoi tang dao.
                     */
                    c.setContent(snippet(c.getContent(), kw));
                    list.add(c);
                }
            }
        }
        return list;
    }

    /**
     * Bo ky tu dieu khien cua cu phap boolean fulltext.
     *
     * Danh sach: + - > < ( ) ~ * " @
     * Nguoi dung go mot dau ngoac kep le loi la MySQL nem loi cu phap va ca
     * trang tim kiem chet — trong khi y ho chi la tim mot cau co dau ngoac.
     */
    private String removeOperators(String s) {
        return s.replaceAll("[+\\-><()~*\"@]", " ").trim();
    }

    /**
     * Cat mot doan quanh cho tu khoa xuat hien.
     *
     * Khong tim thay (truong hop FULLTEXT khop o cot title) thi lay tam phan
     * dau. Van co ich: nguoi doc thay duoc chuong noi ve cai gi.
     */
    private String snippet(String content, String keyword) {
        if (content == null) return "";

        String flat = content.replace('\n', ' ').replace('\r', ' ');
        int at = flat.toLowerCase().indexOf(keyword.toLowerCase());

        if (at < 0) {
            return flat.length() <= SNIPPET_PAD * 2
                 ? flat
                 : flat.substring(0, SNIPPET_PAD * 2) + "…";
        }

        int from = Math.max(0, at - SNIPPET_PAD);
        int to   = Math.min(flat.length(), at + keyword.length() + SNIPPET_PAD);
        String cut = flat.substring(from, to);

        if (from > 0) cut = "…" + cut;
        if (to < flat.length()) cut = cut + "…";
        return cut;
    }

    /** CASE 09 — mọi chương KÈM nội dung, để ghép thành file .txt tải về. */
    public List<Chapter> findAllWithContent(int storyId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.chapters(storyId);
        String sql = "SELECT id, story_id, chapter_no, title, content, created_at, updated_at "
                   + "FROM chapters WHERE story_id = ? ORDER BY chapter_no";
        List<Chapter> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs, true));
                }
            }
        }
        return list;
    }

    /**
     * @param withContent truy vấn có lấy cột content không.
     *        Cần cờ này vì rs.getString("content") trên ResultSet không chứa
     *        cột đó sẽ ném SQLException, không phải trả null.
     */
    private Chapter mapRow(ResultSet rs, boolean withContent) throws SQLException {
        Chapter c = new Chapter();
        c.setId(rs.getInt("id"));
        c.setStoryId(rs.getInt("story_id"));
        c.setChapterNo(rs.getInt("chapter_no"));
        c.setTitle(rs.getString("title"));
        if (withContent) {
            c.setContent(rs.getString("content"));
        }
        return c;
    }
}
