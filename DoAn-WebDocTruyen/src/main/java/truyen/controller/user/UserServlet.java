package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import truyen.util.DBConnection;
import truyen.util.UploadUtil;
import truyen.dao.FollowDAO;
import truyen.dao.GamificationDAO;
import truyen.dao.StoryDAO;
import truyen.dao.IdentityDAO;
import truyen.dao.UserDAO;
import truyen.dao.WalletDAO;
import truyen.model.DailyCheckin;
import truyen.model.Story;
import truyen.model.User;
import truyen.model.UserIdentity;
import truyen.util.GoogleConfig;
import truyen.util.GoogleTokenVerifier;
import truyen.util.PasswordUtil;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.trimOrEmpty;

/** TRANG 4 · 14 · 15 — Hồ sơ người dùng. */
@WebServlet("/user")
@MultipartConfig(
        fileSizeThreshold = 512 * 1024,        // 512 KB
        maxFileSize       = 2L * 1024 * 1024,  // 2 MB mỗi file
        maxRequestSize    = 4L * 1024 * 1024)  // 4 MB cả request
public class UserServlet extends HttpServlet {

    private UserDAO userDAO;
    private StoryDAO storyDAO;
    private FollowDAO followDAO;
    private IdentityDAO identityDAO;
    private WalletDAO walletDAO;
    private GamificationDAO gamificationDAO;
    private GoogleTokenVerifier googleTokenVerifier;

    /** Số truyện mỗi trang trên hồ sơ tác giả. */
    private static final int PAGE_SIZE = 12;

