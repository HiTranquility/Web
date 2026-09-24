# 🔍 [Review] Chặn dò mật khẩu và chống spam bình luận — Nghiệm thu ISSUE-003

> **Đích:** `docs/projects/issues/ISSUE-003-chan-do-mat-khau-va-spam/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | ISSUE-003: Chặn dò mật khẩu (Brute-force) và chống spam bình luận |
| **Ngày review** | 2026-09-17 |
| **Người review** | Antigravity Pair Programmer & Developer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Đối chiếu trực tiếp với các mục tiêu và **Acceptance Criteria** đã cam kết trong `issue.md`:
1. Bảo mật: Ngăn chặn triệt để hành vi brute-force tấn công dò mật khẩu tài khoản.
2. Trải nghiệm người dùng: Thông báo trực quan số lần thử còn lại và số phút chờ khi bị khóa.
3. Chống spam: Giới hạn tần suất gửi bình luận tối thiểu 20 giây giữa 2 lượt liên tiếp.
4. Kiểm thử: `RateLimiterTest.java` phủ kín các kịch bản khóa, giải phóng, cô lập người dùng và cooldown bình luận.

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước khi làm | Sau khi làm | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 74 pass / 0 fail | 80 pass / 0 fail (+6 test mới) | ✅ |
| Biên dịch code Java (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Chặn brute-force đăng nhập | Không giới hạn | Khóa 15 phút sau 5 lần sai | ✅ |
| Giới hạn tần suất bình luận | Bấm liên tục không cản | Cooldown 20s, báo `flashWarn` | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Cơ chế trượt thời gian (Sliding Window) | ✅ | Triển khai trong `RateLimiter.java`, thread-safe với `ConcurrentHashMap` |
| Tích hợp AuthServlet | ✅ | Kiểm tra trước khi truy vấn mật khẩu, reset sau khi đăng nhập thành công |
| Cảnh báo số lần còn lại | ✅ | Báo rõ "Còn N lần thử", vượt quá báo số phút còn lại phải chờ |
| Tích hợp CommentServlet | ✅ | Kiểm tra `isCommentSpam()`, trả về cảnh báo `flashWarn` trên layout chính |
| Tự động thu dọn bộ nhớ | ✅ | `cleanExpired` loại bỏ mốc thời gian ngoài cửa sổ trượt |
| Bộ kiểm thử tự động | ✅ | 6 ca kiểm thử trong `RateLimiterTest.java` chạy thành công tuyệt đối |

---

## 🏁 Kết luận (Verdict)

- **Đóng ISSUE-003:** **CÓ** (Giải quyết hoàn tất vấn đề an ninh mức 🔴)
- **Lời nhắn:** Hệ thống đã được gia cố vững chắc trước các cuộc tấn công quét mật khẩu tự động và hành vi spam bình luận.
