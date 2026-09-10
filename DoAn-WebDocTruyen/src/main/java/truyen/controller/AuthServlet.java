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

/** CASE 01 — Đăng ký / Đăng nhập / Đăng xuất. */
@WebServlet("/auth")
public class AuthServlet extends HttpServlet {

    private UserDAO userDAO;
    private PasswordResetDAO resetDAO;

    /* SecureRandom, KHÔNG phải Random. */
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

        /* MỘT THÔNG BÁO CHUNG CHO CẢ HAI TRƯỜNG HỢP SAI. */
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
         * LẤY ĐÍCH ĐẾN RA TRƯỚC KHI HUỶ PHIÊN.
         *
         * AuthFilter/AdminFilter lưu "redirectAfterLogin" vào phiên CŨ khi đá
         * khách về đây. Ngay dưới là lệnh invalidate() — huỷ phiên là xoá sạch
         * mọi thuộc tính trong đó, kể cả cái này. Đọc sau khi huỷ thì luôn
         * nhận null và người dùng luôn bị ném về trang chủ.
         *
         * Không đổi được thứ tự: đổi id phiên PHẢI làm trước khi đặt
         * currentUser, nếu không thì kẻ tấn công đã biết id phiên từ trước
         * vẫn dùng lại được nó sau khi nạn nhân đăng nhập (session fixation).
         */
        HttpSession old = request.getSession(false);
        String target = old == null ? null : (String) old.getAttribute("redirectAfterLogin");
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
        response.sendRedirect(request.getContextPath() + landingPage(user, target));
        return null;
    }

    /**
     * Đăng nhập xong thì thả người dùng xuống đâu?
     *
     * Thứ tự ưu tiên, dừng ở điều kiện đầu tiên đúng:
     *
     *   1. NƠI HỌ ĐỊNH ĐẾN. Ai đó bấm vào link một chương, bị đá về đăng nhập,
     *      thì đăng nhập xong phải thấy đúng chương đó. Ném về trang chủ là bắt
     *      họ tự mò lại từ đầu — mà thường là họ bỏ luôn.
     *
     *   2. ADMIN -> thẳng bảng điều khiển. Admin đăng nhập gần như luôn là để
     *      làm việc quản trị, không phải để đọc truyện. Thả xuống trang chủ rồi
     *      bắt tìm menu là thừa một bước cho mọi lần đăng nhập.
     *
     *   3. Còn lại -> trang chủ.
     *
     * KIỂM ĐÍCH ĐẾN TRƯỚC KHI DÙNG
     *   target lấy từ getRequestURI() nên vốn đã là đường dẫn nội bộ. Nhưng
     *   hàm này không tự biết điều đó, và chỉ cần sau này có ai truyền đích đến
     *   qua tham số URL là lỗ hổng chuyển hướng mở (open redirect) xuất hiện:
     *   trang đăng nhập của chính mình lại đẩy người dùng sang web lừa đảo.
     *
     *   Nên chặn ngay tại đây: bắt buộc bắt đầu bằng "/" và KHÔNG bắt đầu bằng
     *   "//" — vì "//ac.com/x" là URL tuyệt đối hợp lệ trong trình duyệt, chỉ
     *   là viết tắt phần giao thức.
     */
    private String landingPage(User user, String target) {
        if (target != null
                && target.startsWith("/")
                && !target.startsWith("//")) {

            /* Bỏ tiền tố context ra — chỗ gọi đã tự nối lại rồi, giữ nguyên
               là nối hai lần thành "/app/app/chapter". */
            String ctx = getServletContext().getContextPath();
            if (!ctx.isEmpty() && target.startsWith(ctx)) {
                target = target.substring(ctx.length());
            }
            if (target.startsWith("/") && !target.startsWith("//")) {
                return target;
            }
        }
        return user.isAdmin() ? "/admin/dashboard" : "/";
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

    /** TRANG 21 — Xin cấp vé đặt lại mật khẩu. */
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

    /** TRANG 22 — Đặt lại mật khẩu bằng vé. */
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

    /** Sinh token 32 byte ngẫu nhiên, mã hoá base64 an-toàn-cho-URL. */
    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
