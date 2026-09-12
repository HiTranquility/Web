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
        "/chapter",     // thêm, sửa chương
        "/comment",     // bình luận
        "/bookmark",    // đánh dấu
        "/history",     // lịch sử đọc
        "/follow",      // theo dõi tác giả
        "/notification",// thông báo
        "/report",      // báo cáo vi phạm
        "/user"         // hồ sơ người dùng
})
public class AuthFilter implements Filter {

    /**
     * Những action công khai — khách xem được, không cần đăng nhập.
     * Danh sách TRẮNG: mặc định là CHẶN, chỉ cho qua thứ có tên ở đây.
     *
     * Ngược lại (danh sách đen — chặn thứ có tên) là sai hướng: thêm action
     * mới mà quên bổ sung vào danh sách là nó lọt ra ngoài, không ai biết.
     *
     * TÁCH RIÊNG THEO TỪNG ĐƯỜNG DẪN, KHÔNG DÙNG CHUNG MỘT DANH SÁCH
     *   Bản trước để chung một mảng cho cả bốn servlet. Nhưng cùng một chữ
     *   "list" lại mang hai nghĩa khác hẳn nhau:
     *       /story?action=list      kho truyện   -> ai cũng xem được
     *       /bookmark?action=list   truyện đã lưu -> RIÊNG của từng người
     *   Dùng chung nghĩa là mở công khai cho cả cái thứ hai. Không lộ dữ liệu
     *   vì BookmarkServlet còn tự kiểm lại lần nữa, nhưng bộ lọc đã hết tác
     *   dụng ở đó — và hậu quả nhìn thấy được là khách bị đá về đăng nhập mà
     *   không ai nhớ họ định vào đâu, nên đăng nhập xong rơi về trang chủ.
     *
     *   Một tên action chỉ có nghĩa trong phạm vi servlet của nó. Danh sách
     *   trắng cũng phải theo phạm vi đó.
     */
    private static String[] publicActionsFor(String path) {
        switch (path) {
            // Kho truyện, chi tiết truyện, tìm kiếm, gợi ý tự động — nội dung công khai.
            case "/story":
                return new String[] { "list", "detail", "search", "suggest" };

            // "raw" la ban khong khung cua "read", dung cho doc lien tuc.
            // "toc" la muc luc tran, cho bang tha xuong o thanh doc.
            // Thieu chung o day thi fetch() bi da ve trang dang nhap va nhan
            // lai NGUYEN mot trang HTML — noi vao giua trang dang doc la hong
            // het. Ca hai deu chi DOC, va deu tu kiem lai quyen voi truyen
            // nhap trong servlet.
            case "/chapter":
                return new String[] { "read", "raw", "toc" };

            // Hồ sơ tác giả công khai. me, edit, save, password thì cần đăng nhập.
            case "/user":
                return new String[] { "profile" };

            // /comment, /bookmark, /history, /follow, /notification, /report:
            // không có action nào công khai cho khách.
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

        /*
         * getServletPath() chứ không phải getRequestURI(): cái sau còn kèm cả
         * tiền tố context ("/DoAn/story"), nên so sánh với "/story" sẽ không
         * bao giờ khớp và mọi thứ đều bị chặn.
         */
        // Action công khai -> cho qua ngay, khỏi kiểm session
        for (String pub : publicActionsFor(request.getServletPath())) {
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

        /* Chưa đăng nhập -> đá về trang đăng nhập. */
        String target = request.getRequestURI();
        if (request.getQueryString() != null) {
            target += "?" + request.getQueryString();
        }
        request.getSession(true).setAttribute("redirectAfterLogin", target);

        response.sendRedirect(request.getContextPath() + "/auth?action=login");
    }
}
