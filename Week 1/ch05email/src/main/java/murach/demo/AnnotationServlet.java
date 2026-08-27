package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 03 — Map servlet bằng annotation @WebServlet       (slide 10-11)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Khai mapping trong web.xml nghĩa là mỗi servlet mới phải sửa hai chỗ, và
 *   URL nằm cách xa cái code xử lý nó. Từ Servlet 3.0 có annotation làm thay.
 *
 * CÁCH DÙNG — ba dạng ở slide 10-11
 *   @WebServlet("/test")                                   // gọn nhất
 *   @WebServlet(urlPatterns={"/emailList", "/email/*"})    // nhiều URL
 *   @WebServlet(name="MurachTestServlet", urlPatterns={"/test"})  // đặt tên
 *
 *   Lưu ý cú pháp: có ĐÚNG MỘT tham số thì viết @WebServlet("/test") được.
 *   Từ hai tham số trở lên bắt buộc gọi tên: urlPatterns={...}. Viết
 *   @WebServlet("/a", name="X") là lỗi biên dịch.
 *
 * TẠI SAO NÓ CHẠY ĐƯỢC MÀ KHÔNG CẦN KHAI GÌ THÊM
 *   Lúc khởi động, Tomcat quét mọi lớp trong WEB-INF/classes tìm annotation này
 *   rồi tự đăng ký. Nên lớp này KHÔNG xuất hiện một dòng nào trong web.xml.
 *   (Nếu web.xml có metadata-complete="true" thì Tomcat bỏ qua bước quét và
 *   annotation mất tác dụng hoàn toàn — một cái bẫy khó chẩn đoán.)
 *
 * KHI NÀO DÙNG CÁI NÀO
 *   Annotation: mapping thuần tuý. Ngắn hơn, và không bao giờ lệch với tên lớp.
 *   web.xml:    khi giá trị phải đổi được mà không biên dịch lại (CASE 12), và
 *               cho <error-page>, <context-param>, <session-config> — mấy thứ
 *               này không có annotation tương ứng.
 *   Dùng đồng thời cả hai trong một ứng dụng là bình thường — app này đang vậy.
 * ========================================================================= */
@WebServlet(
        // name = giá trị mà getServletName() trả về. Không khai thì mặc định là
        // tên lớp đầy đủ. Ở đây cố tình đặt khác tên lớp để trang demo chứng
        // minh được nó lấy từ annotation chứ không phải từ class.
        name = "MurachAnnotationServlet",

        // Hai pattern cho một servlet — đúng như dạng thứ hai ở slide 11.
        // /anno/* để bạn so sánh getPathInfo() với /annotation (xem CASE 02).
        urlPatterns = {"/annotation", "/anno/*"})
public class AnnotationServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Lấy từ thuộc tính name của annotation ở trên, không phải tên lớp.
        request.setAttribute("servletName", getServletName());

        // Cùng ý nghĩa như CASE 02, chỉ khác là mapping khai bằng annotation.
        request.setAttribute("servletPath", request.getServletPath());
        request.setAttribute("pathInfo", request.getPathInfo());

        getServletContext()
                .getRequestDispatcher("/demo/case03.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
