package truyen.util;

import java.io.BufferedReader;
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
 * Xác minh tính hợp lệ và chấm điểm rủi ro bot qua Google reCAPTCHA v3 (ISSUE-001 Phase 4).
 *
 * Tiêu chí kỹ thuật nghiêm ngặt:
 * 1. Gọi trực tiếp API https://www.google.com/recaptcha/api/siteverify (Zero external JAR dependency).
 * 2. Timeout cứng 2 giây (2000 ms).
 * 3. Nếu kết nối Google gặp sự cố mạng (Timeout / IOException):
 *    -> Áp dụng cơ chế FAIL-OPEN (cho qua) CÓ CHỦ Ý, không chặn nhầm làm tê liệt toàn bộ trang web.
 * 4. Kiểm tra action tương ứng với form gửi lên (chống tấn công tráo đổi token giữa các form).
 */
public class RecaptchaVerifier {

    private static final String SITEVERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    private static final int TIMEOUT_MS = 2000; // 2 giây

    public static class RecaptchaResult {
        private final boolean success;
        private final double score;
        private final String action;
        private final String hostname;
        private final boolean networkFallback;

        public RecaptchaResult(boolean success, double score, String action, String hostname) {
            this(success, score, action, hostname, false);
        }

        public RecaptchaResult(boolean success, double score, String action, String hostname, boolean networkFallback) {
            this.success = success;
            this.score = score;
            this.action = action;
            this.hostname = hostname;
            this.networkFallback = networkFallback;
        }

        public boolean isSuccess() {
            return success;
        }

        public double getScore() {
            return score;
        }

        public String getAction() {
            return action;
        }

        public String getHostname() {
            return hostname;
        }

        public boolean isNetworkFallback() {
            return networkFallback;
        }
    }

    public RecaptchaVerifier() {
    }

    /**
     * Xác minh token reCAPTCHA nhận từ client.
     *
     * @param token Chuỗi token reCAPTCHA v3 sinh ra từ client
     * @param expectedAction Tên action mong đợi (ví dụ: 'register', 'login', 'comment')
     * @param remoteIp Địa chỉ IP của client (có thể null)
     * @return RecaptchaResult chứa kết quả xác thực và điểm số bot
     */
    public RecaptchaResult verify(String token, String expectedAction, String remoteIp) {
        if (!GoogleConfig.isRecaptchaEnabled()) {
            // Tính năng tắt -> cho qua
            return new RecaptchaResult(true, 1.0, expectedAction, "disabled", false);
        }

        if (token == null || token.trim().isEmpty()) {
            return new RecaptchaResult(false, 0.0, null, "missing-token", false);
        }

        String secretKey = GoogleConfig.getRecaptchaSecretKey();
        if (secretKey.isEmpty()) {
            return new RecaptchaResult(true, 1.0, expectedAction, "missing-secret", false);
        }

        HttpURLConnection conn = null;
        try {
            StringBuilder body = new StringBuilder();
            body.append("secret=").append(URLEncoder.encode(secretKey, StandardCharsets.UTF_8.name()));
            body.append("&response=").append(URLEncoder.encode(token.trim(), StandardCharsets.UTF_8.name()));
            if (remoteIp != null && !remoteIp.trim().isEmpty()) {
                body.append("&remoteip=").append(URLEncoder.encode(remoteIp.trim(), StandardCharsets.UTF_8.name()));
            }

            byte[] postData = body.toString().getBytes(StandardCharsets.UTF_8);

            URL url = java.net.URI.create(SITEVERIFY_URL).toURL();
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(postData.length));
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(postData);
                os.flush();
            }

            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                // fail-open, CÓ CHỦ Ý: Google server lỗi -> cho qua
                return new RecaptchaResult(true, 1.0, expectedAction, "google-http-" + status, true);
            }

            StringBuilder sb = new StringBuilder();
            try (InputStream in = conn.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            String json = sb.toString();
            Boolean success = extractJsonBoolean(json, "success");
            Double score = extractJsonDouble(json, "score");
            String action = extractJsonString(json, "action");
            String hostname = extractJsonString(json, "hostname");

            if (Boolean.TRUE.equals(success)) {
                double verifiedScore = score != null ? score : 0.0;

                // Kiểm tra khớp action nếu có yêu cầu
                if (expectedAction != null && action != null && !expectedAction.equalsIgnoreCase(action)) {
                    return new RecaptchaResult(false, 0.0, action, hostname, false);
                }

                return new RecaptchaResult(true, verifiedScore, action, hostname, false);
            } else {
                return new RecaptchaResult(false, 0.0, action, hostname, false);
            }

        } catch (IOException e) {
            // fail-open, CÓ CHỦ Ý: Mạng chập chờn hoặc timeout 2s thì cho qua, không chặn nhầm cả site
            return new RecaptchaResult(true, 1.0, expectedAction, "fail-open-timeout", true);
        } catch (Exception e) {
            return new RecaptchaResult(false, 0.0, null, "internal-error", false);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    // --- Parser JSON thuần không cần thư viện ngoài ---

    static Boolean extractJsonBoolean(String json, String key) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*(true|false)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return null;
    }

    static Double extractJsonDouble(String json, String key) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*([0-9]*\\.?[0-9]+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    static String extractJsonString(String json, String key) {
        if (json == null) return null;
        Pattern pattern = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
