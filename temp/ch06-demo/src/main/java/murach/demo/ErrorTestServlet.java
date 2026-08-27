package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * KHÔNG có trong sách — lớp này mình thêm vào, nên để package riêng
 * murach.demo thay vì murach.email (murach.email là package của sách).
 *
 * Cho phép mở thẳng từng trang lỗi bằng URL gọn:
 *
 *     http://localhost:8080/404
 *     http://localhost:8080/403
 *     http://localhost:8080/500
 *
 * VÌ SAO CẦN SERVLET NÀY
 *   Trang lỗi bình thường chỉ hiện khi có lỗi thật, nên rất khó mở ra xem hay
 *   demo cho giảng viên. Servlet này tạo ra đúng lỗi đó theo yêu cầu.
 *
 * VÌ SAO KHÔNG TRỎ THẲNG URL VÀO FILE .jsp
 *   Mở /error_404.jsp trực tiếp thì trình duyệt nhận HTTP **200 OK** kèm nội
 *   dung trang 404 — nhìn thì giống, nhưng mã trạng thái SAI. Đi qua servlet
 *   này thì sendError() kích hoạt đúng cơ chế <error-page> của web.xml, nên
 *   trình duyệt nhận mã 404 thật. Kiểm chứng bằng tab Network của DevTools.
 */
@WebServlet(urlPatterns = {"/404", "/403", "/500"})
public class ErrorTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {

        /*
         * getServletPath() trả về đúng phần URL đã khớp url-pattern:
         * "/404", "/403" hoặc "/500". Cắt dấu "/" đầu là ra mã số.
         *
         * Nhờ vậy một servlet phục vụ được cả ba URL, không cần ba lớp
         * gần giống hệt nhau.
         */
        String path = request.getServletPath();     // ví dụ "/403"
        String code = path.substring(1);            // -> "403"

        if (code.equals("403")) {
            /*
             * sendError() chứ KHÔNG phải setStatus().
             *   sendError(403) -> kích hoạt <error-page>, hiện trang tuỳ biến
             *   setStatus(403) -> chỉ đặt mã, KHÔNG kích hoạt error-page
             * Nhầm hai method này là trang lỗi của bạn không bao giờ hiện ra.
             */
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

        } else if (code.equals("404")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

        } else {
            /*
             * Với 500 thì ném exception KHÔNG bắt, để nó bay lên tới container.
             * Khai báo <exception-type>java.lang.Throwable</exception-type>
             * trong web.xml sẽ tóm lấy và chuyển sang error_500.jsp.
             *
             * Bọc try/catch ở đây là tự tay vô hiệu hoá cơ chế error-page.
             */
            throw new IllegalStateException(
                    "Lỗi cố ý tạo ra để kiểm thử trang error_500.jsp");
        }
    }
}
