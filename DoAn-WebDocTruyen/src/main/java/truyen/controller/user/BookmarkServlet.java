package truyen.controller.user;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.util.ChapterAccess;
import truyen.util.DBConnection;
import truyen.util.JsonUtil;
import truyen.util.ServletHelper;

import static truyen.util.ServletHelper.parseIntOr;
import truyen.dao.BookmarkDAO;
import truyen.dao.ChapterBookmarkDAO;
import truyen.dao.ChapterDAO;
import truyen.dao.ChapterPageDAO;
import truyen.dao.ReadingProgressDAO;
import truyen.dao.StoryDAO;
import truyen.dao.UnlockDAO;
import truyen.model.Chapter;
import truyen.model.ChapterBookmark;
import truyen.model.ReadingProgress;
import truyen.model.Story;
import truyen.model.User;

/**
 * CASE 08 — Truyện đã lưu, đoạn đánh dấu và vị trí đọc.
 *
 * URL: /bookmark?action=
 *   list      (GET)  — tủ sách; &tab=marks để xem đoạn đã đánh dấu
 *   add       (POST) — lưu truyện
 *   remove    (POST) — bỏ lưu truyện
 *   mark      (POST) — đánh dấu một đoạn văn (chapterId, paraNo)
 *   unmark    (POST) — bỏ đánh dấu (chapterId + paraNo, hoặc id)
 *   progress  (POST) — trang đọc báo vị trí đang đọc (navigator.sendBeacon)
 */
@WebServlet("/bookmark")
public class BookmarkServlet extends HttpServlet {

    private BookmarkDAO bookmarkDAO;
    private ChapterBookmarkDAO markDAO;
    private ReadingProgressDAO progressDAO;
    private ChapterDAO chapterDAO;
    private ChapterPageDAO pageDAO;
    private StoryDAO storyDAO;
    private UnlockDAO unlockDAO;

