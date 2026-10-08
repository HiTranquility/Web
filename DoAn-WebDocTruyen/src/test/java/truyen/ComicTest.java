package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import truyen.model.Chapter;
import truyen.model.ChapterPage;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.ChapterAccess;
import truyen.util.ImageInfo;
import truyen.util.UploadUtil;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Truyện tranh (migration-028): loại truyện, kích thước ảnh, thứ tự trang và
 * luật quyền đọc dùng chung. Toàn hàm thuần — không cần MySQL hay Tomcat.
 */
@DisplayName("Truyện tranh · loại truyện · trang ảnh · quyền đọc")
class ComicTest {

    // =========================================================== loại truyện
    @Nested
    @DisplayName("Story.normalizeType / filterType")
    class StoryType {

        @Test
        @DisplayName("Giá trị lạ, rỗng, null -> TEXT; không phân biệt hoa thường")
        void normalize() {
            assertEquals(Story.TYPE_COMIC, Story.normalizeType("comic"));
            assertEquals(Story.TYPE_COMIC, Story.normalizeType(" COMIC "));
            assertEquals(Story.TYPE_TEXT, Story.normalizeType("AUDIO"));
            assertEquals(Story.TYPE_TEXT, Story.normalizeType(""));
            assertEquals(Story.TYPE_TEXT, Story.normalizeType(null));
        }

        @Test
        @DisplayName("Bộ lọc: rỗng / lạ -> null (tất cả), KHÔNG ép về TEXT")
        void filter() {
            assertEquals(Story.TYPE_COMIC, Story.filterType("comic"));
            assertEquals(Story.TYPE_TEXT, Story.filterType("text"));
            assertNull(Story.filterType(""));
            assertNull(Story.filterType("' OR 1=1 --"));
            assertNull(Story.filterType(null));
        }

        @Test
        @DisplayName("Truyện mới mặc định là truyện chữ; nhãn đơn vị đổi theo loại")
        void defaults() {
            Story s = new Story();
            assertFalse(s.isComic());
            assertEquals("đoạn", s.getUnitLabel());
            s.setStoryType("COMIC");
            assertTrue(s.isComic());
            assertEquals("trang", s.getUnitLabel());
            assertEquals("Truyện tranh", s.getTypeLabel());
        }
    }

    // =========================================================== kích thước ảnh
    @Nested
    @DisplayName("ImageInfo.dimensions — đọc rộng x cao từ phần đầu file")
    class Dimensions {

        private int[] dim(byte[] b) throws IOException {
            return ImageInfo.dimensions(new ByteArrayInputStream(b));
        }

        @Test
        @DisplayName("PNG: đọc từ khung IHDR")
        void png() throws IOException {
            byte[] b = new byte[33];
            byte[] sig = { (byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 13, 'I', 'H', 'D', 'R' };
            System.arraycopy(sig, 0, b, 0, sig.length);
            b[16] = 0; b[17] = 0; b[18] = 0x03; b[19] = 0x20;     // 800
            b[20] = 0; b[21] = 0; b[22] = 0x2E; b[23] = (byte) 0xE0;   // 12000
            assertArrayEquals(new int[] { 800, 12000 }, dim(b));
        }

        @Test
        @DisplayName("GIF: little-endian 16 bit")
        void gif() throws IOException {
            byte[] b = new byte[30];
            byte[] h = { 'G', 'I', 'F', '8', '9', 'a', 0x40, 0x01, (byte) 0xF0, 0x00 };   // 320 x 240
            System.arraycopy(h, 0, b, 0, h.length);
            assertArrayEquals(new int[] { 320, 240 }, dim(b));
        }

        @Test
        @DisplayName("JPEG: bỏ qua khung APP0 rồi đọc khung SOF0")
        void jpeg() throws IOException {
            byte[] b = {
                (byte) 0xFF, (byte) 0xD8,                                   // SOI
                (byte) 0xFF, (byte) 0xE0, 0x00, 0x10,                       // APP0, dài 16
                'J', 'F', 'I', 'F', 0, 1, 1, 0, 0, 1, 0, 1, 0, 0,
                (byte) 0xFF, (byte) 0xC0, 0x00, 0x11, 0x08,                 // SOF0, 8 bit
                0x07, (byte) 0xD0,                                          // cao 2000
                0x02, (byte) 0xBC,                                          // rộng 700
                0x03, 0, 0, 0, 0, 0, 0, 0, 0, 0
            };
            assertArrayEquals(new int[] { 700, 2000 }, dim(b));
        }

        @Test
        @DisplayName("WebP VP8X: rộng-1 / cao-1, 24 bit")
        void webpExtended() throws IOException {
            byte[] b = new byte[30];
            byte[] h = { 'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8', 'X' };
            System.arraycopy(h, 0, b, 0, h.length);
            b[24] = (byte) 0x1F; b[25] = 0x03; b[26] = 0;    // 799 -> 800
            b[27] = (byte) 0xAF; b[28] = 0x04; b[29] = 0;    // 1199 -> 1200
            assertArrayEquals(new int[] { 800, 1200 }, dim(b));
        }

        @Test
        @DisplayName("File không phải ảnh / quá ngắn -> null, không ném lỗi")
        void garbage() throws IOException {
            assertNull(dim("hello world, not an image at all".getBytes()));
            assertNull(dim(new byte[] { 1, 2, 3 }));
        }
    }

