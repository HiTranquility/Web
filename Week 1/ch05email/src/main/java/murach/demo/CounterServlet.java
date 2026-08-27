package murach.demo;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 15 — Vì sao KHÔNG dùng biến instance trong servlet (slide 48-50)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Tomcat tạo một instance servlet duy nhất (đã chứng minh ở CASE 14) và chạy
 *   mọi request qua nó, mỗi request một thread. Nên biến instance bị TẤT CẢ
 *   thread dùng chung.
 *
 * TẠI SAO globalCount++ LẠI KHÔNG AN TOÀN
 *   Nhìn thì tưởng một lệnh, thực ra CPU làm ba việc:
 *       1. đọc  globalCount ra thanh ghi
 *       2. cộng 1
 *       3. ghi  kết quả trở lại
 *   Hai thread có thể xen kẽ nhau:
 *
 *       thread A đọc  -> 5
 *       thread B đọc  -> 5          (A chưa kịp ghi)
 *       thread A ghi  -> 6
 *       thread B ghi  -> 6          (đáng lẽ phải là 7)
 *
 *   Hai lần tăng, nhưng chỉ nhích lên một. Một update biến mất. Đó đúng là
 *   "lost updates" mà slide 50 nói tới.
 *
 * TẠI SAO PHẢI CÓ NÚT STRESS TEST TRONG LỚP NÀY
 *   Nói suông thì dễ, nhìn thấy mới tin. Bấm F5 từng cái một sẽ KHÔNG BAO GIỜ
 *   lộ ra lỗi — một trình duyệt là một thread, mà đua thì cần ít nhất hai.
 *   Nên method runStressTest() bắn THREADS x INCREMENTS lần tăng vào cả hai
 *   biến đếm cùng lúc. Biến int thường gần như luôn về đích THIẾU vài trăm
 *   nghìn. AtomicInteger thì không bao giờ sai một đơn vị.
 *
 * VẬY THÌ ĐỂ TRẠNG THÁI Ở ĐÂU
 *   Dữ liệu của riêng một request  -> biến CỤC BỘ trong doGet/doPost. Mỗi
 *                                     thread có bản riêng, tuyệt đối an toàn.
 *   Dữ liệu của riêng một người dùng -> session (chương 7)
 *   Dữ liệu chung toàn ứng dụng     -> ServletContext, hoặc AtomicInteger /
 *                                     synchronized nếu buộc phải là field
 *
 *   Lưu ý cho công bằng: field "final" mà không bao giờ bị sửa sau init() thì
 *   HOÀN TOÀN ổn — một giá trị cấu hình, một object truy cập dữ liệu. Cái nguy
 *   hiểm là việc GHI vào field, không phải bản thân field.
 * ========================================================================= */
@WebServlet("/counter")
public class CounterServlet extends HttpServlet {

    // Đặt số đủ lớn để cuộc đua chắc chắn xảy ra. Nhỏ quá thì các thread chạy
    // xong lần lượt và không bao giờ giẫm lên nhau, bài demo mất tác dụng.
    private static final int THREADS = 50;
    private static final int INCREMENTS = 20000;

    // ---- biến instance, đúng như slide 48 ----------------------------------
    // KHÔNG thread-safe. Đây là thứ chương này bảo bạn đừng viết.
    private int globalCount;

    // ---- cũng là biến đếm, nhưng làm đúng cách -----------------------------
    // AtomicInteger đảm bảo "đọc-cộng-ghi" là MỘT thao tác không thể chen vào
    // giữa, nhờ chỉ thị compare-and-swap của CPU. Nhanh hơn synchronized vì
    // không có thread nào phải nằm chờ.
    private final AtomicInteger safeCount = new AtomicInteger();

    /*
     * init() là chỗ đúng để khởi tạo (CASE 14) — chạy một lần, trước khi có
     * request nào. Thực ra int mặc định đã là 0, gán lại chỉ để nói rõ ý đồ,
     * đúng như slide 48 viết.
     */
    @Override
    public void init() throws ServletException {
        globalCount = 0;
        safeCount.set(0);
    }

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");

        if ("stress".equals(action)) {
            // "stress".equals(action) chứ không phải action.equals("stress"):
            // viết hằng số trước thì action null cũng không sao, khỏi cần check.
            runStressTest(request);

        } else if ("reset".equals(action)) {
            globalCount = 0;
            safeCount.set(0);

        } else {
            // Tăng bình thường, đúng như slide 49.
            globalCount++;                  // <- dòng KHÔNG thread-safe
            safeCount.incrementAndGet();    // <- dòng an toàn, để đối chiếu
        }

        request.setAttribute("globalCount", globalCount);
        request.setAttribute("safeCount", safeCount.get());
        request.setAttribute("threads", THREADS);
        request.setAttribute("increments", INCREMENTS);

        getServletContext()
                .getRequestDispatcher("/demo/case15.jsp")
                .forward(request, response);
    }

    /**
     * Giả lập nhiều trình duyệt cùng gọi servlet này đúng một khoảnh khắc.
     *
     * Không gọi qua HTTP mà đập thẳng vào field, vì mục tiêu là tạo ra cuộc đua
     * chứ không phải đo tốc độ server. Kết quả y hệt: cùng một field, cùng
     * nhiều thread, cùng một lệnh ++.
     */
    private void runStressTest(HttpServletRequest request) {
        globalCount = 0;
        safeCount.set(0);

        /*
         * CountDownLatch dùng như cổng xuất phát của một cuộc đua.
         *
         *   start: đếm từ 1. Mọi thread gọi start.await() và đứng chờ ở đó.
         *          Khi luồng chính gọi countDown(), cả 50 thread bung ra CÙNG
         *          LÚC. Không có cái cổng này thì thread tạo trước đã chạy gần
         *          xong lúc thread cuối mới sinh ra, và chúng ít khi giẫm chân
         *          nhau — lỗi có thể không hiện ra.
         *
         *   done:  đếm từ 50. Mỗi thread xong thì countDown() một cái. Luồng
         *          chính await() cho tới khi về 0, để chắc chắn đọc kết quả sau
         *          khi tất cả đã chạy xong.
         */
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int t = 0; t < THREADS; t++) {
            new Thread(() -> {
                try {
                    start.await();          // chờ ở vạch xuất phát

                    for (int i = 0; i < INCREMENTS; i++) {
                        globalCount++;               // cách sai
                        safeCount.incrementAndGet(); // cách đúng
                    }
                } catch (InterruptedException e) {
                    // Bị ngắt thì phải bật lại cờ interrupt rồi thoát, không
                    // được nuốt im lặng — nếu không, code phía trên mất khả
                    // năng biết mà dừng.
                    Thread.currentThread().interrupt();
                } finally {
                    // finally: dù thành công hay lỗi cũng phải đếm xuống, không
                    // thì luồng chính treo mãi ở done.await() và trang không
                    // bao giờ trả về.
                    done.countDown();
                }
            }).start();
        }

        start.countDown();      // mở cổng, cả 50 thread cùng lao ra

        try {
            done.await();       // chờ tất cả về đích
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int expected = THREADS * INCREMENTS;
        request.setAttribute("ranStress", true);
        request.setAttribute("expected", expected);

        // Đây là con số biết nói: bao nhiêu lần tăng đã bốc hơi.
        // Lưu ý nó luôn ÂM hoặc BẰNG 0 chứ không bao giờ dương — mất mát chứ
        // không bao giờ dư ra.
        request.setAttribute("lostUpdates", expected - globalCount);
    }
}
