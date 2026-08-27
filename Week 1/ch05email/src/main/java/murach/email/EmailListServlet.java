package murach.email;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import murach.business.User;
import murach.data.UserIO;

/* ============================================================================
 * CASE 11 — Kiểm tra dữ liệu ở phía server                (slide 27-31)
 * ============================================================================
 * Đây là servlet chính của chương 5, và là nơi 4 case khác hợp lại:
 *   CASE 07  getRealPath()   — đổi đường dẫn tương đối thành đường dẫn thật
 *   CASE 09  forward()       — giao dữ liệu cho JSP
 *   CASE 12  init parameter  — lấy tên file từ web.xml
 *   CASE 16  log()           — ghi vết ra console và log file
 *
 * VẤN ĐỀ
 *   Ở chương 2, ba ô input có thuộc tính HTML5 "required" nên trình duyệt tự
 *   chặn khi bỏ trống. Nhưng đó là kiểm tra Ở MÁY NGƯỜI DÙNG. Ai cũng tắt được:
 *   mở DevTools xoá chữ required, hoặc gửi thẳng request bằng curl/Postman mà
 *   không cần mở form. Ở chương 5, các thuộc tính required bị bỏ đi và SERVLET
 *   phải tự kiểm tra.
 *
 *   Quy tắc chung: kiểm tra phía client là để trải nghiệm mượt (báo lỗi ngay,
 *   không phải chờ mạng). Kiểm tra phía server là để ĐÚNG. Cái đầu có thể bỏ,
 *   cái sau thì không bao giờ.
 *
 * MẪU "MỘT SERVLET, NHIỀU HÀNH ĐỘNG"
 *   Cả form join lẫn nút Return đều gửi vào /emailList, phân biệt nhau bằng
 *   tham số ẩn "action". Servlet đọc action rồi rẽ nhánh. Nhờ vậy một servlet
 *   điều phối được cả luồng, thay vì mỗi nút một servlet.
 *
 * CẤU TRÚC CỦA MỌI NHÁNH — luôn chỉ làm hai việc
 *   1. gán biến url = trang cần hiện
 *   2. setAttribute dữ liệu mà trang đó cần
 *   Không nhánh nào tự forward. Forward đúng một lần ở cuối method (xem CASE 09).
 *
 * KHÁC SLIDE MỘT CHỖ
 *   Slide gọi trang nhập là index.jsp. Ở đây index.jsp là trang mục lục 16 case,
 *   nên trang nhập đổi tên thành join.jsp. Ngoài cái tên ra, không khác gì.
 * ========================================================================= */
