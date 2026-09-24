# 🏷️ ISSUE-004: Bình luận theo từng chương (Chapter Comments)

> **Đích:** `docs/projects/issues/ISSUE-004-binh-luan-theo-chuong/issue.md` — issue là **thư mục**: file này (`issue.md`) · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-004 |
| **Người làm** | Dev B — Reader & Story |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-17 |
| **CASE liên quan** | CASE 06 — Chương truyện & CASE 07 — Bình luận |
| **Đụng vào** | `database/migration-004-chapter-comments.sql` · `truyen/model/Comment.java` · `truyen/dao/CommentDAO.java` · `truyen/controller/user/CommentServlet.java` · `truyen/controller/common/ChapterServlet.java` · `views/_partials/_comment.jsp` · `views/common/chapter/read.jsp` · `truyen/ChapterCommentTest.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

Cho phép độc giả thảo luận, gửi cảm nghĩ, trả lời (reply) và tương tác thả tim ngay dưới chân mỗi chương đọc cụ thể.

- **Hiện tại:**
  - Bình luận mới chỉ có ở cấp độ Truyện (trang chi tiết truyện `/story?action=detail&id=...`).
  - Độc giả đọc tới các cao trào, diễn biến bất ngờ của từng chương không có nơi chia sẻ cảm xúc ngay tại chương đó.
- **Sau khi xong:**
  - Dưới chân mỗi chương đọc (`/chapter?action=read&id=...#comments`) có khu vực thảo luận riêng.
  - Bình luận chương không bị lẫn lộn lên trang chi tiết truyện chung (tránh lộ trước spoiler cốt truyện cho người mới đọc).
  - Hỗ trợ trả lời (reply) phân cấp và thả tim bình luận chương hoạt động mượt mà.
  - Tác giả nhận được thông báo ghi rõ độc giả vừa bình luận vào chương nào.

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] Đã tạo script migration `database/migration-004-chapter-comments.sql` thêm cột `chapter_id INT NULL` có khóa ngoại và index.
- [x] Tầng Model `Comment.java` hỗ trợ `chapterId`, `chapterNo`, `isChapterComment()`.
- [x] Tầng DAO `CommentDAO` có `findByChapter(int chapterId)`, `countByChapter(int chapterId)`, và `findByStory(int storyId)` chỉ lấy bình luận cấp truyện.
- [x] Tầng Controller:
  - `ChapterServlet` nạp danh sách bình luận của chương và số lượng bình luận vào request attributes.
  - `CommentServlet` nhận `chapterId`, lưu vào DB và chuyển hướng về đúng trang chương (`#comments`).
  - Gửi thông báo kèm số chương cho tác giả / người được trả lời.
- [x] Tầng Giao diện:
  - `views/common/chapter/read.jsp` hiển thị tiêu đề thảo luận chương, form gửi bình luận, danh sách bình luận (dùng lại `_comment.jsp`).
  - Thả tim AJAX và trả lời (reply) hoạt động trơn tru.
- [x] Viết unit test `ChapterCommentTest.java` và toàn bộ 67 bài test của dự án chạy pass 100%.

---

## 3. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** `git checkout HEAD~1` hoặc revert commit liên quan.
- **Dữ liệu / CSDL:** Chạy script rollback có sẵn trong `database/migration-004-chapter-comments.sql`:
  ```sql
  ALTER TABLE comments DROP FOREIGN KEY fk_comments_chapter;
  ALTER TABLE comments DROP INDEX idx_comments_chapter;
  ALTER TABLE comments DROP COLUMN chapter_id;
  ```
