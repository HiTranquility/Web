package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.AuditDAO;
import truyen.model.AuditLog;
import static truyen.util.ServletHelper.parseIntOr;

/**
 * Hiển thị nhật ký thao tác quản trị cho Quản trị viên (ISSUE-023).
 *
 * BẢO MẬT BẤT BIẾN:
 * Chỉ hỗ trợ phương thức GET (Chỉ đọc). Tuyệt đối KHÔNG có phương thức POST / UPDATE / DELETE.
 */
@WebServlet(name = "AdminAuditServlet", urlPatterns = "/admin/audit")
public class AdminAuditServlet extends HttpServlet {

    private final AuditDAO auditDAO = new AuditDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String actionFilter = request.getParameter("action");
        if (actionFilter == null || actionFilter.trim().isEmpty()) {
            actionFilter = "ALL";
        }

        int page = parseIntOr(request.getParameter("page"), 1);
        if (page < 1) page = 1;
        int limit = 20;
        int offset = (page - 1) * limit;

        try {
            List<AuditLog> auditLogs = auditDAO.findLatest(limit, offset, actionFilter);
            int total = auditDAO.count(actionFilter);
            int totalPages = (int) Math.ceil((double) total / limit);
            if (totalPages < 1) totalPages = 1;

            request.setAttribute("auditLogs", auditLogs);
            request.setAttribute("total", total);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("actionFilter", actionFilter);
        } catch (SQLException e) {
            log("AdminAuditServlet: lỗi truy vấn nhật ký thao tác", e);
            request.setAttribute("error", "Không thể nạp nhật ký kiểm duyệt. Vui lòng thử lại.");
        }

        request.setAttribute("pageTitle", "Nhật ký Thao tác Quản trị");
        request.setAttribute("activeNav", "admin-audit");
        request.setAttribute("adminSection", "audit");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/audit.jsp");
        request.getRequestDispatcher("/WEB-INF/views/layout/admin.jsp").forward(request, response);
    }
}
