package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import truyen.model.Bookmark;

@DisplayName("Bookmark — Tiến độ đọc và trạng thái trong Tủ sách")
class BookmarkTest {

    @Test
    @DisplayName("Chưa đọc chương nào: progress = 0%, status = unread, badge = 'Chưa đọc'")
    void testChuaDoc() {
        Bookmark b = new Bookmark();
        b.setLastChapterId(0);
        b.setLastChapterNo(0);
        b.setTotalChapters(50);

        assertFalse(b.isStarted());
        assertEquals(0, b.getProgressPercent());
        assertEquals("unread", b.getReadingStatus());
        assertEquals("Chưa đọc", b.getStatusBadge());
    }

    @Test
    @DisplayName("Đang đọc dở: progress tính chính xác, status = reading, badge = 'Đang đọc'")
    void testDangDoc() {
        Bookmark b = new Bookmark();
        b.setLastChapterId(10);
        b.setLastChapterNo(25);
        b.setTotalChapters(100);

        assertTrue(b.isStarted());
        assertEquals(25, b.getProgressPercent());
        assertEquals("reading", b.getReadingStatus());
        assertEquals("Đang đọc", b.getStatusBadge());
    }

    @Test
    @DisplayName("Đã đọc xong: progress = 100%, status = completed, badge = 'Đã đọc xong'")
    void testDaDocXong() {
        Bookmark b = new Bookmark();
        b.setLastChapterId(100);
        b.setLastChapterNo(100);
        b.setTotalChapters(100);

        assertTrue(b.isStarted());
        assertEquals(100, b.getProgressPercent());
        assertEquals("completed", b.getReadingStatus());
        assertEquals("Đã đọc xong", b.getStatusBadge());
    }

    @Test
    @DisplayName("Biên: totalChapters = 0 không ném ArithmeticException (chia cho 0)")
    void testTotalChaptersZero() {
        Bookmark b = new Bookmark();
        b.setLastChapterId(5);
        b.setLastChapterNo(5);
        b.setTotalChapters(0);

        assertEquals(0, b.getProgressPercent());
        assertEquals("reading", b.getReadingStatus());
    }
}
