package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import truyen.model.ReadHistory;

/**
 * Hai hàm tính sẵn cho JSP trong ReadHistory (trang 31 và dải "Đọc tiếp").
 *
 * Cả hai đều nằm ở model đúng vì lý do này: ở model thì test được, còn viết
 * trong JSP thì chỉ mở trình duyệt nhìn bằng mắt mới biết đúng hay sai.
 */
@DisplayName("ReadHistory — nhãn thời gian và phần trăm đọc")
class ReadHistoryTest {

    private static ReadHistory doc(int soNgayTruoc) {
        ReadHistory h = new ReadHistory();
        // trừ thêm 2 giờ để không rơi đúng ranh giới nửa đêm
        h.setLastViewed(LocalDateTime.now().minusDays(soNgayTruoc).minusHours(2));
        return h;
    }

    private static ReadHistory tienDo(int daDoc, int tongSo) {
        ReadHistory h = new ReadHistory();
        h.setLastChapterNo(daDoc);
        h.setTotalChapters(tongSo);
        return h;
    }

    @Test
    @DisplayName("Nhãn: hôm nay, hôm qua, N ngày trước, rồi ngày/tháng")
    void nhanThoiGian() {
        assertEquals("Hôm nay", doc(0).getViewedLabel());
        assertEquals("Hôm qua", doc(1).getViewedLabel());
        assertEquals("3 ngày trước", doc(3).getViewedLabel());
        assertEquals("6 ngày trước", doc(6).getViewedLabel());

        /*
         * Từ một tuần trở đi thì đổi sang ngày/tháng. "23 ngày trước" bắt
         * người đọc tự nhẩm ra hôm nào — mà chính là việc mình nên làm thay
         * họ. Dưới một tuần thì ngược lại: "5 ngày trước" dễ hình dung hơn
         * một con số ngày tháng.
         */
        assertTrue(doc(20).getViewedLabel().matches("\\d+/\\d+"),
                "quá một tuần phải hiện ngày/tháng, nhận được: "
                + doc(20).getViewedLabel());
    }

    @Test
    @DisplayName("Chưa từng đọc -> nhãn rỗng, không ném lỗi")
    void chuaDoc() {
        assertEquals("", new ReadHistory().getViewedLabel());
    }

    @Test
    @DisplayName("Phần trăm là SỐ NGUYÊN, không phải số thập phân dài")
    void phanTramNguyen() {
        /*
         * 3/7 = 42,857142857142854 nếu tính bằng EL trong JSP — cả chuỗi đó
         * đi thẳng vào thuộc tính style của HTML. Trình duyệt vẽ vẫn đúng,
         * nhưng xem mã nguồn trang thì rất bẩn.
         */
        assertEquals(42, tienDo(3, 7).getProgressPercent());
        assertEquals(50, tienDo(5, 10).getProgressPercent());
        assertEquals(100, tienDo(7, 7).getProgressPercent());
    }

    @Test
    @DisplayName("Truyện chưa có chương nào -> 0, KHÔNG chia cho 0")
    void chiaChoKhong() {
        assertEquals(0, tienDo(0, 0).getProgressPercent());
        assertEquals(0, new ReadHistory().getProgressPercent());
    }

    @Test
    @DisplayName("Tác giả xoá bớt chương -> chặn ở 100, không vượt")
    void khongVuot100() {
        /*
         * Người đọc tới chương 10, tác giả xoá còn 5 chương. Không chặn thì
         * phần trăm ra 200 và thanh tiến độ tràn ra ngoài khung.
         */
        assertEquals(100, tienDo(10, 5).getProgressPercent());
    }

    @Test
    @DisplayName("isResumable: có vị trí đọc mới cho 'Đọc tiếp'")
    void doTiep() {
        assertFalse(new ReadHistory().isResumable(), "chưa đọc thì không có gì để tiếp");

        ReadHistory h = new ReadHistory();
        h.setLastChapterId(42);
        assertTrue(h.isResumable());
    }

    @Test
    @DisplayName("getInitial: chữ cái đầu làm bìa thay thế")
    void chuCaiDau() {
        ReadHistory h = new ReadHistory();
        h.setStoryTitle("Kiếm Khí");
        assertEquals("K", h.getInitial());

        // Không có tên -> dấu hỏi, KHÔNG ném StringIndexOutOfBounds
        assertEquals("?", new ReadHistory().getInitial());

        ReadHistory rong = new ReadHistory();
        rong.setStoryTitle("");
        assertEquals("?", rong.getInitial());
    }
}
