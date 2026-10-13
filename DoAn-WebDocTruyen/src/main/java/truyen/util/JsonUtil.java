package truyen.util;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
 * Tiện ích chuẩn hoá định dạng và phản hồi JSON qua HTTP Servlet (Clean Architecture).
 *
 * Hỗ trợ:
 * - jsonEscape: Mã hóa ký tự đặc biệt theo chuẩn JSON RFC 8259 an toàn tiếng Việt.
 * - sendJson: Gửi nội dung JSON thô kèm mã HTTP status và charset UTF-8.
 * - sendSuccess: Gửi đối tượng JSON thành công chuẩn {"success":true,"message":...}.
 * - sendError: Gửi đối tượng JSON thông báo lỗi chuẩn {"error":...,"status":...}.
 */
public final class JsonUtil {

    private JsonUtil() {
        // Lớp tiện ích tĩnh, ngăn khởi tạo
    }

    /**
     * Escape chuỗi an toàn theo chuẩn JSON (kèm cặp ngoặc kép bao ngoài).
     * Trả về "" (có cặp ngoặc kép) nếu chuỗi null.
     */
    public static String jsonEscape(String s) {
        if (s == null) {
            return "\"\"";
        }
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("\\\"");
                    break;
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    if (c < ' ') {
                        String hex = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    /**
     * Gửi phản hồi JSON trực tiếp tới client với mã status và UTF-8.
     */
    public static void sendJson(HttpServletResponse resp, int status, String rawJson) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json; charset=UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(rawJson);
    }

    /**
     * Gửi phản hồi JSON thông báo lỗi có định dạng chuẩn:
     * {"error": "...", "status": status}
     */
    public static void sendError(HttpServletResponse resp, int status, String message) throws IOException {
        sendJson(resp, status, "{\"error\":" + jsonEscape(message) + ",\"status\":" + status + "}");
    }

    /**
     * Gửi phản hồi JSON thông báo thành công có định dạng chuẩn:
     * {"success": true, "message": "..."}
     */
    public static void sendSuccess(HttpServletResponse resp, String message) throws IOException {
        sendJson(resp, HttpServletResponse.SC_OK, "{\"success\":true,\"message\":" + jsonEscape(message) + "}");
    }
}
