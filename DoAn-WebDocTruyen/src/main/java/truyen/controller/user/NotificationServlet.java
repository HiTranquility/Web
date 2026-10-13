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

        String action = request.getParameter("action");
        if ("quick".equals(action)) {
            try {
                List<Notification> list = notificationDAO.findByUser(me.getId(), 5);
                int unread = notificationDAO.countUnread(me.getId());
                StringBuilder json = new StringBuilder();
                json.append("{")
                    .append("\"unread\":").append(unread).append(",")
                    .append("\"notifications\":[");
                for (int i = 0; i < list.size(); i++) {
                    Notification n = list.get(i);
                    if (i > 0) json.append(",");
                    String timeStr = "";
                    if (n.getCreatedAt() != null) {
                        timeStr = n.getCreatedAt().getDayOfMonth() + "/" + n.getCreatedAt().getMonthValue() + " "
                                + String.format("%02d:%02d", n.getCreatedAt().getHour(), n.getCreatedAt().getMinute());
                    }
                    json.append("{")
                        .append("\"id\":").append(n.getId()).append(",")
                        .append("\"icon\":").append(truyen.util.JsonUtil.jsonEscape(n.getIcon())).append(",")
                        .append("\"message\":").append(truyen.util.JsonUtil.jsonEscape(n.getMessage())).append(",")
                        .append("\"isRead\":").append(n.isRead()).append(",")
                        .append("\"storyId\":").append(n.getStoryId() != null ? n.getStoryId() : 0).append(",")
                        .append("\"chapterId\":").append(n.getChapterId() != null ? n.getChapterId() : 0).append(",")
                        .append("\"time\":").append(truyen.util.JsonUtil.jsonEscape(timeStr))
                        .append("}");
                }
                json.append("]}");
                truyen.util.JsonUtil.sendJson(response, HttpServletResponse.SC_OK, json.toString());
                return;
            } catch (SQLException e) {
                truyen.util.JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi nạp thông báo");
                return;
            }
        }

        if ("mark_read".equals(action)) {
            try {
                notificationDAO.markAllRead(me.getId());
                truyen.util.JsonUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"success\":true}");
                return;
            } catch (SQLException e) {
                truyen.util.JsonUtil.sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi CSDL");
                return;
            }
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
