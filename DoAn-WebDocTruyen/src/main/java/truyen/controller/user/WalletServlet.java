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
import truyen.util.JsonUtil;

/**
 * Xử lý các nghiệp vụ ví xu ảo và tặng thưởng tác giả (ISSUE-008).
 */
@WebServlet(name = "WalletServlet", urlPatterns = "/wallet")
public class WalletServlet extends HttpServlet {

    private final WalletDAO walletDAO = new WalletDAO();
    private final truyen.dao.NotificationDAO notificationDAO = new truyen.dao.NotificationDAO();

    /** Trần nạp xu MÔ PHỎNG mỗi người mỗi ngày — xem handleBuy(). */
    static final int MAX_TOPUPS_PER_DAY = 3;
    static final int MAX_TOPUP_COINS_PER_DAY = 2000;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        if ("balance".equals(action)) {
            handleBalance(req, resp);
        } else if (action == null || "topup".equals(action) || "history".equals(action) || "view".equals(action)) {
            handleTopupView(req, resp);
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
        } else if ("buy".equals(action) || "topup".equals(action)) {
            handleBuy(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void handleBalance(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Chưa đăng nhập");
            return;
        }

        try {
            int balance = walletDAO.getBalance(currentUser.getId());
            JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, "{\"balance\":" + balance + "}");
        } catch (SQLException e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi CSDL");
        }
    }

