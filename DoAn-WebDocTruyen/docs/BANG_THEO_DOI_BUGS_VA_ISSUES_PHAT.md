# 📋 BẢNG THEO DÕI VÀ HƯỚNG DẪN XỬ LÝ BUG & ISSUES

- **Dự án:** DoAn-WebDocTruyen
- **Người thực hiện:** **Phát**
- **Ngày lập:** 2026-10-18
- **Tình trạng tổng quan:** 5 Bugs bảo mật/logic + 6 Issues tối ưu giao diện & CSS

---

## 📑 Bảng tổng hợp nhiệm vụ

| STT | Mã nhiệm vụ | Tên nhiệm vụ / Mô tả | Mức độ | File ảnh hưởng chính | Trạng thái |
|:---:|:---:|---|:---:|---|:---:|
| 1 | **[bug-003](projects/bugs/bug-003-admin-filter-lo-hong-phan-quyen-editor/bug.md)** | `AdminFilter` kiểm tra sai điều kiện, để `EDITOR` lọt vào xem và sửa `/admin/wallet`, `/admin/user` | 🔴 Chặn | `truyen/filter/AdminFilter.java` | ✅ Đã xong |
| 2 | **[bug-004](projects/bugs/bug-004-mat-quyen-rbac-khi-cap-nhat-ho-so-hoac-vip/bug.md)** | Cập nhật hồ sơ hoặc gia hạn VIP làm mất sạch vai trò và quyền RBAC trong phiên cho đến khi relogin | 🔴 Chặn | `truyen/controller/user/UserServlet.java` | ✅ Đã xong |
| 3 | **[bug-005](projects/bugs/bug-005-moderator-bi-chan-403-khi-an-binh-luan-trang-doc/bug.md)** | `CommentServlet` văng lỗi 403 Forbidden khi `MODERATOR` bấm ẩn bình luận vi phạm trực tiếp trên trang đọc | 🟠 Nặng | `truyen/controller/user/CommentServlet.java` | ✅ Đã xong |
| 4 | **[ISSUE-026](projects/issues/ISSUE-026-dong-bo-modal-confirm-quicktopup-va-toi-uu-css/issue.md)** | Gom CSS modal vào `components.css`, dọn sạch ~430 dòng `<style>` trùng lặp trong 3 JSP modal, đồng bộ confirm huỷ liên kết Google | 🔵 Tối ưu | `_confirm-modal.jsp` · `_quick-topup-modal.jsp` · `_vip-unlock-modal.jsp` · `components.css` · `edit.jsp` | ✅ Đã xong |
| 5 | **[ISSUE-027](projects/issues/ISSUE-027-menu-sidebar-phan-quyen-dong-rbac/issue.md)** | Thích ứng động Menu Header và Sidebar Quản trị theo quyền RBAC; kích hoạt quyền `READ_VIP_FREE` | 🟢 Giao diện | `nav.jsp` · `admin.jsp` · `ChapterAccess.java` · `ChapterServlet.java` · `User.java` | ✅ Đã xong |
| 6 | **[bug-006](projects/bugs/bug-006-moderator-bi-chan-403-khi-xem-anh-bang-chung-bao-cao/bug.md)** | `UploadedFileServlet` chặn 403 khi `MODERATOR` có quyền `HANDLE_REPORTS` xem ảnh bằng chứng báo cáo vi phạm | 🔴 Chặn | `truyen/controller/common/UploadedFileServlet.java` | ✅ Đã xong |
| 7 | **[bug-007](projects/bugs/bug-007-lech-dong-bo-rbac-khi-doi-vai-tro-admin-user/bug.md)** | `AdminUserServlet` chỉ đổi `users.role` mà không đồng bộ bảng RBAC `user_roles` và thiếu ghi `AuditDAO` | 🔴 Chặn | `truyen/controller/admin/AdminUserServlet.java` · `AuditLog.java` · `audit.jsp` | ✅ Đã xong |
| 8 | **[ISSUE-028](projects/issues/ISSUE-028-tang-cuong-bao-mat-xac-nhan-thao-tac-va-html5-form-support/issue.md)** | Tăng cường tương tác: Modal xác nhận cho thao tác Nâng/Hạ/Khoá nick, Duyệt truyện, hỗ trợ HTML5 form và ghi log Report | 🟢 Giao diện | `_confirm-modal.jsp` · `users.jsp` · `stories.jsp` · `AdminReportServlet.java` | ✅ Đã xong |
| 9 | **[bug-008](projects/bugs/bug-008-chong-csrf-action-mark-read-notification/bug.md)** | Chặn CSRF: `action=mark_read` đổi CSDL trên GET không qua CsrfFilter; chuyển sang POST có bảo vệ CSRF | 🔴 Chặn | `NotificationServlet.java` · `nav.js` · `head.jsp` | ✅ Đã xong |
| 10 | **[ISSUE-029](projects/issues/ISSUE-029-toi-uu-tuong-tac-giao-dien-modal-confirm-va-bao-ve-thao-tac/issue.md)** | Tối ưu tương tác giao diện: Form validation trước khi hiện modal, phím Escape, chuẩn hoá modal ví Admin & data-confirm bảo vệ ví/dữ liệu | 🟢 Giao diện | `_confirm-modal.jsp` · `_quick-topup-modal.jsp` · `_vip-unlock-modal.jsp` · `wallet.jsp` · `me.jsp` · `history.jsp` · `_comment.jsp` · `comments.jsp` | ✅ Đã xong |
| 11 | **[bug-009](projects/bugs/bug-009-chong-logout-csrf-va-bao-ve-dang-xuat/bug.md)** | Chống Logout CSRF: Chặn huỷ phiên âm thầm qua `GET /auth?action=logout`, bảo vệ bằng POST CSRF và modal xác nhận đăng xuất | 🔴 Chặn | `AuthServlet.java` · `nav.jsp` · `403.jsp` | ✅ Đã xong |
| 12 | **[ISSUE-030](projects/issues/ISSUE-030-chuan-hoa-tip-modal-va-nang-cap-gamification-ux/issue.md)** | Chuẩn hoá Modal Tặng Xu (`_tip-modal.jsp`) sang `.app-modal-backdrop` & `.is-open`, phím Escape, bảo vệ chống mất xu oan; Tối ưu Gamification Điểm danh & Nhận thưởng qua AJAX với debounce và hiệu ứng ăn mừng trên `user/me.jsp` | 🟢 Giao diện | `_tip-modal.jsp` · `components.css` · `UserServlet.java` · `user/me.jsp` | ✅ Đã xong |
| 13 | **[ISSUE-031](projects/issues/ISSUE-031-ajax-vote-va-interactive-star-picker/issue.md)** | Bộ chọn 5 sao trực quan (Interactive Star Rating Picker) kèm hover animation & nhãn giải thích thời gian thực; Bình chọn đánh giá hữu ích không tải lại trang qua AJAX & Debounce gửi đánh giá | 🟢 Giao diện | `detail.jsp` · `_review.jsp` · `components.css` · `ReviewDAO.java` · `RatingServlet.java` | ✅ Đã xong |
| 14 | **[ISSUE-032](projects/issues/ISSUE-032-nang-cap-loc-nhay-chuong-va-debounce-comment/issue.md)** | Nâng cấp ô lọc chương: Nút xóa nhanh clear button, thông báo empty state khi không thấy chương, phím Enter cuộn tới và kích hoạt hiệu ứng pulse; Chống spam submit bình luận và phản hồi | 🟢 Giao diện | `detail.jsp` · `_comment.jsp` · `components.css` | ✅ Đã xong |
| 15 | **[ISSUE-033](projects/issues/ISSUE-033-nang-cap-ux-studio-tac-gia-va-tai-anh/issue.md)** | Nâng cấp toàn diện UX Studio Tác Giả: Kéo thả ảnh bìa (Drag & Drop), nút hủy chọn file ảnh, đảo ngược thứ tự trang truyện tranh, xóa ảnh mới, debounce submit & thông báo Flash toast | 🟢 Giao diện | `story/form.jsp` · `chapter/form-comic.jsp` · `chapter/form.jsp` · `StoryServlet.java` · `ChapterServlet.java` | ✅ Đã xong |

