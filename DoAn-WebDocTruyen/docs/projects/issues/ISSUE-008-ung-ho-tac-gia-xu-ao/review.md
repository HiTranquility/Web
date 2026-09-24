# 🔍 [Review] Ví xu ảo & Ủng hộ tác giả — Nghiệm thu ISSUE-008

> **Đích:** `docs/projects/issues/ISSUE-008-ung-ho-tac-gia-xu-ao/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-008 |
| **Review cho** | ISSUE-008: Ủng hộ tác giả bằng xu ảo & Ví tiền ảo |
| **Người làm** | Dev C |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Cơ sở dữ liệu:**
  - Bổ sung bảng `wallets` và `transactions` trong `database/schema.sql` và migration trực tiếp vào MySQL.
- **Tầng DAO `WalletDAO.java`:**
  - Triển khai `getBalance(userId)` khởi tạo mặc định 100 xu cho tài khoản mới.
  - Triển khai `transfer(...)` theo cơ chế ACID transaction (AutoCommit false, kiểm tra số dư, cập nhật số dư, ghi transaction log và commit/rollback).
- **Tầng Servlet `WalletServlet.java`:**
  - Cung cấp `/wallet?action=balance` và `/wallet?action=tip`.
- **Kiểm thử tự động:**
  - `WalletTest.java` kiểm thử các trường hợp chuyển xu số âm, số 0 và tự chuyển cho chính mình đạt 100% OK.
