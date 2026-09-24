# 🔍 [Review 2] Tích hợp Google — Review Phase 2: Xác minh idToken máy chủ & Đóng bug-001

> **Đích:** `docs/projects/issues/ISSUE-001-tich-hop-google/review-2.md`
>
> **Cách dùng:** Điền sau khi hoàn thành xong `phase-2.md`. Đối chiếu baseline kiểm thử, ngăn chặn lỗ hổng giả mạo và Acceptance Criteria.

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | [phase-2.md](phase-2.md) |
| **Ngày review** | 2026-09-17 |
| **Người review** | Tech Lead / Security Reviewer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Phase 2 bao gồm:
1. Đọc và nạp cấu hình Google/Firebase an toàn qua `GoogleConfig.java` (đọc từ `google.properties`, có fallback không gây lỗi 500).
2. Viết `GoogleTokenVerifier.java` xác thực token OIDC/Firebase, trích xuất `sub`, `email`, `name`, `picture`. Không ném exception ra ngoài.
3. Viết lại `AuthServlet.firebaseGoogleLogin()` tuân thủ nghiêm ngặt 10 bước bảo mật:
   - Xác thực chữ ký số `idToken` từ Google OIDC.
   - Tuyệt đối không tin `email`/`uid` gửi tự do từ client.
   - Chống Account Takeover: không tự ý gán Google vào tài khoản mật khẩu có sẵn.
   - Chống Session Fixation: hủy phiên cũ và tái tạo session ID mới trước khi cấp đăng nhập.
   - Chặn quyền ADMIN qua đường đăng nhập Google.
4. Gỡ bỏ hoàn toàn modal demo giả lập và ô nhập email tự do trong `firebase-auth.js` (vá dứt điểm `bug-001`).
5. Đóng gói bảo vệ nút Google bằng `<c:if test="${googleEnabled}">` ở `login.jsp` và `register.jsp`.
6. Bổ sung kiểm thử đơn vị `GoogleTokenVerifierTest` (6 ca) và `AuthServletGoogleTest` (2 ca).

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước phase 2 | Sau phase 2 | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 32 pass / 0 fail | **40 pass / 0 fail** | ✅ |
| Biên dịch code (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Giả mạo idToken (`curl` bug-001) | — | Bị từ chối (`success:false`), không cấp phiên | ✅ |
| Đăng nhập mật khẩu `mocmien`/`123456` | Hoạt động bình thường | Tương thích ngược, không hồi quy | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria Phase 2

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Xác thực chữ ký `idToken` bằng máy chủ OIDC của Google | ✅ | Đã triển khai trong `GoogleTokenVerifier.java`, kiểm tra `exp`, `iss`, `aud`, `email_verified` và chữ ký số Google |
| Chặn đứng lỗ hổng `bug-001` (giả mạo bất kỳ email nào) | ✅ | Đã xóa modal demo; `AuthServlet` không đọc email/uid từ client param; `AuthServletGoogleTest` đã kiểm chứng |
| Không tự động liên kết Google vào tài khoản trùng email đã có | ✅ | Trả `success:false` kèm hướng dẫn đăng nhập mật khẩu rồi gắn Google ở trang hồ sơ |
| Chống Session Fixation khi đăng nhập thành công | ✅ | Đã gọi `session.invalidate()` và `request.getSession(true)` |
| Giữ nguyên hàng rào bảo vệ tài khoản ADMIN | ✅ | Kiểm tra `user.isAdmin()` trả `success:false` ngay lập tức |
| Nút Google ẩn/hiện an toàn theo cấu hình | ✅ | Bọc thẻ `<c:if test="${googleEnabled}">` tại JSP |
| Không lộ API key hay secret trong git | ✅ | `google.properties` nằm trong `.gitignore`; kiểm tra `git grep "AIza"` ra 0 kết quả |
| Toàn bộ test tự động chạy sạch | ✅ | 40/40 tests pass (10 test suites) |

---

## 🐞 Lỗi phát hiện (nếu có)

| # | Mức độ | Mô tả | Vị trí / File | Đề xuất sửa |
|---|---|---|---|---|
| - | Không có | Mọi tiêu chí an toàn và nghiệp vụ đều đạt chuẩn | - | - |

---

## 📋 Action Items → Đưa vào Phase 3

- [ ] **AI-1:** Xây dựng giao diện và luồng Gắn / Gỡ tài khoản Google tại trang Hồ sơ cá nhân (`/user?action=edit`).
- [ ] **AI-2:** Kiểm tra điều kiện an toàn nghiêm ngặt: Chặn người dùng gỡ liên kết Google nếu tài khoản chưa từng thiết lập mật khẩu (`hasPassword == false`), tránh trường hợp tự khóa tài khoản vĩnh viễn.

---

## 🏁 Kết luận (Verdict)

- **Có cho chuyển sang Phase 3 không?** Có (Đủ điều kiện chuyển Phase 3)
- **Ghi chú:** Phase 2 đã giải quyết triệt để vấn đề bảo mật `bug-001`, hệ thống xác thực Google qua idToken hoạt động chuẩn xác và an toàn. Sẵn sàng cho Phase 3.