---

## 🛠️ Hướng dẫn chi tiết từng việc cho Phát

---

### 1. [bug-003] Sửa lỗ hổng phân quyền trong `AdminFilter.java`
- **File:** [`src/main/java/truyen/filter/AdminFilter.java`](../src/main/java/truyen/filter/AdminFilter.java)
- **Vấn đề:** Điều kiện `else if (!me.isAdmin() && !me.hasPermission("MANAGE_STORIES") ...)` bị sai với `EDITOR` vì Editor có quyền sửa truyện, làm cho Editor bypass vào được `/admin/wallet` và `/admin/user`.
- **Cách sửa:**
  1. Đổi annotation `@WebFilter("/admin/*")` thành `@WebFilter(urlPatterns = {"/admin", "/admin/*"})`.
  2. Thay đoạn kiểm tra `if (me.hasRole("MODERATOR") ...)` từ dòng 56–64 bằng:
  ```java
  if (!me.isAdmin()) {
      boolean isStaff = me.isModerator() || me.isEditor()
              || (me.getPermissions() != null && !me.getPermissions().isEmpty());

      if (!isStaff) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }

      if (path.startsWith("/admin/wallet") && !me.hasPermission("MANAGE_WALLET")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (path.startsWith("/admin/user") && !me.hasPermission("MANAGE_USERS")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (path.startsWith("/admin/audit") && !me.hasPermission("MANAGE_SYSTEM") && !me.hasPermission("MANAGE_WALLET")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (path.startsWith("/admin/tag") && !me.hasPermission("MANAGE_STORIES")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (path.startsWith("/admin/story") && !me.hasPermission("MANAGE_STORIES")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (path.startsWith("/admin/comment") && !me.hasPermission("MODERATE_COMMENTS")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (path.startsWith("/admin/report") && !me.hasPermission("HANDLE_REPORTS")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      if (!path.equals("/admin") && !path.equals("/admin/") && !path.startsWith("/admin/dashboard")) {
          response.sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
  }
  ```

