# 🏷️ ISSUE-009: API JSON RESTful cho ứng dụng ngoài & di động

> **Đích:** `docs/projects/issues/ISSUE-009-api-json/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-009 |
| **Người làm** | Dev C — API & Data |
| **Trạng thái** | 🟡 Đang làm |
| **Ngày mở** | 2026-09-18 |
| **CASE liên quan** | Tích hợp hệ thống — REST API |
| **Đụng vào** | `src/main/java/truyen/controller/api/ApiServlet.java` · `src/test/java/truyen/ApiServletTest.java` |

---

## 1. Làm cái gì, và vì sao (Goal)

Cung cấp giao diện lập trình ứng dụng (API) chuẩn JSON theo phương thức HTTP GET (Read-Only) để bên thứ ba hoặc ứng dụng mobile/Flutter/React Native có thể tiêu thụ dữ liệu truyện mà không cần dựng lại tầng CSDL.

**Các endpoint:**
1. `GET /api/stories`: Danh sách truyện kèm phân trang, tìm kiếm từ khóa và lọc thể loại.
2. `GET /api/story/{id}`: Thông tin chi tiết một tác phẩm kèm danh sách chương.
3. `GET /api/chapter/{id}`: Nội dung đầy đủ của một chương đọc.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Trả về JSON hợp lệ với `Content-Type: application/json; charset=UTF-8`.
- [ ] Dùng lại `StoryDAO` và `ChapterDAO` hiện có, không viết lại câu lệnh SQL trùng lặp.
- [ ] Xử lý an toàn khi ID không tồn tại hoặc sai định dạng (`404 Not Found`, `400 Bad Request`).
- [ ] Có bài kiểm thử tự động `ApiServletTest.java` đạt 100% OK.
