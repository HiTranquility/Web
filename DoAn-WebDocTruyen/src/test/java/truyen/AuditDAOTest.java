package truyen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.dao.AuditDAO;
import truyen.model.AuditLog;

@DisplayName("AuditDAO & AuditLog — Nhật ký thao tác quản trị (ISSUE-023)")
class AuditDAOTest {

    @Test
    @DisplayName("Ca 1: AuditLog model khởi tạo đầy đủ và ánh xạ nhãn tiếng Việt chính xác")
    void testAuditLogModelLabels() {
        AuditLog log1 = new AuditLog(1, "DELETE_STORY", "STORY", 10, "Đấu Phá Khung Thương", "Nội dung vi phạm");
        assertEquals("Gỡ truyện", log1.getActionLabel());
        assertEquals("badge-danger", log1.getActionBadgeClass());
        assertEquals("STORY", log1.getTargetType());
        assertEquals(10, log1.getTargetId());
        assertEquals("Đấu Phá Khung Thương", log1.getTargetLabel());
        assertNotNull(log1.getFormattedTime());

        AuditLog log2 = new AuditLog(1, "RESTORE_STORY", "STORY", 10, "Đấu Phá Khung Thương", "Khôi phục hợp lệ");
        assertEquals("Khôi phục truyện", log2.getActionLabel());
        assertEquals("badge-success", log2.getActionBadgeClass());

        AuditLog log3 = new AuditLog(2, "GRANT_COIN", "WALLET", 5, "Ví User #5 (+500 xu)", "Thưởng sự kiện");
        assertEquals("Cấp xu", log3.getActionLabel());
        assertEquals("badge-success", log3.getActionBadgeClass());

        AuditLog log4 = new AuditLog(2, "REVOKE_COIN", "WALLET", 5, "Ví User #5 (-200 xu)", "Thu hồi tặng nhầm");
        assertEquals("Thu hồi xu", log4.getActionLabel());
        assertEquals("badge-danger", log4.getActionBadgeClass());

        AuditLog log5 = new AuditLog(1, "BAN_USER", "USER", 8, "spammer", "Spam liên tục");
        assertEquals("Khoá tài khoản", log5.getActionLabel());
        assertEquals("badge-danger", log5.getActionBadgeClass());

        AuditLog log6 = new AuditLog(1, "UNBAN_USER", "USER", 8, "spammer", "Hết hạn khoá");
        assertEquals("Mở khoá tài khoản", log6.getActionLabel());
        assertEquals("badge-success", log6.getActionBadgeClass());
    }

    @Test
    @DisplayName("Ca 2: AuditDAO thực thi an toàn, không ném ngoại lệ ở chế độ không CSDL")
    void testAuditDAOSafeExecution() throws java.sql.SQLException {
        AuditDAO dao = new AuditDAO();
        // Không có kết nối DB hoặc chế độ demo phải không sập
        boolean ok = dao.log(1, "DELETE_STORY", "STORY", 99, "Truyện test", "Lý do test");
        assertTrue(ok, "log() phải trả về an toàn khi CSDL chưa kích hoạt hoặc thực thi thành công");

        int count = dao.count("ALL");
        assertTrue(count >= 0, "count() phải trả về số nguyên không âm");
    }
}
