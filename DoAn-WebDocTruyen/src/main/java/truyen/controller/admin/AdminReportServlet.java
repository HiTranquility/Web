package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.ReportDAO;

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
        String action = request.getParameter("action");
        String status = request.getParameter("status");

        if ("resolve".equals(action) || "dismiss".equals(action)
                || "resolve_hide".equals(action) || "resolve_delete".equals(action)) {
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
                if (targetId > 0) new truyen.dao.CommentDAO().hide(targetId);
            } else if ("resolve_delete".equals(action)) {
                reportDAO.updateStatus(reportId, "RESOLVED");
                if (targetId > 0) new truyen.dao.StoryDAO().updateStatus(targetId, "DELETED");
            }

            // Post-Redirect-Get — F5 không gửi lại thao tác
            if ("POST".equalsIgnoreCase(request.getMethod()) && action != null) {
                String qs = status != null && !status.isEmpty() ? "?status=" + status : "";
                response.sendRedirect(request.getContextPath() + "/admin/report" + qs);
                return;
            }

            request.setAttribute("reports", reportDAO.findAll(status));
            request.setAttribute("status", status);
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

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
