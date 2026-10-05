package truyen.controller.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import truyen.util.DBConnection;

import static truyen.util.ServletHelper.parseIntOr;
import truyen.dao.ChapterDAO;
import truyen.dao.StoryDAO;
import truyen.model.Chapter;
import truyen.model.Story;
import truyen.util.SlugUtil;

import truyen.model.User;
import truyen.util.EpubWriter;

/** CASE 09 — Tải truyện về dạng .txt hoặc .epub (ISSUE-021) */
@WebServlet("/download")
public class DownloadServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private ChapterDAO chapterDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        chapterDAO = new ChapterDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        int storyId = parseIntOr(request.getParameter("storyId"), 0);
        String format = request.getParameter("format");

        Story story;
        List<Chapter> chapters;
        try {
            story = storyDAO.findById(storyId);
            if (story == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Nghiệp vụ: Chưa đăng nhập thì chưa cho tải truyện
            User currentUser = (User) request.getSession().getAttribute("currentUser");
            if (currentUser == null) {
                String target = request.getRequestURI();
                if (request.getQueryString() != null) {
                    target += "?" + request.getQueryString();
                }
                request.getSession().setAttribute("redirectAfterLogin", target);
                request.getSession().setAttribute("flash", "Vui lòng đăng nhập để tải truyện về máy nhé!");
                response.sendRedirect(request.getContextPath() + "/auth?action=login");
                return;
            }

            // Kiểm quyền: truyện nháp hoặc đã gỡ chỉ tác giả hoặc admin mới được tải
            boolean isAuthor = (currentUser.getId() == story.getAuthorId());
            boolean isAdmin = currentUser.isAdmin();
            if (!"PUBLISHED".equals(story.getStatus()) && !isAuthor && !isAdmin) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            chapters = chapterDAO.findAllWithContent(storyId);
        } catch (SQLException e) {
            log("DownloadServlet: không đọc được truyện id=" + storyId, e);

            // Chưa có CSDL thì không thể sinh file. Đưa về trang truyện kèm
            // một câu giải thích, thay vì một trang 500 không nói lên gì.
            if (!DBConnection.isReady()) {
                request.getSession().setAttribute("flash",
                        "Chưa nối cơ sở dữ liệu nên chưa tải truyện được.");
                response.sendRedirect(request.getContextPath()
                        + "/story?action=detail&id=" + storyId);
                return;
            }
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String asciiName = SlugUtil.toSlug(story.getTitle());
        if (asciiName.isEmpty()) {
            asciiName = "truyen";
        }

        // ====================================================================
        // 1. XUẤT ĐỊNH DẠNG EPUB (Đọc trên Kindle, Apple Books - ISSUE-021)
        // ====================================================================
        if ("epub".equalsIgnoreCase(format)) {
            response.setContentType("application/epub+zip");
            String utf8Name = URLEncoder.encode(story.getTitle() + ".epub", "UTF-8").replace("+", "%20");
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"" + asciiName + ".epub\"; "
                  + "filename*=UTF-8''" + utf8Name);

            try (java.io.OutputStream out = response.getOutputStream()) {
                EpubWriter.write(out, story, chapters);
            }
            return;
        }

        // ====================================================================
        // 2. XUẤT ĐỊNH DẠNG TEXT (.txt thuần)
        // ====================================================================
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String utf8Name = URLEncoder.encode(story.getTitle() + ".txt", "UTF-8").replace("+", "%20");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiName + ".txt\"; "
              + "filename*=UTF-8''" + utf8Name);

        try (PrintWriter out = response.getWriter()) {
            out.print(truyen.util.ChapterToTxt.formatStory(story, chapters));
        }
    }
}
