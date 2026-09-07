package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.RatingDAO;
import truyen.model.User;

/**
 * Chấm sao truyện.
 *
 * TẦNG: controller/
 *
 * URL: POST /rating   (storyId, score)
 *
 * CHỈ NHẬN POST — KHÔNG CÓ doGet.
 *   Chấm sao là hành động THAY ĐỔI dữ liệu. Nếu nhận cả GET thì chỉ cần dụ
 *   người khác mở một thẻ <img src="/rating?storyId=1&score=1"> là truyện bị
 *   dìm điểm mà chủ trình duyệt không hề bấm gì. Đó là CSRF.
 *
 *   Chặn bằng POST không diệt hẳn CSRF (form giả vẫn POST được), nhưng loại bỏ
 *   được kiểu tấn công dễ nhất. Hàng rào đủ cho quy mô đồ án; hệ thống thật
 *   thêm token CSRF cho mỗi form.
 *
 * Đường dẫn /rating nằm trong danh sách cần đăng nhập của AuthFilter, nên tới
 * được đây là chắc chắn đã có currentUser.
 */
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
