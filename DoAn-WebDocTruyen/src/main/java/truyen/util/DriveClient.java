package truyen.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Client tương tác với Google Drive REST API v3.
 *
 * Chỉ dùng quyền hạn tối thiểu: scope `https://www.googleapis.com/auth/drive.file`.
 *
 * Chức năng:
 * 1. Đảm bảo cấu trúc thư mục trên Drive của người dùng:
 *    DocTruyen/
 *      └── <Tên truyện>/
 *            ├── 001 - Mở đầu.txt
 *            └── 002 - Chương 2.txt
 * 2. Tải lên file mới hoặc GHI ĐÈ nếu file cùng tên đã tồn tại (không đẻ file trùng tên dạng (1), (2)).
 * 3. Hoàn toàn dùng thư viện chuẩn Java (HttpURLConnection), không phụ thuộc SDK nặng.
 */
public class DriveClient {

    private static final String DRIVE_API_URL = "https://www.googleapis.com/drive/v3/files";
    private static final String DRIVE_UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files";
    private static final int TIMEOUT_MS = 15000;

    /**
     * Interface hỗ trợ mock mạng cho Unit Test không cần kết nối internet thật.
     */
    public interface HttpTransport {
        HttpURLConnection openConnection(URL url) throws IOException;
    }

    private static HttpTransport transport = url -> (HttpURLConnection) url.openConnection();

    public static void setHttpTransport(HttpTransport customTransport) {
        transport = customTransport != null ? customTransport : (url -> (HttpURLConnection) url.openConnection());
    }

    public static void resetHttpTransport() {
        transport = url -> (HttpURLConnection) url.openConnection();
    }

    /**
     * Lấy hoặc tạo thư mục dành riêng cho truyện: DocTruyen/<tên truyện>.
     *
     * @param accessToken OAuth2 access token có scope drive.file
     * @param storyTitle  Tên truyện
     * @return folderId của thư mục truyện trên Drive
     */
    public static String getOrCreateStoryFolder(String accessToken, String storyTitle) throws IOException {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Google Drive accessToken không được để trống.");
        }
        String safeTitle = ChapterToTxt.sanitizeFileName(storyTitle);
        if (safeTitle.isEmpty()) {
            safeTitle = "Truyen";
        }

        // 1. Tìm hoặc tạo thư mục gốc "DocTruyen"
        String rootFolderId = findFolder(accessToken, "DocTruyen", null);
        if (rootFolderId == null) {
            rootFolderId = createFolder(accessToken, "DocTruyen", null);
        }

        // 2. Tìm hoặc tạo thư mục con mang tên truyện trong "DocTruyen"
        String storyFolderId = findFolder(accessToken, safeTitle, rootFolderId);
        if (storyFolderId == null) {
            storyFolderId = createFolder(accessToken, safeTitle, rootFolderId);
        }

