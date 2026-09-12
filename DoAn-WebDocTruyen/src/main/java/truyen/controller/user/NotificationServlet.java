package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.NotificationDAO;
import truyen.model.Notification;
import truyen.model.User;

/** TRANG 18 — Thông báo. */
@WebServlet("/notification")
public class NotificationServlet extends HttpServlet {

    private NotificationDAO notificationDAO;

    /** Chỉ giữ 50 thông báo gần nhất. Cũ hơn nữa thì không ai đọc lại. */
    private static final int LIMIT = 50;

    @Override
    public void init() throws ServletException {
        notificationDAO = new NotificationDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User me = (User) request.getSession().getAttribute("currentUser");
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        try {
            // 1. lấy danh sách kèm trạng thái đọc/chưa đọc HIỆN TẠI
            List<Notification> list = notificationDAO.findByUser(me.getId(), LIMIT);
            request.setAttribute("notifications", list);

            // 2. rồi mới xoá chấm đỏ cho lần sau
            notificationDAO.markAllRead(me.getId());

        } catch (SQLException e) {
            log("NotificationServlet: lỗi truy vấn", e);
            request.setAttribute("message", "Không tải được thông báo.");
            request.setAttribute("notifications", java.util.Collections.emptyList());
        }

        request.setAttribute("pageTitle", "Thông báo");
        request.setAttribute("activeNav", "notification");
        request.setAttribute("contentPage", "/WEB-INF/views/user/notifications.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
