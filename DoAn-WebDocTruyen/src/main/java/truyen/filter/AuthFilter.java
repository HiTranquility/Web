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

/**
 * CASE 01 — Chặn khách chưa đăng nhập.
 *
 * VẤN ĐỀ NÓ GIẢI QUYẾT
 *   Nhiều trang cần đăng nhập mới vào được. Chép đoạn kiểm tra session vào
 *   từng servlet thì quên MỘT chỗ là thủng. Filter chạy TRƯỚC servlet, nên
 *   thêm servlet mới dưới URL đã bảo vệ là nó tự động được bảo vệ theo.
 *
 * FILTER LÀM ĐƯỢC GÌ
 *   Biết "đã đăng nhập chưa". Hết.
 *
 * FILTER KHÔNG LÀM ĐƯỢC GÌ — chỗ này quan trọng
 *   Nó KHÔNG biết truyện id=6 là của ai. Việc kiểm quyền sở hữu
 *   (story.authorId == currentUser.id) phải nằm trong servlet.
 *   Thiếu bước đó thì người đã đăng nhập chỉ cần sửa ?id=5 thành ?id=6 là
 *   sửa được truyện người khác — filter cho qua vì họ vẫn là người dùng hợp lệ.
 */
@WebFilter(urlPatterns = {
        "/story",       // đăng, sửa, xoá truyện
        "/chapter",     // thêm, sửa chương
        "/comment",     // bình luận
        "/bookmark"     // đánh dấu
})
public class AuthFilter implements Filter {

    /**
     * Những action công khai — khách xem được, không cần đăng nhập.
     * Danh sách TRẮNG: mặc định là CHẶN, chỉ cho qua thứ có tên ở đây.
     *
     * Ngược lại (danh sách đen — chặn thứ có tên) là sai hướng: thêm action
     * mới mà quên bổ sung vào danh sách là nó lọt ra ngoài, không ai biết.
     */
    private static final String[] PUBLIC_ACTIONS = { "list", "detail", "read", "search" };

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }

        // Action công khai -> cho qua ngay, khỏi kiểm session
        for (String pub : PUBLIC_ACTIONS) {
            if (pub.equals(action)) {
                chain.doFilter(req, res);
                return;
            }
        }

        /*
         * getSession(false) — tham số false rất quan trọng.
         *   getSession()      hoặc getSession(true)  -> TẠO phiên mới nếu chưa có
         *   getSession(false)                        -> trả null nếu chưa có
         * Dùng bản true ở đây là mỗi con bot ghé qua đều được cấp một phiên,
         * server phải giữ hết trong bộ nhớ. Chỉ hỏi thôi thì dùng false.
         */
        HttpSession session = request.getSession(false);
        boolean daDangNhap = session != null && session.getAttribute("currentUser") != null;

        if (daDangNhap) {
            chain.doFilter(req, res);   // cho đi tiếp tới servlet
            return;
        }

        /*
         * Chưa đăng nhập -> đá về trang đăng nhập.
         *
         * KHÔNG gọi chain.doFilter() ở nhánh này, nên servlet phía sau KHÔNG
         * BAO GIỜ chạy. Đó là toàn bộ sức mạnh của filter.
         *
         * Nhớ lại URL họ định vào, để đăng nhập xong đưa họ trở lại đúng chỗ
         * thay vì quăng về trang chủ.
         */
        String target = request.getRequestURI();
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }
        request.getSession(true).setAttribute("redirectAfterLogin", target);

        response.sendRedirect(request.getContextPath() + "/auth?action=login");
    }
}
