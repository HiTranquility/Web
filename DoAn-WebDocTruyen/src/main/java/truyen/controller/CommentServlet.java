package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.util.DBConnection;
import truyen.dao.CommentDAO;
import truyen.model.Comment;
import truyen.model.User;

/** CASE 07 — Bình luận. */
@WebServlet("/comment")
public class CommentServlet extends HttpServlet {

    /** Giới hạn độ dài — khớp với VARCHAR(1000) của cột content trong database. */
    private static final int MAX_LENGTH = 1000;

    private CommentDAO commentDAO;

    @Override
    public void init() throws ServletException {
        commentDAO = new CommentDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        if (action == null) {
            action = "add";
        }

        int storyId = parseIntOr(request.getParameter("storyId"), 0);

        try {
            if ("delete".equals(action)) {
                delete(request, response);
            } else {
                add(request, storyId);
            }
        } catch (SQLException e) {
            log("CommentServlet: lỗi truy vấn, action=" + action, e);

            /*
             * KHONG nem trang 500 khi nguyen nhan la CHUA CO CSDL.
             *
             * O che do xem giao dien, moi lenh GHI deu that bai — dung nhu
             * thiet ke. Nhung tra ve trang 500 thi nguoi dung tuong web hong,
             * trong khi thuc te chi la chua chay setup-db.ps1.
             *
             * Noi ro nguyen nhan roi tra ho ve cho cu. Chi loi THAT SU bat ngo
             * moi dang mot trang 500.
             */
            if (!DBConnection.isReady()) {
                request.getSession().setAttribute("flash",
                        "Chưa nối cơ sở dữ liệu nên chưa lưu được. "
                        + "Chạy scripts\\setup-db.ps1 rồi tạo db.properties.");
                response.sendRedirect(request.getContextPath()
                        + "/story?action=detail&id=" + storyId);
                return;
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId + "#comments");
    }

    /**
     * Chỉ nhận POST. Không override doGet.
     *
     * Bình luận là hành động GHI, nên phải là POST — quy tắc chọn GET/POST ở
     * standards 01. Để GET thì người ta gửi được link
     * "/comment?action=add&content=spam" cho người khác bấm nhầm.
     */
    private void add(HttpServletRequest request, int storyId) throws SQLException {
        User me = currentUser(request);
        String content = trim(request.getParameter("content"));

        // Rỗng thì bỏ qua lặng lẽ — không cần báo lỗi cho một ô trống
        if (me == null || storyId <= 0 || content.isEmpty()) {
            return;
        }
        if (content.length() > MAX_LENGTH) {
            content = content.substring(0, MAX_LENGTH);
        }

        Comment c = new Comment();
        c.setStoryId(storyId);

        /*
         * parentId co thi day la TRA LOI, khong co thi la binh luan goc.
         *
         * Khong kiem o day xem cha co thuoc dung truyen nay khong —
         * CommentDAO.insert() da tra lai bang findById va tu ep ve goc.
         * Kiem hai lan cung mot thu o hai tang la cach chac chan de mot ngay
         * nao do hai noi lech nhau.
         */
        int parentId = parseIntOr(request.getParameter("parentId"), 0);
        c.setParentId(parentId > 0 ? parentId : null);
        c.setUserId(me.getId());
        c.setContent(content);
        commentDAO.insert(c);
    }

    /**
     * Ẩn bình luận. Người viết tự gỡ được, admin gỡ được của bất kỳ ai.
     *
     * KIỂM TRA QUYỀN SỞ HỮU — giống hệt truyện. Thiếu đoạn này thì ai cũng
     * xoá được bình luận của người khác chỉ bằng cách đổi id trong form.
     */
    private void delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        User me = currentUser(request);
        Comment c = commentDAO.findById(parseIntOr(request.getParameter("id"), 0));

        if (c == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if (me == null || (me.getId() != c.getUserId() && !me.isAdmin())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        commentDAO.hide(c.getId());   // XOÁ MỀM — giữ lại làm bằng chứng
    }

    private User currentUser(HttpServletRequest request) {
        return request.getSession(false) == null
                ? null
                : (User) request.getSession(false).getAttribute("currentUser");
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
