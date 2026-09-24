package truyen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.controller.api.ApiServlet;

@DisplayName("ApiServlet — RESTful JSON API (ISSUE-009)")
class ApiServletTest {

    @Test
    @DisplayName("Ca 1: jsonEscape mã hóa ký tự đặc biệt, dấu ngoặc kép và xuống dòng an toàn")
    void testJsonEscape() {
        assertEquals("\"\"", ApiServlet.jsonEscape(null));
        assertEquals("\"Hello World\"", ApiServlet.jsonEscape("Hello World"));
        assertEquals("\"\\\"Bình luận\\\"\"", ApiServlet.jsonEscape("\"Bình luận\""));
        assertEquals("\"Dòng 1\\nDòng 2\"", ApiServlet.jsonEscape("Dòng 1\nDòng 2"));
        assertEquals("\"Tab\\tỞ đây\"", ApiServlet.jsonEscape("Tab\tỞ đây"));
    }

    @Test
    @DisplayName("Ca 2: jsonEscape xử lý an toàn dấu gạch chéo ngược")
    void testJsonEscapeBackslash() {
        assertEquals("\"Đường dẫn: C:\\\\Users\\\\Admin\"", ApiServlet.jsonEscape("Đường dẫn: C:\\Users\\Admin"));
    }
}
