package truyen.controller.common;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Xử lý lỗi tập trung (403, 404, 500) được Tomcat điều hướng qua &lt;error-page&gt;.
 * Đảm bảo hiển thị trang lỗi thân thiện, đồng bộ layout và không lộ thông tin máy chủ.
 *
 * <p>403 được gộp về đây từ bug-002. Trước đó ISSUE-016 chỉ làm 404 và 500, nên
 * {@code web.xml} buộc phải giữ nguyên một bộ {@code <error-page>} cũ chỉ vì mã
 * lỗi này — thành ra khai trùng 404 hai lần. Đủ ba mã ở một chỗ thì bộ cũ mới
 * xoá được.
 */
@WebServlet(name = "ErrorServlet", urlPatterns = "/error")
public class ErrorServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processError(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        processError(req, resp);
    }

    private void processError(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Integer statusCode = (Integer) req.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            String paramCode = req.getParameter("code");
            if (paramCode != null && !paramCode.trim().isEmpty()) {
                try {
                    statusCode = Integer.parseInt(paramCode.trim());
                } catch (NumberFormatException ignored) {}
            }
        }
        if (statusCode == null) {
            statusCode = 404;
        }

        req.setAttribute("errorCode", statusCode);
        if (statusCode == 403) {
            req.setAttribute("pageTitle", "403 — Không đủ quyền truy cập");
            req.setAttribute("contentPage", "/WEB-INF/views/error/403.jsp");
        } else if (statusCode == 404) {
            req.setAttribute("pageTitle", "404 — Không tìm thấy trang");
            req.setAttribute("contentPage", "/WEB-INF/views/error/404.jsp");
        } else {
            req.setAttribute("pageTitle", "500 — Sự cố máy chủ");
            req.setAttribute("contentPage", "/WEB-INF/views/error/500.jsp");
        }

        /*
         * Trả đúng mã trạng thái HTTP, không chỉ đúng giao diện.
         *
         * Khi Tomcat tự điều hướng tới đây qua <error-page> thì mã đã đúng sẵn.
         * Nhưng đường "/error?code=404" gõ tay hoặc gọi trực tiếp thì mặc định
         * trả 200 OK kèm một trang viết "404" — trình duyệt và Google đều tin
         * con số trong header chứ không đọc chữ trên trang, nên đó là một
         * "soft 404": máy tìm kiếm sẽ đi lập chỉ mục cho trang lỗi.
         *
         * Đặt trước forward, vì sau khi forward đã ghi ra response rồi thì
         * setStatus không còn tác dụng.
         */
        resp.setStatus(statusCode);

        req.getRequestDispatcher("/WEB-INF/views/layout/main.jsp").forward(req, resp);
    }
}