    // =========================================================== thứ tự trang
    @Nested
    @DisplayName("ChapterPage.arrange — sắp xếp, bỏ, thêm trang")
    class Arrange {

        private ChapterPage old(int id) {
            ChapterPage p = new ChapterPage();
            p.setId(id);
            return p;
        }
        private ChapterPage fresh(String path) {
            ChapterPage p = new ChapterPage();
            p.setImagePath(path);
            return p;
        }
        private String sig(List<ChapterPage> l) {
            StringBuilder sb = new StringBuilder();
            for (ChapterPage p : l) sb.append(p.getId() > 0 ? "p" + p.getId() : p.getImagePath()).append(' ');
            return sb.toString().trim();
        }

        @Test
        @DisplayName("Chèn trang mới vào giữa, đảo trang cũ, bỏ một trang")
        void mixed() {
            List<ChapterPage> r = ChapterPage.arrange(
                    Arrays.asList(old(1), old(2), old(3)),
                    Arrays.asList(fresh("a"), fresh("b")),
                    Arrays.asList("p3", "n0", "p1", "n1", "p2"),
                    new HashSet<>(Collections.singletonList(2)));
            assertEquals("p3 a p1 b", sig(r));
        }

        @Test
        @DisplayName("Form quên nhắc tới trang nào thì GIỮ LẠI, xếp cuối")
        void keepsForgotten() {
            List<ChapterPage> r = ChapterPage.arrange(
                    Arrays.asList(old(1), old(2)), Collections.singletonList(fresh("a")),
                    Collections.singletonList("p2"), null);
            assertEquals("p2 p1 a", sig(r));
        }

        @Test
        @DisplayName("Token rác, lặp, id của chương khác -> bỏ qua")
        void junkTokens() {
            List<ChapterPage> r = ChapterPage.arrange(
                    Arrays.asList(old(1), old(2)), Collections.emptyList(),
                    Arrays.asList("p2", "p2", "p999", "x1", "", "n7", "pABC"), null);
            assertEquals("p2 p1", sig(r));
        }
    }

    // =========================================================== quyền đọc
    @Nested
    @DisplayName("ChapterAccess — một luật cho trang đọc, tải về, đánh dấu và ảnh trang")
    class Access {

        private User user(int id, boolean admin) {
            User u = new User();
            u.setId(id);
            u.setRole(admin ? "ADMIN" : "USER");
            return u;
        }
        private Story story(String status) {
            Story s = new Story();
            s.setAuthorId(10);
            s.setStatus(status);
            return s;
        }
        private Chapter vip() {
            Chapter c = new Chapter();
            c.setId(5);
            c.setVip(true);
            return c;
        }
        private final ChapterAccess.UnlockCheck none = (u, c) -> false;
        private final ChapterAccess.UnlockCheck paid = (u, c) -> true;

        @Test
        @DisplayName("Chương VIP: khoá với người thường, mở với tác giả / admin / người đã trả xu")
        void vipRules() throws SQLException {
            Story s = story("PUBLISHED");
            assertTrue(ChapterAccess.isLocked(user(1, false), s, vip(), none));
            assertFalse(ChapterAccess.isLocked(user(1, false), s, vip(), paid));
            assertFalse(ChapterAccess.isLocked(user(10, false), s, vip(), none));   // tác giả
            assertFalse(ChapterAccess.isLocked(user(2, true), s, vip(), none));     // admin
            assertTrue(ChapterAccess.isLocked(null, s, vip(), paid));               // khách
        }

        @Test
        @DisplayName("Nháp chỉ tác giả/admin; đã gỡ thì không ai; khách không đọc được gì")
        void storyStatus() throws SQLException {
            Chapter free = new Chapter();
            assertFalse(ChapterAccess.canRead(user(1, false), story("DRAFT"), free, none));
            assertTrue(ChapterAccess.canRead(user(10, false), story("DRAFT"), free, none));
            assertFalse(ChapterAccess.canRead(user(2, true), story("DELETED"), free, none));
            assertFalse(ChapterAccess.canRead(null, story("PUBLISHED"), free, none));
            assertTrue(ChapterAccess.canRead(user(1, false), story("PUBLISHED"), free, none));
        }
    }

    @Test
    @DisplayName("UploadUtil.chapterIdOfPage — đọc chapterId từ đường dẫn ảnh, chặn đường dẫn lạ")
    void chapterIdOfPage() {
        assertEquals(12, UploadUtil.chapterIdOfPage("pages/3/12/abc.jpg"));
        assertEquals(0, UploadUtil.chapterIdOfPage("pages/3/abc.jpg"));
        assertEquals(0, UploadUtil.chapterIdOfPage("pages/3/x/abc.jpg"));
        assertEquals(0, UploadUtil.chapterIdOfPage("evidence/2026/10/a.jpg"));
        assertEquals(0, UploadUtil.chapterIdOfPage(null));
    }
}
