package truyen.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Điểm duy nhất trong dự án mở kết nối tới MySQL.
 *
 * VÌ SAO GOM VÀO MỘT LỚP
 *   Đổi máy, đổi mật khẩu, đổi tên database — sửa đúng một file. Nếu mỗi DAO
 *   tự viết DriverManager.getConnection("jdbc:mysql://...") thì tới lúc nộp
 *   bài trên máy khác sẽ phải đi sửa 7 chỗ và chắc chắn sót một chỗ.
 *
 * VÌ SAO ĐỌC TỪ FILE .properties CHỨ KHÔNG VIẾT CỨNG
 *   Mật khẩu database KHÔNG được nằm trong file .java. Code thì đem nộp, đẩy
 *   lên GitHub, gửi cho bạn cùng nhóm — mật khẩu đi theo luôn. Để trong
 *   db.properties rồi cho file đó vào .gitignore là cách rẻ nhất để tránh.
 *
 *   Cách này cũng giống hệt tinh thần init-param của chương 5: giá trị đổi
 *   theo nơi chạy thì để ra ngoài code.
 *
 * GHI CHÚ VỀ CONNECTION POOL
 *   Lớp này mở kết nối MỚI mỗi lần gọi, và người gọi phải đóng. Đúng cho đồ án.
 *   Web thật dùng connection pool (HikariCP, hoặc DataSource của Tomcat) vì mở
 *   kết nối tốn khoảng chục mili-giây — không đáng kể với vài người dùng, nhưng
 *   là thảm hoạ với vài nghìn.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    // Đọc file cấu hình đúng MỘT lần, lần đầu lớp này được dùng tới.
    private static final Properties CONFIG = loadConfig();

    // Thông báo lỗi cấu hình, giữ lại để hiện cho người dùng thay vì để
    // nguyên stack trace khó hiểu.
    private static String configError;

    private static Properties loadConfig() {
        Properties props = new Properties();

        // getResourceAsStream đọc file trong WEB-INF/classes (tức là thư mục
        // src/main/resources sau khi build). KHÔNG dùng new File(...) ở đây:
        // đường dẫn tuyệt đối sẽ khác nhau trên mỗi máy — đúng bài học
        // getRealPath ở chương 5.
        try (InputStream in = DBConnection.class.getClassLoader()
                                                .getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                configError = "Không tìm thấy " + CONFIG_FILE
                        + " trong src/main/resources. "
                        + "Hãy chép db.properties.example thành db.properties.";
                return props;
            }
            props.load(in);
        } catch (IOException e) {
            configError = "Đọc " + CONFIG_FILE + " thất bại: " + e.getMessage();
        }
        return props;
    }

    /**
     * Mở một kết nối mới.
     *
     * NGƯỜI GỌI PHẢI ĐÓNG. Luôn dùng try-with-resources:
     *
     *     try (Connection con = DBConnection.get();
     *          PreparedStatement ps = con.prepareStatement(sql)) {
     *         ...
     *     }
     *
     * Quên đóng thì kết nối bị rò rỉ. Sau vài chục request, MySQL đạt giới hạn
     * max_connections và toàn bộ trang web chết — với lỗi trông chẳng liên quan
     * gì tới chỗ thật sự sai.
     */
    public static Connection get() throws SQLException {
        if (configError != null) {
            throw new SQLException(configError);
        }

        String url = CONFIG.getProperty("db.url");
        String user = CONFIG.getProperty("db.username");
        String password = CONFIG.getProperty("db.password");

        if (url == null || user == null) {
            throw new SQLException("db.properties thiếu db.url hoặc db.username.");
        }

        // Từ JDBC 4.0, driver tự đăng ký khi có mặt trong classpath nên không
        // cần Class.forName("com.mysql.cj.jdbc.Driver") như các hướng dẫn cũ.
        // Chỉ cần file mysql-connector-j.jar nằm trong WEB-INF/lib.
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Kiểm tra kết nối được hay không, KHÔNG ném exception.
     *
     * Dùng ở trang chủ để hiện hướng dẫn cài đặt tử tế khi database chưa sẵn
     * sàng, thay vì quăng ra một trang lỗi 500 cho người dùng nhìn.
     * Trả về null nếu ổn, hoặc câu mô tả lỗi nếu không.
     */
    public static String checkConnection() {
        try (Connection con = get()) {
            return con.isValid(3) ? null : "Kết nối không hợp lệ.";
        } catch (SQLException e) {
            return e.getMessage();
        }
    }
}
