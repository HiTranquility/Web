package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.FollowDAO;
import truyen.model.Follow;
import truyen.model.User;

/** TRANG 17 — Theo dõi tác giả. */
@WebServlet("/follow")
public class FollowServlet extends HttpServlet {

    private FollowDAO followDAO;

    @Override
    public void init() throws ServletException {
        followDAO = new FollowDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        User me = (User) request.getSession().getAttribute("currentUser");
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        try {
            List<Follow> following = followDAO.findFollowing(me.getId());
            request.setAttribute("following", following);
        } catch (SQLException e) {
            log("FollowServlet: lỗi truy vấn danh sách theo dõi", e);
            request.setAttribute("message", "Không tải được danh sách theo dõi.");
            request.setAttribute("following", java.util.Collections.emptyList());
        }

        request.setAttribute("pageTitle", "Đang theo dõi");
        request.setAttribute("activeNav", "follow");
        request.setAttribute("contentPage", "/WEB-INF/views/user/following.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
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

        int authorId = parseIntOr(request.getParameter("authorId"), 0);
        boolean unfollow = "unfollow".equals(request.getParameter("do"));

        try {
            if (unfollow) {
                followDAO.unfollow(me.getId(), authorId);
            } else {
                followDAO.follow(me.getId(), authorId);
            }
        } catch (SQLException e) {
            log("FollowServlet: không đổi được trạng thái theo dõi", e);
        }

        /*
         * Quay lại đúng chỗ người dùng vừa bấm.
         *
         * Nút theo dõi xuất hiện ở hai nơi: trang tác giả và trang "Đang theo
         * dõi". Bấm bỏ theo dõi ở danh sách mà bị ném sang trang hồ sơ tác giả
         * là mất mạch. Form gửi kèm "back" để controller biết đường về.
         */
        String back = request.getParameter("back");
        if ("list".equals(back)) {
            response.sendRedirect(request.getContextPath() + "/follow?action=list");
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/user?action=profile&id=" + authorId);
        }
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
