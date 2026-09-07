package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.FollowDAO;
import truyen.dao.StoryDAO;
import truyen.dao.UserDAO;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.PasswordUtil;

/** TRANG 4 · 14 · 15 — Hồ sơ người dùng. */
@WebServlet("/user")
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private StoryDAO storyDAO;
    private FollowDAO followDAO;

    /** Số truyện mỗi trang trên hồ sơ tác giả. */
    private static final int PAGE_SIZE = 12;

    // init() chạy MỘT lần — chỗ đúng để tạo DAO, không tạo lại ở mỗi request
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        storyDAO = new StoryDAO();
        followDAO = new FollowDAO();
    }

    /*
     * GET hiển thị, POST thay đổi. Cả hai đi vào cùng handle() vì phần đuôi
     * (chọn layout, forward) giống hệt nhau — chỉ khác danh sách action.
     */
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

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "profile";
        }

        String url;
        try {
            switch (action) {
                case "me":       url = me(request, response);       break;
                case "edit":     url = edit(request, response);     break;
                case "save":     url = save(request, response);     break;
                case "password": url = password(request, response); break;
                default:         url = profile(request, response);  break;
            }
        } catch (SQLException e) {
            log("UserServlet: lỗi truy vấn, action=" + action, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (url == null) {
            return;   // method con đã sendError hoặc redirect xong
        }

        request.setAttribute("contentPage", url);
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }

    private String profile(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseIntOr(request.getParameter("id"), 0);
        User author = userDAO.findById(id);

        if (author == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        /* KHÔNG BAO GIỜ để chuỗi băm mật khẩu đi ra ngoài tầng controller. */
        author.setPasswordHash(null);

        int page = parseIntOr(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;
        }

        int total = storyDAO.countPublishedByAuthor(id);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE));
        if (page > totalPages) {
            page = totalPages;
        }

        List<Story> stories = storyDAO.findPublishedByAuthor(
                id, (page - 1) * PAGE_SIZE, PAGE_SIZE);

        request.setAttribute("author", author);
        request.setAttribute("stories", stories);
        request.setAttribute("totalStories", total);
        request.setAttribute("totalViews", storyDAO.totalViewsByAuthor(id));
        request.setAttribute("followerCount", followDAO.countFollowers(id));

        /*
         * Nút "Theo dõi" hay "Đang theo dõi"? Chỉ hỏi khi ĐÃ đăng nhập và
         * KHÔNG phải hồ sơ của chính mình — hai trường hợp còn lại không hiện
         * nút nên hỏi cũng vô ích, chỉ tốn thêm một câu SQL.
         */
        User me = (User) request.getSession().getAttribute("currentUser");
        if (me != null && me.getId() != id) {
            request.setAttribute("isFollowing",
                    followDAO.isFollowing(me.getId(), id));
        }
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("pageTitle", "Tác giả " + author.getName());
        return "/WEB-INF/views/user/profile.jsp";
    }


    /**
     * TRANG 14 — Hồ sơ của tôi.
     *
     * Không nhận tham số id. "Của tôi" luôn là người đang đăng nhập, đọc từ
     * session. Cho phép truyền id vào đây là mở cửa cho việc xem hồ sơ riêng
     * tư của người khác chỉ bằng cách đổi số trên thanh địa chỉ.
     */
    private String me(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = requireLogin(request, response);
        if (me == null) return null;

        // Đọc LẠI từ CSDL thay vì dùng thẳng object trong session: session giữ
        // ảnh chụp lúc đăng nhập, có thể đã cũ sau khi sửa hồ sơ ở tab khác.
        User fresh = userDAO.findById(me.getId());
        if (fresh == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        fresh.setPasswordHash(null);
        fresh.setStoryCount(storyDAO.countPublishedByAuthor(fresh.getId()));
        fresh.setFollowerCount(followDAO.countFollowers(fresh.getId()));

        request.setAttribute("me", fresh);
        request.setAttribute("totalViews", storyDAO.totalViewsByAuthor(fresh.getId()));
        request.setAttribute("pageTitle", "Hồ sơ của tôi");
        request.setAttribute("activeNav", "me");
        return "/WEB-INF/views/user/me.jsp";
    }

    /** TRANG 15 — Form sửa hồ sơ. */
    private String edit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = requireLogin(request, response);
        if (me == null) return null;

        User fresh = userDAO.findById(me.getId());
        if (fresh != null) fresh.setPasswordHash(null);

        request.setAttribute("me", fresh);
        request.setAttribute("pageTitle", "Sửa hồ sơ");
        request.setAttribute("activeNav", "me");
        return "/WEB-INF/views/user/edit.jsp";
    }

    /** Lưu hồ sơ. */
    private String save(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = requireLogin(request, response);
        if (me == null) return null;

        String name  = trim(request.getParameter("displayName"));
        String email = trim(request.getParameter("email"));
        String bio   = trim(request.getParameter("bio"));
        String avatar = trim(request.getParameter("avatarUrl"));

        String error = null;
        if (name.isEmpty()) {
            error = "Tên hiển thị không được để trống.";
        } else if (name.length() > 100) {
            error = "Tên hiển thị tối đa 100 ký tự.";
        } else if (!email.contains("@") || email.length() > 150) {
            error = "Email không hợp lệ.";
        } else if (bio.length() > 500) {
            error = "Giới thiệu tối đa 500 ký tự.";
        }

        if (error != null) {
            // Trả lại form kèm lỗi VÀ kèm những gì người dùng vừa gõ. Xoá
            // trắng form rồi bắt gõ lại từ đầu là cách nhanh nhất làm người
            // ta bỏ cuộc.
            User back = new User();
            back.setId(me.getId());
            back.setUsername(me.getUsername());
            back.setDisplayName(name);
            back.setEmail(email);
            back.setBio(bio);
            back.setAvatarUrl(avatar);
            request.setAttribute("me", back);
            request.setAttribute("message", error);
            request.setAttribute("pageTitle", "Sửa hồ sơ");
            return "/WEB-INF/views/user/edit.jsp";
        }

        User u = new User();
        u.setId(me.getId());
        u.setDisplayName(name);
        u.setEmail(email);
        u.setBio(bio);
        u.setAvatarUrl(avatar.isEmpty() ? null : avatar);
        userDAO.updateProfile(u);

        /*
         * CẬP NHẬT LẠI SESSION.
         *
         * Thanh menu hiện tên từ session. Không cập nhật thì đổi tên xong vẫn
         * thấy tên cũ trên đầu trang cho tới lần đăng nhập sau — người dùng sẽ
         * tưởng việc lưu thất bại và bấm lưu thêm mấy lần nữa.
         */
        me.setDisplayName(name);
        me.setEmail(email);
        me.setBio(bio);
        me.setAvatarUrl(u.getAvatarUrl());
        request.getSession().setAttribute("currentUser", me);
        request.getSession().setAttribute("flash", "Đã lưu hồ sơ.");

        response.sendRedirect(request.getContextPath() + "/user?action=me");
        return null;
    }

    /**
     * Đổi mật khẩu.
     *
     * BẮT NHẬP MẬT KHẨU CŨ dù người dùng đang đăng nhập.
     *   Nghe thừa, nhưng nó chặn đúng một tình huống rất thật: máy tính để
     *   quên chưa đăng xuất. Không hỏi mật khẩu cũ thì bất kỳ ai đi ngang qua
     *   cũng đổi được mật khẩu và chiếm luôn tài khoản.
     */
    private String password(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = requireLogin(request, response);
        if (me == null) return null;

        String oldPass = request.getParameter("oldPassword");
        String newPass = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");

        User full = userDAO.findById(me.getId());
        String error = null;

        if (full == null || !PasswordUtil.verify(oldPass, full.getPasswordHash())) {
            error = "Mật khẩu hiện tại không đúng.";
        } else if (newPass == null || newPass.length() < 6) {
            error = "Mật khẩu mới phải từ 6 ký tự.";
        } else if (!newPass.equals(confirm)) {
            error = "Hai lần nhập mật khẩu mới không khớp.";
        }

        if (error != null) {
            User back = userDAO.findById(me.getId());
            if (back != null) back.setPasswordHash(null);
            request.setAttribute("me", back);
            request.setAttribute("message", error);
            request.setAttribute("pageTitle", "Sửa hồ sơ");
            return "/WEB-INF/views/user/edit.jsp";
        }

        userDAO.updatePassword(me.getId(), PasswordUtil.hash(newPass));
        request.getSession().setAttribute("flash", "Đã đổi mật khẩu.");
        response.sendRedirect(request.getContextPath() + "/user?action=me");
        return null;
    }

    /**
     * Lấy người đang đăng nhập, chuyển hướng nếu chưa.
     *
     * Trả về null nghĩa là "đã xử lý xong, đừng làm gì nữa" — đúng quy ước
     * chung của các method con trong servlet này.
     */
    private User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        User me = (User) request.getSession().getAttribute("currentUser");
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
        return me;
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }

    /** ?id=abc không được làm sập trang — trả về giá trị mặc định. */
    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
