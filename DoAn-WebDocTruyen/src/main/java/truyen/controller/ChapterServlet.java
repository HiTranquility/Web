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
import truyen.dao.FollowDAO;
import truyen.dao.NotificationDAO;
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
    private FollowDAO followDAO;
    private NotificationDAO notificationDAO;
    private StoryDAO storyDAO;
    private BookmarkDAO bookmarkDAO;

    @Override
    public void init() throws ServletException {
        chapterDAO = new ChapterDAO();
        followDAO = new FollowDAO();
        notificationDAO = new NotificationDAO();
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

        /*
         * Mỗi action tự chọn layout của mình — đây là chỗ khác StoryServlet.
         *
         * Servlet này một mình dùng BA layout khác nhau, và đó là lý do khối
         * chọn layout nằm ngay trong switch chứ không đặt cứng ở cuối:
         *     đọc chương    -> reader  (không nav, không footer, chữ to)
         *     soạn chương   -> editor  (không footer, cột hẹp, ô soạn cao)
         *     xoá / còn lại -> main
         */
        String url;
        String layout = "/WEB-INF/views/layout/main.jsp";

        try {
            switch (action) {
                case "create":
                    url = createOrEdit(request, response, true);
                    layout = "/WEB-INF/views/layout/editor.jsp";   // khung soạn thảo
                    break;
                case "edit":
                    url = createOrEdit(request, response, false);
                    layout = "/WEB-INF/views/layout/editor.jsp";
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
            request.setAttribute("editorBack",
                    "/story?action=detail&id=" + story.getId());
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
            notifyFollowers(story, chapter);
        } else {
            chapterDAO.update(chapter);
            // Sửa chương KHÔNG gửi thông báo. Tác giả sửa lỗi chính tả ba lần
            // mà người theo dõi nhận ba thông báo "có chương mới" là cách
            // nhanh nhất khiến họ tắt thông báo vĩnh viễn.
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


    /**
     * Báo cho những người đang theo dõi tác giả rằng có chương mới.
     *
     * VÌ SAO BỌC TRONG try-catch RIÊNG VÀ NUỐT LỖI
     *   Chương ĐÃ được lưu ở dòng trên. Nếu bảng notifications trục trặc mà
     *   để ngoại lệ bay lên, tác giả sẽ thấy trang lỗi 500 và tưởng chương
     *   chưa lưu — rồi bấm đăng lại, thành hai chương trùng.
     *
     *   Thông báo là việc phụ. Việc phụ hỏng thì không được kéo việc chính
     *   xuống theo. Ghi log để người bảo trì biết, nhưng người dùng không cần
     *   biết và cũng không làm gì được.
     *
     * VÌ SAO CÂU THÔNG BÁO ĐƯỢC GHÉP SẴN Ở ĐÂY
     *   Tên truyện hôm nay có thể đổi ngày mai. Thông báo là ảnh chụp một
     *   thời điểm nên phải giữ nguyên câu chữ lúc gửi — xem ghi chú trong
     *   model/Notification.java.
     */
    private void notifyFollowers(Story story, Chapter chapter) {
        try {
            List<Integer> followers = followDAO.findFollowerIds(story.getAuthorId());
            if (followers.isEmpty()) {
                return;   // không ai theo dõi thì khỏi mở kết nối
            }
            String message = story.getAuthorName() + " vừa đăng chương "
                           + chapter.getChapterNo() + " của \"" + story.getTitle() + "\"";
            notificationDAO.notifyFollowers(followers, story.getId(),
                                            chapter.getId(), message);
        } catch (SQLException e) {
            log("Không gửi được thông báo chương mới cho truyện " + story.getId(), e);
        }
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
