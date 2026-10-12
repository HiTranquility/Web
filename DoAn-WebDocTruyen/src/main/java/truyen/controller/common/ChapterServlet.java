package truyen.controller.common;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import truyen.util.ChapterAccess;
import truyen.util.DBConnection;
import truyen.util.ServletHelper;
import truyen.util.UploadUtil;

import static truyen.util.ServletHelper.parseIntOr;
import static truyen.util.ServletHelper.trimOrEmpty;
import truyen.dao.BookmarkDAO;
import truyen.dao.ChapterBookmarkDAO;
import truyen.dao.ChapterDAO;
import truyen.dao.ChapterPageDAO;
import truyen.dao.CommentDAO;
import truyen.dao.FollowDAO;
import truyen.dao.NotificationDAO;
import truyen.dao.ReadingProgressDAO;
import truyen.dao.StoryDAO;
import truyen.dao.UnlockDAO;
import truyen.dao.UserDAO;
import truyen.dao.WalletDAO;
import truyen.model.Chapter;
import truyen.model.ChapterPage;
import truyen.model.Comment;
import truyen.model.ReadingProgress;
import truyen.model.Story;
import truyen.model.User;

/**
 * CASE 06 — Chương truyện.
 *
 * URL: /chapter?action=read | create | edit | delete
 *
 * Trang đọc dùng layout `reader` — bỏ hết nav và footer để không có gì phân
 * tán khi đọc. Đây là ví dụ rõ nhất cho luật "layout mới chỉ khi KHUNG khác".
 *
 * @MultipartConfig: chương TRUYỆN TRANH gửi ảnh từng trang lên bằng form
 * multipart. Form chương chữ vẫn là form thường — cấu hình này chỉ có tác
 * dụng khi request thật sự là multipart, không ảnh hưởng gì tới nó.
 *   maxFileSize    — một trang, khớp UploadUtil.MAX_PAGE_BYTES (5 MB) + dư
 *   maxRequestSize — một lần lưu: ~30 trang x 5 MB. Nhiều hơn thì tải làm
 *                    nhiều lần (form cho thêm trang vào chương đã có).
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,         // > 1 MB thì ghi tạm ra đĩa, không giữ RAM
        maxFileSize       = 6L * 1024 * 1024,
        maxRequestSize    = 160L * 1024 * 1024)
@WebServlet("/chapter")
public class ChapterServlet extends HttpServlet {

    private ChapterDAO chapterDAO;
    private ChapterPageDAO pageDAO;
    private CommentDAO commentDAO;
    private FollowDAO followDAO;
    private NotificationDAO notificationDAO;
    private StoryDAO storyDAO;
    private BookmarkDAO bookmarkDAO;
    private ChapterBookmarkDAO markDAO;
    private ReadingProgressDAO progressDAO;
    private UnlockDAO unlockDAO;
    private WalletDAO walletDAO;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        chapterDAO = new ChapterDAO();
        pageDAO = new ChapterPageDAO();
        commentDAO = new CommentDAO();
        followDAO = new FollowDAO();
        notificationDAO = new NotificationDAO();
        storyDAO = new StoryDAO();
        bookmarkDAO = new BookmarkDAO();
        markDAO = new ChapterBookmarkDAO();
        progressDAO = new ReadingProgressDAO();
        unlockDAO = new UnlockDAO();
        walletDAO = new WalletDAO();
        userDAO = new UserDAO();
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
        if (!ChapterAccess.canSeeStory(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Hàng rào chương VIP (ISSUE-020) — luật chung ở ChapterAccess
        boolean isLocked = ChapterAccess.isLocked(me, story, chapter, unlockDAO::hasUnlocked);

        if (isLocked) {
            hideLockedContent(request, me, chapter);
        } else {
            openChapter(request, story, chapter);
            /*
             * Tự động ghi lại vị trí đọc (chỉ khi được đọc).
             *
             * ĐỌC vị trí cũ TRƯỚC rồi mới GHI: nếu người đọc quay lại đúng
             * chương đang dở, trang đọc cần biết đoạn cũ để cuộn tới — ghi
             * trước thì không còn gì để hỏi. touch() cũng giữ nguyên số đoạn
             * khi mở lại cùng chương, nên mở trang không xoá mất vị trí.
             *
             * Hỏng thì chỉ mất tính năng nhớ chỗ — KHÔNG được làm hỏng việc đọc.
             */
            try {
                ReadingProgress old = progressDAO.find(me.getId(), story.getId());
                if (old != null && old.getChapterId() == chapter.getId() && old.getParaNo() > 1) {
                    request.setAttribute("resumePara", old.getParaNo());
                }
                progressDAO.touch(me.getId(), story.getId(), chapter.getId());
            } catch (SQLException e) {
                log("Không lưu được vị trí đọc, userId=" + me.getId(), e);
            }
            setMarkedParas(request, me, chapter);
        }

        try {
            request.setAttribute("bookmarked", bookmarkDAO.exists(me.getId(), story.getId()));
        } catch (SQLException e) {
            log("Không đọc được trạng thái lưu truyện, userId=" + me.getId(), e);
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

        // Nạp số chương đọc để tính đúng cấp bậc ưu đãi
        try {
            me.setReadChapters(userDAO.countReadChapters(me.getId()));
        } catch (Exception ignored) {}

        int origPrice = chapter.getCoinPrice();
        int discount = me.getDiscountPercent();
        int finalPrice = origPrice;
        if (discount > 0) {
            finalPrice = Math.max(1, (int) Math.round(origPrice * (100.0 - discount) / 100.0));
        }

        boolean success = walletDAO.unlockChapter(me.getId(), chapter.getId(), story.getAuthorId(), story.getId(), finalPrice);
        if (success) {
            if (discount > 0) {
                request.getSession().setAttribute("flash", "🎉 Mở khoá chương thành công! Bạn được giảm " + discount
                        + "% nhờ cấp " + me.getTierName() + " (chỉ tốn " + finalPrice + " xu thay vì " + origPrice + " xu).");
            } else {
                request.getSession().setAttribute("flash", "🎉 Mở khoá chương thành công! Chúc bạn đọc truyện vui vẻ.");
            }
        } else {
            request.getSession().setAttribute("flashError", "Số dư xu của bạn không đủ để mở khoá chương này (cần " + finalPrice + " xu). Hãy điểm danh nhận xu nhé!");
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
     * VI SAO KHONG GHI VI TRI DOC O DAY NUA
     *   Truoc day raw() ghi vi tri moi lan nap — nhung JavaScript nap chuong
     *   sau TRUOC khi nguoi doc toi do 600px (de khoi phai cho). Ket qua: vua
     *   doc toi cuoi chuong 5 la he thong da ghi "dang doc chuong 6", thoat
     *   ra roi "Doc tiep" la nhay qua mat doan cuoi chuong 5.
     *
     *   Gio trang doc tu bao vi tri THAT (chuong + doan dang hien tren man
     *   hinh) qua /bookmark?action=progress. raw() chi con lo tra noi dung.
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
        if (!ChapterAccess.canSeeStory(me, story)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        // Hàng rào VIP đối với raw() — CÙNG luật với read(), xem ChapterAccess
        if (ChapterAccess.isLocked(me, story, chapter, unlockDAO::hasUnlocked)) {
            hideLockedContent(request, me, chapter);
        } else {
            openChapter(request, story, chapter);
            setMarkedParas(request, me, chapter);
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

        // Truyện tranh soạn bằng form khác (tải ảnh), truyện chữ dùng trình soạn văn bản.
        String formView = story.isComic()
                ? "/WEB-INF/views/user/chapter/form-comic.jsp"
                : "/WEB-INF/views/user/chapter/form.jsp";

        if (!"POST".equals(request.getMethod())) {
            if (story.isComic() && !isCreate) {
                chapter.setPages(pageDAO.findByChapter(chapter.getId()));
                ChapterAccess.grant(request.getSession(), chapter.getId());   // để form hiện ảnh xem trước
            }
            return showForm(request, story, chapter, isCreate, formView, null);
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
        // Chương truyện tranh không có chữ — content để rỗng (cột NOT NULL).
        chapter.setContent(story.isComic() || content == null ? "" : content);
        chapter.setChapterNo(chapterNo);
        chapter.setVip(isVip);
        chapter.setCoinPrice(coinPrice);

        String error = null;
        if (title.isEmpty()) {
            error = "Tiêu đề chương không được để trống.";
        } else if (!story.isComic() && chapter.getContent().trim().isEmpty()) {
            error = "Nội dung chương không được để trống.";
        } else if (title.length() > 200) {
            error = "Tiêu đề chương tối đa 200 ký tự.";
        } else if (chapterNo <= 0) {
            error = "Số thứ tự chương phải là số nguyên dương lớn hơn 0.";
        } else if (chapterNo > 100000) {
            error = "Số thứ tự chương quá lớn.";
        } else if (chapterDAO.chapterNoExists(story.getId(), chapterNo, chapter.getId())) {
            error = "Truyện đã có Chương " + chapterNo + ". Chọn số khác (gợi ý: "
                    + chapterDAO.nextChapterNo(story.getId()) + ").";
        }
        if (error != null) {
            if (story.isComic() && !isCreate) chapter.setPages(pageDAO.findByChapter(chapter.getId()));
            return showForm(request, story, chapter, isCreate, formView, error);
        }

        boolean notifyUpdate = "1".equals(request.getParameter("notifyFollowers"))
                || "true".equalsIgnoreCase(request.getParameter("notifyFollowers"))
                || "on".equalsIgnoreCase(request.getParameter("notifyFollowers"));

        if (story.isComic()) {
            String comicError = saveComicChapter(request, story, chapter, isCreate);
            if (comicError != null) {
                return showForm(request, story, chapter, isCreate, formView, comicError);
            }
            if (isCreate) {
                notifyFollowers(story, chapter, false);
            } else if (notifyUpdate) {
                notifyFollowers(story, chapter, true);
            }
        } else if (isCreate) {
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

    private String showForm(HttpServletRequest request, Story story, Chapter chapter,
                            boolean isCreate, String view, String error) {
        if (error != null) request.setAttribute("message", error);
        request.setAttribute("chapter", chapter);
        request.setAttribute("story", story);
        request.setAttribute("editorBack", "/story?action=detail&id=" + story.getId());
        request.setAttribute("pageTitle", isCreate ? "Thêm chương" : "Sửa chương");
        request.setAttribute("maxPages", ChapterPageDAO.MAX_PAGES_PER_CHAPTER);
        request.setAttribute("maxPageMb", UploadUtil.MAX_PAGE_BYTES / 1024 / 1024);
        return view;
    }

    /**
     * Lưu chương TRUYỆN TRANH: thông tin chương + danh sách trang ảnh.
     *
     * THỨ TỰ CÁC BƯỚC LÀ CỐ Ý
     *   1. Chương mới thì INSERT trước — cần chapterId để đặt thư mục ảnh.
     *   2. Ghi file ảnh mới ra đĩa. Hỏng giữa chừng (ảnh sai định dạng, quá
     *      nặng) thì xoá các file vừa ghi, và với chương MỚI xoá luôn chương
     *      vừa INSERT — không để lại chương rỗng hay file mồ côi.
     *   3. Ghi danh sách trang vào CSDL trong MỘT transaction (replaceAll).
     *   4. Chỉ SAU KHI commit mới xoá file của các trang bị bỏ. Xoá trước mà
     *      transaction hỏng thì CSDL còn trỏ tới ảnh đã mất.
     *
     * @return thông báo lỗi cho người dùng, hoặc null nếu lưu xong.
     */
    private String saveComicChapter(HttpServletRequest request, Story story, Chapter chapter,
                                    boolean isCreate) throws SQLException, IOException {
        List<javax.servlet.http.Part> files = new java.util.ArrayList<>();
        try {
            for (javax.servlet.http.Part p : request.getParts()) {
                if ("pages".equals(p.getName()) && p.getSize() > 0) files.add(p);
            }
        } catch (ServletException | IllegalStateException e) {
            // Vượt maxFileSize / maxRequestSize của @MultipartConfig
            log("Form chương truyện tranh vượt giới hạn tải lên", e);
            return "Ảnh tải lên quá lớn. Mỗi trang tối đa " + (UploadUtil.MAX_PAGE_BYTES / 1024 / 1024)
                    + " MB; nhiều trang thì chia làm vài lần lưu.";
        }

        List<ChapterPage> existing = isCreate
                ? new java.util.ArrayList<>() : pageDAO.findByChapter(chapter.getId());
        java.util.Set<Integer> remove = parseIds(request.getParameter("removePages"));
        int keptOld = 0;
        for (ChapterPage p : existing) if (!remove.contains(p.getId())) keptOld++;

        int total = keptOld + files.size();
        if (total == 0) {
            chapter.setPages(existing);
            return "Chương truyện tranh cần ít nhất một trang ảnh.";
        }
        if (total > ChapterPageDAO.MAX_PAGES_PER_CHAPTER) {
            chapter.setPages(existing);
            return "Một chương tối đa " + ChapterPageDAO.MAX_PAGES_PER_CHAPTER
                    + " trang (đang là " + total + "). Tách bớt sang chương sau nhé.";
        }

        if (isCreate) {
            chapterDAO.insert(chapter);
        }

        List<ChapterPage> added = new java.util.ArrayList<>();
        try {
            for (javax.servlet.http.Part part : files) {
                UploadUtil.SavedPage saved = UploadUtil.savePage(
                        part, story.getId(), chapter.getId(), getServletContext());
                ChapterPage p = new ChapterPage();
                p.setChapterId(chapter.getId());
                p.setImagePath(saved.path);
                p.setWidth(saved.width);
                p.setHeight(saved.height);
                added.add(p);
            }
        } catch (UploadUtil.UploadException e) {
            for (ChapterPage p : added) UploadUtil.deletePage(p.getImagePath(), getServletContext());
            if (isCreate) {
                chapterDAO.delete(chapter.getId());
                chapter.setId(0);
            }
            chapter.setPages(existing);
            return e.getMessage();
        }

        List<String> order = java.util.Arrays.asList(
                trimOrEmpty(request.getParameter("pageOrder")).split(","));
        List<ChapterPage> finalPages = ChapterPage.arrange(existing, added, order, remove);

        List<String> removedPaths;
        try {
            if (!isCreate) chapterDAO.update(chapter);
            removedPaths = pageDAO.replaceAll(chapter.getId(), finalPages);
        } catch (SQLException e) {
            for (ChapterPage p : added) UploadUtil.deletePage(p.getImagePath(), getServletContext());
            if (isCreate) chapterDAO.delete(chapter.getId());
            throw e;
        }
        for (String path : removedPaths) UploadUtil.deletePage(path, getServletContext());
        return null;
    }

    /** "12,15,x,15" -> {12, 15}. Bỏ qua mọi thứ không phải số nguyên dương. */
    private static java.util.Set<Integer> parseIds(String csv) {
        java.util.Set<Integer> out = new java.util.HashSet<>();
        if (csv == null) return out;
        for (String s : csv.split(",")) {
            int id = parseIntOr(s.trim(), 0);
            if (id > 0) out.add(id);
        }
        return out;
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
        // Truyện tranh: nhớ đường dẫn ảnh TRƯỚC khi xoá (dòng chapter_pages đi
        // theo khoá ngoại CASCADE), xoá xong chương mới dọn file trên đĩa.
        List<String> imagePaths = story.isComic()
                ? pageDAO.findPaths(chapter.getId()) : java.util.Collections.emptyList();
        chapterDAO.delete(chapter.getId());
        for (String path : imagePaths) UploadUtil.deletePage(path, getServletContext());

        response.sendRedirect(request.getContextPath()
                + "/story?action=detail&id=" + story.getId());
        return null;
    }

    // ---- tiện ích ----------------------------------------------------------

    /**
     * Các đoạn người đọc đã đánh dấu trong chương này, dạng "3,17,42".
     * _block.jsp in ra data-marked để JavaScript tô sẵn — dùng chung cho cả
     * read() lẫn raw(), nên chương nối thêm khi đọc liên tục cũng được tô.
     */
    private void setMarkedParas(HttpServletRequest request, User me, Chapter chapter) {
        if (me == null) return;
        try {
            StringBuilder sb = new StringBuilder();
            for (Integer p : markDAO.findParaNos(me.getId(), chapter.getId())) {
                if (sb.length() > 0) sb.append(',');
                sb.append(p);
            }
            request.setAttribute("markedParas", sb.toString());
        } catch (SQLException e) {
            log("Không đọc được đoạn đánh dấu, userId=" + me.getId(), e);
        }
    }

    /**
     * Chương VIP đang khoá: rút SẠCH nội dung trước khi tới JSP.
     * Ẩn bằng CSS là không đủ — Ctrl+U vẫn thấy. Với truyện tranh cũng
     * không nạp danh sách trang: không có đường dẫn ảnh nào lọt ra HTML.
     */
    private void hideLockedContent(HttpServletRequest request, User me, Chapter chapter)
            throws SQLException {
        int origPrice = chapter.getCoinPrice();
        int discount = 0;
        int finalPrice = origPrice;
        if (me != null) {
            try {
                me.setReadChapters(userDAO.countReadChapters(me.getId()));
            } catch (Exception ignored) {}
            discount = me.getDiscountPercent();
            if (discount > 0) {
                finalPrice = Math.max(1, (int) Math.round(origPrice * (100.0 - discount) / 100.0));
            }
        }
        request.setAttribute("isLocked", true);
        request.setAttribute("origPrice", origPrice);
        request.setAttribute("unlockPrice", finalPrice);
        request.setAttribute("discountPercent", discount);
        request.setAttribute("readerTierName", me != null ? me.getTierName() : "");
        request.setAttribute("readerTierIcon", me != null ? me.getTierIcon() : "");
        request.setAttribute("isReaderVip", me != null && me.isVip());
        request.setAttribute("walletBalance", me != null ? walletDAO.getBalance(me.getId()) : 0);
        chapter.setContent("");
        chapter.setPages(null);
    }

    /**
     * Chương được đọc: nạp trang ảnh (truyện tranh) và CẤP QUYỀN theo phiên để
     * UploadedFileServlet phát ảnh mà khỏi hỏi lại CSDL cho từng trang.
     */
    private void openChapter(HttpServletRequest request, Story story, Chapter chapter)
            throws SQLException {
        if (story.isComic()) {
            chapter.setPages(pageDAO.findByChapter(chapter.getId()));
            ChapterAccess.grant(request.getSession(), chapter.getId());
        }
    }

    private boolean canEdit(User user, Story story) {
        return ChapterAccess.canEdit(user, story);
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