---

### 2. [bug-004] Ngăn chặn mất vai trò & quyền RBAC trong `UserServlet.java`
- **File:** [`src/main/java/truyen/controller/user/UserServlet.java`](../src/main/java/truyen/controller/user/UserServlet.java)
- **Vấn đề:** Khi user lưu hồ sơ (`doPost`) hoặc kích hoạt gói VIP (`handleUpgradeVip`), đối tượng `fresh` mới lấy từ DB chưa được nạp roles/permissions mà đã gán thẳng vào `session.setAttribute("currentUser", fresh)`.
- **Cách sửa:**
  1. Thêm import: `import truyen.dao.RbacDAO;`
  2. Tại dòng ~257:
     ```java
     fresh.setReadChapters(userDAO.countReadChapters(fresh.getId()));
     new RbacDAO().populateRbac(fresh); // <-- THÊM DÒNG NÀY
     request.getSession().setAttribute("currentUser", fresh);
     ```
  3. Tại dòng ~346:
     ```java
     fresh.setReadChapters(userDAO.countReadChapters(fresh.getId()));
     new RbacDAO().populateRbac(fresh); // <-- THÊM DÒNG NÀY
     request.getSession().setAttribute("currentUser", fresh);
     ```

---

### 3. [bug-005] Cho phép MODERATOR ẩn bình luận trên trang đọc trong `CommentServlet.java`
- **File:** [`src/main/java/truyen/controller/user/CommentServlet.java`](../src/main/java/truyen/controller/user/CommentServlet.java)
- **Vấn đề:** Dòng 232 chỉ cho phép tác giả bình luận hoặc Admin, làm Moderator bị lỗi 403 khi ấn nút ẩn bình luận rác ngoài giao diện đọc truyện.
- **Cách sửa:** Sửa dòng 232 thành:
  ```java
  if (me == null || (me.getId() != c.getUserId() && !me.isAdmin() && !me.hasPermission("MODERATE_COMMENTS"))) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
  }
  ```

---

### 4. [ISSUE-026] Gom CSS Modal & Dọn dẹp ~430 dòng trùng lặp trong JSP
- **Các file:**
  - [`src/main/webapp/WEB-INF/views/_partials/_confirm-modal.jsp`](../src/main/webapp/WEB-INF/views/_partials/_confirm-modal.jsp)
  - [`src/main/webapp/WEB-INF/views/_partials/_quick-topup-modal.jsp`](../src/main/webapp/WEB-INF/views/_partials/_quick-topup-modal.jsp)
  - [`src/main/webapp/WEB-INF/views/_partials/_vip-unlock-modal.jsp`](../src/main/webapp/WEB-INF/views/_partials/_vip-unlock-modal.jsp)
  - [`src/main/webapp/assets/css/components.css`](../src/main/webapp/assets/css/components.css)
  - [`src/main/webapp/WEB-INF/views/user/edit.jsp`](../src/main/webapp/WEB-INF/views/user/edit.jsp)
