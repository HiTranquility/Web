package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.ViewLogDAO;
import truyen.model.ReadHistory;
import truyen.model.User;

/** TRANG 31 — Lịch sử đọc. */
@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    private ViewLogDAO viewLogDAO;

    /**
     * Chỉ giữ 50 truyện gần nhất.
     *
     * Không phân trang: lịch sử là để tìm lại thứ vừa đọc, mà thứ "vừa đọc"
     * thì luôn nằm ở đầu. Ai lật tới trang 4 của lịch sử thì thật ra họ đang
     * cần ô tìm kiếm, không phải thêm trang.
     */
    private static final int LIMIT = 50;

    @Override
    public void init() throws ServletException {
        viewLogDAO = new ViewLogDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User me = requireLogin(request, response);
        if (me == null) {
            return;
        }

        try {
            List<ReadHistory> history = viewLogDAO.findByUser(me.getId(), LIMIT);
            request.setAttribute("history", history);
            request.setAttribute("totalRead", viewLogDAO.countStories(me.getId()));

        } catch (SQLException e) {
            log("HistoryServlet: lỗi truy vấn lịch sử đọc", e);
            request.setAttribute("message", "Không tải được lịch sử đọc.");
            request.setAttribute("history", java.util.Collections.emptyList());
            request.setAttribute("totalRead", 0);
        }

        show(request, response);
    }

    /**
     * Xoá lịch sử.
     *
     * POST chứ không GET: đây là hành động THAY ĐỔI dữ liệu. Để ở GET thì chỉ
     * cần một thẻ <img src="/history?action=clear"> trên trang bất kỳ là xoá
     * sạch lịch sử của người ghé qua, mà họ không hề bấm gì.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        User me = requireLogin(request, response);
        if (me == null) {
            return;
        }

        if ("clear".equals(request.getParameter("action"))) {
            try {
                int n = viewLogDAO.clearForUser(me.getId());
                request.getSession().setAttribute("flash",
                        n == 0 ? "Lịch sử vốn đã trống."
                               : "Đã xoá lịch sử đọc (" + n + " lượt).");
            } catch (SQLException e) {
                log("HistoryServlet: không xoá được lịch sử", e);
                request.getSession().setAttribute("flash", "Không xoá được lịch sử.");
            }
        }

        /*
         * Post/Redirect/Get: kết thúc bằng redirect nên F5 sau đó chỉ tải lại
         * trang, không hỏi "gửi lại biểu mẫu?" rồi xoá thêm lần nữa.
         */
        response.sendRedirect(request.getContextPath() + "/history");
    }

    /** Trả về người đang đăng nhập, hoặc null KÈM redirect nếu chưa. */
    private User requireLogin(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8");
        User me = (User) request.getSession().getAttribute("currentUser");
        if (me == null) {
            /* Nhớ đường quay lại, y như AuthFilter và AdminFilter. */
            request.getSession(true).setAttribute("redirectAfterLogin",
                    request.getRequestURI());
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }
        return me;
    }

    private void show(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setAttribute("pageTitle", "Lịch sử đọc");
        request.setAttribute("activeNav", "history");
        request.setAttribute("contentPage", "/WEB-INF/views/user/history.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }
}
