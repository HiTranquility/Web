package truyen.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Quản lý cấu hình tích hợp Google Authentication & Firebase.
 *
 * Đọc file src/main/resources/google.properties đúng MỘT lần khi khởi động.
 * Nếu file chưa có hoặc thiếu thông tin, tự động tắt tính năng an toàn (fallback),
 * tuyệt đối không gây lỗi 500 hay làm trắng trang web.
 */
public class GoogleConfig {

    private static final String CONFIG_FILE = "google.properties";

    private static Properties defaultProps = loadConfig();
    private static Properties activeProps = defaultProps;

    private static Properties loadConfig() {
        Properties props = new Properties();
        try (InputStream in = GoogleConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            // Không ném lỗi ra ngoài - fallback an toàn
        }
        return props;
    }

    /**
     * Kiểm tra tính năng Đăng nhập Google có đang được kích hoạt và cấu hình hợp lệ không.
     */
    public static boolean isEnabled() {
        String enabledVal = activeProps.getProperty("google.auth.enabled", "false").trim();
        String clientId = getClientId();
        String apiKey = getApiKey();
        return "true".equalsIgnoreCase(enabledVal) && (!clientId.isEmpty() || !apiKey.isEmpty());
    }

    /**
     * Kiểm tra tính năng Sao lưu Google Drive có đang được kích hoạt hay không.
     */
    public static boolean isDriveEnabled() {
        String enabledVal = activeProps.getProperty("google.drive.enabled", "true").trim();
        String clientId = getClientId();
        return "true".equalsIgnoreCase(enabledVal) && !clientId.isEmpty();
    }

    public static String getClientId() {
        return activeProps.getProperty("google.client_id", "").trim();
    }

    public static String getClientSecret() {
        return activeProps.getProperty("google.client_secret", "").trim();
    }

    public static String getApiKey() {
        return activeProps.getProperty("firebase.api_key", "").trim();
    }

    public static String getAuthDomain() {
        return activeProps.getProperty("firebase.auth_domain", "").trim();
    }

    public static String getProjectId() {
        return activeProps.getProperty("firebase.project_id", "").trim();
    }

    public static String getAppId() {
        return activeProps.getProperty("firebase.app_id", "").trim();
    }

    /**
     * Kiểm tra tính năng Google reCAPTCHA v3 có đang được bật và cấu hình đầy đủ hay không.
     */
    public static boolean isRecaptchaEnabled() {
        String enabledVal = activeProps.getProperty("google.recaptcha.enabled", "false").trim();
        String siteKey = getRecaptchaSiteKey();
        String secretKey = getRecaptchaSecretKey();
        return "true".equalsIgnoreCase(enabledVal) && !siteKey.isEmpty() && !secretKey.isEmpty();
    }

    public static String getRecaptchaSiteKey() {
        return activeProps.getProperty("google.recaptcha.site_key", "").trim();
    }

    public static String getRecaptchaSecretKey() {
        return activeProps.getProperty("google.recaptcha.secret_key", "").trim();
    }

    public static double getRecaptchaThreshold() {
        String val = activeProps.getProperty("google.recaptcha.threshold", "0.5").trim();
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return 0.5;
        }
    }

    /**
     * Cho phép nạp cấu hình ghi đè phục vụ kiểm thử đơn vị (Unit Tests).
     */
    public static void setOverrideConfig(Properties props) {
        if (props != null) {
            activeProps = props;
        }
    }

    /**
     * Phục hồi lại cấu hình nạp từ file google.properties ban đầu.
     */
    public static void reset() {
        activeProps = defaultProps;
    }
}
