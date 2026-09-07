package truyen.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.model.User;

/** CASE 10 — Chặn mọi người không phải admin ở khu quản trị. */
@WebFilter("/admin/*")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        // getSession(false): chỉ HỎI, không tạo phiên mới cho mỗi con bot ghé qua
        HttpSession session = request.getSession(false);
        User me = session == null ? null : (User) session.getAttribute("currentUser");

        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        if (!me.isAdmin()) {
            /*
             * sendError chứ KHÔNG phải setStatus.
             * Chỉ sendError mới kích hoạt <error-page> trong web.xml để hiện
             * trang lỗi tự làm. setStatus chỉ đặt mã, trang vẫn trắng.
             */
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        chain.doFilter(req, res);
    }
}
