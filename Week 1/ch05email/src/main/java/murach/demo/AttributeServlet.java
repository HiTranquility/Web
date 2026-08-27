package murach.demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.User;

/* ============================================================================
 * CASE 08 — Attribute của request                         (slide 21-22)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Servlet đã dựng xong object User. JSP cần hiển thị nó. Nhưng JSP không nên
 *   chứa code Java, và hai bên là hai file khác nhau — không gọi hàm cho nhau
 *   được. Attribute chính là chỗ bàn giao: servlet ĐẶT object vào request,
 *   JSP LẤY ra bằng EL.
 *
 * CÁCH DÙNG (slide 22)
 *   // trong servlet
 *   User user = new User(firstName, lastName, email);
 *   request.setAttribute("user", user);
 *
 *   // đọc lại trong Java — trả về Object nên phải ép kiểu
 *   User user = (User) request.getAttribute("user");
 *
 *   // trong JSP thì không cần ép kiểu, EL tự lo
 *   ${user.email}
 *
 * PHÂN BIỆT ATTRIBUTE VỚI PARAMETER — hai thứ hoàn toàn khác nhau
 *   getParameter()  : dữ liệu do TRÌNH DUYỆT gửi lên. Luôn là String. Chỉ đọc.
 *   getAttribute()  : dữ liệu do CODE CỦA BẠN đặt vào. Là Object bất kỳ.
 *   Người mới hay nhầm hai cái này vì tên gần giống nhau.
 *
 * "ATTRIBUTE RESET GIỮA CÁC REQUEST" (slide 21) NGHĨA LÀ GÌ
 *   Mỗi request HTTP là một object HttpServletRequest mới toanh. F5 một cái là
 *   object cũ bị vứt, attribute trong đó biến mất. Nên attribute chỉ sống sót
 *   qua forward (CASE 09) — vì forward dùng LẠI đúng object đó — chứ không sống
 *   qua redirect (CASE 10) và càng không sống qua lần F5 kế tiếp.
 *   Muốn giữ dữ liệu lâu hơn thì cần session, đó là chương 7.
 *   Method doGet dưới đây kiểm tra điều này bằng cách đọc thử trước khi ghi.
 * ========================================================================= */
@WebServlet("/attributes")
public class AttributeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        /*
         * Chứng minh attribute KHÔNG sống qua request trước.
         * Đọc trước khi ghi bất cứ thứ gì. Dù bạn F5 bao nhiêu lần, biến này
         * vẫn null — vì mỗi lần F5 là một request object hoàn toàn mới.
         */
        Object leftover = request.getAttribute("user");
        request.setAttribute("leftoverWasNull", leftover == null);

        // ---- đặt một attribute kiểu object --------------------------------
        // Tên attribute là chuỗi tuỳ ý bạn đặt. "user" ở đây khớp với ${user...}
        // bên JSP — gõ sai một chữ là JSP im lặng in ra rỗng, không báo lỗi.
        User user = new User("John", "Smith", "jsmith@gmail.com");
        request.setAttribute("user", user);

        /*
         * Lấy lại trong Java thì PHẢI ép kiểu, vì getAttribute() khai báo trả
         * về Object — API không thể biết bạn đã bỏ kiểu gì vào.
         * Ép sai kiểu -> ClassCastException lúc chạy, biên dịch vẫn qua.
         */
        User sameUser = (User) request.getAttribute("user");

        // So sánh bằng == (so địa chỉ, không phải nội dung): chứng minh lấy ra
        // đúng CÙNG MỘT object chứ không phải bản sao. Attribute không copy gì cả.
        request.setAttribute("castWorked", sameUser == user);

        // ---- đặt attribute cho kiểu nguyên thuỷ ---------------------------
        /*
         * setAttribute nhận Object nên int phải được "đóng hộp" thành Integer.
         * Slide 22 viết "new Integer(id)". Cách đó đã deprecated từ Java 9 và
         * bị xoá ở các bản mới — dùng Integer.valueOf() thay thế, vừa đúng
         * chuẩn hiện tại vừa tái sử dụng cache cho các số nhỏ.
         * (Thực ra chỉ viết setAttribute("id", id) cũng được, Java tự autobox.)
         */
        int id = 1;
        request.setAttribute("id", Integer.valueOf(id));

        // Chiều ngược lại: ép về Integer rồi Java tự "mở hộp" thành int.
        // Nếu attribute là null thì dòng này ném NullPointerException lúc mở
        // hộp — một chỗ rất dễ sập mà nhìn code không thấy dấu hiệu gì.
        int idBack = (Integer) request.getAttribute("id");
        request.setAttribute("idBack", idBack);

        // Attribute chưa từng đặt -> null (slide 21). Không ném exception.
        request.setAttribute("missingIsNull",
                request.getAttribute("noSuchAttribute") == null);

        getServletContext()
                .getRequestDispatcher("/demo/case08.jsp")
                .forward(request, response);
    }
}