    // init() chạy MỘT lần — chỗ đúng để tạo DAO, không tạo lại ở mỗi request
    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
        storyDAO = new StoryDAO();
        followDAO = new FollowDAO();
        identityDAO = new IdentityDAO();
        walletDAO = new WalletDAO();
        gamificationDAO = new GamificationDAO();
        googleTokenVerifier = new GoogleTokenVerifier();
    }

    public void setGoogleTokenVerifier(GoogleTokenVerifier verifier) {
        this.googleTokenVerifier = verifier;
    }

    public void setUserDAO(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void setIdentityDAO(IdentityDAO identityDAO) {
        this.identityDAO = identityDAO;
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
            User me = currentUser(request);
            if (me != null && request.getParameter("id") == null) {
                action = "me";
            } else {
                action = "profile";
            }
        }

        if ("save".equals(action) || "password".equals(action) || "link-google".equals(action)
                || "unlink-google".equals(action) || "set-password".equals(action)
                || "checkin".equals(action) || "claim-quest".equals(action)) {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
        }

        String url;
        try {
            switch (action) {
                case "me":            url = me(request, response);            break;
                case "checkin":       handleCheckin(request, response);       return;
                case "claim-quest":   handleClaimQuest(request, response);    return;
                case "edit":          url = edit(request, response);          break;
                case "save":          url = save(request, response);          break;
                case "password":      url = password(request, response);      break;
                case "set-password":  url = setPassword(request, response);   break;
                case "link-google":   linkGoogle(request, response);          return;
                case "unlink-google": unlinkGoogle(request, response);        return;
                default:              url = profile(request, response);       break;
            }
        } catch (SQLException e) {
            log("UserServlet: lỗi truy vấn, action=" + action, e);

            /*
             * KHONG nem trang 500 khi nguyen nhan la CHUA CO CSDL.
             *
             * O che do xem giao dien, moi lenh GHI deu that bai — dung nhu
             * thiet ke. Nhung tra ve trang 500 thi nguoi dung tuong web hong,
             * trong khi thuc te chi la chua chay setup-db.ps1.
             *
             * Noi ro nguyen nhan roi tra ho ve cho cu. Chi loi THAT SU bat ngo
             * moi dang mot trang 500.
             */
            if (!DBConnection.isReady()) {
                request.getSession().setAttribute("flash",
                        "Chưa nối cơ sở dữ liệu nên chưa lưu được. "
                        + "Chạy scripts\\setup-db.ps1 rồi tạo db.properties.");
                response.sendRedirect(request.getContextPath() + "/user?action=me");
                return;
            }
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
        if (id <= 0) {
            User me = currentUser(request);
            if (me != null) {
                response.sendRedirect(request.getContextPath() + "/user?action=me");
                return null;
            } else {
                response.sendRedirect(request.getContextPath() + "/");
                return null;
            }
        }
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
        User me = currentUser(request);
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

        UserIdentity googleIdentity = identityDAO.findByUserAndProvider(fresh.getId(), "GOOGLE");
        request.setAttribute("googleIdentity", googleIdentity);

        request.setAttribute("me", fresh);
        request.setAttribute("totalViews", storyDAO.totalViewsByAuthor(fresh.getId()));
        request.setAttribute("walletBalance", walletDAO.getBalance(fresh.getId()));
        request.setAttribute("todayCheckin", gamificationDAO.getTodayCheckin(fresh.getId()));
        request.setAttribute("currentStreak", gamificationDAO.getCurrentStreak(fresh.getId()));
        request.setAttribute("dailyQuests", gamificationDAO.getDailyQuests(fresh.getId()));
        request.setAttribute("pageTitle", "Hồ sơ của tôi");
        request.setAttribute("activeNav", "me");
        return "/WEB-INF/views/user/me.jsp";
    }

    private void handleCheckin(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        User me = requireLogin(request, response);
        if (me == null) return;

        DailyCheckin checkin = gamificationDAO.doCheckin(me.getId());
        if (checkin != null) {
            request.getSession().setAttribute("flash", "🎉 Điểm danh thành công! Bạn nhận được +" + checkin.getRewardCoins() + " xu.");
        } else {
            request.getSession().setAttribute("flash", "Bạn đã điểm danh hôm nay rồi!");
        }

        if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"success\":true,\"newBalance\":" + walletDAO.getBalance(me.getId()) + "}");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/user?action=me#gamification");
    }

    private void handleClaimQuest(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        User me = requireLogin(request, response);
        if (me == null) return;

        String key = trimOrEmpty(request.getParameter("key"));
        boolean ok = gamificationDAO.claimQuestReward(me.getId(), key);
        if (ok) {
            request.getSession().setAttribute("flash", "🎁 Nhận thưởng nhiệm vụ thành công!");
        } else {
            request.getSession().setAttribute("flashError", "Không thể nhận thưởng hoặc bạn đã nhận trước đó.");
        }

        if ("XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))) {
            response.setContentType("application/json; charset=UTF-8");
            response.getWriter().write("{\"success\":" + ok + ",\"newBalance\":" + walletDAO.getBalance(me.getId()) + "}");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/user?action=me#gamification");
    }

    /** TRANG 15 — Form sửa hồ sơ. */
    private String edit(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = requireLogin(request, response);
        if (me == null) return null;

        User fresh = userDAO.findById(me.getId());
        if (fresh != null) fresh.setPasswordHash(null);

        UserIdentity googleIdentity = identityDAO.findByUserAndProvider(fresh.getId(), "GOOGLE");
        boolean hasPassword = userDAO.hasPassword(fresh.getId());

        request.setAttribute("googleIdentity", googleIdentity);
        request.setAttribute("hasPassword", hasPassword);
        request.setAttribute("googleEnabled", GoogleConfig.isEnabled());
        request.setAttribute("googleApiKey", GoogleConfig.getApiKey());
        request.setAttribute("googleAuthDomain", GoogleConfig.getAuthDomain());
        request.setAttribute("googleProjectId", GoogleConfig.getProjectId());
        request.setAttribute("googleAppId", GoogleConfig.getAppId());

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

        String name  = trimOrEmpty(request.getParameter("displayName"));
        String email = trimOrEmpty(request.getParameter("email"));
        String bio   = trimOrEmpty(request.getParameter("bio"));
        String avatar = trimOrEmpty(request.getParameter("avatarUrl"));

        String error = null;

        // Xử lý tải ảnh đại diện từ máy
        Part avatarPart = null;
        try {
            avatarPart = request.getPart("avatarFile");
        } catch (IllegalStateException | ServletException e) {
            error = "File ảnh đại diện quá lớn (tối đa 2 MB) hoặc không hợp lệ.";
        }

        String removeAvatar = request.getParameter("removeAvatar");
        if ("1".equals(removeAvatar)) {
            avatar = "";
        } else if (avatarPart != null && avatarPart.getSize() > 0 && error == null) {
            try {
                String uploaded = UploadUtil.save(avatarPart, getServletContext());
                if (uploaded != null) {
                    avatar = uploaded;
                }
            } catch (UploadUtil.UploadException e) {
                error = e.getMessage();
            }
        }

        if (error == null) {
            if (name.isEmpty()) {
                error = "Tên hiển thị không được để trống.";
            } else if (name.length() > 100) {
                error = "Tên hiển thị tối đa 100 ký tự.";
            } else if (!email.contains("@") || email.length() > 150) {
                error = "Email không hợp lệ.";
            } else if (bio.length() > 500) {
                error = "Giới thiệu tối đa 500 ký tự.";
            } else {
                User existing = userDAO.findByEmail(email);
                if (existing != null && existing.getId() != me.getId()) {
                    error = "Email này đã được sử dụng bởi tài khoản khác.";
                }
            }
        }

        if (error != null) {
            User back = userDAO.findById(me.getId());
            if (back != null) back.setPasswordHash(null);
            else back = new User();
            back.setId(me.getId());
            back.setUsername(me.getUsername());
            back.setDisplayName(name);
            back.setEmail(email);
            back.setBio(bio);
            back.setAvatarUrl(avatar);

            UserIdentity googleIdentity = identityDAO.findByUserAndProvider(me.getId(), "GOOGLE");
            boolean hasPassword = userDAO.hasPassword(me.getId());

            request.setAttribute("googleIdentity", googleIdentity);
            request.setAttribute("hasPassword", hasPassword);
            request.setAttribute("googleEnabled", GoogleConfig.isEnabled());
            request.setAttribute("googleApiKey", GoogleConfig.getApiKey());
            request.setAttribute("googleAuthDomain", GoogleConfig.getAuthDomain());
            request.setAttribute("googleProjectId", GoogleConfig.getProjectId());
            request.setAttribute("googleAppId", GoogleConfig.getAppId());

            request.setAttribute("me", back);
            request.setAttribute("message", error);
            request.setAttribute("pageTitle", "Sửa hồ sơ");
            request.setAttribute("activeNav", "me");
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
        User me = currentUser(request);
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
        }
        return me;
    }

    /**
     * Tạo mật khẩu lần đầu cho tài khoản đăng ký qua Google (chưa có mật khẩu).
     */
    private String setPassword(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = requireLogin(request, response);
        if (me == null) return null;

        boolean hasPassword = userDAO.hasPassword(me.getId());
        if (hasPassword) {
            request.getSession().setAttribute("flashError", "Tài khoản đã có mật khẩu. Vui lòng sử dụng tính năng Đổi mật khẩu.");
            response.sendRedirect(request.getContextPath() + "/user?action=edit");
            return null;
        }

        String newPass = request.getParameter("newPassword");
        String confirm = request.getParameter("confirmPassword");
        String error = null;

        if (newPass == null || newPass.length() < 6) {
            error = "Mật khẩu mới phải từ 6 ký tự trở lên.";
        } else if (!newPass.equals(confirm)) {
            error = "Hai lần nhập mật khẩu không khớp.";
        }

        if (error != null) {
            User fresh = userDAO.findById(me.getId());
            if (fresh != null) fresh.setPasswordHash(null);
            request.setAttribute("me", fresh);
            request.setAttribute("googleIdentity", identityDAO.findByUserAndProvider(me.getId(), "GOOGLE"));
            request.setAttribute("hasPassword", false);
            request.setAttribute("googleEnabled", GoogleConfig.isEnabled());
            request.setAttribute("googleApiKey", GoogleConfig.getApiKey());
            request.setAttribute("googleAuthDomain", GoogleConfig.getAuthDomain());
            request.setAttribute("googleProjectId", GoogleConfig.getProjectId());
            request.setAttribute("googleAppId", GoogleConfig.getAppId());
            request.setAttribute("message", error);
            request.setAttribute("pageTitle", "Sửa hồ sơ");
            request.setAttribute("activeNav", "me");
            return "/WEB-INF/views/user/edit.jsp";
        }

        userDAO.updatePassword(me.getId(), PasswordUtil.hash(newPass));
        request.getSession().setAttribute("flash", "Đã tạo mật khẩu thành công! Giờ đây bạn có thể đăng nhập bằng tên đăng nhập và mật khẩu.");
        response.sendRedirect(request.getContextPath() + "/user?action=me");
        return null;
    }

    /**
     * Liên kết tài khoản Google với tài khoản hiện tại.
     * Nhận Google idToken qua POST, xác thực và lưu vào user_identities.
     */
    private void linkGoogle(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = currentUser(request);
        if (me == null) {
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, false, "Vui lòng đăng nhập để thực hiện.");
            return;
        }

        if (!GoogleConfig.isEnabled()) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, false, "Chức năng Google chưa được cấu hình.");
            return;
        }

        String idToken = request.getParameter("idToken");
        if (idToken == null || idToken.trim().isEmpty()) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, false, "Thiếu idToken từ Google.");
            return;
        }

        GoogleTokenVerifier.GoogleUser gUser = googleTokenVerifier.verify(idToken.trim());
        if (gUser == null) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, false, "Google token không hợp lệ hoặc đã hết hạn.");
            return;
        }

        String sub = gUser.getSub();
        String email = gUser.getEmail();

        // Kiểm tra xem tài khoản Google này đã gắn với ai chưa
        UserIdentity existing = identityDAO.findByProviderUid("GOOGLE", sub);
        if (existing != null) {
            if (existing.getUserId() == me.getId()) {
                sendJsonResponse(response, HttpServletResponse.SC_OK, true, "Tài khoản của bạn đã được gắn với Google này rồi.");
            } else {
                sendJsonResponse(response, HttpServletResponse.SC_CONFLICT, false,
                        "Tài khoản Google này (" + email + ") đã được gắn với một người dùng khác.");
            }
            return;
        }

        // Kiểm tra xem tài khoản hiện tại đã gắn Google khác chưa
        UserIdentity currentGoogle = identityDAO.findByUserAndProvider(me.getId(), "GOOGLE");
        if (currentGoogle != null) {
            sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, false,
                    "Tài khoản của bạn đã liên kết với Google (" + currentGoogle.getEmail() + "). Vui lòng hủy liên kết cũ trước.");
            return;
        }

        UserIdentity newIdentity = new UserIdentity(me.getId(), "GOOGLE", sub, email);
        identityDAO.insert(newIdentity);

        sendJsonResponse(response, HttpServletResponse.SC_OK, true, "Đã liên kết tài khoản Google (" + email + ") thành công!");
    }

    /**
     * Hủy liên kết tài khoản Google.
     * Chặn tuyệt đối nếu tài khoản CHƯA có mật khẩu (hasPassword == false).
     */
    private void unlinkGoogle(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = currentUser(request);
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        boolean isAjax = isAjax(request);

        // Security gate: nếu tài khoản chưa có mật khẩu thì cấm gỡ
        if (!userDAO.hasPassword(me.getId())) {
            String errorMsg = "Bạn chưa tạo mật khẩu cho tài khoản. Vui lòng tạo mật khẩu trước khi hủy liên kết Google để tránh mất quyền đăng nhập!";
            if (isAjax) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, false, errorMsg);
            } else {
                request.getSession().setAttribute("flashError", errorMsg);
                response.sendRedirect(request.getContextPath() + "/user?action=edit");
            }
            return;
        }

        UserIdentity google = identityDAO.findByUserAndProvider(me.getId(), "GOOGLE");
        if (google == null) {
            String errorMsg = "Tài khoản của bạn hiện không có liên kết Google nào.";
            if (isAjax) {
                sendJsonResponse(response, HttpServletResponse.SC_BAD_REQUEST, false, errorMsg);
            } else {
                request.getSession().setAttribute("flashError", errorMsg);
                response.sendRedirect(request.getContextPath() + "/user?action=edit");
            }
            return;
        }

        identityDAO.deleteByUserAndProvider(me.getId(), "GOOGLE");

        String successMsg = "Đã hủy liên kết tài khoản Google thành công.";
        if (isAjax) {
            sendJsonResponse(response, HttpServletResponse.SC_OK, true, successMsg);
        } else {
            request.getSession().setAttribute("flash", successMsg);
            response.sendRedirect(request.getContextPath() + "/user?action=edit");
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || (request.getHeader("Accept") != null && request.getHeader("Accept").contains("application/json"))
                || "1".equals(request.getParameter("ajax"));
    }

    private void sendJsonResponse(HttpServletResponse response, int statusCode, boolean success, String message)
            throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json;charset=UTF-8");
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"success\":").append(success).append(",");
        sb.append("\"message\":\"").append(escapeJson(message)).append("\"");
        sb.append("}");
        response.getWriter().write(sb.toString());
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}

