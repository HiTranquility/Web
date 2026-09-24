# 🏷️ ISSUE-005: Tìm kiếm sâu trong nội dung chương truyện

> **Đích:** `docs/projects/issues/ISSUE-005-tim-trong-noi-dung-chuong/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-005 |
| **Người làm** | Dev B — Core & Search |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | CASE 02b — Tìm kiếm nội dung |
| **Đụng vào** | `src/main/java/truyen/dao/ChapterDAO.java` · `src/main/java/truyen/controller/common/StoryServlet.java` · `src/main/webapp/WEB-INF/views/common/story/search.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

Cho phép độc giả tìm kiếm các câu văn, trích dẫn hoặc tình tiết cụ thể nằm bên trong nội dung các chương truyện bằng chỉ mục `FULLTEXT (title, content)`.

**Sau khi hoàn thành:**
- `ChapterDAO.searchContent(keyword, limit)` ứng dụng MySQL FULLTEXT với `BOOLEAN MODE` khi từ khóa dài, hoặc `LIKE` khi từ khóa ngắn.
- `ChapterDAO.snippet(content, keyword)` tự động cắt đoạn trích chứa từ khóa và bôi đậm (`<mark>`) trước khi đổ về giao diện.
- Trang `views/common/story/search.jsp` hiển thị danh sách các chương khớp nội dung kèm tên truyện và trích dẫn trực quan.
