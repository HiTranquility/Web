package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 10 — Redirect response bằng sendRedirect()         (slide 25-26)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Forward chạy hoàn toàn trong server nên không ra khỏi ứng dụng được, và để
 *   lại thanh địa chỉ không khớp với trang đang xem. Redirect giải quyết cả hai.
 *
 * CÁCH DÙNG (slide 26 — ba kiểu đường dẫn)
 *   response.sendRedirect("email");                        // tương đối thư mục hiện tại
 *   response.sendRedirect("/musicStore/email/");           // tương đối servlet engine
 *   response.sendRedirect("http://www.murach.com/email/"); // sang server khác
 *
 * REDIRECT LÀM GÌ Ở BÊN TRONG — KHÁC HẲN FORWARD
 *   Server KHÔNG chạy trang mới. Nó chỉ trả về mã 302 kèm header Location, rồi
 *   TRÌNH DUYỆT tự gửi một request THỨ HAI tới địa chỉ đó. Tức là hai vòng đi
 *   về, và hai object request hoàn toàn khác nhau.
 *
 *   Mọi khác biệt còn lại đều suy ra từ đúng câu trên:
 *     - Thanh địa chỉ đổi         -> vì trình duyệt thật sự đi tới URL mới
 *     - Attribute mất sạch        -> vì request thứ hai là object mới tinh
 *     - Đi được tới server khác   -> vì trình duyệt mới là bên đi, không phải server
 *     - Chậm hơn forward          -> hai vòng đi về thay vì một
 *
 * KHI NÀO DÙNG CÁI NÀO
 *   forward      : giao dữ liệu cho JSP trong cùng ứng dụng. Mặc định của MVC.
 *   sendRedirect : sau một POST đã ghi dữ liệu, và khi cần đi ra ngoài.
 *
 * MẪU POST/REDIRECT/GET — lý do thực tế quan trọng nhất của redirect
 *   POST xong mà forward thẳng sang trang kết quả thì URL vẫn là URL của POST.
 *   Người dùng bấm F5 -> trình duyệt hỏi "gửi lại biểu mẫu?" -> bấm OK là đơn
 *   hàng được đặt hai lần. Kết thúc bằng redirect thì request cuối cùng trong
 *   lịch sử là một GET vô hại, F5 chỉ tải lại trang, không ghi gì thêm.
 *
 * LƯU Ý KHI VIẾT CODE
 *   sendRedirect không dừng method. Các dòng phía sau vẫn chạy tiếp, và nếu
 *   chúng đụng vào response thì ném IllegalStateException. Gọi xong thì return
 *   ngay, hoặc để nó là lệnh cuối cùng như trong lớp này.
 * ========================================================================= */
@WebServlet("/redirect")
public class RedirectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Đặt attribute này để chứng minh nó SẼ MẤT.
         * So với CASE 09: cùng một dòng lệnh, forward thì còn, redirect thì
         * không — vì trang đích được nạp bởi một request khác hoàn toàn.
         */
        request.setAttribute("setBy", "RedirectServlet trước khi redirect");

        String target = request.getParameter("target");
        if (target == null) {
            target = "relative";
        }

        if (target.equals("contextRelative")) {
            /*
             * Kiểu 2 của slide 26 — đường dẫn bắt đầu bằng "/", tính từ gốc
             * servlet engine (không phải gốc ứng dụng!).
             *
             * Slide viết cứng "/musicStore/email/". Đừng bắt chước chỗ đó:
             * "musicStore" là tên ứng dụng lúc deploy, đổi tên là gãy. Ghép
             * getContextPath() vào như dưới đây thì deploy tên gì cũng đúng.
             */
            response.sendRedirect(request.getContextPath()
                    + "/demo/case10.jsp?via=contextRelative");

        } else if (target.equals("external")) {
            /*
             * Kiểu 3 của slide 26 — URL đầy đủ sang server khác.
             * Đây là việc forward không bao giờ làm được.
             *
             * BẢO MẬT: đừng bao giờ lấy thẳng tham số của người dùng làm đích
             * redirect (sendRedirect(request.getParameter("url"))). Đó là lỗ
             * hổng "open redirect" — kẻ xấu gửi link trông như của web bạn
             * nhưng đá nạn nhân sang trang giả mạo. Ở đây đích là chuỗi cứng.
             */
            response.sendRedirect("https://www.murach.com/");

        } else {
            /*
             * Kiểu 1 của slide 26 — không có "/" ở đầu, tính tương đối so với
             * thư mục của URL hiện tại. Servlet đang ở /ch05email/redirect nên
             * thư mục là /ch05email/, cộng thêm "demo/case10.jsp" thành
             * /ch05email/demo/case10.jsp.
             *
             * Kiểu này ngắn nhưng dễ gãy nhất: chỉ cần đổi url-pattern của
             * servlet sang /admin/redirect là đích lệch sang /ch05email/admin/.
             * Trong code thật, ưu tiên kiểu 2 ở trên.
             */
            response.sendRedirect("demo/case10.jsp?via=relative");
        }

        // Không viết gì thêm sau sendRedirect — xem "LƯU Ý KHI VIẾT CODE" ở trên.
    }
}
