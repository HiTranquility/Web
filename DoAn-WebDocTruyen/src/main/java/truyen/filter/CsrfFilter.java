package truyen.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.util.CsrfUtil;

/**
 * Chặn CSRF: mọi POST phải mang token của phiên.
 *
 * KHAI TRONG web.xml, KHÔNG DÙNG @WebFilter
 *   Thứ tự chạy giữa các filter khai bằng annotation là KHÔNG XÁC ĐỊNH, mà
 *   filter này BẮT BUỘC phải chạy sau EncodingFilter: nó gọi getParameter()
 *   để đọc token, và một khi tham số đã bị đọc thì setCharacterEncoding()
 *   gọi sau đó không còn tác dụng — đúng cái bẫy đã làm mất dấu tiếng Việt
 *   khi gửi form. Thứ tự các <filter-mapping> trong web.xml thì được bảo đảm.
 *
 * CHỈ KIỂM POST
 *   GET theo quy ước là chỉ ĐỌC, không đổi gì, nên không có gì để giả mạo.
 *   Quy ước đó chỉ đúng nếu code giữ đúng nó — vì vậy trong dự án này mọi
 *   hành động ghi đều là POST (kể cả xoá lịch sử, vốn dễ tiện tay làm bằng
 *   một link GET).
 */
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        /*
         * Tài nguyên tĩnh -> đi thẳng, không đụng gì tới phiên.
         *
         * Filter map vào /* nên nó chạy cho CẢ file CSS, JS, ảnh. Mà một trang
         * kéo theo cả chục file như vậy. Không chặn ở đây thì mỗi con bot quét
         * một file .css cũng được cấp một phiên nằm trong bộ nhớ server —
         * đúng cái bẫy getSession(true) mà AuthFilter đã tránh.
         *
         * Không có file tĩnh nào là POST, nên bỏ qua ở đây không hở gì.
         */
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath() + "/assets")
                || path.startsWith(request.getContextPath() + "/uploads")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".ico") || path.endsWith(".png")) {
            chain.doFilter(req, res);
            return;
        }

        /*
         * Đặt token vào request để JSP in ra ô ẩn: ${csrfToken}.
         *
         * Làm ở đây thay vì bắt từng servlet tự đặt — 20 servlet mà quên một
         * cái là các form của trang đó gửi lên thiếu token và bị chính filter
         * này chặn. Một chỗ đặt thì không có gì để quên.
         *
         * getSession(true) tạo phiên nếu chưa có: khách chưa đăng nhập vẫn có
         * form cần token (đăng nhập, đăng ký, quên mật khẩu).
         */
        request.setAttribute("csrfToken", CsrfUtil.token(request.getSession(true)));

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(req, res);
            return;
        }

        if (CsrfUtil.isValid(request)) {
            chain.doFilter(req, res);
            return;
        }

        /*
         * 403 chứ không phải chuyển hướng về trang đăng nhập.
         *
         * Token sai KHÔNG có nghĩa là hết phiên — nghĩa là request này không
         * đến từ giao diện của web. Đá về trang đăng nhập sẽ khiến người dùng
         * tưởng mình bị đăng xuất và cứ đăng nhập lại vô ích.
         *
         * sendError chứ không setStatus: chỉ sendError mới kích hoạt
         * <error-page> trong web.xml để hiện trang 403 tự làm.
         */
        request.getServletContext().log("CSRF: tu choi POST " + path
                + " (Referer: " + request.getHeader("Referer") + ")");

        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
