package truyen.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CASE 11 — Trang tĩnh: Hướng dẫn sử dụng và Nội quy cộng đồng.
 *
 * URL: /page?name=guide | rules
 *
 * VÌ SAO CẦN SERVLET CHO TRANG TĨNH
 *   Hai trang này không có dữ liệu động, đáng lẽ để file .html là xong. Nhưng
 *   chúng cần khung chung (nav, footer) — mà JSP nằm trong WEB-INF thì không
 *   gõ URL vào được. Servlet mỏng này là cầu nối.
 *
 * DANH SÁCH TRẮNG — chỗ quan trọng nhất của file
 *   Tham số name đi thẳng vào đường dẫn file. Nếu không kiểm, ai đó gõ
 *       /page?name=../../../../etc/passwd
 *   là đọc được file ngoài ứng dụng. Lỗ hổng này gọi là "path traversal".
 *   Chỉ chấp nhận đúng những tên có trong danh sách là chặn được hoàn toàn.
 */
@WebServlet("/page")
public class PageServlet extends HttpServlet {

    private static final List<String> ALLOWED = Arrays.asList("guide", "rules");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("name");

        // Kiểm bằng DANH SÁCH TRẮNG, không phải lọc ký tự xấu.
        // Lọc ký tự thì luôn sót cách mã hoá nào đó; danh sách trắng thì không.
        if (name == null || !ALLOWED.contains(name)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        request.setAttribute("pageTitle",
                "rules".equals(name) ? "Nội quy cộng đồng" : "Hướng dẫn sử dụng");
        request.setAttribute("activeNav", "rules".equals(name) ? "rules" : null);
        request.setAttribute("contentPage", "/WEB-INF/views/page/" + name + ".jsp");

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
