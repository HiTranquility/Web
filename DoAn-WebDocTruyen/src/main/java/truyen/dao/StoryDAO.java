package truyen.dao;

import truyen.util.DemoData;

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
      + "       s.rating_sum, s.rating_count, "
      + "       s.created_at, s.updated_at, "
      // COALESCE: chua dat ten hien thi thi lay tam ten dang nhap, khong de trong.
      + "       COALESCE(u.display_name, u.username) AS author_name, "
      + "       (SELECT COUNT(*) FROM chapters c WHERE c.story_id = s.id) AS chapter_count "
      + "FROM stories s "
      + "JOIN users u ON u.id = s.author_id ";

    /**
     * Truyện mới cập nhật gần đây nhất, để hiện ở trang chủ.
     *
     * @param limit số truyện tối đa
     */
    public List<Story> findLatest(int limit) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.top("newest", limit);
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.top("views", limit);
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.stories().size();
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
                                String progress, int offset, int limit)
            throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) {
            return DemoData.slice(
                    DemoData.sort(DemoData.filter(tagSlug, keyword, progress), sort),
                    offset, limit);
        }

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
            /*
             * Tim theo TEN TRUYEN hoac TEN TAC GIA.
             *
             * Nguoi doc go "Moc Mien" la ho dang tim tac gia, khong ai nghi
             * phai sang trang khac de tim. Mot o tim kiem lo ca hai la dung
             * ky vong hon.
             *
             * Ba dau ? nhan CUNG mot gia tri nhung van phai them ba lan vao
             * danh sach tham so: PreparedStatement dem theo vi tri dau ?,
             * no khong biet ba cho do la cung mot chuoi.
             */
            sql.append("AND (s.title LIKE ? OR u.display_name LIKE ? OR u.username LIKE ?) ");
            // Dau % nam trong GIA TRI, khong nam trong cau lenh -> van an toan
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if ("ongoing".equals(progress)) {
            sql.append("AND s.progress = 'ONGOING' ");
        } else if ("completed".equals(progress)) {
            sql.append("AND s.progress = 'COMPLETED' ");
        }

        /*
         * ORDER BY khong nhan dau ? — ten cot khong phai gia tri. Nen o day
         * so sanh voi chuoi HANG SO trong code, khong bao gio ghep tham so
         * nguoi dung vao. Gia tri la roi vao nhanh mac dinh.
         */
        if ("popular".equals(sort)) {
            sql.append("ORDER BY s.view_count DESC ");
        } else if ("rating".equals(sort)) {
            // Truyen 1 nguoi cham 5 sao KHONG duoc dung tren truyen 200 nguoi
            // cham 4.8. Doi it nhat 3 luot moi cho vao bang xep theo diem.
            sql.append("ORDER BY (s.rating_count >= 3) DESC, ")
               .append("s.rating_sum / NULLIF(s.rating_count, 0) DESC, ")
               .append("s.rating_count DESC ");
        } else {
            sql.append("ORDER BY s.updated_at DESC ");
        }
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
    public int countPage(String tagSlug, String keyword, String progress)
            throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) {
            return DemoData.filter(tagSlug, keyword, progress).size();
        }

        /*
         * PHAI JOIN users O DAY DU KHONG LAY COT NAO CUA users.
         * Menh de WHERE ben duoi so sanh u.display_name khi co tu khoa. Bo
         * JOIN di thi cau dem va cau lay du lieu loc khac nhau -> so trang
         * hien ra khong khop so truyen thuc su co.
         */
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(DISTINCT s.id) FROM stories s "
              + "JOIN users u ON u.id = s.author_id ");
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
            sql.append("AND (s.title LIKE ? OR u.display_name LIKE ? OR u.username LIKE ?) ");
            String like = "%" + keyword + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if ("ongoing".equals(progress)) {
            sql.append("AND s.progress = 'ONGOING' ");
        } else if ("completed".equals(progress)) {
            sql.append("AND s.progress = 'COMPLETED' ");
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.story(id);
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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.storiesByAuthor(authorId);
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
        // CHE DO XEM GIAO DIEN: khong co DB thi bo qua, KHONG nem ngoai le.
        // Day chi la bo dem luot xem — hong thi khong dang de lam hong ca
        // trang chi tiet truyen. Cac lenh ghi that (them/sua/xoa truyen) van
        // nem ngoai le de nguoi dung biet ro la chua luu duoc.
        if (!DBConnection.isReady()) return;

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
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.stories();
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


    // ========================================================================
    //  TRANG TAC GIA  (trang 4) va BANG XEP HANG  (trang 5)
    // ========================================================================

    /**
     * Truyen DA CONG KHAI cua mot tac gia, co phan trang.
     *
     * KHAC findByAuthor() o cho nao:
     *   findByAuthor           -> gom ca DRAFT, dung cho trang "Truyen cua toi"
     *   findPublishedByAuthor  -> chi PUBLISHED, dung cho trang cong khai
     *
     * Tach hai method thay vi them tham so boolean, vi ten method tu noi ro
     * no tra ve gi. Doc "findPublishedByAuthor" la biet ngay, khong phai di
     * tim xem tham so true/false nghia la gi.
     */
    public List<Story> findPublishedByAuthor(int authorId, int offset, int limit)
            throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.slice(DemoData.storiesByAuthor(authorId), offset, limit);
        String sql = SELECT_BASE
                   + "WHERE s.author_id = ? AND s.status = 'PUBLISHED' "
                   + "ORDER BY s.updated_at DESC LIMIT ? OFFSET ?";

        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /** Dem truyen da cong khai cua mot tac gia - de tinh so trang. */
    public int countPublishedByAuthor(int authorId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.storiesByAuthor(authorId).size();
        String sql = "SELECT COUNT(*) FROM stories "
                   + "WHERE author_id = ? AND status = 'PUBLISHED'";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Tong luot xem cua tat ca truyen mot tac gia - thong ke trang ca nhan. */
    public int totalViewsByAuthor(int authorId) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.viewsOfAuthor(authorId);
        String sql = "SELECT COALESCE(SUM(view_count), 0) FROM stories "
                   + "WHERE author_id = ? AND status = 'PUBLISHED'";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, authorId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Bang xep hang.
     *
     * @param by "chapters" = nhieu chuong nhat
     *           "newest"   = moi dang gan day
     *           mac dinh   = nhieu luot xem nhat
     *
     * DUNG DANH SACH TRANG CHO ORDER BY - doc ky cho nay.
     *   Tham so "by" den tu URL, tuc la NGUOI DUNG kiem soat. Ghep thang no
     *   vao cau lenh:
     *       "ORDER BY " + by          <-- SQL INJECTION
     *   la nguoi ta go ?by=1;DROP TABLE stories-- va xong doi.
     *
     *   Dau ? KHONG dung duoc cho ten cot va ORDER BY - PreparedStatement chi
     *   thay the GIA TRI, khong thay the cau truc cau lenh.
     *
     *   Nen cach duy nhat an toan: so sanh voi danh sach cho phep, roi dung
     *   chuoi HANG SO do minh viet ra. Gia tri la nao cung roi vao "else".
     */
    public List<Story> findTop(String by, int limit) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.top(by, limit);
        String orderBy;
        if ("chapters".equals(by)) {
            orderBy = "chapter_count DESC";
        } else if ("newest".equals(by)) {
            orderBy = "s.created_at DESC";
        } else {
            orderBy = "s.view_count DESC";
        }

        String sql = SELECT_BASE
                   + "WHERE s.status = 'PUBLISHED' "
                   + "ORDER BY " + orderBy + " LIMIT ?";

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

    /*
     * Đổi MỘT dòng ResultSet thành MỘT object Story.
     *
     * Tách riêng ra vì mọi method truy vấn ở trên đều cần. Sau này thêm cột
     * mới thì sửa đúng một chỗ này, thay vì đi sửa từng vòng lặp — và chắc
     * chắn sẽ sót một chỗ nếu không tách.
     */
    /**
     * Bang xep hang THEO KHOANG THOI GIAN — trang 5.
     *
     * VI SAO KHONG DUNG stories.view_count DUOC
     *   view_count la mot so cong don tu ngay dang. No khong nho luot xem nao
     *   xay ra khi nao, nen khong tra loi duoc "tuan nay truyen nao hot".
     *   Cau nay dem tren view_logs — bang ghi TUNG luot xem kem thoi diem.
     *
     * VI SAO JOIN VOI MOT BANG CON THAY VI JOIN THANG view_logs
     *   JOIN thang roi GROUP BY se phai gom theo toan bo cot cua stories.
     *   Gom truoc trong bang con (chi hai cot story_id va so luot) roi moi
     *   noi sang stories thi MySQL chi phai xu ly danh sach ngan.
     *
     * @param days 7 = tuan nay, 30 = thang nay
     */
    public List<Story> findTopByPeriod(int days, int limit) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.top("views", limit);

        String sql = SELECT_BASE
                   + "JOIN ( "
                   + "   SELECT story_id, COUNT(*) AS hits "
                   + "   FROM view_logs "
                   + "   WHERE viewed_at >= DATE_SUB(NOW(), INTERVAL ? DAY) "
                   + "   GROUP BY story_id "
                   + ") v ON v.story_id = s.id "
                   + "WHERE s.status = 'PUBLISHED' "
                   + "ORDER BY v.hits DESC "
                   + "LIMIT ?";

        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, days);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * Ghi mot luot xem vao nhat ky.
     *
     * Goi NGAY SAU increaseView(). Hai thao tac tach roi co chu y: bo dem
     * view_count phai luon dung vi no hien tren moi the truyen, con nhat ky
     * co the thieu vai dong ma khong ai chet — bang xep hang tuan lech mot
     * luot thi khong sao.
     *
     * userId = 0 nghia la khach chua dang nhap -> ghi NULL.
     */
    public void logView(int storyId, int userId) throws SQLException {
        if (!DBConnection.isReady()) return;

        String sql = "INSERT INTO view_logs (story_id, user_id) VALUES (?, ?)";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            if (userId > 0) {
                ps.setInt(2, userId);
            } else {
                ps.setNull(2, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Thong ke cho MOT truyen — trang 16 (thong ke truyen cua toi).
     *
     * Tra ve mot mang 3 phan tu: [luot xem, so nguoi danh dau, so binh luan].
     *
     * VI SAO GOM BA CON SO VAO MOT CAU
     *   Ba cau rieng la ba lan di lai voi MySQL. Ba subquery trong mot cau chi
     *   mot lan. Voi trang thong ke liet ke 20 truyen thi khac biet la 60 lan
     *   di lai so voi 20.
     *
     * VI SAO TRA VE int[] MA KHONG PHAI MOT LOP RIENG
     *   Chi mot trang duy nhat dung. Tao lop StoryStats cho ba con so la them
     *   mot file de doc mot lan. Neu co trang thu hai can thi tach ngay.
     */
    public int[] statsOf(int storyId) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.statsOf(storyId);

        String sql =
            "SELECT s.view_count, "
          + "       (SELECT COUNT(*) FROM bookmarks b WHERE b.story_id = s.id) AS saves, "
          + "       (SELECT COUNT(*) FROM comments  c WHERE c.story_id = s.id "
          + "        AND c.status = 'VISIBLE') AS comments "
          + "FROM stories s WHERE s.id = ?";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return new int[]{0, 0, 0};
                return new int[]{
                        rs.getInt("view_count"),
                        rs.getInt("saves"),
                        rs.getInt("comments")
                };
            }
        }
    }

    /**
     * So lieu tong quan cho bang dieu khien quan tri — trang 25.
     *
     * Tra ve [so truyen, so chuong, so tai khoan, tong luot xem].
     *
     * DUNG MOT CAU SELECT KHONG CO FROM, boc bon subquery. Cach nay tranh
     * duoc bon lan mo dong ket noi, va doc ra thi rat ro rang: moi dong la
     * mot con so tren bang dieu khien.
     */
    public int[] adminOverview() throws SQLException {
        if (!DBConnection.isReady()) return DemoData.adminOverview();

        /*
         * SAU subquery trong MOT cau, khong phai sau cau rieng.
         *
         * Trang bang dieu khien chi hien sau con so. Goi sau lan la sau lan
         * di lai voi MySQL cho mot man hinh tinh. Gop lai thi mot lan di,
         * sau con so ve.
         *
         * COALESCE cho SUM: bang rong thi SUM tra ve NULL chu khong phai 0,
         * va getInt(NULL) tra ve 0 nhung wasNull() moi biet — COALESCE xu ly
         * ngay trong SQL, gon hon.
         */
        String sql =
            "SELECT (SELECT COUNT(*) FROM stories WHERE status = 'PUBLISHED'), "
          + "       (SELECT COUNT(*) FROM chapters), "
          + "       (SELECT COUNT(*) FROM users), "
          + "       (SELECT COALESCE(SUM(view_count), 0) FROM stories), "
          + "       (SELECT COUNT(*) FROM comments WHERE status = 'VISIBLE'), "
          + "       (SELECT COUNT(*) FROM stories WHERE status = 'DELETED')";
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return new int[]{0, 0, 0, 0, 0, 0};
            return new int[]{rs.getInt(1), rs.getInt(2), rs.getInt(3),
                             rs.getInt(4), rs.getInt(5), rs.getInt(6)};
        }
    }

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

        /*
         * Hai cot diem danh gia chi co trong cac cau dung SELECT_BASE.
         * Cau viet tay (vi du findAllForAdmin) khong co -> getInt se nem
         * SQLException. Bat va bo qua de mot cau thieu cot khong lam hong ca
         * trang; gia tri mac dinh 0 nghia la "chua ai cham".
         */
        try {
            s.setRatingSum(rs.getInt("rating_sum"));
            s.setRatingCount(rs.getInt("rating_count"));
        } catch (SQLException ignore) { }

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
