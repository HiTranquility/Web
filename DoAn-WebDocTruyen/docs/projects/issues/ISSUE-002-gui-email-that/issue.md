# 🏷️ ISSUE-002: Gửi email thật cho quên mật khẩu (Password Reset via SMTP)

> **Đích:** `docs/projects/issues/ISSUE-002-gui-email-that/issue.md` — issue là **thư mục**: file này (`issue.md`) · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-002 |
| **Người làm** | Dev A — Auth & Security |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-17 |
| **CASE liên quan** | CASE 01 — Đăng ký / Đăng nhập / Quên mật khẩu |
| **Đụng vào** | `truyen/util/MailSender.java` *(mới)* · `src/main/resources/mail.properties` *(mới)* · `truyen/controller/common/AuthServlet.java` · `views/auth/forgot.jsp` · `pom.xml` · `scripts/run.ps1` · `truyen/MailSenderTest.java` *(mới)* |

---

## 1. Làm cái gì, và vì sao (Goal)

Khắc phục lỗ hổng bảo mật nghiêm trọng 🔴 (**Account Takeover**):
Khi người dùng bấm quên mật khẩu, hệ thống cũ in thẳng liên kết kèm token bí mật (`devLink`) ra màn hình web của người gửi yêu cầu. Kẻ xấu chỉ cần gõ email của bất kỳ người dùng nào là có thể cướp tài khoản của họ ngay lập tức.

- **Hiện tại:**
  - `AuthServlet:335` tạo token và nhét thẳng vào `request.setAttribute("devLink", ...)`.
  - Giao diện `views/auth/forgot.jsp` hiển thị nút "Đặt lại mật khẩu ngay" cho người đang đứng trước màn hình.
  - Không có kết nối máy chủ gửi thư SMTP.
- **Sau khi xong:**
  - Xóa bỏ triệt để việc in token ra màn hình giao diện.
  - Tích hợp dịch vụ gửi thư SMTP (JavaMail `javax.mail`) hỗ trợ STARTTLS / SSL.
  - Xử lý bất đồng bộ (**Asynchronous Queue**) qua `ExecutorService` để giao diện web phản hồi tức thì, không bị treo khi chờ bắt tay SMTP.
  - Cung cấp chế độ **Mô phỏng an toàn (Dev Mode)**: khi chưa điền thông tin SMTP thật trong `mail.properties`, link được ghi vào Console log bảo mật của lập trình viên, tuyệt đối không lộ ra trình duyệt.

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] Tạo lớp `MailSender.java` hỗ trợ gửi thư SMTP thật và hàng đợi xử lý ngầm (`sendPasswordResetEmailAsync`).
- [x] Tạo mẫu email HTML thẩm mỹ, chuyên nghiệp, responsive trên di động và chống XSS trong tên người nhận.
- [x] `AuthServlet.forgot()` gọi `MailSender` gửi link an toàn và **không** đưa `devLink` vào `request.setAttribute`.
- [x] Giao diện `views/auth/forgot.jsp` loại bỏ khối cảnh báo và link `devLink`, hiển thị thông báo đã gửi thư an toàn theo khuyến nghị OWASP.
- [x] Cập nhật dependency JavaMail vào `pom.xml` và script chạy `scripts/run.ps1`.
- [x] Bộ kiểm thử tự động `MailSenderTest.java` và toàn bộ 74 bài test của dự án chạy pass 100%.

---

## 3. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** `git checkout HEAD~1` hoặc revert commit tương ứng. Không có thay đổi CSDL nên không cần script rollback SQL.
