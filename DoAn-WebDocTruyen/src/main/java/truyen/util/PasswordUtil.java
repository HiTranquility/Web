package truyen.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Băm và kiểm tra mật khẩu. */
public class PasswordUtil {

    private static final String ALGO = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    // SecureRandom, KHÔNG phải Random. Random dự đoán được từ vài giá trị đầu,
    // dùng cho salt là vô hiệu hoá luôn tác dụng của salt.
    private static final SecureRandom RANDOM = new SecureRandom();

    public static final int MIN_LENGTH = 6;

    /**
     * Có TRẦN độ dài. PBKDF2 chạy 120.000 vòng trên TỪNG ký tự của mật khẩu;
     * không có trần thì ai đó gửi một "mật khẩu" vài MB vào ô đăng ký là bắt
     * máy chủ đốt CPU vài giây cho mỗi request. 128 thừa sức cho mật khẩu thật.
     */
    public static final int MAX_LENGTH = 128;

    /**
     * MỘT luật độ dài cho cả đăng ký, đổi mật khẩu và đặt lại mật khẩu.
     * @return câu báo lỗi, hoặc null nếu hợp lệ
     */
    public static String lengthError(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return "Mật khẩu phải từ " + MIN_LENGTH + " ký tự trở lên.";
        }
        if (password.length() > MAX_LENGTH) {
            return "Mật khẩu tối đa " + MAX_LENGTH + " ký tự.";
        }
        return null;
    }

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

    /** So sánh hai mảng byte trong thời gian KHÔNG phụ thuộc nội dung. */
    private static boolean slowEquals(byte[] a, byte[] b) {
        int diff = a.length ^ b.length;
        for (int i = 0; i < a.length && i < b.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }
}