- **Cách sửa:**
  1. Trong `components.css`: Đảm bảo các thuộc tính backdrop và open card của modal hoạt động độc lập không cần thẻ `<style>` inline.
  2. Trong 3 file JSP modal: Xoá sạch toàn bộ khối `<style> ... </style>` ở cuối file.
  3. Trong `_confirm-modal.jsp`: Sửa đoạn JS kiểm tra từ khoá thành không phân biệt hoa thường (`toLowerCase()`), hỗ trợ cả dạng có dấu `xóa`, `xoá`, `gỡ`, `hủy`, `khoá`.
  4. Trong `edit.jsp:133`: Đổi `onsubmit="return confirm('Bạn có chắc chắn muốn hủy liên kết tài khoản Google?');"` thành:
     ```html
     data-confirm="Bạn có chắc chắn muốn hủy liên kết tài khoản Google?"
     data-confirm-title="Xác nhận hủy liên kết"
     data-confirm-ok="Đồng ý hủy"
     ```

---

### 5. [ISSUE-027] Thích ứng động Menu Header, Sidebar Admin & Đặc quyền VIP
- **Các file:**
  - [`src/main/webapp/WEB-INF/views/layout/parts/nav.jsp`](../src/main/webapp/WEB-INF/views/layout/parts/nav.jsp)
  - [`src/main/webapp/WEB-INF/views/layout/admin.jsp`](../src/main/webapp/WEB-INF/views/layout/admin.jsp)
  - [`src/main/java/truyen/util/ChapterAccess.java`](../src/main/java/truyen/util/ChapterAccess.java)
  - [`src/main/java/truyen/controller/common/ChapterServlet.java`](../src/main/java/truyen/controller/common/ChapterServlet.java)
  - [`src/main/java/truyen/model/User.java`](../src/main/java/truyen/model/User.java)
- **Cách sửa:**
  1. `nav.jsp:183`: Đổi `<c:if test="${currentUser.admin}">` thành `<c:if test="${currentUser.admin or currentUser.moderator or currentUser.editor}">`.
  2. `admin.jsp:31-47`: Bọc từng link trong sidebar theo quyền:
     - Thể loại & Truyện: `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_STORIES')}">`
     - Bình luận: `<c:if test="${currentUser.admin or currentUser.hasPermission('MODERATE_COMMENTS')}">`
     - Báo cáo: `<c:if test="${currentUser.admin or currentUser.hasPermission('HANDLE_REPORTS')}">`
     - Tài khoản: `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_USERS')}">`
     - Ví xu: `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_WALLET')}">`
     - Nhật ký: `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_SYSTEM') or currentUser.hasPermission('MANAGE_WALLET')}">`
  3. `ChapterAccess.java`: Thêm kiểm tra `READ_VIP_FREE`:
     ```java
     if (canEdit(me, story) || me.hasPermission("READ_VIP_FREE")) return false;
     ```
  4. `ChapterServlet.java:306`: Thay `"ADMIN".equals(me.getRole())` bằng `me.isAdmin() || me.hasPermission("READ_VIP_FREE")`.
  5. `User.java`: Xoá dòng javadoc mồ côi dòng 150 `/** Chữ cái đầu, dùng làm avatar thay thế khi chưa có ảnh. */` và chuyển các import `List, ArrayList, Set, HashSet` lên đầu file.

---

### 6. [bug-006] Sửa lỗ hổng xem ảnh bằng chứng tố cáo của Moderator trong `UploadedFileServlet.java`
- **File:** [`src/main/java/truyen/controller/common/UploadedFileServlet.java`](../src/main/java/truyen/controller/common/UploadedFileServlet.java)
- **Vấn đề:** Kiểm tra cứng `!me.isAdmin()` khiến `MODERATOR` có quyền `HANDLE_REPORTS` bị dính lỗi 403 Forbidden khi xem ảnh bằng chứng báo cáo vi phạm.
- **Cách sửa:** Đổi điều kiện dòng 89 thành:
  ```java
  if (me == null || (!me.isAdmin() && !me.hasPermission("HANDLE_REPORTS"))) {
      response.sendError(HttpServletResponse.SC_FORBIDDEN);
      return;
  }
  ```

