# 🏷️ ISSUE-013: Dọn dẹp nhật ký lượt xem (view_logs) định kỳ

> **Đích:** `docs/projects/issues/ISSUE-013-don-view-logs-dinh-ky/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-013 |
| **Người làm** | Dev C — Database & Performance |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Bảo trì hệ thống — Hiệu năng CSDL |
| **Đụng vào** | `src/main/java/truyen/dao/ViewLogDAO.java` · `src/main/java/truyen/util/AppListener.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

Trong `database/schema.sql`, bảng `view_logs` ghi nhận mỗi lượt mở đọc truyện và được cảnh báo là bảng lớn nhất hệ thống. Sau thời gian dài vận hành, hàng triệu bản ghi vô danh (`user_id IS NULL`) quá cũ sẽ làm chậm truy vấn thống kê và làm phình to kích thước cơ sở dữ liệu.

**Sau khi hoàn thành:**
- `ViewLogDAO` có phương thức `cleanOldLogs(int days)` dọn dẹp các dòng nhật ký vô danh đã vượt quá 90 ngày.
- `AppListener` kích hoạt luồng dọn dẹp ngầm (background thread) định kỳ khi khởi động hệ thống, không làm nghẽn luồng chính.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] `ViewLogDAO.cleanOldLogs(days)` xóa đúng các dòng `user_id IS NULL` và `viewed_at < NOW() - INTERVAL ? DAY`.
- [ ] Chạy an toàn khi ở chế độ DemoData (không ném ngoại lệ).
- [ ] Tích hợp tự động trong `AppListener`.
