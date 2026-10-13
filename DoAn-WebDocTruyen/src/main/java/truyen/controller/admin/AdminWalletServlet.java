package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.WalletDAO;
import truyen.model.Transaction;
import truyen.model.User;
import truyen.model.Wallet;
import truyen.util.ServletHelper;
import static truyen.util.ServletHelper.currentUser;
import static truyen.util.ServletHelper.parseIntOr;

/**
 * Quản lý dòng tiền ví xu và cấp/thu hồi xu hệ thống cho Quản trị viên (ISSUE-023).
 */
@WebServlet(name = "AdminWalletServlet", urlPatterns = "/admin/wallet")
public class AdminWalletServlet extends HttpServlet {

    private final WalletDAO walletDAO = new WalletDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String q = request.getParameter("q");
        int page = parseIntOr(request.getParameter("page"), 1);
        if (page < 1) page = 1;
        int limit = 15;
        int offset = (page - 1) * limit;

        try {
            List<Wallet> wallets = walletDAO.findAllWallets(q, offset, limit);
            int total = walletDAO.countWallets(q);
            int totalPages = (int) Math.ceil((double) total / limit);
            if (totalPages < 1) totalPages = 1;

            long totalCoins = walletDAO.getTotalCirculatingCoins();
            long totalTxs = walletDAO.getTotalTransactions();
            long totalTipped = walletDAO.getTotalCoinsTipped();
            List<Transaction> recentTxs = walletDAO.findRecentTransactions(15);

            request.setAttribute("wallets", wallets);
            request.setAttribute("total", total);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("q", q);

            request.setAttribute("totalCoins", totalCoins);
            request.setAttribute("totalTxs", totalTxs);
            request.setAttribute("totalTipped", totalTipped);
            request.setAttribute("recentTxs", recentTxs);
        } catch (SQLException e) {
            log("AdminWalletServlet: lỗi nạp dữ liệu ví", e);
            request.setAttribute("error", "Lỗi nạp dữ liệu ví: " + e.getMessage());
        }

        request.setAttribute("pageTitle", "Quản lý Ví xu Toàn hệ thống");
        request.setAttribute("activeNav", "admin-wallet");
        request.setAttribute("adminSection", "wallet");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/wallet.jsp");
        request.getRequestDispatcher("/WEB-INF/views/layout/admin.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        User me = currentUser(request);
        if (me == null || !me.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        if ("adjust".equals(action)) {
            handleAdjust(request, response, me);
        } else {
            response.sendRedirect(request.getContextPath() + "/admin/wallet");
        }
    }

    private void handleAdjust(HttpServletRequest request, HttpServletResponse response, User admin)
            throws IOException {
        int userId = parseIntOr(request.getParameter("userId"), 0);
        int rawAmount = parseIntOr(request.getParameter("amount"), 0);
        String type = request.getParameter("type"); // "grant" hoặc "revoke"
        String reason = request.getParameter("reason");

        if (userId <= 0) {
            request.getSession().setAttribute("flashError", "Tài khoản người dùng không hợp lệ.");
            response.sendRedirect(request.getContextPath() + "/admin/wallet");
            return;
        }

        if (rawAmount <= 0) {
            request.getSession().setAttribute("flashError", "Số xu điều chỉnh phải lớn hơn 0.");
            response.sendRedirect(request.getContextPath() + "/admin/wallet");
            return;
        }

        if (reason == null || reason.trim().isEmpty()) {
            request.getSession().setAttribute("flashError", "Bắt buộc nhập lý do khi cấp hoặc thu hồi xu.");
            response.sendRedirect(request.getContextPath() + "/admin/wallet");
            return;
        }

        int finalAmount = "revoke".equalsIgnoreCase(type) ? -rawAmount : rawAmount;

        try {
            boolean ok = walletDAO.adminAdjust(admin.getId(), userId, finalAmount, reason.trim());
            if (ok) {
                String actionNote = finalAmount > 0 ? "Cấp thành công +" + finalAmount + " xu"
                                                   : "Thu hồi thành công " + rawAmount + " xu";
                request.getSession().setAttribute("flash", "🎉 " + actionNote + " cho tài khoản #" + userId + "!");
            } else {
                request.getSession().setAttribute("flashError", "Không thể thu hồi xu: số dư ví không đủ hoặc số dư sẽ bị âm.");
            }
        } catch (SQLException e) {
            log("AdminWalletServlet: lỗi điều chỉnh xu", e);
            request.getSession().setAttribute("flashError", "Lỗi CSDL khi điều chỉnh ví xu: " + e.getMessage());
        }

        response.sendRedirect(request.getContextPath() + "/admin/wallet");
    }
}
