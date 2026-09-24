package truyen;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import truyen.controller.story.DriveBackupServlet;
import truyen.dao.ChapterDAO;
import truyen.dao.StoryDAO;
import truyen.model.Chapter;
import truyen.model.Story;
import truyen.model.User;
import truyen.util.DriveClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DriveBackupServlet — Sao lưu Google Drive và kiểm soát quyền tác giả (Phase 5)")
class DriveBackupServletTest {

    private DriveBackupServlet servlet;
    private Map<String, Object> sessionAttributes;
    private StringWriter responseOutput;
    private int responseStatus;
    private String redirectedUrl;

    // Mock DAOs
    private Story mockStory;
    private List<Chapter> mockChapters;

    @BeforeEach
    void setUp() throws Exception {
        servlet = new DriveBackupServlet();
        servlet.init();

        sessionAttributes = new HashMap<>();
        responseOutput = new StringWriter();
        responseStatus = 200;
        redirectedUrl = null;

        mockStory = new Story();
        mockStory.setId(10);
        mockStory.setTitle("Dấu Chân Mùa Thu");
        mockStory.setAuthorId(1); // Tác giả là mocmien (id=1)

        mockChapters = new ArrayList<>();

        // Cài đặt Mock StoryDAO và ChapterDAO
        servlet.setStoryDAO(new StoryDAO() {
            @Override
            public Story findById(int id) throws SQLException {
                if (id == mockStory.getId()) {
                    return mockStory;
                }
                return null;
            }
        });

        servlet.setChapterDAO(new ChapterDAO() {
            @Override
            public List<Chapter> findByStory(int storyId) throws SQLException {
                return mockChapters;
            }

            @Override
            public List<Chapter> findAllWithContent(int storyId) throws SQLException {
                return mockChapters;
            }

            @Override
            public Chapter findById(int id) throws SQLException {
                for (Chapter c : mockChapters) {
                    if (c.getId() == id) return c;
                }
                return null;
            }
        });
    }

