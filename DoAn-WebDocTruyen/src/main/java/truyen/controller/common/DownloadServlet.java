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

/** CASE 09 — Tải truyện về dạng .txt */
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

            // Chua co CSDL thi khong the sinh file. Dua ve trang truyen kem
            // mot cau giai thich, thay vi mot trang 500 khong noi len gi.
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

        /* BA HEADER PHẢI ĐẶT TRƯỚC getWriter() — sau đó là muộn. */
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        /*
         * 3. Content-Disposition: attachment — đây là header BẮT TRÌNH DUYỆT
         * TẢI XUỐNG thay vì mở trong tab. Không có nó thì nội dung truyện
         * hiện thẳng ra màn hình.
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
            out.print(truyen.util.ChapterToTxt.formatStory(story, chapters));
        }
    }

}
