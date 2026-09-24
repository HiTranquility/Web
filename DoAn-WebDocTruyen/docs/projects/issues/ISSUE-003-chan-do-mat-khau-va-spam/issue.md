# 🏷️ ISSUE-003: Chặn dò mật khẩu (Brute-force) và chống spam bình luận (Rate Limiting)

> **Đích:** `docs/projects/issues/ISSUE-003-chan-do-mat-khau-va-spam/issue.md` — issue là **thư mục**: file này (`issue.md`) · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-003 |
| **Người làm** | Dev A — Auth & Security |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-17 |
| **CASE liên quan** | CASE 01 — Đăng nhập / Xác thực · CASE 07 — Bình luận |
| **Đụng vào** | `truyen/util/RateLimiter.java` *(mới)* · `truyen/controller/common/AuthServlet.java` · `truyen/controller/user/CommentServlet.java` · `views/layout/main.jsp` · `truyen/RateLimiterTest.java` *(mới)* |

---

## 1. Làm cái gì, và vì sao (Goal)

Khắc phục lỗ hổng an ninh 🔴 liên quan đến việc thiếu cơ chế kiểm soát tần suất (**Rate Limiting**):
1. **Tấn công dò mật khẩu (Brute-force / Credential Stuffing):** Kẻ tấn công có thể dùng bot chạy hàng nghìn yêu cầu thử mật khẩu liên tục trên endpoint `/auth?action=login` mà không bị giới hạn số lần thử, dẫn tới nguy cơ lộ mật khẩu tài khoản người dùng và gây quá tải tài nguyên máy chủ.
2. **Tấn công spam bình luận:** Người dùng hoặc bot có thể bấm nút gửi bình luận liên tiếp hàng chục lần trong vài giây, làm tràn ngập phần bình luận và gửi thông báo rác tới tác giả truyện.

- **Hiện tại:**
  - `AuthServlet.login()` chỉ kiểm tra thông tin đăng nhập trong CSDL, sai bao nhiêu lần cũng không khóa.
  - `CommentServlet.add()` chỉ kiểm tra độ dài và rỗng, không giới hạn khoảng cách thời gian giữa các lần bình luận.
- **Sau khi xong:**
  - Xây dựng tiện ích `RateLimiter` thread-safe in-memory sliding window:
    - Chặn dò mật khẩu: Tối đa 5 lần thử sai trong cửa sổ 15 phút theo cặp IP + Username. Nếu vi phạm, khóa đăng nhập và thông báo thời gian còn lại phải chờ. Đăng nhập đúng sẽ xóa lịch sử thất bại.
    - Chống spam bình luận: Áp dụng khoảng cách cooldown 20 giây giữa hai lần bình luận liên tiếp của cùng một tài khoản.
  - Giao diện người dùng nhận được thông báo rõ ràng (`flashWarn` / `panel-warn`).

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] Tạo lớp `RateLimiter.java` thread-safe, tự động dọn dẹp các mốc thời gian quá hạn để tránh rò rỉ RAM.
- [x] Tích hợp kiểm tra và ghi nhận số lần đăng nhập thất bại trong `AuthServlet.java`.
- [x] Hiển thị số lượt thử còn lại khi đăng nhập sai, và khóa kèm số phút chờ khi vượt quá 5 lần.
- [x] Tích hợp cooldown 20 giây vào `CommentServlet.java`, cảnh báo người dùng khi thao tác quá nhanh.
- [x] Bổ sung hiển thị `flashWarn` trên `views/layout/main.jsp`.
- [x] Bộ kiểm thử tự động `RateLimiterTest.java` gồm 6 ca kiểm thử chi tiết chạy pass 100%.

---

## 3. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** Revert commit hoặc hoàn tác các file sửa đổi. Không can thiệp cấu trúc CSDL nên không cần script rollback SQL.
