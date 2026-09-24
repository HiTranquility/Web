package truyen.controller.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.StoryDAO;
import truyen.dao.UserDAO;
import truyen.model.Story;
import truyen.model.User;

/** TRANG 5 — Bảng xếp hạng. */
@WebServlet("/rank")
public class RankServlet extends HttpServlet {

    /** Danh sách TRẮNG các kiểu xếp hạng. */
    private static final List<String> ALLOWED =
            Arrays.asList("views", "week", "month", "chapters", "newest", "rating", "authors");

    private StoryDAO storyDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        userDAO = new UserDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String by = request.getParameter("by");
        if (by == null || !ALLOWED.contains(by)) {
            by = "views";    // giá trị lạ -> về mặc định, không báo lỗi
        }

        try {
            if ("authors".equals(by)) {
                List<User> topAuthors = userDAO.findTopAuthors(20);
                request.setAttribute("topAuthors", topAuthors);
            } else if ("week".equals(by)) {
                List<Story> stories = storyDAO.findTopByPeriod(7, 20);
                request.setAttribute("stories", stories);
            } else if ("month".equals(by)) {
                List<Story> stories = storyDAO.findTopByPeriod(30, 20);
                request.setAttribute("stories", stories);
            } else {
                List<Story> stories = storyDAO.findTop(by, 20);
                request.setAttribute("stories", stories);
            }
            request.setAttribute("by", by);

        } catch (SQLException e) {
            log("RankServlet: lỗi truy vấn, by=" + by, e);
            request.setAttribute("message", "Không tải được bảng xếp hạng.");
            request.setAttribute("stories", java.util.Collections.emptyList());
            request.setAttribute("topAuthors", java.util.Collections.emptyList());
        }

        request.setAttribute("pageTitle", "Bảng xếp hạng");
        request.setAttribute("activeNav", "rank");
        request.setAttribute("contentPage", "/WEB-INF/views/common/rank.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
