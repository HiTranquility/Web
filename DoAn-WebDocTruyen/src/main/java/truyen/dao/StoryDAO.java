package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import truyen.model.Story;
import truyen.util.DBConnection;

/**
 * Tầng truy cập dữ liệu cho bảng stories.
 *
 * QUY TẮC CỦA CẢ TẦNG DAO — áp dụng cho mọi lớp trong package này:
 *
 *  1. CHỈ nói chuyện với database. Không forward, không đụng tới
 *     HttpServletRequest, không quyết định hiển thị gì. Servlet lo phần đó.
 *
 *  2. LUÔN dùng PreparedStatement với dấu ?, TUYỆT ĐỐI không nối chuỗi SQL.
 *     Đây là hàng rào chống SQL injection, và là lỗi bảo mật kinh điển nhất
 *     của sinh viên. Giải thích kỹ ở method findLatest bên dưới.
 *
 *  3. LUÔN try-with-resources. Connection/Statement/ResultSet đều là tài
 *     nguyên phải trả lại. Rò rỉ kết nối làm sập cả web sau vài chục request.
 *
 *  4. Ném SQLException lên trên, đừng nuốt. Tầng này không biết phải làm gì
 *     khi lỗi; servlet mới biết (hiện thông báo, ghi log).
 */
public class StoryDAO {

    /*
     * Viết SQL ra hằng số thay vì nhét thẳng vào lời gọi method:
     * dễ đọc, dễ sửa, và copy sang MySQL Workbench chạy thử được ngay.
     *
     * JOIN sang users để lấy luôn tên tác giả trong MỘT truy vấn.
     * Nếu lấy danh sách truyện rồi lặp qua từng truyện để truy vấn tên tác giả
     * thì 20 truyện = 21 truy vấn. Đó gọi là lỗi N+1, và là nguyên nhân phổ
     * biến nhất khiến trang danh sách chậm.
     */
    private static final String SELECT_BASE =
        "SELECT s.id, s.title, s.slug, s.description, s.cover_url, "
      + "       s.author_id, s.status, s.progress, s.view_count, "
      + "       s.created_at, s.updated_at, "
      + "       u.username AS author_name, "
      + "       (SELECT COUNT(*) FROM chapters c WHERE c.story_id = s.id) AS chapter_count "
      + "FROM stories s "
      + "JOIN users u ON u.id = s.author_id ";

    /**
     * Truyện mới cập nhật gần đây nhất, để hiện ở trang chủ.
     *
     * @param limit số truyện tối đa
     */
    public List<Story> findLatest(int limit) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE s.status = 'PUBLISHED' "
                   + "ORDER BY s.updated_at DESC "
                   + "LIMIT ?";

