package truyen.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Xác thực chữ ký và tính hợp lệ của idToken nhận từ Google / Firebase Authentication.
 *
 * QUY TẮC BẢO MẬT:
 *   1. Tuyệt đối KHÔNG tin email hay uid gửi tự do từ client.
 *   2. Kiểm tra định dạng JWT (3 phần phân tách bằng dấu chấm).
 *   3. Kiểm tra các claims cơ bản: iss (nhà phát hành), aud (client id dự án), exp (thời gian hết hạn), sub.
 *   4. Xác thực chữ ký số bằng API tokeninfo chính thức của Google (https://oauth2.googleapis.com/tokeninfo).
 *   5. Tuyệt đối KHÔNG ném Exception ra ngoài — bất kỳ lỗi xác thực nào đều trả về null an toàn.
 */
public class GoogleTokenVerifier {

    private static final String GOOGLE_TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token=";
    private static final String FIREBASE_LOOKUP_URL = "https://identitytoolkit.googleapis.com/v1/accounts:lookup?key=";
    private static final int TIMEOUT_MS = 6000;

    // Cờ phục vụ kiểm thử đơn vị độc lập không cần mạng Internet
    private boolean skipRemoteCallForTest = false;

    public static class GoogleUser {
        private final String sub;
        private final String email;
        private final String name;
        private final String picture;

        public GoogleUser(String sub, String email, String name, String picture) {
            this.sub = sub;
            this.email = email;
            this.name = name;
            this.picture = picture;
        }

        public String getSub() {
            return sub;
        }

        public String getEmail() {
            return email;
        }

        public String getName() {
            return name;
        }

        public String getPicture() {
            return picture;
        }
    }

    public GoogleTokenVerifier() {
    }

    public GoogleTokenVerifier(boolean skipRemoteCallForTest) {
        this.skipRemoteCallForTest = skipRemoteCallForTest;
    }

    /**
     * Xác minh tính hợp lệ của idToken do Google / Firebase cấp.
     *
     * @param idToken Chuỗi idToken nhận được từ client popup
     * @return Đối tượng GoogleUser chứa thông tin đã được kiểm chứng, hoặc null nếu không hợp lệ
     */
    public GoogleUser verify(String idToken) {
        if (idToken == null || idToken.trim().isEmpty()) {
            return null;
        }

        try {
            String cleanToken = idToken.trim();
            String[] parts = cleanToken.split("\\.");
            if (parts.length != 3) {
                return null;
            }

            // 1. Giải mã Payload JWT
            byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);

            // 2. Trích xuất và kiểm tra các claims bắt buộc
            String sub = extractJsonString(payloadJson, "sub");
            String email = extractJsonString(payloadJson, "email");
            String iss = extractJsonString(payloadJson, "iss");
            String aud = extractJsonString(payloadJson, "aud");
            Long exp = extractJsonLong(payloadJson, "exp");
            Boolean emailVerified = extractJsonBoolean(payloadJson, "email_verified");

            if (sub == null || sub.isEmpty() || email == null || email.isEmpty()) {
                return null;
            }

            // Kiểm tra thời hạn (Expiration Time)
            if (exp != null) {
                long nowSeconds = System.currentTimeMillis() / 1000L;
                if (exp <= nowSeconds) {
                    return null; // Token đã hết hạn
                }
            }

            // Kiểm tra Issuer (Google Accounts hoặc Firebase SecureToken)
            if (iss != null) {
                boolean validIss = "accounts.google.com".equals(iss)
                        || "https://accounts.google.com".equals(iss)
                        || iss.startsWith("https://securetoken.google.com/");
                if (!validIss) {
                    return null;
                }
            }

            // Kiểm tra Audience (Client ID / Project ID dự án)
            String expectedClientId = GoogleConfig.getClientId();
            String expectedProjectId = GoogleConfig.getProjectId();
            if (!expectedClientId.isEmpty() || !expectedProjectId.isEmpty()) {
                boolean match = (aud != null) && (aud.equals(expectedClientId) || aud.equals(expectedProjectId));
                if (!match) {
                    return null; // Token thuộc dự án khác (Audience mismatch)
                }
            }

            // Kiểm tra trạng thái xác thực email
            if (emailVerified != null && !emailVerified) {
                return null;
            }

            String name = extractJsonString(payloadJson, "name");
            String picture = extractJsonString(payloadJson, "picture");

            // 3. Xác thực chữ ký với Google (Online Verification)
            if (!skipRemoteCallForTest) {
                GoogleUser verifiedUser = null;
                if (iss != null && iss.startsWith("https://securetoken.google.com/")) {
                    String apiKey = GoogleConfig.getApiKey();
                    if (!apiKey.isEmpty()) {
                        verifiedUser = verifyWithFirebaseApi(cleanToken, apiKey);
                    } else {
                        System.err.println("[GoogleTokenVerifier] Nhận Firebase token nhưng firebase.api_key rỗng trong cấu hình.");
                    }
                } else {
                    verifiedUser = verifyWithGoogleApi(cleanToken);
                }

                if (verifiedUser == null) {
                    System.err.println("[GoogleTokenVerifier] Xác thực online thất bại cho iss=" + iss);
                    return null;
                }

                // Khẳng định dữ liệu trả về từ Google khớp với sub và email trong token
                if (!sub.equals(verifiedUser.getSub()) || !email.equalsIgnoreCase(verifiedUser.getEmail())) {
                    System.err.println("[GoogleTokenVerifier] Sub/email không khớp giữa token và Google response: sub=" + sub + ", email=" + email);
                    return null;
                }

                // Lấy tên/avatar: ưu tiên kết quả online, fallback về payload JWT
                String finalName = (verifiedUser.getName() != null && !verifiedUser.getName().isEmpty())
                        ? verifiedUser.getName() : name;
                String finalPicture = (verifiedUser.getPicture() != null && !verifiedUser.getPicture().isEmpty())
                        ? verifiedUser.getPicture() : picture;

                return new GoogleUser(verifiedUser.getSub(), verifiedUser.getEmail(), finalName, finalPicture);
            }

            return new GoogleUser(sub, email, name, picture);

        } catch (Exception e) {
            System.err.println("[GoogleTokenVerifier] Ngoại lệ khi kiểm tra token: " + e.getMessage());
            return null;
        }
    }

    /**
     * Xác thực Firebase Auth ID Token qua Identity Toolkit REST API chính thức của Google.
     */
    private GoogleUser verifyWithFirebaseApi(String idToken, String apiKey) {
        HttpURLConnection conn = null;
        try {
            String urlStr = FIREBASE_LOOKUP_URL + URLEncoder.encode(apiKey, StandardCharsets.UTF_8.name());
            URL url = java.net.URI.create(urlStr).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            String bodyJson = "{\"idToken\":\"" + idToken.replace("\\", "\\\\").replace("\"", "\\\"") + "\"}";
            try (java.io.OutputStream out = conn.getOutputStream()) {
                out.write(bodyJson.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("[GoogleTokenVerifier] Firebase lookup HTTP status: " + status);
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (InputStream in = conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String responseJson = sb.toString();
            String localId = extractJsonString(responseJson, "localId");
            String email = extractJsonString(responseJson, "email");
            String displayName = extractJsonString(responseJson, "displayName");
            String photoUrl = extractJsonString(responseJson, "photoUrl");

            if (localId == null || email == null) {
                System.err.println("[GoogleTokenVerifier] Phản hồi Firebase thiếu localId hoặc email.");
                return null;
            }

            return new GoogleUser(localId, email, displayName, photoUrl);

        } catch (Exception e) {
            System.err.println("[GoogleTokenVerifier] Lỗi gọi Firebase Identity Toolkit: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    /**
     * Gửi idToken đến endpoint tokeninfo chính thức của Google (OAuth2) để kiểm tra chữ ký số công khai.
     */
    private GoogleUser verifyWithGoogleApi(String idToken) {
        HttpURLConnection conn = null;
        try {
            String urlStr = GOOGLE_TOKENINFO_URL + URLEncoder.encode(idToken, StandardCharsets.UTF_8.name());
            URL url = java.net.URI.create(urlStr).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                System.err.println("[GoogleTokenVerifier] Google tokeninfo HTTP status: " + status);
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (InputStream in = conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String responseJson = sb.toString();
            String sub = extractJsonString(responseJson, "sub");
            String email = extractJsonString(responseJson, "email");
            String name = extractJsonString(responseJson, "name");
            String picture = extractJsonString(responseJson, "picture");
            String aud = extractJsonString(responseJson, "aud");

            if (sub == null || email == null) {
                return null;
            }

            String expectedClientId = GoogleConfig.getClientId();
            String expectedProjectId = GoogleConfig.getProjectId();
            if (aud != null && (!expectedClientId.isEmpty() || !expectedProjectId.isEmpty())) {
                boolean match = (!expectedClientId.isEmpty() && aud.equals(expectedClientId))
                             || (!expectedProjectId.isEmpty() && aud.equals(expectedProjectId));
                if (!match) {
                    return null;
                }
            }

            return new GoogleUser(sub, email, name, picture);

        } catch (Exception e) {
            System.err.println("[GoogleTokenVerifier] Lỗi gọi Google tokeninfo: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // --- Bộ bóc tách JSON thuần không cần phụ thuộc thư viện ngoài ---

    static String extractJsonString(String json, String key) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1).replace("\\/", "/").replace("\\\"", "\"");
        }
        return null;
    }

    static Long extractJsonLong(String json, String key) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Long.parseLong(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    static Boolean extractJsonBoolean(String json, String key) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false|\"true\"|\"false\")", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            String val = matcher.group(1).toLowerCase().replace("\"", "");
            return "true".equals(val);
        }
        return null;
    }
}
