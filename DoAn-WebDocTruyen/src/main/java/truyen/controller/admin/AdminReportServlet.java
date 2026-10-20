package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.AuditDAO;
import truyen.dao.CommentDAO;
import truyen.dao.ReportDAO;
import truyen.dao.StoryDAO;
import truyen.model.Story;
import truyen.model.User;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.parseIntOr;

/** TRANG 30 — Xử lý báo cáo vi phạm. */
@WebServlet("/admin/report")
public class AdminReportServlet extends HttpServlet {

    private ReportDAO reportDAO;

    @Override
    public void init() throws ServletException {
        reportDAO = new ReportDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action   = request.getParameter("action");
        String status   = request.getParameter("status");
        String category = request.getParameter("category");   // ISSUE-025

        if ("resolve".equals(action) || "dismiss".equals(action)
                || "resolve_hide".equals(action) || "resolve_delete".equals(action)
                || "resolve_restore".equals(action)) {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
        }

        try {
            int reportId = parseIntOr(request.getParameter("id"), 0);
            int targetId = parseIntOr(request.getParameter("targetId"), 0);

            if ("resolve".equals(action)) {
                reportDAO.updateStatus(reportId, "RESOLVED");
            } else if ("dismiss".equals(action)) {
                reportDAO.updateStatus(reportId, "DISMISSED");
            } else if ("resolve_hide".equals(action)) {
                reportDAO.updateStatus(reportId, "RESOLVED");
                if (targetId > 0) {
                    new CommentDAO().hide(targetId);
                    User me = currentUser(request);
                    new AuditDAO().log(me != null ? me.getId() : 0, "HIDE_COMMENT", "COMMENT", targetId,
                            "Bình luận #" + targetId, "Ẩn bình luận theo xử lý báo cáo vi phạm #" + reportId);
                }
            } else if ("resolve_restore".equals(action)) {
                reportDAO.updateStatus(reportId, "RESOLVED");
                if (targetId > 0) {
                    StoryDAO stDAO = new StoryDAO();
                    Story story = stDAO.findById(targetId);
                    String storyTitle = story != null ? story.getTitle() : ("Truyện #" + targetId);
                    stDAO.updateStatus(targetId, "PUBLISHED");
                    User me = currentUser(request);
                    new AuditDAO().log(me != null ? me.getId() : 0, "RESTORE_STORY", "STORY", targetId,
                            storyTitle, "Chấp nhận khiếu nại và khôi phục truyện theo báo cáo #" + reportId);

                    if (story != null && story.getAuthorId() > 0) {
                        new truyen.dao.NotificationDAO().sendNotification(
                            story.getAuthorId(),
                            targetId,
                            null,
                            "MODERATION",
                            "Tin vui! Đơn khiếu nại cho truyện \"" + storyTitle + "\" đã được Quản trị viên chấp thuận. Tác phẩm của bạn đã được khôi phục trạng thái Công khai."
                        );
                    }
                }
            } else if ("resolve_delete".equals(action)) {
                reportDAO.updateStatus(reportId, "RESOLVED");
                if (targetId > 0) {
                    StoryDAO stDAO = new StoryDAO();
                    Story story = stDAO.findById(targetId);
                    String storyTitle = story != null ? story.getTitle() : ("Truyện #" + targetId);
                    stDAO.updateStatus(targetId, "DELETED");
                    User me = currentUser(request);
                    new AuditDAO().log(me != null ? me.getId() : 0, "DELETE_STORY", "STORY", targetId,
                            storyTitle, "Gỡ truyện theo xử lý báo cáo vi phạm #" + reportId);

                    // Thông báo kiểm duyệt tới tác giả
                    if (story != null && story.getAuthorId() > 0) {
                        new truyen.dao.NotificationDAO().sendNotification(
                            story.getAuthorId(),
                            targetId,
                            null,
                            "MODERATION",
                            "Truyện \"" + storyTitle + "\" của bạn đã bị Quản trị viên gỡ bỏ do vi phạm tiêu chuẩn (Báo cáo #" + reportId + "). Bạn có quyền gửi thư khiếu nại nếu cho rằng đây là sự nhầm lẫn."
                        );
                    }
                }
            }

            // Post-Redirect-Get — F5 không gửi lại thao tác.
            // Giữ CẢ HAI bộ lọc, không thì mỗi lần xử lý một báo cáo là admin
            // bị ném về danh sách đầy đủ và phải lọc lại từ đầu.
            if ("POST".equalsIgnoreCase(request.getMethod()) && action != null) {
                StringBuilder qs = new StringBuilder();
                if (status != null && !status.isEmpty()) {
                    qs.append(qs.length() == 0 ? "?" : "&").append("status=").append(status);
                }
                if (category != null && !category.isEmpty()) {
                    qs.append(qs.length() == 0 ? "?" : "&").append("category=").append(category);
                }
                response.sendRedirect(request.getContextPath() + "/admin/report" + qs);
                return;
            }

            request.setAttribute("reports", reportDAO.findAll(status, category));
            request.setAttribute("status", status);
            request.setAttribute("category", category);
            request.setAttribute("pending", reportDAO.countPending());

        } catch (SQLException e) {
            log("AdminReportServlet: lỗi truy vấn, action=" + action, e);
            request.setAttribute("message", "Không tải được danh sách báo cáo.");
            request.setAttribute("reports", java.util.Collections.emptyList());
        }

        request.setAttribute("pageTitle", "Quản trị — Báo cáo");
        request.setAttribute("activeNav", "admin");
        request.setAttribute("adminSection", "report");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/reports.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/admin.jsp")
                .forward(request, response);
    }
}
