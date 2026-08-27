package murach.email;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 01 — Servlet tự sinh HTML                          (slide 4-6)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Có lúc servlet không có JSP đứng sau mà vẫn phải trả về một trang. Nó phải
 *   tự dựng HTML và ghi thẳng vào response.
 *
 * CÁCH DÙNG
 *   response.setContentType("text/html; charset=UTF-8");
 *   PrintWriter out = response.getWriter();
 *   out.println("<h1>...</h1>");
 *
 * TẠI SAO TRONG THỰC TẾ GẦN NHƯ KHÔNG AI LÀM VẬY
 *   Slide 4 dạy cách này để bạn thấy servlet trả HTML thế nào, nhưng nó vi phạm
 *   đúng cái MVC mà chương 2 vừa dựng lên: HTML (tầng view) bị nhét vào file
 *   .java (tầng controller). Hệ quả:
 *     - sửa một chữ trong giao diện cũng phải biên dịch lại và restart server
 *     - không designer nào mở file .java để sửa layout
 *     - IDE không kiểm tra được HTML nằm trong chuỗi, gõ sai thẻ không ai báo
 *   Nên: dùng cách này cho response không phải HTML (JSON, CSV, file tải về),
 *   còn trả trang cho người xem thì forward sang JSP (CASE 09).
 *
 * MAP Ở ĐÂU
 *   Trong WEB-INF/web.xml, url-pattern = /test (slide 7). Cố ý map bằng
 *   web.xml chứ không phải annotation để CASE 02 và CASE 03 có cái so sánh.
 * ========================================================================= */
public class TestServlet extends HttpServlet {

    /*
     * doPost xử lý mọi request dùng method POST (slide 6).
     * "protected" chứ không phải "public": chỉ Tomcat gọi method này, không ai
     * khác cần thấy nó. HttpServlet khai báo protected nên override cũng để
     * protected — mở rộng lên public được, nhưng không có lý do gì để làm thế.
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * setContentType PHẢI gọi TRƯỚC getWriter().
         *
         * Lý do: getWriter() chốt luôn charset dùng để mã hoá ký tự. Gọi
         * setContentType sau đó thì header có thể đã gửi đi rồi, Tomcat lặng lẽ
         * bỏ qua, và trang ra tiếng Việt lỗi font mà không có lỗi nào cả.
         *
         * Slide 4 viết "text/html" không kèm charset. Thêm "; charset=UTF-8"
         * vào, không thì mặc định là ISO-8859-1 và mọi ký tự có dấu thành "?".
         */
        response.setContentType("text/html; charset=UTF-8");

        // getWriter() trả về luồng ký tự để ghi text.
        // (Ghi file nhị phân — ảnh, PDF — thì dùng getOutputStream(), và KHÔNG
        //  được gọi cả hai trên cùng một response, sẽ ném IllegalStateException.)
        PrintWriter out = response.getWriter();

        try {
            out.println("<!DOCTYPE html>");
            out.println("<html><head><meta charset='utf-8'>");
            out.println("<link rel='stylesheet' href='styles/main.css'>");
            out.println("<title>Case 01</title></head><body>");
            out.println("<h1>HTML from servlet</h1>");
            out.println("<p>Mọi dòng trên trang này do <code>out.println()</code> "
                    + "trong " + TestServlet.class.getName() + " sinh ra.</p>");

            // getMethod() trả "GET" hay "POST" — dùng để chứng minh cả hai
            // đường đều rơi vào đúng method này.
            out.println("<p>HTTP method đã dùng: <b>"
                    + request.getMethod() + "</b> "
                    + "(doGet gọi doPost nên cả hai cùng vào đây).</p>");
            out.println("<p><a href='demo/case01.jsp'>Quay lại case 01</a></p>");
            out.println("</body></html>");
        } finally {
            // finally: đóng writer kể cả khi ở trên ném exception giữa chừng.
            // Slide 4 viết đúng như vậy và đó là thói quen tốt.
            out.close();
        }
    }

    /*
     * doGet xử lý mọi request dùng method GET (slide 6).
     *
     * TẠI SAO doGet CHỈ GỌI doPost
     *   HttpServlet mặc định trả lỗi 405 Method Not Allowed cho method nào bạn
     *   không override. Nếu chỉ viết doPost thì gõ URL trên thanh địa chỉ (đó là
     *   một GET) sẽ ra 405 ngay. Viết logic một lần rồi cho method kia gọi sang
     *   là cách ngắn nhất để servlet nhận cả hai mà không nhân đôi code.
     *
     *   Xem CASE 04 để thấy đúng lỗi 405 đó, ở servlet cố tình chỉ có doGet.
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        doPost(request, response);
    }
}
