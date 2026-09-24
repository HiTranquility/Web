package truyen;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.FilterChain;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.filter.RecaptchaFilter;
import truyen.util.GoogleConfig;
import truyen.util.RecaptchaVerifier;

/**
 * Kiểm thử bộ lọc Google reCAPTCHA v3 (ISSUE-001 Phase 4).
 *
 * Tiêu chí kiểm định:
 * 1. Cho qua tất cả request khi tính năng reCAPTCHA chưa bật.
 * 2. Bỏ qua mọi request GET (chỉ kiểm tra POST).
 * 3. Bỏ qua các URL ngoài 3 cửa nhạy cảm (POST /story, POST /user...).
 * 4. Chặn POST /auth?action=register khi điểm bot < 0.5.
 * 5. Cho qua POST /auth?action=login khi điểm bot = 0.4 (vượt ngưỡng 0.3).
 * 6. Chặn POST /auth?action=login khi điểm bot < 0.3.
 * 7. Cho qua POST /comment?action=add khi điểm bot >= 0.5.
 * 8. Kiểm tra cơ chế FAIL-OPEN (CÓ CHỦ Ý) khi mạng gặp sự cố.
 */
@DisplayName("RecaptchaFilterTest — Chặn bot tại 3 cửa nhạy cảm (Phase 4)")
class RecaptchaFilterTest {

    private RecaptchaFilter filter;
    private Map<String, Object> sessionAttributes;
    private Map<String, Object> requestAttributes;
    private StringWriter responseOutput;
    private int responseStatusCode;
    private String forwardedPath;

    @BeforeEach
    void setUp() throws Exception {
        sessionAttributes = new HashMap<>();
        requestAttributes = new HashMap<>();
        responseOutput = new StringWriter();
        responseStatusCode = 200;
        forwardedPath = null;

        Properties props = new Properties();
        props.setProperty("google.recaptcha.enabled", "true");
        props.setProperty("google.recaptcha.site_key", "6Ld_test_site_key");
        props.setProperty("google.recaptcha.secret_key", "6Ld_test_secret_key");
        props.setProperty("google.recaptcha.threshold", "0.5");
        GoogleConfig.setOverrideConfig(props);

        filter = new RecaptchaFilter();
        filter.init(null);
    }

    @AfterEach
    void tearDown() {
        GoogleConfig.reset();
    }

