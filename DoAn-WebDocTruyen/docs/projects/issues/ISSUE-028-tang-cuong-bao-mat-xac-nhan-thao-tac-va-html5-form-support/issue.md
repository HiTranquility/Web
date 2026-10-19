# [ISSUE-028] Tăng Cường Bảo Mật Tương Tác Giao Diện: Hộp Thoại Xác Nhận An Toàn & Hỗ Trợ HTML5 Form

- **Người thực hiện:** **Phát**
- **Ngày phát hiện:** 2026-10-18
- **Mức độ:** 🟢 Giao diện & Tương tác người dùng (UI Interaction Hardening)
- **File ảnh hưởng:**
  - `src/main/webapp/WEB-INF/views/_partials/_confirm-modal.jsp`
  - `src/main/webapp/WEB-INF/views/admin/users.jsp`
  - `src/main/webapp/WEB-INF/views/admin/stories.jsp`
  - `src/main/webapp/WEB-INF/views/admin/audit.jsp`

---

## 1. Hiện trạng & Rủi ro

1. **Thao tác nhầm lẫn:**
   - Trên trang quản trị tài khoản `admin/users.jsp`, các nút `Khoá`, `Bỏ khoá`, `↑ Nâng / ↓ Hạ` không có bất kỳ xác nhận nào. Quản trị viên chỉ cần vô tình click nhầm chuột là form POST kích hoạt ngay lập tức.
   - Trên trang quản trị truyện `admin/stories.jsp`, nút `✓ Duyệt đăng` duyệt truyện công khai không có hộp thoại xác nhận.
2. **Lỗi logic bắt form HTML5 trong `_confirm-modal.jsp`:**
   - Khi một nút submit nằm lồng trong form cha (ví dụ `form.jsp`, `form-comic.jsp`, `story/form.jsp`) nhưng chỉ định thuộc tính `form="deleteChapterForm"`, lệnh `target.closest('form')` trong `_confirm-modal.jsp` đã lấy nhầm form cha (form sửa nội dung) thay vì form xoá! Khi người dùng bấm xác nhận xoá, hệ thống lại submit form lưu dữ liệu.

---

## 2. Giải pháp thực hiện

1. **Hỗ trợ trọn vẹn HTML5 `form="..."` trong `_confirm-modal.jsp`:**
   - Dùng `var formId = target.getAttribute('form'); var form = formId ? document.getElementById(formId) : target.closest('form');`.
   - Bổ sung từ khoá tiếng Việt: nhận diện từ khoá `hạ` (danger), `nâng` (info), `duyệt` (info), `khoá` (danger), `xoá`/`gỡ`/`hủy` (danger) để đổi màu sắc icon và text nút bấm tương ứng.
2. **Gắn xác nhận an toàn vào các nút quản trị:**
   - Nút nâng/hạ quyền: `data-confirm="Bạn có chắc chắn muốn hạ quyền/nâng lên Quản trị viên cho tài khoản..."`
   - Nút khoá tài khoản: `data-confirm="Bạn có chắc chắn muốn khoá tài khoản..."`
   - Nút mở khoá: `data-confirm="Bạn có chắc chắn muốn mở khoá cho tài khoản..."`
   - Nút duyệt đăng truyện: `data-confirm="Bạn có chắc chắn muốn duyệt đăng truyện... lên kho truyện công khai?"`
3. **Bộ lọc nhật ký:**
   - Bổ sung thẻ lọc `👤 Đổi vai trò` (`action=CHANGE_ROLE`) trên thanh phân loại của `admin/audit.jsp`.