        return storyFolderId;
    }

    /**
     * Tải lên một chương truyện hoặc GHI ĐÈ nếu chương đó đã từng được sao lưu.
     *
     * @param accessToken OAuth2 access token
     * @param folderId    ID thư mục truyện trên Drive
     * @param fileName    Tên file (ví dụ: "001 - Mở đầu.txt")
     * @param content     Nội dung văn bản định dạng UTF-8
     * @return fileId trên Drive sau khi lưu
     */
    public static String uploadOrUpdateFile(String accessToken, String folderId, String fileName, String content)
            throws IOException {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Google Drive accessToken không được để trống.");
        }
        if (folderId == null || folderId.trim().isEmpty()) {
            throw new IllegalArgumentException("folderId không được để trống.");
        }

        // Tìm xem file đã tồn tại trong folder này chưa
        String existingFileId = findFile(accessToken, fileName, folderId);
        if (existingFileId != null && !existingFileId.isEmpty()) {
            // Đã có file cũ -> Ghi đè nội dung (tránh tạo (1), (2))
            return updateFileContent(accessToken, existingFileId, content);
        } else {
            // Chưa có -> Tạo file mới qua multipart upload
            return createNewFile(accessToken, folderId, fileName, content);
        }
    }

    /**
     * Tìm file theo tên trong một thư mục nhất định.
     */
    public static String findFile(String accessToken, String fileName, String parentFolderId) throws IOException {
        return searchFileOrFolder(accessToken, fileName, parentFolderId, false);
    }

    /**
     * Tìm thư mục theo tên.
     */
    public static String findFolder(String accessToken, String folderName, String parentFolderId) throws IOException {
        return searchFileOrFolder(accessToken, folderName, parentFolderId, true);
    }

    private static String searchFileOrFolder(String accessToken, String name, String parentFolderId, boolean isFolder)
            throws IOException {
        StringBuilder q = new StringBuilder();
        // Escaping single quotes trong truy vấn Google Drive: ' -> \'
        String escapedName = name.replace("'", "\\'");
        q.append("name = '").append(escapedName).append("' and trashed = false");

        if (isFolder) {
            q.append(" and mimeType = 'application/vnd.google-apps.folder'");
        } else {
            q.append(" and mimeType != 'application/vnd.google-apps.folder'");
        }

        if (parentFolderId != null && !parentFolderId.trim().isEmpty()) {
            q.append(" and '").append(parentFolderId).append("' in parents");
        }

        String urlStr = DRIVE_API_URL + "?q=" + URLEncoder.encode(q.toString(), "UTF-8")
                + "&fields=files(id,name)&spaces=drive";

        URL url = java.net.URI.create(urlStr).toURL();
        HttpURLConnection conn = transport.openConnection(url);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            String resp = readStream(conn.getInputStream());
            return extractFirstId(resp);
        } else {
            String err = readStream(conn.getErrorStream());
            throw new IOException("Lỗi tìm kiếm Google Drive (HTTP " + code + "): " + err);
        }
    }

    /**
     * Tạo một thư mục mới trên Google Drive.
     */
    public static String createFolder(String accessToken, String folderName, String parentFolderId) throws IOException {
        URL url = java.net.URI.create(DRIVE_API_URL).toURL();
        HttpURLConnection conn = transport.openConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"name\":\"").append(escapeJson(folderName)).append("\",");
        json.append("\"mimeType\":\"application/vnd.google-apps.folder\"");
        if (parentFolderId != null && !parentFolderId.trim().isEmpty()) {
            json.append(",\"parents\":[\"").append(escapeJson(parentFolderId)).append("\"]");
        }
        json.append("}");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.toString().getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            String resp = readStream(conn.getInputStream());
            return extractFirstId(resp);
        } else {
            String err = readStream(conn.getErrorStream());
            throw new IOException("Lỗi tạo thư mục Google Drive (HTTP " + code + "): " + err);
        }
    }

    /**
     * Tạo một file mới kèm nội dung text/plain sử dụng multipart upload.
     */
    public static String createNewFile(String accessToken, String folderId, String fileName, String content)
            throws IOException {
        String boundary = "===DocTruyenDriveBoundary" + System.currentTimeMillis() + "===";
        URL url = java.net.URI.create(DRIVE_UPLOAD_URL + "?uploadType=multipart").toURL();
        HttpURLConnection conn = transport.openConnection(url);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "multipart/related; boundary=" + boundary);
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        byte[] metadataPart = ("--" + boundary + "\r\n"
                + "Content-Type: application/json; charset=UTF-8\r\n\r\n"
                + "{\"name\":\"" + escapeJson(fileName) + "\",\"parents\":[\"" + escapeJson(folderId) + "\"]}\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        byte[] mediaPartHeader = ("--" + boundary + "\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n\r\n"
        ).getBytes(StandardCharsets.UTF_8);

        byte[] mediaContent = content != null ? content.getBytes(StandardCharsets.UTF_8) : new byte[0];

        byte[] boundaryEnd = ("\r\n--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(metadataPart);
            os.write(mediaPartHeader);
            os.write(mediaContent);
            os.write(boundaryEnd);
        }

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            String resp = readStream(conn.getInputStream());
            return extractFirstId(resp);
        } else {
            String err = readStream(conn.getErrorStream());
            throw new IOException("Lỗi tải file lên Google Drive (HTTP " + code + "): " + err);
        }
    }

    /**
     * Ghi đè nội dung file cũ trên Google Drive (sử dụng uploadType=media với X-HTTP-Method-Override: PATCH).
     */
    public static String updateFileContent(String accessToken, String fileId, String content) throws IOException {
        URL url = java.net.URI.create(DRIVE_UPLOAD_URL + "/" + URLEncoder.encode(fileId, "UTF-8") + "?uploadType=media").toURL();
        HttpURLConnection conn = transport.openConnection(url);
        // Sử dụng POST kèm header override để tương thích mọi phiên bản Java / firewall
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-HTTP-Method-Override", "PATCH");
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "text/plain; charset=UTF-8");
        conn.setDoOutput(true);
        conn.setConnectTimeout(TIMEOUT_MS);
        conn.setReadTimeout(TIMEOUT_MS);

        byte[] mediaContent = content != null ? content.getBytes(StandardCharsets.UTF_8) : new byte[0];
        try (OutputStream os = conn.getOutputStream()) {
            os.write(mediaContent);
        }

        int code = conn.getResponseCode();
        if (code >= 200 && code < 300) {
            String resp = readStream(conn.getInputStream());
            String updatedId = extractFirstId(resp);
            return updatedId != null && !updatedId.isEmpty() ? updatedId : fileId;
        } else {
            String err = readStream(conn.getErrorStream());
            throw new IOException("Lỗi ghi đè file Google Drive (HTTP " + code + "): " + err);
        }
    }

    /**
     * Trích xuất trường "id" đầu tiên trong chuỗi JSON trả về từ Google Drive API.
     */
    public static String extractFirstId(String json) {
        if (json == null) return null;
        // Khớp pattern "id":\s*"([^"]+)"
        Pattern p = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
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

    private static String readStream(InputStream in) throws IOException {
        if (in == null) return "";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n;
        while ((n = in.read(buffer)) != -1) {
            baos.write(buffer, 0, n);
        }
        return baos.toString(StandardCharsets.UTF_8.name());
    }
}
