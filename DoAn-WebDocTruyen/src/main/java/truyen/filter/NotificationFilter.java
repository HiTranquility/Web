package truyen.filter;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import truyen.dao.NotificationDAO;
import truyen.dao.WalletDAO;
import truyen.model.User;

/**
 * Đếm số thông báo chưa đọc và số dư ví xu, đặt vào request cho thanh menu dùng.
 *
 * VÌ SAO PHẢI LÀ FILTER CHỨ KHÔNG PHẢI SERVLET
 *   Chấm đỏ và badge xu nằm ở parts/nav.jsp — mảnh dùng chung cho MỌI trang.
 *   Filter chạy TRƯỚC mọi servlet nên đặt một lần là xong.
 */
@WebFilter("/*")
public class NotificationFilter implements Filter {

    private NotificationDAO notificationDAO;
    private WalletDAO walletDAO;

    @Override
    public void init(javax.servlet.FilterConfig config) {
        notificationDAO = new NotificationDAO();
        walletDAO = new WalletDAO();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        /*
         * Bỏ qua tài nguyên tĩnh.
         */
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath() + "/assets")
                || path.startsWith(request.getContextPath() + "/uploads")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".ico") || path.endsWith(".png")) {
            chain.doFilter(req, res);
            return;
        }

        javax.servlet.http.HttpSession session = request.getSession(false);
        User me = session != null ? (User) session.getAttribute("currentUser") : null;
        if (me != null) {
            try {
                request.setAttribute("unreadCount",
                        notificationDAO.countUnread(me.getId()));
            } catch (SQLException e) {
                // Nuốt có chủ ý — xem ghi chú ở đầu lớp.
                request.getServletContext().log(
                        "NotificationFilter: không đếm được thông báo chưa đọc", e);
            }
            try {
                request.setAttribute("walletBalance",
                        walletDAO.getBalance(me.getId()));
            } catch (SQLException e) {
                request.getServletContext().log(
                        "NotificationFilter: không lấy được số dư ví xu", e);
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }
}
