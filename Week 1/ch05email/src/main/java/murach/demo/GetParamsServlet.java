package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 04 — Truyền tham số bằng HTTP GET                  (slide 12-13)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Request GET mang tham số trong query string — phần sau dấu "?" của URL.
 *   Có ba cách tạo ra nó, và servlet KHÔNG phân biệt được cách nào, đó mới là
 *   điều đáng nhớ: với servlet, cả ba đều y hệt nhau.
 *
 * BA CÁCH TẠO REQUEST GET (slide 13)
 *
 *   1. Gõ thẳng URL vào thanh địa chỉ
 *      http://localhost:8081/ch05email/emailList?action=add&firstName=John
 *
 *   2. Form dùng method GET
 *      <form action="emailList">                  <!-- không ghi method thì
 *      <form action="emailList" method="get">          mặc định LÀ get -->
 *
 *   3. Thẻ anchor
 *      <a href="emailList?action=join">Display Email Entry Test</a>
 *
 * CÚ PHÁP QUERY STRING
 *   ?ten=giatri              tham số đầu tiên mở bằng dấu ?
 *   &ten2=giatri2            các tham số sau nối bằng &
 *   Ký tự đặc biệt phải mã hoá: dấu cách thành %20 hoặc +, "@" thành %40...
 *   Trình duyệt tự làm việc này khi submit form; tự nối chuỗi bằng tay thì
 *   phải tự gọi URLEncoder.encode(), không thì tham số có dấu cách sẽ đứt.
 *
 * TẠI SAO LỚP NÀY CHỈ CÓ doGet, KHÔNG CÓ doPost
 *   Cố ý. Slide 13 ghi "servlet phải cài đặt doGet để xử lý request GET" —
 *   và điều ngược lại cũng đúng. Không override doPost thì HttpServlet dùng
 *   bản mặc định của nó, tức là trả về 405 Method Not Allowed.
 *   Trang demo có sẵn nút POST vào đây để bạn thấy đúng lỗi 405 đó.
 *   So sánh với CASE 01, nơi doGet gọi doPost để nhận được cả hai.
 * ========================================================================= */
@WebServlet("/getDemo")
public class GetParamsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * getParameter(ten) đọc tham số theo TÊN, không theo thứ tự.
         * Tên đó đến từ đâu:
         *   - form  -> thuộc tính name của thẻ input
         *   - URL   -> phần trước dấu = trong query string
         * Phân biệt hoa thường: "firstName" khác "firstname".
         *
         * Trả về null nếu tham số không có mặt (xem kỹ hơn ở CASE 06).
         */
        request.setAttribute("action", request.getParameter("action"));
        request.setAttribute("firstName", request.getParameter("firstName"));
        request.setAttribute("lastName", request.getParameter("lastName"));

        /*
         * getQueryString() trả nguyên chuỗi thô sau dấu "?", chưa tách, chưa
         * giải mã. Hầu như không bao giờ dùng để đọc dữ liệu — getParameter()
         * đã tách và giải mã sẵn rồi. Ở đây chỉ để trang demo cho bạn thấy
         * tham số nằm ở đâu, và để đối chiếu với CASE 05 (POST thì cái này null).
         */
        request.setAttribute("queryString", request.getQueryString());
        request.setAttribute("method", request.getMethod());

        getServletContext()
                .getRequestDispatcher("/demo/case04.jsp")
                .forward(request, response);
    }

    /*
     * KHÔNG override doPost — đó là chủ ý, không phải thiếu sót.
     * Xem phần "TẠI SAO" ở khối chú thích đầu file.
     */
}
