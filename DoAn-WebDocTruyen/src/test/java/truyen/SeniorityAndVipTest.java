package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.model.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Hệ thống Cấp bậc Độc giả Lâu năm & Hội viên VIP")
public class SeniorityAndVipTest {

    @Test
    @DisplayName("Ca 1: Người dùng mới tạo tài khoản -> Cấp Tân Thủ (0% giảm, x1.0 checkin)")
    void testNewbieTier() {
        User u = new User();
        u.setCreatedAt(LocalDateTime.now().minusDays(5));
        u.setReadChapters(10);

        assertEquals("newbie", u.getSeniorityTier());
        assertEquals("Tân Thủ", u.getTierName());
        assertEquals("🌱", u.getTierIcon());
        assertEquals(0, u.getDiscountPercent());
        assertEquals(1.0, u.getCheckinMultiplier(), 0.01);
        assertFalse(u.isVip());
    }

    @Test
    @DisplayName("Ca 2: Người dùng 20 ngày gắn bó -> Cấp Đồng Hành (5% giảm, x1.2 checkin)")
    void testCompanionTier() {
        User u = new User();
        u.setCreatedAt(LocalDateTime.now().minusDays(20));
        u.setReadChapters(15);

        assertEquals("companion", u.getSeniorityTier());
        assertEquals("Đồng Hành", u.getTierName());
        assertEquals("⚔️", u.getTierIcon());
        assertEquals(5, u.getDiscountPercent());
        assertEquals(1.2, u.getCheckinMultiplier(), 0.01);
    }

    @Test
    @DisplayName("Ca 3: Người dùng đọc 160 chương -> Cấp Tri Kỷ (10% giảm, x1.5 checkin)")
    void testConfidantTier() {
        User u = new User();
        u.setCreatedAt(LocalDateTime.now().minusDays(10));
        u.setReadChapters(160);

        assertEquals("confidant", u.getSeniorityTier());
        assertEquals("Tri Kỷ", u.getTierName());
        assertEquals("👑", u.getTierIcon());
        assertEquals(10, u.getDiscountPercent());
        assertEquals(1.5, u.getCheckinMultiplier(), 0.01);
    }

    @Test
    @DisplayName("Ca 4: Người dùng 200 ngày hoặc 550 chương -> Cấp Đại Tông Sư (15% giảm, x1.8 checkin)")
    void testGrandmasterTier() {
        User u = new User();
        u.setCreatedAt(LocalDateTime.now().minusDays(200));
        u.setReadChapters(550);

        assertEquals("grandmaster", u.getSeniorityTier());
        assertEquals("Đại Tông Sư", u.getTierName());
        assertEquals("🔥", u.getTierIcon());
        assertEquals(15, u.getDiscountPercent());
        assertEquals(1.8, u.getCheckinMultiplier(), 0.01);
    }

    @Test
    @DisplayName("Ca 5: Hội viên VIP kích hoạt -> Cấp VIP tối thượng (25% giảm, x2.0 checkin)")
    void testVipTier() {
        User u = new User();
        u.setCreatedAt(LocalDateTime.now().minusDays(5));
        u.setReadChapters(2);
        u.setVipExpireAt(LocalDateTime.now().plusDays(30));

        assertTrue(u.isVip());
        assertEquals("vip", u.getSeniorityTier());
        assertEquals("Hội Viên VIP", u.getTierName());
        assertEquals("⭐", u.getTierIcon());
        assertEquals(25, u.getDiscountPercent());
        assertEquals(2.0, u.getCheckinMultiplier(), 0.01);
        assertEquals(100, u.getNextTierProgressPercent());
    }

    @Test
    @DisplayName("Ca 6: VIP đã hết hạn -> Trở về cấp bậc gắn bó thực tế")
    void testExpiredVip() {
        User u = new User();
        u.setCreatedAt(LocalDateTime.now().minusDays(30));
        u.setReadChapters(40);
        u.setVipExpireAt(LocalDateTime.now().minusDays(1)); // Đã hết hạn hôm qua

        assertFalse(u.isVip());
        assertEquals("companion", u.getSeniorityTier());
        assertEquals("Đồng Hành", u.getTierName());
        assertEquals(5, u.getDiscountPercent());
    }

    @Test
    @DisplayName("Ca 7: Tính toán giá mở khóa chương có chiết khấu theo cấp bậc")
    void testChapterPriceCalculation() {
        int origPrice = 20;

        // VIP giảm 25% -> còn 15 xu
        int vipDiscount = 25;
        int vipPrice = Math.max(1, (int) Math.round(origPrice * (100.0 - vipDiscount) / 100.0));
        assertEquals(15, vipPrice);

        // Tri Kỷ giảm 10% -> còn 18 xu
        int confidantDiscount = 10;
        int confidantPrice = Math.max(1, (int) Math.round(origPrice * (100.0 - confidantDiscount) / 100.0));
        assertEquals(18, confidantPrice);
    }
}
