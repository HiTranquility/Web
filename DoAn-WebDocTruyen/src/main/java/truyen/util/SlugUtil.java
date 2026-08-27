package truyen.util;

import java.text.Normalizer;
import java.util.Locale;

/**
 * Đổi tiêu đề tiếng Việt thành chuỗi thân thiện URL.
 *
 *     "Kiếm Khí Trường Sinh"   ->   "kiem-khi-truong-sinh"
 *     "Đường Về Cố Hương"      ->   "duong-ve-co-huong"
 *
 * VÌ SAO CẦN
 *   URL có dấu và dấu cách bị trình duyệt mã hoá thành %E1%BA%BF%20... — vừa
 *   xấu, vừa dài, vừa dễ hỏng khi copy sang chỗ khác. Slug thì gõ tay được,
 *   chia sẻ được, và công cụ tìm kiếm đọc hiểu được.
 *
 * CÁCH BỎ DẤU TIẾNG VIỆT — hai bước, thứ tự quan trọng
 *
 *   Bước 1: thay "đ" và "Đ" THỦ CÔNG.
 *     Phải làm TRƯỚC. Lý do: "đ" KHÔNG phải là "d" cộng thêm dấu — nó là một
 *     chữ cái RIÊNG trong bảng Unicode. Nên bước 2 bên dưới không tách được nó,
 *     và nếu bỏ qua bước này thì "đ" bị xoá mất luôn.
 *
 *   Bước 2: Normalizer.NFD rồi xoá ký tự dấu.
 *     NFD tách "ế" thành ba phần: "e" + dấu mũ + dấu sắc, mỗi dấu là một ký tự
 *     riêng thuộc nhóm Unicode "Mark". Xoá hết nhóm đó là còn lại "e".
 */
public class SlugUtil {

    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        String s = input.trim().toLowerCase(Locale.forLanguageTag("vi"));

        // BƯỚC 1 — bắt buộc làm trước bước 2, xem giải thích ở khối trên
        s = s.replace('đ', 'd');   // đ
        s = s.replace('Đ', 'd');   // Đ

        // BƯỚC 2 — tách chữ khỏi dấu, rồi xoá dấu
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{Mn}\\p{Mc}]", "");

        s = s.replaceAll("[^a-z0-9\\s-]", "");   // bỏ ký tự đặc biệt còn lại
        s = s.replaceAll("[\\s-]+", "-");        // khoảng trắng -> một gạch ngang
        s = s.replaceAll("^-+|-+$", "");         // bỏ gạch thừa ở hai đầu

        // Cột slug trong database là VARCHAR(220), cắt cho chắc
        if (s.length() > 200) {
            s = s.substring(0, 200).replaceAll("-+$", "");
        }
        return s;
    }

    /**
     * Slug phải UNIQUE trong bảng stories. Trùng thì thêm số vào đuôi:
     *     "tien-hiep"  ->  "tien-hiep-2"  ->  "tien-hiep-3"
     *
     * StoryDAO gọi hàm này trong vòng lặp cho tới khi tìm được slug chưa ai dùng.
     */
    public static String withSuffix(String slug, int n) {
        return n <= 1 ? slug : slug + "-" + n;
    }
}
