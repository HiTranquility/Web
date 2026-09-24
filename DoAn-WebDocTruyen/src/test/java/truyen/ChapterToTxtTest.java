package truyen;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.model.Chapter;
import truyen.model.Story;
import truyen.util.ChapterToTxt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ChapterToTxt — Định dạng chương truyện và tên file sao lưu Drive (Phase 5)")
class ChapterToTxtTest {

    @Test
    @DisplayName("Ca 1: Tên file có số 3 chữ số (001, 029, 105) chuẩn định dạng Drive")
    void testFormatChapterFileName3Digits() {
        assertEquals("001 - Mở đầu.txt", ChapterToTxt.formatChapterFileName(1, "Mở đầu"));
        assertEquals("029 - Hồi kết.txt", ChapterToTxt.formatChapterFileName(29, "Hồi kết"));
        assertEquals("105 - Ngoại truyện đặc biệt.txt", ChapterToTxt.formatChapterFileName(105, "Ngoại truyện đặc biệt"));
    }

    @Test
    @DisplayName("Ca 2: Tiêu đề chương rỗng hoặc null -> sinh tên file số 3 chữ số an toàn")
    void testFormatChapterFileNameEmptyTitle() {
        assertEquals("001.txt", ChapterToTxt.formatChapterFileName(1, null));
        assertEquals("002.txt", ChapterToTxt.formatChapterFileName(2, ""));
        assertEquals("003.txt", ChapterToTxt.formatChapterFileName(3, "   "));
    }

    @Test
    @DisplayName("Ca 3: Tiêu đề chứa ký tự cấm của hệ điều hành/Drive (\\/:*?\"<>|) -> khử sạch")
    void testSanitizeFileName() {
        String dirtyTitle = "Hồi 1: Gặp gỡ / Chia ly? \"Bất ngờ\" <Hay> *Kỳ lạ* | Tái ngộ\\";
        String fileName = ChapterToTxt.formatChapterFileName(1, dirtyTitle);

        assertFalse(fileName.contains(":"));
        assertFalse(fileName.contains("/"));
        assertFalse(fileName.contains("?"));
        assertFalse(fileName.contains("\""));
        assertFalse(fileName.contains("<"));
        assertFalse(fileName.contains(">"));
        assertFalse(fileName.contains("*"));
        assertFalse(fileName.contains("|"));
        assertFalse(fileName.contains("\\"));
        assertTrue(fileName.startsWith("001 - "));
        assertTrue(fileName.endsWith(".txt"));
    }

    @Test
    @DisplayName("Ca 4: Ghép từng chương đơn lẻ — đúng tiêu đề, phân cách và nội dung tiếng Việt")
    void testFormatSingleChapter() {
        Chapter c = new Chapter();
        c.setChapterNo(7);
        c.setTitle("Dưới bóng cây phong");
        c.setContent("Gió thu thổi nhẹ qua từng tán lá vàng rơi rụng.\nTiếng bước chân vang lên đều đặn.");

        String text = ChapterToTxt.formatChapter(c);

        assertTrue(text.startsWith("Chương 7: Dưới bóng cây phong\n"));
        assertTrue(text.contains("------------------------------------------------------------\n\n"));
        assertTrue(text.contains("Gió thu thổi nhẹ qua từng tán lá vàng rơi rụng."));
    }

    @Test
    @DisplayName("Ca 5: Ghép trọn bộ truyện (DownloadServlet) — đúng thứ tự, header và footer")
    void testFormatStoryFull() {
        Story s = new Story();
        s.setTitle("Dấu Chân Mùa Thu");
        s.setAuthorName("Mộc Miên");
        s.setDescription("Một câu chuyện tình nhẹ nhàng giữa mùa thu Hà Nội.");

        List<Chapter> list = new ArrayList<>();
        Chapter c1 = new Chapter();
        c1.setChapterNo(1);
        c1.setTitle("Khởi đầu");
        c1.setContent("Nội dung chương 1");
        list.add(c1);

        Chapter c2 = new Chapter();
        c2.setChapterNo(2);
        c2.setTitle("Gặp gỡ");
        c2.setContent("Nội dung chương 2");
        list.add(c2);

        String full = ChapterToTxt.formatStory(s, list);

        assertTrue(full.contains("Dấu Chân Mùa Thu\n"));
        assertTrue(full.contains("Tác giả: Mộc Miên\n"));
        assertTrue(full.contains("Một câu chuyện tình nhẹ nhàng giữa mùa thu Hà Nội."));
        assertTrue(full.contains("Chương 1: Khởi đầu"));
        assertTrue(full.contains("Chương 2: Gặp gỡ"));
        assertTrue(full.contains("Tải từ web Đọc Truyện — đồ án môn Lập trình Web"));

        // Kiểm tra thứ tự chương 1 xuất hiện trước chương 2
        int idx1 = full.indexOf("Chương 1: Khởi đầu");
        int idx2 = full.indexOf("Chương 2: Gặp gỡ");
        assertTrue(idx1 < idx2, "Chương 1 phải xuất hiện trước chương 2");
    }

    @Test
    @DisplayName("Ca 6: Truyện 0 chương -> không ném lỗi, thông báo tử tế")
    void testFormatStoryZeroChapters() {
        Story s = new Story();
        s.setTitle("Truyện Chưa Có Chương");
        s.setAuthorName("Tác Giả Mới");

        String full = ChapterToTxt.formatStory(s, new ArrayList<>());
        assertTrue(full.contains("(Truyện chưa có chương nào.)"));
    }

    @Test
    @DisplayName("Ca 7: repeat helper lặp chuỗi chính xác")
    void testRepeat() {
        assertEquals("", ChapterToTxt.repeat('=', 0));
        assertEquals("", ChapterToTxt.repeat('=', -5));
        assertEquals("=====", ChapterToTxt.repeat('=', 5));
    }
}
