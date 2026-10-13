package truyen;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import truyen.util.JsonUtil;

@DisplayName("JsonUtil — Chuẩn hoá định dạng và tiện ích JSON")
class JsonUtilTest {

    @Test
    @DisplayName("Ca 1: jsonEscape mã hóa chuỗi thường và chuỗi null an toàn")
    void testJsonEscapeBasic() {
        assertEquals("\"\"", JsonUtil.jsonEscape(null));
        assertEquals("\"\"", JsonUtil.jsonEscape(""));
        assertEquals("\"Xin chào thế giới\"", JsonUtil.jsonEscape("Xin chào thế giới"));
    }

    @Test
    @DisplayName("Ca 2: jsonEscape mã hóa ký tự đặc biệt, dấu ngoặc kép, tab và xuống dòng")
    void testJsonEscapeSpecialChars() {
        assertEquals("\"\\\"Trích dẫn\\\"\"", JsonUtil.jsonEscape("\"Trích dẫn\""));
        assertEquals("\"Dòng 1\\nDòng 2\"", JsonUtil.jsonEscape("Dòng 1\nDòng 2"));
        assertEquals("\"Cột 1\\tCột 2\"", JsonUtil.jsonEscape("Cột 1\tCột 2"));
    }

    @Test
    @DisplayName("Ca 3: jsonEscape xử lý chuẩn xác dấu gạch chéo ngược")
    void testJsonEscapeBackslash() {
        assertEquals("\"Đường dẫn: C:\\\\App\\\\Web\"", JsonUtil.jsonEscape("Đường dẫn: C:\\App\\Web"));
    }
}