---

### 7. [bug-007] Sửa lệch đồng bộ vai trò RBAC & bổ sung Audit Log trong `AdminUserServlet.java`
- **File:** [`src/main/java/truyen/controller/admin/AdminUserServlet.java`](../src/main/java/truyen/controller/admin/AdminUserServlet.java) · [`AuditLog.java`](../src/main/java/truyen/model/AuditLog.java) · [`audit.jsp`](../src/main/webapp/WEB-INF/views/admin/audit.jsp)
- **Vấn đề:** Nâng/Hạ quyền chỉ cập nhật `users.role` mà không đồng bộ bảng RBAC `user_roles`, đồng thời không ghi nhật ký kiểm toán.
- **Cách sửa:**
  1. Đồng bộ `RbacDAO.assignRole(id, "ADMIN")` / `RbacDAO.removeRole(id, "ADMIN")`.
  2. Ghi `AuditDAO.log` với action `CHANGE_ROLE`.
  3. Bổ sung nhãn `"Đổi vai trò"` trên giao diện `audit.jsp` và `AuditLog.java`.

---

### 8. [ISSUE-028] Tăng cường bảo mật tương tác giao diện: Modal xác nhận thao tác & hỗ trợ form HTML5
- **File:** [`_confirm-modal.jsp`](../src/main/webapp/WEB-INF/views/_partials/_confirm-modal.jsp) · [`users.jsp`](../src/main/webapp/WEB-INF/views/admin/users.jsp) · [`stories.jsp`](../src/main/webapp/WEB-INF/views/admin/stories.jsp) · [`AdminReportServlet.java`](../src/main/java/truyen/controller/admin/AdminReportServlet.java)
- **Cách sửa:**
  1. Nâng cấp `_confirm-modal.jsp`: Hỗ trợ thuộc tính HTML5 `form="..."` cho các nút bấm nằm lồng trong form khác để không submit nhầm form cha; nhận diện từ khoá tiếng Việt `hạ`, `nâng`, `khoá`, `xoá`, `gỡ`, `hủy`, `duyệt`.
  2. Thêm `data-confirm` vào nút Nâng/Hạ vai trò, nút Khoá nick, nút Bỏ khoá trong `admin/users.jsp`.
  3. Thêm `data-confirm` vào nút Duyệt đăng truyện trong `admin/stories.jsp`.
  4. Bổ sung ghi log `AuditDAO` trong `AdminReportServlet` khi gỡ truyện vi phạm hoặc ẩn comment vi phạm.

---

### 9. [bug-008] Chống CSRF Thao Tác Đánh Dấu Đã Đọc Thông Báo
- **File:** [`src/main/java/truyen/controller/user/NotificationServlet.java`](../src/main/java/truyen/controller/user/NotificationServlet.java) · [`nav.js`](../src/main/webapp/assets/js/nav.js) · [`head.jsp`](../src/main/webapp/WEB-INF/views/layout/parts/head.jsp)
- **Vấn đề:** `action=mark_read` thay đổi dữ liệu trong CSDL nhưng nằm trong `doGet()`, không được `CsrfFilter` bảo vệ (do CsrfFilter chỉ lọc POST). Kẻ xấu có thể nhúng `<img src=".../notification?action=mark_read">` để làm nạn nhân bị xoá thông báo âm thầm.
- **Cách sửa:**
  1. Trong `NotificationServlet.java`:
     - Chặn GET với `action=mark_read` bằng `response.sendError(405)`.
     - Cài đặt `doPost()` kiểm tra phiên và xử lý `action=mark_read` với sự bảo vệ của `CsrfFilter`.
  2. Trong `head.jsp`: Thêm `<meta name="csrf-token" content="${csrfToken}">`.
  3. Trong `nav.js`: Chuyển lời gọi `fetch` của nút chuông sang `POST` có header `X-CSRF-Token` và body `_csrf=...`.

---

