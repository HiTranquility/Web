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
import truyen.dao.ChapterPageDAO;
import truyen.dao.StoryDAO;
import truyen.dao.TagDAO;
import truyen.dao.UnlockDAO;
import truyen.dao.UserDAO;
import truyen.model.Chapter;
import truyen.model.ChapterPage;
import truyen.model.Story;
import truyen.model.Tag;
import truyen.model.User;
import truyen.util.ChapterAccess;
import truyen.util.JsonUtil;
import truyen.util.ServletHelper;

/**
 * RESTful JSON API cho các ứng dụng bên ngoài và di động (ISSUE-009).
 * Hỗ trợ:
 * - GET /api/stories
 * - GET /api/story/{id}
 * - GET /api/chapter/{id}
 * - GET /api/tags
 * - GET /api/rank?by=views|week|month|authors
 */
@WebServlet(name = "ApiServlet", urlPatterns = {"/api/*"})
public class ApiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private StoryDAO storyDAO;
    private ChapterDAO chapterDAO;
    private ChapterPageDAO chapterPageDAO;
    private TagDAO tagDAO;
    private UserDAO userDAO;
    private UnlockDAO unlockDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        chapterDAO = new ChapterDAO();
        chapterPageDAO = new ChapterPageDAO();
        tagDAO = new TagDAO();
        userDAO = new UserDAO();
        unlockDAO = new UnlockDAO();
    }

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
        } else if (pathInfo.equals("/tags") || pathInfo.equals("/tags/")) {
            handleTags(req, resp);
        } else if (pathInfo.equals("/rank") || pathInfo.equals("/ranks") || pathInfo.startsWith("/rank/") || pathInfo.startsWith("/ranks/")) {
            handleRanks(req, resp);
        } else {
            sendError(resp, 404, "Endpoint không tồn tại");
        }
    }

    private void handleStories(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String q = req.getParameter("q");
        String tag = req.getParameter("tag");
        String sort = req.getParameter("sort");
        int page = parseInt(req.getParameter("page"), 1);
        if (page < 1) page = 1;
        int limit = parseInt(req.getParameter("limit"), 20);
        if (limit < 1) limit = 20;
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

            User me = ServletHelper.currentUser(req);
            boolean isAdmin = me != null && me.isAdmin();
            boolean isAuthor = me != null && me.getId() == s.getAuthorId();
            if ("DELETED".equals(s.getStatus()) && !isAdmin) {
                sendError(resp, 404, "Khong tim thay truyen");
                return;
            }
            if ("DRAFT".equals(s.getStatus()) && !isAuthor && !isAdmin) {
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
                    .append("\"readMinutes\":").append(c.getReadMinutes()).append(",")
                    .append("\"vip\":").append(c.isVip()).append(",")
                    .append("\"coinPrice\":").append(c.getCoinPrice())
                    .append("}");
            }
            json.append("]}");

            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            sendError(resp, 500, "Loi truy van CSDL");
        }
    }

    private void handleChapter(String idStr, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (ServletHelper.currentUser(req) == null) {
            sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Vui long dang nhap de doc truyen");
            return;
        }
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

            Story s = storyDAO.findById(c.getStoryId());
            User me = ServletHelper.currentUser(req);
            boolean isAdmin = me != null && me.isAdmin();
            boolean isAuthor = me != null && s != null && me.getId() == s.getAuthorId();
            if (s == null || ("DELETED".equals(s.getStatus()) && !isAdmin) || ("DRAFT".equals(s.getStatus()) && !isAuthor && !isAdmin)) {
                sendError(resp, 404, "Khong tim thay chuong");
                return;
            }

            boolean isLocked = false;
            try {
                isLocked = ChapterAccess.isLocked(me, s, c, unlockDAO::hasUnlocked);
            } catch (SQLException ex) {
                isLocked = c.isVip();
            }

            List<ChapterPage> pages = isLocked ? java.util.Collections.emptyList() : chapterPageDAO.findByChapter(c.getId());
            String displayContent = isLocked
                    ? "[Chương VIP — mở khoá " + c.getCoinPrice() + " xu trên web để đọc nội dung chương này.]"
                    : c.getContent();

            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"id\":").append(c.getId()).append(",")
                .append("\"storyId\":").append(c.getStoryId()).append(",")
                .append("\"chapterNo\":").append(c.getChapterNo()).append(",")
                .append("\"title\":").append(jsonEscape(c.getTitle())).append(",")
                .append("\"content\":").append(jsonEscape(displayContent)).append(",")
                .append("\"readMinutes\":").append(c.getReadMinutes()).append(",")
                .append("\"vip\":").append(c.isVip()).append(",")
                .append("\"isLocked\":").append(isLocked).append(",")
                .append("\"coinPrice\":").append(c.getCoinPrice()).append(",")
                .append("\"pages\":[");
            for (int i = 0; i < pages.size(); i++) {
                ChapterPage p = pages.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"id\":").append(p.getId()).append(",")
                    .append("\"pageNo\":").append(p.getPageNo()).append(",")
                    .append("\"url\":").append(jsonEscape(p.getUrl())).append(",")
                    .append("\"width\":").append(p.getWidth()).append(",")
                    .append("\"height\":").append(p.getHeight())
                    .append("}");
            }
            json.append("]}");

            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            sendError(resp, 500, "Loi truy van CSDL");
        }
    }

    private void handleTags(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<Tag> tags = tagDAO.findAllWithCount();
            StringBuilder json = new StringBuilder();
            json.append("{")
                .append("\"total\":").append(tags.size()).append(",")
                .append("\"tags\":[");
            for (int i = 0; i < tags.size(); i++) {
                Tag t = tags.get(i);
                if (i > 0) json.append(",");
                json.append("{")
                    .append("\"id\":").append(t.getId()).append(",")
                    .append("\"name\":").append(jsonEscape(t.getName())).append(",")
                    .append("\"slug\":").append(jsonEscape(t.getSlug())).append(",")
                    .append("\"storyCount\":").append(t.getStoryCount())
                    .append("}");
            }
            json.append("]}");
            resp.getWriter().write(json.toString());
        } catch (SQLException e) {
            sendError(resp, 500, "Loi truy van CSDL");
        }
    }

    private void handleRanks(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String by = req.getParameter("by");
        if (by == null || by.trim().isEmpty()) {
            by = "views";
        }
        int limit = parseInt(req.getParameter("limit"), 20);
        if (limit > 50) limit = 50;

        try {
            StringBuilder json = new StringBuilder();
            json.append("{\"by\":").append(jsonEscape(by)).append(",");
            if ("authors".equalsIgnoreCase(by)) {
                List<User> authors = userDAO.findTopAuthors(limit);
                json.append("\"authors\":[");
                for (int i = 0; i < authors.size(); i++) {
                    User u = authors.get(i);
                    if (i > 0) json.append(",");
                    json.append("{")
                        .append("\"id\":").append(u.getId()).append(",")
                        .append("\"username\":").append(jsonEscape(u.getUsername())).append(",")
                        .append("\"name\":").append(jsonEscape(u.getName())).append(",")
                        .append("\"avatarUrl\":").append(jsonEscape(u.getAvatarUrl() != null ? u.getAvatarUrl() : "")).append(",")
                        .append("\"totalViews\":").append(u.getTotalViews())
                        .append("}");
                }
                json.append("]}");
            } else {
                List<Story> stories;
                if ("week".equalsIgnoreCase(by)) {
                    stories = storyDAO.findTopByPeriod(7, limit);
                } else if ("month".equalsIgnoreCase(by)) {
                    stories = storyDAO.findTopByPeriod(30, limit);
                } else {
                    stories = storyDAO.findTop(by, limit);
                }
                json.append("\"stories\":[");
                for (int i = 0; i < stories.size(); i++) {
                    Story s = stories.get(i);
                    if (i > 0) json.append(",");
                    appendStoryJson(json, s);
                }
                json.append("]}");
            }
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
            .append("\"type\":").append(jsonEscape(s.getStoryType())).append(",")
            .append("\"completed\":").append(s.isCompleted())
            .append("}");
    }

    private void sendError(HttpServletResponse resp, int status, String msg) throws IOException {
        JsonUtil.sendError(resp, status, msg);
    }

    private int parseInt(String s, int def) {
        if (s == null) return def;
        try {
            return Integer.parseInt(s.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }

    /**
     * Escape chuỗi an toàn theo chuẩn JSON (ủy quyền cho JsonUtil dùng chung).
     */
    public static String jsonEscape(String s) {
        return JsonUtil.jsonEscape(s);
    }
}
