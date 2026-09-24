# 🏷️ ISSUE-008: Ủng hộ tác giả bằng xu ảo & Ví tiền ảo

> **Đích:** `docs/projects/issues/ISSUE-008-ung-ho-tac-gia-xu-ao/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-008 |
| **Người làm** | Dev C — Database & Transaction |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | CASE 14 — Hồ sơ cá nhân & Chi tiết truyện |
| **Đụng vào** | `database/schema.sql` · `src/main/java/truyen/dao/WalletDAO.java` · `src/main/java/truyen/controller/user/WalletServlet.java` · `src/test/java/truyen/WalletTest.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

Xây dựng hệ thống ví xu ảo nội bộ (Virtual Coin Wallet) để độc giả có thể tặng thưởng (tip) xu cho tác giả mà họ yêu mến mà không dính líu tới tiền tệ thực tế hay các thủ tục thanh toán phức tạp.

**Sau khi hoàn thành:**
- Bảng `wallets` lưu số dư xu của người dùng (tự động tặng 100 xu khi mở tài khoản).
- Bảng `transactions` ghi lại lịch sử các lượt tặng xu (từ ai, tới ai, cho truyện nào, số lượng, lời nhắn).
- `WalletDAO.transfer(...)` thực thi giao dịch nguyên tử (Atomic Transaction với `con.setAutoCommit(false)` và rollback nếu thiếu số dư).
- `WalletServlet` cung cấp endpoint `/wallet?action=tip` và `/wallet?action=balance`.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Schema có bảng `wallets` và `transactions` với khóa ngoại liên kết.
- [ ] Giao dịch chuyển xu trừ đúng người gửi, cộng đúng người nhận và không cho phép tự tặng chính mình.
- [ ] Chống âm tiền khi số dư không đủ.
- [ ] Unit test `WalletTest.java` chạy thành công.
