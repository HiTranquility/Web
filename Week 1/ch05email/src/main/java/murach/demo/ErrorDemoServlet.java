package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 13 — Xử lý lỗi tuỳ biến                            (slide 41-45)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Không cấu hình gì thì gõ sai URL sẽ ra trang 404 mặc định của Tomcat, còn
 *   exception không bắt sẽ ra nguyên một stack trace. Cả hai đều xấu, lạc tông
 *   với giao diện web, và cái thứ hai còn khoe cho kẻ tấn công biết tên lớp,
 *   tên thư viện, số dòng và cả phiên bản server bạn đang chạy.
 *
 * CÁCH DÙNG (slide 42, 44) — khai trong web.xml, không có dạng annotation
 *
 *   theo MÃ TRẠNG THÁI HTTP
 *     <error-page>
 *         <error-code>404</error-code>
 *         <location>/error_404.jsp</location>
 *     </error-page>
 *
 *   theo LOẠI EXCEPTION JAVA
 *     <error-page>
 *         <exception-type>java.lang.Throwable</exception-type>
 *         <location>/error_java.jsp</location>
 *     </error-page>
 *
 * TẠI SAO DÙNG java.lang.Throwable
 *   Vì mọi exception đều kế thừa từ nó, nên MỘT khai báo bắt được tất cả. Muốn
 *   xử lý riêng vài loại thì khai thêm error-page cụ thể hơn — Tomcat chọn cái
 *   khớp SÁT nhất, giống như catch nhiều tầng. Ví dụ khai riêng cho
 *   java.sql.SQLException để hiện thông báo "hệ thống đang bảo trì".
 *
 * TRANG LỖI PHẢI CÓ isErrorPage="true"
 *   Không có thuộc tính này trong page directive thì biến ngầm định
 *   pageContext.exception là null, EL in ra rỗng, và trang lỗi trông như bị
 *   hỏng mà chẳng có dấu hiệu gì để lần. Xem error_java.jsp.
 *
 * GHI CHÚ CỦA SLIDE 33 — VÀ LÝ DO CỦA NÓ
 *   Sách viết: "you can comment out these error tags when the app is in
 *   development". Đúng, vì lúc dev thì stack trace CHÍNH LÀ thứ bạn cần đọc;
 *   một trang lỗi lịch sự chỉ che mất nó đi. Bật lên khi lên production.
 *   Và trên production thì đừng in ${pageContext.exception} ra cho người dùng
 *   xem — chi tiết kỹ thuật thuộc về log file (CASE 16), không thuộc về màn hình.
 * ========================================================================= */
@WebServlet("/errorDemo")
public class ErrorDemoServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String type = request.getParameter("type");

        // Không có tham số type: chỉ hiện trang giới thiệu case, không ném gì.
        if (type == null) {
            getServletContext()
                    .getRequestDispatcher("/demo/case13.jsp")
                    .forward(request, response);

            // return BẮT BUỘC. Thiếu nó thì code chạy tiếp xuống dưới và có thể
            // đụng vào response đã forward xong -> IllegalStateException.
            return;
        }

        /*
         * Ba nhánh dưới đây ném ba exception KHÁC NHAU, và cả ba cùng rơi vào
         * một khai báo error-page duy nhất (java.lang.Throwable). Đó là điều
         * trang demo muốn chứng minh.
         *
         * Chú ý: không có try/catch ở đây. Cố ý. Phải để exception bay lên tận
         * container thì cơ chế error-page mới kích hoạt. Bắt lại là tự tay vô
         * hiệu hoá nó.
         */
        if (type.equals("nullPointer")) {
            // Lỗi kinh điển: gọi method trên biến null.
            String nothing = null;
            nothing.length();                 // -> NullPointerException

        } else if (type.equals("arithmetic")) {
            // Chia cho 0 với số nguyên. (Với double thì KHÔNG ném exception mà
            // ra Infinity — một khác biệt hay bị quên.)
            int zero = 0;
            System.out.println(42 / zero);    // -> ArithmeticException

        } else if (type.equals("custom")) {
            // Exception do mình chủ động ném, kèm thông điệp rõ ràng — đây là
            // cách bạn báo lỗi nghiệp vụ trong code thật.
            throw new IllegalStateException(
                    "A deliberate exception thrown by ErrorDemoServlet.");

        } else if (type.equals("statusCode")) {
            /*
             * Nhánh này KHÔNG ném exception. Nó chủ động trả về mã trạng thái,
             * và error-page theo <error-code>404> bắt lấy.
             *
             * Phân biệt hai method dễ nhầm:
             *   sendError(404)  -> kích hoạt error-page, hiện trang tuỳ biến
             *   setStatus(404)  -> chỉ đặt mã, KHÔNG kích hoạt error-page
             * Muốn hiện trang lỗi của mình thì phải là sendError.
             */
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
