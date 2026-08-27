package murach.demo;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* ============================================================================
 * CASE 07 — ServletContext.getRealPath()                  (slide 18-20)
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Servlet biết file dưới cái tên "/WEB-INF/EmailList.txt" — đường dẫn tương
 *   đối so với gốc ứng dụng web. Nhưng java.io.File không hiểu khái niệm đó;
 *   nó cần đường dẫn tuyệt đối trên ổ đĩa. Và đường dẫn tuyệt đối thì mỗi nơi
 *   deploy một khác.
 *
 * CÁCH DÙNG (slide 19 — hai cách viết, y hệt nhau)
 *   ServletContext sc = this.getServletContext();
 *   String path = sc.getRealPath("/WEB-INF/EmailList.txt");
 *
 *   String path = this.getServletContext()
 *                     .getRealPath("/WEB-INF/EmailList.txt");
 *
 * TẠI SAO KHÔNG HARD-CODE ĐƯỜNG DẪN TUYỆT ĐỐI
 *   Slide 19 in ra kết quả trên máy tác giả:
 *     C:\murach\servlet_and_jsp\netbeans\book_apps\ch05email\build\web\WEB-INF\EmailList.txt
 *   Máy bạn ra một chuỗi khác. Server của trường ra chuỗi khác nữa, và trên
 *   Linux còn không có ổ C:. Viết cứng chuỗi này vào code là chương trình chỉ
 *   chạy trên đúng một máy. getRealPath() hỏi server "cái này ở đâu trên máy
 *   ANH" ngay lúc chạy, nên đi đâu cũng đúng.
 *
 * TẠI SAO getServletContext() LÚC NÀO CŨNG GỌI ĐƯỢC (slide 20)
 *   Vì HttpServlet kế thừa GenericServlet, mà method này nằm ở GenericServlet.
 *   Không cần khai báo gì thêm, không cần inject gì cả.
 *   ServletContext còn dùng để: đọc init parameter toàn cục (CASE 12), chia sẻ
 *   biến toàn ứng dụng, ghi log (CASE 16), và lấy RequestDispatcher (CASE 09).
 *
 * CẢNH BÁO — getRealPath() CÓ THỂ TRẢ VỀ null
 *   Nếu ứng dụng chạy trực tiếp từ file .war chưa giải nén thì không có file
 *   thật nào trên đĩa cả, và method này trả null. Tomcat mặc định có giải nén
 *   nên ở đây luôn có giá trị, nhưng đó là lý do code thật thường tránh
 *   getRealPath() và dùng getResourceAsStream() để ĐỌC. Còn để GHI thì vẫn
 *   phải có đường dẫn thật — và thực tế, ghi dữ liệu vào trong thư mục webapp
 *   là chuyện chỉ nên làm khi học: deploy lại một phát là mất sạch.
 * ========================================================================= */
@WebServlet("/realPath")
public class RealPathServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // ---- cách viết thứ nhất ở slide 19: tách ra biến -------------------
        // Dễ đọc hơn khi cần dùng lại sc cho nhiều việc.
        ServletContext sc = this.getServletContext();

        // Tham số phải bắt đầu bằng "/" — đó là gốc ỨNG DỤNG, không phải gốc ổ
        // đĩa. "/WEB-INF/x.txt" nghĩa là WEB-INF/x.txt bên trong ch05email.
        String path = sc.getRealPath("/WEB-INF/EmailList.txt");

        // ---- cách viết thứ hai ở slide 19: nối chuỗi lời gọi ---------------
        // Cùng một lệnh, chỉ khác cách trình bày. Trang demo so sánh hai chuỗi
        // để chứng minh chúng bằng nhau, chứ không phải hai API khác nhau.
        String concise = this.getServletContext()
                             .getRealPath("/WEB-INF/EmailList.txt");

        // Có đường dẫn tuyệt đối rồi thì java.io.File mới làm việc được — đây
        // chính là lý do tồn tại của getRealPath().
        File file = new File(path);

        request.setAttribute("relativePath", "/WEB-INF/EmailList.txt");
        request.setAttribute("realPath", path);
        request.setAttribute("bothFormsAgree", path.equals(concise));

        // File chưa tồn tại là bình thường: chưa ai submit form ở CASE 11.
        request.setAttribute("fileExists", file.exists());
        request.setAttribute("fileSize", file.exists() ? file.length() : 0L);

        // getRealPath("/") cho biết gốc ứng dụng nằm ở đâu — lúc dev là
        // src/main/webapp, lúc deploy là thư mục webapps/ch05email đã giải nén.
        request.setAttribute("contextRoot", sc.getRealPath("/"));

        getServletContext()
                .getRequestDispatcher("/demo/case07.jsp")
                .forward(request, response);
    }
}
