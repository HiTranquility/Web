package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.CommentDAO;

/**
 * TRANG 29 — Quản lý bình luận.
 *
 * TẦNG: controller/admin/
 *
 * URL: /admin/comment?action=list | hide | unhide [&filter=hidden]
 *
 * ẨN, KHÔNG XOÁ.
 *   Bình luận vi phạm bị chuyển sang status = 'HIDDEN': độc giả không thấy
 *   nữa, nhưng dòng dữ liệu vẫn còn. Hai lý do:
 *
 *     1. Bấm nhầm thì bỏ ẩn được. Xoá thật thì không có đường về.
 *     2. Ẩn xong người viết quay lại cãi thì còn nội dung gốc để đối chiếu.
 *        Xoá rồi thì chỉ còn lời qua tiếng lại.
 *
 *   Đây cùng một nguyên tắc với việc gỡ truyện (status = 'DELETED') và khoá
 *   tài khoản (status = 'BANNED') — toàn dự án dùng xoá mềm, không có chỗ nào
 *   thực sự DELETE dữ liệu người dùng.
 */
@WebServlet("/admin/comment")
public class AdminCommentServlet extends HttpServlet {

    private CommentDAO commentDAO;

    @Override
    public void init() throws ServletException {
        commentDAO = new CommentDAO();
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
        String filter = request.getParameter("filter");

        try {
            if ("hide".equals(action)) {
                commentDAO.hide(parseIntOr(request.getParameter("id"), 0));
            } else if ("unhide".equals(action)) {
                commentDAO.unhide(parseIntOr(request.getParameter("id"), 0));
            }

            boolean onlyHidden = "hidden".equals(filter);
            request.setAttribute("comments", commentDAO.findAllForAdmin(onlyHidden));
            request.setAttribute("filter", filter);

        } catch (SQLException e) {
            log("AdminCommentServlet: lỗi truy vấn, action=" + action, e);
            request.setAttribute("message", "Không tải được danh sách bình luận.");
            request.setAttribute("comments", java.util.Collections.emptyList());
        }

        request.setAttribute("pageTitle", "Quản trị — Bình luận");
        request.setAttribute("activeNav", "admin");
        request.setAttribute("adminSection", "comment");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/comments.jsp");
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
