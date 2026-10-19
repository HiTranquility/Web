# 🐞 bug-003: Lỗ hổng kiểm tra quyền trong AdminFilter cho phép EDITOR vào Ví xu và Quản lý tài khoản

> **Đích:** `docs/projects/bugs/bug-003-admin-filter-lo-hong-phan-quyen-editor/bug.md`
> **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | bug-003 |
| **Mức** | 🔴 Chặn (Hở bảo mật / Bypass Authorization) |
| **Feature liên quan** | [ISSUE-023](../../issues/ISSUE-023-admin-vi-xu-nhat-ky/issue.md) · RBAC Setup |
| **Người sửa** | Phát |
| **Trạng thái** | 🛠️ Đang làm |
| **Ngày phát hiện** | 2026-10-18 |
| **Nơi xảy ra** | `src/main/java/truyen/filter/AdminFilter.java` |

---

## 1. Tái hiện thế nào

1. Đăng nhập bằng tài khoản `editor1` (mật khẩu `123456`), tài khoản này có vai trò `EDITOR` và quyền `MANAGE_STORIES`.
2. Trên thanh địa chỉ trình duyệt, gõ link nhạy cảm: `http://localhost:8080/admin/wallet` hoặc `http://localhost:8080/admin/user`.
3. Bấm Enter.

**Thấy:** `editor1` xem được toàn bộ số dư ví xu, lịch sử biến động và danh sách toàn bộ người dùng, thậm chí có thể cộng/trừ xu hoặc khoá tài khoản.
**Đáng ra phải:** Trả về mã lỗi `403 Forbidden` vì `EDITOR` chỉ có quyền biên tập truyện, không được phép can thiệp vào tài chính (`MANAGE_WALLET`) và tài khoản người dùng (`MANAGE_USERS`).

---

## 2. Chứng cứ

Mã nguồn trong `AdminFilter.java` dòng 56–64:
```java
if (me.hasRole("MODERATOR") && !me.isAdmin()) {
    if (path.startsWith("/admin/wallet") || path.startsWith("/admin/audit") || path.startsWith("/admin/user")) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
} else if (!me.isAdmin() && !me.hasPermission("MANAGE_STORIES") && !me.hasPermission("MODERATE_COMMENTS")) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

Khi `editor1` đăng nhập:
- `me.hasRole("MODERATOR")` -> `false` (vì role là `EDITOR`).
- Chuyển sang nhánh `else if`: Kiểm tra `!me.hasPermission("MANAGE_STORIES")`.
- Vì `editor1` CÓ quyền `MANAGE_STORIES`, nên biểu thức `!me.hasPermission("MANAGE_STORIES")` trả về `false`.
- Toàn bộ mệnh đề `else if` bị `false`, filter không ném 403 mà cho qua (`chain.doFilter`)!
- Ngoài ra, `@WebFilter("/admin/*")` không khớp với URL `/admin` (thiếu gạch chéo cuối).

---

## 3. Nguyên nhân

- **Sai ở:** `src/main/java/truyen/filter/AdminFilter.java:56-64`
- **Vì:** Viết điều kiện theo kiểu loại trừ cục bộ `MODERATOR` thay vì kiểm tra theo đúng ma trận quyền RBAC (Role-Based Access Control) cho từng đường dẫn (`path.startsWith`).

---

## 4. Sửa thế nào (Hướng dẫn cho Phát)

1. Cập nhật `@WebFilter(urlPatterns = {"/admin", "/admin/*"})`.
2. Trong hàm `doFilter`, kiểm tra phân quyền chi tiết theo path:
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

## 5. Kiểm lại

- [ ] Đăng nhập `editor1` vào `/admin/wallet` -> Thấy 403 Forbidden.
- [ ] Đăng nhập `editor1` vào `/admin/story` -> Truy cập bình thường.
- [ ] Đăng nhập `mod1` vào `/admin/comment` -> Truy cập bình thường.
- [ ] Đăng nhập `mod1` vào `/admin/wallet` -> Thấy 403 Forbidden.
- [ ] Đăng nhập `admin` -> Vào được tất cả các mục.
- [ ] Chạy `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` -> 100% test pass.
