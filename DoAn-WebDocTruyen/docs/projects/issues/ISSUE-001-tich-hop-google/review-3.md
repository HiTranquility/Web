# 🔍 [Review 3] Tích hợp Google — Review Phase 3: Gắn và gỡ tài khoản Google ở trang hồ sơ

> **Đích:** `docs/projects/issues/ISSUE-001-tich-hop-google/review-3.md`
>
> **Cách dùng:** Điền sau khi hoàn thành xong `phase-3.md`. Đối chiếu baseline kiểm thử, hàng rào an toàn chống lockout (Unlink Security Gate) và Acceptance Criteria.

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | [phase-3.md](phase-3.md) |
| **Ngày review** | 2026-09-17 |
| **Người review** | Tech Lead / Security Reviewer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Phase 3 bao gồm:
1. `UserServlet`:
   - Thêm route `action=link-google`: nhận `idToken`, xác thực chữ ký qua `GoogleTokenVerifier`, chống gắn tài khoản Google đã bị người khác liên kết (409 Conflict), ghi nhận vào `user_identities`.
   - Thêm route `action=unlink-google` kèm **Hàng rào bảo mật máy chủ (Security Gate)**: cấm tuyệt đối việc gỡ Google nếu `!userDAO.hasPassword(me.getId())`, ngăn chặn hoàn toàn nguy cơ người dùng tự khóa vĩnh viễn tài khoản của mình.
   - Thêm route `action=set-password`: cho phép tài khoản đăng ký qua Google (`password_hash IS NULL`) tạo mật khẩu lần đầu an toàn mà không đòi hỏi `oldPassword`.
2. Giao diện người dùng:
   - `views/user/edit.jsp`: Thêm khối "Tài khoản liên kết" với 2 trạng thái (Đã gắn kèm email + nút Hủy liên kết; Chưa gắn kèm nút Liên kết Google). Tự động điều chỉnh form mật khẩu: "Đổi mật khẩu" (khi đã có mật khẩu) hoặc "Tạo mật khẩu lần đầu" (khi chưa có).
   - `views/user/me.jsp`: Thêm dòng trạng thái "Liên kết Google" trong danh sách thông tin tài khoản riêng tư.
3. JavaScript:
   - `assets/js/firebase-auth.js`: Gắn sự kiện cho `#btn-link-google`, mở popup Google xác thực, gửi `idToken` lên `/user?action=link-google` và hiển thị phản hồi trực quan.
4. Kiểm thử:
   - Viết mới `src/test/java/truyen/UnlinkGuardTest.java` với 4 ca kiểm thử chuyên sâu cho toàn bộ luồng bảo vệ và xử lý tranh chấp liên kết.

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước phase 3 | Sau phase 3 | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 40 pass / 0 fail | **44 pass / 0 fail** | ✅ |
| Biên dịch code (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Chặn gỡ Google khi chưa có mật khẩu (Lockout Guard) | — | 100% bị chặn ở cấp Servlet và CSDL | ✅ |
| Đặt mật khẩu lần đầu cho Google User | — | Hoạt động trơn tru, mở khóa gỡ Google | ✅ |
| Các chức năng cũ của form sửa hồ sơ (đổi tên, avatar, bio) | Hoạt động bình thường | Hoạt động bình thường, không hồi quy | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria Phase 3

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Nhận `idToken` và liên kết Google cho người đang đăng nhập | ✅ | `UserServlet.linkGoogle()` xác minh token với `GoogleTokenVerifier` và lưu vào `user_identities` |
| Chặn gắn tài khoản Google đã thuộc về người khác | ✅ | Trả về `409 Conflict` kèm thông báo rõ ràng, không gây lỗi 500 |
| **Hàng rào máy chủ cho `unlink-google`** | ✅ | Kiểm tra `!userDAO.hasPassword(me.getId())` ngay đầu method, chặn cả AJAX lẫn truy cập URL trực tiếp |
| Đặt mật khẩu lần đầu (`set-password`) | ✅ | Không yêu cầu mật khẩu cũ, cập nhật hash mật khẩu mới vào `users` |
| Giao diện `edit.jsp` thích ứng động | ✅ | Hiện đúng trạng thái đã gắn/chưa gắn; nút gỡ bị vô hiệu hóa khi chưa có mật khẩu; form mật khẩu phân nhánh rõ ràng |
| Giao diện `me.jsp` có chỉ báo liên kết Google | ✅ | Hiển thị badge "Đã liên kết" kèm email hoặc "Chưa liên kết" kèm lối tắt |
| Bộ test tự động `UnlinkGuardTest` (4 ca) | ✅ | Đạt 4/4 ca kiểm thử độc lập, không phụ thuộc CSDL thật |
| Không tụt lùi hồi quy | ✅ | Toàn bộ 44 test suites chạy hoàn tất sau ~1 giây |

---

## 🐞 Lỗi phát hiện & Đã xử lý

| # | Mức độ | Mô tả | Vị trí / File | Cách xử lý |
|---|---|---|---|---|
| 1 | Thấp | `identityDAO.findByUserId` trả về List nhưng gán vào object đơn | `UserServlet.java` | Chuyển sang gọi `identityDAO.findByUserAndProvider(userId, "GOOGLE")` |
| 2 | Thấp | Tên method `GoogleUser.getSubject()` khác với `getSub()` | `UserServlet.java` | Sửa lại thành `gUser.getSub()` theo đúng định nghĩa model |

---

## 📋 Action Items → Đưa vào Phase 4

1. Tích hợp Google reCAPTCHA v2 / v3 ở các form nhạy cảm (`login`, `register`, `forgot`) để ngăn bot vét tài khoản và spam.
2. Viết `RecaptchaVerifier.java` và bổ sung cấu hình `recaptcha.site_key`, `recaptcha.secret_key` vào `google.properties`.
3. Kiểm thử tự động với mock token cho luồng reCAPTCHA.
