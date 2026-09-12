package truyen.controller.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.StoryDAO;
import truyen.dao.ViewLogDAO;
import truyen.model.ReadHistory;
import truyen.model.Story;
import truyen.model.User;

/** Trang chủ. */
@WebServlet("")
public class HomeServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private ViewLogDAO viewLogDAO;

    /** Số truyện trong dải "Đọc tiếp". Ba là vừa một hàng, không đẩy kho
     *  truyện xuống quá sâu. */
    private static final int RESUME_COUNT = 3;

    /*
     * init() chạy một lần, đúng chỗ để tạo DAO (bài học CASE 14 của chương 5).
     *
     * Để DAO làm biến instance có an toàn không? CÓ — vì StoryDAO không có
     * trạng thái nào bị thay đổi: nó chỉ có method, không có field nào bị ghi.
     * Cái nguy hiểm ở CASE 15 là việc GHI vào field dùng chung, không phải bản
     * thân việc có field.
     */
    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        viewLogDAO = new ViewLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Biến cho layout dùng — xem header.jsp
        request.setAttribute("pageTitle", "ĐọcTruyện — Kho truyện cộng đồng");
        request.setAttribute("activeNav", "home");

        try {
            List<Story> latest = storyDAO.findLatest(12);
            List<Story> popular = storyDAO.findPopular(6);

            request.setAttribute("latest", latest);
            request.setAttribute("popular", popular);
            request.setAttribute("totalStories", storyDAO.countPublished());

            /*
             * DẢI "ĐỌC TIẾP" — phần duy nhất của trang chủ khác nhau tuỳ người.
             *
             * Chỉ hỏi khi ĐÃ đăng nhập. Khách thì không có gì để tiếp, hỏi cũng
             * chỉ tốn thêm một câu SQL cho mỗi lượt ghé trang chủ — mà trang
             * chủ là trang đông lượt truy cập nhất.
             *
             * Dữ liệu đã có sẵn từ trước: view_logs biết đọc lúc nào,
             * bookmarks.last_chapter_id biết dừng ở đâu. Trang chủ chỉ là chỗ
             * thứ hai dùng lại chúng, không thêm bảng hay cột nào.
             */
            User me = (User) request.getSession().getAttribute("currentUser");
            if (me != null) {
                List<ReadHistory> resume = viewLogDAO.findResumable(me.getId(), RESUME_COUNT);
                request.setAttribute("resume", resume);
            }

        } catch (SQLException e) {
            /*
             * DAO ném SQLException lên, servlet mới là nơi quyết định làm gì.
             *
             * log(message, exception) ghi kèm stack trace — bài học CASE 16.
             * Người dùng chỉ thấy thông báo tử tế, chi tiết kỹ thuật vào log.
             */
            log("Không tải được danh sách truyện ở trang chủ", e);
            request.setAttribute("message",
                    "Không tải được danh sách truyện. Vui lòng thử lại sau.");
            request.setAttribute("latest", Collections.<Story>emptyList());
            request.setAttribute("popular", Collections.<Story>emptyList());
        }

        forward(request, response);
    }

    /*
     * Gom lệnh forward vào một chỗ để không lặp lại ba lần, và để chắc chắn
     * mọi nhánh đều đi tới đúng một view.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("contentPage", "/WEB-INF/views/story/home.jsp");

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
