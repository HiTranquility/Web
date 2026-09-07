package truyen.controller.admin;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.TagDAO;
import truyen.model.Tag;
import truyen.util.SlugUtil;

/**
 * TRANG 28 — Quản lý thể loại.
 *
 * TẦNG: controller/admin/
 *
 * URL: /admin/tag?action=list | create | update | delete
 */
@WebServlet("/admin/tag")
public class AdminTagServlet extends HttpServlet {

    private TagDAO tagDAO;

    @Override
    public void init() throws ServletException {
        tagDAO = new TagDAO();
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
            if ("create".equals(action) || "update".equals(action)) {
                save(request, "update".equals(action));
            } else if ("delete".equals(action)) {
                remove(request);
            }
            request.setAttribute("tags", tagDAO.findAllWithCount());

        } catch (SQLException e) {
            log("AdminTagServlet: lỗi truy vấn, action=" + action, e);
            request.setAttribute("message", "Không thao tác được với thể loại.");
        }

        request.setAttribute("pageTitle", "Quản trị — Thể loại");
        request.setAttribute("activeNav", "admin");
        request.setAttribute("adminSection", "tag");
        request.setAttribute("contentPage", "/WEB-INF/views/admin/tags.jsp");
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/admin.jsp")
                .forward(request, response);
    }

    /**
     * Thêm mới hoặc đổi tên.
     *
     * SLUG SINH TỰ ĐỘNG TỪ TÊN, không cho admin tự gõ.
     *   Slug là phần xuất hiện trong URL (/story?action=list&tag=ngon-tinh).
     *   Để gõ tay thì sẽ có người gõ "Ngôn Tình" kèm dấu cách và dấu tiếng
     *   Việt — URL hỏng, hoặc thành %C3%B4 dài loằng ngoằng.
     *
     * KHI SỬA, SLUG CŨ ĐƯỢC GIỮ NGUYÊN.
     *   Đổi tên "Ngôn tình" thành "Ngôn tình hiện đại" mà slug đổi theo là mọi
     *   đường dẫn đã chia sẻ trước đó chết hết. Tên hiển thị và định danh là
     *   hai thứ khác nhau — đây cũng chính là lý do username không cho đổi.
     */
    private void save(HttpServletRequest request, boolean isUpdate) throws SQLException {
        String name = trim(request.getParameter("name"));
        if (name.isEmpty()) {
            request.setAttribute("message", "Tên thể loại không được để trống.");
            return;
        }

        Tag tag = new Tag();
        tag.setName(name);

        if (isUpdate) {
            tag.setId(parseIntOr(request.getParameter("id"), 0));
            tagDAO.update(tag);          // TagDAO.update chỉ sửa cột name
        } else {
            String slug = SlugUtil.toSlug(name);

            // Trùng slug thì thêm số phía sau: ngon-tinh, ngon-tinh-2, ...
            String base = slug;
            int n = 2;
            while (tagDAO.slugExists(slug, 0)) {
                slug = base + "-" + n;
                n++;
            }
            tag.setSlug(slug);
            tagDAO.insert(tag);
        }
    }

    /**
     * Xoá thể loại.
     *
     * KHÔNG XOÁ THỂ LOẠI CÒN TRUYỆN.
     *   Khoá ngoại của story_tags có ON DELETE CASCADE, nên xoá thể loại là
     *   xoá luôn mọi liên kết truyện–thể loại đó, im lặng và không hoàn tác
     *   được. Chặn ở đây để cái CASCADE kia không bao giờ có cơ hội chạy.
     *
     *   Admin muốn xoá thật thì phải gỡ thể loại khỏi từng truyện trước —
     *   phiền, nhưng đó là loại phiền có ích.
     */
    private void remove(HttpServletRequest request) throws SQLException {
        int id = parseIntOr(request.getParameter("id"), 0);

        for (Tag t : tagDAO.findAllWithCount()) {
            if (t.getId() == id && t.getStoryCount() > 0) {
                request.setAttribute("message",
                        "Không xoá được: thể loại \"" + t.getName() + "\" còn "
                        + t.getStoryCount() + " truyện. Gỡ thể loại khỏi các "
                        + "truyện đó trước.");
                return;
            }
        }
        tagDAO.delete(id);
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
