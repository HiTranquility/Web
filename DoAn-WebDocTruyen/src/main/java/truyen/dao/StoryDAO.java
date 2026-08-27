package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
