# [bug-008] Chống CSRF Thao Tác Đánh Dấu Đã Đọc Thông Báo (NotificationServlet)

- **Người thực hiện:** **Phát**
- **Thời gian xử lý:** 2026-10-18
- **Mức độ nghiêm trọng:** 🔴 Chặn (Lỗ hổng bảo mật CSRF State-Changing on GET)
- **Trạng thái:** ✅ Đã hoàn thành

---

## 1. Mô tả lỗ hổng

Trước khi sửa:
- Thao tác đánh dấu đã đọc toàn bộ thông báo (`action=mark_read`) làm thay đổi trạng thái trong CSDL (`notificationDAO.markAllRead(me.getId())`), nhưng lại được đặt bên trong `doGet()` của [`NotificationServlet.java`](../../../src/main/java/truyen/controller/user/NotificationServlet.java).
- `CsrfFilter` của dự án tuân theo quy ước RFC/HTTP: chỉ kiểm tra CSRF token đối với phương thức `POST` (`!"POST".equalsIgnoreCase(request.getMethod()) { chain.doFilter... }`), còn các request `GET` thì bỏ qua vì mặc định GET chỉ để đọc dữ liệu.
- Do đó, bất kỳ website độc hại nào bên ngoài cũng có thể nhúng một thẻ hình ảnh:
  ```html
  <img src="http://localhost:8080/notification?action=mark_read">
  ```
  Khi một độc giả đang đăng nhập vô tình mở trang này, trình duyệt sẽ tự động gửi kèm cookie phiên tới web truyện và xoá toàn bộ chấm đỏ / đánh dấu đã đọc tất cả thông báo của nạn nhân mà nạn nhân không hề hay biết hay bấm nút nào.

---

## 2. Giải pháp kỹ thuật

1. **Chuyển `mark_read` sang phương thức `doPost()` trong `NotificationServlet.java`:**
   - Trong `doGet()`: Chặn dứt điểm mọi request `GET` có `action=mark_read` bằng mã phản hồi `405 Method Not Allowed`.
   - Cài đặt `doPost()`: Kiểm tra phiên đăng nhập, xác nhận `action=mark_read`, gọi `notificationDAO.markAllRead(me.getId())` và trả về JSON `{"success":true}`.
   - Khi chạy qua `POST`, `CsrfFilter` tự động chặn đứng nếu thiếu CSRF token hợp lệ.
2. **Cập nhật JavaScript phía client (`nav.js`):**
   - Sự kiện click nút chuông "✓ Đã đọc hết" (`bellMarkAllBtn`) gửi request `POST` kèm header `X-CSRF-Token` và body `_csrf=...`.
3. **Bổ sung thẻ `<meta name="csrf-token" content="${csrfToken}">` vào `head.jsp`:**
   - Giúp toàn bộ mã script AJAX (Header, Navbar, Reader, v.v.) dễ dàng truy cập token CSRF của phiên hiện tại một cách nhất quán theo chuẩn web.

---

## 3. Các tệp đã thay đổi

| STT | Tệp tin | Thay đổi chính |
|:---:|---|---|
| 1 | [`src/main/java/truyen/controller/user/NotificationServlet.java`](../../../src/main/java/truyen/controller/user/NotificationServlet.java) | Thêm `doPost()`, chuyển `mark_read` sang POST, chặn GET bằng HTTP 405 |
| 2 | [`src/main/webapp/WEB-INF/views/layout/parts/head.jsp`](../../../src/main/webapp/WEB-INF/views/layout/parts/head.jsp) | Thêm thẻ meta `csrf-token` |
| 3 | [`src/main/webapp/assets/js/nav.js`](../../../src/main/webapp/assets/js/nav.js) | Cập nhật fetch sang `POST` có header `X-CSRF-Token` |
| 4 | [`scripts/verify_full_system.py`](../../../scripts/verify_full_system.py) | Bổ sung bài kiểm tra tự động 4.8 và 4.9 cho bug-008 |

---

## 4. Kết quả kiểm thử

- **HTTP 405 Method Not Allowed:** `GET /notification?action=mark_read` bị từ chối chính xác.
- **HTTP 200 OK & JSON success:** `POST /notification?action=mark_read` với CSRF token thành công 100%.
- Toàn bộ 181 bài unit tests và 43 bài integration tests đều PASS.
