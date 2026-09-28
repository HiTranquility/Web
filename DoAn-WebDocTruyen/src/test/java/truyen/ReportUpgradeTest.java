package truyen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import truyen.model.Report;
import truyen.model.ReportEvidence;
import truyen.util.RateLimiter;

@DisplayName("Báo cáo vi phạm — phân loại, ảnh bằng chứng, link tới nội dung (ISSUE-025)")
class ReportUpgradeTest {

    @Nested
    @DisplayName("Phân loại vi phạm")
    class Categories {

        @Test
        @DisplayName("Ca 1: Đủ 8 loại, và 'Khác' đứng cuối cùng")
        void testEightCategories() {
            assertEquals(8, Report.CATEGORIES.size());
            assertEquals("OTHER", Report.CATEGORIES.get(Report.CATEGORIES.size() - 1),
                    "\"Khác\" phải đứng cuối ô chọn, không lẫn vào giữa");
        }

        @Test
        @DisplayName("Ca 2: Form bình luận KHÔNG có 'Đạo văn' và 'Sai thể loại'")
        void testCommentCategoriesFiltered() {
            List<String> forComment = Report.categoriesFor("COMMENT");

            assertFalse(forComment.contains("PLAGIARISM"),
                    "Đạo văn vô nghĩa với một dòng bình luận");
            assertFalse(forComment.contains("WRONG_INFO"),
                    "Sai thể loại vô nghĩa với một dòng bình luận");
            assertTrue(forComment.contains("HARASSMENT"));
            assertTrue(forComment.contains("SPAM"));
        }

        @Test
        @DisplayName("Ca 3: Form truyện KHÔNG có 'Quấy rối', nhưng có 'Đạo văn'")
        void testStoryCategoriesFiltered() {
            List<String> forStory = Report.categoriesFor("STORY");

            assertFalse(forStory.contains("HARASSMENT"),
                    "Quấy rối là chuyện giữa người với người, không phải của truyện");
            assertTrue(forStory.contains("PLAGIARISM"));
            assertTrue(forStory.contains("WRONG_INFO"));
        }

        @Test
        @DisplayName("Ca 4: Loại sai ngữ cảnh bị từ chối — không tin ô select của trình duyệt")
        void testCrossContextCategoryRejected() {
            // Người dùng sửa HTML rồi gửi PLAGIARISM cho một bình luận.
            assertFalse(Report.isValidCategory("PLAGIARISM", "COMMENT"));
            assertFalse(Report.isValidCategory("HARASSMENT", "STORY"));

            // Rác và rỗng cũng phải trượt.
            assertFalse(Report.isValidCategory("KHONG_CO_LOAI_NAY", "STORY"));
            assertFalse(Report.isValidCategory(null, "STORY"));
            assertFalse(Report.isValidCategory("", "STORY"));

            // Còn loại đúng ngữ cảnh thì phải lọt.
            assertTrue(Report.isValidCategory("PLAGIARISM", "STORY"));
            assertTrue(Report.isValidCategory("HARASSMENT", "COMMENT"));
            assertTrue(Report.isValidCategory("SPAM", "STORY"));
            assertTrue(Report.isValidCategory("SPAM", "COMMENT"));
        }

        @Test
        @DisplayName("Ca 5: Đúng ba loại nặng được đẩy lên đầu hàng đợi")
        void testSevereCategories() {
            assertEquals(3, Report.SEVERE.size());
            assertTrue(Report.SEVERE.contains("ADULT"));
            assertTrue(Report.SEVERE.contains("VIOLENCE"));
            assertTrue(Report.SEVERE.contains("PRIVACY"));

            Report r = new Report();
            r.setCategory("ADULT");
            assertTrue(r.isSevere());

            r.setCategory("SPAM");
            assertFalse(r.isSevere(), "Spam để chiều xử cũng được, không phải loại nặng");
        }

        @Test
        @DisplayName("Ca 6: Mọi loại đều có nhãn tiếng Việt, kể cả mã rác")
        void testEveryCategoryHasLabel() {
            for (String code : Report.CATEGORIES) {
                String label = Report.categoryLabel(code);
                assertFalse(label == null || label.isEmpty(),
                        "Loại " + code + " chưa có nhãn");
            }
            // Mã lạ hoặc null không được làm vỡ trang.
            assertEquals("Khác", Report.categoryLabel(null));
            assertEquals("Khác", Report.categoryLabel("MA_LA_HOAC"));
        }
    }

