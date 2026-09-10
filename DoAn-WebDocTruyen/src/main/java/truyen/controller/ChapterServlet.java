package truyen.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.util.DBConnection;
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

        /* Mỗi action tự chọn layout của mình — đây là chỗ khác StoryServlet. */
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
                case "raw":
                    // Tra ve TRAN, khong boc layout nao. raw() tu forward roi
                    // tra null, nen khoi if (url == null) ben duoi se thoat.
                    url = raw(request, response);
                    break;
                case "toc":
                    // Cung kieu tran nhu raw — muc luc cho bang tha xuong o
                    // trang doc, nap bang fetch() luc nguoi dung bam mo.
                    url = toc(request, response);
                    break;
                default:
                    url = read(request, response);
                    layout = "/WEB-INF/views/layout/reader.jsp";   // khung đọc
                    break;
            }
        } catch (SQLException e) {
            log("ChapterServlet: lỗi truy vấn, action=" + action, e);

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
                response.sendRedirect(request.getContextPath() + "/story?action=mine");
                return;
            }
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

        /* Tự động ghi lại vị trí đọc cho người đã đăng nhập. */
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
    /**
     * Tra ve CHI noi dung mot chuong, khong co khung trang.
     *
     * JavaScript o trang doc goi duong dan nay bang fetch() de nap chuong ke
     * tiep roi noi vao cuoi trang dang doc (doc lien tuc).
     *
     * VI SAO TU FORWARD ROI TRA VE null
     *   handle() o cuoi luon boc ket qua vao mot layout. Rieng action nay
     *   KHONG duoc boc — no phai nha ra dung mot the <article>. Nen no tu
     *   forward toi raw.jsp roi tra null, dung quy uoc chung cua servlet nay:
     *   null = "da xu ly xong, dung forward nua".
     *
     * VI SAO VAN GHI VI TRI DOC O DAY
     *   Day la cai bay de sot nhat cua doc lien tuc. read() ghi vi tri moi
     *   lan TAI TRANG — nhung doc lien tuc chi tai trang MOT lan, roi noi
     *   them 20 chuong ma khong tai lai lan nao. Khong ghi o day thi tu
     *   truyen mai mai bao "dang doc chuong 1".
     *
     *   Ghi ngay trong chinh request nap chuong: khong ton them mot vong goi
     *   nao, va khong the quen.
     */
    private String raw(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {

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

        // Chuong cua truyen NHAP chi tac gia va admin duoc doc.
        // Kiem lai o day chu khong tin rang JS chi goi nhung id hop le —
        // duong dan nay go thang vao thanh dia chi cung goi duoc.
        User me = currentUser(request);
        if ("DRAFT".equals(story.getStatus()) && !canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        if (me != null) {
            try {
                bookmarkDAO.updateProgress(me.getId(), story.getId(), chapter.getId());
            } catch (SQLException e) {
                log("Khong luu duoc vi tri doc, userId=" + me.getId(), e);
            }
        }

        request.setAttribute("chapter", chapter);
        request.setAttribute("story", story);
        request.setAttribute("next",
                chapterDAO.findNeighbour(story.getId(), chapter.getChapterNo(), +1));

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/chapter/raw.jsp")
                .forward(request, response);
        return null;
    }

    /**
     * MUC LUC TRAN — danh sach chuong cua mot truyen, khong khung trang.
     *
     * Trang doc goi bang fetch() khi nguoi dung mo bang "Muc luc".
     *
     * VI SAO NAP MUON, KHONG IN SAN VAO TRANG
     *   Truyen 500 chuong la 500 the <a>. In san vao MOI trang doc nghia la
     *   moi lan lat chuong deu tai lai tung ay, trong khi phan lon nguoi doc
     *   khong mo muc luc lan nao. Nap khi mo thi ai can moi tra gia.
     *
     * VI SAO KHONG TRA VE JSON
     *   Tra JSON thi phia trinh duyet phai tu dung the HTML bang JavaScript —
     *   them mot cho sinh HTML, va la cho DE QUEN escape nhat. Tra thang HTML
     *   do JSP dung san thi <c:out> lo phan escape, giong het moi trang khac.
     *
     * Tham so la storyId chu khong phai chapter id: muc luc thuoc ve TRUYEN.
     */
    private String toc(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException, ServletException {

        int storyId = parseIntOr(request.getParameter("storyId"), 0);
        Story story = storyDAO.findById(storyId);
        if (story == null || "DELETED".equals(story.getStatus())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Truyen nhap: chi tac gia va admin. Kiem lai y het raw() — duong dan
        // nay go thang vao thanh dia chi cung goi duoc.
        User me = currentUser(request);
        if ("DRAFT".equals(story.getStatus()) && !canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        request.setAttribute("story", story);
        request.setAttribute("chapters", chapterDAO.findByStory(storyId));

        // id chuong DANG doc, de to dam dung dong trong danh sach
        request.setAttribute("currentId", parseIntOr(request.getParameter("current"), 0));

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/chapter/toc.jsp")
                .forward(request, response);
        return null;
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


    /** Báo cho những người đang theo dõi tác giả rằng có chương mới. */
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
