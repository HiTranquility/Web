package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
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

/**
 * TRANG 4 — Trang tác giả (hồ sơ công khai).
 *
 * TẦNG: controller/ — chỉ điều phối. Không viết SQL, không sinh HTML.
 *
 * URL: /user?action=profile&id=2
 *
 * NHIỆM VỤ CỦA MỘT CONTROLLER, đúng 4 bước:
 *   1. đọc tham số từ request
 *   2. gọi DAO lấy dữ liệu
 *   3. setAttribute cho JSP
 *   4. forward tới layout ĐÚNG MỘT LẦN ở cuối
 *
 * Không có bước thứ 5. Mọi câu SQL nằm ở dao/, mọi thẻ HTML nằm ở views/.
 */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private StoryDAO storyDAO;

    /** Số truyện mỗi trang trên hồ sơ tác giả. */
    private static final int PAGE_SIZE = 12;

    // init() chạy MỘT lần — chỗ đúng để tạo DAO, không tạo lại ở mỗi request
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        storyDAO = new StoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "profile";
        }

        String url;
        try {
            // Hiện chỉ có một action. switch để sau này thêm "edit", "me"
            // là chèn thêm nhánh, không phải viết lại cấu trúc.
            switch (action) {
                default:
                    url = profile(request, response);
                    break;
            }
        } catch (SQLException e) {
            log("UserServlet: lỗi truy vấn, action=" + action, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (url == null) {
            return;   // method con đã sendError hoặc redirect xong
        }

        request.setAttribute("contentPage", url);
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }

    private String profile(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseIntOr(request.getParameter("id"), 0);
        User author = userDAO.findById(id);

        if (author == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        /*
         * KHÔNG BAO GIỜ để chuỗi băm mật khẩu đi ra ngoài tầng controller.
         *
         * UserDAO.findById() trả về đầy đủ, gồm cả password_hash. Nếu để
         * nguyên rồi setAttribute, một dòng ${author.passwordHash} lỡ tay
         * trong JSP là in chuỗi băm ra trang web công khai.
         *
         * Xoá ngay tại đây — sớm nhất có thể sau khi rời DAO.
         */
        author.setPasswordHash(null);

        int page = parseIntOr(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        int total = storyDAO.countPublishedByAuthor(id);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }

        List<Story> stories = storyDAO.findPublishedByAuthor(
                id, (page - 1) * PAGE_SIZE, PAGE_SIZE);

        request.setAttribute("author", author);
        request.setAttribute("stories", stories);
        request.setAttribute("totalStories", total);
        request.setAttribute("totalViews", storyDAO.totalViewsByAuthor(id));
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageTitle", "Tác giả " + author.getName());
        return "/WEB-INF/views/user/profile.jsp";
    }

    /** ?id=abc không được làm sập trang — trả về giá trị mặc định. */
    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
