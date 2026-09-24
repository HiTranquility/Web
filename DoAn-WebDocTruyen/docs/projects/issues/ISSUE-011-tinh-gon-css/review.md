# 🔍 [Review] Tinh gọn CSS & Kiến trúc tải theo phân tầng — Nghiệm thu ISSUE-011

> **Đích:** `docs/projects/issues/ISSUE-011-tinh-gon-css/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-011 |
| **Review cho** | ISSUE-011: Tinh gọn CSS & Kiến trúc tải theo phân tầng |
| **Người làm** | Dev D |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Phân tách stylesheet chuẩn mực:**
  - `base.css`: Biến màu CSS variables, reset thống nhất, typography, utilities.
  - `components.css`: Nút bấm, thẻ truyện, nhãn thể loại, hộp bình luận, thanh toast thông báo, modal nạp xu, preview bìa.
  - `layout-auth.css`: 8.5 KB biệt lập cho trang đăng nhập, đăng ký, quên mật khẩu.
  - `layout-reader.css`: 18 KB biệt lập cho trải nghiệm đọc truyện (cỡ chữ, giãn dòng, danh sách chương, phím tắt điều hướng).
  - `layout-admin.css`: 1.7 KB cho dashboard quản trị.
  - `layout-editor.css`: 1.8 KB cho khu vực viết và đăng chương.
  - `layout-main.css`: 1.8 KB cho layout chung của trang chủ, chi tiết, bảng xếp hạng.

- **Nạp có điều kiện qua `head.jsp`:**
  - Các trang chỉ nạp `layout-*.css` tương ứng khi khai báo `<c:set var="layoutCss" value="..."/>`.
  - Hạn chế tối đa việc tải dư thừa CSS giữa các ngữ cảnh sử dụng khác nhau.
