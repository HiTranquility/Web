# [bug-006] Moderator Bị Chặn 403 Forbidden Khi Xem Ảnh Bằng Chứng Tố Cáo

- **Người thực hiện:** **Phát**
- **Ngày phát hiện:** 2026-10-18
- **Mức độ:** 🔴 Nghiêm trọng (Broken Access Control)
- **File ảnh hưởng:** [`src/main/java/truyen/controller/common/UploadedFileServlet.java`](../../../../src/main/java/truyen/controller/common/UploadedFileServlet.java)

---

## 1. Hiện tượng & Nguyên nhân

### Hiện tượng
Khi tài khoản có vai trò `MODERATOR` (được cấp quyền `HANDLE_REPORTS`) truy cập vào trang Quản trị Báo cáo vi phạm `/admin/report`, mở xem danh sách báo cáo có đính kèm ảnh bằng chứng (`evidence/...`), trình duyệt gửi request tới `/uploads/evidence/...` và nhận về mã lỗi **403 Forbidden**. Moderator không thể kiểm tra được ảnh bằng chứng của người dùng báo cáo để ra quyết định xử lý.

### Nguyên nhân
Trong `UploadedFileServlet.java` (dòng 88–93):
```java
String rel = root.relativize(file).toString().replace('\\', '/');
if (rel.startsWith(truyen.util.UploadUtil.EVIDENCE_PREFIX)) {
    truyen.model.User me = truyen.util.ServletHelper.currentUser(request);
    if (me == null || !me.isAdmin()) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
}
```
Code chỉ kiểm tra `!me.isAdmin()`. Mặc dù `MODERATOR` có quyền `HANDLE_REPORTS` và được phép vào xem trang báo cáo, nhưng servlet phát file ảnh lại chặn cứng nếu không phải `ADMIN` tối cao.

---

## 2. Giải pháp khắc phục

Mở rộng điều kiện kiểm tra quyền trong `UploadedFileServlet.java`:
```java
String rel = root.relativize(file).toString().replace('\\', '/');
if (rel.startsWith(truyen.util.UploadUtil.EVIDENCE_PREFIX)) {
    truyen.model.User me = truyen.util.ServletHelper.currentUser(request);
    if (me == null || (!me.isAdmin() && !me.hasPermission("HANDLE_REPORTS"))) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
}
```

Bảo đảm cả Quản trị viên (`ADMIN`) lẫn Kiểm duyệt viên được phân quyền xử lý tố cáo (`HANDLE_REPORTS`) đều xem được ảnh bằng chứng an toàn, trong khi người dùng bình thường và khách vãng lai vẫn bị chặn 403 tuyệt đối.
