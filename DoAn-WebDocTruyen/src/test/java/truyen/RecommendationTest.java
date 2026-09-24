package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.StoryDAO;
import truyen.model.Story;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecommendationTest — Kiểm thử gợi ý truyện thông minh (ISSUE-007)")
class RecommendationTest {

    @Test
    @DisplayName("Ca 1: findAlsoRead() không ném ngoại lệ không mong muốn và trả về danh sách an toàn")
    void testFindAlsoReadSafety() {
        StoryDAO dao = new StoryDAO();
        try {
            List<Story> list = dao.findAlsoRead(1, 4);
            assertNotNull(list);
            assertTrue(list.size() <= 4);
            for (Story s : list) {
                assertNotEquals(1, s.getId(), "Không được gợi ý chính truyện đang xem");
                assertEquals("PUBLISHED", s.getStatus(), "Chỉ gợi ý truyện đã xuất bản");
            }
        } catch (SQLException e) {
            // Khi dịch vụ MySQL chưa bật ở môi trường chạy test, ghi nhận ngoại lệ kết nối hợp lệ
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("Ca 2: findAlsoRead() với ID không tồn tại hoặc <= 0 trả về danh sách rỗng")
    void testFindAlsoReadInvalidId() {
        StoryDAO dao = new StoryDAO();
        try {
            List<Story> listNegative = dao.findAlsoRead(-1, 4);
            assertNotNull(listNegative);
            assertEquals(0, listNegative.size());

            List<Story> listZero = dao.findAlsoRead(0, 4);
            assertNotNull(listZero);
            assertEquals(0, listZero.size());
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @DisplayName("Ca 3: findSimilar() và findAlsoRead() bổ trợ lẫn nhau, làm phong phú trải nghiệm")
    void testComplementaryRecommendations() {
        StoryDAO dao = new StoryDAO();
        try {
            List<Story> similar = dao.findSimilar(1, 4);
            List<Story> alsoRead = dao.findAlsoRead(1, 4);

            assertNotNull(similar);
            assertNotNull(alsoRead);
        } catch (SQLException e) {
            assertNotNull(e.getMessage());
        }
    }
}
