package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.AchievementDAO;
import truyen.model.Achievement;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.DemoData;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hệ thống Tủ Huy Hiệu & Bộ Lọc Nâng Cao")
public class AchievementTest {

    @Test
    @DisplayName("Ca 1: Tính toán phần trăm tiến độ của Huy hiệu Thành tích")
    void testAchievementProgressPercent() {
        Achievement ach1 = new Achievement("test_1", "Thử nghiệm 1", "⭐", "Test", "Mô tả", false, 15, 30, "15/30");
        assertEquals(50, ach1.getProgressPercent());
        assertFalse(ach1.isUnlocked());

        Achievement ach2 = new Achievement("test_2", "Thử nghiệm 2", "👑", "Test", "Mô tả", true, 30, 30, "30/30");
        assertEquals(100, ach2.getProgressPercent());
        assertTrue(ach2.isUnlocked());

        Achievement ach3 = new Achievement("test_3", "Thử nghiệm 3", "🔥", "Test", "Mô tả", false, 0, 10, "0/10");
        assertEquals(0, ach3.getProgressPercent());
        assertFalse(ach3.isUnlocked());
    }

    @Test
    @DisplayName("Ca 2: Danh sách Huy hiệu mặc định trả về đủ 8 danh hiệu")
    void testDefaultMockAchievements() {
        AchievementDAO dao = new AchievementDAO();
        List<Achievement> list = dao.getAchievements(0, null);

        assertNotNull(list);
        assertEquals(8, list.size());

        assertTrue(list.stream().anyMatch(a -> "night_owl".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "bookworm".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "scholar".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "generous".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "critic".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "streak_master".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "vip_patron".equals(a.getId())));
        assertTrue(list.stream().anyMatch(a -> "collector".equals(a.getId())));
    }

    @Test
    @DisplayName("Ca 3: Trạng thái Huy hiệu VIP thay đổi theo trạng thái người dùng")
    void testVipAchievementStatus() {
        AchievementDAO dao = new AchievementDAO();

        // User thường không có VIP
        User normalUser = new User();
        normalUser.setId(999);
        normalUser.setVipExpireAt(null);

        List<Achievement> normalList = dao.getAchievements(normalUser.getId(), normalUser);
        Achievement normalVipAch = normalList.stream()
                .filter(a -> "vip_patron".equals(a.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(normalVipAch);
        assertFalse(normalVipAch.isUnlocked());

        // User có VIP
        User vipUser = new User();
        vipUser.setId(1000);
        vipUser.setVipExpireAt(LocalDateTime.now().plusDays(30));

        List<Achievement> vipList = dao.getAchievements(vipUser.getId(), vipUser);
        Achievement vipAch = vipList.stream()
                .filter(a -> "vip_patron".equals(a.getId()))
                .findFirst()
                .orElse(null);
        assertNotNull(vipAch);
        assertTrue(vipAch.isUnlocked());
        assertEquals(100, vipAch.getProgressPercent());
    }

    @Test
    @DisplayName("Ca 4: Bộ lọc DemoData theo số lượng chương (short < 10, medium 10..50, long > 50)")
    void testDemoDataChapterFilters() {
        List<Story> shortStories = DemoData.filter(null, null, null, null, "short");
        assertNotNull(shortStories);
        for (Story s : shortStories) {
            assertTrue(s.getChapterCount() < 10, "Truyện ngắn phải có ít hơn 10 chương: " + s.getTitle() + " (" + s.getChapterCount() + ")");
        }

        List<Story> mediumStories = DemoData.filter(null, null, null, null, "medium");
        assertNotNull(mediumStories);
        for (Story s : mediumStories) {
            assertTrue(s.getChapterCount() >= 10 && s.getChapterCount() <= 50,
                    "Truyện vừa phải có 10-50 chương: " + s.getTitle() + " (" + s.getChapterCount() + ")");
        }

        List<Story> longStories = DemoData.filter(null, null, null, null, "long");
        assertNotNull(longStories);
        for (Story s : longStories) {
            assertTrue(s.getChapterCount() > 50,
                    "Truyện dài phải có hơn 50 chương: " + s.getTitle() + " (" + s.getChapterCount() + ")");
        }
    }
}
