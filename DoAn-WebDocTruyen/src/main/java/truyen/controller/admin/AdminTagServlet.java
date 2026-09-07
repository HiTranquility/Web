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

    /** Thêm mới hoặc đổi tên. */
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

    /** Xoá thể loại. */
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
