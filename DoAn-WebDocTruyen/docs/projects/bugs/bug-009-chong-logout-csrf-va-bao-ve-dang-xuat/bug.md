# 🐛 bug-009: Chống Lỗ Hổng Logout CSRF & Bảo Vệ Đăng Xuất An Toàn

- **Mã định danh:** `bug-009`
- **Mức độ:** 🔴 Chặn (Blocker / Security CSRF)
- **Người thực hiện:** **Phát**
- **Ngày phát hiện & xử lý:** 2026-10-19
- **Trạng thái:** ✅ Đã hoàn thành (Verified PASS)

---

## 1. Mô tả lỗ hổng
Trong `AuthServlet.java`, switch case `action=logout` ban đầu tiếp nhận cả yêu cầu `GET` và `POST`.
Do `CsrfFilter` chỉ kích hoạt kiểm tra token đối với các request phương thức `POST`, các request `GET` lọt qua filter hoàn toàn.

Hậu quả:
- Kẻ xấu có thể nhúng thẻ ảnh ẩn `<img src="http://localhost:8080/auth?action=logout">` trên bất kỳ trang web hoặc diễn đàn nào.
- Khi người dùng truy cập trang web đó, trình duyệt tự động gửi GET request đến `/auth?action=logout`, kích hoạt `session.invalidate()` và huỷ sạch phiên làm việc của người dùng mà không cần tương tác hay sự cho phép từ nạn nhân (Logout CSRF / Session Denial of Service).
- Trên thanh điều hướng, nút Đăng xuất là một liên kết thẻ `<a>` thông thường, người dùng bấm vào là bị đá ra ngay lập tức mà không có cơ chế xác nhận chống bấm nhầm.

---

## 2. Giải pháp kỹ thuật

1. **Khóa chặt method POST trong `AuthServlet.java`:**
   ```java
   private void logout(HttpServletRequest request, HttpServletResponse response)
           throws IOException {
       if (!"POST".equalsIgnoreCase(request.getMethod())) {
           response.sendRedirect(request.getContextPath() + "/");
           return;
       }
       HttpSession session = request.getSession(false);
       if (session != null) {
           session.invalidate();
       }
       response.sendRedirect(request.getContextPath() + "/");
   }
   ```
   Nếu kẻ xấu tấn công qua GET, yêu cầu bị từ chối huỷ phiên và chuyển hướng an toàn về `/`.

2. **Form POST ẩn & Modal Xác nhận trên `nav.jsp`:**
   Tạo form ẩn có chứa `_csrf` token. Khi bấm Đăng xuất, gọi `window.showConfirmModal` hiển thị hộp thoại xác nhận ("Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?"). Khi người dùng bấm đồng ý mới submit POST form.

3. **Chuyển đổi đăng xuất an toàn trên `error/403.jsp`:**
   Chuyển nút Đổi tài khoản sang submit POST kèm token CSRF.

---

## 3. Kết quả kiểm thử
- **Integration Test:** Đã thêm bài kiểm tra 4.12 và 4.13 trong `verify_full_system.py`.
- **Kết quả:** `GET /auth?action=logout` không làm mất phiên; `POST /auth?action=logout` huỷ phiên thành công. Đạt 100% PASS.
