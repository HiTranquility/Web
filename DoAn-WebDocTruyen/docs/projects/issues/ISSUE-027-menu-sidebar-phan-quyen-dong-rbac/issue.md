# 🏷️ ISSUE-027: Thích ứng động Menu Header và Sidebar Quản trị theo quyền RBAC

> **Đích:** `docs/projects/issues/ISSUE-027-menu-sidebar-phan-quyen-dong-rbac/issue.md`
> **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-027 |
| **Người làm** | Phát |
| **Trạng thái** | ✅ Đã xong |
| **Ngày mở** | 2026-10-18 |
| **CASE liên quan** | CASE 10 · ISSUE-023 · RBAC Setup |
| **Đụng vào** | `views/layout/parts/nav.jsp` · `views/layout/admin.jsp` · `model/User.java` · `controller/common/ChapterServlet.java` · `util/ChapterAccess.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

- **Hiện tại:** 
  1. Trong [`nav.jsp:183`](../../../../src/main/webapp/WEB-INF/views/layout/parts/nav.jsp#L183), menu dropdown người dùng chỉ kiểm tra `<c:if test="${currentUser.admin}">`. Nhân sự có role `MODERATOR` hoặc `EDITOR` sau khi đăng nhập không hề thấy liên kết "Khu vực Quản trị / Kiểm duyệt", phải tự gõ tay URL `/admin/dashboard`.
  2. Trong [`admin.jsp:31-47`](../../../../src/main/webapp/WEB-INF/views/layout/admin.jsp#L31-L47), sidebar quản trị hiển thị cố định tất cả 8 mục cho mọi người (kể cả những mục mà Moderator bị cấm như "Tài khoản", "Ví xu", "Nhật ký"). Khi họ bấm vào các mục này, hệ thống ném mã lỗi 403 cụt lủn.
  3. Quyền đặc biệt `READ_VIP_FREE` (Hội viên VIP đọc miễn phí chương VIP) đã có trong DB nhưng chưa được kiểm tra trong [`ChapterAccess.java`](../../../../src/main/java/truyen/util/ChapterAccess.java) và [`ChapterServlet.java:306`](../../../../src/main/java/truyen/controller/common/ChapterServlet.java#L306).
- **Sau khi xong:**
  1. Moderator và Editor thấy mục "Khu vực Quản trị" ngay trong menu header.
  2. Sidebar `admin.jsp` chỉ hiển thị các mục tương ứng với quyền của người đang đăng nhập.
  3. Tích hợp trọn vẹn đặc quyền đọc VIP miễn phí `READ_VIP_FREE`.

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] Đăng nhập `mod1` -> Thấy link "Khu vực Quản trị" trong menu header.
- [x] Vào `/admin/dashboard` với `mod1` -> Sidebar chỉ hiện các mục: *Tổng quan, Truyện, Bình luận, Báo cáo*. Các mục *Ví xu, Tài khoản, Nhật ký* bị ẩn tự động.
- [x] Đăng nhập `editor1` -> Sidebar chỉ hiện các mục: *Tổng quan, Truyện, Thể loại*.
- [x] Đăng nhập `admin` -> Hiện toàn bộ 8 mục như cũ.
- [x] Tài khoản có quyền `READ_VIP_FREE` đọc thẳng chương VIP mà không bị hiện hàng rào khoá xu.

---

## 3. Các bước triển khai (Hướng dẫn cho Phát)

### Bước 1: Sửa điều kiện menu trong `nav.jsp`
Đổi dòng 183 từ:
```jsp
<c:if test="${currentUser.admin}">
```
Thành:
```jsp
<c:if test="${currentUser.admin or currentUser.moderator or currentUser.editor}">
```

### Bước 2: Ẩn/hiện động các mục sidebar trong `admin.jsp`
Bọc từng thẻ `<a>` trong sidebar với quyền tương ứng:
- **Truyện & Thể loại:** `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_STORIES')}">`
- **Tài khoản:** `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_USERS')}">`
- **Bình luận:** `<c:if test="${currentUser.admin or currentUser.hasPermission('MODERATE_COMMENTS')}">`
- **Báo cáo:** `<c:if test="${currentUser.admin or currentUser.hasPermission('HANDLE_REPORTS')}">`
- **Ví xu:** `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_WALLET')}">`
- **Nhật ký:** `<c:if test="${currentUser.admin or currentUser.hasPermission('MANAGE_SYSTEM') or currentUser.hasPermission('MANAGE_WALLET')}">`

### Bước 3: Hoàn thiện `ChapterAccess.java` và `ChapterServlet.java`
- Trong `ChapterAccess.isLocked`: Bổ sung `if (canEdit(me, story) || me.hasPermission("READ_VIP_FREE")) return false;`
- Trong `ChapterServlet:306`: Thay `"ADMIN".equals(me.getRole())` bằng `me.isAdmin() || me.hasPermission("READ_VIP_FREE")`.
- Dọn dẹp dòng javadoc thừa tại dòng 150 trong `User.java`.

---

## 4. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** `git checkout -- src/main/webapp/WEB-INF/views/layout/ src/main/java/truyen/`
- **Dữ liệu / CSDL:** Không đụng CSDL.
