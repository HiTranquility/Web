# 📚 TỔNG HỢP TOÀN BỘ HỆ THỐNG WEB ĐỌC TRUYỆN

> **Tài liệu tổng hợp kiến trúc, bản đồ trang, tính năng lõi và các phân hệ nâng cao đã xây dựng.**  
> Cập nhật: 2026-09-18 · **Phiên bản:** Hoàn thiện 18/18 ISSUE · **Độ phủ test:** 97/97 tests PASS (100%).

---

## 📑 MỤC LỤC
1. [Tổng quan Đồ án & Nền tảng Kỹ thuật](#1-tổng-quan-đồ-án--nền-tảng-kỹ-thuật)
2. [Bản đồ 33 Trang Web (Sitemap & Page Catalog)](#2-bản-đồ-33-trang-web-sitemap--page-catalog)
3. [Danh mục Tính năng Lõi (Core Features)](#3-danh-mục-tính-năng-lõi-core-features)
4. [Hệ thống 18 Tính năng Nâng cao (Advanced Modules)](#4-hệ-thống-18-tính-năng-nâng-cao-advanced-modules)
5. [Thiết kế Cơ sở Dữ liệu (14 Bảng CSDL)](#5-thiết-kế-cơ-sở-dữ-liệu-14-bảng-csdl)
6. [Bảo mật & Trợ năng (Security & Accessibility)](#6-bảo-mật--trợ-năng-security--accessibility)
7. [Hệ thống Kiểm thử Tự động (97 Tests)](#7-hệ-thống-kiểm-thử-tự-động-97-tests)
8. [Hướng dẫn Vận hành Nhanh](#8-hướng-dẫn-vận-hành-nhanh)

---

## 1. Tổng quan Đồ án & Nền tảng Kỹ thuật

| Thành phần | Đặc tả kỹ thuật |
|---|---|
| **Tên đồ án** | Xây dựng website đọc truyện trực tuyến có quản lý nội dung và phân quyền người dùng |
| **Tên ứng dụng** | **ĐọcTruyện — Hi Tranquility** |
| **Mô hình kiến trúc** | **MVC Model 2 (Model – View – Controller)** thuần Java, Server-side Rendering (SSR) |
| **Công nghệ lõi** | Java Servlet 3.1, JSP 2.3, JSTL 1.2, HikariCP Connection Pool |
| **Cơ sở dữ liệu** | MySQL 8.0 (InnoDB, `utf8mb4_unicode_ci`, FULLTEXT Indexing) |
| **Máy chủ Web/App** | Apache Tomcat 9.0 (Embedded / Standalone) |
| **Frontend** | Vanilla CSS (Kiến trúc 4 tầng tinh gọn), Vanilla JS (PWA, Service Worker, FileReader) |
| **Tích hợp bên thứ 3** | Google OAuth / Firebase OIDC, Google reCAPTCHA v3, Google Drive API v3, JavaMail SMTP |
| **Quy mô mã nguồn** | **60+ lớp Java**, **50+ file JSP**, **14 bảng CSDL**, **97 bài Unit Test** |

---

## 2. Bản đồ 33 Trang Web (Sitemap & Page Catalog)

Toàn bộ ứng dụng sử dụng **4 khung Layout chuẩn mực** để đảm bảo tính nhất quán:
- `main.jsp`: Khung chính (Nav kính mờ + Shell nội dung + Footer)
- `auth.jsp`: Khung xác thực tối giản căn giữa màn hình
- `reader.jsp`: Khung đọc truyện chuyên biệt (tối ưu thị giác, font có chân, không phân tâm)
- `admin.jsp`: Khung bảng điều khiển quản trị (Sidebar trái + Header quản trị)

### A. Nhóm Công cộng & Khám phá (Public & Discovery)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 1 | **Trang chủ** | `/` | `main` | `HomeServlet` | `common/home.jsp` | Tất cả |
| 2 | **Kho truyện & Tìm kiếm** | `/story?action=list` | `main` | `StoryServlet` | `common/story/search.jsp` | Tất cả |
| 3 | **Chi tiết truyện** | `/story?action=detail&id={id}` | `main` | `StoryServlet` | `common/story/detail.jsp` | Tất cả |
| 4 | **Đọc chương truyện** | `/chapter?action=read&id={id}` | `reader` | `ChapterServlet` | `common/chapter/read.jsp` | Tất cả |
| 5 | **Bảng xếp hạng (Truyện & Tác giả)** | `/rank` hoặc `/rank?by=authors` | `main` | `RankServlet` | `common/rank.jsp` | Tất cả |
| 6 | **Hướng dẫn sử dụng** | `/page?name=guide` | `main` | `PageServlet` | `common/page/guide.jsp` | Tất cả |
| 7 | **Nội quy cộng đồng** | `/page?name=rules` | `main` | `PageServlet` | `common/page/rules.jsp` | Tất cả |
| 8 | **Hồ sơ tác giả công khai** | `/user?action=profile&id={id}` | `main` | `UserServlet` | `user/profile.jsp` | Tất cả |
| 9 | **Tải truyện Offline (.txt)** | `/download?storyId={id}` | *Raw Text* | `DownloadServlet` | *Stream trực tiếp* | Tất cả |
| 10 | **Sơ đồ trang SEO (XML Sitemap)** | `/sitemap.xml` | *XML* | `SitemapServlet` | *Stream XML* | Công cụ tìm kiếm |
| 11 | **Tệp chỉ dẫn Robots** | `/robots.txt` | *Text* | Static | `webapp/robots.txt` | Bot tìm kiếm |
| 12 | **Báo lỗi 404 (Not Found)** | `/error?code=404` | `main` | `ErrorServlet` | `error/404.jsp` | Tất cả |
| 13 | **Báo lỗi 500 (Server Error)** | `/error?code=500` | `main` | `ErrorServlet` | `error/500.jsp` | Tất cả |

### B. Nhóm Xác thực & Tài khoản (Authentication)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 14 | **Đăng nhập** | `/auth?action=login` | `auth` | `AuthServlet` | `auth/login.jsp` | Khách |
| 15 | **Đăng ký thành viên** | `/auth?action=register` | `auth` | `AuthServlet` | `auth/register.jsp` | Khách |
| 16 | **Quên mật khẩu** | `/auth?action=forgot` | `auth` | `AuthServlet` | `auth/forgot.jsp` | Khách |
| 17 | **Đặt lại mật khẩu** | `/auth?action=reset&token={token}` | `auth` | `AuthServlet` | `auth/reset.jsp` | Khách |

### C. Nhóm Thành viên & Độc giả (Member & Reader Dashboard)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 18 | **Trung tâm cá nhân (Hồ sơ & Ví)** | `/user?action=me` | `main` | `UserServlet` | `user/me.jsp` | Thành viên |
| 19 | **Chỉnh sửa thông tin cá nhân** | `/user?action=edit` | `main` | `UserServlet` | `user/edit.jsp` | Thành viên |
| 20 | **Đổi mật khẩu tài khoản** | `/user?action=password` | `main` | `UserServlet` | `user/edit.jsp` | Thành viên |
| 21 | **Truyện đã lưu (Bookmarks)** | `/bookmark?action=list` | `main` | `BookmarkServlet` | `user/bookmarks.jsp` | Thành viên |
| 22 | **Lịch sử đọc truyện** | `/history` | `main` | `HistoryServlet` | `user/history.jsp` | Thành viên |
| 23 | **Tác giả đang theo dõi** | `/follow?action=list` | `main` | `FollowServlet` | `user/following.jsp` | Thành viên |
| 24 | **Hộp thông báo** | `/notification?action=list` | `main` | `NotificationServlet` | `user/notifications.jsp` | Thành viên |
| 25 | **Gửi báo cáo vi phạm** | `/report?action=create` | `main` | `ReportServlet` | `user/report.jsp` | Thành viên |

### D. Nhóm Tác giả & Sáng tác (Author Studio)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 26 | **Tủ truyện của tôi** | `/story?action=mine` | `main` | `StoryServlet` | `user/story/mine.jsp` | Thành viên |
| 27 | **Đăng / Sửa truyện (Kèm Live Preview)** | `/story?action=create` hoặc `edit` | `main` | `StoryServlet` | `user/story/form.jsp` | Tác giả / Admin |
| 28 | **Danh sách chương của truyện** | `/chapter?action=mine&storyId={id}` | `main` | `ChapterServlet` | `user/chapter/mine.jsp` | Tác giả / Admin |
| 29 | **Soạn thảo / Sửa chương truyện** | `/chapter?action=create` hoặc `edit` | `editor` | `ChapterServlet` | `user/chapter/form.jsp` | Tác giả / Admin |

### E. Nhóm Quản trị viên (Admin Dashboard)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 30 | **Bảng điều khiển & Thống kê tổng** | `/admin/dashboard` | `admin` | `AdminDashboardServlet` | `admin/dashboard.jsp` | Quản trị viên |
| 31 | **Quản lý toàn bộ truyện** | `/admin/story` | `admin` | `AdminStoryServlet` | `admin/stories.jsp` | Quản trị viên |
| 32 | **Quản lý tài khoản & Khóa người dùng** | `/admin/user` | `admin` | `AdminUserServlet` | `admin/users.jsp` | Quản trị viên |
| 33 | **Quản lý thể loại (Tags)** | `/admin/tag` | `admin` | `AdminTagServlet` | `admin/tags.jsp` | Quản trị viên |
| 34 | **Quản lý báo cáo vi phạm** | `/admin/report` | `admin` | `AdminReportServlet` | `admin/reports.jsp` | Quản trị viên |
| 35 | **Kiểm duyệt bình luận** | `/admin/comment` | `admin` | `AdminCommentServlet` | `admin/comments.jsp` | Quản trị viên |

---

## 3. Danh mục Tính năng Lõi (Core Features)

### 1. Trải nghiệm Đọc truyện Chuyên sâu
- **Giao diện Reader chuyên biệt:** Tự động ẩn menu, cố định độ rộng cột văn bản (38em) chuẩn khoa học thị giác giúp chống mỏi mắt.
- **Tùy biến môi trường đọc:** Chuyển đổi Dark Mode / Light Mode tức thì, chỉnh cỡ chữ linh hoạt (A- / A+), giãn dòng, lựa chọn phông Serif / Sans-serif.
- **Điều hướng phím tắt:** Bấm `←` (chương trước) hoặc `→` (chương sau) trên bàn phím.
- **Tự động lưu tiến độ đọc:** Ghi nhận lịch sử đọc vào CSDL và LocalStorage, nút *"Đọc tiếp"* thông minh tại trang chủ dẫn thẳng đến chương và % vị trí đang đọc dở.

### 2. Sáng tác & Quản lý Truyện (Author Studio)
- **Quy trình xuất bản chuẩn mực:** Tạo truyện, phân loại đa thể loại, cập nhật trạng thái (*Đang ra* / *Hoàn thành*).
- **Trình soạn thảo tập trung:** Bề ngang rộng, ô soạn thảo chiếm 60vh, đếm số từ thời gian thực, tự động sinh slug URL thân thiện.
- **Tải ảnh bìa & Preview trực tiếp:** Tải file ảnh từ máy tính (`multipart/form-data`), xem trước tức thì với FileReader API tỉ lệ 3:4 chuẩn bìa sách.

### 3. Tương tác Cộng đồng & Xã hội
- **Bình luận đa cấp (Nested Comments):** Bình luận theo đầu truyện hoặc thảo luận chi tiết dưới chân từng chương đọc. Cho phép trả lời lồng nhau, tác giả trả lời có huy hiệu nổi bật.
- **Thả tim bình luận:** Tương tác nhanh với các bình luận tâm đắc.
- **Chấm sao đánh giá:** Thang điểm 1–5 sao với ràng buộc khoá chính kép (mỗi người chấm 1 lần/truyện).
- **Theo dõi tác giả (Follow):** Nhận thông báo tự động ngay khi tác giả đăng chương mới.

---

## 4. Hệ thống 18 Tính năng Nâng cao (Advanced Modules)

Dự án đã giải quyết trọn vẹn lộ trình 18 ISSUE công nghệ cao:

| Mã | Tên Module Nâng cao | Điểm sáng kỹ thuật |
|---|---|---|
| **ISSUE-001** | **Tích hợp Google Toàn diện** | Xác thực `idToken` Google OIDC ở backend; Bảo vệ tài khoản Unlink Guard (phải có mật khẩu mới cho gỡ Google); reCAPTCHA v3; Sao lưu truyện lên Google Drive API v3. |
| **ISSUE-002** | **Gửi Email SMTP Thật (JavaMail)** | Tích hợp thư viện JavaMail, gửi email đặt lại mật khẩu với giao diện HTML thương hiệu sang trọng. Hỗ trợ gửi bất đồng bộ qua hàng đợi và chế độ mô phỏng an toàn (Dev Simulation). |
| **ISSUE-003** | **Chặn Dò Mật khẩu & Spam** | Thuật toán cửa sổ trượt (Sliding Window in-memory) luồng an toàn: Tự động khóa IP/tài khoản 15 phút sau 5 lần thử sai; Cooldown 20 giây giữa các lượt bình luận. |
| **ISSUE-004** | **Bình luận theo từng Chương** | Mở rộng CSDL với `chapter_id`, nạp danh sách thảo luận chân chương, liên kết bình luận cha-con mượt mà không tải lại trang. |
| **ISSUE-005** | **Tìm kiếm Sâu trong Nội dung Chương** | Chỉ mục `FULLTEXT (title, content)` trong MySQL. Tự động trích xuất ngữ cảnh xung quanh từ khóa và bôi đậm (`<mark>`) trước khi hiển thị. |
| **ISSUE-006** | **Trang Tác giả & Bảng Xếp hạng Tác giả** | Trang hồ sơ công khai `/user?action=profile&id={id}`, thống kê tổng lượt đọc mọi truyện, số người theo dõi. Bổ sung tab xếp hạng tác giả xuất sắc `/rank?by=authors`. |
| **ISSUE-007** | **Gợi ý Truyện Thông minh** | Thuật toán lai ghép (Hybrid Recommendation): Gợi ý cùng thể loại kết hợp Lọc cộng tác (Collaborative Filtering: *"Người đọc truyện này cũng đọc"*). |
| **ISSUE-008** | **Ví Xu Ảo & Tặng thưởng Tác giả** | Bảng `wallets` và `transactions`. Cơ chế giao dịch nguyên tử (ACID Transaction) chuyển xu ảo ủng hộ tác giả, gửi tặng hoa, nạp xu trải nghiệm. |
| **ISSUE-009** | **RESTful JSON API Chuẩn Quốc tế** | Cung cấp endpoints `/api/stories`, `/api/story/{id}`, `/api/chapter/{id}` với CORS Headers đầy đủ, phục vụ ứng dụng di động hoặc bên thứ ba. |
| **ISSUE-010** | **PWA & Khả năng Đọc Ngoại tuyến** | Web App Manifest đạt chuẩn cài đặt HomeScreen; Service Worker chiến lược Cache-First cho tài nguyên tĩnh và nội dung chương đã mở. |
| **ISSUE-011** | **Kiến trúc CSS Phân tầng Tinh gọn** | Phân tầng 4 lớp: `base.css` (biến màu, reset) → `components.css` (thành phần chung) → `layout-*.css` (chuyên biệt cho Auth, Reader, Admin) → `pageCss`. Tiết kiệm băng thông tải trang. |
| **ISSUE-012** | **SEO Động, Sitemap & OpenGraph** | Tự động sinh `sitemap.xml` theo chuẩn sitemaps.org; Cung cấp `robots.txt`; Tự động sinh thẻ `og:title`, `og:image`, `og:description` khi chia sẻ lên MXH. |
| **ISSUE-013** | **Dọn dẹp `view_logs` Định kỳ** | Background thread chạy ngầm lúc khởi động máy chủ (AppListener), tự động dọn dẹp các dòng log vô danh quá 90 ngày, bảo vệ hiệu năng CSDL. |
| **ISSUE-014** | **Nâng Độ phủ Test Toàn diện** | Mở rộng bộ kiểm thử tự động từ 28 bài lên 97 bài test, bao phủ toàn bộ các DAO nghiệp vụ, Servlets, Filters và các trường hợp biên. |
| **ISSUE-015** | **Trải nghiệm Mobile 360px & Trợ năng A11y** | Hoàn thiện layout cho màn hình từ 360px; Vùng chạm ngón tay tối thiểu 44px (chuẩn WCAG 2.1); Hỗ trợ `:focus-visible`, `.skip-link` và tôn trọng `prefers-reduced-motion`. |
| **ISSUE-016** | **Trang Báo lỗi 404 & 500 Tùy biến** | Bắt lỗi tập trung qua `ErrorServlet`, giao diện đồng bộ phong cách truyện, che giấu stack trace nhạy cảm của máy chủ. |
| **ISSUE-017** | **Live Preview Ảnh Bìa Truyện** | Khung xem trước bìa sách 3:4 trực quan khi người dùng chọn ảnh từ máy tính trước khi bấm lưu. |
| **ISSUE-018** | **Tiện ích Chia sẻ Truyện 1 Chạm** | Nút sao chép liên kết truyện vào clipboard kèm Toast thông báo nổi tức thì; Phím tắt chia sẻ Facebook/Twitter nhanh chóng. |

---

## 5. Thiết kế Cơ sở Dữ liệu (14 Bảng CSDL)

Database: `webdoctruyen` (Charset: `utf8mb4`, Collate: `utf8mb4_unicode_ci`)

```text
[users] ───────────────┬───────────────────────────────┐
  │ 1                  │ 1                             │ 1
  ▼ N                  ▼ N                             ▼ N
[stories]          [user_identities]               [wallets]
  │ 1                  (Google OAuth OIDC)             (Ví xu ảo)
  ├────────────────┬───────────────────┐
  ▼ N              ▼ N                 ▼ N
[chapters]     [story_tags]        [bookmarks]
  │ 1              (N-N tags)          (Vị trí đọc dở)
  ▼ N
[comments]
```

### Chi tiết 14 Bảng:
1. **`users`**: Tài khoản người dùng (id, username, email, password_hash, display_name, avatar_url, bio, role, status, ban_reason, created_at).
2. **`user_identities`**: Liên kết tài khoản mạng xã hội (Google Provider UID, email, tên hiển thị, avatar).
3. **`stories`**: Thông tin truyện (id, title, slug, description, cover_url, author_id, status, progress, view_count, rating_sum, rating_count).
4. **`chapters`**: Nội dung chương (`content` kiểu `MEDIUMTEXT`, `FULLTEXT (title, content)`).
5. **`tags`**: Danh mục thể loại (id, name, slug, description).
6. **`story_tags`**: Bảng nối nhiều-nhiều giữa truyện và thể loại.
7. **`bookmarks`**: Truyện đã lưu và vị trí chương đọc dở (`last_chapter_id`).
8. **`comments`**: Bình luận truyện & chương (`story_id`, `chapter_id`, `parent_id`, `likes_count`).
9. **`ratings`**: Chấm sao đánh giá (khóa chính kép `user_id, story_id`, thang điểm 1–5).
10. **`follows`**: Theo dõi tác giả (`follower_id, author_id`).
11. **`reports`**: Báo cáo vi phạm nội dung (`reporter_id, target_type, target_id, reason, status`).
12. **`notifications`**: Thông báo người dùng (`user_id, story_id, type, message, is_read`).
13. **`view_logs`**: Nhật ký mở đọc truyện (`user_id, story_id, chapter_id, ip_address, viewed_at`).
14. **`wallets` & `transactions`**: Ví xu ảo và lịch sử chuyển xu ủng hộ tác giả.

---

## 6. Bảo mật & Trợ năng (Security & Accessibility)

### Các chốt chặn bảo mật đa lớp:
- **Chống SQL Injection:** 100% câu truy vấn qua DAO dùng `PreparedStatement` với ràng buộc tham số `?`.
- **Chống XSS:** Toàn bộ dữ liệu hiển thị trên JSP được escape bằng `<c:out value="..."/>`.
- **Bảo vệ CSRF:** Token CSRF sinh theo phiên (`sessionScope.csrfToken`) và kiểm tra tự động qua `CsrfFilter` cho mọi request POST/PUT/DELETE.
- **Băm mật khẩu PBKDF2WithHmacSHA256:** Kèm muối ngẫu nhiên (salt) 16 bytes và 65.536 vòng lặp, ngăn chặn bảng cầu vồng (Rainbow Table).
- **Phân quyền 2 tầng:** `AuthFilter` và `AdminFilter` chặn theo URL; Servlet kiểm tra quyền sở hữu bản ghi (`authorId == currentUser.id`).
- **Phòng chống Brute-force & Spam:** Thuật toán Sliding Window giới hạn đăng nhập và bình luận.

### Trợ năng (A11y - WCAG 2.1):
- **Phím Tab thân thiện:** Hiển thị viền `:focus-visible` màu thương hiệu rõ nét.
- **Skip-to-content:** Lớp `.skip-link` giúp độc giả khiếm thị dùng bàn phím nhảy thẳng vào nội dung chính.
- **Vùng chạm di động:** Đạt kích thước tối thiểu 44px trên smartphone.
- **Giảm chuyển động:** Tự động vô hiệu hóa hiệu ứng chuyển động khi hệ điều hành bật chế độ `prefers-reduced-motion`.

---

## 7. Hệ thống Kiểm thử Tự động (97 Tests)

Kiểm thử tự động thực thi qua công cụ JUnit 5 Console Standalone, độc lập môi trường mạng và CSDL:

```text
JUnit Platform Suite: 97 tests found, 97 successful, 0 failed.
Thời gian thực thi: ~1.46 giây.
```

### Các nhóm test case tiêu biểu:
- **`GoogleTokenVerifierTest` (6 tests):** Kiểm thử xác thực token JWT, token hết hạn, sai Client ID, tài khoản chưa xác minh email.
- **`AuthServletGoogleTest` (2 tests):** Ngăn chặn giả mạo tài khoản qua `idToken` rác (Vá lỗ hổng Bug-001).
- **`UnlinkGuardTest` (4 tests):** Bảo vệ tài khoản: Không cho phép gỡ liên kết Google nếu chưa đặt mật khẩu dự phòng.
- **`RateLimiterTest` (6 tests):** Kiểm thử khóa tài khoản sau 5 lần thử sai và cooldown 20s khi bình luận.
- **`MailSenderTest` (4 tests):** Kiểm thử dịch vụ gửi email SMTP, hàng đợi bất đồng bộ và chế độ Dev an toàn.
- **`ChapterCommentTest` (3 tests):** Kiểm thử bình luận theo chương và quan hệ trả lời lồng nhau.
- **`RecommendationTest` (3 tests):** Kiểm thử thuật toán gợi ý truyện tương đồng và người đọc cùng đọc.
- **`WalletTest` (4 tests):** Kiểm thử số dư ví, nạp xu, logic chuyển xu nguyên tử, tìm ví và lịch sử giao dịch.
- **`ApiServletTest` (2 tests):** Kiểm thử JSON serialization, mã hoá an toàn chuỗi đặc biệt.
- **`ErrorServletTest` (2 tests):** Kiểm thử bắt lỗi 404 và 500, điều hướng view tương ứng.
- **`StoryDAOTest` & `ViewLogDAOTest` (5 tests):** Kiểm thử truy vấn phân trang an toàn, xử lý ID âm/0, dọn dẹp log.
- **`PasswordUtilTest`, `SlugUtilTest`, `ChapterToTxtTest` (40+ tests):** Kiểm thử các hàm thuật toán cốt lõi.

---

## 8. Hướng dẫn Vận hành Nhanh

### 1. Khởi động Máy chủ Ứng dụng
```powershell
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```
- Ứng dụng mở tại: **`http://localhost:8080/`**
- Tài khoản quản trị viên mặc định: `admin` / `admin123`
- Tài khoản tác giả thử nghiệm: `tacgia1` / `MatKhau123!`

### 2. Chạy Kiểm thử Tự động
```powershell
powershell -ExecutionPolicy Bypass -File scripts\test.ps1
```

### 3. Xem Bảng theo dõi ISSUES
- Truy cập thư mục tài liệu: [`docs/projects/issues/README.md`](projects/issues/README.md)
