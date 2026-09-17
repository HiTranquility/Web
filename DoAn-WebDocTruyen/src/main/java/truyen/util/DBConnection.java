package truyen.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối tới MySQL thông qua Connection Pool HikariCP.
 *
 * TỐC ĐỘ & HIỆU NĂNG:
 *   Thay vì gọi DriverManager.getConnection() cho mỗi query (mở và đóng TCP socket liên tục),
 *   HikariCP duy trì một pool các kết nối sẵn sàng. Tốc độ lấy kết nối tính bằng microsecond,
 *   tránh lỗi "Too many connections" và nghẽn hệ thống khi có nhiều người truy cập đồng thời.
 */
public class DBConnection {

    private static final String CONFIG_FILE = "db.properties";

    // Đọc file cấu hình đúng MỘT lần, lần đầu lớp này được dùng tới.
    private static final Properties CONFIG = loadConfig();

    // Thông báo lỗi cấu hình, giữ lại để hiện cho người dùng thay vì để
    // nguyên stack trace khó hiểu.
    private static String configError;

    // Connection pool HikariCP
    private static HikariDataSource dataSource = initPool();

    private static Properties loadConfig() {
        Properties props = new Properties();

        // getResourceAsStream đọc file trong WEB-INF/classes (tức là thư mục
        // src/main/resources sau khi build). KHÔNG dùng new File(...) ở đây:
        // đường dẫn tuyệt đối sẽ khác nhau trên mỗi máy — đúng bài học
        // getRealPath ở chương 5.
        try (InputStream in = DBConnection.class.getClassLoader()
                                                .getResourceAsStream(CONFIG_FILE)) {
            if (in == null) {
                configError = "Đang ở CHẾ ĐỘ XEM GIAO DIỆN (chưa nối cơ sở dữ liệu). "
                        + "Xem thì được, nhưng chưa lưu được gì. "
                        + "Muốn lưu thật: chạy scripts\\setup-db.ps1 rồi tạo "
                        + CONFIG_FILE + " trong src/main/resources. "
                        + "Hãy chép db.properties.example thành db.properties.";
                return props;
            }
            props.load(in);
        } catch (IOException e) {
            configError = "Đọc " + CONFIG_FILE + " thất bại: " + e.getMessage();
        }
        return props;
    }

    private static HikariDataSource initPool() {
        if (configError != null) return null;

        String url = CONFIG.getProperty("db.url");
        String user = CONFIG.getProperty("db.username");
        String password = CONFIG.getProperty("db.password");

        if (url == null || user == null) {
            return null;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);

            // Cấu hình pool tối ưu
            config.setPoolName("TruyenHikariPool");
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30000);        // 30 giây
            config.setConnectionTimeout(10000);  // 10 giây chờ kết nối
            config.setMaxLifetime(1800000);      // 30 phút

            // Tối ưu MySQL JDBC Driver
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            return new HikariDataSource(config);
        } catch (Throwable t) {
            // Nếu thiếu thư viện hoặc lỗi khởi tạo, ghi nhận để fallback sang DriverManager
            return null;
        }
    }

    /** Đã cấu hình được database chưa? */
    public static boolean isReady() {
        return configError == null
                && CONFIG.getProperty("db.url") != null
                && CONFIG.getProperty("db.username") != null;
    }

    /** Lấy một kết nối từ pool (hoặc fallback nếu pool chưa sẵn sàng). */
    public static Connection get() throws SQLException {
        if (configError != null) {
            throw new SQLException(configError);
        }

        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getConnection();
        }

        // Fallback sang DriverManager nếu pool không khởi tạo được
        String url = CONFIG.getProperty("db.url");
        String user = CONFIG.getProperty("db.username");
        String password = CONFIG.getProperty("db.password");

        if (url == null || user == null) {
            throw new SQLException("db.properties thiếu db.url hoặc db.username.");
        }

        return DriverManager.getConnection(url, user, password);
    }

    /** Đóng connection pool khi tắt server. */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
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
