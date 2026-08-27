package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.dao.UserDAO;
import truyen.model.User;
import truyen.util.PasswordUtil;

/**
 * CASE 01 — Đăng ký / Đăng nhập / Đăng xuất.
 *
 * URL:  /auth?action=login | register | logout
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

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
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
    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
