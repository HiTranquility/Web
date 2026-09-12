package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.ReportDAO;
import truyen.model.Report;
import truyen.model.User;

/** Người dùng gửi báo cáo vi phạm. */
@WebServlet("/report")
public class ReportServlet extends HttpServlet {

    private ReportDAO reportDAO;

    @Override
    public void init() throws ServletException {
        reportDAO = new ReportDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        User me = (User) request.getSession().getAttribute("currentUser");
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        String type   = request.getParameter("targetType");
        int targetId  = parseIntOr(request.getParameter("targetId"), 0);
        int storyId   = parseIntOr(request.getParameter("storyId"), targetId);
        String reason = trim(request.getParameter("reason"));

        if (reason.isEmpty()) {
            reason = "Không nêu lý do";
        }
        if (reason.length() > 500) {
            // Cắt cho vừa cột VARCHAR(500). Để CSDL tự từ chối thì người dùng
            // nhận được một trang lỗi 500 khó hiểu thay vì báo cáo được gửi.
            reason = reason.substring(0, 500);
        }

        Report r = new Report();
        r.setReporterId(me.getId());
        r.setTargetType("COMMENT".equals(type) ? "COMMENT" : "STORY");
        r.setTargetId(targetId);
        r.setReason(reason);

        try {
            reportDAO.insert(r);
            request.getSession().setAttribute("flash",
                    "Đã gửi báo cáo. Quản trị viên sẽ xem xét.");
        } catch (SQLException e) {
            log("ReportServlet: không gửi được báo cáo", e);
            request.getSession().setAttribute("flash",
                    "Chưa gửi được báo cáo, thử lại sau.");
        }

        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId);
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
