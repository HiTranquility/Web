package murach.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import murach.business.User;

/* ============================================================================
 * TẦNG DATA ACCESS — lớp UserIO                        (slide 56)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Servlet cần lưu User xuống đâu đó. Nếu viết thẳng code ghi file vào trong
 *   servlet thì đến chương 12 đổi sang database là phải mổ lại servlet.
 *
 * CÁCH DÙNG
 *   UserIO.add(user, path);
 *   Slide 56 gọi đúng chữ ký này. Slide không in nội dung lớp — chỉ gọi nó —
 *   nên phần thân dưới đây là mình viết.
 *
 * TẠI SAO TÁCH RA MỘT LỚP RIÊNG
 *   Đây là "data access layer" mà slide 5 của ch02 nói tới. Ý tưởng: servlet
 *   chỉ biết "lưu giùm cái User này", không biết lưu vào file hay MySQL.
 *   Đến chương 12, thay ruột method add() bằng JDBC là xong — servlet, JSP,
 *   lớp User không đụng một dòng nào. Đó là toàn bộ giá trị của việc tách lớp.
 *
 * TẠI SAO add() NHẬN path TỪ BÊN NGOÀI CHỨ KHÔNG TỰ BIẾT
 *   Vì lớp này nằm ngoài tầng web — nó không có ServletContext, không gọi được
 *   getRealPath(). Servlet mới là chỗ biết đường dẫn thật (CASE 07 + CASE 12).
 *   Truyền path vào cũng khiến lớp này test được bằng JUnit mà không cần server.
 * ========================================================================= */
public class UserIO {

    /**
     * Ghi thêm 1 dòng vào cuối file, mỗi trường cách nhau bằng ký tự Tab.
     *
     * @param user object cần lưu
     * @param path đường dẫn TUYỆT ĐỐI trên đĩa (servlet đã getRealPath() rồi)
     * @throws IOException để NGUYÊN cho servlet bắt. Không nuốt exception ở
     *         đây: tầng data không biết phải làm gì khi lỗi, còn servlet thì
     *         biết (hiện thông báo, ghi log — xem CASE 16).
     */
    public static void add(User user, String path) throws IOException {
        Path file = Paths.get(path);

        // Tạo thư mục cha nếu chưa có. Không có bước này thì lần chạy đầu tiên
        // trên máy mới sẽ ném NoSuchFileException, dù đường dẫn hoàn toàn đúng.
        if (file.getParent() != null) {
            Files.createDirectories(file.getParent());
        }

        // try-with-resources: writer tự đóng kể cả khi giữa chừng ném exception.
        // Nếu viết kiểu cũ (mở writer, ghi, rồi writer.close() ở cuối) thì gặp
        // lỗi là file không bao giờ được đóng -> mất dữ liệu chưa flush và
        // Windows giữ khoá file luôn.
        try (Writer writer = Files.newBufferedWriter(file,
                    // Chỉ định UTF-8 rõ ràng. Bỏ trống là dùng charset mặc định
                    // của máy — máy này ghi được "Đinh Thị", máy khác ra "?inh Th?".
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,   // chưa có thì tạo
                    StandardOpenOption.APPEND);  // có rồi thì ghi tiếp, KHÔNG đè
             PrintWriter out = new PrintWriter(writer)) {

            // Ngăn cách bằng Tab chứ không phải dấu phẩy: họ tên người ta hay có
            // dấu phẩy, còn Tab thì gần như không ai gõ vào ô input.
            out.println(user.getEmail() + "\t"
                    + user.getFirstName() + "\t"
                    + user.getLastName());
        }
    }

    /**
     * Đọc ngược file thành danh sách User.
     * Chương 5 chưa dùng tới, nhưng có nó thì tầng data mới đúng nghĩa
     * "đọc VÀ ghi" như slide 5 (ch02) mô tả — và để bạn kiểm tra file đã ghi đúng.
     */
    public static List<User> getAll(String path) throws IOException {
        List<User> users = new ArrayList<>();
        File file = new File(path);

        // Chưa ai submit thì file chưa tồn tại. Trả list rỗng, đừng ném exception:
        // "chưa có dữ liệu" là trạng thái bình thường, không phải lỗi.
        if (!file.exists()) {
            return users;
        }

        for (String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
            String[] parts = line.split("\t");

            // Bỏ qua dòng hỏng thay vì để ArrayIndexOutOfBoundsException làm chết
            // cả trang chỉ vì 1 dòng lỗi (ví dụ file bị sửa tay).
            if (parts.length == 3) {
                // Thứ tự trong file là email-first-last, còn constructor là
                // first-last-email. Đảo nhầm ở đây là bug im lặng: chương trình
                // chạy bình thường, chỉ có dữ liệu sai chỗ.
                users.add(new User(parts[1], parts[2], parts[0]));
            }
        }
        return users;
    }
}
