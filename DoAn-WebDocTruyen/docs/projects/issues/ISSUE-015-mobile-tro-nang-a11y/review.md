# 🔍 [Review] Trải nghiệm di động (Mobile 360px) và Trợ năng (A11y) — Nghiệm thu ISSUE-015

> **Đích:** `docs/projects/issues/ISSUE-015-mobile-tro-nang-a11y/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-015 |
| **Review cho** | ISSUE-015: Trải nghiệm di động (Mobile 360px) và Trợ năng (A11y) |
| **Người làm** | Dev D |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Khả năng hiển thị di động & Responsive:**
  - Layout co dãn linh hoạt xuống tới màn hình 360px (iPhone SE, Android tiêu chuẩn).
  - Khung nội dung truyện và danh sách chương tự động thu gọn padding phù hợp trên thiết bị nhỏ (`.shell { padding: 0 16px; }`).
- **Trợ năng & Khả năng tiếp cận (Accessibility):**
  - Bổ sung quy tắc vùng chạm ngón tay tối thiểu 44px (`min-height: 44px`) cho các nút bấm và phân trang trên màn hình di động theo chuẩn WCAG 2.1.
  - Hỗ trợ `:focus-visible` tương phản cao viền kép sắc nét khi di chuyển bằng phím Tab.
  - Hỗ trợ `.skip-link` trồi lên khi focus để hỗ trợ bộ đọc màn hình (Screen Reader).
  - Tích hợp bộ lọc `@media (prefers-reduced-motion: reduce)` dừng mọi hiệu ứng transition/animation khi người dùng kích hoạt giảm chuyển động trong hệ điều hành.