        List<Story> list = new ArrayList<>();

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {

            /*
             * ĐÂY LÀ ĐIỂM QUAN TRỌNG NHẤT CỦA CẢ FILE.
             *
             * Giá trị đi vào truy vấn qua setInt/setString, KHÔNG nối chuỗi.
             * So sánh hai cách:
             *
             *     SAI:   "... LIMIT " + limit
             *     ĐÚNG:  "... LIMIT ?"  rồi  ps.setInt(1, limit)
             *
             * Với số thì nhìn có vẻ vô hại, nhưng với chuỗi từ người dùng thì
             * nối chuỗi là thảm hoạ. Ví dụ ô tìm kiếm nhập:
             *
             *     ' OR '1'='1
             *
             * Nối chuỗi sẽ thành  WHERE title LIKE '%' OR '1'='1'%'  — trả về
             * toàn bộ bảng. Nhập thứ khác còn xoá được cả bảng.
             *
             * PreparedStatement gửi câu lệnh và dữ liệu qua HAI đường riêng
             * biệt, nên dữ liệu không bao giờ được hiểu là câu lệnh. Đó là lý
             * do nó an toàn — không phải vì nó "lọc ký tự xấu".
             */
            ps.setInt(1, limit);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Truyện nhiều lượt xem nhất. */
    public List<Story> findPopular(int limit) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE s.status = 'PUBLISHED' "
                   + "ORDER BY s.view_count DESC "
                   + "LIMIT ?";

        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Tổng số truyện đã công khai — cho phần thống kê ở trang chủ. */
    public int countPublished() throws SQLException {
        String sql = "SELECT COUNT(*) FROM stories WHERE status = 'PUBLISHED'";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }


    // ========================================================================
    //  CASE 02 / 03 — Danh sach co phan trang va loc theo the loai
    // ========================================================================

    /**
     * Danh sach truyen, co phan trang va loc tuy chon theo the loai.
     *
     * XAY CAU SQL DONG MA VAN AN TOAN — doc ky cho nay.
     *   Cau lenh duoc ghep tu nhieu manh vi dieu kien loc thay doi theo tham so.
     *   Nhung thu duoc ghep chi la KHUNG cau lenh (" AND t.slug = ? "), con
     *   GIA TRI thi luon di qua dau ? va setInt/setString.
     *
     *   Ghep khung  : an toan, vi khung do code minh viet ra.
     *   Ghep gia tri: SQL injection.
     *
     * @param tagSlug null hoac rong = khong loc
     * @param sort    "popular" = nhieu luot xem; con lai = moi cap nhat
     * @param offset  bo qua bao nhieu dong dau (trang 2 voi 24/trang -> 24)
     */
    public List<Story> findPage(String tagSlug, String keyword, String sort,
                                int offset, int limit) throws SQLException {

        StringBuilder sql = new StringBuilder(SELECT_BASE);
        List<Object> params = new ArrayList<>();

        if (tagSlug != null && !tagSlug.isEmpty()) {
            sql.append("JOIN story_tags st ON st.story_id = s.id ")
               .append("JOIN tags t ON t.id = st.tag_id ");
        }
        sql.append("WHERE s.status = 'PUBLISHED' ");

        if (tagSlug != null && !tagSlug.isEmpty()) {
            sql.append("AND t.slug = ? ");
            params.add(tagSlug);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND s.title LIKE ? ");
            // Dau % nam trong GIA TRI, khong nam trong cau lenh -> van an toan
            params.add("%" + keyword + "%");
        }

        sql.append("popular".equals(sort)
                ? "ORDER BY s.view_count DESC "
                : "ORDER BY s.updated_at DESC ");
        sql.append("LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);

        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Tong so truyen khop bo loc — de tinh so trang. */
    public int countPage(String tagSlug, String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(DISTINCT s.id) FROM stories s ");
        List<Object> params = new ArrayList<>();

        if (tagSlug != null && !tagSlug.isEmpty()) {
            sql.append("JOIN story_tags st ON st.story_id = s.id ")
               .append("JOIN tags t ON t.id = st.tag_id ");
        }
        sql.append("WHERE s.status = 'PUBLISHED' ");
        if (tagSlug != null && !tagSlug.isEmpty()) {
            sql.append("AND t.slug = ? ");
            params.add(tagSlug);
        }
        if (keyword != null && !keyword.isEmpty()) {
            sql.append("AND s.title LIKE ? ");
            params.add("%" + keyword + "%");
        }

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Gan danh sach tham so vao PreparedStatement theo dung thu tu. */
    private void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object v = params.get(i);
            if (v instanceof Integer) {
                ps.setInt(i + 1, (Integer) v);
            } else {
                ps.setString(i + 1, String.valueOf(v));
            }
        }
    }

    // ========================================================================
    //  CASE 04 / 05 — Chi tiet, them, sua, xoa
    // ========================================================================

    /**
     * Mot truyen theo id. KHONG loc theo status — vi tac gia can xem duoc
     * truyen DRAFT cua chinh minh, va admin can xem duoc truyen da go.
     * Viec quyet dinh AI DUOC XEM la cua SERVLET, khong phai cua DAO.
     */
    public Story findById(int id) throws SQLException {
        String sql = SELECT_BASE + "WHERE s.id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    /** Truyen cua mot tac gia — cho trang "Truyen cua toi". Gom ca DRAFT. */
    public List<Story> findByAuthor(int authorId) throws SQLException {
        String sql = SELECT_BASE
                   + "WHERE s.author_id = ? AND s.status != 'DELETED' "
                   + "ORDER BY s.updated_at DESC";
        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Slug nay da co truyen nao dung chua (bo qua chinh truyen dang sua). */
    public boolean slugExists(String slug, int exceptId) throws SQLException {
        String sql = "SELECT 1 FROM stories WHERE slug = ? AND id != ? LIMIT 1";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, slug);
            ps.setInt(2, exceptId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void insert(Story s) throws SQLException {
        String sql = "INSERT INTO stories (title, slug, description, cover_url, "
                   + "author_id, status, progress) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getSlug());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getCoverUrl());
            ps.setInt(5, s.getAuthorId());
            ps.setString(6, s.getStatus());
            ps.setString(7, s.getProgress());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    s.setId(keys.getInt(1));
                }
            }
        }
    }

    public void update(Story s) throws SQLException {
        String sql = "UPDATE stories SET title = ?, slug = ?, description = ?, "
                   + "cover_url = ?, status = ?, progress = ? WHERE id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, s.getTitle());
            ps.setString(2, s.getSlug());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getCoverUrl());
            ps.setString(5, s.getStatus());
            ps.setString(6, s.getProgress());
            ps.setInt(7, s.getId());
            ps.executeUpdate();
        }
    }

