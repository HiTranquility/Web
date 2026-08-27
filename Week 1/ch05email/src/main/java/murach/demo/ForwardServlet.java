package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 09 — Forward request bằng RequestDispatcher        (slide 23-24)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Servlet quyết định HIỂN THỊ CÁI GÌ, JSP biết HIỂN THỊ NHƯ THẾ NÀO. Cần một
 *   cách để servlet giao việc còn lại cho JSP, kèm theo dữ liệu.
 *
 * CÁCH DÙNG (slide 24 — cả ba loại đích đều cùng một cú pháp)
 *   String url = "/index.html";          // sang trang HTML tĩnh
 *   String url = "/thanks.jsp";          // sang JSP
 *   String url = "/cart/displayInvoice"; // sang servlet khác
 *   getServletContext().getRequestDispatcher(url).forward(request, response);
 *
 *   Đường dẫn PHẢI bắt đầu bằng "/" và tính từ gốc ứng dụng — KHÔNG kèm
 *   context path. Viết "/ch05email/thanks.jsp" là sai, ra 404.
 *
 * FORWARD LÀM GÌ Ở BÊN TRONG
 *   Toàn bộ chuyện xảy ra trong server, trong CÙNG MỘT request. Tomcat chỉ đổi
 *   đích rồi chạy tiếp. Trình duyệt không hề biết có chuyện gì xảy ra — nó gửi
 *   đi một request và nhận về một response, hết.
 *
 * BA HỆ QUẢ, VÀ VÌ SAO CHÚNG QUAN TRỌNG
 *   1. Attribute còn nguyên. Vì vẫn là object request cũ. Đây chính là cơ chế
 *      khiến MVC chạy được: servlet setAttribute rồi forward, JSP đọc bằng EL.
 *      Không có nó thì không có CASE 08, không có CASE 11.
 *   2. URL trên thanh địa chỉ không đổi. Vẫn là /forward dù đang xem case09.jsp.
 *   3. Không ra khỏi được ứng dụng. Muốn sang server khác thì phải redirect
 *      (CASE 10).
 *
 * CHỈ ĐƯỢC FORWARD MỘT LẦN
 *   Sau khi forward, response coi như đã gửi. Forward lần nữa (hoặc ghi thêm
 *   vào response) sẽ ném IllegalStateException. Vì vậy mọi servlet trong chương
 *   này đều viết theo một khuôn: tính ra biến url trước, forward đúng một lần ở
 *   cuối method. Đừng forward ở giữa rồi chạy tiếp.
 * ========================================================================= */
@WebServlet("/forward")
public class ForwardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String target = request.getParameter("target");
        if (target == null) {
            target = "jsp";     // giá trị mặc định khi vào thẳng /forward
        }

        /*
         * Tính url TRƯỚC, forward SAU — đúng khuôn mà slide 24 và CASE 11 dùng.
         * Mỗi nhánh chỉ gán biến, không nhánh nào tự forward. Nhờ vậy chắc chắn
         * chỉ có đúng một lần forward, dù thêm bao nhiêu nhánh đi nữa.
         */
        String url;
        if (target.equals("html")) {
            url = "/demo/forward_target.html";     // đích là trang HTML tĩnh
        } else if (target.equals("servlet")) {
            url = "/attributes";                   // đích là servlet khác
        } else {
            url = "/demo/case09.jsp";              // đích là JSP
        }

        // Attribute này chứng minh forward dùng LẠI object request cũ: nó vẫn
        // còn khi trang đích render. Đổi sang redirect ở CASE 10 là nó mất.
        request.setAttribute("setBy", "ForwardServlet trước khi forward tới " + url);
        request.setAttribute("forwardedTo", url);

        /*
         * URL mà TRÌNH DUYỆT đã yêu cầu.
         *
         * Phải lưu lại ở đây, vì sau khi forward thì bên trong trang đích,
         * request.getRequestURI() trả về đường dẫn của ĐÍCH chứ không phải URL
         * gốc — Tomcat đã ghi đè nó khi đổi đích. Thanh địa chỉ thì vẫn giữ URL
         * gốc, nên hai giá trị đó khác nhau và dễ gây hiểu nhầm.
         *
         * Cách khác: đọc attribute "javax.servlet.forward.request_uri" mà
         * Tomcat tự đặt. Tự lưu như dưới đây thì rõ ràng hơn khi đang học.
         */
        request.setAttribute("originalURI", request.getRequestURI());

        /*
         * getRequestDispatcher(url) lấy về "cầu nối" tới tài nguyên đó,
         * forward(request, response) chuyển quyền xử lý sang nó.
         *
         * Có hai đường lấy dispatcher:
         *   getServletContext().getRequestDispatcher(url)  -> BẮT BUỘC "/" đầu
         *   request.getRequestDispatcher(url)              -> cho phép tương đối
         * Slide dùng cách đầu. Nên theo, vì đường dẫn tuyệt đối không phụ thuộc
         * servlet hiện tại đang được map ở URL nào.
         */
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);

        // Không có dòng code nào sau lệnh forward. Cố ý.
    }
}
