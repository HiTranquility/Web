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
import truyen.util.DBConnection;

/**
 * Trang chủ.
 *
 * MẪU CHUNG CỦA MỌI CONTROLLER TRONG DỰ ÁN — giống hệt CASE 11 của chương 5:
 *   1. đọc tham số
 *   2. gọi DAO lấy dữ liệu
 *   3. setAttribute cho JSP
 *   4. forward ĐÚNG MỘT LẦN ở cuối
 * Controller không chứa SQL, không chứa HTML. Nó chỉ điều phối.
 *
 * Vì sao map "" (chuỗi rỗng) chứ không phải "/":
 *   "" là pattern dành riêng cho context root, tức http://localhost:8080/app/
 *   "/" là "default servlet", sẽ nuốt luôn cả file CSS và ảnh — trang web mất
 *   sạch định dạng. Đây là cái bẫy rất hay gặp.
 */
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

        /*
         * Kiểm tra database TRƯỚC khi truy vấn.
         *
         * Ở giai đoạn này của đồ án, database có thể chưa được tạo. Thay vì để
         * người dùng nhận một trang lỗi 500 khó hiểu, trang chủ sẽ hiện hướng
         * dẫn cài đặt. Khối này sẽ được gỡ khi mọi thứ đã chạy ổn định.
         */
        String dbError = DBConnection.checkConnection();
        if (dbError != null) {
            request.setAttribute("dbError", dbError);
            request.setAttribute("latest", Collections.<Story>emptyList());
            request.setAttribute("popular", Collections.<Story>emptyList());
            forward(request, response);
            return;   // return để KHÔNG chạy tiếp xuống forward lần hai
        }

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
            request.setAttribute("dbError", e.getMessage());
            request.setAttribute("latest", Collections.<Story>emptyList());
            request.setAttribute("popular", Collections.<Story>emptyList());
        }

        forward(request, response);
    }

    /*
     * Gom lệnh forward vào một chỗ để không lặp lại ba lần, và để chắc chắn
     * mọi nhánh đều đi tới đúng một view.
     *
     * CÁCH LAYOUT HOẠT ĐỘNG — hai bước:
     *   1. đặt contentPage = mảnh nội dung cần hiện
     *   2. forward tới LAYOUT, không phải tới mảnh đó
     *
     * layout/main.jsp dựng khung HTML đầy đủ rồi chèn mảnh vào giữa bằng
     * <jsp:include page="${contentPage}" />.
     *
     * Muốn trang này dùng khung khác (ví dụ auth) thì chỉ đổi dòng forward
     * cuối cùng sang "/WEB-INF/views/layout/auth.jsp" — mảnh nội dung không
     * phải sửa gì cả. Đó là toàn bộ lợi ích của cách chia này.
     *
     * Cả layout lẫn mảnh đều nằm trong /WEB-INF/ nên không ai gõ URL vào xem
     * trực tiếp được — mọi lối vào đều phải qua controller.
     */
    private void forward(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("contentPage", "/WEB-INF/views/story/home.jsp");

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
