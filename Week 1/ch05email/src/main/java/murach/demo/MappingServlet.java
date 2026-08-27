package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 02 — Servlet mapping và URL pattern                (slide 7-9)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Lớp servlet chỉ là một lớp Java bình thường, không có gì trong đó nói URL
 *   nào sẽ chạy tới nó. Phải khai báo mapping ở ngoài.
 *
 * CÁCH DÙNG — trong WEB-INF/web.xml (slide 7)
 *   <servlet>
 *       <servlet-name>MappingServlet</servlet-name>
 *       <servlet-class>murach.demo.MappingServlet</servlet-class>
 *   </servlet>
 *   <servlet-mapping>
 *       <servlet-name>MappingServlet</servlet-name>
 *       <url-pattern>/mapping</url-pattern>
 *   </servlet-mapping>
 *
 *   <servlet-name> là chất keo: nó không phải URL, cũng không phải tên lớp,
 *   chỉ để nối hai khối trên với nhau trong phạm vi file web.xml.
 *
 * TẠI SAO PHẢI TÁCH LÀM HAI KHỐI
 *   Vì quan hệ là một-nhiều: MỘT servlet có thể nhận NHIỀU url-pattern. Servlet
 *   này được map hai lần — /mapping và /email/* — để bạn so sánh trực tiếp.
 *
 * BA KIỂU PATTERN (slide 9)
 *   /emailList   khớp đúng một URL đó thôi
 *   /email/*     khớp mọi URL bắt đầu bằng /email/ — phần đuôi rơi vào getPathInfo()
 *   *.do         khớp theo phần mở rộng (slide không nhắc, nhưng có tồn tại)
 *   Tomcat luôn ưu tiên pattern CỤ THỂ hơn: có cả /email/add và /email/* thì
 *   URL /email/add chạy vào cái đầu.
 * ========================================================================= */
public class MappingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Bốn method dưới đây cùng "cắt" một URL thành các mảnh khác nhau.
         * Với http://localhost:8081/ch05email/email/list/2024 :
         *
         *   getRequestURI()  = /ch05email/email/list/2024   (toàn bộ, trừ host)
         *   getContextPath() = /ch05email                   (tên ứng dụng)
         *   getServletPath() = /email                       (phần khớp pattern)
         *   getPathInfo()    = /list/2024                   (phần dấu * nuốt)
         *
         * getRequestURI = getContextPath + getServletPath + getPathInfo.
         */
        request.setAttribute("requestURI", request.getRequestURI());

        /*
         * getContextPath() quan trọng hơn vẻ ngoài của nó: ứng dụng có thể được
         * deploy dưới tên khác (/ch05email hôm nay, / ngày mai trên production).
         * Mọi link tuyệt đối trong JSP đều phải bắt đầu bằng nó, không thì đổi
         * tên deploy là gãy hết link. Trong JSP dùng ${pageContext.request.contextPath}.
         */
        request.setAttribute("contextPath", request.getContextPath());

        // Phần URL đã khớp <url-pattern>.
        request.setAttribute("servletPath", request.getServletPath());

        /*
         * getPathInfo() trả về NULL khi pattern là loại khớp chính xác — không
         * phải chuỗi rỗng. Đây là cách servlet biết nó được gọi qua pattern nào
         * trong nhiều pattern cùng trỏ về nó. Quên check null ở đây là NPE.
         *
         * Đây cũng chính là cơ chế để một servlet phục vụ /email/add,
         * /email/delete, /email/list — đọc pathInfo rồi rẽ nhánh, thay vì viết
         * ba servlet gần giống hệt nhau.
         */
        request.setAttribute("pathInfo", request.getPathInfo());

        // Tên khai trong <servlet-name>, KHÔNG phải tên lớp.
        request.setAttribute("servletName", getServletName());

        // Đưa dữ liệu cho JSP hiển thị — cơ chế forward xem CASE 09.
        getServletContext()
                .getRequestDispatcher("/demo/case02.jsp")
                .forward(request, response);
    }

    // Cho phép cả POST vào, để nút form trên trang demo cũng chạy được.
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
