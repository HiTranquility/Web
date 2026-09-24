# 🏷️ ISSUE-014: Nâng độ phủ Unit Test cho DAO và Servlet

> **Đích:** `docs/projects/issues/ISSUE-014-nang-phu-test-dao-servlet/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-014 |
| **Người làm** | Dev D — Testing & Quality Assurance |
| **Trạng thái** | ✅ Đã hoàn thành |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Tự động hóa kiểm thử — Đảm bảo chất lượng mã nguồn |
| **Đụng vào** | `src/test/java/truyen/StoryDAOTest.java` · `src/test/java/truyen/ViewLogDAOTest.java` · `src/test/java/truyen/ErrorServletTest.java` · `src/test/java/truyen/ApiServletTest.java` · `src/test/java/truyen/WalletTest.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

Trước đây bộ test chủ yếu tập trung vào các lớp tiện ích (`PasswordUtil`, `SlugUtil`, `Chapter.getParagraphs`) và xác thực Google. Các tầng nghiệp vụ trọng yếu như `StoryDAO`, `ViewLogDAO`, `ErrorServlet`, `ApiServlet` và logic ví xu ảo `WalletDAO` chưa có các bài kiểm thử biên và kiểm thử an toàn tự động.

**Sau khi hoàn thành:**
- Viết mới các bài test bao phủ các kịch bản ngoại lệ, giá trị biên (boundary conditions), và trường hợp CSDL rỗng/offline.
- Nâng tổng số bài unit test từ 84 bài ban đầu lên **97 bài test**.
- Tất cả bài test thực thi nhanh trong `scripts/test.ps1` (< 2 giây), không phụ thuộc môi trường ngoài.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [x] Tạo các bài test cho `StoryDAO`: kiểm tra tham số trang không hợp lệ, ID âm hoặc 0, đếm trang.
- [x] Tạo các bài test cho `ViewLogDAO`: kiểm tra xóa log cũ `cleanOldLogs` và đếm lượt đọc `countStories`.
- [x] Tạo các bài test cho `ErrorServlet`: kiểm tra bắt lỗi 404 và 500, gán tiêu đề trang tương ứng.
- [x] Tạo các bài test cho `ApiServlet`: kiểm tra JSON escaping an toàn chống XSS/JSON injection.
- [x] Tạo các bài test cho `WalletTest`: kiểm tra nạp xu, tặng xu tác giả, kiểm tra số dư, tìm theo userId và lịch sử giao dịch.
- [x] Tất cả 97 bài test chạy pass 100% trong `scripts/test.ps1`.
