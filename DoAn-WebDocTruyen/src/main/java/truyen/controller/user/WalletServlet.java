package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.dao.WalletDAO;
import truyen.model.User;

/**
 * Xử lý các nghiệp vụ ví xu ảo và tặng thưởng tác giả (ISSUE-008).
 */
@WebServlet(name = "WalletServlet", urlPatterns = "/wallet")
public class WalletServlet extends HttpServlet {

    private final WalletDAO walletDAO = new WalletDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("balance".equals(action)) {
            handleBalance(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("tip".equals(action)) {
            handleTip(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleBalance(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"error\":\"Chưa đăng nhập\"}");
            return;
        }

        try {
            int balance = walletDAO.getBalance(currentUser.getId());
            resp.getWriter().write("{\"balance\":" + balance + "}");
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Lỗi CSDL\"}");
        }
    }

    private void handleTip(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write("{\"success\":false,\"message\":\"Vui lòng đăng nhập để tặng xu\"}");
            return;
        }

        int toUserId = parseInt(req.getParameter("toUserId"), 0);
        int amount = parseInt(req.getParameter("amount"), 0);
        Integer storyId = parseInteger(req.getParameter("storyId"));
        String message = req.getParameter("message");

        if (toUserId <= 0 || toUserId == currentUser.getId()) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Không thể tự tặng xu cho chính mình\"}");
            return;
        }

        if (amount <= 0 || amount > 1000) {
            resp.getWriter().write("{\"success\":false,\"message\":\"Số xu tặng không hợp lệ (tối đa 1.000 xu)\"}");
            return;
        }

        try {
            boolean ok = walletDAO.transfer(currentUser.getId(), toUserId, storyId, amount, message);
            if (ok) {
                int newBalance = walletDAO.getBalance(currentUser.getId());
                resp.getWriter().write("{\"success\":true,\"newBalance\":" + newBalance
                        + ",\"message\":\"✨ Tặng " + amount + " xu cho tác giả thành công!\"}");
            } else {
                resp.getWriter().write("{\"success\":false,\"message\":\"Số dư xu không đủ để thực hiện giao dịch\"}");
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"success\":false,\"message\":\"Sự cố hệ thống máy chủ\"}");
        }
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private Integer parseInteger(String s) {
        if (s == null || s.trim().isEmpty()) return null;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
