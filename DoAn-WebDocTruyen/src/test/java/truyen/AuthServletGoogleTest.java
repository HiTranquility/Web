package truyen;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.controller.common.AuthServlet;
import truyen.util.GoogleConfig;
import truyen.util.GoogleTokenVerifier;

/**
 * Kiểm thử đường đăng nhập AuthServlet với Google idToken (bug-001 & ISSUE-001 Phase 2).
 *
 * Kiểm tra khẳng định:
 * - Gửi idToken bịa đặt -> trả success:false, tuyệt đối KHÔNG cấp currentUser vào session.
 * - Gửi idToken rỗng -> trả success:false, không cấp phiên.
 */
@DisplayName("AuthServlet — Đăng nhập Google và ngăn chặn giả mạo (bug-001)")
class AuthServletGoogleTest {

    private AuthServlet authServlet;
    private Map<String, Object> sessionAttributes;
    private StringWriter responseOutput;

    @BeforeEach
    void setUp() throws Exception {
        authServlet = new AuthServlet();
        authServlet.init();
        authServlet.setGoogleTokenVerifier(new GoogleTokenVerifier(false)); // Chế độ kiểm tra nghiêm ngặt

        sessionAttributes = new HashMap<>();
        responseOutput = new StringWriter();
    }

    private HttpServletRequest createMockRequest(String method, Map<String, String> params) {
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
                    if ("invalidate".equals(name)) {
                        sessionAttributes.clear();
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
                    if ("getCharacterEncoding".equals(name)) return "UTF-8";
                    if ("setCharacterEncoding".equals(name)) return null;
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
                    if ("sendError".equals(name)) {
                        responseOutput.write("ERROR_" + args[0]);
                        return null;
                    }
                    return null;
                }
        );
    }

    @Test
    @DisplayName("Gửi idToken bịa đặt (bug-001) -> Bị từ chối, KHÔNG cấp currentUser vào session")
    void testFakeIdTokenRejected() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "firebase-google");
        params.put("idToken", "day-la-chuoi-vo-nghia-bi-dat-123456");
        params.put("email", "mocmien@gmail.com"); // Cố tình gửi kèm email của nạn nhân
        params.put("uid", "fake-uid-attacker");
        params.put("ajax", "1");

        HttpServletRequest req = createMockRequest("POST", params);
        HttpServletResponse resp = createMockResponse();

        authServlet.service(req, resp);

        String json = responseOutput.toString();
        assertTrue(json.contains("\"success\":false"), "Máy chủ phải trả về success:false khi token bịa");
        assertNull(sessionAttributes.get("currentUser"), "TUYỆT ĐỐI KHÔNG cấp currentUser vào session");
    }

    @Test
    @DisplayName("Gửi idToken rỗng -> Trả về lỗi, không cấp phiên")
    void testEmptyIdTokenRejected() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("action", "firebase-google");
        params.put("idToken", "");
        params.put("ajax", "1");

        HttpServletRequest req = createMockRequest("POST", params);
        HttpServletResponse resp = createMockResponse();

        authServlet.service(req, resp);

        String json = responseOutput.toString();
        assertTrue(json.contains("\"success\":false"));
        assertNull(sessionAttributes.get("currentUser"));
    }
}
