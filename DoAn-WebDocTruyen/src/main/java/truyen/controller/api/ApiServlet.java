package truyen.controller.api;

import java.io.IOException;
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

/**
 * RESTful JSON API cho các ứng dụng bên ngoài và di động (ISSUE-009).
 * Hỗ trợ:
 * - GET /api/stories
 * - GET /api/story/{id}
 * - GET /api/chapter/{id}
 */
@WebServlet(name = "ApiServlet", urlPatterns = {"/api/*"})
public class ApiServlet extends HttpServlet {

    private final StoryDAO storyDAO = new StoryDAO();
    private final ChapterDAO chapterDAO = new ChapterDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");
        resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.equals("/stories")) {
            handleStories(req, resp);
        } else if (pathInfo.startsWith("/story/")) {
            handleStory(pathInfo.substring(7), req, resp);
        } else if (pathInfo.startsWith("/chapter/")) {
            handleChapter(pathInfo.substring(9), req, resp);
        } else {
            sendError(resp, 404, "Endpoint không tồn tại");
        }
    }

    private void handleStories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        String tag = req.getParameter("tag");
        String sort = req.getParameter("sort");
        int page = parseInt(req.getParameter("page"), 1);
        int limit = parseInt(req.getParameter("limit"), 20);
        if (limit > 50) limit = 50;
        int offset = (page - 1) * limit;

        try {
            List<Story> stories = storyDAO.findPage(tag, q, sort != null ? sort : "latest", "PUBLISHED", offset, limit);
            int total = storyDAO.countPage(tag, q, "PUBLISHED");

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"page\":").append(page).append(",")
                .append("\"limit\":").append(limit).append(",")
                .append("\"total\":").append(total).append(",")
                .append("\"stories\":[");

            for (int i = 0; i < stories.size(); i++) {
                Story s = stories.get(i);
                if (i > 0) json.append(",");
                appendStoryJson(json, s);
            }
            json.append("]}");

            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            sendError(resp, 500, "Loi truy van CSDL");
        }
    }

    private void handleStory(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = parseInt(idStr, 0);
        if (id <= 0) {
            sendError(resp, 400, "ID truyen khong hop le");
            return;
        }

        try {
            Story s = storyDAO.findById(id);
            if (s == null) {
                sendError(resp, 404, "Khong tim thay truyen");
                return;
            }

            List<Chapter> chapters = chapterDAO.findByStory(id);

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"story\":");
            appendStoryJson(json, s);
            json.append(",\"chapters\":[");
            for (int i = 0; i < chapters.size(); i++) {
                Chapter c = chapters.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"id\":").append(c.getId()).append(",")
                    .append("\"chapterNo\":").append(c.getChapterNo()).append(",")
                    .append("\"title\":").append(jsonEscape(c.getTitle())).append(",")
                    .append("\"readMinutes\":").append(c.getReadMinutes())
                    .append("}");
            }
            json.append("]}");

            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            sendError(resp, 500, "Loi truy van CSDL");
        }
    }

    private void handleChapter(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int id = parseInt(idStr, 0);
        if (id <= 0) {
            sendError(resp, 400, "ID chuong khong hop le");
            return;
        }

        try {
            Chapter c = chapterDAO.findById(id);
            if (c == null) {
                sendError(resp, 404, "Khong tim thay chuong");
                return;
            }

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"id\":").append(c.getId()).append(",")
                .append("\"storyId\":").append(c.getStoryId()).append(",")
                .append("\"chapterNo\":").append(c.getChapterNo()).append(",")
                .append("\"title\":").append(jsonEscape(c.getTitle())).append(",")
                .append("\"content\":").append(jsonEscape(c.getContent())).append(",")
                .append("\"readMinutes\":").append(c.getReadMinutes())
                .append("}");

            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            sendError(resp, 500, "Loi truy van CSDL");
        }
    }

    private void appendStoryJson(StringBuilder json, Story s) {
        json.append("{")
            .append("\"id\":").append(s.getId()).append(",")
            .append("\"title\":").append(jsonEscape(s.getTitle())).append(",")
            .append("\"authorName\":").append(jsonEscape(s.getAuthorName())).append(",")
            .append("\"authorId\":").append(s.getAuthorId()).append(",")
            .append("\"coverUrl\":").append(jsonEscape(s.getCoverUrl() != null ? s.getCoverUrl() : "")).append(",")
            .append("\"description\":").append(jsonEscape(s.getDescription() != null ? s.getDescription() : "")).append(",")
            .append("\"chapterCount\":").append(s.getChapterCount()).append(",")
            .append("\"viewCount\":").append(s.getViewCount()).append(",")
            .append("\"completed\":").append(s.isCompleted())
            .append("}");
    }

    private void sendError(HttpServletResponse resp, int status, String msg) throws IOException {
        resp.setStatus(status);
        resp.getWriter().write("{\"error\":" + jsonEscape(msg) + ",\"status\":" + status + "}");
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static String jsonEscape(String s) {
        if (s == null) return "\"\"";
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\b': sb.append("\\b");  break;
                case '\f': sb.append("\\f");  break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:
                    if (c < ' ') {
                        String hex = "000" + Integer.toHexString(c);
                        sb.append("\\u").append(hex.substring(hex.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append("\"");
        return sb.toString();
    }
}
