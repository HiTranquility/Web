# 🏷️ ISSUE-006: Trang tác giả công khai & Bảng xếp hạng tác giả nổi bật

> **Đích:** `docs/projects/issues/ISSUE-006-trang-tac-gia-cong-khai/issue.md` — issue là **thư mục**: file này (`issue.md`) · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-006 |
| **Người làm** | Dev B — Story & Reader Experience |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-17 |
| **CASE liên quan** | CASE 05 — Bảng xếp hạng · CASE 14/15 — Hồ sơ người dùng & Tác giả |
| **Đụng vào** | `truyen/model/User.java` · `truyen/dao/UserDAO.java` · `truyen/util/DemoData.java` · `truyen/controller/common/RankServlet.java` · `views/common/rank.jsp` · `views/_partials/_card.jsp` · `assets/css/components.css` · `truyen/AuthorRankTest.java` *(mới)* |

---

## 1. Làm cái gì, và vì sao (Goal)

Nâng cao trải nghiệm cộng đồng độc giả và vinh danh những người sáng tác 🟢:
1. **Bảng xếp hạng tác giả nổi bật (`/rank?by=authors`):** Trước đây bảng xếp hạng chỉ có xếp theo truyện (lượt xem tuần, tháng, điểm đánh giá, số chương, mới đăng). Độc giả không có cách nào tìm kiếm các tác giả viết hay và uy tín nhất trên nền tảng.
2. **Liên kết tác giả thông suốt từ thẻ truyện (`_card.jsp`):** Khi xem danh sách truyện ở trang chủ, kho truyện, gợi ý, độc giả có thể click trực tiếp vào tên tác giả trên thẻ truyện để xem ngay toàn bộ tác phẩm của tác giả đó (`/user?action=profile&id=...`).

- **Hiện tại:**
  - `RankServlet` chỉ chấp nhận các kiểu xếp hạng truyện (`views`, `week`, `month`, `chapters`, `newest`, `rating`).
  - `UserDAO` chưa có câu truy vấn tổng hợp lượt xem và người theo dõi của tác giả.
  - Tên tác giả trên thẻ truyện `_card.jsp` chỉ là text tĩnh không click được.
- **Sau khi xong:**
  - Bổ sung trường `totalViews` vào model `User.java`.
  - Bổ sung `findTopAuthors(limit)` vào `UserDAO.java` và `DemoData.topAuthors(limit)` cho chế độ xem demo.
  - Bổ sung tab `✍️ Tác giả nổi bật` (`by=authors`) vào `RankServlet.java` và giao diện `rank.jsp`.
  - Thiết kế thẻ xếp hạng tác giả với huy hiệu top 3, avatar chữ cái đầu, số truyện, tổng lượt xem, người theo dõi và nút "Xem hồ sơ".
  - Cho phép click tên tác giả trên mọi thẻ truyện `_card.jsp` để điều hướng nhanh đến trang cá nhân.

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] Thêm thuộc tính `totalViews` vào JavaBean `User.java`.
- [x] Triển khai `DemoData.topAuthors(limit)` tổng hợp số truyện, lượt xem và người theo dõi.
- [x] Thêm `UserDAO.findTopAuthors(limit)` với câu lệnh SQL GROUP BY và ORDER BY `total_views DESC, follower_count DESC`.
- [x] Cập nhật `RankServlet.java` tiếp nhận `by=authors` và chuyển danh sách tác giả cho view.
- [x] Giao diện `views/common/rank.jsp` bổ sung tab và khối hiển thị bảng xếp hạng tác giả thẩm mỹ cao.
- [x] Cập nhật `_partials/_card.jsp` và CSS để bấm vào tên tác giả chuyển tới hồ sơ tác giả.
- [x] Bộ kiểm thử tự động `AuthorRankTest.java` (4 test cases) chạy pass 100%.

---

## 3. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** Hoàn tác các file mã nguồn đã sửa đổi. CSDL không thay đổi cấu trúc bảng nên không cần script rollback SQL.
