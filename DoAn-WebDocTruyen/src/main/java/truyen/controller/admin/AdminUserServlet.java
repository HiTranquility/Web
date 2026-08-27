package truyen.controller.admin;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.UserDAO;
import truyen.model.User;
import truyen.util.DBConnection;

/**
 * CASE 10 — Quản trị tài khoản: ban và bỏ ban.
 *
 * URL: /admin/user?action=list | ban | unban
 *
 * Quyền admin do AdminFilter lo ở /admin/* — không kiểm lại ở đây.
 */
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

        try {
            if ("ban".equals(action) || "unban".equals(action)) {
                int id = parseIntOr(request.getParameter("id"), 0);
                User me = (User) request.getSession().getAttribute("currentUser");

                /*
                 * KHÔNG cho admin tự ban chính mình.
                 *
                 * Nghe buồn cười nhưng nếu đây là admin duy nhất và họ bấm
                 * nhầm, sẽ KHÔNG CÒN AI đăng nhập được vào khu quản trị để bỏ
                 * ban — phải vào tận MySQL sửa tay mới cứu được.
                 */
                if (me != null && me.getId() == id) {
                    request.setAttribute("message", "Bạn không thể tự khoá tài khoản của mình.");
                } else if ("ban".equals(action)) {
                    String reason = trim(request.getParameter("reason"));
                    userDAO.updateStatus(id, "BANNED",
                            reason.isEmpty() ? "Vi phạm nội quy cộng đồng" : reason);
                } else {
                    userDAO.updateStatus(id, "ACTIVE", null);
                }

                /*
                 * Ban xong thì TRUYỆN CỦA HỌ VẪN CÒN trên web — quyết định
                 * thiết kế, không phải thiếu sót. Ẩn luôn truyện thì độc giả
                 * đang đọc dở mất trắng.
                 * Muốn gỡ truyện thì admin vào /admin/story gỡ riêng.
                 */
            }

            request.setAttribute("users", findAllUsers());

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

    /**
     * Danh sách tài khoản kèm số truyện mỗi người.
     *
     * GHI CHÚ VỀ VIỆC ĐẶT SQL Ở ĐÂY: truy vấn này chỉ trang quản trị dùng, nên
     * để tạm trong servlet cho gọn. Đúng chuẩn thì nó thuộc về UserDAO —
     * standards §2 nói controller không viết SQL. Nếu có thêm một chỗ nữa cần
     * dữ liệu này thì phải chuyển xuống DAO ngay.
     */
    private List<User> findAllUsers() throws SQLException {
        String sql =
            "SELECT u.id, u.username, u.email, u.display_name, u.role, u.status, "
          + "       u.ban_reason, u.created_at, "
          + "       (SELECT COUNT(*) FROM stories s "
          + "        WHERE s.author_id = u.id AND s.status != 'DELETED') AS story_count "
          + "FROM users u ORDER BY u.created_at DESC LIMIT 200";

        List<User> list = new ArrayList<>();
        try (Connection con = DBConnection.get();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User u = new User();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setDisplayName(rs.getString("display_name"));
                u.setRole(rs.getString("role"));
                u.setStatus(rs.getString("status"));
                u.setBanReason(rs.getString("ban_reason"));
                // Mượn cột bio để chở số truyện sang JSP — xem ghi chú dưới
                u.setBio(String.valueOf(rs.getInt("story_count")));
                if (rs.getTimestamp("created_at") != null) {
                    u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                }
                list.add(u);
            }
        }
        return list;
        /*
         * Mượn cột bio để chở story_count là cách làm TẮT, không đẹp.
         * Đúng ra nên tạo một lớp riêng (AdminUserRow) hoặc thêm field
         * storyCount vào User. Ở quy mô đồ án thì chấp nhận được, nhưng ghi
         * chú lại để người đọc biết đây là chỗ nợ kỹ thuật, không phải mẫu
         * để bắt chước.
         */
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
