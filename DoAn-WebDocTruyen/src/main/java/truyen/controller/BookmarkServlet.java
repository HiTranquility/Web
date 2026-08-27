package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.BookmarkDAO;
import truyen.model.User;

/**
 * CASE 08 — Đánh dấu truyện.
 *
 * URL: /bookmark?action=list | add | remove
 */
@WebServlet("/bookmark")
public class BookmarkServlet extends HttpServlet {

    private BookmarkDAO bookmarkDAO;

    @Override
    public void init() throws ServletException {
        bookmarkDAO = new BookmarkDAO();
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
        if (action == null) {
            action = "list";
        }

        /*
         * AuthFilter đã chặn khách chưa đăng nhập ở URL này, nên tới đây
         * currentUser chắc chắn khác null.
         *
         * Vẫn kiểm lại một lần cho chắc: nếu sau này ai đó sửa url-pattern của
         * filter mà quên servlet này, đoạn dưới vẫn chặn được thay vì ném
         * NullPointerException.
         */
        User me = currentUser(request);
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        int storyId = parseIntOr(request.getParameter("storyId"), 0);

        try {
            switch (action) {
                case "add":
                    bookmarkDAO.add(me.getId(), storyId);
                    backToStory(request, response, storyId);
                    return;

                case "remove":
                    bookmarkDAO.remove(me.getId(), storyId);
                    // Gỡ từ trang danh sách thì quay về danh sách, gỡ từ trang
                    // truyện thì quay về truyện — dựa vào tham số "from"
                    if ("list".equals(request.getParameter("from"))) {
                        response.sendRedirect(request.getContextPath() + "/bookmark?action=list");
                    } else {
                        backToStory(request, response, storyId);
                    }
                    return;

                default:
                    request.setAttribute("bookmarks", bookmarkDAO.findByUser(me.getId()));
                    request.setAttribute("pageTitle", "Truyện đã lưu");
                    request.setAttribute("contentPage", "/WEB-INF/views/user/bookmarks.jsp");
                    getServletContext()
                            .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                            .forward(request, response);
            }
        } catch (SQLException e) {
            log("BookmarkServlet: lỗi truy vấn, action=" + action, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void backToStory(HttpServletRequest request, HttpServletResponse response,
                             int storyId) throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId);
    }

    private User currentUser(HttpServletRequest request) {
        return request.getSession(false) == null
                ? null
                : (User) request.getSession(false).getAttribute("currentUser");
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
