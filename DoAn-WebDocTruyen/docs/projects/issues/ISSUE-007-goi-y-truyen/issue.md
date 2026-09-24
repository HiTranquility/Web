# 🏷️ ISSUE-007: Gợi ý truyện thông minh — Độc giả đọc truyện này cũng đọc

> **Đích:** `docs/projects/issues/ISSUE-007-goi-y-truyen/issue.md` — issue là **thư mục**: file này (`issue.md`) · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-007 |
| **Người làm** | Dev C — Admin & Data |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-17 |
| **CASE liên quan** | CASE 05 — Chi tiết truyện |
| **Đụng vào** | `truyen/dao/StoryDAO.java` · `truyen/controller/common/StoryServlet.java` · `views/common/story/detail.jsp` · `truyen/RecommendationTest.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

Tích hợp cơ chế gợi ý truyện thông minh đa chiều (Content-Based + Collaborative Filtering) tại trang chi tiết truyện:
1. **Có thể bạn cũng thích:** Truyện cùng thể loại (Content-based filtering qua `story_tags`).
2. **Độc giả đọc truyện này cũng đọc:** Truyện có cùng độc giả đọc/theo dõi nhiều nhất (Collaborative filtering qua `view_logs` và `bookmarks`).

- **Hiện tại:**
  - Chỉ có khối gợi ý cùng thể loại dựa vào số tag trùng lặp.
  - Chưa khai thác được kho dữ liệu quý giá từ nhật ký đọc (`view_logs`) và danh sách lưu truyện (`bookmarks`).
- **Sau khi xong:**
  - Trang chi tiết truyện hiển thị khối **"Độc giả đọc truyện này cũng đọc"**, đề xuất chính xác các tác phẩm được nhóm độc giả cùng sở thích đón nhận.
  - Tối ưu hiệu năng bằng 1 câu truy vấn SQL chuẩn kết hợp `UNION ALL` và `GROUP BY`, không tốn tài nguyên và không cần cài thư viện ngoài.

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] `StoryDAO` có phương thức `findAlsoRead(int storyId, int limit)` truy vấn đồng độc giả từ `view_logs` và `bookmarks`.
- [x] Không gợi ý chính truyện đang xem và chỉ gợi ý truyện đang có trạng thái `PUBLISHED`.
- [x] `StoryServlet` gọi `findAlsoRead` trong action `detail()` và nạp vào request attribute `alsoRead`.
- [x] `detail.jsp` hiển thị khối `Độc giả đọc truyện này cũng đọc` bằng `_card.jsp` tái sử dụng giao diện chuẩn.
- [x] Bộ kiểm thử `RecommendationTest.java` và toàn bộ 70 bài unit test chạy pass 100%.

---

## 3. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** `git checkout HEAD~1` hoặc revert commit tương ứng. Không đụng đến schema CSDL nên không cần script rollback CSDL.
