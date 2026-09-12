package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.RatingDAO;
import truyen.model.User;

/** Chấm sao truyện. */
@WebServlet("/rating")
public class RatingServlet extends HttpServlet {

    private RatingDAO ratingDAO;

    @Override
    public void init() throws ServletException {
        ratingDAO = new RatingDAO();
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

        int storyId = parseIntOr(request.getParameter("storyId"), 0);
        int score   = parseIntOr(request.getParameter("score"), 0);

        try {
            ratingDAO.rate(me.getId(), storyId, score);
        } catch (SQLException e) {
            log("RatingServlet: không chấm được, storyId=" + storyId, e);
            // Không dựng trang lỗi cho việc này. Người dùng quay lại trang
            // truyện và thấy sao chưa đổi là đủ hiểu.
        }

        /*
         * Redirect sau POST (mẫu Post/Redirect/Get).
         *
         * Nếu forward thẳng tới JSP, thanh địa chỉ vẫn là /rating với method
         * POST. Người dùng bấm F5 là trình duyệt hỏi "gửi lại biểu mẫu?" và
         * chấm thêm một lần nữa. Redirect biến trang sau đó thành một GET
         * bình thường, F5 bao nhiêu lần cũng vô hại.
         */
        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId + "#rating");
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