    @Override
    public void init() throws ServletException {
        bookmarkDAO = new BookmarkDAO();
        markDAO = new ChapterBookmarkDAO();
        progressDAO = new ReadingProgressDAO();
        chapterDAO = new ChapterDAO();
        pageDAO = new ChapterPageDAO();
        storyDAO = new StoryDAO();
        unlockDAO = new UnlockDAO();
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

        /*
         * AuthFilter đã chặn khách chưa đăng nhập ở URL này, nên tới đây
         * currentUser chắc chắn khác null.
         *
         * Vẫn kiểm lại một lần cho chắc: nếu sau này ai đó sửa url-pattern của
         * filter mà quên servlet này, đoạn dưới vẫn chặn được thay vì ném
         * NullPointerException.
         *
         * VÀ NÓ ĐÃ THẬT SỰ CỨU MỘT LẦN. Có giai đoạn AuthFilter dùng chung một
         * danh sách trắng cho cả bốn servlet, trong đó có "list" — nên
         * /bookmark?action=list lọt thẳng qua filter, câu trên thành sai. Dữ
         * liệu không lộ đúng nhờ mấy dòng "thừa" này.
         */
        User me = ServletHelper.currentUser(request);
        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        int storyId = parseIntOr(request.getParameter("storyId"), 0);

        // Mọi action GHI chỉ nhận POST — xem ghi chú ở CsrfFilter.
        boolean isWrite = "add".equals(action) || "remove".equals(action)
                || "mark".equals(action) || "unmark".equals(action) || "progress".equals(action);
        if (isWrite && !"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }

        boolean isAjax = ServletHelper.isAjax(request);

        try {
            switch (action) {
                case "add":
                    bookmarkDAO.add(me.getId(), storyId);
                    if (isAjax) {
                        writeJson(response, "{\"success\":true,\"bookmarked\":true,\"message\":\"Đã lưu truyện vào tủ sách!\"}");
                        return;
                    }
                    // Lưu từ trang lịch sử đọc thì ở lại đó — người ta đang
                    // lướt lại truyện cũ, ném sang trang truyện là mất chỗ.
                    if ("history".equals(request.getParameter("from"))) {
                        request.getSession().setAttribute("flash", "Đã lưu truyện.");
                        response.sendRedirect(request.getContextPath() + "/history");
                    } else {
                        backToStory(request, response, storyId);
                    }
                    return;

                case "remove":
                    bookmarkDAO.remove(me.getId(), storyId);
                    if (isAjax) {
                        writeJson(response, "{\"success\":true,\"bookmarked\":false,\"message\":\"Đã bỏ lưu truyện.\"}");
                        return;
                    }
                    // Gỡ từ trang danh sách thì quay về danh sách, gỡ từ trang
                    // truyện thì quay về truyện — dựa vào tham số "from"
                    if ("list".equals(request.getParameter("from"))) {
                        response.sendRedirect(request.getContextPath() + "/bookmark?action=list");
                    } else {
                        backToStory(request, response, storyId);
                    }
                    return;

                case "mark":
                    mark(request, response, me, isAjax);
                    return;

                case "unmark":
                    unmark(request, response, me, isAjax);
                    return;

                case "progress":
                    progress(request, response, me);
                    return;

                default:
                    boolean marksTab = "marks".equals(request.getParameter("tab"));
                    List<ChapterBookmark> marks = markDAO.findByUser(me.getId());
                    request.setAttribute("bookmarks", bookmarkDAO.findByUser(me.getId()));
                    request.setAttribute("marks", marks);
                    request.setAttribute("tab", marksTab ? "marks" : "stories");
                    request.setAttribute("pageTitle", marksTab ? "Đoạn đã đánh dấu" : "Truyện đã lưu");
                    request.setAttribute("activeNav", "bookmark");
                    request.setAttribute("contentPage", "/WEB-INF/views/user/bookmarks.jsp");
                    getServletContext()
                            .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                            .forward(request, response);
            }
        } catch (SQLException e) {
            log("BookmarkServlet: lỗi truy vấn, action=" + action, e);

            if (isAjax || "progress".equals(action)) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                writeJson(response, "{\"success\":false,\"message\":\"Chưa lưu được, thử lại sau nhé.\"}");
                return;
            }

            /*
             * KHONG nem trang 500 khi nguyen nhan la CHUA CO CSDL.
             * O che do xem giao dien moi lenh GHI deu that bai — dung nhu
             * thiet ke — nhung trang 500 khien nguoi dung tuong web hong.
             */
            if (!DBConnection.isReady()) {
                request.getSession().setAttribute("flash",
                        "Chưa nối cơ sở dữ liệu nên chưa lưu được. "
                        + "Chạy scripts\\setup-db.ps1 rồi tạo db.properties.");
                response.sendRedirect(request.getContextPath() + "/bookmark");
                return;
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    // ---- đánh dấu đoạn văn -------------------------------------------------

    private void mark(HttpServletRequest request, HttpServletResponse response,
                      User me, boolean isAjax) throws SQLException, IOException {
        Chapter chapter = chapterDAO.findById(parseIntOr(request.getParameter("chapterId"), 0));
        Story story = chapter == null ? null : storyDAO.findById(chapter.getStoryId());

        /*
         * Phải KIỂM QUYỀN ĐỌC, không chỉ kiểm chương có tồn tại.
         * Đoạn trích (snippet) được cắt từ nội dung chương ở phía server. Bỏ
         * bước này thì ai cũng đánh dấu được từng đoạn của chương VIP chưa mở
         * khoá rồi đọc trọn nội dung qua trang "Đoạn đã đánh dấu".
         */
        if (!canRead(me, story, chapter)) {
            replyMark(request, response, isAjax, false, false, "Bạn chưa đọc được chương này.");
            return;
        }

        int paraNo = ReadingProgress.clampPara(
                parseIntOr(request.getParameter("paraNo"), 0), unitCount(story, chapter));
        // Truyện tranh không có chữ để trích — trang tủ sách hiện ảnh thu nhỏ thay vào.
        String snippet = null;
        if (paraNo > 0 && !story.isComic()) {
            List<String> paras = chapter.getParagraphs();
            snippet = ChapterBookmark.snippetOf(paras.get(paraNo - 1));
        }

        if (markDAO.countByUser(me.getId()) >= ChapterBookmarkDAO.MAX_PER_USER) {
            replyMark(request, response, isAjax, false, false,
                    "Bạn đã có " + ChapterBookmarkDAO.MAX_PER_USER
                    + " đánh dấu. Bỏ bớt đánh dấu cũ rồi thử lại nhé.");
            return;
        }

        markDAO.add(me.getId(), chapter.getId(), paraNo, snippet);
        replyMark(request, response, isAjax, true, true,
                paraNo > 0 ? "Đã đánh dấu " + story.getUnitLabel() + " " + paraNo + "."
                           : "Đã đánh dấu chương.");
    }

    private void unmark(HttpServletRequest request, HttpServletResponse response,
                        User me, boolean isAjax) throws SQLException, IOException {
        int id = parseIntOr(request.getParameter("id"), 0);
        if (id > 0) {
            markDAO.removeById(me.getId(), id);
        } else {
            markDAO.remove(me.getId(),
                    parseIntOr(request.getParameter("chapterId"), 0),
                    parseIntOr(request.getParameter("paraNo"), 0));
        }
        replyMark(request, response, isAjax, true, false, "Đã bỏ đánh dấu.");
    }

    private void replyMark(HttpServletRequest request, HttpServletResponse response,
                           boolean isAjax, boolean success, boolean marked, String message)
            throws IOException {
        if (isAjax) {
            JsonUtil.sendJson(response, HttpServletResponse.SC_OK, "{\"success\":" + success + ",\"marked\":" + marked
                    + ",\"message\":" + JsonUtil.jsonEscape(message) + "}");
            return;
        }
        request.getSession().setAttribute(success ? "flash" : "flashError", message);
        response.sendRedirect(request.getContextPath() + "/bookmark?action=list&tab=marks");
    }

    // ---- vị trí đọc --------------------------------------------------------

    /**
     * Trang đọc báo "đang ở chương X, đoạn Y" — qua navigator.sendBeacon khi
     * người đọc dừng cuộn hoặc rời trang.
     *
     * Trả 204 không nội dung: sendBeacon không đọc phản hồi, gửi gì về cũng
     * phí băng thông. Request hỏng (chương không có, không có quyền) cũng chỉ
     * lặng lẽ bỏ qua — đây là việc nền, không có ai đứng chờ thông báo lỗi.
     */
    private void progress(HttpServletRequest request, HttpServletResponse response, User me)
            throws SQLException {
        Chapter chapter = chapterDAO.findById(parseIntOr(request.getParameter("chapterId"), 0));
        Story story = chapter == null ? null : storyDAO.findById(chapter.getStoryId());

        if (canRead(me, story, chapter)) {
            int paraNo = ReadingProgress.clampPara(
                    parseIntOr(request.getParameter("paraNo"), 0), unitCount(story, chapter));
            progressDAO.save(me.getId(), story.getId(), chapter.getId(), paraNo);
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    /**
     * Số "đơn vị" của chương để ép paraNo: số ĐOẠN với truyện chữ, số TRANG
     * với truyện tranh. Cùng một cột para_no, chỉ khác cách đếm.
     */
    private int unitCount(Story story, Chapter chapter) throws SQLException {
        return story.isComic() ? pageDAO.countByChapter(chapter.getId())
                               : chapter.getParagraphs().size();
    }

    /** Cùng luật với ChapterServlet — một chỗ duy nhất ở ChapterAccess. */
    private boolean canRead(User me, Story story, Chapter chapter) throws SQLException {
        return ChapterAccess.canRead(me, story, chapter, unlockDAO::hasUnlocked);
    }

    // ---- tiện ích ----------------------------------------------------------

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        JsonUtil.sendJson(response, HttpServletResponse.SC_OK, json);
    }

    private void backToStory(HttpServletRequest request, HttpServletResponse response,
                             int storyId) throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + storyId);
    }

}
