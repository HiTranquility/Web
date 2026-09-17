package truyen.controller.user;

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
import truyen.util.ServletHelper;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.currentUser;

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
        User me = currentUser(request);
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
        User me = currentUser(request);
        boolean isAjax = ServletHelper.isAjax(request);

        if (me == null) {
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập để thực hiện.\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        int authorId = parseIntOr(request.getParameter("authorId"), 0);
        if (authorId <= 0 || authorId == me.getId()) {
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Không thể tự theo dõi chính mình.\"}");
                return;
            }
            response.sendRedirect(request.getContextPath() + "/user?action=profile&id=" + authorId);
            return;
        }

        boolean unfollow = "unfollow".equals(request.getParameter("do"));

        try {
            if (unfollow) {
                followDAO.unfollow(me.getId(), authorId);
            } else {
                followDAO.follow(me.getId(), authorId);
            }
        } catch (SQLException e) {
            log("FollowServlet: không đổi được trạng thái theo dõi", e);
            if (isAjax) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"success\":false,\"message\":\"Lỗi hệ thống khi cập nhật theo dõi.\"}");
                return;
            }
        }

        if (isAjax) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":true,\"following\":" + (!unfollow)
                    + ",\"message\":\"" + (!unfollow ? "Đã theo dõi tác giả!" : "Đã bỏ theo dõi tác giả.") + "\"}");
            return;
        }

        /*
         * Quay lại đúng chỗ người dùng vừa bấm.
         */
        String back = request.getParameter("back");
        if ("list".equals(back)) {
            response.sendRedirect(request.getContextPath() + "/follow?action=list");
        } else if ("story".equals(back) && request.getParameter("storyId") != null) {
            response.sendRedirect(request.getContextPath()
                    + "/story?action=detail&id=" + request.getParameter("storyId"));
        } else {
            response.sendRedirect(request.getContextPath()
                    + "/user?action=profile&id=" + authorId);
        }
    }
}
