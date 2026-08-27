package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.dao.BookmarkDAO;
import truyen.dao.ChapterDAO;
import truyen.dao.CommentDAO;
import truyen.dao.StoryDAO;
import truyen.dao.TagDAO;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.SlugUtil;

/**
 * CASE 02, 03, 04, 05 — Truyện.
 *
 * URL: /story?action=list | detail | mine | create | edit | delete
 *
 * MỘT SERVLET CHO MỘT THỰC THỂ.
 * Tách mỗi thao tác một servlet thì 6 thực thể x 4 thao tác = 24 servlet.
 * Gộp lại còn 6. Mỗi action một method private, doGet chỉ điều phối.
 */
@WebServlet("/story")
public class StoryServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private TagDAO tagDAO;
    private ChapterDAO chapterDAO;
    private CommentDAO commentDAO;
    private BookmarkDAO bookmarkDAO;

    /** Số truyện mỗi trang. Đọc từ context-param nên đổi được mà khỏi biên dịch lại. */
    private int pageSize = 24;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        tagDAO = new TagDAO();
        chapterDAO = new ChapterDAO();
        commentDAO = new CommentDAO();
        bookmarkDAO = new BookmarkDAO();

        String cfg = getServletContext().getInitParameter("pageSize");
        if (cfg != null) {
            try {
                pageSize = Integer.parseInt(cfg);
            } catch (NumberFormatException ignore) {
                // web.xml ghi sai số -> dùng mặc định, không làm sập app
            }
        }
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
            action = "list";
        }

        String url;
        try {
            switch (action) {
                case "detail": url = detail(request, response); break;
                case "mine":   url = mine(request);             break;
                case "create": url = createOrEdit(request, response, true);  break;
                case "edit":   url = createOrEdit(request, response, false); break;
                case "delete": url = delete(request, response);  break;
                default:       url = list(request);              break;
            }
        } catch (SQLException e) {
            log("StoryServlet: lỗi truy vấn, action=" + action, e);
            request.setAttribute("message", "Không tải được dữ liệu. Vui lòng thử lại.");
            url = "/WEB-INF/views/story/list.jsp";
        }

        if (url == null) {
            return;    // method con đã redirect hoặc sendError xong
        }

        request.setAttribute("contentPage", url);
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }

    // ---- CASE 02 + 03: danh sách, phân trang, lọc tag ----------------------

    private String list(HttpServletRequest request) throws SQLException {
        String tag = request.getParameter("tag");
        String keyword = trim(request.getParameter("q"));
        String sort = request.getParameter("sort");
        int page = parseIntOr(request.getParameter("page"), 1);
        if (page < 1) {
            page = 1;   // ?page=-5 không được thành OFFSET âm
        }

        int total = storyDAO.countPage(tag, keyword);
        int totalPages = Math.max(1, (int) Math.ceil(total / (double) pageSize));
        if (page > totalPages) {
            page = totalPages;
        }

        List<Story> stories = storyDAO.findPage(tag, keyword, sort,
                                                (page - 1) * pageSize, pageSize);

        request.setAttribute("stories", stories);
        request.setAttribute("tags", tagDAO.findAllWithCount());
        request.setAttribute("currentTag", tag);
        request.setAttribute("keyword", keyword);
        request.setAttribute("sort", sort);
        request.setAttribute("page", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalStories", total);
        request.setAttribute("pageTitle", "Kho truyện");
        request.setAttribute("activeNav", "browse");
        return "/WEB-INF/views/story/list.jsp";
    }

    // ---- CASE 04: chi tiết -------------------------------------------------

    private String detail(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        int id = parseIntOr(request.getParameter("id"), 0);
        Story story = storyDAO.findById(id);

        if (story == null || "DELETED".equals(story.getStatus())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        /*
         * Truyện DRAFT chỉ tác giả và admin xem được.
         * Trả 404 chứ không phải 403 — cố ý. 403 vô tình xác nhận "truyện này
         * CÓ tồn tại", còn 404 thì không tiết lộ gì cả.
         */
        User me = currentUser(request);
        if ("DRAFT".equals(story.getStatus()) && !canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        storyDAO.increaseView(id);

        request.setAttribute("story", story);
        request.setAttribute("tags", tagDAO.findByStory(id));
        request.setAttribute("chapters", chapterDAO.findByStory(id));
        request.setAttribute("comments", commentDAO.findByStory(id));
        request.setAttribute("canEdit", canEdit(me, story));
        if (me != null) {
            request.setAttribute("bookmarked", bookmarkDAO.exists(me.getId(), id));
        }
        request.setAttribute("pageTitle", story.getTitle());
        request.setAttribute("activeNav", "browse");
        return "/WEB-INF/views/story/detail.jsp";
    }

    /** Truyện của tôi — gồm cả bản nháp. */
    private String mine(HttpServletRequest request) throws SQLException {
        User me = currentUser(request);
        request.setAttribute("stories", storyDAO.findByAuthor(me.getId()));
        request.setAttribute("pageTitle", "Truyện của tôi");
        request.setAttribute("mine", true);
        return "/WEB-INF/views/story/mine.jsp";
    }

    // ---- CASE 05: đăng / sửa ----------------------------------------------

    private String createOrEdit(HttpServletRequest request, HttpServletResponse response,
                                boolean isCreate) throws SQLException, IOException {

        User me = currentUser(request);
        Story story;

        if (isCreate) {
            story = new Story();
            story.setAuthorId(me.getId());
            story.setStatus("DRAFT");
            story.setProgress("ONGOING");
        } else {
            story = storyDAO.findById(parseIntOr(request.getParameter("id"), 0));
            if (story == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            /*
             * KIỂM TRA QUYỀN SỞ HỮU — không chỉ kiểm vai trò.
             *
             * AuthFilter đã chặn khách chưa đăng nhập, nhưng nó KHÔNG biết
             * truyện id=6 là của ai. Thiếu đoạn này thì người dùng A sửa
             * ?id=5 thành ?id=6 là sửa được truyện của người dùng B.
             */
            if (!canEdit(me, story)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        }

        // GET = chỉ hiện form
        if (!"POST".equals(request.getMethod())) {
            request.setAttribute("story", story);
            request.setAttribute("allTags", tagDAO.findAllWithCount());
            request.setAttribute("selectedTags", isCreate
                    ? java.util.Collections.emptyList()
                    : tagDAO.findByStory(story.getId()));
            request.setAttribute("pageTitle", isCreate ? "Đăng truyện mới" : "Sửa truyện");
            return "/WEB-INF/views/story/form.jsp";
        }

        // POST = lưu
        String title = trim(request.getParameter("title"));
        String description = trim(request.getParameter("description"));
        String coverUrl = trim(request.getParameter("coverUrl"));
        String status = request.getParameter("status");
        String progress = request.getParameter("progress");
        String[] tagIds = request.getParameterValues("tagIds");

        story.setTitle(title);
        story.setDescription(description);
        story.setCoverUrl(coverUrl.isEmpty() ? null : coverUrl);
        story.setStatus("PUBLISHED".equals(status) ? "PUBLISHED" : "DRAFT");
        story.setProgress("COMPLETED".equals(progress) ? "COMPLETED" : "ONGOING");

        if (title.isEmpty()) {
            request.setAttribute("message", "Tiêu đề không được để trống.");
            request.setAttribute("story", story);
            request.setAttribute("allTags", tagDAO.findAllWithCount());
            request.setAttribute("pageTitle", isCreate ? "Đăng truyện mới" : "Sửa truyện");
            return "/WEB-INF/views/story/form.jsp";
        }

        story.setSlug(uniqueSlug(title, story.getId()));

        if (isCreate) {
            storyDAO.insert(story);
        } else {
            storyDAO.update(story);
        }
        tagDAO.setTagsForStory(story.getId(), tagIds);

        // Post/Redirect/Get — F5 sau khi lưu không gửi lại form
        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + story.getId());
        return null;
    }

    private String delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        Story story = storyDAO.findById(parseIntOr(request.getParameter("id"), 0));
        if (story == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (!canEdit(currentUser(request), story)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        storyDAO.softDelete(story.getId());   // xoá MỀM
        response.sendRedirect(request.getContextPath() + "/story?action=mine");
        return null;
    }

    // ---- tiện ích ----------------------------------------------------------

    /**
     * Sinh slug chưa ai dùng. Trùng thì thêm số: tien-hiep, tien-hiep-2...
     * Thử tối đa 50 lần rồi bỏ cuộc, tránh vòng lặp vô hạn nếu database lỗi.
     */
    private String uniqueSlug(String title, int exceptId) throws SQLException {
        String base = SlugUtil.toSlug(title);
        if (base.isEmpty()) {
            base = "truyen";
        }
        for (int n = 1; n <= 50; n++) {
            String candidate = SlugUtil.withSuffix(base, n);
            if (!storyDAO.slugExists(candidate, exceptId)) {
                return candidate;
            }
        }
        return base + "-" + System.currentTimeMillis();
    }

    /** Chủ truyện hoặc admin thì được sửa. */
    private boolean canEdit(User user, Story story) {
        return user != null
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
            return fallback;    // ?id=abc không được làm sập trang
        }
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
