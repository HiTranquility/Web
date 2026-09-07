package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.ReportDAO;

/**
 * TRANG 30 — Xử lý báo cáo vi phạm.
 *
 * TẦNG: controller/admin/
 *
 * URL: /admin/report?action=list | resolve | dismiss [&status=PENDING]
 *
 * ĐÁNH DẤU CHỨ KHÔNG TỰ HÀNH ĐỘNG.
 *   Bấm "Đã xử lý" KHÔNG tự gỡ truyện hay ẩn bình luận. Nó chỉ ghi lại rằng
 *   admin đã xem. Việc gỡ/ẩn làm ở trang tương ứng (/admin/story,
 *   /admin/comment).
 *
 *   Nghe như thừa một bước, nhưng gộp lại là nguy hiểm: một cú bấm nhầm ở
 *   trang báo cáo sẽ gỡ mất truyện của người khác. Tách ra thì mỗi hành động
 *   phá huỷ đều phải làm có ý thức, đúng chỗ của nó.
 */
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

        try {
            if ("resolve".equals(action)) {
                reportDAO.updateStatus(parseIntOr(request.getParameter("id"), 0),
                                       "RESOLVED");
            } else if ("dismiss".equals(action)) {
                reportDAO.updateStatus(parseIntOr(request.getParameter("id"), 0),
                                       "DISMISSED");
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
