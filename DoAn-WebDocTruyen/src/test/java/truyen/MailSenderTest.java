package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.util.MailSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MailSenderTest — Kiểm thử dịch vụ gửi email SMTP (ISSUE-002)")
class MailSenderTest {

    @Test
    @DisplayName("Ca 1: buildResetEmailHtml() sinh mã HTML đẹp mắt, an toàn và chứa đúng liên kết")
    void testBuildResetEmailHtml() {
        String name = "Nguyễn Văn A <script>alert(1)</script>";
        String link = "https://example.com/auth?action=reset&token=xyz123";

        String html = MailSender.buildResetEmailHtml(name, link);

        assertNotNull(html);
        assertTrue(html.contains("https://example.com/auth?action=reset&amp;token=xyz123")
                || html.contains("https://example.com/auth?action=reset&token=xyz123"));
        // Đảm bảo chống XSS trong tên người nhận
        assertFalse(html.contains("<script>alert(1)</script>"));
        assertTrue(html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
        assertTrue(html.contains("Đặt lại mật khẩu"));
    }

    @Test
    @DisplayName("Ca 2: sendPasswordResetEmail() trong chế độ mô phỏng trả về true và không gây lỗi")
    void testDevSimulationMode() {
        boolean result = MailSender.sendPasswordResetEmail(
                "user@example.com",
                "Độc giả Mộc Miên",
                "http://localhost:8080/auth?action=reset&token=test-token-456"
        );

        assertTrue(result, "Chế độ mô phỏng an toàn phải trả về true khi email hợp lệ");
    }

    @Test
    @DisplayName("Ca 3: sendPasswordResetEmail() trả về false khi địa chỉ email rỗng hoặc null")
    void testInvalidEmail() {
        assertFalse(MailSender.sendPasswordResetEmail(null, "Test", "http://localhost:8080"));
        assertFalse(MailSender.sendPasswordResetEmail("", "Test", "http://localhost:8080"));
        assertFalse(MailSender.sendPasswordResetEmail("   ", "Test", "http://localhost:8080"));
    }

    @Test
    @DisplayName("Ca 4: sendPasswordResetEmailAsync() chạy bất đồng bộ trong hàng đợi thành công")
    void testAsyncQueueExecution() throws Exception {
        CompletableFuture<Boolean> future = MailSender.sendPasswordResetEmailAsync(
                "tester@gmail.com",
                "Hải Đường",
                "http://localhost:8080/auth?action=reset&token=async-token-789"
        );

        assertNotNull(future);
        Boolean success = future.get(5, TimeUnit.SECONDS);
        assertTrue(success);
    }
}
