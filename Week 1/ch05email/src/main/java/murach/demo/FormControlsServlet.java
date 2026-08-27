package murach.demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 06 — getParameter so với getParameterValues        (slide 16-17)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Ba loại control trên form gửi dữ liệu theo ba kiểu khác nhau:
 *     - text box       : một giá trị
 *     - check box      : CÓ giá trị nếu được tick, KHÔNG GỬI GÌ nếu không tick
 *     - list box multi : nhiều giá trị dưới cùng một tên
 *   Một method không thể phục vụ cả ba, nên API có hai.
 *
 * CÁCH DÙNG (slide 16)
 *   getParameter(ten)        -> String,   hoặc null nếu tham số không có
 *   getParameterValues(ten)  -> String[], hoặc null nếu tham số không có
 *
 * QUY TẮC CHUNG CHO CẢ HAI, VÀ LÀ CHỖ HAY SAI NHẤT
 *   "Không có tham số" luôn là null — KHÔNG PHẢI chuỗi rỗng, KHÔNG PHẢI mảng
 *   rỗng. Trình duyệt không gửi tham số cho control mà nó coi là "không có giá
 *   trị". Cho nên:
 *
 *     - Check box không tick: trình duyệt không gửi tên đó đi. Nó KHÔNG gửi
 *       "off", không gửi "false", không gửi chuỗi rỗng — nó không gửi gì hết.
 *       Nên cách duy nhất để biết là so sánh với null.
 *
 *     - List box không chọn gì: getParameterValues trả null, không phải
 *       String[0]. Code ở slide 17 viết thẳng for-each mà không check null —
 *       gặp trường hợp này là NullPointerException. Xem chỗ sửa bên dưới.
 *
 *   Ngược lại, text box để trống thì VẪN được gửi, với giá trị là chuỗi rỗng.
 *   Nên text box trống -> "" (không null), còn check box không tick -> null.
 *   Hai trường hợp khác nhau, phải xử lý khác nhau. Đây là lý do CASE 11 phải
 *   kiểm tra CẢ null LẪN isEmpty().
 * ========================================================================= */
@WebServlet("/controls")
public class FormControlsServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Trước mọi getParameter() — xem giải thích ở CASE 05.
        request.setCharacterEncoding("UTF-8");

        // ---- text box: một giá trị -----------------------------------------
        // Gõ gì được nấy. Để trống thì là "" chứ không phải null.
        String firstName = request.getParameter("firstName");

        // ---- check box: có tick thì có giá trị, không tick thì null ---------
        // Giá trị trả về là thuộc tính value của thẻ input. HTML không ghi
        // value thì trình duyệt gửi "on" — đó là mặc định của chuẩn HTML, không
        // phải của servlet. Nên đừng bao giờ so sánh với chuỗi "on", chỉ cần
        // biết nó khác null là được, đúng như slide 17 viết.
        String rockCheckBox = request.getParameter("rock");
        boolean rockChecked = rockCheckBox != null;

        // ---- list box nhiều lựa chọn: mảng giá trị --------------------------
        // Cùng name="country", chọn 3 mục thì mảng có 3 phần tử.
        // (Dùng getParameter() ở đây cũng chạy, nhưng chỉ lấy được giá trị ĐẦU
        //  TIÊN và im lặng bỏ mất phần còn lại — một bug rất khó thấy.)
        String[] selectedCountries = request.getParameterValues("country");

        /*
         * ĐÂY LÀ CHỖ SỬA LẠI SO VỚI SLIDE 17.
         * Slide viết:
         *     for (String country : selectedCountries) { ... }
         * Không chọn gì -> selectedCountries là null -> NullPointerException.
         *
         * Đổi null thành list rỗng ngay tại biên như dưới đây thì mọi code phía
         * sau khỏi phải nhớ check null nữa — vòng lặp trên list rỗng chạy 0 lần,
         * hoàn toàn an toàn. Đây là mẹo chung: khử null càng sớm càng tốt.
         */
        List<String> countries = selectedCountries == null
                ? Collections.<String>emptyList()
                : Arrays.asList(selectedCountries);

        request.setAttribute("firstName", firstName);
        request.setAttribute("rockCheckBox", rockCheckBox);
        request.setAttribute("rockChecked", rockChecked);
        request.setAttribute("countries", countries);

        // Giữ lại thông tin "ban đầu nó có null không" để trang demo nói được
        // sự thật, thay vì chỉ khoe cái list rỗng đã xử lý xong.
        request.setAttribute("countriesWasNull", selectedCountries == null);
        request.setAttribute("submitted", true);

        getServletContext()
                .getRequestDispatcher("/demo/case06.jsp")
                .forward(request, response);
    }

    // Vào trang lần đầu bằng GET: chưa có dữ liệu, chỉ hiện form trống.
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        getServletContext()
                .getRequestDispatcher("/demo/case06.jsp")
                .forward(request, response);
    }
}
