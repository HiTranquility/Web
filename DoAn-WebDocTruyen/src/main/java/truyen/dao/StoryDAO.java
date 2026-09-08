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

/** Tầng truy cập dữ liệu cho bảng stories. */
public class StoryDAO {

    /*
     * Viết SQL ra hằng số thay vì nhét thẳng vào lời gọi method:
     * dễ đọc, dễ sửa, và copy sang MySQL Workbench chạy thử được ngay.
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

            /* ĐÂY LÀ ĐIỂM QUAN TRỌNG NHẤT CỦA CẢ FILE. */
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

    /** Danh sach truyen, co phan trang va loc tuy chon theo the loai. */
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
            /* Tim theo TEN TRUYEN hoac TEN TAC GIA. */
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

    /** Truyen DA CONG KHAI cua mot tac gia, co phan trang. */
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

    /** Bang xep hang. */
    public List<Story> findTop(String by, int limit) throws SQLException {
        // CHE DO XEM GIAO DIEN: chua co db.properties thi lay du lieu gia.
        if (!DBConnection.isReady()) return DemoData.top(by, limit);
        String orderBy;
        if ("chapters".equals(by)) {
            orderBy = "chapter_count DESC";
        } else if ("newest".equals(by)) {
            orderBy = "s.created_at DESC";
        } else if ("rating".equals(by)) {
            /*
             * NGUONG 3 LUOT CHAM.
             * Truyen mot nguoi cham 5 sao KHONG duoc dung tren truyen 200
             * nguoi cham 4.8 — mot phieu khong noi len dieu gi. Dua dieu kien
             * (rating_count >= 3) len dau ORDER BY: MySQL coi bieu thuc dung
             * la 1, sai la 0, nen DESC day het nhom du 3 luot len tren.
             */
            orderBy = "(s.rating_count >= 3) DESC, "
                    + "s.rating_sum / NULLIF(s.rating_count, 0) DESC, "
                    + "s.rating_count DESC";
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
    /** Truyen TUONG TU — cung the loai, khac chinh no. */
    public List<Story> findSimilar(int storyId, int limit) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.similar(storyId, limit);

        String sql = SELECT_BASE
                   + "JOIN story_tags st ON st.story_id = s.id "
                   + "WHERE s.status = 'PUBLISHED' "
                   + "  AND s.id <> ? "
                   + "  AND st.tag_id IN (SELECT tag_id FROM story_tags WHERE story_id = ?) "
                   + "GROUP BY s.id "
                   + "ORDER BY COUNT(*) DESC, s.view_count DESC "
                   + "LIMIT ?";

        List<Story> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, storyId);
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    /** Bang xep hang THEO KHOANG THOI GIAN — trang 5. */
    public List<Story> findTopByPeriod(int days, int limit) throws SQLException {
        if (!DBConnection.isReady()) return DemoData.topByPeriod(days, limit);

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

    /** Ghi mot luot xem vao nhat ky. */
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

    /** Thong ke cho MOT truyen — trang 16 (thong ke truyen cua toi). */
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
     * Dem theo NGAY cho bieu do — dung chung cho ba loai so lieu.
     *
     * VI SAO MOT HAM CHO CA BA thay vi countStoriesByDay / countUsersByDay /
     * countViewsByDay
     *   Ba cau SQL khac nhau dung TEN BANG va TEN COT khac nhau, phan con lai
     *   giong het: gom theo ngay, lap day ngay trong, tra ve mang. Viet ba lan
     *   la chep ba lan cung mot logic lap day.
     *
     *   Ten bang KHONG the truyen bang dau ? — no khong phai gia tri. Nen o day
     *   dung DANH SACH TRANG: chuoi `what` chi chon duoc mot trong ba cau da
     *   viet san, khong co duong nao ghep chu nguoi dung vao SQL.
     *
     * VI SAO PHAI LAP DAY NGAY TRONG
     *   GROUP BY chi tra ve nhung ngay CO du lieu. Ngay khong ai dang truyen
     *   thi bien mat khoi ket qua, va bieu do se noi lien hai cot cach nhau ba
     *   ngay nhu the chung lien nhau — doc ra sai han xu huong.
     *
     * @param what "stories" | "users" | "views"
     * @param days so ngay gan nhat, tinh ca hom nay
     * @return mang do dai `days`, phan tu 0 la ngay xa nhat
     */
    public int[] countByDay(String what, int days) throws SQLException {
        if (days < 1) days = 1;
        if (days > 90) days = 90;      // chan xin mot nam du lieu

        if (!DBConnection.isReady()) return DemoData.countByDay(what, days);

        String sql;
        if ("users".equals(what)) {
            sql = "SELECT DATE(created_at) d, COUNT(*) n FROM users "
                + "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY d";
        } else if ("views".equals(what)) {
            sql = "SELECT DATE(viewed_at) d, COUNT(*) n FROM view_logs "
                + "WHERE viewed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY d";
        } else {
            sql = "SELECT DATE(created_at) d, COUNT(*) n FROM stories "
                + "WHERE status != 'DELETED' "
                + "  AND created_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                + "GROUP BY d";
        }

        // Dat ket qua vao dung o theo khoang cach ngay so voi hom nay.
        int[] out = new int[days];
        java.time.LocalDate today = java.time.LocalDate.now();

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, days - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("d");
                    if (d == null) continue;
                    long back = java.time.temporal.ChronoUnit.DAYS.between(
                            d.toLocalDate(), today);
                    int idx = days - 1 - (int) back;
                    if (idx >= 0 && idx < days) out[idx] = rs.getInt("n");
                }
            }
        }
        return out;
    }

    /** Luot xem theo ngay cua RIENG mot truyen — trang thong ke cua tac gia. */
    public int[] viewsByDay(int storyId, int days) throws SQLException {
        if (days < 1) days = 1;
        if (days > 90) days = 90;

        if (!DBConnection.isReady()) return DemoData.viewsByDay(storyId, days);

        String sql = "SELECT DATE(viewed_at) d, COUNT(*) n FROM view_logs "
                   + "WHERE story_id = ? "
                   + "  AND viewed_at >= DATE_SUB(CURDATE(), INTERVAL ? DAY) "
                   + "GROUP BY d";

        int[] out = new int[days];
        java.time.LocalDate today = java.time.LocalDate.now();

        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, storyId);
            ps.setInt(2, days - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.sql.Date d = rs.getDate("d");
                    if (d == null) continue;
                    long back = java.time.temporal.ChronoUnit.DAYS.between(
                            d.toLocalDate(), today);
                    int idx = days - 1 - (int) back;
                    if (idx >= 0 && idx < days) out[idx] = rs.getInt("n");
                }
            }
        }
        return out;
    }

    /** So lieu tong quan cho bang dieu khien quan tri — trang 25. */
    public int[] adminOverview() throws SQLException {
        if (!DBConnection.isReady()) return DemoData.adminOverview();

        /* SAU subquery trong MOT cau, khong phai sau cau rieng. */
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
