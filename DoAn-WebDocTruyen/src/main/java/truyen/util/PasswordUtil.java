package truyen.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Băm và kiểm tra mật khẩu.
 *
 * TUYỆT ĐỐI KHÔNG LƯU MẬT KHẨU DẠNG CHỮ THƯỜNG.
 * Lộ database là lộ hết tài khoản — và vì người ta hay dùng chung một mật khẩu
 * cho nhiều nơi, lộ ở đây là lộ luôn email, Facebook của họ.
 *
 * BA THỨ LÀM NÊN MỘT HÀM BĂM MẬT KHẨU TỬ TẾ
 *
 *  1. MỘT CHIỀU — băm được, không giải ngược được.
 *     Nên "quên mật khẩu" chỉ đặt lại được, không lấy lại được. Web nào gửi
 *     mail báo đúng mật khẩu cũ của bạn nghĩa là nó lưu chữ thường.
 *
 *  2. CÓ SALT — mỗi người một chuỗi ngẫu nhiên riêng, trộn vào trước khi băm.
 *     Không có salt thì hai người cùng đặt "123456" sẽ ra cùng một chuỗi băm,
 *     và kẻ tấn công tra bảng tính sẵn (rainbow table) là ra ngay.
 *
 *  3. CHẬM CÓ CHỦ Ý — lặp 120.000 vòng.
 *     Nghe vô lý nhưng đây là điểm mấu chốt. MD5/SHA-256 được thiết kế để
 *     NHANH, nên máy tấn công thử được hàng tỉ mật khẩu mỗi giây. Hàm băm mật
 *     khẩu cố tình chậm (~100ms) — người dùng đăng nhập không thấy gì, còn kẻ
 *     dò thì từ hàng tỉ xuống còn vài chục lần thử mỗi giây.
 *
 * VÌ SAO DÙNG PBKDF2 CHỨ KHÔNG PHẢI BCRYPT
 *   BCrypt tốt hơn một chút, nhưng phải thêm thư viện ngoài (jbcrypt).
 *   PBKDF2 có SẴN trong JDK, không cần tải gì — với đồ án thì đây là đánh đổi
 *   đúng: an toàn thật, mà máy nào có Java là chạy được.
 *
 *   ĐỪNG dùng MD5 hay SHA-256 trần cho mật khẩu. Chúng nhanh, và nhanh là dở.
 *
 * ĐỊNH DẠNG CHUỖI LƯU VÀO DATABASE
 *   pbkdf2$120000$&lt;salt-base64&gt;$&lt;hash-base64&gt;
 *   Gói cả tham số vào chuỗi để sau này tăng số vòng lặp mà mật khẩu cũ vẫn
 *   kiểm được — vì mỗi chuỗi tự mang theo số vòng của chính nó.
 */
public class PasswordUtil {

    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    // SecureRandom, KHÔNG phải Random. Random dự đoán được từ vài giá trị đầu,
    // dùng cho salt là vô hiệu hoá luôn tác dụng của salt.
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Băm mật khẩu để lưu vào database.
     * Gọi hai lần với cùng một mật khẩu sẽ ra HAI chuỗi khác nhau — vì salt
     * random. Đó là đúng, không phải lỗi.
     */
    public static String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        byte[] key = pbkdf2(password.toCharArray(), salt, ITERATIONS);

        return "pbkdf2$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(key);
    }

    /**
     * Kiểm tra mật khẩu người dùng gõ có khớp chuỗi băm trong database không.
     *
     * Không "giải mã" chuỗi băm (không giải được). Cách làm: lấy salt và số
     * vòng lặp ra khỏi chuỗi đã lưu, băm lại mật khẩu vừa gõ bằng đúng tham số
     * đó, rồi so hai kết quả.
     */
    public static boolean verify(String password, String stored) {
        if (password == null || stored == null) {
            return false;
        }
        String[] parts = stored.split("\\$");
        if (parts.length != 4 || !"pbkdf2".equals(parts[0])) {
            return false;   // chuỗi hỏng hoặc định dạng lạ -> coi như sai
        }
        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expected = Base64.getDecoder().decode(parts[3]);

            byte[] actual = pbkdf2(password.toCharArray(), salt, iterations);
            return slowEquals(expected, actual);

        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGO).generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            // Hai lỗi này chỉ xảy ra khi JDK hỏng — không phải lỗi người dùng,
            // và không có cách xử lý tử tế nào. Ném lên cho sập sớm còn hơn
            // âm thầm cho qua với mật khẩu không được băm.
            throw new IllegalStateException("Không băm được mật khẩu", e);
        }
    }

    /**
     * So sánh hai mảng byte trong thời gian KHÔNG phụ thuộc nội dung.
     *
     * Vì sao không dùng Arrays.equals(): nó dừng ngay khi gặp byte đầu tiên
     * khác nhau. Kẻ tấn công đo thời gian phản hồi có thể đoán dần từng byte
     * của chuỗi băm — gọi là "timing attack".
     *
     * Cách dưới đây luôn duyệt HẾT mảng, dồn khác biệt vào biến diff bằng phép
     * XOR, nên chạy bao lâu cũng như nhau dù sai ở byte đầu hay byte cuối.
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
