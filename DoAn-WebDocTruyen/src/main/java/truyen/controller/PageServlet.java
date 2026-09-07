package truyen.controller;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** CASE 11 — Trang tĩnh: Hướng dẫn sử dụng và Nội quy cộng đồng. */
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
