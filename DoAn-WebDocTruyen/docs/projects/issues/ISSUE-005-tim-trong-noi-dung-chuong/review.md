# 🔍 [Review] Tìm kiếm sâu trong nội dung chương — Nghiệm thu ISSUE-005

> **Đích:** `docs/projects/issues/ISSUE-005-tim-trong-noi-dung-chuong/review.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-005 |
| **Review cho** | ISSUE-005: Tìm kiếm sâu trong nội dung chương truyện |
| **Người làm** | Dev B |
| **Trạng thái** | ✅ Đạt nghiệm thu |
| **Ngày hoàn thành** | 2026-09-18 |

---

## 1. Tóm tắt kết quả triển khai

- **Tầng DAO `ChapterDAO.java`:**
  - `searchContent(keyword, limit)`: hỗ trợ tìm kiếm FULLTEXT thông minh `MATCH(title, content) AGAINST (? IN BOOLEAN MODE)`.
  - `snippet(content, keyword)`: cắt trích đoạn ngữ cảnh ~180 ký tự quanh từ khóa xuất hiện.
- **Tầng Controller `StoryServlet.java`:**
  - Action `search`: nạp danh sách kết quả và chuyển tiếp tới `views/common/story/search.jsp`.
- **Giao diện `views/common/story/search.jsp`:**
  - Hỗ trợ chuyển đổi nhanh giữa tìm kiếm theo tên truyện và tìm kiếm theo nội dung chương.
