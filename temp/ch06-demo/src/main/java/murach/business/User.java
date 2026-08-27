package murach.business;

import java.io.Serializable;

/* ============================================================================
 * TẦNG MODEL — lớp User (JavaBean)
 * Chapter 5 dùng lại nguyên lớp này từ chapter 2, slide 20 của ch02.
 * ============================================================================
 *
 * VẤN ĐỀ
 *   Servlet đọc được 3 chuỗi rời rạc từ form: firstName, lastName, email.
 *   Nếu cứ truyền 3 biến đó đi khắp nơi thì mỗi lần thêm một trường (số điện
 *   thoại chẳng hạn) là phải sửa chữ ký của mọi method trên đường đi.
 *   Gom chúng vào một object thì thêm trường chỉ sửa đúng lớp này.
 *
 * TẠI SAO PHẢI LÀ "JavaBean" CHỨ KHÔNG PHẢI LỚP THƯỜNG
 *   Slide 20 (ch02) định nghĩa JavaBean là lớp có đủ 3 thứ:
 *     1. constructor không tham số
 *     2. get/set cho mọi biến instance
 *     3. implements Serializable
 *   Không phải quy ước cho vui — thiếu thứ nào là hỏng thứ đó:
 *
 *   - Thiếu get method  -> EL trên JSP im lặng in ra rỗng. Vì ${user.email}
 *                          KHÔNG đọc field, nó gọi getEmail(). Đây là lỗi hay
 *                          gặp nhất và khó thấy nhất, vì không có exception.
 *   - Thiếu constructor rỗng -> các framework tạo object bằng reflection
 *                          (JSP useBean, JPA ở chương sau) không dựng nổi.
 *   - Thiếu Serializable -> Tomcat không ghi được object xuống đĩa khi restart
 *                          hoặc khi cân tải giữa nhiều server. Ở chương 7 khi
 *                          User nằm trong session thì cái này mới thực sự cần.
 * ========================================================================= */
public class User implements Serializable {

    // private hết — bên ngoài chỉ được đụng qua get/set. Đây là điều kiện để
    // sau này muốn validate hay đổi cách lưu thì chỉ sửa trong lớp này.
    private String firstName;
    private String lastName;
    private String email;

    /**
     * Constructor không tham số (điều kiện 1 của JavaBean).
     * Gán chuỗi rỗng thay vì để null: nếu để null, mỗi chỗ dùng đều phải check
     * null trước khi gọi .isEmpty() hay .length(), không thì NullPointerException.
     */
    public User() {
        firstName = "";
        lastName = "";
        email = "";
    }

    /** Constructor tiện dụng — servlet dựng User từ 3 tham số của request. */
    public User(String firstName, String lastName, String email) {
        // this.firstName là biến instance, firstName là tham số.
        // Trùng tên nên bắt buộc phải có "this.", bỏ đi là gán biến cho chính nó.
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // ---- get/set (điều kiện 2 của JavaBean) --------------------------------
    // Tên method phải đúng chuẩn getXxx/setXxx. EL cắt chữ "get", hạ chữ cái
    // đầu, ra tên property: getFirstName() -> ${user.firstName}.
    // Đặt tên là layTen() thì EL không tìm thấy.

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
