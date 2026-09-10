package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import truyen.model.Chapter;

/**
 * Cắt nội dung chương thành từng đoạn — Chapter.getParagraphs().
 *
 * VÌ SAO HÀM NÀY XỨNG ĐÁNG CÓ TEST RIÊNG
 *   Nó từng hỏng theo kiểu tệ nhất: KHÔNG BÁO LỖI GÌ. Cả chương hiện ra dính
 *   liền thành một khối chữ, trang vẫn tải bình thường, console sạch trơn.
 *
 *   Nguyên nhân là file .jsp được lưu kiểu CRLF nên ký tự xuống dòng viết
 *   thẳng trong fn:replace là "\r\n", trong khi nội dung chương lưu bằng "\n"
 *   — hai chuỗi khác nhau nên không khớp gì cả. Đổi sang c:forTokens cũng
 *   hỏng, vì JSP cổ điển không giải mã thực thể HTML trong thuộc tính thẻ.
 *
 *   Chốt lại: việc cắt đoạn thuộc về MODEL, không thuộc về JSP. Và một khi đã
 *   ở model thì test được — đó chính là lợi ích của việc đặt đúng chỗ.
 *
 * Mấy test dưới đây khoá lại đúng những trường hợp đã làm mình mất thời gian.
 */
@DisplayName("Chapter.getParagraphs — cắt đoạn văn")
class ChapterParagraphTest {

    private static Chapter withContent(String content) {
        Chapter c = new Chapter();
        c.setContent(content);
        return c;
    }

    @Test
    @DisplayName("Xuống dòng kiểu Unix (\\n)")
    void unix() {
        List<String> p = withContent("Đoạn một\nĐoạn hai\nĐoạn ba").getParagraphs();
        assertEquals(3, p.size());
        assertEquals("Đoạn một", p.get(0));
        assertEquals("Đoạn ba", p.get(2));
    }

    @Test
    @DisplayName("Xuống dòng kiểu Windows (\\r\\n) — không sót ký tự \\r")
    void windows() {
        List<String> p = withContent("Đoạn một\r\nĐoạn hai").getParagraphs();
        assertEquals(2, p.size());

        /* Không được sót "\r" ở cuối đoạn. Sót thì mắt thường không thấy,
           nhưng nó là một ký tự thật nằm trong HTML. */
        assertEquals("Đoạn một", p.get(0));
        assertFalse(p.get(0).contains("\r"), "còn sót ký tự \\r");
    }

    /*
     * GHI CHÚ SAU KHI THỬ PHÁ CODE
     *
     * Đổi split("\\R") thành split("\n") rồi chạy lại: test CRLF ngay trên
     * VẪN ĐẠT. Lý do là .trim() trong getParagraphs() dọn hộ ký tự "\r" còn
     * lửng ở cuối — hai hàng rào chồng lên nhau, gỡ một cái vẫn còn cái kia.
     *
     * Hai test dưới mới là chỗ thật sự bắt được: chúng có "\r" ĐỨNG MỘT MÌNH,
     * mà split("\n") không coi đó là chỗ ngắt dòng nên hai đoạn dính làm một
     * và trim() chẳng cứu được gì.
     *
     * Bài học: một test "đạt" chưa chắc đã canh được thứ mình tưởng. Cách duy
     * nhất để biết là cố tình phá code rồi xem test có đỏ không.
     */
    @Test
    @DisplayName("Xuống dòng kiểu Mac cũ (\\r đứng một mình)")
    void macCu() {
        assertEquals(2, withContent("Đoạn một\rĐoạn hai").getParagraphs().size());
    }

    @Test
    @DisplayName("Trộn lẫn cả ba kiểu trong một chương")
    void tronLan() {
        // Tác giả soạn ở Word rồi dán vào — chuyện xảy ra thật
        List<String> p = withContent("Một\nHai\r\nBa\rBốn").getParagraphs();
        assertEquals(4, p.size());
        assertEquals(List.of("Một", "Hai", "Ba", "Bốn"), p);
    }

    @Test
    @DisplayName("Dòng trống bị bỏ, không thành đoạn rỗng")
    void bochDongTrong() {
        // Người ta hay gõ hai lần Enter cho thoáng
        List<String> p = withContent("Đoạn một\n\n\nĐoạn hai").getParagraphs();
        assertEquals(2, p.size(), "dòng trống không được thành thẻ <p> rỗng");
    }

    @Test
    @DisplayName("Nội dung rỗng hoặc null không được ném NullPointerException")
    void rong() {
        assertTrue(withContent(null).getParagraphs().isEmpty());
        assertTrue(withContent("").getParagraphs().isEmpty());
        assertTrue(withContent("   \n  \n ").getParagraphs().isEmpty());
    }

    @Test
    @DisplayName("Một đoạn duy nhất, không có ký tự xuống dòng nào")
    void motDoan() {
        List<String> p = withContent("Chỉ có một đoạn").getParagraphs();
        assertEquals(1, p.size());
        assertEquals("Chỉ có một đoạn", p.get(0));
    }
}
