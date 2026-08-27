package truyen.controller;

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

import truyen.dao.ChapterDAO;
import truyen.dao.StoryDAO;
import truyen.model.Chapter;
import truyen.model.Story;
import truyen.util.SlugUtil;

/**
 * CASE 09 — Tải truyện về dạng .txt
 *
 * URL: /download?storyId=5
 *
 * SERVLET DUY NHẤT TRONG DỰ ÁN KHÔNG FORWARD SANG JSP.
 *
 * Mọi servlet khác đều kết thúc bằng forward tới layout. Servlet này thì tự
 * ghi thẳng ra response — vì đầu ra không phải HTML mà là một file .txt.
 * Đây đúng là trường hợp mà chương 5 (CASE 01) nói: dùng PrintWriter khi
 * response không phải trang web.
 */
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

        Story story;
        List<Chapter> chapters;
        try {
            story = storyDAO.findById(storyId);
            if (story == null || !"PUBLISHED".equals(story.getStatus())) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            chapters = chapterDAO.findAllWithContent(storyId);
        } catch (SQLException e) {
            log("DownloadServlet: không đọc được truyện id=" + storyId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        /*
         * BA HEADER PHẢI ĐẶT TRƯỚC getWriter() — sau đó là muộn.
         *
         * 1. setContentType: text/plain, KHÔNG phải text/html. Đặt sai thì
         *    trình duyệt cố hiển thị như trang web thay vì tải xuống.
         *
         * 2. setCharacterEncoding UTF-8: thiếu là tiếng Việt trong file thành
         *    dấu hỏi khi mở bằng Notepad.
         */
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        /*
         * 3. Content-Disposition: attachment — đây là header BẮT TRÌNH DUYỆT
         *    TẢI XUỐNG thay vì mở trong tab. Không có nó thì nội dung truyện
         *    hiện thẳng ra màn hình.
         *
         * VÌ SAO CÓ HAI CÁCH GHI TÊN FILE
         *   filename=          — tên đơn giản, chỉ ASCII. Trình duyệt cũ hiểu.
         *   filename*=UTF-8''  — tên có dấu, đã mã hoá. Trình duyệt mới ưu tiên
         *                        cái này và hiện đúng "Kiếm Khí Trường Sinh.txt"
         *   Ghi cả hai thì trình duyệt nào cũng ra tên đọc được.
         */
        String asciiName = SlugUtil.toSlug(story.getTitle());
        if (asciiName.isEmpty()) {
            asciiName = "truyen";
        }
        String utf8Name = URLEncoder.encode(story.getTitle() + ".txt", "UTF-8")
                                    .replace("+", "%20");

        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + asciiName + ".txt\"; "
              + "filename*=UTF-8''" + utf8Name);

        // getWriter() phải gọi SAU khi đặt xong header
        try (PrintWriter out = response.getWriter()) {
            out.println(story.getTitle());
            out.println("Tác giả: " + story.getAuthorName());
            if (story.getDescription() != null && !story.getDescription().isEmpty()) {
                out.println();
                out.println(story.getDescription());
            }
            out.println();
            out.println(repeat('=', 60));
            out.println();

            if (chapters.isEmpty()) {
                out.println("(Truyện chưa có chương nào.)");
            }
            for (Chapter c : chapters) {
                out.println();
                out.println("Chương " + c.getChapterNo() + ": " + c.getTitle());
                out.println(repeat('-', 60));
                out.println();
                out.println(c.getContent());
                out.println();
            }

            out.println(repeat('=', 60));
            out.println("Tải từ web Đọc Truyện — đồ án môn Lập trình Web");
        }
    }

    /**
     * Java 8 không có String.repeat() (đó là Java 11+). Dự án đặt
     * maven.compiler.target = 11 nên dùng được, nhưng viết tay thì chắc chắn
     * chạy trên mọi phiên bản, kể cả khi ai đó hạ target xuống 8.
     */
    private String repeat(char c, int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    private int parseIntOr(String s, int fallback) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException | NullPointerException e) {
            return fallback;
        }
    }
}
