# [bug-007] Lệch Đồng Bộ Phân Quyền RBAC Khi Thay Đổi Vai Trò ADMIN/USER

- **Người thực hiện:** **Phát**
- **Ngày phát hiện:** 2026-10-18
- **Mức độ:** 🔴 Nghiêm trọng (Privilege Escalation / RBAC Desync)
- **File ảnh hưởng:** [`src/main/java/truyen/controller/admin/AdminUserServlet.java`](../../../../src/main/java/truyen/controller/admin/AdminUserServlet.java) · [`src/main/java/truyen/model/AuditLog.java`](../../../../src/main/java/truyen/model/AuditLog.java)

---

## 1. Hiện tượng & Nguyên nhân

### Hiện tượng
Khi Quản trị viên sử dụng chức năng "Hạ/Nâng" vai trò của người dùng trên trang `/admin/user` (`action=role`):
- Khi nâng một User thường lên `ADMIN`, người dùng đó chỉ được cập nhật cột `users.role = 'ADMIN'`, nhưng bảng phân quyền đa vai trò RBAC `user_roles` hoàn toàn không được cấp quyền `ADMIN`.
- Ngược lại, khi hạ quyền một `ADMIN` xuống `USER`, bản ghi trong `user_roles` vẫn còn giữ role `ADMIN` (hoặc các permission của admin). Khi người dùng đăng nhập lại hoặc phiên làm mới, họ vẫn giữ quyền quản trị trong RBAC!
- Đồng thời thao tác đổi vai trò tài khoản hoàn toàn không được ghi nhận vào nhật ký kiểm toán `audit_logs` (`AuditDAO`), tạo kẽ hở cho việc âm thầm thăng/hạ quyền mà không ai truy vết được.

### Nguyên nhân
Trong `AdminUserServlet.java`:
```java
String role = "ADMIN".equals(request.getParameter("role"))
            ? "ADMIN" : "USER";
userDAO.updateRole(id, role);
request.getSession().setAttribute("flash", "Đã đổi vai trò tài khoản.");
```
Code chỉ gọi `userDAO.updateRole(id, role)` cập nhật bảng `users`, bỏ quên tầng RBAC `user_roles` và bỏ quên `AuditDAO.log`.

---

## 2. Giải pháp khắc phục

Đồng bộ dữ liệu hai chiều giữa `users.role` và `user_roles`, đồng thời ghi nhật ký kiểm toán:
```java
String role = "ADMIN".equals(request.getParameter("role"))
            ? "ADMIN" : "USER";
userDAO.updateRole(id, role);

RbacDAO rbacDAO = new RbacDAO();
if ("ADMIN".equals(role)) {
    rbacDAO.assignRole(id, "ADMIN");
} else {
    rbacDAO.removeRole(id, "ADMIN");
}

User targetUser = userDAO.findById(id);
String targetLabel = targetUser != null ? targetUser.getUsername() : ("User #" + id);
new AuditDAO().log(me != null ? me.getId() : 0, "CHANGE_ROLE", "USER", id, targetLabel,
        "ADMIN".equals(role) ? "Thăng cấp lên Quản trị viên (ADMIN)" : "Hạ quyền xuống Thành viên thường (USER)");
```

Bổ sung nhãn hiển thị `CHANGE_ROLE` -> `"Đổi vai trò"` trên màn hình `admin/audit.jsp` và model `AuditLog.java`.
