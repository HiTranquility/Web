package truyen.util;

import java.text.Normalizer;
import java.util.Locale;

/** Đổi tiêu đề tiếng Việt thành chuỗi thân thiện URL. */
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
