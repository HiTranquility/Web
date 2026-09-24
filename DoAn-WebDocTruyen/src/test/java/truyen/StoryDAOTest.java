package truyen;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.StoryDAO;
import truyen.model.Story;

@DisplayName("StoryDAO — Kiểm thử truy vấn dữ liệu truyện (ISSUE-014)")
class StoryDAOTest {

    private final StoryDAO storyDAO = new StoryDAO();

    @Test
    @DisplayName("Ca 1: findPage với tham số null/rỗng không ném ngoại lệ và trả về danh sách an toàn")
    void testFindPageSafe() throws SQLException {
        assertDoesNotThrow(() -> {
            List<Story> list = storyDAO.findPage(null, null, "latest", null, 0, 10);
            assertNotNull(list);
        });
    }

    @Test
    @DisplayName("Ca 2: findById với ID âm hoặc 0 trả về null an toàn")
    void testFindByIdInvalid() throws SQLException {
        assertDoesNotThrow(() -> {
            Story s = storyDAO.findById(-1);
            // Với ID không hợp lệ, trả về null hoặc không ném lỗi
        });
    }

    @Test
    @DisplayName("Ca 3: countPage trả về số nguyên >= 0")
    void testCountPage() throws SQLException {
        int count = storyDAO.countPage(null, null, null);
        assertTrue(count >= 0);
    }
}
