package truyen;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.GamificationDAO;
import truyen.dao.ReviewDAO;
import truyen.dao.UnlockDAO;
import truyen.model.DailyQuest;
import truyen.model.Review;

@DisplayName("New Features — Chương VIP, Đánh giá Spoiler & Gamification")
class NewFeaturesTest {

    private final UnlockDAO unlockDAO = new UnlockDAO();
    private final ReviewDAO reviewDAO = new ReviewDAO();
    private final GamificationDAO gamificationDAO = new GamificationDAO();

    @Test
    @DisplayName("Ca 1: UnlockDAO.hasUnlocked trả về false cho người dùng chưa mở khoá")
    void testUnlockDAOForNonUnlocked() throws SQLException {
        // userId 99999 chưa mở khoá chương 61
        boolean unlocked = unlockDAO.hasUnlocked(99999, 61);
        assertFalse(unlocked);
    }

    @Test
    @DisplayName("Ca 2: UnlockDAO.findUnlockedChapterIds trả về tập hợp an toàn không null")
    void testFindUnlockedChapterIds() throws SQLException {
        List<Integer> ids = unlockDAO.findUnlockedChapterIds(1, 1);
        assertNotNull(ids);
    }

    @Test
    @DisplayName("Ca 3: Review model lưu trữ đúng điểm số, cờ spoiler và số vote")
    void testReviewModel() {
        Review r = new Review();
        r.setId(100);
        r.setStoryId(1);
        r.setUserId(2);
        r.setScore(5);
        r.setTitle("Truyện xuất sắc");
        r.setContent("Phân tích cốt truyện chi tiết");
        r.setHasSpoiler(true);
        r.setHelpfulCount(12);
        r.setVotedByMe(true);

        assertEquals(5, r.getScore());
        assertTrue(r.isHasSpoiler());
        assertEquals(12, r.getHelpfulCount());
        assertTrue(r.isVotedByMe());
        assertEquals("Truyện xuất sắc", r.getTitle());
    }

    @Test
    @DisplayName("Ca 4: ReviewDAO.findByStory trả về danh sách không null")
    void testReviewDAOQuery() throws SQLException {
        List<Review> list = reviewDAO.findByStory(1, 1);
        assertNotNull(list);
    }

    @Test
    @DisplayName("Ca 5: GamificationDAO.getDailyQuests trả về đủ 3 nhiệm vụ ngày")
    void testGamificationQuests() throws SQLException {
        List<DailyQuest> quests = gamificationDAO.getDailyQuests(1);
        assertNotNull(quests);
        assertEquals(3, quests.size());
        assertTrue(quests.stream().anyMatch(q -> "DAILY_CHECKIN".equals(q.getKey())));
        assertTrue(quests.stream().anyMatch(q -> "READ_CHAPTER".equals(q.getKey())));
        assertTrue(quests.stream().anyMatch(q -> "COMMENT_REVIEW".equals(q.getKey())));
    }
}
