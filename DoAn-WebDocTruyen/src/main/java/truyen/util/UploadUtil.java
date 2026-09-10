package truyen.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.util.Base64;
import javax.servlet.ServletContext;
import javax.servlet.http.Part;

/**
 * Nhận ảnh bìa người dùng tải lên.
 *
 * VÌ SAO LƯU NGOÀI THƯ MỤC WEBAPP
 *   Để trong webapp thì mỗi lần triển khai lại, Tomcat xoá sạch thư mục cũ và
 *   ảnh của người dùng bay theo. Đây đúng là cái bẫy getRealPath() — nó trả về
 *   một đường dẫn CÓ THẬT nên code chạy ngon, chỉ mất dữ liệu về sau.
 *
 *   Đường dẫn đọc từ context-param "uploadDir" trong web.xml: đổi máy chỉ cần
 *   sửa một dòng, không phải biên dịch lại.
 *
 * HẬU QUẢ: Tomcat KHÔNG tự phục vụ được thư mục đó nữa. Vì vậy có thêm
 * UploadedFileServlet map vào /uploads/* để đọc file ra. Đó là cái giá phải
 * trả, và trả là đúng.
 */
public final class UploadUtil {

    /** Giới hạn mềm; @MultipartConfig của servlet mới là chốt chặn thật. */
    public static final long MAX_BYTES = 2L * 1024 * 1024;   // 2 MB

    private static final SecureRandom RANDOM = new SecureRandom();

    private UploadUtil() { }

    /** Lỗi có câu chữ đưa thẳng cho người dùng đọc được. */
    public static class UploadException extends Exception {
        public UploadException(String message) { super(message); }
    }

    /**
     * Lưu file, trả về đường dẫn công khai ("/uploads/abc123.png").
     * Trả null nếu người dùng không chọn file nào.
     */
    public static String save(Part part, ServletContext ctx)
            throws UploadException, IOException {

        if (part == null || part.getSize() == 0) {
            return null;      // không chọn file -> không phải lỗi
        }
        if (part.getSize() > MAX_BYTES) {
            throw new UploadException("Ảnh quá "
                    + (MAX_BYTES / 1024 / 1024) + " MB. Chọn ảnh nhỏ hơn.");
        }

        String dir = ctx.getInitParameter("uploadDir");
        if (dir == null || dir.trim().isEmpty()) {
            throw new UploadException(
                    "Máy chủ chưa cấu hình thư mục lưu ảnh (uploadDir trong web.xml).");
        }

        /*
         * NHẬN DẠNG ẢNH BẰNG BYTE ĐẦU FILE, KHÔNG TIN Content-Type
         *
         * Content-Type và tên file đều do TRÌNH DUYỆT gửi lên, mà cái gì do
         * phía kia gửi thì phía kia sửa được. Đặt tên "hack.jsp" rồi khai
         * "image/png" là qua được mọi phép kiểm dựa trên hai thứ đó.
         *
         * Vài byte đầu file thì nằm trong chính nội dung — muốn giả phải làm
         * ra một file mở lên đúng là ảnh thật.
         */
        String ext;
        try (InputStream in = part.getInputStream()) {
            ext = sniff(in);
        }
        if (ext == null) {
            throw new UploadException(
                    "File này không phải ảnh PNG, JPG, GIF hay WebP.");
        }

        File folder = new File(dir);
        if (!folder.isDirectory() && !folder.mkdirs()) {
            throw new UploadException("Không tạo được thư mục lưu ảnh: " + dir);
        }

        /*
         * TÊN FILE DO MÌNH SINH, TUYỆT ĐỐI KHÔNG DÙNG TÊN NGƯỜI DÙNG GỬI LÊN.
         *
         * Tên gốc có thể là "../../../web.xml" (ghi đè file hệ thống), có thể
         * trùng tên file người khác (đè mất ảnh của họ), có thể chứa thẻ HTML
         * (thành lỗ XSS khi in tên ra trang). Sinh tên ngẫu nhiên là hết cả
         * ba, không cần lọc gì.
         */
        byte[] bytes = new byte[12];
        RANDOM.nextBytes(bytes);
        String name = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes) + ext;

        Path dest = folder.toPath().resolve(name);
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        }
        return "/uploads/" + name;
    }

    /**
     * Đọc vài byte đầu để đoán định dạng thật. Trả phần đuôi file, hoặc null
     * nếu không phải ảnh mình chấp nhận.
     *
     * Đây gọi là "magic number" — mỗi định dạng có một chuỗi byte mở đầu cố
     * định do chuẩn của nó quy định.
     */
    private static String sniff(InputStream in) throws IOException {
        byte[] h = new byte[12];
        int n = 0;
        while (n < h.length) {
            int r = in.read(h, n, h.length - n);
            if (r < 0) break;
            n += r;
        }
        if (n < 12) {
            return null;     // ngắn hơn cả phần đầu -> chắc chắn không phải ảnh
        }

        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') {
            return ".png";
        }
        // JPEG: FF D8 FF
        if ((h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) {
            return ".jpg";
        }
        // GIF: "GIF87a" hoac "GIF89a"
        if (h[0] == 'G' && h[1] == 'I' && h[2] == 'F') {
            return ".gif";
        }
        // WebP: "RIFF" ....(4 byte do dai).... "WEBP"
        if (h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') {
            return ".webp";
        }
        return null;
    }
}