    private HttpServletRequest createMockRequest(String method, Map<String, String> params) {
        HttpSession mockSession = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getAttribute".equals(name)) {
                        return sessionAttributes.get((String) args[0]);
                    }
                    if ("setAttribute".equals(name)) {
                        sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    }
                    if ("removeAttribute".equals(name)) {
                        sessionAttributes.remove((String) args[0]);
                        return null;
                    }
                    return null;
                });

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("getMethod".equals(name)) return method;
                    if ("getParameter".equals(name)) return params.get((String) args[0]);
                    if ("getSession".equals(name)) return mockSession;
                    if ("getContextPath".equals(name)) return "/app";
                    if ("getRequestURI".equals(name)) return "/app/drive";
                    if ("getQueryString".equals(name)) return "action=backup&storyId=10";
                    if ("getServletPath".equals(name)) return "/drive";
                    return null;
                });
    }

    private HttpServletResponse createMockResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, m, args) -> {
                    String name = m.getName();
                    if ("sendRedirect".equals(name)) {
                        redirectedUrl = (String) args[0];
                        return null;
                    }
                    if ("sendError".equals(name)) {
                        responseStatus = (int) args[0];
                        return null;
                    }
                    if ("setStatus".equals(name)) {
                        responseStatus = (int) args[0];
                        return null;
                    }
                    if ("getStatus".equals(name)) return responseStatus;
                    if ("getWriter".equals(name)) return new PrintWriter(responseOutput);
                    if ("setContentType".equals(name) || "setCharacterEncoding".equals(name)) return null;
                    return null;
                });
    }

    @Test
    @DisplayName("Ca 1: Người dùng khác (haiduong) gọi backup truyện của mocmien -> 403 Forbidden")
    void testForbiddenWhenNotAuthor() throws Exception {
        // Đăng nhập tài khoản id=2 (haiduong), không phải tác giả (id=1)
        User haiduong = new User();
        haiduong.setId(2);
        haiduong.setUsername("haiduong");
        sessionAttributes.put("currentUser", haiduong);

        Map<String, String> params = new HashMap<>();
        params.put("action", "backup");
        params.put("storyId", "10");

        HttpServletRequest req = createMockRequest("POST", params);
        HttpServletResponse resp = createMockResponse();

        servlet.service(req, resp);

        assertEquals(403, responseStatus, "Phải trả về 403 Forbidden khi người gọi không phải tác giả");
    }

    @Test
    @DisplayName("Ca 5: Khách chưa đăng nhập gọi /drive -> Chuyển hướng đá về trang đăng nhập")
    void testUnauthenticatedRedirect() throws Exception {
        // Không có currentUser trong session
        Map<String, String> params = new HashMap<>();
        params.put("action", "backup");
        params.put("storyId", "10");

        HttpServletRequest req = createMockRequest("GET", params);
        HttpServletResponse resp = createMockResponse();

        servlet.service(req, resp);

        assertEquals("/app/auth?action=login", redirectedUrl);
    }

    @Test
    @DisplayName("Ca 3: Tác giả mở truyện 0 chương -> Thông báo 'Truyện chưa có chương nào để sao lưu'")
    void testZeroChaptersNotice() throws Exception {
        // Đăng nhập tài khoản tác giả (id=1)
        User mocmien = new User();
        mocmien.setId(1);
        mocmien.setUsername("mocmien");
        sessionAttributes.put("currentUser", mocmien);

        Map<String, String> params = new HashMap<>();
        params.put("action", "chapters");
        params.put("storyId", "10");

        HttpServletRequest req = createMockRequest("GET", params);
        HttpServletResponse resp = createMockResponse();

        servlet.service(req, resp);

        String out = responseOutput.toString();
        assertTrue(out.contains("\"success\":true"));
        assertTrue(out.contains("\"total\":0"));
        assertTrue(out.contains("Truyện chưa có chương nào để sao lưu."));
    }

    @Test
    @DisplayName("Ca 4: Tác giả lấy danh sách chương -> Trả về JSON chứa tên file 3 chữ số")
    void testAuthorGetChaptersList() throws Exception {
        User mocmien = new User();
        mocmien.setId(1);
        mocmien.setUsername("mocmien");
        sessionAttributes.put("currentUser", mocmien);

        Chapter c1 = new Chapter();
        c1.setId(101);
        c1.setStoryId(10);
        c1.setChapterNo(1);
        c1.setTitle("Khởi đầu");
        mockChapters.add(c1);

        Chapter c2 = new Chapter();
        c2.setId(102);
        c2.setStoryId(10);
        c2.setChapterNo(29);
        c2.setTitle("Đoàn tụ");
        mockChapters.add(c2);

        Map<String, String> params = new HashMap<>();
        params.put("action", "chapters");
        params.put("storyId", "10");

        HttpServletRequest req = createMockRequest("GET", params);
        HttpServletResponse resp = createMockResponse();

        servlet.service(req, resp);

        String out = responseOutput.toString();
        assertTrue(out.contains("\"success\":true"));
        assertTrue(out.contains("\"total\":2"));
        assertTrue(out.contains("001 - Khởi đầu.txt"));
        assertTrue(out.contains("029 - Đoàn tụ.txt"));
    }

    @Test
    @DisplayName("Ca 6: DriveClient.extractFirstId trích xuất chính xác ID từ JSON của Google Drive")
    void testDriveClientExtractFirstId() {
        String folderJson = "{\"kind\": \"drive#fileList\", \"files\": [{\"id\": \"1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs\", \"name\": \"DocTruyen\"}]}";
        String id = DriveClient.extractFirstId(folderJson);
        assertEquals("1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs", id);

        String singleJson = "{\"id\": \"uploaded_file_id_999\", \"name\": \"001 - Mo dau.txt\"}";
        assertEquals("uploaded_file_id_999", DriveClient.extractFirstId(singleJson));

        assertNull(DriveClient.extractFirstId(null));
        assertNull(DriveClient.extractFirstId("{}"));
    }
}
