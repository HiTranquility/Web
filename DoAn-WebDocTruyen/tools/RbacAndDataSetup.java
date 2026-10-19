package tools;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

import truyen.util.DBConnection;
import truyen.util.PasswordUtil;

/**
 * Script thiết lập hệ thống Phân quyền động RBAC và Bổ sung 15+ bộ truyện phong phú.
 */
public class RbacAndDataSetup {

    public static void main(String[] args) {
        System.out.println("=== 1. KHỞI TẠO HỆ THỐNG PHÂN QUYỀN RBAC (ROLES & PERMISSIONS) ===");
        if (!DBConnection.isReady()) {
            System.err.println("Lỗi: Không kết nối được CSDL.");
            System.exit(1);
        }

        try (Connection con = DBConnection.get()) {
            con.setAutoCommit(false);

            // 1. Mở rộng cột role trong bảng users để hỗ trợ RBAC đa vai trò
            try (Statement st = con.createStatement()) {
                st.execute("ALTER TABLE users MODIFY COLUMN role VARCHAR(50) DEFAULT 'USER';");

                st.execute("CREATE TABLE IF NOT EXISTS roles ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "code VARCHAR(50) NOT NULL UNIQUE,"
                        + "name VARCHAR(100) NOT NULL,"
                        + "description VARCHAR(255),"
                        + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                // 2. Tạo bảng permissions
                st.execute("CREATE TABLE IF NOT EXISTS permissions ("
                        + "id INT AUTO_INCREMENT PRIMARY KEY,"
                        + "code VARCHAR(50) NOT NULL UNIQUE,"
                        + "name VARCHAR(100) NOT NULL,"
                        + "description VARCHAR(255)"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                // 3. Tạo bảng role_permissions
                st.execute("CREATE TABLE IF NOT EXISTS role_permissions ("
                        + "role_id INT NOT NULL,"
                        + "permission_id INT NOT NULL,"
                        + "PRIMARY KEY (role_id, permission_id),"
                        + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,"
                        + "FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

                // 4. Tạo bảng user_roles
                st.execute("CREATE TABLE IF NOT EXISTS user_roles ("
                        + "user_id INT NOT NULL,"
                        + "role_id INT NOT NULL,"
                        + "assigned_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                        + "PRIMARY KEY (user_id, role_id),"
                        + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                        + "FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE"
                        + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
            }

            // 5. Thêm dữ liệu Roles
            String[][] roles = {
                {"SUPER_ADMIN", "Quản trị viên Cấp cao", "Toàn quyền quản trị hệ thống, cấp ví xu và phân quyền"},
                {"ADMIN", "Quản trị viên", "Quản trị truyện, người dùng, thể loại và báo cáo"},
                {"MODERATOR", "Kiểm duyệt viên", "Kiểm duyệt bình luận, xử lý khiếu nại và duyệt truyện"},
                {"EDITOR", "Biên tập viên", "Biên tập nội dung chương, duyệt bản thảo tác giả"},
                {"VERIFIED_AUTHOR", "Tác giả Xác minh", "Đăng truyện, xuất bản chương VIP, sao lưu Drive"},
                {"VIP_MEMBER", "Hội viên VIP", "Đọc truyện chiết khấu, miễn phí một số chương VIP"},
                {"USER", "Độc giả Tiêu chuẩn", "Đọc truyện, bình luận, đánh giá, lưu tủ sách"}
            };

            for (String[] r : roles) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO roles (code, name, description) VALUES (?, ?, ?)")) {
                    ps.setString(1, r[0]);
                    ps.setString(2, r[1]);
                    ps.setString(3, r[2]);
                    ps.executeUpdate();
                }
            }

            // 6. Thêm dữ liệu Permissions
            String[][] perms = {
                {"MANAGE_SYSTEM", "Quản trị cấu hình hệ thống", "Thay đổi cấu hình server, bảo trì"},
                {"MANAGE_USERS", "Quản lý người dùng", "Khoá nick, mở khoá nick, sửa hồ sơ thành viên"},
                {"MANAGE_STORIES", "Quản lý tác phẩm", "Duyệt truyện, gỡ bỏ tác phẩm vi phạm, khôi phục truyện"},
                {"MODERATE_COMMENTS", "Kiểm duyệt bình luận", "Ẩn bình luận thô tục, xoá bình luận rác"},
                {"HANDLE_REPORTS", "Xử lý khiếu nại báo cáo", "Duyệt báo cáo vi phạm từ độc giả"},
                {"MANAGE_WALLET", "Quản lý ví xu & kiểm toán", "Cấp xu, thu hồi xu, xem nhật ký kiểm toán"},
                {"PUBLISH_STORY", "Sáng tác & xuất bản", "Đăng tải truyện, chương mới và đặt giá xu VIP"},
                {"READ_VIP_FREE", "Đặc quyền đọc VIP", "Đặc quyền miễn phí chương VIP"}
            };

            for (String[] p : perms) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO permissions (code, name, description) VALUES (?, ?, ?)")) {
                    ps.setString(1, p[0]);
                    ps.setString(2, p[1]);
                    ps.setString(3, p[2]);
                    ps.executeUpdate();
                }
            }

            // 7. Gán quyền cho từng Role
            assignRolePerm(con, "SUPER_ADMIN", new String[]{"MANAGE_SYSTEM", "MANAGE_USERS", "MANAGE_STORIES", "MODERATE_COMMENTS", "HANDLE_REPORTS", "MANAGE_WALLET", "PUBLISH_STORY", "READ_VIP_FREE"});
            assignRolePerm(con, "ADMIN", new String[]{"MANAGE_USERS", "MANAGE_STORIES", "MODERATE_COMMENTS", "HANDLE_REPORTS", "MANAGE_WALLET", "PUBLISH_STORY"});
            assignRolePerm(con, "MODERATOR", new String[]{"MANAGE_STORIES", "MODERATE_COMMENTS", "HANDLE_REPORTS"});
            assignRolePerm(con, "EDITOR", new String[]{"MANAGE_STORIES", "MODERATE_COMMENTS"});
            assignRolePerm(con, "VERIFIED_AUTHOR", new String[]{"PUBLISH_STORY"});
            assignRolePerm(con, "VIP_MEMBER", new String[]{"READ_VIP_FREE"});

            // 8. Tạo tài khoản moderator mẫu: mod1 / 123456 nếu chưa có
            createOrUpdateUser(con, "mod1", "mod1@webdoctruyen.local", "123456", "Kiểm Duyệt Viên Số 1", "MODERATOR");
            createOrUpdateUser(con, "editor1", "editor1@webdoctruyen.local", "123456", "Biên Tập Viên Mẫu", "EDITOR");

            // 9. Đồng bộ role cho các user cũ:
            syncUserRole(con, "admin", "SUPER_ADMIN");
            syncUserRole(con, "admin", "ADMIN");
            syncUserRole(con, "mod1", "MODERATOR");
            syncUserRole(con, "editor1", "EDITOR");
            syncUserRole(con, "mocmien", "VERIFIED_AUTHOR");
            syncUserRole(con, "haiduong", "USER");

            con.commit();
            System.out.println("-> Hoàn tất khởi tạo RBAC và đồng bộ phân quyền thành công!");

            // 10. Bổ sung thêm 15 bộ truyện phong phú
            System.out.println("\n=== 2. TIẾN HÀNH BỔ SUNG 15 BỘ TRUYỆN MỚI ===");
            addExtraStories(con);
            con.commit();
            System.out.println("-> Đã bổ sung 15 bộ truyện phong phú thành công!");

        } catch (SQLException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void assignRolePerm(Connection con, String roleCode, String[] permCodes) throws SQLException {
        int roleId = queryId(con, "SELECT id FROM roles WHERE code = ?", roleCode);
        if (roleId <= 0) return;
        for (String pc : permCodes) {
            int permId = queryId(con, "SELECT id FROM permissions WHERE code = ?", pc);
            if (permId > 0) {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT IGNORE INTO role_permissions (role_id, permission_id) VALUES (?, ?)")) {
                    ps.setInt(1, roleId);
                    ps.setInt(2, permId);
                    ps.executeUpdate();
                }
            }
        }
    }

    private static void createOrUpdateUser(Connection con, String username, String email, String pass, String displayName, String role) throws SQLException {
        int uid = queryId(con, "SELECT id FROM users WHERE username = ?", username);
        String hash = PasswordUtil.hash(pass);
        if (uid <= 0) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users (username, email, password_hash, display_name, role, status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, email);
                ps.setString(3, hash);
                ps.setString(4, displayName);
                ps.setString(5, role);
                ps.executeUpdate();
            }
        }
    }

    private static void syncUserRole(Connection con, String username, String roleCode) throws SQLException {
        int uid = queryId(con, "SELECT id FROM users WHERE username = ?", username);
        int rid = queryId(con, "SELECT id FROM roles WHERE code = ?", roleCode);
        if (uid > 0 && rid > 0) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT IGNORE INTO user_roles (user_id, role_id) VALUES (?, ?)")) {
                ps.setInt(1, uid);
                ps.setInt(2, rid);
                ps.executeUpdate();
            }
        }
    }

    private static int queryId(Connection con, String sql, String param) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private static void addExtraStories(Connection con) throws SQLException {
        int authorId = queryId(con, "SELECT id FROM users WHERE username = ?", "mocmien");
        if (authorId <= 0) authorId = 1;

        String[][] newStories = {
            {"Vạn Cổ Đệ Nhất Thần", "text", "Thiên địa sơ khai, hồng mông hỗn độn. Thiếu niên Lý Thiên Mệnh nắm giữ thập đại Thái Cổ Hỗn Độn Cự Thú, nghịch chuyển càn khôn phá toái chư thiên.", "TIEN_HIEP", "45000", "4.9", "/assets/images/covers/cover-vanco.svg"},
            {"Toàn Trí Độc Giả: Điểm Nhìn Thần Bí", "comic", "Một ngày nọ, cuốn tiểu thuyết dài tập mà chỉ một mình Kim Dokja kiên trì đọc đến chương cuối bỗng biến thành hiện thực tàn khốc trên toàn cầu.", "MANGA", "82000", "5.0", "/assets/images/covers/cover-toantri.svg"},
            {"Trùng Sinh Đô Thị Tu Tiên", "text", "Độ Kiếp kỳ đại tu sĩ Trần Phàm độ kiếp thất bại, trùng sinh trở về thời niên thiếu tại thành phố Sở Châu, bắt đầu hành trình nghịch thiên cải mệnh.", "DO_THI", "39000", "4.8", "/assets/images/covers/cover-trungsinh-dothi.svg"},
            {"Bí Ẩn Làng Sương Mù", "text", "Đội điều tra hình sự số 4 nhận nhiệm vụ thâm nhập vào một ngôi làng cổ nằm sâu trong dãy Hoàng Liên Sơn, nơi xảy ra những vụ mất tích kỳ dị.", "TRINH_THAM", "28000", "4.7", "/assets/images/covers/cover-langsuongmu.svg"},
            {"Đại Phụng Đả Canh Nhân", "text", "Hứa Thất An tốt nghiệp trường cảnh sát, xuyên không vào thế giới luyện khí, nho gia và yêu ma hoành hành, giải khai những đại án kinh thiên động địa.", "TIEN_HIEP", "96000", "4.9", "/assets/images/covers/cover-daiphung.svg"},
            {"Võ Luyện Đỉnh Phong", "text", "Võ đạo đỉnh phong là cô độc, là tịch mịch, là bước đi một mình trên con đường chông gai. Thiếu niên quét rác Dương Khai tình cờ nhặt được cuốn Hắc Thư bí ẩn.", "KIEM_HIEP", "54000", "4.6", "/assets/images/covers/cover-voluyen.svg"},
            {"Ngự Thú Phiên Bản Toàn Dân", "comic", "Thế giới bước vào kỷ nguyên ngự thú toàn dân, mỗi người đều có thể thức tỉnh linh thú bản mệnh. Diệp Viễn thức tỉnh thiên phú cấp Thần vô hạn tiến hoá.", "MANGA", "63000", "4.9", "/assets/images/covers/cover-nguthu.svg"},
            {"Thám Tử Kính Lúp: Vụ Án Đồng Hồ Cát", "text", "Một vụ án mạng trong căn phòng kín hoàn hảo tại toà tháp đồng hồ cổ kính ở thủ đô London thế kỷ 19.", "TRINH_THAM", "18000", "4.5", "/assets/images/covers/cover-thantu-dongho.svg"},
            {"Quỷ Bí Chi Chủ", "text", "Chu Minh Thụy tỉnh lại trong thân xác của Klein Moretti tại một thế giới mang phong cách hơi nước Victoria, đầy ắp ma dược và các thế lực tà thần ẩn nấp.", "HUYEN_HUYEN", "110000", "5.0", "/assets/images/covers/cover-quy-bi.svg"},
            {"Đấu La Chi Thần Cấp Khởi Đầu", "text", "Xuyên không về Đấu La Đại Lục, thức tỉnh song sinh võ hồn Thí Thần Thương và Thập Nhị Dực Đọa Lạc Thiên Sứ, chấn động Vũ Hồn Điện.", "HUYEN_HUYEN", "32000", "4.7", "/assets/images/covers/cover-daula.svg"},
            {"Thợ Rèn Huyền Thoại Trong Thế Giới Game", "comic", "Shin Youngwoo là một kẻ xui xẻo nợ nần ngập đầu trong tựa game thực tế ảo số 1 thế giới Satisfy, cho đến khi cậu phát hiện ra Trang Sách Pagma.", "MANGA", "71000", "4.8", "/assets/images/covers/cover-thoren.svg"},
            {"Y Đạo Vô Song: Thần Y Hạ Sơn", "text", "Thiếu niên Lâm Phong tu luyện trên núi 10 năm, y thuật tuyệt thế, võ đạo thông thần, vâng lệnh sư phụ xuống núi giải trừ huyết hải thâm thù.", "DO_THI", "41000", "4.6", "/assets/images/covers/cover-ydo.svg"},
            {"Phù Thủy Cuối Cùng Ở Học Viện Phép Thuật", "text", "Khi ma pháp cổ xưa dần bị lãng quên bởi kỷ nguyên công nghệ máy móc, một học sinh dị biệt đã đánh thức cấm thuật cổ đại ngàn năm.", "HUYEN_HUYEN", "24000", "4.7", "/assets/images/covers/cover-phuthuy.svg"},
            {"Căn Phòng Số 404: Ám Ảnh Ký Túc Xá", "text", "Ký túc xá đại học vào nửa đêm thường xuyên phát ra những âm thanh kỳ lạ từ căn phòng vốn đã bị niêm phong suốt 15 năm qua.", "KINH_DI", "35000", "4.8", "/assets/images/covers/cover-phong404.svg"},
            {"Trở Thành Bạo Chúa Trong Tiểu Thuyết Xuyên Không", "comic", "Nữ sinh viên bình thường bỗng xuyên không vào vai hoàng đế bạo chúa tàn nhẫn trong cuốn tiểu thuyết lãng mạn, mục tiêu duy nhất là sống sót.", "MANGA", "58000", "4.9", "/assets/images/covers/cover-baochua.svg"}
        };

        for (String[] st : newStories) {
            String title = st[0];
            boolean isComic = "comic".equalsIgnoreCase(st[1]);
            String summary = st[2];
            int views = Integer.parseInt(st[4]);
            double rating = Double.parseDouble(st[5]);
            String coverPath = st[6];

            // Kiểm tra xem truyện đã có chưa
            int sid = queryId(con, "SELECT id FROM stories WHERE title = ?", title);
            if (sid > 0) {
                // Đã có truyện: cập nhật ảnh bìa chuyên biệt nếu chưa có hoặc cập nhật mới
                try (PreparedStatement ps = con.prepareStatement("UPDATE stories SET cover_url = ? WHERE id = ?")) {
                    ps.setString(1, coverPath);
                    ps.setInt(2, sid);
                    ps.executeUpdate();
                }
            } else {
                String slug = truyen.util.SlugUtil.toSlug(title);
                String storyType = isComic ? "COMIC" : "TEXT";
                int rCount = 15;
                int rSum = (int) Math.round(rating * rCount);

                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO stories (title, slug, author_id, description, status, progress, story_type, cover_url, view_count, rating_sum, rating_count, created_at, updated_at) "
                        + "VALUES (?, ?, ?, ?, 'PUBLISHED', 'ONGOING', ?, ?, ?, ?, ?, NOW(), NOW())",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, title);
                    ps.setString(2, slug);
                    ps.setInt(3, authorId);
                    ps.setString(4, summary);
                    ps.setString(5, storyType);
                    ps.setString(6, coverPath);
                    ps.setInt(7, views);
                    ps.setInt(8, rSum);
                    ps.setInt(9, rCount);
                    ps.executeUpdate();
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) sid = rs.getInt(1);
                    }
                }

                if (sid > 0) {
                    // Thêm 5 chương cho mỗi truyện mới (chương 1-3 miễn phí, chương 4-5 VIP 5 xu)
                    for (int ch = 1; ch <= 5; ch++) {
                        boolean isVip = (ch >= 4);
                        int coinPrice = isVip ? 5 : 0;
                        String chTitle = "Chương " + ch + ": " + (ch == 1 ? "Khởi Đầu Biến Cố" : (ch == 2 ? "Ngộ Đạo & Đột Phá" : (ch == 3 ? "Gặp Gỡ Cố Nhân" : (ch == 4 ? "Bí Mật Tông Môn [VIP]" : "Huyết Chiến Hắc Phong [VIP]"))));
                        String content = "Nội dung chi tiết của " + chTitle + " thuộc tác phẩm " + title + ".\n"
                                + "Trời đất cuồn cuộn mây giông, tiếng gió rít qua từng khe đá như tiếng gầm của dã thú thời cổ đại.\n"
                                + "Thiếu niên đứng vững giữa sấm sét, trong ánh mắt không hề có một tia sợ hãi.\n"
                                + "\"Nếu thiên đạo bất công, ta liền phá toái thiên đạo!\"\n"
                                + "Khí tức toàn thân bộc phát, chấn động bốn phương tám hướng.\n";

                        int chId = 0;
                        try (PreparedStatement ps = con.prepareStatement(
                                "INSERT INTO chapters (story_id, chapter_no, title, content, is_vip, coin_price) "
                                + "VALUES (?, ?, ?, ?, ?, ?)",
                                Statement.RETURN_GENERATED_KEYS)) {
                            ps.setInt(1, sid);
                            ps.setInt(2, ch);
                            ps.setString(3, chTitle);
                            ps.setString(4, content);
                            ps.setBoolean(5, isVip);
                            ps.setInt(6, coinPrice);
                            ps.executeUpdate();
                            try (ResultSet rs = ps.getGeneratedKeys()) {
                                if (rs.next()) chId = rs.getInt(1);
                            }
                        }

                        // Nếu là truyện tranh, thêm các trang ảnh comic
                        if (isComic && chId > 0) {
                            for (int p = 1; p <= 4; p++) {
                                try (PreparedStatement ps = con.prepareStatement(
                                        "INSERT INTO chapter_pages (chapter_id, page_no, image_path, width, height) "
                                        + "VALUES (?, ?, ?, 800, 1200)")) {
                                    ps.setInt(1, chId);
                                    ps.setInt(2, p);
                                    ps.setString(3, "assets/images/comic/p" + ((p % 3) + 1) + ".jpg");
                                    ps.executeUpdate();
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
