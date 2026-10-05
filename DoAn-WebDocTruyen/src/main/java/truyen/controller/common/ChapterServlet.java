package truyen.controller.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.util.DBConnection;
import truyen.util.ServletHelper;

import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.trimOrEmpty;
import truyen.dao.BookmarkDAO;
import truyen.dao.ChapterDAO;
import truyen.dao.CommentDAO;
import truyen.dao.FollowDAO;
import truyen.dao.NotificationDAO;
import truyen.dao.StoryDAO;
import truyen.dao.UnlockDAO;
import truyen.dao.WalletDAO;
import truyen.model.Chapter;
import truyen.model.Comment;
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
    private CommentDAO commentDAO;
    private FollowDAO followDAO;
    private NotificationDAO notificationDAO;
    private StoryDAO storyDAO;
    private BookmarkDAO bookmarkDAO;
    private UnlockDAO unlockDAO;
    private WalletDAO walletDAO;

    @Override
    public void init() throws ServletException {
        chapterDAO = new ChapterDAO();
        commentDAO = new CommentDAO();
        followDAO = new FollowDAO();
        notificationDAO = new NotificationDAO();
        storyDAO = new StoryDAO();
        bookmarkDAO = new BookmarkDAO();
        unlockDAO = new UnlockDAO();
        walletDAO = new WalletDAO();
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
                case "unlock":
                    url = unlock(request, response);
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
        if (id <= 0) {
            int storyId = parseIntOr(request.getParameter("storyId"), 0);
            if (storyId > 0) {
                response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId);
            } else {
                response.sendRedirect(request.getContextPath() + "/story");
            }
            return null;
        }
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

        User me = ServletHelper.currentUser(request);

        // Nghiệp vụ: Phải đăng nhập mới được đọc truyện
        if (me == null) {
            String target = request.getRequestURI();
            if (request.getQueryString() != null) {
                target += "?" + request.getQueryString();
            }
            HttpSession session = request.getSession(true);
            session.setAttribute("redirectAfterLogin", target);
            session.setAttribute("flash", "Vui lòng đăng nhập tài khoản để đọc truyện nhé!");
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }

        // Chương của truyện NHÁP chỉ tác giả và admin được đọc.
        if ("DRAFT".equals(story.getStatus()) && !canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Hàng rào chương VIP (ISSUE-020)
        boolean isLocked = false;
        if (chapter.isVip()) {
            if (me == null) {
                isLocked = true;
            } else if (canEdit(me, story) || "ADMIN".equals(me.getRole())) {
                isLocked = false; // Tác giả và Admin đọc miễn phí
            } else {
                isLocked = !unlockDAO.hasUnlocked(me.getId(), chapter.getId());
            }
        }

        if (isLocked) {
            request.setAttribute("isLocked", true);
            request.setAttribute("unlockPrice", chapter.getCoinPrice());
            request.setAttribute("walletBalance", me != null ? walletDAO.getBalance(me.getId()) : 0);
            chapter.setContent(""); // Triệt tiêu nội dung khỏi HTML ngăn ngừa lộ qua Ctrl+U
        } else {
            /* Tự động ghi lại vị trí đọc cho người đã đăng nhập (chỉ khi được đọc). */
            if (me != null) {
                try {
                    bookmarkDAO.updateProgress(me.getId(), story.getId(), chapter.getId());
                } catch (SQLException e) {
                    log("Không lưu được vị trí đọc, userId=" + me.getId(), e);
                }
            }
        }

        /* Nạp danh sách bình luận của chương */
        List<Comment> chapterComments = commentDAO.findByChapter(chapter.getId());
        commentDAO.populateLikes(chapterComments, me != null ? me.getId() : 0);
        request.setAttribute("chapterComments", chapterComments);
        request.setAttribute("chapterCommentCount", commentDAO.countByChapter(chapter.getId()));

        request.setAttribute("chapter", chapter);
        request.setAttribute("story", story);
        request.setAttribute("prev",
                chapterDAO.findNeighbour(story.getId(), chapter.getChapterNo(), -1));
        request.setAttribute("next",
                chapterDAO.findNeighbour(story.getId(), chapter.getChapterNo(), +1));
        request.setAttribute("pageTitle",
                "Chương " + chapter.getChapterNo() + " — " + story.getTitle());
        return "/WEB-INF/views/common/chapter/read.jsp";
    }

    private String unlock(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        User me = ServletHelper.currentUser(request);
        int chapterId = parseIntOr(request.getParameter("id"), 0);
        if (chapterId <= 0) {
            response.sendRedirect(request.getContextPath() + "/story");
            return null;
        }
        Chapter chapter = chapterDAO.findById(chapterId);
        if (chapter == null) {
            response.sendRedirect(request.getContextPath() + "/story");
            return null;
        }

        if (me == null) {
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return null;
        }

        Story story = storyDAO.findById(chapter.getStoryId());
        if (story == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Tác giả hoặc admin hoặc chương không VIP thì không cần mở khoá
        if (!chapter.isVip() || canEdit(me, story) || "ADMIN".equals(me.getRole())) {
            response.sendRedirect(request.getContextPath() + "/chapter?action=read&id=" + chapterId);
            return null;
        }

        // Đã mở khoá trước đó rồi
        if (unlockDAO.hasUnlocked(me.getId(), chapter.getId())) {
            response.sendRedirect(request.getContextPath() + "/chapter?action=read&id=" + chapterId);
            return null;
        }

        boolean success = walletDAO.unlockChapter(me.getId(), chapter.getId(), story.getAuthorId(), story.getId(), chapter.getCoinPrice());
        if (success) {
            request.getSession().setAttribute("flash", "🎉 Mở khoá chương thành công! Chúc bạn đọc truyện vui vẻ.");
        } else {
            request.getSession().setAttribute("flashError", "Số dư xu của bạn không đủ để mở khoá chương này (cần " + chapter.getCoinPrice() + " xu). Hãy điểm danh nhận xu nhé!");
        }
        response.sendRedirect(request.getContextPath() + "/chapter?action=read&id=" + chapterId);
        return null;
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
        User me = ServletHelper.currentUser(request);
        if (me == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập để đọc tiếp");
            return null;
        }
        if ("DRAFT".equals(story.getStatus()) && !canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Hàng rào VIP đối với raw()
        boolean isLocked = false;
        if (chapter.isVip()) {
            if (me == null) {
                isLocked = true;
            } else if (canEdit(me, story) || "ADMIN".equals(me.getRole())) {
                isLocked = false;
            } else {
                isLocked = !unlockDAO.hasUnlocked(me.getId(), chapter.getId());
            }
        }

        if (isLocked) {
            request.setAttribute("isLocked", true);
            request.setAttribute("unlockPrice", chapter.getCoinPrice());
            request.setAttribute("walletBalance", me != null ? walletDAO.getBalance(me.getId()) : 0);
            chapter.setContent("");
        } else {
            if (me != null) {
                try {
                    bookmarkDAO.updateProgress(me.getId(), story.getId(), chapter.getId());
                } catch (SQLException e) {
                    log("Khong luu duoc vi tri doc, userId=" + me.getId(), e);
                }
            }
        }

        request.setAttribute("chapter", chapter);
        request.setAttribute("story", story);
        request.setAttribute("next",
                chapterDAO.findNeighbour(story.getId(), chapter.getChapterNo(), +1));

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/common/chapter/raw.jsp")
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
        User me = ServletHelper.currentUser(request);
        if (me == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Vui lòng đăng nhập");
            return null;
        }
        if ("DRAFT".equals(story.getStatus()) && !canEdit(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        request.setAttribute("story", story);
        request.setAttribute("chapters", chapterDAO.findByStory(storyId));

        if (me != null) {
            List<Integer> unlockedIds = unlockDAO.findUnlockedChapterIds(me.getId(), storyId);
            request.setAttribute("unlockedChapterIds", unlockedIds);
        }

        // id chuong DANG doc, de to dam dung dong trong danh sach
        request.setAttribute("currentId", parseIntOr(request.getParameter("current"), 0));

        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/common/chapter/toc.jsp")
                .forward(request, response);
        return null;
    }


    // ---- thêm / sửa chương -------------------------------------------------

    private String createOrEdit(HttpServletRequest request, HttpServletResponse response,
                                boolean isCreate) throws SQLException, IOException {

        User me = ServletHelper.currentUser(request);
        Chapter chapter;
        Story story;

        if (isCreate) {
            story = storyDAO.findById(parseIntOr(request.getParameter("storyId"), 0));
            if (story == null) {
                response.sendRedirect(request.getContextPath() + "/story?action=mine");
                return null;
            }
            chapter = new Chapter();
            chapter.setStoryId(story.getId());
            chapter.setChapterNo(chapterDAO.nextChapterNo(story.getId()));
        } else {
            chapter = chapterDAO.findById(parseIntOr(request.getParameter("id"), 0));
            if (chapter == null) {
                response.sendRedirect(request.getContextPath() + "/story?action=mine");
                return null;
            }
            story = storyDAO.findById(chapter.getStoryId());
            if (story == null) {
                response.sendRedirect(request.getContextPath() + "/story?action=mine");
                return null;
            }
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
            return "/WEB-INF/views/user/chapter/form.jsp";
        }

        String title = trimOrEmpty(request.getParameter("title"));
        String content = request.getParameter("content");
        int chapterNo = parseIntOr(request.getParameter("chapterNo"), chapter.getChapterNo());

        boolean isVip = "1".equals(request.getParameter("isVip")) || "true".equalsIgnoreCase(request.getParameter("isVip")) || "on".equalsIgnoreCase(request.getParameter("isVip"));
        int coinPrice = parseIntOr(request.getParameter("coinPrice"), 0);
        if (!isVip || coinPrice < 0) {
            coinPrice = 0;
            isVip = false;
        } else if (coinPrice > 10000) {
            coinPrice = 10000;
        }

        chapter.setTitle(title);
        chapter.setContent(content == null ? "" : content);
        chapter.setChapterNo(chapterNo);
        chapter.setVip(isVip);
        chapter.setCoinPrice(coinPrice);

        String error = null;
        if (title.isEmpty() || chapter.getContent().trim().isEmpty()) {
            error = "Tiêu đề và nội dung chương không được để trống.";
        } else if (title.length() > 200) {
            error = "Tiêu đề chương tối đa 200 ký tự.";
        } else if (chapterNo <= 0) {
            error = "Số thứ tự chương phải là số nguyên dương lớn hơn 0.";
        }

        if (error != null) {
            request.setAttribute("message", error);
            request.setAttribute("chapter", chapter);
            request.setAttribute("story", story);
            request.setAttribute("editorBack", "/story?action=detail&id=" + story.getId());
            request.setAttribute("pageTitle", isCreate ? "Thêm chương" : "Sửa chương");
            return "/WEB-INF/views/user/chapter/form.jsp";
        }

        boolean notifyUpdate = "1".equals(request.getParameter("notifyFollowers"))
                || "true".equalsIgnoreCase(request.getParameter("notifyFollowers"))
                || "on".equalsIgnoreCase(request.getParameter("notifyFollowers"));

        if (isCreate) {
            chapterDAO.insert(chapter);
            notifyFollowers(story, chapter, false);
        } else {
            chapterDAO.update(chapter);
            if (notifyUpdate) {
                notifyFollowers(story, chapter, true);
            }
        }

        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + story.getId());
        return null;
    }

    private String delete(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {

        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return null;
        }

        Chapter chapter = chapterDAO.findById(parseIntOr(request.getParameter("id"), 0));
        if (chapter == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        Story story = storyDAO.findById(chapter.getStoryId());
        if (!canEdit(ServletHelper.currentUser(request), story)) {
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

    /** Báo cho những người đang theo dõi tác giả và những người đã lưu truyện rằng có chương mới hoặc có cập nhật nội dung. */
    private void notifyFollowers(Story story, Chapter chapter, boolean isUpdate) {
        try {
            java.util.Set<Integer> recipients = new java.util.LinkedHashSet<>();
            recipients.addAll(followDAO.findFollowerIds(story.getAuthorId()));
            recipients.addAll(bookmarkDAO.findBookmarkedUserIds(story.getId()));
            recipients.remove(story.getAuthorId());

            if (recipients.isEmpty()) {
                return;   // không ai theo dõi hoặc lưu thì thôi
            }
            String type = isUpdate ? "UPDATE_CHAPTER" : "NEW_CHAPTER";
            String action = isUpdate ? " vừa cập nhật nội dung chương " : " vừa đăng chương ";
            String message = story.getAuthorName() + action
                           + chapter.getChapterNo() + " của \"" + story.getTitle() + "\"";
            notificationDAO.notifyFollowers(new java.util.ArrayList<>(recipients), story.getId(),
                                            chapter.getId(), type, message);
        } catch (SQLException e) {
            log("Không gửi được thông báo chương cho truyện " + story.getId(), e);
        }
    }
}
