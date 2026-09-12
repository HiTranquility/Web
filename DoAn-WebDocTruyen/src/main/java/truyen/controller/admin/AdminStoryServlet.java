package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.StoryDAO;

/** CASE 10 — Quản trị truyện: gỡ và khôi phục. */
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

        if ("delete".equals(action) || "restore".equals(action) || "publish".equals(action)) {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
        }

        try {
            if ("delete".equals(action) || "restore".equals(action) || "publish".equals(action)) {
                int id = parseIntOr(request.getParameter("id"), 0);

                String targetStatus = "DELETED";
                if ("restore".equals(action) || "publish".equals(action)) {
                    targetStatus = "PUBLISHED";
                }
                storyDAO.updateStatus(id, targetStatus);

                // Post/Redirect/Get — F5 không gửi lại request
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
