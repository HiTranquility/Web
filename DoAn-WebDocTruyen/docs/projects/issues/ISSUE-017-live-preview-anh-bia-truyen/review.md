# 🔍 [Review] Xem trước ảnh bìa truyện trực tiếp — Nghiệm thu ISSUE-017

> **Đích:** `docs/projects/issues/ISSUE-017-live-preview-anh-bia-truyen/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-017 |
| **Review cho** | ISSUE-017: Tải ảnh bìa truyện từ máy tính & Xem trước trực tiếp (Live Preview) |
| **Người làm** | Dev D |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Giao diện `views/user/story/form.jsp`:**
  - Bổ sung khung xem trước bìa sách `#cover-preview-box` chuẩn tỷ lệ 3:4, bóng đổ sang trọng.
  - Nút chọn tệp tùy biến đẹp mắt: *"📁 Chọn ảnh bìa từ máy"*, hiển thị tên tệp đã chọn.
  - Tích hợp `FileReader API` cho phép hiển thị ảnh bìa ngay tức khắc khi tác giả chọn ảnh từ thiết bị.
  - Hỗ trợ xem trước khi dán đường dẫn URL (`#coverUrl`) với cơ chế xử lý lỗi ảnh tự động.
  - Kiểm tra dung lượng tệp phía client (> 2 MB) cảnh báo sớm.
