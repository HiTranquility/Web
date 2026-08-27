package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 12 — Initialization parameter                      (slide 35-40)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Có những giá trị đổi theo nơi deploy: địa chỉ email chăm sóc khách hàng,
 *   đường dẫn file, khoá API, chuỗi kết nối database. Viết cứng vào .java thì
 *   mỗi lần đổi là biên dịch lại và đóng gói lại. Để trong web.xml thì chỉ cần
 *   sửa một dòng text rồi deploy lại.
 *
 * HAI PHẠM VI, HAI CÁCH KHAI (slide 35, 38)
 *
 *   context-param — dùng chung cho MỌI servlet trong ứng dụng
 *     <context-param>
 *         <param-name>custServEmail</param-name>
 *         <param-value>custserv@murach.com</param-value>
 *     </context-param>
 *
 *   init-param — chỉ RIÊNG servlet chứa nó, khai lồng trong thẻ servlet
 *     <servlet>
 *         <servlet-name>EmailListServlet</servlet-name>
 *         <servlet-class>murach.email.EmailListServlet</servlet-class>
 *         <init-param>
 *             <param-name>relativePathToFile</param-name>
 *             <param-value>/WEB-INF/EmailList.txt</param-value>
 *         </init-param>
 *     </servlet>
 *
 * HAI PHẠM VI THÌ ĐỌC BẰNG HAI OBJECT KHÁC NHAU (slide 39-40)
 *   getServletContext().getInitParameter(...)  -> đọc context-param
 *   getServletConfig().getInitParameter(...)   -> đọc init-param
 *
 *   Cả hai object đều kế thừa sẵn từ GenericServlet nên gọi thẳng được.
 *
 * CÁI BẪY LỚN NHẤT CỦA CASE NÀY
 *   Hai object khác nhau nhưng method TRÙNG TÊN, TRÙNG CHỮ KÝ. Gọi nhầm object
 *   thì trình biên dịch không hé răng một lời, IDE cũng không gạch đỏ, và lúc
 *   chạy thì nhận về null. Sau đó chương trình chết ở một chỗ khác hoàn toàn —
 *   thường là NullPointerException cách đó vài chục dòng — nên rất mất thời
 *   gian mới lần ra. Method doGet dưới đây cố tình gọi nhầm một lần để bạn
 *   thấy tận mắt.
 *
 *   Cách nhớ:  context = <context-param>,  config = <init-param>.
 *
 * TẠI SAO SERVLET NÀY MAP BẰNG web.xml CHỨ KHÔNG DÙNG ANNOTATION
 *   Vì init-param của nó nằm trong web.xml rồi. Đã phải khai thẻ <servlet>
 *   trong đó để chứa init-param thì khai luôn <servlet-mapping> cho gọn một
 *   chỗ, thay vì rải config ra hai nơi. (Vẫn có dạng annotation — xem cuối file.)
 * ========================================================================= */
public class InitParamServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * ---- ĐỌC ĐÚNG: context-param qua ServletContext (slide 40) --------
         * Mọi servlet trong ứng dụng đều đọc được giá trị này. Hợp cho những
         * thứ dùng chung: email hỗ trợ, tên công ty, chế độ bật/tắt tính năng.
         */
        String custServEmail = this.getServletContext()
                                   .getInitParameter("custServEmail");

        /*
         * ---- ĐỌC ĐÚNG: init-param qua ServletConfig (slide 40) ------------
         * Chỉ servlet này thấy. Hợp cho thứ riêng của một servlet — như đường
         * dẫn file mà chỉ nó ghi vào.
         */
        String relativePath = this.getServletConfig()
                                  .getInitParameter("relativePathToFile");

        /*
         * ---- ĐỌC SAI, CỐ Ý ------------------------------------------------
         * Cùng tên tham số, nhưng hỏi nhầm object: init-param của servlet thì
         * ServletContext không nhìn thấy. Kết quả là null, không có exception,
         * không có cảnh báo. Đây chính là cái bẫy nói ở đầu file.
         */
        String wrongScope = this.getServletContext()
                                .getInitParameter("relativePathToFile");

        request.setAttribute("custServEmail", custServEmail);
        request.setAttribute("relativePath", relativePath);
        request.setAttribute("wrongScopeIsNull", wrongScope == null);

        /*
         * Init parameter thường chỉ là NỬA câu trả lời: nó cho biết đường dẫn
         * tương đối, còn getRealPath() mới đổi ra đường dẫn thật (CASE 07).
         * Đây đúng là cặp đôi mà EmailListServlet dùng ở CASE 11.
         * Vẫn phải chặn null vì nếu ai đó xoá init-param trong web.xml thì
         * relativePath là null và getRealPath(null) sẽ ném exception.
         */
        request.setAttribute("resolvedPath",
                relativePath == null
                        ? null
                        : getServletContext().getRealPath(relativePath));

        getServletContext()
                .getRequestDispatcher("/demo/case12.jsp")
                .forward(request, response);
    }
}

/* ----------------------------------------------------------------------------
 * DẠNG ANNOTATION (slide 37) — chỉ để tham khảo, không dùng ở lớp này
 *
 *   @WebServlet(urlPatterns = {"/emailList"},
 *               initParams = {@WebInitParam(name  = "relativePathToFile",
 *                                           value = "/WEB-INF/EmailList.txt")})
 *
 * LƯU Ý: slide 37 viết tắt là @InitParam. Tên thật của annotation là
 * @WebInitParam (trong javax.servlet.annotation). Gõ đúng theo slide sẽ không
 * biên dịch được — sách in thiếu.
 *
 * VÀ ĐÂY CŨNG LÀ LÝ DO NÊN CÂN NHẮC: viết giá trị vào annotation thì nó nằm
 * trong file .class, muốn đổi phải biên dịch lại — tức là mất đúng cái lợi ích
 * chính của init parameter. Cần đổi được lúc deploy thì cứ để trong web.xml.
 * ------------------------------------------------------------------------- */
