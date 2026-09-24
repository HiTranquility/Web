package truyen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.controller.user.UserServlet;
import truyen.dao.IdentityDAO;
import truyen.dao.UserDAO;
import truyen.model.User;
import truyen.model.UserIdentity;
import truyen.util.GoogleConfig;
import truyen.util.GoogleTokenVerifier;

/**
 * Kiểm thử lớp bảo vệ tài khoản (Security Guard) khi gắn và gỡ tài khoản Google (ISSUE-001 Phase 3).
 *
 * Tiêu chí kiểm định:
 * 1. Chặn tuyệt đối việc gỡ liên kết Google khi tài khoản chưa có mật khẩu (ngăn ngừa lock-out).
 * 2. Cho phép gỡ liên kết Google bình thường khi tài khoản đã có mật khẩu.
 * 3. Thiết lập mật khẩu lần đầu (set-password) thành công và mở khóa cho phép gỡ Google.
 * 4. Chặn liên kết Google nếu tài khoản Google đó đã được gắn với tài khoản khác trong hệ thống.
 */
@DisplayName("UnlinkGuardTest — Bảo vệ tài khoản khi gắn và gỡ Google (Phase 3)")
class UnlinkGuardTest {

    private UserServlet userServlet;
    private Map<String, Object> sessionAttributes;
    private StringWriter responseOutput;
    private int responseStatusCode;
    private String redirectLocation;

    private Map<Integer, Boolean> userPasswords;
    private Map<String, UserIdentity> identitiesByProviderUid;
    private Map<String, UserIdentity> identitiesByUserAndProvider;

    @BeforeEach
    void setUp() throws Exception {
        userPasswords = new HashMap<>();
        identitiesByProviderUid = new HashMap<>();
        identitiesByUserAndProvider = new HashMap<>();

        Properties props = new Properties();
        props.setProperty("google.auth.enabled", "true");
        props.setProperty("google.client_id", "test-client-id.apps.googleusercontent.com");
        GoogleConfig.setOverrideConfig(props);

        userServlet = new UserServlet();
        userServlet.init();

        // Cung cấp Mock UserDAO không chạm DB
        UserDAO mockUserDAO = new UserDAO() {
            @Override
            public boolean hasPassword(int userId) {
                return Boolean.TRUE.equals(userPasswords.get(userId));
            }

            @Override
            public void updatePassword(int userId, String passwordHash) {
                userPasswords.put(userId, passwordHash != null && !passwordHash.trim().isEmpty());
            }

            @Override
            public User findById(int id) {
                User u = new User();
                u.setId(id);
                u.setUsername("user_" + id);
                u.setDisplayName("User " + id);
                u.setEmail("user" + id + "@example.com");
                if (Boolean.TRUE.equals(userPasswords.get(id))) {
                    u.setPasswordHash("$2a$10$hashedPasswordValue");
                } else {
                    u.setPasswordHash(null);
                }
                return u;
            }
        };

        // Cung cấp Mock IdentityDAO không chạm DB
        IdentityDAO mockIdentityDAO = new IdentityDAO() {
            @Override
            public UserIdentity findByUserAndProvider(int userId, String provider) {
                return identitiesByUserAndProvider.get(userId + ":" + provider);
            }

            @Override
            public UserIdentity findByProviderUid(String provider, String providerUid) {
                return identitiesByProviderUid.get(provider + ":" + providerUid);
            }

            @Override
            public void insert(UserIdentity identity) {
                identitiesByProviderUid.put(identity.getProvider() + ":" + identity.getProviderUid(), identity);
                identitiesByUserAndProvider.put(identity.getUserId() + ":" + identity.getProvider(), identity);
            }

            @Override
            public boolean deleteByUserAndProvider(int userId, String provider) {
                UserIdentity removed = identitiesByUserAndProvider.remove(userId + ":" + provider);
                if (removed != null) {
                    identitiesByProviderUid.remove(removed.getProvider() + ":" + removed.getProviderUid());
                    return true;
                }
                return false;
            }
        };

        // Cung cấp Mock GoogleTokenVerifier
        GoogleTokenVerifier mockVerifier = new GoogleTokenVerifier() {
            @Override
            public GoogleUser verify(String idToken) {
                if ("token-google-user10".equals(idToken)) {
                    return new GoogleUser("sub-google-10", "user10@gmail.com", "User 10", null);
                }
                if ("token-google-other".equals(idToken)) {
                    return new GoogleUser("sub-google-other", "other@gmail.com", "Other User", null);
                }
                return null;
            }
        };

        userServlet.setUserDAO(mockUserDAO);
        userServlet.setIdentityDAO(mockIdentityDAO);
        userServlet.setGoogleTokenVerifier(mockVerifier);

        sessionAttributes = new HashMap<>();
        responseOutput = new StringWriter();
        responseStatusCode = 200;
        redirectLocation = null;
    }

