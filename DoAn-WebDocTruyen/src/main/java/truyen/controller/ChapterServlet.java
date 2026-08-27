package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.BookmarkDAO;
import truyen.dao.ChapterDAO;
import truyen.dao.StoryDAO;
import truyen.model.Chapter;
import truyen.model.Story;
import truyen.model.User;

/**
 * CASE 06 — Chương truyện.
 *
 * URL: /chapter?action=read | create | edit | delete
 *
 * Trang đọc dùng layout `reader` — bỏ hết nav và footer để không có gì phân
 * tán khi đọc. Đây là ví dụ rõ nhất cho luật "layout mới chỉ khi KHUNG khác".
 */
@WebServlet("/chapter")
public class ChapterServlet extends HttpServlet {

    private ChapterDAO chapterDAO;
    private StoryDAO storyDAO;
    private BookmarkDAO bookmarkDAO;

    @Override
    public void init() throws ServletException {
        chapterDAO = new ChapterDAO();
        storyDAO = new StoryDAO();
        bookmarkDAO = new BookmarkDAO();
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
        if (action == null) {
            action = "read";
        }

        // Mỗi action tự chọn layout của mình — đây là chỗ khác StoryServlet
        String url;
        String layout = "/WEB-INF/views/layout/main.jsp";

        try {
            switch (action) {
                case "create":
                    url = createOrEdit(request, response, true);
                    break;
                case "edit":
                    url = createOrEdit(request, response, false);
                    break;
                case "delete":
                    url = delete(request, response);
                    break;
                default:
                    url = read(request, response);
                    layout = "/WEB-INF/views/layout/reader.jsp";   // khung đọc
                    break;
            }
        } catch (SQLException e) {
            log("ChapterServlet: lỗi truy vấn, action=" + action, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (url == null) {
            return;
        }

        request.setAttribute("contentPage", url);
        getServletContext().getRequestDispatcher(layout).forward(request, response);
    }

    // ---- đọc chương --------------------------------------------------------

    private String read(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseIntOr(request.getParameter("id"), 0);
        Chapter chapter = chapterDAO.findById(id);
        if (chapter == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        Story story = storyDAO.findById(chapter.getStoryId());
        if (story == null || "DELETED".equals(story.getStatus())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        /*
         * Tự động ghi lại vị trí đọc cho người đã đăng nhập.
         *
         * Người dùng không phải bấm gì — mở chương là hệ thống nhớ. Lần sau
         * vào trang "Truyện đã lưu" sẽ thấy nút "Đọc tiếp chương N".
         *
         * Bọc try/catch riêng: ghi vị trí đọc thất bại KHÔNG được làm hỏng
         * việc đọc truyện. Đây là chức năng phụ, không phải chức năng chính.
         */
        User me = currentUser(request);
        if (me != null) {
            try {
                bookmarkDAO.updateProgress(me.getId(), story.getId(), chapter.getId());
            } catch (SQLException e) {
                log("Không lưu được vị trí đọc, userId=" + me.getId(), e);
            }
        }

        request.setAttribute("chapter", chapter);
        request.setAttribute("story", story);
        request.setAttribute("prev",
                chapterDAO.findNeighbour(story.getId(), chapter.getChapterNo(), -1));
        request.setAttribute("next",
                chapterDAO.findNeighbour(story.getId(), chapter.getChapterNo(), +1));
        request.setAttribute("pageTitle",
                "Chương " + chapter.getChapterNo() + " — " + story.getTitle());
        return "/WEB-INF/views/chapter/read.jsp";
    }

    // ---- thêm / sửa chương -------------------------------------------------

    private String createOrEdit(HttpServletRequest request, HttpServletResponse response,
                                boolean isCreate) throws SQLException, IOException {

        User me = currentUser(request);
        Chapter chapter;
        Story story;

        if (isCreate) {
            story = storyDAO.findById(parseIntOr(request.getParameter("storyId"), 0));
            if (story == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            chapter = new Chapter();
            chapter.setStoryId(story.getId());
            chapter.setChapterNo(chapterDAO.nextChapterNo(story.getId()));
        } else {
            chapter = chapterDAO.findById(parseIntOr(request.getParameter("id"), 0));
            if (chapter == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            story = storyDAO.findById(chapter.getStoryId());
        }

        // Quyền sở hữu: chỉ tác giả truyện (hoặc admin) mới thêm/sửa chương
        if (!canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        if (!"POST".equals(request.getMethod())) {
            request.setAttribute("chapter", chapter);
            request.setAttribute("story", story);
            request.setAttribute("pageTitle", isCreate ? "Thêm chương" : "Sửa chương");
            return "/WEB-INF/views/chapter/form.jsp";
        }

        String title = trim(request.getParameter("title"));
        String content = request.getParameter("content");
        int chapterNo = parseIntOr(request.getParameter("chapterNo"), chapter.getChapterNo());

        chapter.setTitle(title);
        chapter.setContent(content == null ? "" : content);
        chapter.setChapterNo(chapterNo);

        if (title.isEmpty() || chapter.getContent().trim().isEmpty()) {
            request.setAttribute("message", "Tiêu đề và nội dung chương không được để trống.");
            request.setAttribute("chapter", chapter);
            request.setAttribute("story", story);
            request.setAttribute("pageTitle", isCreate ? "Thêm chương" : "Sửa chương");
            return "/WEB-INF/views/chapter/form.jsp";
        }

        if (isCreate) {
            chapterDAO.insert(chapter);
        } else {
            chapterDAO.update(chapter);
        }

        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + story.getId());
        return null;
    }

    private String delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Chapter chapter = chapterDAO.findById(parseIntOr(request.getParameter("id"), 0));
        if (chapter == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Story story = storyDAO.findById(chapter.getStoryId());
        if (!canEdit(currentUser(request), story)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        chapterDAO.delete(chapter.getId());
        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + story.getId());
        return null;
    }

    // ---- tiện ích ----------------------------------------------------------

    private boolean canEdit(User user, Story story) {
        return user != null && story != null
                && (user.getId() == story.getAuthorId() || user.isAdmin());
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
