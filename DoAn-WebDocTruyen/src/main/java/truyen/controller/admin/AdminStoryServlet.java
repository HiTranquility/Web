package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.StoryDAO;
import static truyen.util.ServletHelper.parseIntOr;

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

        String status = request.getParameter("status");

        try {
            if ("delete".equals(action) || "restore".equals(action) || "publish".equals(action)) {
                int id = parseIntOr(request.getParameter("id"), 0);

                String targetStatus = "DELETED";
                if ("restore".equals(action) || "publish".equals(action)) {
                    targetStatus = "PUBLISHED";
                }
                storyDAO.updateStatus(id, targetStatus);

                // Post/Redirect/Get — giữ nguyên bộ lọc status nếu có
                String qs = (status != null && !status.isEmpty()) ? "?status=" + status : "";
                response.sendRedirect(request.getContextPath() + "/admin/story" + qs);
                return;
            }

            java.util.List<truyen.model.Story> all = storyDAO.findAllForAdmin();
            java.util.List<truyen.model.Story> filtered = new java.util.ArrayList<>();
            int draftCount = 0, publishedCount = 0, deletedCount = 0;

            for (truyen.model.Story s : all) {
                if ("DRAFT".equals(s.getStatus())) draftCount++;
                else if ("PUBLISHED".equals(s.getStatus())) publishedCount++;
                else if ("DELETED".equals(s.getStatus())) deletedCount++;

                if (status == null || status.isEmpty() || status.equalsIgnoreCase(s.getStatus())) {
                    filtered.add(s);
                }
            }

            request.setAttribute("stories", filtered);
            request.setAttribute("status", status);
            request.setAttribute("totalCount", all.size());
            request.setAttribute("publishedCount", publishedCount);
            request.setAttribute("draftCount", draftCount);
            request.setAttribute("deletedCount", deletedCount);

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
}
