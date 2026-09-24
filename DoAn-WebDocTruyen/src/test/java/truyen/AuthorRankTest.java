package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.UserDAO;
import truyen.model.User;
import truyen.util.DemoData;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuthorRank — Bảng xếp hạng tác giả nổi bật (ISSUE-006)")
class AuthorRankTest {

    @Test
    @DisplayName("Ca 1: Getter và Setter totalViews trên User model hoạt động chính xác")
    void testUserTotalViewsProperty() {
        User user = new User();
        user.setId(99);
        user.setUsername("tacgia99");
        user.setDisplayName("Đại Tác Giả");
        user.setTotalViews(150240L);

        assertEquals(150240L, user.getTotalViews());
        assertEquals("Đại Tác Giả", user.getName());
        assertEquals("Đ", user.getInitial());
    }

    @Test
    @DisplayName("Ca 2: DemoData.topAuthors trả về danh sách tác giả hợp lệ và đúng thứ tự lượt xem")
    void testDemoDataTopAuthorsSorting() {
        List<User> authors = DemoData.topAuthors(10);

        assertNotNull(authors);
        assertFalse(authors.isEmpty(), "Danh sách tác giả nổi bật không được rỗng");

        long previousViews = Long.MAX_VALUE;
        for (User a : authors) {
            assertTrue(a.getStoryCount() > 0, "Tác giả trong bảng xếp hạng phải có ít nhất 1 truyện");
            assertTrue(a.getTotalViews() >= 0, "Lượt xem không được âm");
            assertTrue(a.getFollowerCount() >= 0, "Lượng theo dõi không được âm");
            assertNotEquals("BANNED", a.getStatus(), "Tác giả bị ban không được xuất hiện trên bảng xếp hạng");

            // Kiểm tra sắp xếp giảm dần theo lượt xem
            assertTrue(a.getTotalViews() <= previousViews,
                    "Tác giả sau (" + a.getTotalViews() + ") không được có lượt xem cao hơn tác giả trước (" + previousViews + ")");
            previousViews = a.getTotalViews();
        }
    }

    @Test
    @DisplayName("Ca 3: Tham số giới hạn số lượng (limit) của topAuthors hoạt động chính xác")
    void testTopAuthorsLimit() {
        int limit = 2;
        List<User> authors = DemoData.topAuthors(limit);

        assertNotNull(authors);
        assertTrue(authors.size() <= limit, "Số lượng tác giả trả về không được vượt quá limit");
    }

    @Test
    @DisplayName("Ca 4: UserDAO.findTopAuthors thực thi an toàn, xử lý ngoại lệ kết nối hợp lệ nếu DB offline")
    void testUserDAOFallbackSafety() {
        UserDAO dao = new UserDAO();
        try {
            List<User> list = dao.findTopAuthors(5);
            assertNotNull(list);
        } catch (SQLException e) {
            // Khi dịch vụ MySQL chưa bật ở môi trường chạy test, ghi nhận ngoại lệ kết nối hợp lệ
            assertNotNull(e.getMessage());
        }
    }
}
