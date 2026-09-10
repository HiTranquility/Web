package truyen.util;

import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Chống CSRF — Cross-Site Request Forgery.
 *
 * LỖ HỔNG NÓ VÁ
 *   Trình duyệt tự đính kèm cookie phiên vào MỌI request gửi tới web này, kể
 *   cả request do MỘT TRANG KHÁC tạo ra. Nên một trang bất kỳ chỉ cần chứa:
 *
 *       <form action="http://.../admin/user" method="post">
 *           <input type="hidden" name="action" value="ban">
 *           <input type="hidden" name="id" value="5">
 *       </form>
 *       <script>document.forms[0].submit()</script>
 *
 *   là admin nào đang đăng nhập mà lỡ mở trang đó sẽ khoá mất tài khoản số 5,
 *   không hề bấm gì. Server nhìn vào request thấy cookie hợp lệ, không có
 *   cách nào phân biệt với thao tác thật.
 *
 * CÁCH VÁ
 *   Mỗi phiên có một chuỗi bí mật. Mọi form POST đính kèm nó trong một ô ẩn.
 *   Server chỉ nhận request nào mang đúng chuỗi đó.
 *
 *   Vì sao cách này chặn được: trang tấn công KHÔNG ĐỌC ĐƯỢC chuỗi bí mật.
 *   Nó nằm trong HTML của trang mình, mà chính sách same-origin của trình
 *   duyệt cấm site khác đọc nội dung trang của mình. Gửi request thì được,
 *   nhưng ĐỌC để lấy chuỗi thì không.
 *
 * VÌ SAO KHÔNG CHỈ DỰA VÀO SameSite CỦA COOKIE
 *   SameSite=Lax chặn được phần lớn kiểu tấn công này, và web.xml nay đã khai.
 *   Nhưng đó là hàng rào do TRÌNH DUYỆT dựng: trình duyệt cũ không hiểu thuộc
 *   tính đó thì hàng rào biến mất mà server không hay biết. Token là hàng rào
 *   của chính server, không phụ thuộc ai. Dùng cả hai.
 *
 * VÌ SAO MỘT TOKEN CHO CẢ PHIÊN, KHÔNG PHẢI MỖI FORM MỘT TOKEN
 *   Đổi token sau mỗi request nghe an toàn hơn, nhưng làm hỏng việc mở nhiều
 *   tab: tab thứ hai giữ token cũ và mọi thao tác ở đó đều bị từ chối. Một
 *   token cho mỗi phiên đã chặn đúng thứ cần chặn.
 */
public final class CsrfUtil {

    /** Tên ô ẩn trong form, và tên khoá trong phiên. */
    public static final String FIELD = "_csrf";

    private static final String SESSION_KEY = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() { }

    /**
     * Token của phiên hiện tại, tạo mới nếu chưa có.
     *
     * SecureRandom chứ KHÔNG phải Math.random(): Math.random() dự đoán được
     * nếu biết vài giá trị trước đó, mà đoán được token thì token vô dụng.
     * Cùng lý do với chỗ sinh token đặt lại mật khẩu.
     */
    public static String token(HttpSession session) {
        Object cur = session.getAttribute(SESSION_KEY);
        if (cur instanceof String && !((String) cur).isEmpty()) {
            return (String) cur;
        }
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        session.setAttribute(SESSION_KEY, token);
        return token;
    }

    /**
     * Request này có mang đúng token của phiên không?
     *
     * KHÔNG TẠO PHIÊN MỚI khi kiểm (getSession(false)): request tới mà chưa
     * có phiên thì chắc chắn không có token nào để mà đúng — tạo phiên chỉ để
     * kết luận "sai" là cấp bộ nhớ cho mọi con bot gửi POST bừa.
     */
    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        Object expected = session.getAttribute(SESSION_KEY);
        if (!(expected instanceof String)) {
            return false;
        }
        return equalsConstantTime((String) expected, request.getParameter(FIELD));
    }

    /**
     * So sánh chuỗi mà thời gian chạy KHÔNG phụ thuộc vào chỗ khác nhau đầu
     * tiên nằm ở đâu.
     *
     * String.equals() thoát ra ngay ký tự đầu tiên khác nhau. Đo thời gian
     * phản hồi đủ nhiều lần thì dò được token từng ký tự một: đoán đúng ký tự
     * đầu thì phản hồi chậm hơn một chút, cứ thế lần ra cả chuỗi.
     *
     * Ở quy mô đồ án qua mạng thì nhiễu đường truyền át hết chênh lệch này,
     * nên rủi ro gần như bằng không. Nhưng viết đúng cũng chỉ tốn bốn dòng, và
     * đây là thói quen bắt buộc ở mọi chỗ so sánh bí mật.
     */
    private static boolean equalsConstantTime(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) {
            return false;
        }
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            diff |= a.charAt(i) ^ b.charAt(i);   // gộp mọi khác biệt, không thoát sớm
        }
        return diff == 0;
    }
}