public class EmailListServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
                          throws ServletException, IOException {

        // Trước mọi getParameter() — nếu không, tên tiếng Việt thành dấu hỏi.
        // Xem giải thích đầy đủ ở CASE 05.
        request.setCharacterEncoding("UTF-8");

        // Giá trị mặc định, phòng khi không nhánh nào khớp. Không có dòng này
        // thì biến url có thể chưa được gán và code không biên dịch được.
        String url = "/join.jsp";

        /*
         * Đọc hành động hiện tại.
         * action đến từ thẻ input hidden name="action" value="add" trên form,
         * hoặc từ query string ?action=join của nút Return.
         */
        String action = request.getParameter("action");
        if (action == null) {
            action = "join";  // vào thẳng /emailList mà không kèm tham số
        }

        /*
         * So sánh bằng action.equals(...) chứ KHÔNG dùng toán tử ==.
         * Toán tử == so sánh địa chỉ object; với String lấy từ request thì nó
         * gần như luôn cho kết quả sai. Và đặt được action ở vế trái là nhờ đã
         * chặn null ngay phía trên.
         */
        if (action.equals("join")) {
            url = "/join.jsp";    // trang "join"
        }
        else if (action.equals("add")) {

            // ---- lấy tham số từ request -----------------------------------
            // Ba tên này khớp với thuộc tính name của ba thẻ input trên join.jsp.
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            String email = request.getParameter("email");

            // Ghi vết ra console (slide 52). Có kèm tên servlet và tên biến
            // đúng như slide 53 dặn, để khi nhiều servlet cùng in thì còn đọc
            // được. Chi tiết ở CASE 16.
            System.out.println("EmailListServlet email: " + email);

            // Gom dữ liệu vào object. Dựng User TRƯỚC khi kiểm tra, vì kể cả
            // khi dữ liệu sai ta vẫn cần trả nó về form để người dùng không
            // phải gõ lại từ đầu.
            User user = new User(firstName, lastName, email);

            // ---- kiểm tra dữ liệu (slide 31) -------------------------------
            String message;
            if (firstName == null || lastName == null ||
                email == null ||
                firstName.isEmpty() || lastName.isEmpty() ||
                email.isEmpty()) {

                /*
                 * TẠI SAO PHẢI KIỂM TRA CẢ null LẪN isEmpty()
                 *   null    = tham số không hề được gửi lên. Xảy ra khi ai đó
                 *             POST thẳng vào /emailList mà không qua form.
                 *   isEmpty = ô có được gửi nhưng người dùng bỏ trống.
                 *   Hai tình huống khác nhau, và cùng phải chặn.
                 *
                 * TẠI SAO THỨ TỰ PHẢI ĐỂ null TRƯỚC
                 *   Toán tử || có tính "short-circuit": vế trái đúng thì bỏ qua
                 *   vế phải. Nhờ vậy khi firstName là null, chương trình dừng
                 *   ngay ở vế đầu và không bao giờ chạy tới firstName.isEmpty().
                 *   Đảo thứ tự lại là dính NullPointerException ngay trên chính
                 *   dòng lẽ ra phải bảo vệ chương trình.
                 */
                message = "Please fill out all three text boxes.";
                url = "/join.jsp";   // quay lại form, KHÔNG lưu gì cả
            } else {
                message = "";        // rỗng để join.jsp không hiện dòng lỗi nào
                url = "/thanks.jsp";

                /*
                 * Đường dẫn file lấy qua hai bước, không viết cứng:
                 *   1. init parameter cho biết tên tương đối  (CASE 12, slide 40)
                 *   2. getRealPath đổi thành đường dẫn thật   (CASE 07, slide 19)
                 * Muốn đổi chỗ lưu file thì sửa web.xml, không phải sửa file này.
                 *
                 * Dùng getServletConfig() chứ không phải getServletContext():
                 * tham số này khai trong init-param của riêng servlet này. Gọi
                 * nhầm cái kia thì vẫn biên dịch được nhưng trả về null.
                 */
                String relativePath = this.getServletConfig()
                                          .getInitParameter("relativePathToFile");
                String path = this.getServletContext()
                                  .getRealPath(relativePath);
                try {
                    UserIO.add(user, path);

                    // Ghi vào log file của server (slide 55).
                    log("email=" + email);

                } catch (IOException e) {
                    /*
                     * Bắt lỗi ghi file, đúng như slide 56.
                     *
                     * Dạng log(message, exception) ghi kèm cả stack trace — đó
                     * là thứ cho biết lỗi ở dòng nào. Nếu chỉ log(e.getMessage())
                     * thì mất hết vết gọi, gần như vô dụng khi đi tìm bug.
                     *
                     * Và quan trọng: KHÔNG để exception bay lên trình duyệt.
                     * Người dùng thấy thông báo tử tế, còn chi tiết kỹ thuật
                     * nằm trong log — đúng tinh thần của CASE 13.
                     */
                    log("An IOException occurred.", e);
                    message = "Sorry, your entry could not be saved.";
                    url = "/join.jsp";
                }
            }

            /*
             * Đặt hai attribute cho JSP đọc bằng EL (CASE 08).
             * Đặt user kể cả khi dữ liệu sai — nhờ vậy join.jsp điền lại được
             * value cho từng ô và người dùng không mất công gõ lại.
             * Chi tiết nhỏ nhưng là khác biệt lớn về trải nghiệm.
             */
            request.setAttribute("user", user);
            request.setAttribute("message", message);
        }

        // Forward duy nhất một lần, ở cuối, tới trang mà các nhánh trên đã chọn.
        getServletContext()
                .getRequestDispatcher(url)
                .forward(request, response);
    }

    /*
     * Form join dùng POST (vì nó GHI dữ liệu — quy tắc ở CASE 05), còn nút
     * Return dùng GET (chỉ ĐỌC, chỉ hiện lại form). Cho doGet gọi doPost để cả
     * hai đi chung một luồng xử lý, giống CASE 01.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
                         throws ServletException, IOException {
        doPost(request, response);
    }
}