### 10. [ISSUE-029] Tối Ưu Tương Tác Giao Diện, Modal Xác Nhận & Bảo Vệ Thao Tác
- **File:** `_confirm-modal.jsp` · `_quick-topup-modal.jsp` · `_vip-unlock-modal.jsp` · `wallet.jsp` · `me.jsp` · `history.jsp` · `_comment.jsp` · `comments.jsp`
- **Vấn đề & Cách sửa:**
  1. Trong `_confirm-modal.jsp`:
     - Kiểm tra `form.checkValidity()` trước khi kích hoạt `showConfirmModal()`. Nếu các ô bắt buộc chưa điền, gọi `form.reportValidity()` để người dùng điền đầy đủ trước khi hỏi xác nhận.
     - Lắng nghe phím `Escape` để đóng modal xác nhận khi người dùng bấm Esc.
  2. Trong `_quick-topup-modal.jsp` và `_vip-unlock-modal.jsp`: Thêm sự kiện phím `Escape` để đóng modal.
  3. Trong `admin/wallet.jsp`: Chuẩn hoá `adjustModal` sang `.app-modal-backdrop` và `.app-modal-card`, thêm nút đóng `&times;`, cho phép click backdrop đóng modal, hỗ trợ phím `Escape`, và thêm `data-confirm` bảo vệ nút submit.
  4. Trong `user/me.jsp`: Thêm `data-confirm` vào nút nâng cấp VIP tránh trừ xu nhầm.
  5. Trong `user/history.jsp`: Thêm `data-confirm` vào nút xoá toàn bộ lịch sử đọc.
  6. Trong `_comment.jsp`: Đồng bộ điều kiện hiển thị nút gỡ bình luận cho cả `MODERATOR` có quyền `MODERATE_COMMENTS`, thêm `data-confirm` khi gỡ bình luận và câu trả lời.
  7. Trong `admin/comments.jsp`: Thêm `data-confirm` cho nút ẩn/bỏ ẩn bình luận.

---

### 11. [bug-009] Chống Logout CSRF & Bảo Vệ Đăng Xuất An Toàn
- **File:** [`src/main/java/truyen/controller/common/AuthServlet.java`](../src/main/java/truyen/controller/common/AuthServlet.java) · [`nav.jsp`](../src/main/webapp/WEB-INF/views/layout/parts/nav.jsp) · [`403.jsp`](../src/main/webapp/WEB-INF/views/error/403.jsp)
- **Vấn đề:** `action=logout` trước đây được gọi qua GET không kiểm tra CSRF token. Bất kỳ trang web bên ngoài nào nhúng `<img src="http://localhost:8080/auth?action=logout">` đều có thể kích hoạt `session.invalidate()` làm nạn nhân bị đăng xuất âm thầm mà không hề biết.
- **Cách sửa:**
  1. Trong `AuthServlet.java`:
     - Kiểm tra `if (!"POST".equalsIgnoreCase(request.getMethod()))`: từ chối huỷ phiên và chuyển hướng an toàn về `/`.
     - Chỉ thực hiện `session.invalidate()` khi request là POST mang token CSRF hợp lệ (đã được `CsrfFilter` xác thực).
  2. Trong `nav.jsp`:
     - Tạo form ẩn `#navLogoutForm` phương thức POST mang `_csrf`.
     - Tích hợp `data-confirm="Bạn có chắc chắn muốn đăng xuất khỏi tài khoản?"` và sự kiện `onclick` gọi modal xác nhận hiện đại trước khi submit.
  3. Trong `403.jsp`: Chuyển nút Đổi tài khoản sang submit form POST với `_csrf`.

---