    @AfterEach
    void tearDown() {
        GoogleConfig.reset();
    }

    private HttpServletRequest createMockRequest(String method, Map<String, String> params, User currentUser) {
        if (currentUser != null) {
            sessionAttributes.put("currentUser", currentUser);
        }

        HttpSession mockSession = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getAttribute".equals(name)) {
                        return sessionAttributes.get((String) args[0]);
                    }
                    if ("setAttribute".equals(name)) {
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("removeAttribute".equals(name)) {
                        sessionAttributes.remove((String) args[0]);
                        return null;
                    }
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) return method;
                    if ("getParameter".equals(name)) {
                        return params.get((String) args[0]);
                    }
                    if ("getSession".equals(name)) return mockSession;
                    if ("getHeader".equals(name)) {
                        if ("X-Requested-With".equalsIgnoreCase((String) args[0])) return "XMLHttpRequest";
                        return null;
                    }
                    if ("getContextPath".equals(name)) return "";
                    if ("getCharacterEncoding".equals(name)) return "UTF-8";
                    if ("setCharacterEncoding".equals(name)) return null;
                    if ("setAttribute".equals(name)) return null;
                    return null;
                }
        );
    }

    private HttpServletResponse createMockResponse() {
        PrintWriter writer = new PrintWriter(responseOutput);
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getWriter".equals(name)) return writer;
                    if ("setContentType".equals(name)) return null;
                    if ("setStatus".equals(name)) {
                        responseStatusCode = (int) args[0];
                        return null;
                    }
                    if ("sendRedirect".equals(name)) {
                        redirectLocation = (String) args[0];
                        return null;
                    }
                    if ("sendError".equals(name)) {
                        responseStatusCode = (int) args[0];
                        responseOutput.write("ERROR_" + args[0]);
                        return null;
                    }
                    return null;
                }
        );
    }

    @Test
    @DisplayName("Ca 1: Chặn hủy liên kết Google khi tài khoản CHƯA có mật khẩu (Tránh mất tài khoản vĩnh viễn)")
    void testUnlinkBlockedWhenNoPassword() throws Exception {
        int userId = 10;
        User user = new User();
        user.setId(userId);
        user.setUsername("google_only_user");
        userPasswords.put(userId, false); // Chưa có mật khẩu

        UserIdentity identity = new UserIdentity(userId, "GOOGLE", "sub-google-10", "user10@gmail.com");
        identitiesByUserAndProvider.put(userId + ":GOOGLE", identity);
        identitiesByProviderUid.put("GOOGLE:sub-google-10", identity);

        Map<String, String> params = new HashMap<>();
        params.put("action", "unlink-google");
        params.put("ajax", "1");

        HttpServletRequest req = createMockRequest("POST", params, user);
        HttpServletResponse resp = createMockResponse();

        userServlet.service(req, resp);

        String json = responseOutput.toString();
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseStatusCode, "Phải trả về mã lỗi 400 Bad Request");
        assertTrue(json.contains("\"success\":false"), "Phải trả về success:false");
        assertTrue(json.contains("chưa tạo mật khẩu"), "Thông báo phải giải thích rõ cần tạo mật khẩu");

        // Đảm bảo trong CSDL liên kết vẫn còn nguyên vẹn, không bị xóa
        assertNotNull(identitiesByUserAndProvider.get(userId + ":GOOGLE"), "Liên kết Google KHÔNG được phép bị xóa");
    }

    @Test
    @DisplayName("Ca 2: Cho phép hủy liên kết Google khi tài khoản ĐÃ có mật khẩu")
    void testUnlinkAllowedWhenHasPassword() throws Exception {
        int userId = 10;
        User user = new User();
        user.setId(userId);
        user.setUsername("safe_user");
        userPasswords.put(userId, true); // Đã có mật khẩu

        UserIdentity identity = new UserIdentity(userId, "GOOGLE", "sub-google-10", "user10@gmail.com");
        identitiesByUserAndProvider.put(userId + ":GOOGLE", identity);
        identitiesByProviderUid.put("GOOGLE:sub-google-10", identity);

        Map<String, String> params = new HashMap<>();
        params.put("action", "unlink-google");
        params.put("ajax", "1");

        HttpServletRequest req = createMockRequest("POST", params, user);
        HttpServletResponse resp = createMockResponse();

        userServlet.service(req, resp);

        String json = responseOutput.toString();
        assertEquals(HttpServletResponse.SC_OK, responseStatusCode);
        assertTrue(json.contains("\"success\":true"), "Phải trả về success:true");

        // Đảm bảo liên kết đã được xóa
        assertNull(identitiesByUserAndProvider.get(userId + ":GOOGLE"), "Liên kết Google phải được xóa thành công");
    }

    @Test
    @DisplayName("Ca 3: Đặt mật khẩu lần đầu (set-password) thành công giúp mở khóa gỡ Google")
    void testSetPasswordUnlocksUnlink() throws Exception {
        int userId = 10;
        User user = new User();
        user.setId(userId);
        user.setUsername("google_first_user");
        userPasswords.put(userId, false); // Ban đầu chưa có mật khẩu

        UserIdentity identity = new UserIdentity(userId, "GOOGLE", "sub-google-10", "user10@gmail.com");
        identitiesByUserAndProvider.put(userId + ":GOOGLE", identity);
        identitiesByProviderUid.put("GOOGLE:sub-google-10", identity);

        // Bước 1: Người dùng đặt mật khẩu lần đầu
        Map<String, String> setPassParams = new HashMap<>();
        setPassParams.put("action", "set-password");
        setPassParams.put("newPassword", "matkhau123");
        setPassParams.put("confirmPassword", "matkhau123");

        HttpServletRequest req1 = createMockRequest("POST", setPassParams, user);
        HttpServletResponse resp1 = createMockResponse();

        userServlet.service(req1, resp1);

        // Khẳng định mật khẩu đã được cập nhật thành công
        assertTrue(Boolean.TRUE.equals(userPasswords.get(userId)), "Mật khẩu phải được tạo thành công");
        assertNotNull(sessionAttributes.get("flash"), "Phải có thông báo flash thành công");

        // Bước 2: Giờ đây người dùng đã có thể hủy liên kết Google an toàn
        Map<String, String> unlinkParams = new HashMap<>();
        unlinkParams.put("action", "unlink-google");
        unlinkParams.put("ajax", "1");

        HttpServletRequest req2 = createMockRequest("POST", unlinkParams, user);
        HttpServletResponse resp2 = createMockResponse();

        userServlet.service(req2, resp2);

        String json = responseOutput.toString();
        assertEquals(HttpServletResponse.SC_OK, responseStatusCode);
        assertTrue(json.contains("\"success\":true"), "Gỡ liên kết Google phải thành công sau khi đã có mật khẩu");
        assertNull(identitiesByUserAndProvider.get(userId + ":GOOGLE"), "Liên kết Google đã được giải phóng");
    }

    @Test
    @DisplayName("Ca 4: Chặn liên kết tài khoản Google nếu Google này đã bị tài khoản khác gắn")
    void testLinkGoogleRejectsIfLinkedToOtherAccount() throws Exception {
        int currentUserId = 10;
        int otherUserId = 99;

        User currentUser = new User();
        currentUser.setId(currentUserId);
        currentUser.setUsername("current_user");

        // Tài khoản Google "sub-google-other" đã thuộc về user 99
        UserIdentity otherIdentity = new UserIdentity(otherUserId, "GOOGLE", "sub-google-other", "other@gmail.com");
        identitiesByUserAndProvider.put(otherUserId + ":GOOGLE", otherIdentity);
        identitiesByProviderUid.put("GOOGLE:sub-google-other", otherIdentity);

        // User hiện tại (id=10) cố tình liên kết tài khoản Google của user 99
        Map<String, String> params = new HashMap<>();
        params.put("action", "link-google");
        params.put("idToken", "token-google-other");
        params.put("ajax", "1");

        HttpServletRequest req = createMockRequest("POST", params, currentUser);
        HttpServletResponse resp = createMockResponse();

        userServlet.service(req, resp);

        String json = responseOutput.toString();
        assertEquals(HttpServletResponse.SC_CONFLICT, responseStatusCode, "Phải trả về 409 Conflict");
        assertTrue(json.contains("\"success\":false"), "Phải trả về success:false");
        assertTrue(json.contains("đã được gắn với một người dùng khác"), "Phải báo rõ tài khoản Google đã liên kết với người khác");

        // Đảm bảo không ghi đè vào user hiện tại
        assertNull(identitiesByUserAndProvider.get(currentUserId + ":GOOGLE"), "Không được phép liên kết cho current user");
    }
}
