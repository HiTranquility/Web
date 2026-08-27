package murach.email;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.User;

/* ============================================================================
 * Servlet điều khiển cho chương 6                       (slide 7, 8, 28)
 * ============================================================================
 * Chương 6 nói về JSP, nên servlet ở đây rất mỏng — nó chỉ có một việc:
 * ĐẶT dữ liệu vào request rồi forward sang JSP. Toàn bộ phần đáng học nằm
 * trong các file .jsp.
 *
 * Servlet này cung cấp hai attribute mà slide 7 và 8 dùng làm ví dụ:
 *   currentYear  -> ${currentYear}        (slide 7: hiển thị attribute)
 *   user         -> ${user.firstName}     (slide 8: hiển thị property)
 * ========================================================================= */
@WebServlet("/emailList")
public class EmailListServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String url = "/index.jsp";

        String action = request.getParameter("action");
        if (action == null) {
            action = "join";
        }

        if (action.equals("join")) {
            url = "/index.jsp";
        }
        else if (action.equals("add")) {
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            User user = new User(firstName, lastName, email);

            // Kiểm tra dữ liệu (đã học ở chương 5). message rỗng khi hợp lệ —
            // nhờ vậy <c:if test="${message != null}"> ở slide 12/28 mới có
            // cái để kiểm tra.
            String message;
            if (firstName == null || lastName == null || email == null ||
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty()) {
                message = "Vui lòng điền đủ cả ba ô.";
                url = "/index.jsp";
            } else {
                message = null;     // null chứ không phải "" — xem ghi chú dưới
                url = "/thanks.jsp";
            }

            /*
             * VÌ SAO message = null CHỨ KHÔNG PHẢI ""
             * Slide 12 viết  <c:if test="${message != null}">  — kiểm tra null,
             * không kiểm tra rỗng. Nếu đặt "" thì điều kiện vẫn ĐÚNG và trang
             * sẽ hiện một dòng <p><i></i></p> trống, đẩy layout xuống một chút
             * mà không ai hiểu vì sao.
             *
             * (Cách an toàn hơn là dùng ${not empty message} — bắt cả null lẫn
             *  chuỗi rỗng. Nhưng ở đây giữ đúng như slide để bạn đối chiếu.)
             */
            request.setAttribute("user", user);
            request.setAttribute("message", message);
        }

        // ---- slide 7: đặt một attribute kiểu số ---------------------------
        // JSP sẽ hiển thị bằng ${currentYear}
        GregorianCalendar currentDate = new GregorianCalendar();
        int currentYear = currentDate.get(Calendar.YEAR);
        request.setAttribute("currentYear", currentYear);

        getServletContext().getRequestDispatcher(url)
                           .forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {
        doPost(request, response);
    }
}
