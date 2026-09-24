package truyen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.controller.common.ErrorServlet;

@DisplayName("ErrorServlet — Xử lý lỗi 403, 404 & 500 (ISSUE-016 · bug-002)")
class ErrorServletTest {

    /** Gọi thẳng processError() — nó là private nên phải đi qua reflection. */
    private static void run(Map<String, Object> attrs, Map<String, String> params,
                            TestRequest req, TestResponse resp) throws Exception {
        Method m = ErrorServlet.class.getDeclaredMethod(
                "processError", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(new ErrorServlet(), req, resp);
    }

    @Test
    @DisplayName("Ca 1: Mã lỗi 404 nạp đúng tiêu đề và đường dẫn view 404")
    void testError404() throws Exception {
        ErrorServlet servlet = new ErrorServlet();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("javax.servlet.error.status_code", 404);

        TestRequest req = new TestRequest(attributes, null);
        TestResponse resp = new TestResponse();

        Method m = ErrorServlet.class.getDeclaredMethod("processError", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(servlet, req, resp);

        assertEquals(404, attributes.get("errorCode"));
        assertEquals("404 — Không tìm thấy trang", attributes.get("pageTitle"));
        assertEquals("/WEB-INF/views/error/404.jsp", attributes.get("contentPage"));
        assertEquals("/WEB-INF/views/layout/main.jsp", req.forwardedPath);
    }

    @Test
    @DisplayName("Ca 2: Mã lỗi 500 nạp đúng tiêu đề và đường dẫn view 500")
    void testError500() throws Exception {
        ErrorServlet servlet = new ErrorServlet();
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("javax.servlet.error.status_code", 500);

        TestRequest req = new TestRequest(attributes, null);
        TestResponse resp = new TestResponse();

        Method m = ErrorServlet.class.getDeclaredMethod("processError", HttpServletRequest.class, HttpServletResponse.class);
        m.setAccessible(true);
        m.invoke(servlet, req, resp);

        assertEquals(500, attributes.get("errorCode"));
        assertEquals("500 — Sự cố máy chủ", attributes.get("pageTitle"));
        assertEquals("/WEB-INF/views/error/500.jsp", attributes.get("contentPage"));
        assertEquals("/WEB-INF/views/layout/main.jsp", req.forwardedPath);
    }

    @Test
    @DisplayName("Ca 3: Mã lỗi 403 nạp đúng trang 403 — bug-002 gộp 403 về một cửa")
    void testError403() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("javax.servlet.error.status_code", 403);

        TestRequest req = new TestRequest(attributes, null);
        TestResponse resp = new TestResponse();
        run(attributes, null, req, resp);

        assertEquals(403, attributes.get("errorCode"));
        assertEquals("403 — Không đủ quyền truy cập", attributes.get("pageTitle"));
        assertEquals("/WEB-INF/views/error/403.jsp", attributes.get("contentPage"));
        assertEquals("/WEB-INF/views/layout/main.jsp", req.forwardedPath);
    }

    @Test
    @DisplayName("Ca 4: Trả ĐÚNG mã HTTP, không chỉ đúng giao diện — chống soft 404")
    void testStatusCodeIsSent() throws Exception {
        /*
         * Đường "/error?code=404" gõ tay không đi qua <error-page>, nên Tomcat
         * không đặt hộ mã trạng thái. Trước bug-002 nó trả 200 OK kèm một trang
         * viết "404" — Google đọc header chứ không đọc chữ, nên nó sẽ lập chỉ
         * mục cho trang lỗi. Test này khoá lại hành vi đó.
         */
        Map<String, String> params = new HashMap<>();
        params.put("code", "404");

        Map<String, Object> attributes = new HashMap<>();
        TestRequest req = new TestRequest(attributes, params);
        TestResponse resp = new TestResponse();
        run(attributes, params, req, resp);

        assertEquals(404, resp.sentStatus, "Phải trả 404 ở header, không phải 200");
        assertEquals("/WEB-INF/views/error/404.jsp", attributes.get("contentPage"));
    }

    @Test
    @DisplayName("Ca 5: Không có mã lỗi nào -> mặc định 404, không vỡ trang")
    void testMissingCodeFallsBackTo404() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        TestRequest req = new TestRequest(attributes, null);
        TestResponse resp = new TestResponse();
        run(attributes, null, req, resp);

        assertEquals(404, attributes.get("errorCode"));
        assertEquals(404, resp.sentStatus);
    }