### 12. [ISSUE-030] Chuẩn Hoá Modal Tặng Xu & Nâng Tầm Gamification UX
- **File:** [`_tip-modal.jsp`](../src/main/webapp/WEB-INF/views/_partials/_tip-modal.jsp) · [`components.css`](../src/main/webapp/assets/css/components.css) · [`UserServlet.java`](../src/main/java/truyen/controller/user/UserServlet.java) · [`user/me.jsp`](../src/main/webapp/WEB-INF/views/user/me.jsp)
- **Vấn đề & Cách sửa:**
  1. Trong `components.css`:
     - Thêm `#tipModalBackdrop` và `.tip-modal-card` vào các rule chuẩn của `.app-modal-backdrop` và `.app-modal-card`.
     - Xoá các style inline can thiệp DOM cũ (`display: flex`, animation cũ).
  2. Trong `_tip-modal.jsp`:
     - Sử dụng class chuẩn `.app-modal-backdrop` và `.is-open`.
     - Lắng nghe phím `Escape` để đóng modal thuận tiện.
     - Tích hợp bước xác nhận an toàn (`window.showConfirmModal` / `confirm`) trước khi trừ xu trong ví, ngăn chặn hoàn toàn việc mất xu oan do click nhầm.
     - Cập nhật số dư xu trên navbar và bắn hiệu ứng pháo hoa `window.launchCelebration('coins')` khi tặng xu thành công.
  3. Trong `UserServlet.java`:
     - Trả về payload JSON phong phú hơn cho `checkin` và `claim-quest`: `success`, `reward`, `message`, `newBalance`.
  4. Trong `user/me.jsp`:
     - Thêm xử lý AJAX & debounce cho nút Điểm danh hàng ngày (`#form-daily-checkin`) và các nút Nhận thưởng nhiệm vụ (`.form-claim-quest`).
     - Khi bấm, nút lập tức chuyển sang trạng thái `⏳ Đang xử lý...` và disable để chống duplicate click / race condition.
     - Khi thành công, kích hoạt hiệu ứng pháo hoa xu, cập nhật số dư trên thanh header và chuyển trạng thái "Đã nhận" tức thời mà không cần tải lại toàn bộ trang.

---

### 13. [ISSUE-031] Bộ Chọn Sao Trực Quan & Bình Chọn Đánh Giá Hữu Ích Không Tải Lại Trang
- **File:** [`story/detail.jsp`](../src/main/webapp/WEB-INF/views/common/story/detail.jsp) · [`_review.jsp`](../src/main/webapp/WEB-INF/views/_partials/_review.jsp) · [`components.css`](../src/main/webapp/assets/css/components.css) · [`ReviewDAO.java`](../src/main/java/truyen/dao/ReviewDAO.java) · [`RatingServlet.java`](../src/main/java/truyen/controller/user/RatingServlet.java)
- **Vấn đề & Cách sửa:**
  1. Trong `detail.jsp`:
     - Thay thế thẻ `<select name="score">` bằng bộ chọn 5 sao trực quan `#interactive-star-picker` (`.star-item`) kèm nhãn mô tả cảm xúc thời gian thực (`#star-label-text`).
     - Tích hợp sự kiện rê chuột hover phóng to `scale(1.22)` phát sáng amber, cập nhật nhãn động (Tuyệt tác, Rất hay, Khá ổn, Tạm được, Cần cải thiện) và trả về điểm đã chọn khi chuột rời đi.
     - Thêm debounce cho form soạn đánh giá `#story-review-form` (`⏳ Đang lưu đánh giá...`).
     - Đăng ký listener AJAX submit cho `.review-vote-form`, hoán đổi class nút `.btn-primary` <-> `.btn-ghost`, cập nhật số lượt bình chọn và bắn toast mà không cần reload trang.
  2. Trong `_review.jsp`: Bổ sung thuộc tính `data-voted` và thẻ `<span class="helpful-count">`.
  3. Trong `ReviewDAO.java`: Thêm phương thức `getHelpfulCount(int reviewId)`.
  4. Trong `RatingServlet.java`: Trả về JSON `{ "success": true, "voted": ..., "count": ... }` khi phát hiện `X-Requested-With: XMLHttpRequest`, xử lý 401 khi chưa đăng nhập.

---

