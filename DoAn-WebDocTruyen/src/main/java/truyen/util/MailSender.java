package truyen.util;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tiện ích gửi email qua giao thức SMTP (JavaMail) — giải quyết ISSUE-002.
 *
 * Hỗ trợ:
 *   - Hàng đợi xử lý bất đồng bộ (Queue) qua ExecutorService để phản hồi web tức thì.
 *   - Chế độ Mô phỏng (Dev Simulation) an toàn khi chưa cấu hình máy chủ SMTP thật.
 *   - Định dạng thư HTML thẩm mỹ, chuyên nghiệp, bảo mật.
 */
public class MailSender {

    private static final Logger LOGGER = Logger.getLogger(MailSender.class.getName());
    private static final Properties CONFIG = new Properties();

    /** Hàng đợi thực thi gửi email ngầm (không block request của người dùng). */
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2, r -> {
        Thread t = new Thread(r, "MailSender-Worker");
        t.setDaemon(true);
        return t;
    });

    static {
        loadConfig();
    }

    private static void loadConfig() {
        try (InputStream in = MailSender.class.getClassLoader().getResourceAsStream("mail.properties")) {
            if (in != null) {
                CONFIG.load(in);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không đọc được file mail.properties, dùng cấu hình mặc định", e);
        }
    }

    public static boolean isMailEnabled() {
        String enabled = System.getProperty("mail.enabled", CONFIG.getProperty("mail.enabled", "false"));
        return "true".equalsIgnoreCase(enabled.trim());
    }

    /**
     * Gửi email đặt lại mật khẩu bất đồng bộ (đẩy vào hàng đợi ngầm).
     */
    public static CompletableFuture<Boolean> sendPasswordResetEmailAsync(String toEmail, String recipientName, String resetUrl) {
        return CompletableFuture.supplyAsync(() -> sendPasswordResetEmail(toEmail, recipientName, resetUrl), EXECUTOR);
    }

    /**
     * Gửi email đặt lại mật khẩu đồng bộ.
     */
    public static boolean sendPasswordResetEmail(String toEmail, String recipientName, String resetUrl) {
        if (toEmail == null || toEmail.trim().isEmpty()) {
            LOGGER.warning("Không thể gửi email: địa chỉ người nhận rỗng");
            return false;
        }

        String safeName = (recipientName != null && !recipientName.trim().isEmpty()) ? recipientName.trim() : "Bạn";
        String subject = "[Web Đọc Truyện] Hướng dẫn đặt lại mật khẩu tài khoản";

        // Nếu máy chủ chưa bật SMTP thật (Chế độ phát triển / Đồ án):
        if (!isMailEnabled()) {
            printDevSimulationLog(toEmail, safeName, subject, resetUrl);
            return true;
        }

        // Gửi email thật qua SMTP
        try {
            String host = CONFIG.getProperty("mail.smtp.host", "smtp.gmail.com");
            String port = CONFIG.getProperty("mail.smtp.port", "587");
            String auth = CONFIG.getProperty("mail.smtp.auth", "true");
            String starttls = CONFIG.getProperty("mail.smtp.starttls.enable", "true");
            String username = CONFIG.getProperty("mail.smtp.username", "");
            String password = CONFIG.getProperty("mail.smtp.password", "");
            String fromEmail = CONFIG.getProperty("mail.from", "noreply@webdoctruyen.com");
            String fromName = CONFIG.getProperty("mail.from.name", "Web Đọc Truyện");

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", auth);
            props.put("mail.smtp.starttls.enable", starttls);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session;
            if ("true".equalsIgnoreCase(auth) && !username.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject, StandardCharsets.UTF_8.name());

            String htmlBody = buildResetEmailHtml(safeName, resetUrl);
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            LOGGER.info("Đã gửi email đặt lại mật khẩu thành công tới: " + toEmail);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gửi email đặt lại mật khẩu thất bại tới: " + toEmail, e);
            return false;
        }
    }

    /**
     * Dựng nội dung email HTML thẩm mỹ, tương thích tốt với Gmail, Outlook, Apple Mail.
     */
    public static String buildResetEmailHtml(String recipientName, String resetUrl) {
        return "<!DOCTYPE html>"
             + "<html>"
             + "<head><meta charset='UTF-8'></head>"
             + "<body style='margin:0;padding:24px;background-color:#121212;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,sans-serif;color:#e0e0e0;'>"
             + "  <div style='max-width:560px;margin:0 auto;background-color:#1e1e1e;border-radius:12px;padding:32px;border:1px solid #333;'>"
             + "    <div style='text-align:center;margin-bottom:24px;'>"
             + "      <h1 style='color:#e65100;margin:0;font-size:24px;letter-spacing:1px;'>📖 WEB ĐỌC TRUYỆN</h1>"
             + "      <p style='color:#888;margin:6px 0 0 0;font-size:13px;'>Nền tảng đọc và chia sẻ truyện cộng đồng</p>"
             + "    </div>"
             + "    <h2 style='font-size:18px;color:#fff;margin-top:0;'>Xin chào " + escapeHtml(recipientName) + ",</h2>"
             + "    <p style='line-height:1.6;color:#ccc;'>Chúng tôi nhận được yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này.</p>"
             + "    <p style='line-height:1.6;color:#ccc;'>Vui lòng bấm vào nút bên dưới để tiến hành thiết lập mật khẩu mới (liên kết có hiệu lực trong <b>30 phút</b>):</p>"
             + "    <div style='text-align:center;margin:32px 0;'>"
             + "      <a href='" + escapeHtml(resetUrl) + "' style='background-color:#e65100;color:#ffffff;text-decoration:none;padding:12px 28px;border-radius:6px;font-weight:600;display:inline-block;box-shadow:0 2px 8px rgba(230,81,0,0.4);'>Đặt lại mật khẩu</a>"
             + "    </div>"
             + "    <p style='font-size:13px;color:#888;line-height:1.5;'>Nếu nút bấm không hoạt động, bạn có thể sao chép liên kết này dán vào thanh địa chỉ trình duyệt:<br>"
             + "      <a href='" + escapeHtml(resetUrl) + "' style='color:#ff9800;word-break:break-all;'>" + escapeHtml(resetUrl) + "</a>"
             + "    </p>"
             + "    <hr style='border:none;border-top:1px solid #333;margin:28px 0;'>"
             + "    <p style='font-size:12px;color:#777;margin:0;'>Nếu bạn KHÔNG yêu cầu đặt lại mật khẩu, xin vui lòng bỏ qua thư này. Tài khoản của bạn vẫn an toàn tuyệt đối.</p>"
             + "  </div>"
             + "</body>"
             + "</html>";
    }

    private static void printDevSimulationLog(String toEmail, String recipientName, String subject, String resetUrl) {
        System.out.println("\n"
             + "================================================================================\n"
             + " [MailSender - CHẾ ĐỘ MÔ PHỎNG AN TOÀN / DEV MODE]\n"
             + "--------------------------------------------------------------------------------\n"
             + " Người nhận : " + recipientName + " <" + toEmail + ">\n"
             + " Tiêu đề    : " + subject + "\n"
             + " Đường dẫn  : " + resetUrl + "\n"
             + " Thời hạn   : Vé có giá trị trong 30 phút kể từ lúc gửi.\n"
             + " Ghi chú    : Đã bảo vệ an toàn (Không in link ra màn hình giao diện web).\n"
             + "              Bật 'mail.enabled=true' trong mail.properties để gửi qua SMTP thật.\n"
             + "================================================================================\n");
    }

    /**
     * Gửi email khiếu nại kiểm duyệt bất đồng bộ (đẩy vào hàng đợi ngầm).
     */
    public static CompletableFuture<Boolean> sendAppealEmailAsync(String authorName, String authorEmail,
                                                                  String storyTitle, int storyId,
                                                                  String appealTitle, String appealReason) {
        return CompletableFuture.supplyAsync(
                () -> sendAppealEmail(authorName, authorEmail, storyTitle, storyId, appealTitle, appealReason),
                EXECUTOR);
    }

    /**
     * Gửi email khiếu nại kiểm duyệt đồng bộ tới Admin.
     */
    public static boolean sendAppealEmail(String authorName, String authorEmail,
                                          String storyTitle, int storyId,
                                          String appealTitle, String appealReason) {
        String safeAuthor = (authorName != null && !authorName.trim().isEmpty()) ? authorName.trim() : "Tác giả";
        String safeEmail = (authorEmail != null && !authorEmail.trim().isEmpty()) ? authorEmail.trim() : "khong-ro@user.local";
        String safeStory = (storyTitle != null && !storyTitle.trim().isEmpty()) ? storyTitle.trim() : ("Truyện #" + storyId);
        String safeTitle = (appealTitle != null && !appealTitle.trim().isEmpty()) ? appealTitle.trim() : "Khiếu nại quyết định kiểm duyệt truyện";
        String safeReason = appealReason != null ? appealReason.trim() : "";

        String adminEmail = CONFIG.getProperty("mail.admin", CONFIG.getProperty("mail.from", "admin@webdoctruyen.com"));
        String subject = "[KHIẾU NẠI KIỂM DUYỆT] Đơn khiếu nại từ tác giả " + safeAuthor + " - Truyện: " + safeStory;

        if (!isMailEnabled()) {
            printAppealDevSimulationLog(adminEmail, safeAuthor, safeEmail, safeStory, storyId, safeTitle, safeReason);
            return true;
        }

        try {
            String host = CONFIG.getProperty("mail.smtp.host", "smtp.gmail.com");
            String port = CONFIG.getProperty("mail.smtp.port", "587");
            String auth = CONFIG.getProperty("mail.smtp.auth", "true");
            String starttls = CONFIG.getProperty("mail.smtp.starttls.enable", "true");
            String username = CONFIG.getProperty("mail.smtp.username", "");
            String password = CONFIG.getProperty("mail.smtp.password", "");
            String fromEmail = CONFIG.getProperty("mail.from", "noreply@webdoctruyen.com");
            String fromName = CONFIG.getProperty("mail.from.name", "Web Đọc Truyện");

            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", port);
            props.put("mail.smtp.auth", auth);
            props.put("mail.smtp.starttls.enable", starttls);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");

            Session session;
            if ("true".equalsIgnoreCase(auth) && !username.isEmpty()) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
            } else {
                session = Session.getInstance(props);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail, fromName, StandardCharsets.UTF_8.name()));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(adminEmail));
            if (!safeEmail.contains("@user.local")) {
                message.setReplyTo(new Address[]{ new InternetAddress(safeEmail, safeAuthor, StandardCharsets.UTF_8.name()) });
            }
            message.setSubject(subject, StandardCharsets.UTF_8.name());

            String htmlBody = buildAppealEmailHtml(safeAuthor, safeEmail, safeStory, storyId, safeTitle, safeReason);
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            LOGGER.info("Đã gửi email khiếu nại tới Admin: " + adminEmail + " từ tác giả: " + safeEmail);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Gửi email khiếu nại tới Admin thất bại", e);
            return false;
        }
    }

    /**
     * Dựng nội dung email khiếu nại kiểm duyệt HTML.
     */
    public static String buildAppealEmailHtml(String authorName, String authorEmail,
                                              String storyTitle, int storyId,
                                              String appealTitle, String appealReason) {
        return "<!DOCTYPE html>"
             + "<html>"
             + "<head><meta charset='UTF-8'></head>"
             + "<body style='margin:0;padding:24px;background-color:#121212;font-family:-apple-system,BlinkMacSystemFont,\"Segoe UI\",Roboto,sans-serif;color:#e0e0e0;'>"
             + "  <div style='max-width:600px;margin:0 auto;background-color:#1e1e1e;border-radius:12px;padding:32px;border:1px solid #333;'>"
             + "    <div style='text-align:center;margin-bottom:24px;border-bottom:1px solid #333;padding-bottom:16px;'>"
             + "      <h1 style='color:#e65100;margin:0;font-size:22px;letter-spacing:1px;'>🛡️ THƯ KHIẾU NẠI KIỂM DUYỆT</h1>"
             + "      <p style='color:#888;margin:6px 0 0 0;font-size:13px;'>Hệ thống tiếp nhận phản ánh & giải trình từ tác giả</p>"
             + "    </div>"
             + "    <div style='background-color:#2a2a2a;padding:16px;border-radius:8px;margin-bottom:20px;'>"
             + "      <p style='margin:4px 0;font-size:14px;color:#bbb;'><b>Tác giả:</b> <span style='color:#fff;'>" + escapeHtml(authorName) + "</span> (" + escapeHtml(authorEmail) + ")</p>"
             + "      <p style='margin:4px 0;font-size:14px;color:#bbb;'><b>Truyện liên quan:</b> <span style='color:#ff9800;font-weight:600;'>" + escapeHtml(storyTitle) + "</span> (ID: #" + storyId + ")</p>"
             + "      <p style='margin:4px 0;font-size:14px;color:#bbb;'><b>Tiêu đề khiếu nại:</b> <span style='color:#fff;'>" + escapeHtml(appealTitle) + "</span></p>"
             + "    </div>"
             + "    <h3 style='font-size:15px;color:#fff;margin-bottom:8px;'>Nội dung giải trình / khiếu nại từ tác giả:</h3>"
             + "    <div style='background-color:#161616;padding:18px;border-radius:8px;border-left:4px solid #e65100;line-height:1.6;font-size:14px;color:#ddd;white-space:pre-wrap;'>"
             + escapeHtml(appealReason)
             + "    </div>"
             + "    <p style='margin-top:24px;font-size:13px;color:#888;line-height:1.5;'>Admin có thể bấm <b>Reply / Trả lời</b> trực tiếp email này để trao đổi với tác giả hoặc truy cập vào trang <b>Quản trị Báo cáo</b> trên hệ thống.</p>"
             + "  </div>"
             + "</body>"
             + "</html>";
    }

    private static void printAppealDevSimulationLog(String adminEmail, String authorName, String authorEmail,
                                                   String storyTitle, int storyId,
                                                   String appealTitle, String appealReason) {
        System.out.println("\n"
             + "================================================================================\n"
             + " [MailSender - KHIẾU NẠI KIỂM DUYỆT / APPEAL DEV SIMULATION]\n"
             + "--------------------------------------------------------------------------------\n"
             + " Người gửi (Tác giả) : " + authorName + " <" + authorEmail + ">\n"
             + " Truyện khiếu nại     : " + storyTitle + " (ID: #" + storyId + ")\n"
             + " Tiêu đề khiếu nại    : " + appealTitle + "\n"
             + " Nội dung giải trình  : " + appealReason + "\n"
             + " Gửi tới hòm thư Admin: " + adminEmail + "\n"
             + " Ghi chú              : Đã lưu báo cáo & mô phỏng gửi email tới Admin thành công.\n"
             + "================================================================================\n");
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