    private void handleTip(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        User currentUser = session != null ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_UNAUTHORIZED,
                    "{\"success\":false,\"message\":\"Vui lòng đăng nhập để tặng xu\"}");
            return;
        }

        int toUserId = parseInt(req.getParameter("toUserId"), 0);
        int amount = parseInt(req.getParameter("amount"), 0);
        Integer storyId = parseInteger(req.getParameter("storyId"));
        String message = req.getParameter("message");

        if (toUserId <= 0 || toUserId == currentUser.getId()) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_OK,
                    "{\"success\":false,\"message\":\"Không thể tự tặng xu cho chính mình\"}");
            return;
        }

        if (amount <= 0 || amount > 1000) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_OK,
                    "{\"success\":false,\"message\":\"Số xu tặng không hợp lệ (tối đa 1.000 xu)\"}");
            return;
        }

        try {
            boolean ok = walletDAO.transfer(currentUser.getId(), toUserId, storyId, amount, message);
            if (ok) {
                int newBalance = walletDAO.getBalance(currentUser.getId());
                try {
                    String senderName = currentUser.getName() != null && !currentUser.getName().isEmpty()
                            ? currentUser.getName() : currentUser.getUsername();
                    String notifMsg = senderName + " đã tặng bạn " + amount + " xu"
                            + (message != null && !message.trim().isEmpty() ? " với lời nhắn: \"" + message.trim() + "\"" : ".");
                    notificationDAO.sendNotification(toUserId, storyId, null, "TIP", notifMsg);
                } catch (Exception ignore) {
                    // Không ngắt luồng nếu lỗi ghi thông báo
                }
                JsonUtil.sendJson(resp, HttpServletResponse.SC_OK, "{\"success\":true,\"newBalance\":" + newBalance
                        + ",\"message\":\"✨ Tặng " + amount + " xu cho tác giả thành công!\"}");
            } else {
                JsonUtil.sendJson(resp, HttpServletResponse.SC_OK,
                        "{\"success\":false,\"message\":\"Số dư xu không đủ để thực hiện giao dịch\"}");
            }
        } catch (SQLException e) {
            log("WalletServlet: lỗi tặng xu", e);
            JsonUtil.sendError(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Sự cố hệ thống máy chủ");
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

    private void handleTopupView(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = requireLogin(req, resp);
        if (currentUser == null) return;

        try {
            int balance = walletDAO.getBalance(currentUser.getId());
            java.util.List<truyen.model.Transaction> history = walletDAO.findHistoryByUserId(currentUser.getId(), 25);
            req.setAttribute("walletBalance", balance);
            req.setAttribute("history", history);
            req.setAttribute("pageTitle", "Ví Xu & Nạp Xu");
            req.setAttribute("activeNav", "wallet");
            req.setAttribute("contentPage", "/WEB-INF/views/user/wallet.jsp");
            req.getRequestDispatcher("/WEB-INF/views/layout/main.jsp").forward(req, resp);
        } catch (SQLException e) {
            log("WalletServlet: lỗi truy vấn ví xu", e);
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void handleBuy(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        User currentUser = requireLogin(req, resp);
        if (currentUser == null) return;

        String packageId = req.getParameter("packageId");
        int coins = 0;
        int price = 0;
        String packageName = "";

        if ("pkg_20k".equals(packageId)) {
            coins = 55; // 50 + 5 bonus
            price = 20000;
            packageName = "Gói Khởi Động (50 + 5 xu)";
        } else if ("pkg_50k".equals(packageId)) {
            coins = 135; // 120 + 15 bonus
            price = 50000;
            packageName = "Gói Phổ Thông (120 + 15 xu)";
        } else if ("pkg_100k".equals(packageId)) {
            coins = 300; // 260 + 40 bonus
            price = 100000;
            packageName = "Gói Tiết Kiệm (260 + 40 xu)";
        } else if ("pkg_200k".equals(packageId)) {
            coins = 650; // 550 + 100 bonus
            price = 200000;
            packageName = "Gói Bạc (550 + 100 xu)";
        } else if ("pkg_500k".equals(packageId)) {
            coins = 1850; // 1500 + 350 bonus
            price = 500000;
            packageName = "Gói Kim Cương (1500 + 350 xu)";
        } else {
            coins = parseInt(req.getParameter("coins"), 0);
            if (coins > 0 && coins <= MAX_TOPUP_COINS_PER_DAY) {
                price = coins * 400;
                packageName = "Gói Tuỳ Chọn (" + coins + " xu)";
            }
        }

        if (coins <= 0) {
            if (isAjax(req)) {
                resp.setContentType("application/json; charset=UTF-8");
                resp.getWriter().write("{\"success\":false,\"message\":\"Gói nạp xu không hợp lệ.\"}");
            } else {
                req.getSession().setAttribute("flashError", "Gói nạp xu không hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/wallet?action=topup");
            }
            return;
        }

        String paymentMethod = req.getParameter("paymentMethod");
        String methodName = "Cổng thanh toán";
        if ("vnpay".equalsIgnoreCase(paymentMethod)) {
            methodName = "VNPAY QR";
        } else if ("momo".equalsIgnoreCase(paymentMethod)) {
            methodName = "Ví MoMo";
        } else if ("bank_transfer".equalsIgnoreCase(paymentMethod)) {
            methodName = "Chuyển khoản VietQR";
        } else if ("card".equalsIgnoreCase(paymentMethod)) {
            methodName = "Thẻ cào";
        }

        /*
         * NẠP XU Ở ĐÂY LÀ MÔ PHỎNG — không có cổng thanh toán thật.
         *
         * Không chặn gì thì ai cũng gọi thẳng POST này để có xu vô hạn, rồi
         * dùng xu đó mở mọi chương VIP và "tặng" xu cho tài khoản phụ — toàn
         * bộ cơ chế chương trả phí thành vô nghĩa. Hai lớp chặn:
         *   1. context-param demoTopup trong web.xml: đặt false là TẮT hẳn
         *      (khi nào tích hợp VNPAY/MoMo thật thì tắt mô phỏng đi).
         *   2. Trần mỗi ngày: tối đa MAX_TOPUPS_PER_DAY lần và
         *      MAX_TOPUP_COINS_PER_DAY xu — đủ để demo đồ án, không đủ để phá.
         */
        if (!"true".equalsIgnoreCase(getServletContext().getInitParameter("demoTopup"))) {
            replyBuy(req, resp, false, "Nạp xu đang tắt — cổng thanh toán thật chưa được tích hợp.");
            return;
        }
        try {
            int[] today = walletDAO.topupToday(currentUser.getId());
            if (today[0] >= MAX_TOPUPS_PER_DAY || today[1] + coins > MAX_TOPUP_COINS_PER_DAY) {
                replyBuy(req, resp, false, "Hôm nay bạn đã nạp " + today[0] + " lần (" + today[1]
                        + " xu). Bản mô phỏng giới hạn " + MAX_TOPUPS_PER_DAY + " lần / "
                        + MAX_TOPUP_COINS_PER_DAY + " xu mỗi ngày — điểm danh để nhận thêm xu nhé!");
                return;
            }
        } catch (SQLException e) {
            log("WalletServlet: không kiểm được trần nạp xu", e);
            replyBuy(req, resp, false, "Lỗi máy chủ khi nạp xu.");
            return;
        }

        String txMessage = "Nạp " + coins + " xu (" + packageName + ") qua " + methodName;

        try {
            boolean ok = walletDAO.addCoins(currentUser.getId(), coins, txMessage);
            if (ok) {
                int newBalance = walletDAO.getBalance(currentUser.getId());
                if (isAjax(req)) {
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"success\":true,\"coins\":" + coins
                            + ",\"newBalance\":" + newBalance
                            + ",\"message\":\"🎉 Nạp thành công " + coins + " xu vào ví!\"}");
                } else {
                    req.getSession().setAttribute("flash",
                            "🎉 Nạp thành công " + coins + " xu vào ví (" + packageName + ")! Chúc bạn đọc truyện vui vẻ.");
                    resp.sendRedirect(req.getContextPath() + "/wallet?action=topup");
                }
            } else {
                if (isAjax(req)) {
                    resp.setContentType("application/json; charset=UTF-8");
                    resp.getWriter().write("{\"success\":false,\"message\":\"Không thể thực hiện nạp xu. Vui lòng thử lại.\"}");
                } else {
                    req.getSession().setAttribute("flashError", "Không thể nạp xu lúc này. Vui lòng thử lại sau.");
                    resp.sendRedirect(req.getContextPath() + "/wallet?action=topup");
                }
            }
        } catch (SQLException e) {
            log("WalletServlet: lỗi nạp xu", e);
            if (isAjax(req)) {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"success\":false,\"message\":\"Lỗi máy chủ khi nạp xu.\"}");
            } else {
                req.getSession().setAttribute("flashError", "Lỗi máy chủ khi nạp xu.");
                resp.sendRedirect(req.getContextPath() + "/wallet?action=topup");
            }
        }
    }

    /** Trả lời yêu cầu nạp: JSON cho AJAX, flash + quay lại trang ví cho form thường. */
    private void replyBuy(HttpServletRequest req, HttpServletResponse resp, boolean success, String message)
            throws IOException {
        if (isAjax(req)) {
            JsonUtil.sendJson(resp, HttpServletResponse.SC_OK,
                    "{\"success\":" + success + ",\"message\":" + JsonUtil.jsonEscape(message) + "}");
        } else {
            req.getSession().setAttribute(success ? "flash" : "flashError", message);
            resp.sendRedirect(req.getContextPath() + "/wallet?action=topup");
        }
    }

    private User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        User me = session != null ? (User) session.getAttribute("currentUser") : null;
        if (me == null) {
            request.getSession(true).setAttribute("redirectAfterLogin",
                    request.getRequestURI() + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }
        return me;
    }

    private boolean isAjax(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xrw) || (accept != null && accept.contains("application/json"));
    }
}