    @Nested
    @DisplayName("Link tới nội dung bị báo cáo")
    class DeepLink {

        @Test
        @DisplayName("Ca 7: storyId = 0 nghĩa là nội dung đã bị xoá hẳn")
        void testTargetGone() {
            Report r = new Report();
            r.setTargetType("COMMENT");
            r.setTargetId(99);
            r.setStoryId(0);            // COALESCE trả NULL -> rs.getInt() ra 0

            assertTrue(r.isTargetGone(),
                    "Phải hiện \"Nội dung không còn tồn tại\" thay vì link chết");
        }

        @Test
        @DisplayName("Ca 8: Báo cáo bình luận có storyId thì dựng được link sâu")
        void testCommentHasStoryId() {
            Report r = new Report();
            r.setTargetType("COMMENT");
            r.setTargetId(42);
            r.setStoryId(7);            // truyện chứa bình luận đó

            assertFalse(r.isTargetGone());
            assertFalse(r.isStory());
            assertEquals(7, r.getStoryId());
            assertEquals(42, r.getTargetId());
            // JSP dựng: /story?action=detail&id=7#comment-42
        }
    }

    @Nested
    @DisplayName("Ảnh bằng chứng")
    class Evidence {

        @Test
        @DisplayName("Ca 9: Không có ảnh thì danh sách RỖNG chứ không null")
        void testEvidenceNeverNull() {
            Report r = new Report();
            assertFalse(r.getEvidences() == null, "JSP sẽ nổ NPE nếu null");
            assertFalse(r.isHasEvidence());

            // Gán null cũng phải quy về rỗng.
            r.setEvidences(null);
            assertFalse(r.getEvidences() == null);
        }

        @Test
        @DisplayName("Ca 10: Cỡ file hiện ra dạng người đọc được")
        void testSizeLabel() {
            ReportEvidence ev = new ReportEvidence();

            ev.setFileSize(512);
            assertEquals("512 B", ev.getSizeLabel());

            ev.setFileSize(200 * 1024);
            assertEquals("200 KB", ev.getSizeLabel());

            ev.setFileSize((int) (1.5 * 1024 * 1024));
            assertEquals("1.5 MB", ev.getSizeLabel());
        }
    }

    @Nested
    @DisplayName("Chống spam báo cáo")
    class Throttle {

        @BeforeEach
        void reset() {
            RateLimiter.clear();
        }

        @Test
        @DisplayName("Ca 11: Ba lượt đầu lọt, lượt thứ tư bị chặn")
        void testFourthReportBlocked() {
            int userId = 5;

            for (int i = 1; i <= RateLimiter.MAX_REPORTS_PER_WINDOW; i++) {
                assertFalse(RateLimiter.isReportSpam(userId),
                        "Lượt thứ " + i + " phải lọt — người thật báo cáo vài cái liền nhau");
                RateLimiter.recordReport(userId);
            }

            assertTrue(RateLimiter.isReportSpam(userId),
                    "Lượt thứ 4 phải bị chặn — mỗi báo cáo kéo theo tối đa 3 ảnh trên đĩa");
            assertTrue(RateLimiter.getReportCooldownRemainingSeconds(userId) > 0);
        }

        @Test
        @DisplayName("Ca 12: Hạn mức tính riêng từng người, không lây sang nhau")
        void testPerUserIsolation() {
            int a = 5, b = 6;

            for (int i = 0; i < RateLimiter.MAX_REPORTS_PER_WINDOW; i++) {
                RateLimiter.recordReport(a);
            }

            assertTrue(RateLimiter.isReportSpam(a));
            assertFalse(RateLimiter.isReportSpam(b),
                    "Một người spam không được khoá mồm người khác");
        }

        @Test
        @DisplayName("Ca 13: Khách chưa đăng nhập (userId = 0) không làm vỡ bộ đếm")
        void testAnonymousIsNoop() {
            assertFalse(RateLimiter.isReportSpam(0));
            RateLimiter.recordReport(0);
            assertEquals(0, RateLimiter.getReportCooldownRemainingSeconds(0));
        }
    }
}
