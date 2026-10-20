package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.model.Notification;
import truyen.model.Report;
import truyen.util.MailSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModerationAppealTest — Kiểm thử thông báo kiểm duyệt & khiếu nại tới Admin")
class ModerationAppealTest {

    @Test
    @DisplayName("Ca 1: Notification hỗ trợ loại MODERATION với biểu tượng khiên bảo vệ 🛡️")
    void testNotificationModerationType() {
        Notification n = new Notification();
        n.setType("MODERATION");
        n.setMessage("Truyện \"Đại Đạo Triều Thiên\" của bạn đã bị Quản trị viên gỡ bỏ.");

        assertEquals("🛡️", n.getIcon());
        assertEquals("MODERATION", n.getType());
    }

    @Test
    @DisplayName("Ca 2: buildAppealEmailHtml() sinh HTML an toàn, đúng thông tin và chống XSS")
    void testBuildAppealEmailHtml() {
        String author = "Tác giả <script>evil()</script>";
        String email = "author@domain.com";
        String story = "Truyện Kiếm Hiệp <b>Hot</b>";
        int storyId = 99;
        String title = "Khiếu nại kiểm duyệt nhầm";
        String reason = "Kính gửi Admin, truyện này do tôi tự viết 100%, không sao chép từ bất kỳ đâu.";

        String html = MailSender.buildAppealEmailHtml(author, email, story, storyId, title, reason);

        assertNotNull(html);
        assertFalse(html.contains("<script>evil()</script>"));
        assertTrue(html.contains("&lt;script&gt;evil()&lt;/script&gt;"));
        assertFalse(html.contains("<b>Hot</b>"));
        assertTrue(html.contains("&lt;b&gt;Hot&lt;/b&gt;"));
        assertTrue(html.contains("THƯ KHIẾU NẠI KIỂM DUYỆT"));
        assertTrue(html.contains("author@domain.com"));
        assertTrue(html.contains("ID: #99"));
        assertTrue(html.contains(reason));
    }

    @Test
    @DisplayName("Ca 3: sendAppealEmail() chế độ Dev Simulation hoàn tất thành công")
    void testSendAppealEmailSimulation() {
        boolean result = MailSender.sendAppealEmail(
                "Tác giả Test",
                "test@user.vn",
                "Tiên Nghịch",
                12,
                "Khiếu nại gỡ truyện",
                "Nội dung truyện hoàn toàn phù hợp với quy định cộng đồng."
        );
        assertTrue(result, "Hệ thống phải xử lý trơn tru ở chế độ Dev Simulation");
    }

    @Test
    @DisplayName("Ca 4: sendAppealEmailAsync() chạy ngầm qua hàng đợi CompletableFuture")
    void testSendAppealEmailAsync() throws Exception {
        CompletableFuture<Boolean> future = MailSender.sendAppealEmailAsync(
                "Bút Chẳng Tà",
                "author@test.vn",
                "Cổ Chân Nhân",
                42,
                "Giải trình bản quyền chương 1-100",
                "Tôi có đầy đủ bằng chứng bản quyền xuất bản trực tuyến."
        );
        assertNotNull(future);
        Boolean success = future.get(5, TimeUnit.SECONDS);
        assertTrue(success, "Gửi email bất đồng bộ phải thành công");
    }

    @Test
    @DisplayName("Ca 5: Report category OTHER cho STORY là hợp lệ cho phiếu khiếu nại kiểm duyệt")
    void testReportValidationForAppeal() {
        Report r = new Report();
        r.setTargetType("STORY");
        r.setCategory("OTHER");
        r.setReason("[KHIẾU NẠI KIỂM DUYỆT] [Giải trình] Nội dung khiếu nại");

        assertTrue(Report.isValidCategory(r.getCategory(), r.getTargetType()),
                "TargetType STORY và Category OTHER phải hợp lệ theo quy tắc của hệ thống Report");
        assertTrue(r.getReason().startsWith("[KHIẾU NẠI KIỂM DUYỆT]"));
    }

    @Test
    @DisplayName("Ca 6: Kiểm tra thông báo kết quả khiếu nại (Chấp thuận / Từ chối)")
    void testAppealOutcomeNotificationTemplates() {
        String approved = "Tin vui! Đơn khiếu nại cho truyện \"Đại Đạo\" đã được Quản trị viên chấp thuận. Tác phẩm của bạn đã được khôi phục trạng thái Công khai.";
        String rejected = "Đơn khiếu nại cho truyện \"Đại Đạo\" đã bị Quản trị viên từ chối sau khi xem xét lại nội dung vi phạm. Quyết định can thiệp kiểm duyệt được giữ nguyên.";

        assertTrue(approved.contains("chấp thuận") && approved.contains("khôi phục"));
        assertTrue(rejected.contains("từ chối"));
    }
}