    /**
     * XOA MEM — doi status thanh DELETED, khong DELETE FROM.
     * Xoa that thi binh luan va bookmark tro toi truyen se mo coi, phai xu ly
     * day chuyen rat met. An di la du, va admin go nham con khoi phuc duoc.
     */
    public void softDelete(int id) throws SQLException {
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE stories SET status = 'DELETED' WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Tang luot xem.
     *
     * Dung "view_count = view_count + 1" — de DATABASE tu cong, khong phai doc
     * so cu ve Java roi cong roi ghi lai. Cach sau bi MAT luot xem khi hai
     * nguoi mo cung luc (dung bai toan lost update o chuong 5).
     */
    public void increaseView(int id) throws SQLException {
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE stories SET view_count = view_count + 1 WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ========================================================================
    //  CASE 10 — Cho trang quan tri
    // ========================================================================

    /** Moi truyen, gom ca DRAFT va DELETED — chi admin goi. */
    public List<Story> findAllForAdmin() throws SQLException {
        String sql = SELECT_BASE + "ORDER BY s.updated_at DESC LIMIT 200";
        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public void updateStatus(int id, String status) throws SQLException {
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE stories SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /*
     * Đổi MỘT dòng ResultSet thành MỘT object Story.
     *
     * Tách riêng ra vì mọi method truy vấn ở trên đều cần. Sau này thêm cột
     * mới thì sửa đúng một chỗ này, thay vì đi sửa từng vòng lặp — và chắc
     * chắn sẽ sót một chỗ nếu không tách.
     */
    private Story mapRow(ResultSet rs) throws SQLException {
        Story s = new Story();
        s.setId(rs.getInt("id"));
        s.setTitle(rs.getString("title"));
        s.setSlug(rs.getString("slug"));
        s.setDescription(rs.getString("description"));
        s.setCoverUrl(rs.getString("cover_url"));
        s.setAuthorId(rs.getInt("author_id"));
        s.setAuthorName(rs.getString("author_name"));
        s.setStatus(rs.getString("status"));
        s.setProgress(rs.getString("progress"));
        s.setViewCount(rs.getInt("view_count"));
        s.setChapterCount(rs.getInt("chapter_count"));

        // getTimestamp trả về null nếu cột NULL, nên phải kiểm tra trước khi
        // gọi toLocalDateTime() — không thì NullPointerException.
        if (rs.getTimestamp("created_at") != null) {
            s.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            s.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }
        return s;
    }
}