    @Test
    @DisplayName("Ca 6: Mã lỗi rác trong query -> bỏ qua, về 404 thay vì ném lỗi")
    void testGarbageCodeIsIgnored() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("code", "khong-phai-so");

        Map<String, Object> attributes = new HashMap<>();
        TestRequest req = new TestRequest(attributes, params);
        TestResponse resp = new TestResponse();
        run(attributes, params, req, resp);

        assertEquals(404, attributes.get("errorCode"));
        assertEquals("/WEB-INF/views/error/404.jsp", attributes.get("contentPage"));
    }

    // Dummy test classes implementing essential Servlet API subset via reflection or dynamic proxy
    static class TestRequest extends javax.servlet.http.HttpServletRequestWrapper {
        private final Map<String, Object> attrs;
        private final Map<String, String> params;
        String forwardedPath;

        public TestRequest(Map<String, Object> attrs, Map<String, String> params) {
            super(new DummyBaseRequest());
            this.attrs = attrs != null ? attrs : new HashMap<>();
            this.params = params != null ? params : new HashMap<>();
        }

        @Override public Object getAttribute(String name) { return attrs.get(name); }
        @Override public void setAttribute(String name, Object o) { attrs.put(name, o); }
        @Override public String getParameter(String name) { return params.get(name); }
        @Override public RequestDispatcher getRequestDispatcher(String path) {
            this.forwardedPath = path;
            return new RequestDispatcher() {
                @Override public void forward(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) {}
                @Override public void include(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response) {}
            };
        }
    }

    static class DummyBaseRequest implements HttpServletRequest {
        public Object getAttribute(String s) { return null; }
        public java.util.Enumeration<String> getAttributeNames() { return null; }
        public String getCharacterEncoding() { return null; }
        public void setCharacterEncoding(String s) {}
        public int getContentLength() { return 0; }
        public long getContentLengthLong() { return 0; }
        public String getContentType() { return null; }
        public javax.servlet.ServletInputStream getInputStream() { return null; }
        public String getParameter(String s) { return null; }
        public java.util.Enumeration<String> getParameterNames() { return null; }
        public String[] getParameterValues(String s) { return null; }
        public Map<String, String[]> getParameterMap() { return null; }
        public String getProtocol() { return null; }
        public String getScheme() { return null; }
        public String getServerName() { return null; }
        public int getServerPort() { return 0; }
        public java.io.BufferedReader getReader() { return null; }
        public String getRemoteAddr() { return null; }
        public String getRemoteHost() { return null; }
        public void setAttribute(String s, Object o) {}
        public void removeAttribute(String s) {}
        public java.util.Locale getLocale() { return null; }
        public java.util.Enumeration<java.util.Locale> getLocales() { return null; }
        public boolean isSecure() { return false; }
        public RequestDispatcher getRequestDispatcher(String s) { return null; }
        public String getRealPath(String s) { return null; }
        public int getRemotePort() { return 0; }
        public String getLocalName() { return null; }
        public String getLocalAddr() { return null; }
        public int getLocalPort() { return 0; }
        public javax.servlet.ServletContext getServletContext() { return null; }
        public javax.servlet.AsyncContext startAsync() { return null; }
        public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) { return null; }
        public boolean isAsyncStarted() { return false; }
        public boolean isAsyncSupported() { return false; }
        public javax.servlet.AsyncContext getAsyncContext() { return null; }
        public javax.servlet.DispatcherType getDispatcherType() { return null; }
        public String getAuthType() { return null; }
        public javax.servlet.http.Cookie[] getCookies() { return null; }
        public long getDateHeader(String s) { return 0; }
        public String getHeader(String s) { return null; }
        public java.util.Enumeration<String> getHeaders(String s) { return null; }
        public java.util.Enumeration<String> getHeaderNames() { return null; }
        public int getIntHeader(String s) { return 0; }
        public String getMethod() { return "GET"; }
        public String getPathInfo() { return null; }
        public String getPathTranslated() { return null; }
        public String getContextPath() { return ""; }
        public String getQueryString() { return null; }
        public String getRemoteUser() { return null; }
        public boolean isUserInRole(String s) { return false; }
        public java.security.Principal getUserPrincipal() { return null; }
        public String getRequestedSessionId() { return null; }
        public String getRequestURI() { return "/error"; }
        public StringBuffer getRequestURL() { return new StringBuffer("http://localhost:8080/error"); }
        public String getServletPath() { return "/error"; }
        public javax.servlet.http.HttpSession getSession(boolean b) { return null; }
        public javax.servlet.http.HttpSession getSession() { return null; }
        public String changeSessionId() { return null; }
        public boolean isRequestedSessionIdValid() { return false; }
        public boolean isRequestedSessionIdFromCookie() { return false; }
        public boolean isRequestedSessionIdFromURL() { return false; }
        public boolean isRequestedSessionIdFromUrl() { return false; }
        public boolean authenticate(HttpServletResponse httpServletResponse) { return false; }
        public void login(String s, String s1) {}
        public void logout() {}
        public java.util.Collection<javax.servlet.http.Part> getParts() { return null; }
        public javax.servlet.http.Part getPart(String s) { return null; }
        public <T extends javax.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> aClass) { return null; }
    }

    static class TestResponse extends javax.servlet.http.HttpServletResponseWrapper {
        /** Mã trạng thái ErrorServlet đã đặt. 0 = chưa đặt gì (bug-002). */
        int sentStatus;

        public TestResponse() { super(new DummyBaseResponse()); }

        @Override public void setStatus(int sc) { this.sentStatus = sc; }
        @Override public int getStatus() { return sentStatus; }
    }

    static class DummyBaseResponse implements HttpServletResponse {
        public void addCookie(javax.servlet.http.Cookie cookie) {}
        public boolean containsHeader(String s) { return false; }
        public String encodeURL(String s) { return s; }
        public String encodeRedirectURL(String s) { return s; }
        public String encodeUrl(String s) { return s; }
        public String encodeRedirectUrl(String s) { return s; }
        public void sendError(int i, String s) {}
        public void sendError(int i) {}
        public void sendRedirect(String s) {}
        public void setDateHeader(String s, long l) {}
        public void addDateHeader(String s, long l) {}
        public void setHeader(String s, String s1) {}
        public void addHeader(String s, String s1) {}
        public void setIntHeader(String s, int i) {}
        public void addIntHeader(String s, int i) {}
        public void setStatus(int i) {}
        public void setStatus(int i, String s) {}
        public int getStatus() { return 200; }
        public String getHeader(String s) { return null; }
        public java.util.Collection<String> getHeaders(String s) { return null; }
        public java.util.Collection<String> getHeaderNames() { return null; }
        public String getCharacterEncoding() { return "UTF-8"; }
        public String getContentType() { return null; }
        public javax.servlet.ServletOutputStream getOutputStream() { return null; }
        public java.io.PrintWriter getWriter() { return null; }
        public void setCharacterEncoding(String s) {}
        public void setContentLength(int i) {}
        public void setContentLengthLong(long l) {}
        public void setContentType(String s) {}
        public void setBufferSize(int i) {}
        public int getBufferSize() { return 0; }
        public void flushBuffer() {}
        public void resetBuffer() {}
        public boolean isCommitted() { return false; }
        public void reset() {}
        public void setLocale(java.util.Locale locale) {}
        public java.util.Locale getLocale() { return null; }
    }
}
