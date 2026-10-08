package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import truyen.util.PasswordUtil;
import truyen.util.ServletHelper;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Luật kiểm tra dùng CHUNG cho mọi form: đăng ký, đổi mật khẩu, đặt lại mật
 * khẩu, sửa hồ sơ. Trước đây mỗi form tự viết một kiểu — đăng ký kiểm email
 * chặt, sửa hồ sơ chỉ kiểm "có @"; không form nào có trần độ dài mật khẩu.
 */
@DisplayName("Luật kiểm tra form dùng chung")
class ValidationTest {

    @Test
    @DisplayName("Mật khẩu: 6..128 ký tự; ngắn, rỗng, null, quá dài đều bị chặn")
    void passwordLength() {
        assertNull(PasswordUtil.lengthError("123456"));
        assertNull(PasswordUtil.lengthError("a".repeat(PasswordUtil.MAX_LENGTH)));
        assertNotNull(PasswordUtil.lengthError("12345"));
        assertNotNull(PasswordUtil.lengthError(""));
        assertNotNull(PasswordUtil.lengthError(null));
        assertNotNull(PasswordUtil.lengthError("a".repeat(PasswordUtil.MAX_LENGTH + 1)));
    }

    @Test
    @DisplayName("Email: đúng dạng và không vượt cột VARCHAR(150)")
    void email() {
        assertTrue(ServletHelper.isValidEmail("mocmien@gmail.com"));
        assertFalse(ServletHelper.isValidEmail("mocmien"));
        assertFalse(ServletHelper.isValidEmail("a@b"));            // thiếu tên miền cấp cao
        assertFalse(ServletHelper.isValidEmail("a b@c.vn"));       // có khoảng trắng
        assertFalse(ServletHelper.isValidEmail(null));
        String longLocal = "a".repeat(ServletHelper.EMAIL_MAX) + "@x.vn";
        assertFalse(ServletHelper.isValidEmail(longLocal), "dài hơn cột CSDL phải bị chặn ở form");
    }
}
