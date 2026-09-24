package truyen.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.util.GoogleConfig;
import truyen.util.RecaptchaVerifier;

/**
 * Bộ lọc Google reCAPTCHA v3 chặn bot ở 3 cửa nhạy cảm (ISSUE-001 Phase 4).
 *
 * Chỉ kiểm tra POST đối với 3 cửa:
 *   1. POST /auth?action=register  (ngưỡng 0.5)
 *   2. POST /auth?action=login     (ngưỡng 0.3)
 *   3. POST /comment?action=add    (ngưỡng 0.5)
 *
 * Các URL và request khác đều được cho qua trực tiếp mà không tốn tài nguyên gọi Google.
 *
 * THỨ TỰ TRONG web.xml:
 *   Phải đặt SAU CsrfFilter: request thiếu token CSRF là rác, loại trước bằng bộ nhớ,
 *   tránh lãng phí một lượt gọi mạng xác thực reCAPTCHA cho request rác.
 */
public class RecaptchaFilter implements Filter {

    private RecaptchaVerifier recaptchaVerifier;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.recaptchaVerifier = new RecaptchaVerifier();
    }

    public void setRecaptchaVerifier(RecaptchaVerifier verifier) {
        this.recaptchaVerifier = verifier;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // Bỏ qua tài nguyên tĩnh
        String path = request.getServletPath();
        if (path == null || path.isEmpty()) {
            path = request.getRequestURI();
            if (request.getContextPath() != null && path.startsWith(request.getContextPath())) {
                path = path.substring(request.getContextPath().length());
            }
        }

        if (path.startsWith("/assets") || path.startsWith("/uploads")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".ico") || path.endsWith(".png")) {
            chain.doFilter(req, res);
            return;
        }

        // Truyền trạng thái và siteKey vào request để JSP tải thẻ script tương ứng
        boolean recaptchaEnabled = GoogleConfig.isRecaptchaEnabled();
        if (recaptchaEnabled) {
            request.setAttribute("recaptchaEnabled", true);
            request.setAttribute("recaptchaSiteKey", GoogleConfig.getRecaptchaSiteKey());
        }

        // reCAPTCHA chỉ kiểm tra các hành động GHI nhạy cảm (POST)
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        // Nếu tính năng reCAPTCHA chưa được bật trên hệ thống -> cho qua an toàn
        if (!recaptchaEnabled) {
            chain.doFilter(req, res);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) action = "";

        double threshold = -1.0;
        String expectedAction = null;

        if ("/auth".equals(path)) {
            if ("register".equalsIgnoreCase(action)) {
                threshold = 0.5;
                expectedAction = "register";
            } else if ("login".equalsIgnoreCase(action)) {
                threshold = 0.3; // Ngưỡng thấp hơn vì người thật hay gõ nhầm mật khẩu
                expectedAction = "login";
            }
        } else if ("/comment".equals(path)) {
            if ("add".equalsIgnoreCase(action)) {
                threshold = 0.5;
                expectedAction = "comment";
            }
        }

        // Không thuộc 3 cửa được bảo vệ -> Cho qua thẳng
        if (expectedAction == null) {
            chain.doFilter(req, res);
            return;
        }

        // Đọc token do client gửi lên
        String token = request.getParameter("g-recaptcha-token");
        String clientIp = getClientIp(request);

        RecaptchaVerifier.RecaptchaResult result = recaptchaVerifier.verify(token, expectedAction, clientIp);

        // Chặn nếu không đạt ngưỡng điểm quy định
        if (!result.isSuccess() || result.getScore() < threshold) {
            boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                    || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"))
                    || "1".equals(request.getParameter("ajax"));

            if (isAjax) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Không xác minh được bạn là người thật, vui lòng thử lại sau.\"}");
                return;
            } else {
                if ("/auth".equals(path)) {
                    request.setAttribute("message", "Không xác minh được bạn là người thật. Vui lòng tải lại trang và thử lại.");
                    request.setAttribute("username", request.getParameter("username"));
                    if ("register".equalsIgnoreCase(action)) {
                        request.setAttribute("email", request.getParameter("email"));
                        request.setAttribute("contentPage", "/WEB-INF/views/auth/register.jsp");
                    } else {
                        request.setAttribute("contentPage", "/WEB-INF/views/auth/login.jsp");
                    }
                    request.getRequestDispatcher("/WEB-INF/views/layout/auth.jsp").forward(request, response);
                    return;
                } else {
                    request.getSession().setAttribute("flashError", "Không xác minh được người thật khi gửi bình luận.");
                    String referer = request.getHeader("Referer");
                    response.sendRedirect(referer != null && !referer.isEmpty() ? referer : (request.getContextPath() + "/"));
                    return;
                }
            }
        }

        // Đạt ngưỡng điểm -> Cho phép servlet tiếp tục xử lý
        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            int comma = xForwardedFor.indexOf(',');
            return comma > 0 ? xForwardedFor.substring(0, comma).trim() : xForwardedFor.trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void destroy() {
    }
}
