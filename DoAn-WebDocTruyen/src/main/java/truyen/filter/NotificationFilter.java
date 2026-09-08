package truyen.filter;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import truyen.dao.NotificationDAO;
import truyen.model.User;

/**
 * Đếm số thông báo chưa đọc, đặt vào request cho thanh menu dùng.
 *
 * VÌ SAO PHẢI LÀ FILTER CHỨ KHÔNG PHẢI SERVLET
 *   Chấm đỏ nằm ở parts/nav.jsp — mảnh dùng chung cho MỌI trang. Nếu để mỗi
 *   servlet tự đếm thì 20 servlet phải nhớ thêm cùng một dòng, và chỉ cần
 *   quên một chỗ là chấm đỏ biến mất ở đúng trang đó.
 *
 *   Filter chạy TRƯỚC mọi servlet nên đặt một lần là xong.
 *
 * BA CHỐT CHẶN ĐỂ KHÔNG LÀM CHẬM CẢ WEB
 *   Filter này chạy ở MỌI request, kể cả file CSS. Câu SQL rẻ nhất cũng thành
 *   đắt khi nhân với số lần đó, nên:
 *     1. Chưa đăng nhập  -> thoát ngay, không mở kết nối
 *     2. Tài nguyên tĩnh -> thoát ngay (assets, favicon)
 *     3. Lỗi truy vấn    -> nuốt, chỉ ghi log
 *
 *   Chốt thứ ba quan trọng nhất: thông báo là việc phụ. Bảng notifications
 *   trục trặc mà làm cả trang chủ trả về 500 thì đó là lỗi thiết kế, không
 *   phải lỗi cơ sở dữ liệu.
 */
@WebFilter("/*")
public class NotificationFilter implements Filter {

    private NotificationDAO notificationDAO;

    @Override
    public void init(javax.servlet.FilterConfig config) {
        notificationDAO = new NotificationDAO();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        /*
         * Bỏ qua tài nguyên tĩnh.
         *
         * Một trang HTML kéo theo 3-4 file CSS. Không chặn ở đây thì mỗi lần
         * tải trang là 5 câu SQL thay vì 1 — cho cùng một con số.
         */
        String path = request.getRequestURI();
        if (path.startsWith(request.getContextPath() + "/assets")
                || path.endsWith(".css") || path.endsWith(".js")
                || path.endsWith(".ico") || path.endsWith(".png")) {
            chain.doFilter(req, res);
            return;
        }

        User me = (User) request.getSession().getAttribute("currentUser");
        if (me != null) {
            try {
                request.setAttribute("unreadCount",
                        notificationDAO.countUnread(me.getId()));
            } catch (SQLException e) {
                // Nuốt có chủ ý — xem ghi chú ở đầu lớp.
                request.getServletContext().log(
                        "NotificationFilter: không đếm được thông báo chưa đọc", e);
            }
        }

        chain.doFilter(req, res);
    }

    @Override
    public void destroy() { }
}
