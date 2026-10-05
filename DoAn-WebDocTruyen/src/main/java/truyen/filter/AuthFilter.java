package truyen.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** CASE 01 — Chặn khách chưa đăng nhập. */
@WebFilter(urlPatterns = {
        "/story",       // đăng, sửa, xoá truyện
        "/chapter",     // đọc, thêm, sửa chương (Yêu cầu đăng nhập)
        "/download",    // tải truyện (Yêu cầu đăng nhập)
        "/comment",     // bình luận
        "/bookmark",    // đánh dấu
        "/history",     // lịch sử đọc
        "/follow",      // theo dõi tác giả
        "/notification",// thông báo
        "/report",      // báo cáo vi phạm
        "/user",        // hồ sơ người dùng
        "/drive"        // sao lưu truyện lên Google Drive (tác giả)
})
public class AuthFilter implements Filter {

    /**
     * Những action công khai — khách xem được, không cần đăng nhập.
     * Danh sách TRẮNG: mặc định là CHẶN, chỉ cho qua thứ có tên ở đây.
     */
    private static String[] publicActionsFor(String path) {
        switch (path) {
            // Kho truyện, chi tiết truyện, tìm kiếm, gợi ý tự động — nội dung công khai.
            case "/story":
                return new String[] { "list", "detail", "search", "suggest" };

            // Hồ sơ tác giả công khai. me, edit, save, password thì cần đăng nhập.
            case "/user":
                return new String[] { "profile" };

            // "like" là thao tác AJAX — CommentServlet tự trả JSON needLogin nếu khách chưa đăng nhập.
            case "/comment":
                return new String[] { "like" };

            // /chapter, /download, /bookmark, /history, /follow, /notification, /report:
            // Nghiệp vụ: Chưa đăng nhập thì CHƯA cho đọc truyện hay tải truyện.
            default:
                return new String[0];
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String path = request.getServletPath();
        String action = request.getParameter("action");
        if (action == null) {
            if ("/chapter".equals(path)) {
                action = "read";
            } else if ("/user".equals(path)) {
                action = "profile";
            } else {
                action = "list";
            }
        }

        // Action công khai -> cho qua ngay, khỏi kiểm session
        for (String pub : publicActionsFor(request.getServletPath())) {
            if (pub.equals(action)) {
                chain.doFilter(req, res);
                return;
            }
        }

        HttpSession session = request.getSession(false);
        boolean daDangNhap = session != null && session.getAttribute("currentUser") != null;

        if (daDangNhap) {
            chain.doFilter(req, res);   // cho đi tiếp tới servlet
            return;
        }

        /* Chưa đăng nhập -> lưu URL đích và đá về trang đăng nhập kèm thông báo */
        String target = request.getRequestURI();
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("redirectAfterLogin", target);
        newSession.setAttribute("flash", "Vui lòng đăng nhập tài khoản để đọc truyện nhé!");

        response.sendRedirect(request.getContextPath() + "/auth?action=login");
    }
}
