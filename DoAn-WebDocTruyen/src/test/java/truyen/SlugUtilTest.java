package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import truyen.util.SlugUtil;

/**
 * Đổi tiêu đề tiếng Việt thành chuỗi cho URL.
 *
 * Slug là thứ ĐI VÀO ĐƯỜNG DẪN, mà đường dẫn thì người ta chia sẻ cho nhau và
 * công cụ tìm kiếm lưu lại. Sai một lần là link chết vĩnh viễn.
 */
@DisplayName("SlugUtil — tiêu đề thành slug")
class SlugUtilTest {

    @Test
    @DisplayName("Bỏ dấu tiếng Việt")
    void boDau() {
        assertEquals("kiem-khi-truong-sinh", SlugUtil.toSlug("Kiếm Khí Trường Sinh"));
        assertEquals("ngon-tinh", SlugUtil.toSlug("Ngôn tình"));
        assertEquals("huyen-huyen", SlugUtil.toSlug("Huyền huyễn"));
    }

    @Test
    @DisplayName("Chữ đ và Đ thành d — KHÔNG bị mất")
    void chuD() {
        /*
         * Đây là chỗ dễ sai nhất và lý do phải xử lý 'đ' TRƯỚC khi chuẩn hoá
         * NFD: 'đ' không phải "d + dấu" như 'ế' là "e + dấu". Nó là một chữ
         * cái riêng. Chuẩn hoá NFD không tách nó ra được, nên bước xoá dấu
         * phía sau sẽ XOÁ LUÔN cả chữ.
         *
         * Làm sai thì "Đường" ra "uong" — mất hẳn chữ đầu.
         */
        assertEquals("duong", SlugUtil.toSlug("Đường"));
        assertEquals("dai-duong", SlugUtil.toSlug("Đại Dương"));
        assertEquals("do-an", SlugUtil.toSlug("Đồ án"));
        assertFalse(SlugUtil.toSlug("Đường").isEmpty());
    }

    @Test
    @DisplayName("Khoảng trắng thành một gạch ngang, không thành nhiều")
    void khoangTrang() {
        assertEquals("ba-tu", SlugUtil.toSlug("Ba    Từ"));
        assertEquals("ba-tu", SlugUtil.toSlug("  Ba Từ  "));
        assertEquals("a-b", SlugUtil.toSlug("A - B"));
    }

    @Test
    @DisplayName("Bỏ ký tự đặc biệt, không để lọt vào URL")
    void kyTuDacBiet() {
        assertEquals("truyen-hay", SlugUtil.toSlug("Truyện hay!!!"));
        assertEquals("100-ngay", SlugUtil.toSlug("100% ngày"));
        assertEquals("a-b", SlugUtil.toSlug("A & B"));
        // Không được còn dấu ?, &, = — chúng có nghĩa riêng trong URL
        assertFalse(SlugUtil.toSlug("a?b=c&d").matches(".*[?&=].*"));
    }

    @Test
    @DisplayName("Không có gạch ngang thừa ở đầu hoặc cuối")
    void khongGachThua() {
        assertEquals("truyen", SlugUtil.toSlug("---Truyện---"));
        assertEquals("truyen", SlugUtil.toSlug("!!! Truyện !!!"));
    }

    @Test
    @DisplayName("Rỗng và null -> chuỗi rỗng, không ném lỗi")
    void rong() {
        assertEquals("", SlugUtil.toSlug(null));
        assertEquals("", SlugUtil.toSlug(""));
        // Tiêu đề toàn ký tự đặc biệt -> không còn gì
        assertEquals("", SlugUtil.toSlug("!!!???"));
    }

    @Test
    @DisplayName("Tiêu đề dài bị cắt, và không kết thúc bằng gạch ngang")
    void catNgan() {
        String dai = "rat-dai ".repeat(50);          // 400 ký tự
        String slug = SlugUtil.toSlug(dai);
        assertTrue(slug.length() <= 200, "vượt quá giới hạn cột: " + slug.length());
        assertFalse(slug.endsWith("-"), "cắt xong còn gạch ngang lửng ở cuối");
    }

    @Test
    @DisplayName("withSuffix: số 1 giữ nguyên, từ 2 mới thêm đuôi")
    void themDuoi() {
        /*
         * Truyện ĐẦU TIÊN mang tên đó phải có slug sạch "tien-hiep", không
         * phải "tien-hiep-1". Chỉ từ cái thứ hai trở đi mới cần phân biệt.
         */
        assertEquals("tien-hiep", SlugUtil.withSuffix("tien-hiep", 0));
        assertEquals("tien-hiep", SlugUtil.withSuffix("tien-hiep", 1));
        assertEquals("tien-hiep-2", SlugUtil.withSuffix("tien-hiep", 2));
        assertEquals("tien-hiep-10", SlugUtil.withSuffix("tien-hiep", 10));
    }
}
