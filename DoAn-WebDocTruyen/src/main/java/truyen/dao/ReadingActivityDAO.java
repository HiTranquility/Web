package truyen.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import truyen.util.DBConnection;

/** Một bản ghi cho mỗi người/chương; lần đọc lại không nhân số chương tích lũy. */
public class ReadingActivityDAO {
    private static volatile boolean ready;

    public static synchronized void ensureTable() throws SQLException {
        if (ready || !DBConnection.isReady()) return;
        try (Connection con = DBConnection.get(); Statement st = con.createStatement()) {
            boolean existed;
            try (ResultSet tables = con.getMetaData().getTables(con.getCatalog(), null, "user_chapter_reads", new String[] {"TABLE"})) {
                existed = tables.next();
            }
            st.executeUpdate("CREATE TABLE IF NOT EXISTS user_chapter_reads ("
                    + "user_id INT NOT NULL, chapter_id INT NOT NULL, "
                    + "first_read_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "last_read_at DATETIME NULL DEFAULT CURRENT_TIMESTAMP, "
                    + "night_read BOOLEAN NOT NULL DEFAULT FALSE, PRIMARY KEY (user_id, chapter_id), "
                    + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
            // Chỉ giữ chương đã biết tại lần chuyển đổi đầu tiên; không gán ngày đọc giả.
            if (!existed) st.executeUpdate("INSERT IGNORE INTO user_chapter_reads "
                    + "(user_id, chapter_id, first_read_at, last_read_at, night_read) "
                    + "SELECT rp.user_id, rp.chapter_id, NULL, NULL, FALSE FROM reading_progress rp "
                    + "JOIN users u ON u.id = rp.user_id JOIN chapters c ON c.id = rp.chapter_id");
            ready = true;
        }
    }

    public void record(int userId, int chapterId) throws SQLException {
        if (userId <= 0 || chapterId <= 0 || !DBConnection.isReady()) return;
        ensureTable();
        try (Connection con = DBConnection.get(); PreparedStatement ps = con.prepareStatement(
                "INSERT INTO user_chapter_reads (user_id, chapter_id, night_read) "
                + "VALUES (?, ?, (HOUR(NOW()) >= 23 OR HOUR(NOW()) < 5)) "
                + "ON DUPLICATE KEY UPDATE first_read_at = COALESCE(first_read_at, NOW()), "
                + "last_read_at = NOW(), night_read = night_read OR VALUES(night_read)")) {
            ps.setInt(1, userId); ps.setInt(2, chapterId); ps.executeUpdate();
        }
    }
}
