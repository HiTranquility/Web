# 🐞 bug-005: MODERATOR bị văng lỗi 403 Forbidden khi ẩn/xoá bình luận vi phạm trực tiếp trên trang đọc

> **Đích:** `docs/projects/bugs/bug-005-moderator-bi-chan-403-khi-an-binh-luan-trang-doc/bug.md`
> **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | bug-005 |
| **Mức** | 🟠 Nặng (Kiểm duyệt viên không thực hiện được nhiệm vụ) |
| **Feature liên quan** | ISSUE-004 · RBAC Setup |
| **Người sửa** | Phát |
| **Trạng thái** | 🛠️ Đang làm |
| **Ngày phát hiện** | 2026-10-18 |
| **Nơi xảy ra** | `src/main/java/truyen/controller/user/CommentServlet.java:232` |

---

## 1. Tái hiện thế nào

1. Đăng nhập bằng tài khoản kiểm duyệt viên `mod1` (mật khẩu `123456`).
2. Vào đọc một chương truyện bất kỳ có bình luận thô tục/spam của người dùng khác (ví dụ của `haiduong`).
3. Dưới bình luận thô tục, bấm nút "Xoá / Ẩn" để dọn rác.
4. Trình duyệt gửi request POST `/comment?action=delete&id=...`.

**Thấy:** Máy chủ trả về lỗi `403 Forbidden`! Bình luận vi phạm vẫn còn nguyên.
**Đáng ra phải:** `CommentServlet` cho phép `mod1` thực hiện xoá mềm (`commentDAO.hide(id)`) và chuyển hướng mượt mà về lại danh sách bình luận kèm anchor `#comments`.

---

## 2. Chứng cứ

Mã nguồn trong `CommentServlet.java` dòng 232–235:
```java
if (me == null || (me.getId() != c.getUserId() && !me.isAdmin())) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```
Khi `mod1` thao tác:
- `me.getId() != c.getUserId()` -> `true` (không phải bình luận do mod tự viết).
- `!me.isAdmin()` -> `true` (mod1 không phải SUPER_ADMIN hay ADMIN).
- Cả hai điều kiện thoả mãn khiến server ném ngay `SC_FORBIDDEN`!

---

## 3. Nguyên nhân

- **Sai ở:** `CommentServlet.java:232`
- **Vì:** Chưa tích hợp quyền `MODERATE_COMMENTS` vào điều kiện cho phép xoá mềm bình luận.

---

## 4. Sửa thế nào (Hướng dẫn cho Phát)

Trong [`CommentServlet.java`](../../../../src/main/java/truyen/controller/user/CommentServlet.java):
Sửa dòng 232 thành:
```java
if (me == null || (me.getId() != c.getUserId() && !me.isAdmin() && !me.hasPermission("MODERATE_COMMENTS"))) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

---

## 5. Kiểm lại

- [ ] Đăng nhập `mod1`, vào trang đọc bấm ẩn bình luận của độc giả khác -> Bình luận được ẩn thành công.
- [ ] Đăng nhập tài khoản độc giả thường `haiduong`, thử xoá bình luận của `mocmien` -> Bị chặn 403 (bảo vệ quyền chính chủ).
- [ ] Chạy `scripts\test.ps1` -> 100% test pass.
