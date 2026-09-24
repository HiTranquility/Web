# 🏷️ ISSUE-016: Trang báo lỗi 404 & 500 tùy biến phong cách truyện

> **Đích:** `docs/projects/issues/ISSUE-016-trang-loi-404-500/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-016 |
| **Người làm** | Dev D — UI & Khách |
| **Trạng thái** | 🟡 Đang làm |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Toàn bộ hệ thống — Xử lý lỗi tập trung |
| **Đụng vào** | `src/main/webapp/WEB-INF/web.xml` · `src/main/java/truyen/controller/common/ErrorServlet.java` · `src/main/webapp/WEB-INF/views/error/404.jsp` · `src/main/webapp/WEB-INF/views/error/500.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

Hiện tại, khi người dùng truy cập vào một đường dẫn không tồn tại (404) hoặc hệ thống gặp sự cố bất ngờ (500), Apache Tomcat hiển thị trang lỗi màu trắng xám mặc định:
- Làm lộ phiên bản Tomcat, cấu trúc thư mục máy chủ và stack trace (nguy cơ bảo mật).
- Khiến người đọc mất phương hướng, không có thanh điều hướng hay nút để quay lại trang chủ.

**Sau khi hoàn thành:**
- Mọi lỗi 404 và 500 được bắt chặn qua `web.xml` và điều hướng tới `ErrorServlet`.
- Trang lỗi được bọc trong khung `layout/main.jsp`, giữ nguyên thanh điều hướng, thanh tìm kiếm và footer.
- Minh họa phong cách sách truyện bay bổng, thông điệp nhẹ nhàng (*"Trang truyện này dường như đã bị lạc giữa các chiều không gian..."*), nút hành động *"Quay về Trang chủ"* và *"Tìm truyện khác"*.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Khai báo `<error-page>` cho 404, 500 và `java.lang.Throwable` trong `web.xml`.
- [ ] Tạo `ErrorServlet.java` an toàn, trích xuất mã lỗi và chuyển tiếp sang view phù hợp.
- [ ] Giao diện `views/error/404.jsp` và `views/error/500.jsp` đẹp mắt, responsive.
- [ ] Không lộ stack trace nhạy cảm ra ngoài cho người dùng thường.
