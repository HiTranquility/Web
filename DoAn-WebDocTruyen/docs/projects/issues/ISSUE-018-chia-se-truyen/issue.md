# 🏷️ ISSUE-018: Tiện ích Chia sẻ truyện & Sao chép liên kết nhanh

> **Đích:** `docs/projects/issues/ISSUE-018-chia-se-truyen/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-018 |
| **Người làm** | Dev D — UI & Khách |
| **Trạng thái** | 🟡 Đang làm |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | CASE 05 — Chi tiết truyện |
| **Đụng vào** | `src/main/webapp/WEB-INF/views/common/story/detail.jsp` · `src/main/webapp/assets/css/components.css` |

---

## 1. Làm cái gì, và vì sao (Goal)

Hiện tại, độc giả đọc truyện hay muốn giới thiệu cho bạn bè nhưng trang chi tiết truyện (`detail.jsp`) chỉ có các liên kết chia sẻ dạng thô hoặc thiếu nút sao chép link 1 chạm (Copy Link) kèm thông báo trực quan.

**Sau khi hoàn thành:**
- Thêm nút *"🔗 Chia sẻ"* nổi bật tại cụm hành động chính.
- Hộp thoại Modal chia sẻ truyện thanh lịch:
  - Hiển thị liên kết truyện có thể chọn/sao chép.
  - Nút *"Sao chép liên kết"* 1-chạm vào clipboard và gọi `window.showToast(...)` thông báo thành công.
  - Nút chia sẻ nhanh lên Facebook và X (Twitter) mở tab mới an toàn (`rel="noopener"`).

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Bấm nút *"Chia sẻ"* mở modal chia sẻ truyện.
- [ ] Bấm *"Sao chép"* sao chép đúng URL vào Clipboard và hiện toast thông báo.
- [ ] Chia sẻ Facebook hoạt động mượt mà.
