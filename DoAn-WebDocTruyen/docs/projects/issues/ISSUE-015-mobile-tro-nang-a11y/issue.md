# 🏷️ ISSUE-015: Trải nghiệm di động (Mobile 360px) và Trợ năng (A11y)

> **Đích:** `docs/projects/issues/ISSUE-015-mobile-tro-nang-a11y/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-015 |
| **Người làm** | Dev D — UI/UX & Accessibility |
| **Trạng thái** | ✅ Đã hoàn thành |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Giao diện di động & Khả năng tiếp cận Web Accessibility (WCAG 2.1) |
| **Đụng vào** | `src/main/webapp/assets/css/base.css` · `src/main/webapp/assets/css/components.css` · `src/main/webapp/assets/css/layout-*.css` |

---

## 1. Làm cái gì, và vì sao (Goal)

Độc giả đọc truyện phần lớn sử dụng điện thoại di động thông minh với nhiều kích cỡ màn hình khác nhau (từ 360px tới máy tính bảng). Website cần đáp ứng tốt các tiêu chuẩn tiếp cận:
1. **Responsive Viewport:** Không bị vỡ khung hoặc xuất hiện thanh cuộn ngang ngoài ý muốn ở màn hình nhỏ từ 360px.
2. **Kích thước vùng chạm (Touch Target):** Các nút bấm và liên kết phân trang đạt chuẩn tối thiểu 44x44px trên thiết bị di động theo chuẩn WCAG 2.1.
3. **Người dùng bàn phím & Hỗ trợ chuyển động:** Có viền `:focus-visible` rõ ràng khi điều hướng bằng phím Tab, hỗ trợ `.skip-link` bỏ qua header để vào nội dung chính, tôn trọng tùy chọn `prefers-reduced-motion` của người dùng nhạy cảm với hiệu ứng đồ họa.
4. **Văn bản thay thế (Image Alt):** Tất cả thẻ ảnh bìa truyện đều có thuộc tính `alt` mô tả tên tác phẩm.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [x] Responsive mượt mà ở độ phân giải 360px (mobile chuẩn).
- [x] Các nút bấm chính và phân trang trên mobile có kích thước chạm tối thiểu 44px.
- [x] Quy tắc `:focus-visible` rõ ràng với màu viền cam thương hiệu (`var(--ember)`).
- [x] Tuân thủ `prefers-reduced-motion` giảm chuyển động đồ họa khi hệ điều hành yêu cầu.
- [x] Bổ sung lớp `.skip-link` điều hướng nhanh tới phần nội dung chính.
