package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.UserDAO;
import truyen.model.User;
import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.trimOrEmpty;

/** TRANG 27 — Quản trị tài khoản: khoá, mở khoá, đổi vai trò. */
@WebServlet("/admin/user")
public class AdminUserServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        userDAO = new UserDAO();
    }

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

        // Các hành động thay đổi trạng thái người dùng BẮT BUỘC phải qua POST để chống CSRF
        if ("ban".equals(action) || "unban".equals(action) || "role".equals(action)) {
            if (!"POST".equalsIgnoreCase(request.getMethod())) {
                response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                return;
            }
        }

        try {
            if ("ban".equals(action) || "unban".equals(action)) {
                int id = parseIntOr(request.getParameter("id"), 0);
                User me = currentUser(request);

                /*
                 * KHÔNG cho admin tự ban chính mình.
                 *
                 * Nghe buồn cười nhưng nếu đây là admin duy nhất và họ bấm
                 * nhầm, sẽ KHÔNG CÒN AI đăng nhập được vào khu quản trị để bỏ
                 * ban — phải vào tận MySQL sửa tay mới cứu được.
                 */
                if (me != null && me.getId() == id) {
                    request.getSession().setAttribute("flashWarn", "Bạn không thể tự khoá tài khoản của mình.");
                } else if ("ban".equals(action)) {
                    String reason = trimOrEmpty(request.getParameter("reason"));
                    userDAO.updateStatus(id, "BANNED",
                            reason.isEmpty() ? "Vi phạm nội quy cộng đồng" : reason);
                    request.getSession().setAttribute("flash", "Đã khoá tài khoản.");
                } else {
                    userDAO.updateStatus(id, "ACTIVE", null);
                    request.getSession().setAttribute("flash", "Đã mở khoá tài khoản.");
                }
            } else if ("role".equals(action)) {
                int id = parseIntOr(request.getParameter("id"), 0);
                User me = currentUser(request);

                /*
                 * KHÔNG cho admin tự hạ quyền chính mình — cùng lý do với ban:
                 * admin cuối cùng tự hạ quyền là khoá cửa rồi vứt chìa vào
                 * trong. Muốn rời ghế thì nhờ một admin khác hạ giúp.
                 */
                if (me != null && me.getId() == id) {
                    request.getSession().setAttribute("flashWarn",
                            "Bạn không thể tự đổi vai trò của chính mình.");
                } else {
                    String role = "ADMIN".equals(request.getParameter("role"))
                                ? "ADMIN" : "USER";
                    userDAO.updateRole(id, role);
                    request.getSession().setAttribute("flash", "Đã đổi vai trò tài khoản.");
                }
            }

            // Post-Redirect-Get — F5 không gửi lại request
            if ("POST".equalsIgnoreCase(request.getMethod()) && action != null) {
                String q = trimOrEmpty(request.getParameter("q"));
                String status = trimOrEmpty(request.getParameter("status"));
                StringBuilder qs = new StringBuilder();
                if (!q.isEmpty()) {
                    qs.append("q=").append(java.net.URLEncoder.encode(q, "UTF-8"));
                }
                if (!status.isEmpty()) {
                    if (qs.length() > 0) qs.append("&");
                    qs.append("status=").append(java.net.URLEncoder.encode(status, "UTF-8"));
                }
                response.sendRedirect(request.getContextPath() + "/admin/user"
                        + (qs.length() > 0 ? "?" + qs : ""));
                return;
            }

            /*
             * Giữ lại chữ đã gõ và trạng thái đang lọc để hiện lại trên form.
             * Thiếu bước này thì khoá một người xong là ô tìm trống trơn và
             * bảng nhảy về toàn bộ danh sách — admin phải gõ lại từ đầu cho
             * mỗi người muốn xử lý.
             */
            String q = trimOrEmpty(request.getParameter("q"));
            String status = trimOrEmpty(request.getParameter("status"));
            request.setAttribute("q", q);
            request.setAttribute("statusFilter", status);
            request.setAttribute("users", userDAO.searchWithStoryCount(q, status));

        } catch (SQLException e) {
            log("AdminUserServlet: lỗi truy vấn, action=" + action, e);
            request.setAttribute("message", "Không tải được danh sách tài khoản.");
        }

        request.setAttribute("pageTitle", "Quản trị — Tài khoản");
        request.setAttribute("activeNav", "admin");
        request.setAttribute("adminSection", "user");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/users.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/admin.jsp")
                .forward(request, response);
    }
}
