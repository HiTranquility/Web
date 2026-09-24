package truyen;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import truyen.model.User;
import truyen.model.UserIdentity;

/**
 * Kiểm tra UserIdentity và logic tài khoản không mật khẩu (Phase 1 ISSUE-001).
 *
 * Kiểm tra các hàm thuần:
 * - Khởi tạo và gán giá trị UserIdentity
 * - User.hasPassword() khi passwordHash null/rỗng hoặc có giá trị
 */
@DisplayName("UserIdentity & HasPassword — liên kết Google và tài khoản")
class UserIdentityTest {

    @Test
    @DisplayName("UserIdentity: khởi tạo đầy đủ tham số và kiểm tra getter/setter")
    void testUserIdentityConstructorAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        UserIdentity identity = new UserIdentity(10, "GOOGLE", "sub_google_123456789", "user@gmail.com");
        identity.setId(1);
        identity.setCreatedAt(now);

        assertEquals(1, identity.getId());
        assertEquals(10, identity.getUserId());
        assertEquals("GOOGLE", identity.getProvider());
        assertEquals("sub_google_123456789", identity.getProviderUid());
        assertEquals("user@gmail.com", identity.getEmail());
        assertEquals(now, identity.getCreatedAt());
        assertTrue(identity.toString().contains("sub_google_123456789"));
    }

    @Test
    @DisplayName("User.hasPassword(): trả false khi passwordHash là null (tài khoản chỉ dùng Google)")
    void testHasPasswordNull() {
        User u = new User();
        u.setPasswordHash(null);
        assertFalse(u.hasPassword(), "Tài khoản Google chưa đặt mật khẩu thì hasPassword() phải trả về false");
    }

    @Test
    @DisplayName("User.hasPassword(): trả false khi passwordHash là chuỗi rỗng hoặc khoảng trắng")
    void testHasPasswordEmpty() {
        User u = new User();
        u.setPasswordHash("");
        assertFalse(u.hasPassword());

        u.setPasswordHash("   ");
        assertFalse(u.hasPassword());
    }

    @Test
    @DisplayName("User.hasPassword(): trả true khi passwordHash có giá trị chuỗi băm")
    void testHasPasswordPresent() {
        User u = new User();
        u.setPasswordHash("pbkdf2$120000$testSalt$testHash");
        assertTrue(u.hasPassword(), "Tài khoản có chuỗi băm thì hasPassword() phải trả về true");
    }
}
