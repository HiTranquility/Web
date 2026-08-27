package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.User;
import murach.data.UserIO;

/* ============================================================================
 * CASE 16 — Ghi dữ liệu gỡ lỗi ra console và log file     (slide 52-58)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Servlet không chạy bằng cách bạn bấm Run rồi đi từng bước như một method
 *   main — SERVER gọi nó, không phải bạn. Nên cách đơn giản nhất để biết bên
 *   trong đang xảy ra chuyện gì là in ra.
 *
 * BA CÁCH, BA CHỖ ĐẾN KHÁC NHAU
 *
 *   System.out.println(msg)     -> console của servlet engine        (slide 52)
 *   log(msg)                    -> log file của server               (slide 55)
 *   log(msg, throwable)         -> log file, kèm nguyên stack trace  (slide 56)
 *
 *   Hai method log() là của lớp HttpServlet (slide 54), kế thừa sẵn nên gọi
 *   thẳng, không cần khai báo gì. Bên trong nó gọi ServletContext.log().
 *
 * KHÁC NHAU THẾ NÀO
 *   System.out  : nhanh, tiện, nhưng không tự ghi thời gian và KHÔNG tự ghi
 *                 servlet nào in ra. Đóng terminal là mất. Đó là lý do slide 53
 *                 dặn phải TỰ ghi kèm tên servlet và tên biến — nếu không, khi
 *                 năm servlet cùng in thì nhìn vào không hiểu dòng nào của ai.
 *   log()       : server tự thêm thời gian và tên servlet. Ghi vào file nên
 *                 còn lại sau khi tắt máy. Trên Tomcat thật là
 *                 <tomcat>/logs/localhost.yyyy-mm-dd.log (slide 57).
 *   log(msg, t) : như trên, cộng thêm stack trace — thứ duy nhất cho biết lỗi
 *                 xảy ra ở DÒNG NÀO.
 *
 * ĐỌC STACK TRACE THẾ NÀO (slide 56, 58)
 *   Stack trace là chuỗi các lời gọi method dẫn tới chỗ lỗi. Ví dụ của sách:
 *       java.io.FileNotFoundException: ...EmailList.txt (Access is denied)
 *           at java.io.FileOutputStream.openAppend(Native Method)
 *           ...
 *           at murach.data.UserIO.add(UserIO.java:11)
 *           at murach.email.EmailListServlet.doPost(EmailListServlet.java:38)
 *
 *   Mẹo đọc: dòng ĐẦU TIÊN cho biết lỗi gì và vì sao ("Access is denied").
 *   Rồi dò từ trên xuống, tìm dòng đầu tiên thuộc package CỦA BẠN — ở đây là
 *   UserIO.java dòng 11. Đó gần như luôn là chỗ cần sửa. Mấy dòng java.io.*
 *   phía trên chỉ là ruột của thư viện JDK, không phải lỗi của bạn.
 *
 * TRONG DỰ ÁN THẬT THÌ SAO
 *   Không ai rải System.out.println vào code production: nó tốn thời gian ở mọi
 *   request, làm rác log của người khác, và chỉ cần một câu vô ý kiểu
 *   println("password: " + pwd) là mật khẩu nằm chình ình trong file mà người
 *   khác đọc được. Dự án thật dùng thư viện logging (SLF4J/Logback, Log4j2) có
 *   phân cấp DEBUG/INFO/WARN/ERROR, để tắt bật được mà không sửa code.
 *   Nhưng nguyên tắc đọc log thì vẫn y hệt những gì chương này dạy.
 * ========================================================================= */
@WebServlet("/debug")
public class DebugServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String email = request.getParameter("email");
        if (email == null) {
            email = "jsmith@gmail.com";   // giá trị mẫu của slide 52
        }

        /*
         * ---- CÁCH 1: ra console (slide 52) --------------------------------
         * Có kèm tên servlet ("DebugServlet") và tên biến ("email") đúng như
         * slide 53 dặn. So sánh hai dòng này thì thấy ngay vì sao:
         *     println(email);                    -> "vy@example.com"  (của ai?)
         *     println("DebugServlet email: "+e); -> đọc là hiểu ngay
         */
        System.out.println("DebugServlet email: " + email);

        /*
         * ---- CÁCH 2: ra log file (slide 55) -------------------------------
         * Không cần tự ghi tên servlet ở đây — server tự thêm vào, ra dạng:
         *     INFO: murach.demo.DebugServlet: email=vy@example.com
         * (Chạy embedded Tomcat như dự án này thì log cũng đổ ra terminal luôn.)
         */
        log("email=" + email);

        /*
         * ---- CÁCH 3: ra log file kèm stack trace (slide 56) ---------------
         * Chỉ chạy khi có tham số throwIt=yes, để bạn chủ động kích hoạt.
         */
        String stackTraceMessage = null;
        if ("yes".equals(request.getParameter("throwIt"))) {
            try {
                // Ổ Z: không tồn tại nên lệnh này chắc chắn thất bại — cần một
                // lỗi THẬT thì stack trace mới thật.
                UserIO.add(new User("John", "Smith", email),
                        "Z:\\nowhere\\EmailList.txt");

            } catch (Exception e) {
                /*
                 * Đây là dạng hai tham số của slide 56.
                 *
                 * So sánh với việc chỉ ghi log(e.getMessage()): bạn sẽ biết
                 * "Access is denied" nhưng KHÔNG biết dòng nào gây ra, gọi từ
                 * đâu tới. Truyền cả object exception vào thì có đủ chuỗi gọi.
                 *
                 * Và tuyệt đối đừng viết catch rỗng {} — lỗi biến mất không dấu
                 * vết, đó là cách chắc chắn nhất để sau này mất nguyên buổi đi
                 * tìm một bug lẽ ra đã tự khai báo.
                 */
                log("An IOException occurred.", e);

                // Giữ lại thông điệp để trang demo hiện ra, còn stack trace đầy
                // đủ thì nằm trong log — người dùng không cần thấy (CASE 13).
                stackTraceMessage = e.getClass().getName() + ": " + e.getMessage();
            }
        }

        request.setAttribute("email", email);
        request.setAttribute("consoleLine", "DebugServlet email: " + email);
        request.setAttribute("logLine", "email=" + email);
        request.setAttribute("stackTraceMessage", stackTraceMessage);

        getServletContext()
                .getRequestDispatcher("/demo/case16.jsp")
                .forward(request, response);
    }
}
