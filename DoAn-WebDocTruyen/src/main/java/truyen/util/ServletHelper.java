package truyen.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

import truyen.model.User;

/**
 * Tiện ích dùng chung cho mọi servlet.
 *
 * BA HÀM parseIntOr / currentUser / trimOrEmpty trước đây bị CHÉP LẠI
 * trong 12 servlet. Sửa một chỗ mà quên 11 chỗ kia là chắc chắn lệch nhau.
 * Gom về đây: sửa một lần, đúng ở mọi nơi.
 */
public final class ServletHelper {

    private ServletHelper() { }

    // ========================================================================
    //  Parse an toàn
    // ========================================================================

    /**
     * Parse chuỗi thành số nguyên, trả về fallback nếu null, rỗng, hoặc
     * không phải số.
     *
     * Dùng thay cho Integer.parseInt() trực tiếp: tham số trên URL là chuỗi
     * do người dùng gõ — sai định dạng là chuyện bình thường, không phải lỗi
     * cần quăng exception lên trang 500.
     */
    public static int parseIntOr(String s, int fallback) {
        if (s == null || s.isEmpty()) return fallback;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    // ========================================================================
    //  Session helpers
    // ========================================================================

    /**
     * Lấy người dùng đang đăng nhập từ session, trả null nếu chưa đăng nhập.
     *
     * getSession(false) — KHÔNG tạo session mới nếu chưa có. Nếu dùng
     * getSession() (mặc định true), mỗi con bot quét web sẽ được cấp một
     * session chiếm bộ nhớ server.
     */
    public static User currentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session == null
                ? null
                : (User) session.getAttribute("currentUser");
    }

    // ========================================================================
    //  Chuỗi
    // ========================================================================

    /**
     * Null-safe trim: null → "", còn lại trim bình thường.
     *
     * Giúp servlet không cần kiểm tra null riêng cho mỗi tham số form.
     */
    public static String trimOrEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    // ========================================================================
    //  Request helpers
    // ========================================================================

    /**
     * Kiểm tra request có phải AJAX (XMLHttpRequest hoặc format=json) hay không.
     *
     * Dùng để quyết định trả JSON hay HTML khi xảy ra lỗi.
     */
    public static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"))
                || "json".equalsIgnoreCase(request.getParameter("format"));
    }

    /**
     * Set flash message vào session rồi redirect — pattern lặp đi lặp lại
     * ở mọi servlet sau mỗi POST thành công hoặc thất bại.
     */
    public static void flashAndRedirect(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String flash,
                                        String url) throws IOException {
        request.getSession().setAttribute("flash", flash);
        response.sendRedirect(url);
    }
}
