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
 * SÁU KIỂU XẾP HẠNG, HAI NGUỒN DỮ LIỆU
 *   week, month   đếm trên bảng view_logs (mỗi dòng một lượt xem có thời điểm)
 *   views         đọc stories.view_count — số cộng dồn từ ngày đăng
 *   rating        điểm trung bình, có ngưỡng tối thiểu 3 lượt chấm
 *   chapters      số chương
 *   newest        mới đăng
 *
 *   Trước đây trang này KHÔNG làm được tuần/tháng, vì `stories` chỉ có một
 *   con số cộng dồn — nó không nhớ lượt xem nào xảy ra khi nào. Bảng
 *   view_logs sinh ra để trả lời đúng câu hỏi đó.
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
    private static final List<String> ALLOWED =
            Arrays.asList("views", "week", "month", "chapters", "newest", "rating");

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
            /*
             * Hai nhánh xếp hạng, hai nguồn dữ liệu khác nhau:
             *
             *   week / month -> đếm trên view_logs, biết lượt xem xảy ra KHI NÀO
             *   còn lại      -> đọc thẳng số cộng dồn trên stories, nhanh hơn
             *
             * Không gộp làm một được: view_count là một con số duy nhất, nó
             * không nhớ thời điểm. Đó chính là lý do bảng view_logs tồn tại.
             */
            List<Story> stories;
            if ("week".equals(by)) {
                stories = storyDAO.findTopByPeriod(7, 20);
            } else if ("month".equals(by)) {
                stories = storyDAO.findTopByPeriod(30, 20);
            } else {
                stories = storyDAO.findTop(by, 20);
            }
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
