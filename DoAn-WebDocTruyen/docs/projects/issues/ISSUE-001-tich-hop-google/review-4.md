# 🔍 [Review 4] Tích hợp Google — Review Phase 4: Google reCAPTCHA v3 chặn bot ở ba cửa

> **Đích:** `docs/projects/issues/ISSUE-001-tich-hop-google/review-4.md`
>
> **Cách dùng:** Điền sau khi hoàn thành xong `phase-4.md`. Đối chiếu baseline kiểm thử, kiến trúc filter chặn bot và Acceptance Criteria.

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | [phase-4.md](phase-4.md) |
| **Ngày review** | 2026-09-17 |
| **Người review** | Tech Lead / Security Reviewer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Phase 4 bao gồm:
1. `GoogleConfig`:
   - Bổ sung cấu hình `google.recaptcha.enabled`, `google.recaptcha.site_key`, `google.recaptcha.secret_key`, `google.recaptcha.threshold`.
   - Cung cấp method `isRecaptchaEnabled()`, `getRecaptchaSiteKey()`, `getRecaptchaSecretKey()`, `getRecaptchaThreshold()`.
2. `util/RecaptchaVerifier`:
   - Gọi trực tiếp Google siteverify API `https://www.google.com/recaptcha/api/siteverify`.
   - **Timeout cứng 2 giây (2000 ms)**.
   - **Cơ chế Fail-Open (CÓ CHỦ Ý)**: Khi Google timeout hoặc mất mạng, tự động cho request đi tiếp thay vì chặn nhầm làm liệt toàn bộ trang web khi mạng wifi phòng học chập chờn.
   - Kiểm tra `action` khớp với form gửi lên nhằm chống tráo token giữa các form.
3. `filter/RecaptchaFilter`:
   - Đặt **sau `CsrfFilter`** trong `web.xml`: loại bỏ request rác bằng bộ nhớ trước khi gọi mạng sang Google.
   - Chỉ chặn POST ở đúng 3 cửa nhạy cảm:
     - `POST /auth?action=register` (ngưỡng 0.5)
     - `POST /auth?action=login` (ngưỡng 0.3)
     - `POST /comment?action=add` (ngưỡng 0.5)
   - Toàn bộ các request GET và các URL khác đều được cho qua thẳng.
4. Giao diện & Client:
   - `assets/js/recaptcha.js`: Chạy ngầm hoàn toàn, người thật không thấy ô tick, tự động lấy token và gắn vào form khi submit.
   - `head.jsp`: Nạp thẻ script Google reCAPTCHA có điều kiện `<c:if test="${recaptchaEnabled}">`.
   - `login.jsp`, `register.jsp`, `detail.jsp`: Bổ sung thẻ ẩn `g-recaptcha-token`.
5. Kiểm thử:
   - Viết mới `src/test/java/truyen/RecaptchaFilterTest.java` với 8 ca kiểm thử bao phủ toàn bộ luồng xử lý và ngưỡng điểm.

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước phase 4 | Sau phase 4 | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 44 pass / 0 fail | **52 pass / 0 fail** | ✅ |
| Biên dịch code (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Chặn bot đăng ký (điểm < 0.5) | — | Bị chặn, trả thông báo lỗi | ✅ |
| Đăng nhập người thật (điểm >= 0.3) | — | Cho qua bình thường | ✅ |
| Cơ chế Fail-Open khi timeout mạng | — | Cho qua, không làm chết site | ✅ |
| Các trang khác (đọc truyện, sửa hồ sơ, admin) | Hoạt động bình thường | Hoạt động bình thường, không hồi quy | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria Phase 4

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Gắn reCAPTCHA v3 vào đúng 3 cửa nhạy cảm | ✅ | `RecaptchaFilter` chỉ kiểm tra `/auth` (register, login) và `/comment` (add) |
| Người thật không phải tick ô hay chọn ảnh | ✅ | Dùng reCAPTCHA v3 chấm điểm ngầm 0.0 - 1.0 qua JavaScript |
| Timeout cứng 2 giây | ✅ | `conn.setConnectTimeout(2000)` và `conn.setReadTimeout(2000)` trong `RecaptchaVerifier` |
| Fail-open có chủ ý khi mạng lỗi | ✅ | Catch `IOException` trả về điểm 1.0 để tiếp tục xử lý |
| `RecaptchaFilter` đứng sau `CsrfFilter` | ✅ | Khai báo thứ tự chính xác trong `web.xml` |
| Bật/tắt an toàn theo cấu hình | ✅ | Tự tắt khi `google.recaptcha.enabled=false` hoặc thiếu key, không lỗi |
| Bộ test tự động `RecaptchaFilterTest` (8 ca) | ✅ | Đạt 8/8 ca kiểm thử đơn vị |
| Toàn bộ test suite chạy sạch | ✅ | 52/52 tests pass trong ~1.2 giây |

---

## 🐞 Lỗi phát hiện & Đã xử lý

| # | Mức độ | Mô tả | Vị trí / File | Cách xử lý |
|---|---|---|---|---|
| - | Không có | Mọi tiêu chí của Phase 4 đều hoạt động chính xác theo thiết kế | - | - |

---

## 📋 Action Items → Đưa vào Phase 5

1. Triển khai Google Drive Backup cho tác giả sao lưu toàn bộ chương truyện sang Google Drive cá nhân.
2. Xây dựng `DriveAuthServlet` và `DriveService` thực hiện OAuth 2.0 flow với quyền `drive.file`.
