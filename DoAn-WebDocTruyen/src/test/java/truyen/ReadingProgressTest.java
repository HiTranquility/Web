package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import truyen.dao.StoryDAO;
import truyen.model.ChapterBookmark;
import truyen.model.ReadingProgress;
import truyen.model.RecommendationSet;
import truyen.model.Story;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vị trí đọc theo đoạn, đánh dấu đoạn và khối gợi ý gộp (migration-027).
 *
 * Toàn hàm THUẦN — không cần MySQL. Phần ghi CSDL đã được ép đúng ở tầng
 * SQL (khoá chính, UNIQUE); chỗ dễ sai thật sự là mấy phép cắt / gộp này.
 */
@DisplayName("Đọc tiếp đúng đoạn · Đánh dấu đoạn · Gợi ý gộp")
class ReadingProgressTest {

    @Nested
    @DisplayName("ReadingProgress.clampPara — số đoạn từ trình duyệt không tin được")
    class ClampPara {

        @Test
        @DisplayName("Số hợp lệ giữ nguyên")
        void keepsValid() {
            assertEquals(12, ReadingProgress.clampPara(12, 40));
            assertEquals(40, ReadingProgress.clampPara(40, 40));
        }

        @Test
        @DisplayName("Vượt quá số đoạn (chương bị sửa ngắn đi) -> ép về đoạn cuối")
        void clampsOverflow() {
            assertEquals(40, ReadingProgress.clampPara(999, 40));
        }

        @Test
        @DisplayName("Số âm hoặc chương rỗng (VIP bị khoá) -> 0 = đầu chương")
        void negativeOrEmpty() {
            assertEquals(0, ReadingProgress.clampPara(-3, 40));
            assertEquals(0, ReadingProgress.clampPara(5, 0));
        }
    }

    @Nested
    @DisplayName("ChapterBookmark.snippetOf — đoạn trích trong tủ sách")
    class Snippet {

        @Test
        @DisplayName("Đoạn ngắn giữ nguyên, chỉ gộp khoảng trắng")
        void shortKept() {
            assertEquals("Gió nổi lên. Kiếm rơi.",
                    ChapterBookmark.snippetOf("  Gió   nổi lên.\n Kiếm rơi.  "));
        }

        @Test
        @DisplayName("Đoạn dài bị cắt ở ranh giới TỪ, có dấu …, không vượt giới hạn")
        void longCutAtWord() {
            String word = "truyện ";
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 400) sb.append(word);

            String s = ChapterBookmark.snippetOf(sb.toString());
            assertTrue(s.endsWith("…"));
            assertTrue(s.length() <= ChapterBookmark.SNIPPET_MAX + 1);
            // Không cắt đôi chữ: phần trước dấu … phải kết thúc bằng trọn chữ "truyện"
            assertTrue(s.substring(0, s.length() - 1).endsWith("truyện"), s);
        }

        @Test
        @DisplayName("Một \"từ\" dài bất thường (không có dấu cách) vẫn bị cắt")
        void longSingleToken() {
            String s = ChapterBookmark.snippetOf("a".repeat(500));
            assertEquals(ChapterBookmark.SNIPPET_MAX + 1, s.length());
        }

        @Test
        @DisplayName("null -> chuỗi rỗng, không NullPointerException")
        void nullSafe() {
            assertEquals("", ChapterBookmark.snippetOf(null));
        }
    }

    @Nested
    @DisplayName("RecommendationSet.merge — một khối gợi ý, không trùng")
    class Merge {

        private Story s(int id) {
            Story st = new Story();
            st.setId(id);
            return st;
        }

        @Test
        @DisplayName("Nguồn trước được ưu tiên, truyện trùng giữ LÝ DO của nguồn trước")
        void priorityAndReason() {
            RecommendationSet r = RecommendationSet.merge(1, 8,
                    Arrays.asList(Arrays.asList(s(5), s(6)), Arrays.asList(s(6), s(7))),
                    Arrays.asList(RecommendationSet.REASON_ALSO_READ, RecommendationSet.REASON_SAME_TAG));

            assertEquals(3, r.size());
            assertEquals(5, r.getStories().get(0).getId());
            assertEquals(RecommendationSet.REASON_ALSO_READ, r.getReasons().get(6));
            assertEquals(RecommendationSet.REASON_SAME_TAG, r.getReasons().get(7));
        }

        @Test
        @DisplayName("Không bao giờ gợi ý chính truyện đang xem")
        void excludesSelf() {
            RecommendationSet r = RecommendationSet.merge(3, 8,
                    Collections.singletonList(Arrays.asList(s(3), s(4))),
                    Collections.singletonList("x"));
            assertEquals(1, r.size());
            assertEquals(4, r.getStories().get(0).getId());
        }

        @Test
        @DisplayName("Dừng đúng limit dù các nguồn còn dư")
        void respectsLimit() {
            List<Story> many = Arrays.asList(s(10), s(11), s(12), s(13), s(14));
            RecommendationSet r = RecommendationSet.merge(1, 4,
                    Arrays.asList(many, many), Arrays.asList("a", "b"));
            assertEquals(4, r.size());
        }

        @Test
        @DisplayName("Nguồn null hoặc limit 0 -> rỗng, không ném lỗi")
        void nullSafe() {
            assertTrue(RecommendationSet.merge(1, 0, null, null).isEmpty());
            assertTrue(RecommendationSet.merge(1, 4,
                    Arrays.asList(null, Collections.singletonList(s(2))), null).size() == 1);
        }
    }

    @Test
    @DisplayName("findRecommendations với id không hợp lệ -> rỗng, không chạm CSDL")
    void recommendationsInvalidId() throws SQLException {
        StoryDAO dao = new StoryDAO();
        assertTrue(dao.findRecommendations(0, 8).isEmpty());
        assertTrue(dao.findRecommendations(-5, 8).isEmpty());
        assertTrue(dao.findRecommendations(1, 0).isEmpty());
    }
}