    private HttpServletRequest createMockRequest(String method, String servletPath, Map<String, String> params, boolean isAjax) {
        HttpSession mockSession = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getAttribute".equals(name)) return sessionAttributes.get((String) args[0]);
                    if ("setAttribute".equals(name)) {
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    }
                    return null;
                }
        );

        RequestDispatcher mockDispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, m, args) -> {
                    if ("forward".equals(m.getName())) {
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
                    if ("getServletPath".equals(name)) return servletPath;
                    if ("getRequestURI".equals(name)) return servletPath;
                    if ("getContextPath".equals(name)) return "";
                    if ("getParameter".equals(name)) return params.get((String) args[0]);
                    if ("getSession".equals(name)) return mockSession;
                    if ("getAttribute".equals(name)) return requestAttributes.get((String) args[0]);
                    if ("setAttribute".equals(name)) {
                        requestAttributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("getHeader".equals(name)) {
                        if ("X-Requested-With".equalsIgnoreCase((String) args[0]) && isAjax) return "XMLHttpRequest";
                        return null;
                    }
                    if ("getRequestDispatcher".equals(name)) {
                        forwardedPath = (String) args[0];
                        return mockDispatcher;
                    }
                    if ("getRemoteAddr".equals(name)) return "127.0.0.1";
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
                        return null;
                    }
                    return null;
                }
        );
    }

    @Test
    @DisplayName("Ca 1: reCAPTCHA bị tắt -> Cho qua tất cả các request")
    void testPassWhenDisabled() throws Exception {
        Properties disabledProps = new Properties();
        disabledProps.setProperty("google.recaptcha.enabled", "false");
        GoogleConfig.setOverrideConfig(disabledProps);

        Map<String, String> params = new HashMap<>();
        params.put("action", "register");

        HttpServletRequest req = createMockRequest("POST", "/auth", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertTrue(chainCalled.get(), "Phải cho qua chain khi reCAPTCHA tắt");
    }

    @Test
    @DisplayName("Ca 2: Bỏ qua mọi request GET (chỉ kiểm tra POST)")
    void testPassForGetRequests() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "register");

        HttpServletRequest req = createMockRequest("GET", "/auth", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertTrue(chainCalled.get(), "Request GET phải được cho qua thẳng");
    }

    @Test
    @DisplayName("Ca 3: Bỏ qua các URL ngoài 3 cửa nhạy cảm (POST /story, POST /user...)")
    void testPassForOtherPostUrls() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "save");

        HttpServletRequest req = createMockRequest("POST", "/story", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertTrue(chainCalled.get(), "POST vào /story không được kiểm reCAPTCHA");
    }

    @Test
    @DisplayName("Ca 4: Chặn POST /auth?action=register khi điểm bot < 0.5")
    void testBlockRegisterLowScore() throws Exception {
        filter.setRecaptchaVerifier(new RecaptchaVerifier() {
            @Override
            public RecaptchaResult verify(String token, String expectedAction, String remoteIp) {
                // Giả lập bot điểm thấp 0.2
                return new RecaptchaResult(true, 0.2, "register", "localhost", false);
            }
        });

        Map<String, String> params = new HashMap<>();
        params.put("action", "register");
        params.put("g-recaptcha-token", "bot-token");

        HttpServletRequest req = createMockRequest("POST", "/auth", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertFalse(chainCalled.get(), "TUYỆT ĐỐI KHÔNG được cho qua khi điểm bot thấp");
        assertNotNull(requestAttributes.get("message"), "Phải có thông báo lỗi gửi ra giao diện");
        assertTrue(((String) requestAttributes.get("message")).contains("Không xác minh được bạn là người thật"));
    }

    @Test
    @DisplayName("Ca 5: Cho qua POST /auth?action=login khi điểm bot = 0.4 (Vượt ngưỡng 0.3)")
    void testAllowLoginScorePointFour() throws Exception {
        filter.setRecaptchaVerifier(new RecaptchaVerifier() {
            @Override
            public RecaptchaResult verify(String token, String expectedAction, String remoteIp) {
                // Điểm 0.4: dưới 0.5 của đăng ký, nhưng trên 0.3 của đăng nhập
                return new RecaptchaResult(true, 0.4, "login", "localhost", false);
            }
        });

        Map<String, String> params = new HashMap<>();
        params.put("action", "login");
        params.put("g-recaptcha-token", "human-login-token");

        HttpServletRequest req = createMockRequest("POST", "/auth", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertTrue(chainCalled.get(), "Đăng nhập với điểm 0.4 (>= 0.3) phải được cho qua");
    }

    @Test
    @DisplayName("Ca 6: Chặn POST /auth?action=login khi điểm bot < 0.3")
    void testBlockLoginVeryLowScore() throws Exception {
        filter.setRecaptchaVerifier(new RecaptchaVerifier() {
            @Override
            public RecaptchaResult verify(String token, String expectedAction, String remoteIp) {
                // Điểm 0.1 -> chắc chắn là bot dò mật khẩu
                return new RecaptchaResult(true, 0.1, "login", "localhost", false);
            }
        });

        Map<String, String> params = new HashMap<>();
        params.put("action", "login");
        params.put("g-recaptcha-token", "brute-force-bot-token");

        HttpServletRequest req = createMockRequest("POST", "/auth", params, true); // AJAX
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertFalse(chainCalled.get(), "Bot dò mật khẩu phải bị chặn");
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, responseStatusCode);
        assertTrue(responseOutput.toString().contains("\"success\":false"));
    }

    @Test
    @DisplayName("Ca 7: Cho qua POST /comment?action=add khi điểm bot >= 0.5")
    void testAllowValidComment() throws Exception {
        filter.setRecaptchaVerifier(new RecaptchaVerifier() {
            @Override
            public RecaptchaResult verify(String token, String expectedAction, String remoteIp) {
                return new RecaptchaResult(true, 0.9, "comment", "localhost", false);
            }
        });

        Map<String, String> params = new HashMap<>();
        params.put("action", "add");
        params.put("g-recaptcha-token", "good-comment-token");

        HttpServletRequest req = createMockRequest("POST", "/comment", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertTrue(chainCalled.get(), "Bình luận điểm 0.9 phải được cho qua");
    }

    @Test
    @DisplayName("Ca 8: Cơ chế FAIL-OPEN (CÓ CHỦ Ý) khi mạng gặp sự cố (timeout/IOException)")
    void testFailOpenOnNetworkError() throws Exception {
        filter.setRecaptchaVerifier(new RecaptchaVerifier() {
            @Override
            public RecaptchaResult verify(String token, String expectedAction, String remoteIp) {
                // Giả lập mạng lỗi / timeout 2s
                return new RecaptchaResult(true, 1.0, expectedAction, "fail-open", true);
            }
        });

        Map<String, String> params = new HashMap<>();
        params.put("action", "register");
        params.put("g-recaptcha-token", "any-token");

        HttpServletRequest req = createMockRequest("POST", "/auth", params, false);
        HttpServletResponse resp = createMockResponse();

        AtomicBoolean chainCalled = new AtomicBoolean(false);
        FilterChain chain = (r, s) -> chainCalled.set(true);

        filter.doFilter(req, resp, chain);

        assertTrue(chainCalled.get(), "Khi Google timeout hoặc mất mạng, PHẢI FAIL-OPEN cho người dùng tiếp tục thao tác");
    }
}
