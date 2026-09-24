# 🔍 [Review] Dọn dẹp view_logs định kỳ — Nghiệm thu ISSUE-013

> **Đích:** `docs/projects/issues/ISSUE-013-don-view-logs-dinh-ky/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-013 |
| **Review cho** | ISSUE-013: Dọn dẹp nhật ký lượt xem (view_logs) định kỳ |
| **Người làm** | Dev C |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Tầng DAO `ViewLogDAO.java`:**
  - Bổ sung phương thức `cleanOldLogs(int days)` xóa các lượt xem vô danh `user_id IS NULL` có tuổi đời quá 90 ngày.
- **Tầng Listener `AppListener.java`:**
  - Khởi động luồng chạy nền `viewlogs-cleaner` lúc ứng dụng khởi tạo `contextInitialized` để thực hiện dọn dẹp không đồng bộ, ghi log minh bạch.
