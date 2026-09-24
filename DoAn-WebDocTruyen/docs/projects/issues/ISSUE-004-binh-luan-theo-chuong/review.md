# 🔍 [Review] Bình luận theo từng chương — Nghiệm thu ISSUE-004

> **Đích:** `docs/projects/issues/ISSUE-004-binh-luan-theo-chuong/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | ISSUE-004: Bình luận theo từng chương |
| **Ngày review** | 2026-09-17 |
| **Người review** | Antigravity Pair Programmer & Developer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Đối chiếu trực tiếp với các mục tiêu và **Acceptance Criteria** đã cam kết trong `issue.md`:
1. Cơ sở dữ liệu: migration `chapter_id`, ràng buộc và index.
2. Backend: `Comment.java`, `CommentDAO.java`, `CommentServlet.java`, `ChapterServlet.java`.
3. Giao diện: `views/common/chapter/read.jsp`, `_comment.jsp`.
4. Kiểm thử: Suite unit test JUnit 5.

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước khi làm | Sau khi làm | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 64 pass / 0 fail | 67 pass / 0 fail (+3 test mới) | ✅ |
| Biên dịch code Java (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Luồng bình luận cấp truyện cũ (`/story?action=detail`) | Chạy bình thường | Đảm bảo không bị lẫn lộn bình luận chương | ✅ |
| Luồng bình luận theo chương mới (`/chapter?action=read`) | Chưa có | Đầy đủ form, danh sách, reply, like AJAX | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Script migration CSDL | ✅ | `database/migration-004-chapter-comments.sql` + script chạy nhanh `scripts/migrate-004.ps1` |
| Phân tách bình luận cấp truyện & chương | ✅ | `CommentDAO.findByStory()` lọc `chapter_id IS NULL`; `findByChapter()` lọc `chapter_id = ?` |
| Trả lời bình luận (Reply) phân cấp | ✅ | Kế thừa `chapterId` từ bình luận cha, gom nhóm vào cây 2 cấp `replies` |
| Thả tim bình luận chương | ✅ | Tận dụng cơ chế AJAX sẵn có trong `_comment.jsp` |
| Thông báo tác giả | ✅ | `NotificationDAO.sendNotification` được truyền kèm `chapterId` và hiển thị số chương |
| Bộ kiểm thử tự động | ✅ | Thêm `ChapterCommentTest.java` phủ các trường hợp logic cốt lõi |

---

## 🐞 Lỗi phát hiện khi kiểm thử

*Không phát hiện lỗi. Mã nguồn biên dịch sạch, các test JUnit 5 chạy hoàn hảo.*

---

## 🏁 Kết luận (Verdict)

- **Đóng ISSUE-004:** **CÓ** (Đã hoàn thành toàn bộ yêu cầu)
- **Lời nhắn:** Tính năng đã sẵn sàng trên cả backend và frontend. Database đã chuẩn bị sẵn migration script. Chuyển sang tính năng tiếp theo trong danh sách ưu tiên.
