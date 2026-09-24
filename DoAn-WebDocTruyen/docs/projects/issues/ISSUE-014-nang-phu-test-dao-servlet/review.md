# 🔍 [Review] Nâng độ phủ Unit Test cho DAO và Servlet — Nghiệm thu ISSUE-014

> **Đích:** `docs/projects/issues/ISSUE-014-nang-phu-test-dao-servlet/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-014 |
| **Review cho** | ISSUE-014: Nâng độ phủ Unit Test cho DAO và Servlet |
| **Người làm** | Dev D |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Kiểm thử `StoryDAOTest.java`:**
  - Ca 1: `findPage()` với tham số null/rỗng không ném ngoại lệ và trả về danh sách an toàn.
  - Ca 2: `findById()` với ID âm hoặc 0 trả về null an toàn.
  - Ca 3: `countPage()` trả về số nguyên >= 0.

- **Kiểm thử `ViewLogDAOTest.java`:**
  - Ca 1: `cleanOldLogs()` chạy an toàn, không ném lỗi kết nối.
  - Ca 2: `countStories()` với user âm trả về 0 an toàn.

- **Kiểm thử `ErrorServletTest.java`:**
  - Ca 1: Mã lỗi 404 nạp đúng tiêu đề "Không tìm thấy trang (404)" và view 404.
  - Ca 2: Mã lỗi 500 nạp đúng tiêu đề "Lỗi máy chủ (500)" và view 500.

- **Kiểm thử `ApiServletTest.java`:**
  - Ca 1: `jsonEscape()` mã hóa an toàn ký tự đặc biệt, dấu ngoặc kép và xuống dòng.
  - Ca 2: `jsonEscape()` xử lý an toàn dấu gạch chéo ngược.

- **Kiểm thử `WalletTest.java`:**
  - Ca 1: Chuyển xu với số tiền <= 0 trả về false.
  - Ca 2: Tự chuyển xu cho chính mình trả về false.
  - Ca 3: `findByUserId()` trả về đối tượng Wallet với số dư hợp lệ.
  - Ca 4: `findHistoryByUserId()` với limit hợp lệ trả về danh sách an toàn.

- **Kết quả thực thi:**
  - `scripts/test.ps1`: **97 tests successful**, 0 failed, hoàn tất trong ~1.4s.
