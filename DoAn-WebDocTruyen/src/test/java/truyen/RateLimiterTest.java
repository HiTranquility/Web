package truyen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.util.RateLimiter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RateLimiter — Chặn dò mật khẩu và chống spam bình luận (ISSUE-003)")
class RateLimiterTest {

    @BeforeEach
    void setUp() {
        RateLimiter.clear();
    }

    @Test
    @DisplayName("Ca 1: Trạng thái ban đầu không bị chặn, đủ 5 lượt thử")
    void testInitialStateNotBlocked() {
        String ip = "192.168.1.10";
        String username = "mocmien";

        assertFalse(RateLimiter.isLoginBlocked(ip, username));
        assertEquals(RateLimiter.MAX_LOGIN_ATTEMPTS, RateLimiter.getRemainingAttempts(ip, username));
        assertEquals(0, RateLimiter.getLoginBlockedRemainingMinutes(ip, username));
    }

    @Test
    @DisplayName("Ca 2: Thử sai 5 lần liên tiếp -> Bị khóa đăng nhập 15 phút")
    void testLoginBruteForceLockout() {
        String ip = "192.168.1.20";
        String username = "haiduong";

        // Thử sai 4 lần đầu -> chưa bị khóa
        for (int i = 1; i <= 4; i++) {
            RateLimiter.recordLoginFailure(ip, username);
            assertFalse(RateLimiter.isLoginBlocked(ip, username), "Thử sai lần " + i + " chưa bị khóa");
            assertEquals(5 - i, RateLimiter.getRemainingAttempts(ip, username));
        }

        // Lần thứ 5 -> Khóa ngay lập tức
        RateLimiter.recordLoginFailure(ip, username);
        assertTrue(RateLimiter.isLoginBlocked(ip, username), "Thử sai lần 5 phải bị khóa ngay");
        assertEquals(0, RateLimiter.getRemainingAttempts(ip, username));
        assertTrue(RateLimiter.getLoginBlockedRemainingMinutes(ip, username) > 0);
    }

    @Test
    @DisplayName("Ca 3: Đăng nhập thành công -> Xóa lịch sử thất bại")
    void testResetLoginAttempts() {
        String ip = "192.168.1.30";
        String username = "kiemvu";

        // Thử sai 3 lần
        RateLimiter.recordLoginFailure(ip, username);
        RateLimiter.recordLoginFailure(ip, username);
        RateLimiter.recordLoginFailure(ip, username);
        assertEquals(2, RateLimiter.getRemainingAttempts(ip, username));

        // Người dùng nhớ ra mật khẩu đúng và đăng nhập thành công
        RateLimiter.resetLoginAttempts(ip, username);

        assertFalse(RateLimiter.isLoginBlocked(ip, username));
        assertEquals(5, RateLimiter.getRemainingAttempts(ip, username));
    }

    @Test
    @DisplayName("Ca 4: Khóa tài khoản A không làm ảnh hưởng tài khoản B")
    void testAccountIsolation() {
        String ip = "192.168.1.40";
        String userA = "victim";
        String userB = "innocent";

        for (int i = 0; i < 5; i++) {
            RateLimiter.recordLoginFailure(ip, userA);
        }

        assertTrue(RateLimiter.isLoginBlocked(ip, userA));
        assertFalse(RateLimiter.isLoginBlocked(ip, userB));
        assertEquals(5, RateLimiter.getRemainingAttempts(ip, userB));
    }

    @Test
    @DisplayName("Ca 5: Chống spam bình luận (Cooldown 20 giây)")
    void testCommentCooldown() {
        int userId = 101;

        // Chưa bình luận -> Không bị chặn
        assertFalse(RateLimiter.isCommentSpam(userId));
        assertEquals(0, RateLimiter.getCommentCooldownRemainingSeconds(userId));

        // Đăng 1 bình luận
        RateLimiter.recordComment(userId);

        // Bình luận ngay sau đó -> Bị phát hiện spam
        assertTrue(RateLimiter.isCommentSpam(userId));
        long remainingSec = RateLimiter.getCommentCooldownRemainingSeconds(userId);
        assertTrue(remainingSec > 0 && remainingSec <= 20, "Thời gian chờ còn lại phải từ 1 đến 20s");
    }

    @Test
    @DisplayName("Ca 6: Cooldown bình luận cô lập giữa các tài khoản khác nhau")
    void testCommentUserIsolation() {
        int userA = 201;
        int userB = 202;

        RateLimiter.recordComment(userA);

        assertTrue(RateLimiter.isCommentSpam(userA));
        assertFalse(RateLimiter.isCommentSpam(userB));
        assertEquals(0, RateLimiter.getCommentCooldownRemainingSeconds(userB));
    }
}