### 14. [ISSUE-032] Nâng Cấp Lọc & Nhảy Chương Nhanh Cùng Chống Gửi Lặp Bình Luận
- **File:** [`story/detail.jsp`](../src/main/webapp/WEB-INF/views/common/story/detail.jsp) · [`_comment.jsp`](../src/main/webapp/WEB-INF/views/_partials/_comment.jsp) · [`components.css`](../src/main/webapp/assets/css/components.css)
- **Vấn đề & Cách sửa:**
  1. Trong `detail.jsp`:
     - Thêm nút xóa nhanh `#chapter-filter-clear` (`&times;`) trong ô tìm kiếm chương, tự động ẩn/hiện theo nội dung nhập, một chạm xóa trắng và khôi phục danh sách.
     - Thêm khối `#chapter-filter-empty` thông báo trạng thái trống khi không tìm thấy chương nào khớp từ khóa.
     - Lắng nghe sự kiện phím `Enter` trên ô lọc: tự động cuộn mượt tới chương đầu tiên khớp (`scrollIntoView`), kích hoạt animation viền hổ phách nhấp nháy `.chapter-pulse` và thông báo toast định vị.
  2. Trong `_comment.jsp`:
     - Đăng ký bộ xử lý chống spam gửi lặp toàn cục `window.__commentSubmitDebounceSetup` cho tất cả form bình luận `.comment-form` và trả lời `.reply-form`.
     - Vô hiệu hoá tức thời nút gửi kèm nhãn `⏳ Đang gửi...` và khôi phục an toàn sau 5s.

---

### 15. [ISSUE-033] Nâng Cấp Toàn Diện UX Studio Tác Giả & Tải Ảnh Bìa, Trang Truyện Tranh
- **File:** [`story/form.jsp`](../src/main/webapp/WEB-INF/views/user/story/form.jsp) · [`chapter/form-comic.jsp`](../src/main/webapp/WEB-INF/views/user/chapter/form-comic.jsp) · [`chapter/form.jsp`](../src/main/webapp/WEB-INF/views/user/chapter/form.jsp) · [`StoryServlet.java`](../src/main/java/truyen/controller/common/StoryServlet.java) · [`ChapterServlet.java`](../src/main/java/truyen/controller/common/ChapterServlet.java)
- **Vấn đề & Cách sửa:**
  1. Trong `story/form.jsp`:
     - Tích hợp vùng kéo thả Drag & Drop trực tiếp vào ô xem trước bìa `#cover-preview-box` với overlay `#cover-drop-overlay` (`📥 Thả ảnh vào đây`).
     - Bổ sung nút "✕ Hủy chọn file" (`#btnRemoveCoverFile`) để reset nhanh file vừa chọn nhầm và khôi phục ảnh cũ.
     - Chuyển toàn bộ các popup `alert()` kiểm tra dung lượng ảnh (> 2 MB) hoặc tiêu đề/mô tả sang thông báo Toast Design System.
     - Bổ sung submit debounce cho form đăng/sửa truyện `#storyForm` (`⏳ Đang lưu truyện...`).
  2. Trong `chapter/form-comic.jsp`:
     - Bổ sung nút "🔄 Đảo ngược thứ tự" (`#btnReversePages`) cho phép đảo ngược toàn bộ thứ tự trang ảnh từ dưới lên trên và tự động đánh lại số trang.
     - Bổ sung nút "🗑️ Xóa ảnh mới" (`#btnClearNewPages`) dọn sạch hàng loạt trang ảnh mới tải lên nếu chọn nhầm thư mục.
     - Chuyển `alert()` sang Toast và tích hợp submit debounce cho `#btnSaveComic` (`⏳ Đang tải ảnh lên…`).
  3. Trong `chapter/form.jsp`:
     - Chuyển toàn bộ `alert()` sang Toast.
     - Bổ sung submit debounce cho form soạn chương chữ (`⏳ Đang lưu chương...`).
  4. Trong `StoryServlet.java` & `ChapterServlet.java`:
     - Bổ sung thông báo Flash toast trong session sau khi chuyển hướng: Tạo truyện mới, Sửa truyện, Gỡ truyện, Xuất bản chương mới, Cập nhật chương và Xoá chương.

---

### 16. Dọn dẹp rác & Git
1. Xoá file dump crash JVM: `hs_err_pid28388.log` ở thư mục gốc.
2. Thêm vào `.gitignore`:
   ```gitignore
   *.log
   hs_err_pid*
   ```

---

## 🧪 Kiểm tra sau khi hoàn thành

Sau khi Phát sửa xong tất cả các mục trên, mở PowerShell và chạy script test tự động:
```powershell
powershell -ExecutionPolicy Bypass -File scripts\test.ps1
python scripts\verify_full_system.py
```
Đảm bảo kết quả in ra:
- Unit tests: **`181 tests successful [0 failed]`**
- Integration tests: **`51/51 bài kiểm tra PASS (100%)`**



