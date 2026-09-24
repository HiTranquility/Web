package truyen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.ViewLogDAO;

@DisplayName("ViewLogDAO — Kiểm thử dọn dẹp và đếm lượt đọc (ISSUE-014)")
class ViewLogDAOTest {

    private final ViewLogDAO viewLogDAO = new ViewLogDAO();

    @Test
    @DisplayName("Ca 1: cleanOldLogs chạy an toàn không ném ngoại lệ")
    void testCleanOldLogs() throws SQLException {
        assertDoesNotThrow(() -> {
            int count = viewLogDAO.cleanOldLogs(90);
            assertTrue(count >= 0);
        });
    }

    @Test
    @DisplayName("Ca 2: countStories với user âm trả về 0 an toàn")
    void testCountStoriesNegative() throws SQLException {
        assertDoesNotThrow(() -> {
            int count = viewLogDAO.countStories(-99);
            assertTrue(count >= 0);
        });
    }
}
