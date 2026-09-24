package truyen.controller.story;

import java.io.IOException;
import java.io.PrintWriter;
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
import truyen.model.User;
import truyen.util.ChapterToTxt;
import truyen.util.DriveClient;
import truyen.util.ServletHelper;
import static truyen.util.ServletHelper.parseIntOr;

/**
 * ISSUE-001 Phase 5: Tác giả sao lưu truyện lên Google Drive cá nhân.
 *
 * Đường dẫn: /drive?action=...
 *
 * Quy tắc bảo mật:
 * 1. Phạm vi quyền hạn tối thiểu: Chỉ dùng scope drive.file (không dùng drive toàn bộ).
 * 2. Kiểm soát sở hữu nghiêm ngặt: Chỉ tác giả của truyện mới được sao lưu (403 nếu không phải).
 * 3. Chặn khách chưa đăng nhập: Tự động chuyển hướng về trang đăng nhập.
 */
@WebServlet("/drive")
public class DriveBackupServlet extends HttpServlet {

    private StoryDAO storyDAO;
    private ChapterDAO chapterDAO;

    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
        chapterDAO = new ChapterDAO();
    }

    // Hỗ trợ inject mock DAO trong Unit Tests
    public void setStoryDAO(StoryDAO storyDAO) {
        this.storyDAO = storyDAO;
    }

    public void setChapterDAO(ChapterDAO chapterDAO) {
        this.chapterDAO = chapterDAO;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        handleRequest(request, response);
    }

    private void handleRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Kiểm tra trạng thái đăng nhập
        User currentUser = ServletHelper.currentUser(request);
        if (currentUser == null) {
            // Ca 5: Chưa đăng nhập mà gọi /drive -> đá về trang đăng nhập
            String redirectUrl = request.getRequestURI();
            if (request.getQueryString() != null) {
                redirectUrl += "?" + request.getQueryString();
            }
            request.getSession(true).setAttribute("redirectAfterLogin", redirectUrl);
            response.sendRedirect(request.getContextPath() + "/auth?action=login");
            return;
        }

        // 2. Kiểm tra quyền sở hữu truyện (Bắt buộc)
        int storyId = parseIntOr(request.getParameter("storyId"), 0);
        Story story = null;
        try {
            story = storyDAO.findById(storyId);
        } catch (SQLException e) {
            log("Lỗi truy vấn thông tin truyện id=" + storyId, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        if (story == null || story.getAuthorId() != currentUser.getId()) {
            // Ca 1: Không phải tác giả của truyện -> 403 Cấm truy cập
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        String action = request.getParameter("action");
        if (action == null) {
            action = "chapters";
        }

        switch (action) {
            case "chapters":
                handleGetChapters(request, response, story);
                break;
            case "init":
                handleInitFolder(request, response, story);
                break;
            case "backup":
                handleBackup(request, response, story);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                break;
        }
    }

    /**
     * Trả về danh sách các chương của truyện để giao diện hiển thị tiến độ 1/N ... N/N.
     */
    private void handleGetChapters(HttpServletRequest request, HttpServletResponse response, Story story)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        try {
            List<Chapter> chapters = chapterDAO.findByStory(story.getId());
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\":true,");
            json.append("\"storyId\":").append(story.getId()).append(",");
            json.append("\"title\":\"").append(escapeJson(story.getTitle())).append("\",");
            json.append("\"total\":").append(chapters.size()).append(",");
            if (chapters.isEmpty()) {
                json.append("\"message\":\"Truyện chưa có chương nào để sao lưu.\",");
            }
            json.append("\"chapters\":[");
            for (int i = 0; i < chapters.size(); i++) {
                Chapter c = chapters.get(i);
                if (i > 0) json.append(",");
                json.append("{");
                json.append("\"id\":").append(c.getId()).append(",");
                json.append("\"chapterNo\":").append(c.getChapterNo()).append(",");
                json.append("\"title\":\"").append(escapeJson(c.getTitle())).append("\",");
                json.append("\"fileName\":\"").append(escapeJson(ChapterToTxt.formatChapterFileName(c.getChapterNo(), c.getTitle()))).append("\"");
                json.append("}");
            }
            json.append("]}");

            try (PrintWriter out = response.getWriter()) {
                out.print(json.toString());
            }
        } catch (SQLException e) {
            log("Lỗi nạp danh sách chương cho truyện id=" + story.getId(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":false,\"message\":\"Lỗi truy vấn cơ sở dữ liệu.\"}");
            }
        }
    }

    /**
     * Khởi tạo hoặc tìm kiếm thư mục DocTruyen/<tên truyện> trên Google Drive.
     */
    private void handleInitFolder(HttpServletRequest request, HttpServletResponse response, Story story)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String accessToken = request.getParameter("accessToken");
        if (accessToken == null || accessToken.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":false,\"message\":\"Thiếu access_token của Google Drive.\"}");
            }
            return;
        }

        try {
            String folderId = DriveClient.getOrCreateStoryFolder(accessToken.trim(), story.getTitle());
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":true,\"folderId\":\"" + escapeJson(folderId) + "\"}");
            }
        } catch (Exception e) {
            log("Lỗi khởi tạo thư mục Drive cho truyện id=" + story.getId(), e);
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":false,\"message\":\"Không thể tạo thư mục trên Google Drive: "
                        + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    /**
     * Sao lưu từng chương (giao diện gọi theo tiến độ) hoặc sao lưu toàn bộ trong một lượt.
     */
    private void handleBackup(HttpServletRequest request, HttpServletResponse response, Story story)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String accessToken = request.getParameter("accessToken");
        if (accessToken == null || accessToken.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":false,\"message\":\"Thiếu access_token của Google Drive.\"}");
            }
            return;
        }

        int chapterId = parseIntOr(request.getParameter("chapterId"), 0);
        String folderId = request.getParameter("folderId");

        try {
            if (folderId == null || folderId.trim().isEmpty()) {
                folderId = DriveClient.getOrCreateStoryFolder(accessToken.trim(), story.getTitle());
            }

            if (chapterId > 0) {
                // Chế độ 1: Tải lên từng chương đơn lẻ (đáp ứng giao diện hiển thị 1/N, 2/N mượt mà)
                Chapter chapter = chapterDAO.findById(chapterId);
                if (chapter == null || chapter.getStoryId() != story.getId()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    try (PrintWriter out = response.getWriter()) {
                        out.print("{\"success\":false,\"message\":\"Không tìm thấy chương hoặc chương không thuộc truyện này.\"}");
                    }
                    return;
                }

                String fileName = ChapterToTxt.formatChapterFileName(chapter.getChapterNo(), chapter.getTitle());
                String fileContent = ChapterToTxt.formatChapter(chapter);

                String fileId = DriveClient.uploadOrUpdateFile(accessToken.trim(), folderId, fileName, fileContent);

                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":true,\"chapterId\":" + chapterId
                            + ",\"chapterNo\":" + chapter.getChapterNo()
                            + ",\"fileName\":\"" + escapeJson(fileName) + "\""
                            + ",\"fileId\":\"" + escapeJson(fileId) + "\""
                            + ",\"folderId\":\"" + escapeJson(folderId) + "\"}");
                }
            } else {
                // Chế độ 2: Tải lên toàn bộ chương cùng một lượt
                List<Chapter> chapters = chapterDAO.findAllWithContent(story.getId());
                if (chapters.isEmpty()) {
                    try (PrintWriter out = response.getWriter()) {
                        out.print("{\"success\":false,\"message\":\"Truyện chưa có chương nào để sao lưu.\"}");
                    }
                    return;
                }

                int successCount = 0;
                for (Chapter c : chapters) {
                    String fileName = ChapterToTxt.formatChapterFileName(c.getChapterNo(), c.getTitle());
                    String fileContent = ChapterToTxt.formatChapter(c);
                    DriveClient.uploadOrUpdateFile(accessToken.trim(), folderId, fileName, fileContent);
                    successCount++;
                }

                try (PrintWriter out = response.getWriter()) {
                    out.print("{\"success\":true,\"total\":" + chapters.size()
                            + ",\"uploaded\":" + successCount
                            + ",\"folderId\":\"" + escapeJson(folderId) + "\"}");
                }
            }
        } catch (Exception e) {
            log("Lỗi sao lưu truyện lên Google Drive id=" + story.getId(), e);
            response.setStatus(HttpServletResponse.SC_BAD_GATEWAY);
            try (PrintWriter out = response.getWriter()) {
                out.print("{\"success\":false,\"message\":\"Lỗi Google Drive: " + escapeJson(e.getMessage()) + "\"}");
            }
        }
    }

    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\b", "\\b")
                  .replace("\f", "\\f")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
