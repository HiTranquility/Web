package truyen.controller;

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
import truyen.model.Story;

/** Trang chủ. */
@WebServlet("")
public class HomeServlet extends HttpServlet {

    private StoryDAO storyDAO;

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
