package murach.demo;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 14 — Vòng đời của servlet                          (slide 46-47)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Năm method, và không có cách nào nhìn thấy cái nào chạy lúc nào. Thứ tự đó
 *   quan trọng: đặt code khởi tạo nhầm chỗ là nó chạy lại ở MỌI request thay vì
 *   đúng một lần.
 *
 * NĂM METHOD (slide 46)
 *   init()      — chạy MỘT LẦN, khi servlet được nạp
 *   service()   — chạy trước mỗi request, đọc HTTP method rồi gọi doXxx
 *   doGet()     — xử lý request GET
 *   doPost()    — xử lý request POST
 *   destroy()   — chạy một lần, khi servlet bị gỡ (stop server / redeploy)
 *
 * THỨ TỰ THỰC TẾ
 *   Request đầu tiên:   init -> service -> doGet
 *   Các request sau:            service -> doGet        (KHÔNG có init nữa)
 *   Lúc tắt server:     destroy
 *
 * ĐIỀU QUAN TRỌNG NHẤT RÚT RA
 *   Tomcat tạo ĐÚNG MỘT instance của servlet rồi giữ lại dùng cho mọi request.
 *   Từ đó suy ra hai hệ quả trái ngược nhau:
 *     - Lợi:  init() là chỗ đặt việc nặng (mở connection pool, đọc file config).
 *             Đặt trong doGet thì trả giá ở từng request.
 *     - Hại:  một instance dùng chung cho mọi thread nghĩa là biến instance bị
 *             chia sẻ và không thread-safe. Đó chính là CASE 15.
 *
 * VỀ VIỆC OVERRIDE service() — slide 47 BẢO ĐỪNG
 *   Sách nói đúng: override service() là bạn giành lấy việc điều phối cho MỌI
 *   HTTP method, kể cả HEAD và OPTIONS mà HttpServlet vốn đang xử lý đúng giùm
 *   bạn miễn phí. Lớp này override chỉ để CHO THẤY thứ tự gọi, và vẫn gọi
 *   super.service(...) để phần điều phối gốc chạy bình thường. Code thật thì
 *   override doGet hoặc doPost, đừng đụng vào service.
 *
 * TẠI SAO KHÔNG KHAI loadOnStartup
 *   @WebServlet(..., loadOnStartup = 1) sẽ khiến Tomcat gọi init() ngay lúc
 *   khởi động server. Cố ý KHÔNG khai, để bạn thấy init() chạy đúng vào lần
 *   truy cập đầu tiên chứ không phải trước đó.
 * ========================================================================= */
@WebServlet("/lifecycle")
public class LifecycleServlet extends HttpServlet {

    private static final DateTimeFormatter TIME =
            DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    /*
     * Nhật ký các lần gọi.
     *
     * Để "static final" + luôn truy cập trong khối synchronized. Đây chính là
     * bài học của CASE 15 áp dụng ngay tại đây: nhiều thread cùng ghi vào một
     * ArrayList mà không đồng bộ thì list hỏng ngầm — mất phần tử, hoặc ném
     * ArrayIndexOutOfBoundsException ở chỗ chẳng liên quan gì.
     *
     * (Cách gọn hơn trong code thật: Collections.synchronizedList hoặc
     *  CopyOnWriteArrayList. Ở đây viết synchronized tường minh để nhìn thấy
     *  rõ chỗ nào đang được bảo vệ.)
     */
    private static final List<String> EVENTS = new ArrayList<>();

    private static void record(String event) {
        synchronized (EVENTS) {
            EVENTS.add(LocalTime.now().format(TIME) + "  " + event);

            // Giữ tối đa 40 dòng, không thì F5 mãi là list phình vô hạn và
            // ngốn hết bộ nhớ — một kiểu rò rỉ bộ nhớ rất hay gặp.
            while (EVENTS.size() > 40) {
                EVENTS.remove(0);
            }
        }
    }

    /*
     * init() — chạy MỘT LẦN duy nhất, lúc Tomcat nạp servlet.
     *
     * Có hai dạng: init() không tham số (dạng tiện dụng, dùng dạng này) và
     * init(ServletConfig). Nếu override dạng có tham số thì BẮT BUỘC gọi
     * super.init(config), quên là getServletConfig() trả null và mọi lời gọi
     * getInitParameter() ở CASE 12 chết ngay.
     */
    @Override
    public void init() throws ServletException {
        record("init()  - servlet đang được nạp, việc này chỉ xảy ra một lần");
        System.out.println("LifecycleServlet: init()");
    }

    /*
     * service() — chạy trước mỗi request, đọc HTTP method rồi gọi doGet/doPost.
     * Override ở đây CHỈ để ghi nhật ký; xem cảnh báo ở khối chú thích đầu file.
     */
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        record("service() - nhận request HTTP " + request.getMethod());

        // super.service(...) mới là chỗ điều phối sang doGet/doPost.
        // Bỏ dòng này là doGet và doPost không bao giờ được gọi nữa, và trang
        // trả về trắng trơn mà không có lỗi nào.
        super.service(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        record("doGet()  - đang xử lý GET");

        // Chép ra chuỗi ngay trong khối synchronized rồi mới dùng. Không được
        // đưa thẳng EVENTS cho JSP: JSP render ở ngoài khối đồng bộ, lúc đó
        // thread khác có thể đang sửa list -> ConcurrentModificationException.
        String log;
        synchronized (EVENTS) {
            log = String.join("\n", EVENTS);
        }
        request.setAttribute("eventLog", log);

        /*
         * Mã băm định danh của chính object servlet này.
         * F5 bao nhiêu lần cũng ra CÙNG một giá trị — đó là bằng chứng trực
         * tiếp cho câu "Tomcat chỉ tạo một instance", và là nền của CASE 15.
         */
        request.setAttribute("instanceId",
                Integer.toHexString(System.identityHashCode(this)));

        getServletContext()
                .getRequestDispatcher("/demo/case14.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        record("doPost() - đang xử lý POST");
        doGet(request, response);
    }

    /*
     * destroy() — chạy khi servlet bị gỡ: stop server, hoặc redeploy ứng dụng.
     *
     * Đây là chỗ trả lại tài nguyên đã mở trong init(): đóng connection pool,
     * dừng thread nền, ghi nốt cache xuống đĩa. Không có gì cần dọn thì không
     * cần override.
     *
     * Dòng ghi nhật ký dưới đây bạn sẽ KHÔNG bao giờ thấy trên trang web — lúc
     * nó chạy thì server đang tắt, không còn ai để hiện ra nữa. Nhưng dòng
     * System.out.println thì hiện trong terminal lúc bạn bấm Ctrl+C.
     */
    @Override
    public void destroy() {
        record("destroy() - servlet đang bị gỡ");
        System.out.println("LifecycleServlet: destroy()");
    }
}
