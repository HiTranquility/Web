package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import truyen.util.PasswordUtil;

/**
 * Băm và kiểm mật khẩu.
 *
 * Đây là chỗ mà "chạy được" và "đúng" khác nhau xa nhất trong cả dự án: một
 * hàm băm sai vẫn cho đăng nhập bình thường, chỉ là không bảo vệ được gì.
 * Không có test thì không ai phát hiện.
 */
@DisplayName("PasswordUtil — băm mật khẩu")
class PasswordUtilTest {

    @Test
    @DisplayName("Băm rồi kiểm lại đúng mật khẩu -> khớp")
    void khop() {
        String hash = PasswordUtil.hash("matkhau123");
        assertTrue(PasswordUtil.verify("matkhau123", hash));
    }

    @Test
    @DisplayName("Sai mật khẩu -> không khớp")
    void saiMatKhau() {
        String hash = PasswordUtil.hash("matkhau123");
        assertFalse(PasswordUtil.verify("matkhau124", hash));
        assertFalse(PasswordUtil.verify("Matkhau123", hash), "phải phân biệt hoa thường");
        assertFalse(PasswordUtil.verify("", hash));
    }

    @Test
    @DisplayName("CÙNG một mật khẩu, hai lần băm phải RA KHÁC NHAU")
    void muoiNgauNhien() {
        String a = PasswordUtil.hash("matkhau123");
        String b = PasswordUtil.hash("matkhau123");

        /*
         * Đây là phép kiểm quan trọng nhất file này.
         *
         * Hai chuỗi băm giống nhau nghĩa là KHÔNG CÓ MUỐI ngẫu nhiên. Hậu quả:
         * lộ database là nhìn thấy ngay ai đang dùng chung một mật khẩu, và
         * kẻ tấn công băm sẵn một bảng mật khẩu phổ biến rồi tra ngược ra hết
         * (bảng cầu vồng). Muối làm mỗi tài khoản phải phá riêng.
         */
        assertNotEquals(a, b, "thiếu muối ngẫu nhiên");

        // Nhưng cả hai đều phải kiểm được
        assertTrue(PasswordUtil.verify("matkhau123", a));
        assertTrue(PasswordUtil.verify("matkhau123", b));
    }

    @Test
    @DisplayName("Chuỗi băm ghi rõ thuật toán và số vòng lặp")
    void dinhDang() {
        String hash = PasswordUtil.hash("matkhau123");

        /*
         * Dạng "pbkdf2$120000$muối$băm". Ghi số vòng vào trong chuỗi để sau
         * này tăng lên 300.000 thì mật khẩu cũ vẫn kiểm được — mỗi chuỗi tự
         * mang theo tham số của chính nó. Viết cứng số vòng trong code thì
         * ngày tăng lên là mọi người đăng nhập không được nữa.
         */
        String[] phan = hash.split("\\$");
        assertEquals(4, phan.length, "phải có 4 phần ngăn bởi $");
        assertEquals("pbkdf2", phan[0]);
        assertTrue(Integer.parseInt(phan[1]) >= 100_000,
                "số vòng lặp quá thấp: " + phan[1]);
    }

    @Test
    @DisplayName("Chuỗi băm hỏng -> trả false, KHÔNG ném lỗi")
    void chuoiHong() {
        /*
         * Dữ liệu trong CSDL có thể hỏng vì đủ lý do — sửa tay, nhập lỗi,
         * chuyển đổi thiếu. Lúc đó phải TỪ CHỐI đăng nhập, chứ ném exception
         * thì thành trang 500 và người dùng không hiểu chuyện gì.
         */
        assertFalse(PasswordUtil.verify("matkhau123", null));
        assertFalse(PasswordUtil.verify("matkhau123", ""));
        assertFalse(PasswordUtil.verify("matkhau123", "khong-dung-dinh-dang"));
        assertFalse(PasswordUtil.verify("matkhau123", "pbkdf2$abc$xyz$123"));
        assertFalse(PasswordUtil.verify("matkhau123", "pbkdf2$120000$thieu-mot-phan"));
    }

    @Test
    @DisplayName("Mật khẩu tiếng Việt có dấu")
    void tiengViet() {
        String hash = PasswordUtil.hash("mậtkhẩuCóDấu123");
        assertTrue(PasswordUtil.verify("mậtkhẩuCóDấu123", hash));
        assertFalse(PasswordUtil.verify("matkhauCoDau123", hash));
    }
}
