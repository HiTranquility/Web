package truyen.controller;

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
import truyen.model.Story;

/**
 * TRANG 5 — Bảng xếp hạng.
 *
 * TẦNG: controller/
 *
 * URL: /rank?by=views | chapters | newest
 *
 * ⚠️ GIỚI HẠN ĐÃ BIẾT — nói trước để không ai hiểu nhầm
 *   Bản đăng ký đề tài ghi "Bảng xếp hạng theo tuần / tháng". Trang này CHƯA
 *   làm được điều đó, và lý do nằm ở cấu trúc dữ liệu:
 *
 *   Bảng `stories` chỉ có MỘT con số `view_count` cộng dồn từ đầu. Nó không
 *   biết lượt xem nào xảy ra tuần này, lượt nào từ năm ngoái. Muốn xếp hạng
 *   theo tuần thì phải có bảng ghi TỪNG lượt xem kèm thời điểm:
 *
 *       view_logs(story_id, user_id, viewed_at)
 *
 *   rồi đếm `WHERE viewed_at >= NOW() - INTERVAL 7 DAY`.
 *
 *   Bảng đó chưa có, nên hiện xếp hạng theo tổng tích luỹ. Xem
 *   docs/ke-hoach-database.md để biết khi nào thêm.
 */
@WebServlet("/rank")
public class RankServlet extends HttpServlet {

    /**
     * Danh sách TRẮNG các kiểu xếp hạng.
     *
     * Tham số "by" đi thẳng vào mệnh đề ORDER BY của câu SQL. Không kiểm thì
     * đó là SQL injection. Kiểm ở đây là lớp phòng thủ thứ nhất; StoryDAO.findTop()
     * còn một lớp nữa (chỉ dùng chuỗi hằng số).
     *
     * Hai lớp cho một lỗ hổng nghe thừa, nhưng lớp trong không phụ thuộc lớp
     * ngoài — ai đó gọi findTop() từ chỗ khác vẫn an toàn.
     */
    private static final List<String> ALLOWED = Arrays.asList("views", "chapters", "newest");

    private StoryDAO storyDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
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
            List<Story> stories = storyDAO.findTop(by, 20);
            request.setAttribute("stories", stories);
            request.setAttribute("by", by);

        } catch (SQLException e) {
            log("RankServlet: lỗi truy vấn, by=" + by, e);
            request.setAttribute("message", "Không tải được bảng xếp hạng.");
            request.setAttribute("stories", java.util.Collections.emptyList());
        }

        request.setAttribute("pageTitle", "Bảng xếp hạng");
        request.setAttribute("activeNav", "rank");
        request.setAttribute("contentPage", "/WEB-INF/views/story/rank.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
