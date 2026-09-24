# 🏷️ ISSUE-017: Tải ảnh bìa truyện từ máy tính & Xem trước trực tiếp (Live Preview)

> **Đích:** `docs/projects/issues/ISSUE-017-live-preview-anh-bia-truyen/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-017 |
| **Người làm** | Dev D — UI & Tác giả |
| **Trạng thái** | 🟡 Đang làm |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | CASE 04 — Sáng tác & Quản lý truyện |
| **Đụng vào** | `src/main/webapp/WEB-INF/views/user/story/form.jsp` · `src/main/webapp/assets/css/components.css` |

---

## 1. Làm cái gì, và vì sao (Goal)

Trước đây, tại trang Tạo truyện mới (`/story?action=create`) và Sửa truyện (`/story?action=edit`):
- Ô tải ảnh bìa `#coverFile` là ô chọn file thô mặc định của trình duyệt.
- Tác giả chọn ảnh từ máy tính xong không có cơ chế xem trước (Live Preview). Nếu tạo truyện mới thì không hề biết ảnh hiển thị ra sao cho đến khi bấm Lưu.
- Dán link ảnh vào `#coverUrl` cũng không hiển thị bản xem trước tức thì.

**Sau khi hoàn thành:**
- Khung xem trước bìa sách (chuẩn tỷ lệ 3:4) luôn hiện diện trực quan.
- Nút bấm *"📁 Chọn ảnh bìa từ máy"* tùy biến đẹp mắt đồng bộ với giao diện toàn trang.
- Ứng dụng `FileReader API` cho phép hiển thị ảnh bìa ngay lập tức khi tác giả chọn tệp ảnh từ máy hoặc gõ link URL.
- Kiểm tra dung lượng tệp (tối đa 2 MB) ngay phía client trước khi gửi lên máy chủ.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Form đăng/sửa truyện có khung xem trước bìa truyện chuẩn tỷ lệ 3:4.
- [ ] Chọn tệp ảnh từ máy tính -> xem trước tức thì, không cần tải lại trang.
- [ ] Dán link URL ảnh -> xem trước tức thì.
- [ ] Cảnh báo dung lượng nếu file > 2 MB.
