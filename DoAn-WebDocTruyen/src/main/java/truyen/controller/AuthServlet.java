package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.security.SecureRandom;
import java.util.Base64;

import truyen.dao.PasswordResetDAO;
import truyen.dao.UserDAO;
import truyen.model.User;
import truyen.util.PasswordUtil;

/**
 * CASE 01 — Đăng ký / Đăng nhập / Đăng xuất.
 *
 * URL:  /auth?action=login | register | logout | forgot | reset
 *
 * TRANG 19 · 20 · 21 · 22 — cả bốn trang xác thực nằm trong servlet này vì
 * chúng dùng chung layout `auth` và chung một quy trình: nhận form, kiểm tra,
 * hoặc trả lại form kèm lỗi, hoặc chuyển hướng đi.
 *
 * Viết theo đúng khuôn servlet ở docs/standards/01-CODING_CONVENTIONS.md §3:
 * mỗi action một method private, mỗi method chỉ TRẢ VỀ đường dẫn mảnh nội
 * dung, forward đúng một lần ở cuối.
 *
 * Servlet này dùng layout `auth` chứ không phải `main` — trang đăng nhập không
 * có thanh menu (người chưa đăng nhập thì menu để làm gì).
 */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private UserDAO userDAO;
    private PasswordResetDAO resetDAO;

    /*
     * SecureRandom, KHÔNG phải Random.
     *
     * java.util.Random sinh số từ một hạt giống có thể đoán được — biết vài
     * giá trị đầu là suy ra được cả dãy. Với token đặt lại mật khẩu thì đó là
     * lỗ hổng: đoán được token là đổi được mật khẩu người khác.
     *
     * SecureRandom lấy entropy từ hệ điều hành. Chậm hơn một chút, nhưng
     * "chậm hơn một chút" ở đây nghĩa là vài micro-giây mỗi lần quên mật khẩu.
     */
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        resetDAO = new PasswordResetDAO();
    }

    // GET: hiện form. POST: xử lý form. Cả hai vào chung handle().
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handle(request, response);
    }

    private void handle(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Trước mọi getParameter() — nếu không, tên tiếng Việt thành dấu hỏi
        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "login";
        }

        String url;
        try {
            switch (action) {
                case "logout":
                    logout(request, response);
                    return;                       // đã redirect, KHÔNG forward nữa
                case "register":
                    url = register(request, response);
                    break;
                case "forgot":
                    url = forgot(request);
                    break;
                case "reset":
                    url = reset(request, response);
                    break;
                default:
                    url = login(request, response);
                    break;
            }
        } catch (SQLException e) {
            log("AuthServlet: lỗi truy vấn khi action=" + action, e);
            request.setAttribute("message", "Hệ thống đang bận, vui lòng thử lại.");
            url = "/WEB-INF/views/auth/login.jsp";
        }

        // url là null nghĩa là method con đã redirect xong -> không forward nữa
        if (url == null) {
            return;
        }

        request.setAttribute("contentPage", url);
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/auth.jsp")
                .forward(request, response);
    }

    // ---- ĐĂNG NHẬP ---------------------------------------------------------

    private String login(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        request.setAttribute("pageTitle", "Đăng nhập");

        // GET = chỉ hiện form, chưa có gì để xử lý
        if (!"POST".equals(request.getMethod())) {
            return "/WEB-INF/views/auth/login.jsp";
        }

        String username = trim(request.getParameter("username"));
        String password = request.getParameter("password");

        if (username.isEmpty() || password == null || password.isEmpty()) {
            request.setAttribute("message", "Vui lòng nhập đủ tên đăng nhập và mật khẩu.");
            request.setAttribute("username", username);   // giữ lại chữ đã gõ
            return "/WEB-INF/views/auth/login.jsp";
        }

        User user = userDAO.findByUsername(username);

        /*
         * MỘT THÔNG BÁO CHUNG CHO CẢ HAI TRƯỜNG HỢP SAI.
         *
         * Sai tên và sai mật khẩu đều báo "Tên đăng nhập hoặc mật khẩu không
         * đúng" — KHÔNG tách thành "tên này không tồn tại" / "sai mật khẩu".
         *
         * Vì tách ra là tự tay xác nhận cho kẻ tấn công biết tài khoản nào CÓ
         * thật. Nó dò được danh sách username hợp lệ rồi mới tập trung dò mật
         * khẩu. Gọi là "user enumeration".
         */
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            request.setAttribute("message", "Tên đăng nhập hoặc mật khẩu không đúng.");
            request.setAttribute("username", username);
            return "/WEB-INF/views/auth/login.jsp";
        }

        // Bị ban thì chặn đăng nhập, nhưng truyện của họ vẫn còn trên web
        if (user.isBanned()) {
            String reason = (user.getBanReason() == null || user.getBanReason().isEmpty())
                    ? "" : " Lý do: " + user.getBanReason();
            request.setAttribute("message", "Tài khoản đã bị khoá." + reason);
            return "/WEB-INF/views/auth/login.jsp";
        }

        /*
         * ĐỔI ID PHIÊN NGAY TRƯỚC KHI ĐĂNG NHẬP THÀNH CÔNG.
         *
         * Chống "session fixation": kẻ tấn công ép nạn nhân dùng một
         * JSESSIONID mà hắn biết trước, chờ nạn nhân đăng nhập, rồi dùng chính
         * id đó để vào tài khoản. Tạo id mới lúc này là vô hiệu hoá cái cũ.
         *
         * Hai dòng, và nó chặn đứng cả một lớp tấn công.
         */
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);

        // KHÔNG mang chuỗi băm vào session — không có lý do gì để nó ở đó
        user.setPasswordHash(null);
        session.setAttribute("currentUser", user);

        /*
         * REDIRECT chứ không forward.
         * Mẫu Post/Redirect/Get: sau khi POST đã làm thay đổi trạng thái, kết
         * thúc bằng redirect thì request cuối trong lịch sử là một GET vô hại.
         * F5 chỉ tải lại trang, không gửi lại form đăng nhập.
         */
        response.sendRedirect(request.getContextPath() + "/");
        return null;
    }

    // ---- ĐĂNG KÝ -----------------------------------------------------------

    private String register(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        request.setAttribute("pageTitle", "Đăng ký");

        if (!"POST".equals(request.getMethod())) {
            return "/WEB-INF/views/auth/register.jsp";
        }

        String username = trim(request.getParameter("username"));
        String email    = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirm  = request.getParameter("confirm");
        String agree    = request.getParameter("agree");

        // Giữ lại chữ đã gõ để lỗi thì không phải nhập lại từ đầu
        request.setAttribute("username", username);
        request.setAttribute("email", email);

        String message = validateRegister(username, email, password, confirm, agree);
        if (message == null && userDAO.exists(username, email)) {
            message = "Tên đăng nhập hoặc email này đã có người dùng.";
        }
        if (message != null) {
            request.setAttribute("message", message);
            return "/WEB-INF/views/auth/register.jsp";
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(username);

        // Băm Ở ĐÂY, không phải trong DAO. DAO chỉ đọc ghi database.
        user.setPasswordHash(PasswordUtil.hash(password));

        userDAO.insert(user);

        // Đăng ký xong đăng nhập luôn — đỡ bắt người dùng gõ lại
        user.setPasswordHash(null);
        request.getSession(true).setAttribute("currentUser", user);

        response.sendRedirect(request.getContextPath() + "/");
        return null;
    }

    /**
     * Trả về câu báo lỗi, hoặc null nếu mọi thứ hợp lệ.
     *
     * Tách riêng để method register() ở trên đọc là hiểu luồng, không bị chìm
     * trong một khối if dài mười mấy dòng.
     */
    private String validateRegister(String username, String email,
                                    String password, String confirm, String agree) {
        if (username.isEmpty() || email.isEmpty()
                || password == null || password.isEmpty()) {
            return "Vui lòng điền đủ các ô.";
        }
        if (username.length() < 3 || username.length() > 50) {
            return "Tên đăng nhập phải từ 3 đến 50 ký tự.";
        }
        // Chỉ cho chữ, số, gạch dưới — vì username đi vào URL trang cá nhân
        if (!username.matches("[a-zA-Z0-9_]+")) {
            return "Tên đăng nhập chỉ được dùng chữ, số và dấu gạch dưới.";
        }
        if (!email.matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            return "Email không hợp lệ.";
        }
        if (password.length() < 6) {
            return "Mật khẩu phải từ 6 ký tự trở lên.";
        }
        if (!password.equals(confirm)) {
            return "Hai ô mật khẩu không khớp nhau.";
        }
        // Mục tiêu đồ án: người dùng phải đồng ý nội quy trước khi tham gia
        if (agree == null) {
            return "Bạn cần đồng ý với nội quy cộng đồng.";
        }
        return null;
    }

    // ---- ĐĂNG XUẤT ---------------------------------------------------------

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            /*
             * invalidate() xoá TOÀN BỘ phiên, không phải chỉ removeAttribute.
             * Xoá mỗi currentUser thì mọi thứ khác trong phiên vẫn còn, và id
             * phiên cũ vẫn dùng được — người dùng máy chung sẽ để lại dấu vết.
             */
            session.invalidate();
        }
        response.sendRedirect(request.getContextPath() + "/");
    }

    /** null-safe trim: getParameter trả null khi ô không được gửi lên. */

    // ---- QUÊN MẬT KHẨU -----------------------------------------------------

    /**
     * TRANG 21 — Xin cấp vé đặt lại mật khẩu.
     *
     * LUÔN BÁO THÀNH CÔNG, KỂ CẢ KHI EMAIL KHÔNG TỒN TẠI.
     *   Nếu báo "email này chưa đăng ký" thì trang này thành công cụ dò: gõ
     *   thử vài trăm email là biết ai có tài khoản ở đây, ai không. Đó là rò
     *   rỉ thông tin, dù nghe có vẻ chỉ là một câu thông báo tử tế.
     *
     *   Cái giá: người gõ nhầm email sẽ ngồi đợi thư không bao giờ tới. Đây là
     *   đánh đổi mà gần như mọi trang web đều chọn theo hướng an toàn.
     */
    private String forgot(HttpServletRequest request) throws SQLException {
        String email = trim(request.getParameter("email"));

        if (!email.isEmpty()) {
            User u = userDAO.findByEmail(email);
            if (u != null) {
                String token = newToken();
                resetDAO.create(token, u.getId());

                /*
                 * ĐỒ ÁN KHÔNG CÓ MÁY CHỦ GỬI THƯ nên đường dẫn hiện thẳng ra
                 * màn hình. Trong hệ thống thật, dòng dưới đây được thay bằng
                 * lệnh gửi email và TUYỆT ĐỐI không hiện token cho người đang
                 * đứng trước màn hình — ai mở trang cũng đổi được mật khẩu của
                 * người khác chỉ bằng cách gõ email của họ.
                 */
                request.setAttribute("devLink",
                        request.getContextPath() + "/auth?action=reset&token=" + token);
            }
            request.setAttribute("sent", true);
        }

        request.setAttribute("pageTitle", "Quên mật khẩu");
        return "/WEB-INF/views/auth/forgot.jsp";
    }

    /**
     * TRANG 22 — Đặt lại mật khẩu bằng vé.
     *
     * GET  hiện form (đã kiểm vé trước, vé hỏng thì không hiện form làm gì).
     * POST đổi mật khẩu thật.
     *
     * KIỂM VÉ LẠI Ở BƯỚC POST, không tin bước GET.
     *   Vé có thể hết hạn trong lúc người dùng đang gõ, hoặc bị dùng ở tab
     *   khác. Quan trọng hơn: POST tới thẳng đây mà bỏ qua GET là chuyện dễ
     *   làm, nên mọi kiểm tra ở GET đều phải coi như chưa từng xảy ra.
     */
    private String reset(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        String token = trim(request.getParameter("token"));
        int userId = resetDAO.findValidUserId(token);

        if (userId == 0) {
            request.setAttribute("message",
                    "Liên kết không hợp lệ hoặc đã hết hạn. Hãy xin lại liên kết mới.");
            request.setAttribute("pageTitle", "Đặt lại mật khẩu");
            return "/WEB-INF/views/auth/forgot.jsp";
        }

        request.setAttribute("token", token);
        request.setAttribute("pageTitle", "Đặt lại mật khẩu");

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return "/WEB-INF/views/auth/reset.jsp";
        }

        String pass    = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");

        if (pass == null || pass.length() < 6) {
            request.setAttribute("message", "Mật khẩu phải từ 6 ký tự.");
            return "/WEB-INF/views/auth/reset.jsp";
        }
        if (!pass.equals(confirm)) {
            request.setAttribute("message", "Hai lần nhập không khớp.");
            return "/WEB-INF/views/auth/reset.jsp";
        }

        userDAO.updatePassword(userId, PasswordUtil.hash(pass));

        // Đánh dấu đã dùng NGAY. Quên bước này thì vé còn sống tới lúc hết
        // hạn và dùng lại được bao nhiêu lần cũng được.
        resetDAO.markUsed(token);

        request.getSession().setAttribute("flash",
                "Đã đổi mật khẩu. Đăng nhập lại nhé.");
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return null;
    }

    /**
     * Sinh token 32 byte ngẫu nhiên, mã hoá base64 an-toàn-cho-URL.
     *
     * 32 byte = 256 bit. Số tổ hợp lớn tới mức dò tìm là vô vọng, kể cả khi
     * thử hàng tỷ lần mỗi giây.
     *
     * URL-safe base64 vì token nằm trong đường dẫn: base64 thường có ký tự
     * '+' và '/', vào URL sẽ bị hiểu sai. withoutPadding() bỏ dấu '=' thừa,
     * cho ra đúng 43 ký tự — khớp với CHAR(43) khai trong schema.sql.
     */
    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
