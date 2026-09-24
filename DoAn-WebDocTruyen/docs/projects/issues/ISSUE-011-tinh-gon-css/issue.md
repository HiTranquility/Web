# 🏷️ ISSUE-011: Tinh gọn CSS & Kiến trúc tải theo phân tầng

> **Đích:** `docs/projects/issues/ISSUE-011-tinh-gon-css/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-011 |
| **Người làm** | Dev D — Frontend Architecture |
| **Trạng thái** | ✅ Đã hoàn thành |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Tối ưu Frontend — Trải nghiệm người dùng |
| **Đụng vào** | `src/main/webapp/assets/css/*` · `src/main/webapp/WEB-INF/views/layout/parts/head.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

Trước đây hệ thống CSS có nguy cơ bị dồn cục toàn bộ mã giao diện vào một file khổng lồ khiến các trang đơn giản (như đăng nhập, đăng ký, xem chương) phải tải toàn bộ CSS của các màn hình phức tạp (như biên tập truyện, quản trị hệ thống, thanh điều khiển đọc).

**Sau khi hoàn thành:**
- Chuẩn hóa kiến trúc phân tầng CSS 4 lớp rõ ràng thông qua `head.jsp`:
  1. `base.css`: Biến màu CSS variables (Dark/Light mode), reset chuẩn, typography, scale kích thước.
  2. `components.css`: Hệ thống thành phần dùng chung (Buttons, Cards, Tags, Alerts, Toasts, Form inputs, Dropdowns, Modals).
  3. `layout-*.css`: Tách riêng khung chuyên biệt cho từng ngữ cảnh:
     - `layout-auth.css`: Dành riêng cho Đăng nhập, Đăng ký, Quên mật khẩu.
     - `layout-reader.css`: Dành riêng cho màn hình đọc truyện (đổi cỡ chữ, phông chữ, thanh công cụ đọc, danh sách chương trượt).
     - `layout-editor.css`: Dành riêng cho màn hình sáng tác, viết chương.
     - `layout-admin.css`: Dành riêng cho trang thống kê, quản trị viên.
     - `layout-main.css`: Dành cho trang chủ, khám phá, thể loại, hồ sơ.
  4. `pageCss`: Tùy biến cục bộ cho từng trang nếu phát sinh logic đặc thù.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [x] Tách rời các khối CSS đặc thù của Reader sang `layout-reader.css`, Auth sang `layout-auth.css`, Admin sang `layout-admin.css`.
- [x] Giữ `components.css` tinh gọn, tập trung làm Design System đồng nhất.
- [x] Cấu hình nạp stylesheet theo biến `layoutCss` và `pageCss` trong `head.jsp`.
- [x] Không làm bể giao diện ở bất kỳ trang nào.
