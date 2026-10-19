# 🐞 bug-004: Mất sạch Roles & Permissions RBAC trong phiên khi cập nhật hồ sơ hoặc nâng cấp VIP

> **Đích:** `docs/projects/bugs/bug-004-mat-quyen-rbac-khi-cap-nhat-ho-so-hoac-vip/bug.md`
> **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | bug-004 |
| **Mức** | 🔴 Chặn (Mất toàn bộ quyền hạn của nhân sự/VIP trong phiên) |
| **Feature liên quan** | RBAC Setup · `UserServlet` |
| **Người sửa** | Phát |
| **Trạng thái** | 🛠️ Đang làm |
| **Ngày phát hiện** | 2026-10-18 |
| **Nơi xảy ra** | `src/main/java/truyen/controller/user/UserServlet.java:259, 346` |

---

## 1. Tái hiện thế nào

1. Đăng nhập bằng tài khoản `mod1` hoặc `editor1` (hoặc tài khoản có role RBAC).
2. Kiểm tra thấy tài khoản có quyền truy cập khu vực `/admin/dashboard` và kiểm duyệt bình luận.
3. Người dùng vào trang "Cài đặt & Sửa hồ sơ" (`/user?action=edit`) và bấm "Lưu thay đổi" (hoặc vào mục nâng cấp VIP rồi kích hoạt gói VIP).
4. Quay lại trang quản trị `/admin/comment` hoặc bấm vào một chức năng kiểm duyệt.

**Thấy:** Bị văng lỗi `403 Forbidden` ngay lập tức! Kiểm tra `currentUser.getRoles()` và `currentUser.getPermissions()` thấy rỗng (`[]`).
**Đáng ra phải:** Roles và Permissions vẫn được giữ nguyên đầy đủ sau khi cập nhật hồ sơ hay gia hạn VIP.

---

## 2. Chứng cứ

Trong `UserServlet.java`:
```java
// Dòng 253-259
User fresh = userDAO.findById(me.getId());
...
fresh.setPasswordHash(null);
fresh.setStoryCount(storyDAO.countPublishedByAuthor(fresh.getId()));
fresh.setFollowerCount(followDAO.countFollowers(fresh.getId()));
fresh.setReadChapters(userDAO.countReadChapters(fresh.getId()));

// Cập nhật lại session
request.getSession().setAttribute("currentUser", fresh); // <-- fresh CHƯA CÓ RBAC!
```
Tương tự tại dòng 342–346 khi nâng cấp VIP:
```java
User fresh = userDAO.findById(me.getId());
if (fresh != null) {
    fresh.setPasswordHash(null);
    fresh.setReadChapters(userDAO.countReadChapters(fresh.getId()));
    request.getSession().setAttribute("currentUser", fresh); // <-- fresh CHƯA CÓ RBAC!
}
```

---

## 3. Nguyên nhân

- **Sai ở:** `UserServlet.java:258-260` và `UserServlet.java:345-347`.
- **Vì:** `new truyen.dao.RbacDAO().populateRbac(user)` chỉ được gọi ở `AuthServlet` khi đăng nhập lần đầu. Khi `UserServlet` truy vấn lại từ DB qua `userDAO.findById(...)`, đối tượng `fresh` mới tinh chỉ có các thuộc tính bảng `users`, các bảng phụ `user_roles` và `role_permissions` chưa được nạp lại trước khi gán đè vào session.

---

## 4. Sửa thế nào (Hướng dẫn cho Phát)

Trong [`UserServlet.java`](../../../../src/main/java/truyen/controller/user/UserServlet.java):
1. Thêm import: `import truyen.dao.RbacDAO;`
2. Tại dòng 257 (trước khi lưu vào session):
   ```java
   fresh.setReadChapters(userDAO.countReadChapters(fresh.getId()));
   new RbacDAO().populateRbac(fresh); // Nạp lại Roles & Permissions
   request.getSession().setAttribute("currentUser", fresh);
   ```
3. Tại dòng 346 (khi nâng cấp VIP):
   ```java
   fresh.setReadChapters(userDAO.countReadChapters(fresh.getId()));
   new RbacDAO().populateRbac(fresh); // Nạp lại Roles & Permissions
   request.getSession().setAttribute("currentUser", fresh);
   ```

---

## 5. Kiểm lại

- [ ] Đăng nhập `mod1`, vào trang cá nhân sửa bio/tên hiển thị, bấm Lưu.
- [ ] Truy cập `/admin/comment` -> Vẫn hoạt động bình thường, không bị mất quyền.
- [ ] Chạy `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` -> Pass.
