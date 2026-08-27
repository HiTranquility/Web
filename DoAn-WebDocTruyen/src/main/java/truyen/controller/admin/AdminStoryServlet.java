package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.StoryDAO;

/**
 * CASE 10 — Quản trị truyện: gỡ và khôi phục.
 *
 * URL: /admin/story?action=list | delete | restore
 *
 * KHÔNG kiểm quyền admin trong file này — AdminFilter đã chặn ở /admin/*
 * trước khi request tới đây. Kiểm lại lần nữa là thừa, và tệ hơn: nó khiến
 * người đọc tưởng filter không đáng tin.
 *
 * Đặt trong package con `admin` để nhìn cây thư mục là thấy ngay đâu là khu
 * quản trị — và để url-pattern của filter khớp với cấu trúc package.
 */
@WebServlet("/admin/story")
public class AdminStoryServlet extends HttpServlet {

    private StoryDAO storyDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
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

        try {
            if ("delete".equals(action) || "restore".equals(action)) {
                int id = parseIntOr(request.getParameter("id"), 0);

                /*
                 * Gỡ = đổi status sang DELETED. Khôi phục = đổi về PUBLISHED.
                 * Không có DELETE FROM ở đâu cả — nhờ vậy admin bấm nhầm vẫn
                 * lấy lại được, và bình luận/bookmark của truyện không mồ côi.
                 */
                storyDAO.updateStatus(id, "delete".equals(action) ? "DELETED" : "PUBLISHED");

                // Post/Redirect/Get — F5 sau khi gỡ không gỡ lại lần nữa
                response.sendRedirect(request.getContextPath() + "/admin/story");
                return;
            }

            request.setAttribute("stories", storyDAO.findAllForAdmin());

        } catch (SQLException e) {
            log("AdminStoryServlet: lỗi truy vấn, action=" + action, e);
            request.setAttribute("message", "Không tải được danh sách truyện.");
        }

        request.setAttribute("pageTitle", "Quản trị — Truyện");
        request.setAttribute("activeNav", "admin");
        request.setAttribute("adminSection", "story");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/stories.jsp");
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
