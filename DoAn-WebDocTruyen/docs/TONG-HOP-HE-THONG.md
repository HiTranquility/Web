# 📚 TỔNG HỢP TOÀN BỘ HỆ THỐNG WEB ĐỌC TRUYỆN

> **Tài liệu tổng hợp kiến trúc, bản đồ trang, danh mục chức năng đầy đủ và 18 phân hệ nâng cao đã xây dựng.**  
> Cập nhật: 2026-09-28 · **Phiên bản:** Hoàn thiện 18/18 ISSUE · **Độ phủ test:** 114/114 tests PASS (100%).

---

## 📑 MỤC LỤC
1. [Tổng quan Đồ án & Nền tảng Kỹ thuật](#1-tổng-quan-đồ-án--nền-tảng-kỹ-thuật)
2. [Đối chiếu Chức năng: Đề xuất Ban đầu vs Hệ thống Hiện tại](#2-đối-chiếu-chức-năng-đề-xuất-ban-đầu-vs-hệ-thống-hiện-tại)
3. [Bản đồ Hệ thống: 36 Trang Web & 7 Endpoints Dịch vụ](#3-bản-đồ-hệ-thống-36-trang-web--7-endpoints-dịch-vụ)
4. [Danh mục 38 Chức năng Toàn diện (Full Feature Catalog)](#4-danh-mục-38-chức-năng-toàn-diện-full-feature-catalog)
5. [Hệ thống 18 Module Công nghệ Nâng cao (ISSUE-001 → ISSUE-018)](#5-hệ-thống-18-module-công-nghệ-nâng-cao-issue-001--issue-018)
6. [Thiết kế Cơ sở Dữ liệu (14 Bảng CSDL)](#6-thiết-kế-cơ-sở-dữ-liệu-14-bảng-csdl)
7. [Bảo mật & Trợ năng (Security & Accessibility)](#7-bảo-mật--trợ-năng-security--accessibility)
8. [Hệ thống Kiểm thử Tự động (114 Tests)](#8-hệ-thống-kiểm-thử-tự-động-114-tests)
9. [Hướng dẫn Vận hành Nhanh](#9-hướng-dẫn-vận-hành-nhanh)

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
| **Frontend** | Vanilla CSS (Kiến trúc 4 tầng tinh gọn, Mobile 360px), Vanilla JS (PWA, Service Worker, FileReader) |
| **Tích hợp bên thứ 3** | Google OAuth / Firebase OIDC, Google reCAPTCHA v3, Google Drive API v3, JavaMail SMTP |
| **Quy mô mã nguồn** | **77 lớp Java**, **55 file JSP (36 trang nội dung, 5 layouts, 14 partials)**, **14 bảng CSDL**, **22 file test / 114 bài Unit Test** |

---

## 2. Đối chiếu Chức năng: Đề xuất Ban đầu vs Hệ thống Hiện tại

Trước đây, đồ án có các tài liệu mô tả chức năng ở từng giai đoạn:
- **`DANG-KY-DE-TAI.md`**: Bản đề xuất ban đầu đăng ký các tính năng cơ bản và 14 tính năng tuỳ chọn.
- **`MO-TA-DO-AN.md`**: Bản tóm tắt gửi giảng viên (ghi nhận 17 chức năng cốt lõi).
- **`ke-hoach-frontend.md`**: Bản quy hoạch ban đầu dự kiến 30 trang và 4 layouts.

Qua quá trình phát triển và hoàn thiện trọn vẹn **18 ISSUE công nghệ cao**, hệ thống hiện tại đã mở rộng vượt bậc:

| Tiêu chí | Bản đăng ký / mô tả cũ | Hệ thống thực tế hiện tại | Tỉ lệ hoàn thành / Ghi chú |
|---|:---:|:---:|---|
| **Số lượng chức năng** | 17 – 28 chức năng | **38 chức năng hoàn chỉnh** | **Đạt 135%** (toàn bộ tính năng tuỳ chọn đều đã được hiện thực hóa) |
| **Số lượng trang web (Views)** | 30 – 31 trang | **36 trang web JSP + 7 Endpoints** | Bổ sung thêm Thống kê tác giả, Tìm kiếm FULLTEXT, Lỗi 403, Raw view, REST API, Drive Backup, Wallet |
| **Khung bố cục (Layouts)** | 4 layouts | **5 layouts** (`main`, `auth`, `reader`, `admin`, `editor`) | Bổ sung thêm layout `editor` chuyên biệt cho sáng tác chương |
| **Số lớp mã nguồn Java** | 47 – 60 lớp | **77 lớp Java** | 18 Controllers, 14 DAOs, 12 Models, 6 Filters, 12 Utilities, 15 Services/Helpers |
| **Số file view JSP** | 50 – 51 files | **55 file JSP** | 36 trang nội dung, 5 layout wrappers, 14 view partials tái sử dụng |
| **Bảng dữ liệu CSDL** | 13 bảng | **14 bảng** | Thêm bảng `wallets` & `transactions` (Ví xu ảo) |
| **Bộ kiểm thử tự động** | 28 bài | **97 bài Unit Tests (22 classes)** | Đạt 100% PASS, kiểm thử độc lập DAO, Filter, Token, Wallet, RateLimiter |

---

## 3. Bản đồ Hệ thống: 36 Trang Web & 7 Endpoints Dịch vụ

Ứng dụng vận hành với **5 khung Layout chuyên biệt**:
- `main.jsp`: Khung chính (Nav kính mờ + Shell nội dung + Footer thương hiệu)
- `auth.jsp`: Khung xác thực tối giản căn giữa màn hình (tập trung trải nghiệm đăng nhập/đăng ký)
- `reader.jsp`: Khung đọc truyện chuyên biệt (cột chữ 38em, font có chân, không phân tâm, tối ưu mỏi mắt)
- `admin.jsp`: Khung quản trị (Sidebar trái + Header điều hướng đa tác vụ)
- `editor.jsp`: Khung soạn thảo chương truyện tập trung (khung rộng, đếm số từ trực tiếp)

### A. Nhóm Công cộng & Khám phá (Public & Discovery — 15 trang/endpoints)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 1 | **Trang chủ** | `/` | `main` | `HomeServlet` | `common/home.jsp` | Tất cả |
| 2 | **Kho truyện & Bộ lọc thể loại** | `/story?action=list` | `main` | `StoryServlet` | `common/story/list.jsp` | Tất cả |
| 3 | **Tìm kiếm sâu nội dung chương** | `/story?action=search&q={q}` | `main` | `StoryServlet` | `common/story/search.jsp` | Tất cả |
| 4 | **Chi tiết truyện & Gợi ý truyện** | `/story?action=detail&id={id}` | `main` | `StoryServlet` | `common/story/detail.jsp` | Tất cả |
| 5 | **Đọc chương truyện** | `/chapter?action=read&id={id}` | `reader` | `ChapterServlet` | `common/chapter/read.jsp` | Tất cả |
| 6 | **Xem nội dung chương thô (Raw)** | `/chapter?action=raw&id={id}` | *Raw* | `ChapterServlet` | `common/chapter/raw.jsp` | Tất cả |
| 7 | **Bảng xếp hạng (Truyện & Tác giả)** | `/rank` hoặc `/rank?by=authors` | `main` | `RankServlet` | `common/rank.jsp` | Tất cả |
| 8 | **Hướng dẫn sử dụng website** | `/page?name=guide` | `main` | `PageServlet` | `common/page/guide.jsp` | Tất cả |
| 9 | **Nội quy cộng đồng & Tiêu chuẩn** | `/page?name=rules` | `main` | `PageServlet` | `common/page/rules.jsp` | Tất cả |
| 10 | **Hồ sơ tác giả công khai** | `/user?action=profile&id={id}` | `main` | `UserServlet` | `user/profile.jsp` | Tất cả |
| 11 | **Tải truyện Offline (.txt)** | `/download?storyId={id}` | *Raw Text* | `DownloadServlet` | *Stream trực tiếp* | Tất cả |
| 12 | **Sơ đồ trang SEO (XML Sitemap)** | `/sitemap.xml` | *XML* | `SitemapServlet` | *Stream XML* | Bot tìm kiếm |
| 13 | **Tệp chỉ dẫn Robots** | `/robots.txt` | *Text* | Static | `webapp/robots.txt` | Bot tìm kiếm |
| 14 | **Báo lỗi 403 (Truy cập bị từ chối)**| `/error?code=403` | `main` | `ErrorServlet` | `error/403.jsp` | Tất cả |
| 15 | **Báo lỗi 404 (Không tìm thấy trang)** | `/error?code=404` | `main` | `ErrorServlet` | `error/404.jsp` | Tất cả |
| 16 | **Báo lỗi 500 (Lỗi máy chủ nội bộ)** | `/error?code=500` | `main` | `ErrorServlet` | `error/500.jsp` | Tất cả |

### B. Nhóm Xác thực & Tài khoản (Authentication — 4 trang)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 17 | **Đăng nhập (Mật khẩu & Google OIDC)** | `/auth?action=login` | `auth` | `AuthServlet` | `auth/login.jsp` | Khách |
| 18 | **Đăng ký thành viên (+ reCAPTCHA)** | `/auth?action=register` | `auth` | `AuthServlet` | `auth/register.jsp` | Khách |
| 19 | **Quên mật khẩu (Gửi email SMTP)** | `/auth?action=forgot` | `auth` | `AuthServlet` | `auth/forgot.jsp` | Khách |
| 20 | **Đặt lại mật khẩu (Token bảo mật)** | `/auth?action=reset&token={token}` | `auth` | `AuthServlet` | `auth/reset.jsp` | Khách |

### C. Nhóm Thành viên & Độc giả (Member & Reader Experience — 8 trang)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 21 | **Trung tâm cá nhân (Hồ sơ & Ví xu)** | `/user?action=me` | `main` | `UserServlet` | `user/me.jsp` | Thành viên |
| 22 | **Chỉnh sửa hồ sơ (+ Unlink Guard)** | `/user?action=edit` | `main` | `UserServlet` | `user/edit.jsp` | Thành viên |
| 23 | **Đổi mật khẩu tài khoản** | `/user?action=password` | `main` | `UserServlet` | `user/edit.jsp` | Thành viên |
| 24 | **Truyện đã lưu (Bookmarks & Tiến độ)** | `/bookmark?action=list` | `main` | `BookmarkServlet` | `user/bookmarks.jsp` | Thành viên |
| 25 | **Lịch sử đọc truyện cá nhân** | `/history` | `main` | `HistoryServlet` | `user/history.jsp` | Thành viên |
| 26 | **Tác giả đang theo dõi (Following)** | `/follow?action=list` | `main` | `FollowServlet` | `user/following.jsp` | Thành viên |
| 27 | **Hộp thông báo cá nhân** | `/notification?action=list` | `main` | `NotificationServlet` | `user/notifications.jsp` | Thành viên |
| 28 | **Gửi báo cáo vi phạm nội dung** | `/report?action=create` | `main` | `ReportServlet` | `user/report.jsp` | Thành viên |

### D. Nhóm Tác giả & Sáng tác (Author Studio — 5 trang/endpoints)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 29 | **Tủ truyện của tôi (Author Bookshelf)**| `/story?action=mine` | `main` | `StoryServlet` | `user/story/mine.jsp` | Tác giả / Admin |
| 30 | **Đăng / Sửa truyện (+ Live Preview)** | `/story?action=create` hoặc `edit` | `main` | `StoryServlet` | `user/story/form.jsp` | Tác giả / Admin |
| 31 | **Thống kê truyện (Biểu đồ 14 ngày)** | `/story?action=stats` | `main` | `StoryServlet` | `user/story/stats.jsp` | Tác giả / Admin |
| 32 | **Soạn thảo / Sửa chương truyện** | `/chapter?action=create` hoặc `edit` | `editor` | `ChapterServlet` | `user/chapter/form.jsp` | Tác giả / Admin |
| 33 | **Sao lưu truyện sang Google Drive** | `/backup/drive?storyId={id}` | *Raw/Redirect* | `DriveBackupServlet` | *OAuth v3 API* | Tác giả / Admin |

### E. Nhóm Quản trị viên (Admin Dashboard — 6 trang)

| # | Trang web | URL truy cập | Layout | Controller | Mảnh JSP nội dung | Phân quyền |
|:-:|---|---|---|---|---|---|
| 34 | **Bảng điều khiển & Thống kê tổng quan**| `/admin/dashboard` | `admin` | `AdminDashboardServlet` | `admin/dashboard.jsp` | Quản trị viên |
| 35 | **Quản lý toàn bộ kho truyện (Xóa mềm)**| `/admin/story` | `admin` | `AdminStoryServlet` | `admin/stories.jsp` | Quản trị viên |
| 36 | **Quản lý tài khoản & Khóa người dùng**| `/admin/user` | `admin` | `AdminUserServlet` | `admin/users.jsp` | Quản trị viên |
| 37 | **Quản lý danh mục thể loại (Tags)** | `/admin/tag` | `admin` | `AdminTagServlet` | `admin/tags.jsp` | Quản trị viên |
| 38 | **Xử lý báo cáo vi phạm từ người dùng** | `/admin/report` | `admin` | `AdminReportServlet` | `admin/reports.jsp` | Quản trị viên |
| 39 | **Kiểm duyệt & Ẩn bình luận vi phạm** | `/admin/comment` | `admin` | `AdminCommentServlet` | `admin/comments.jsp` | Quản trị viên |

### F. Nhóm RESTful JSON API & Dịch vụ Ngoại tuyến (API, PWA & Endpoints — 5 endpoints)

| # | Endpoint dịch vụ | Giao thức | Controller / File | Vai trò & Mục đích | Phân quyền |
|:-:|---|---|---|---|---|
| 40 | `/api/stories` | GET (JSON) | `ApiServlet` | Cung cấp danh sách truyện hỗ trợ phân trang & CORS | Tất cả |
| 41 | `/api/story/{id}` | GET (JSON) | `ApiServlet` | Cung cấp chi tiết truyện và danh sách mục lục chương | Tất cả |
| 42 | `/api/chapter/{id}` | GET (JSON) | `ApiServlet` | Cung cấp toàn bộ nội dung chương đọc cho client/app | Tất cả |
| 43 | `/wallet?action=balance` / `tip` | GET / POST | `WalletServlet` | API kiểm tra số dư và chuyển xu ảo tặng tác giả (ACID) | Thành viên |
| 44 | `/manifest.json` & `/sw.js` | Static / JS | Service Worker | Quản lý cache PWA, cho phép cài HomeScreen & đọc offline | Tất cả |

---

## 4. Danh mục 38 Chức năng Toàn diện (Full Feature Catalog)

Hệ thống được thiết kế hoàn thiện với **38 chức năng phân thành 5 nhóm đối tượng**:

### Phân hệ 1: Chức năng cho Khách & Độc giả (10 chức năng)
1. **Khám phá Trang chủ đa chiều:** Hiển thị truyện mới cập nhật, truyện đọc nhiều nhất, truyện có đánh giá cao nhất, và khối "Tiếp tục đọc" cho người dùng quay lại.
2. **Duyệt & Lọc kho truyện nâng cao:** Lọc theo 10+ danh mục thể loại, lọc trạng thái (Đang tiến hành / Đã hoàn thành), sắp xếp theo độ phổ biến / mới cập nhật, phân trang chuẩn mực.
3. **Tìm kiếm sâu trong nội dung chương (ISSUE-005):** Tìm kiếm FULLTEXT xuyên suốt hàng trăm ngàn từ của các chương truyện, trích đoạn ngữ cảnh chứa từ khóa kèm highlight `<mark>`.
4. **Xem chi tiết truyện:** Xem ảnh bìa, tóm tắt nội dung, danh sách tác giả, thể loại liên quan, danh sách mục lục chương, thống kê lượt đọc / lượt lưu.
5. **Đọc chương với Reader chuyên biệt:** Tự động ẩn thanh điều hướng, độ rộng cột tối ưu thị giác chống mỏi mắt (38em), điều hướng nhanh bằng phím mũi tên `←` / `→`, mục lục chương dạng popup thả xuống.
6. **Tùy biến môi trường đọc:** Chuyển đổi Dark Mode / Light Mode / Sepia, tăng giảm cỡ chữ (A- / A+), giãn dòng, lựa chọn font chữ có chân (Serif) hoặc không chân (Sans-serif).
7. **Tự động lưu tiến độ đọc:** Không cần bấm lưu, hệ thống tự động ghi nhận chương đang đọc và vị trí % thanh cuộn vào CSDL và LocalStorage.
8. **Tải truyện Offline (.txt):** Cho phép xuất và tải toàn bộ truyện về máy tính/điện thoại để đọc khi không có mạng.
9. **Bảng xếp hạng (Truyện & Tác giả - ISSUE-006):** Xếp hạng truyện theo lượt xem / điểm đánh giá; Bảng vàng vinh danh các tác giả xuất sắc nhất kèm số lượt đọc và người theo dõi.
10. **Trang thông tin & Trợ năng:** Xem Hướng dẫn sử dụng (`/page?name=guide`), Nội quy cộng đồng (`/page?name=rules`), hỗ trợ phím tắt A11y, Skip-link và Mobile chạm chuẩn 44px (ISSUE-015).

### Phân hệ 2: Chức năng cho Thành viên & Độc giả có Tài khoản (8 chức năng)
11. **Xác thực tài khoản đa phương thức:** Đăng ký, đăng nhập bằng tài khoản nội bộ (mật khẩu băm PBKDF2 muối ngẫu nhiên), hoặc đăng nhập 1 chạm an toàn bằng Google OIDC qua Firebase (ISSUE-001).
12. **Quản lý Hồ sơ & Unlink Guard:** Cập nhật thông tin cá nhân (tên hiển thị, tiểu sử bio, avatar). Cơ chế Unlink Guard bắt buộc phải có mật khẩu trước khi cho phép gỡ liên kết Google.
13. **Khôi phục mật khẩu qua Email thật (ISSUE-002):** Gửi email đặt lại mật khẩu với token mã hóa thời hạn 30 phút qua giao thức JavaMail SMTP bất đồng bộ.
14. **Tủ truyện đã lưu (Bookmarks):** Quản lý danh sách các truyện yêu thích, hiển thị tiến độ đọc và nút "Đọc tiếp" nhảy thẳng tới chương đang dở.
15. **Lịch sử đọc truyện (Reading History):** Xem lại toàn bộ các chương đã đọc theo trình tự thời gian, hỗ trợ xóa từng mục hoặc làm sạch lịch sử.
16. **Bình luận đa cấp & Thảo luận theo chương (ISSUE-004):** Bình luận ở chân trang chi tiết truyện hoặc bình luận chuyên sâu dưới chân từng chương đọc; hỗ trợ trả lời lồng nhau (nested comments) và thả tim tương tác.
17. **Đánh giá truyện (Rating):** Chấm sao từ 1 đến 5 sao với cơ chế chống gian lận (khóa chính kép `user_id, story_id`, mỗi tài khoản chỉ đánh giá 1 lần).
18. **Gửi Báo cáo vi phạm (Report):** Gửi phản ánh nội dung truyện hoặc bình luận có dấu hiệu vi phạm để ban quản trị kiểm duyệt.

### Phân hệ 3: Chức năng cho Tác giả & Sáng tác — Author Studio (7 chức năng)
19. **Tủ truyện tác giả (My Stories):** Quản lý danh sách các tác phẩm do chính mình sáng tác, phân loại truyện Đang ra / Hoàn thành / Bản nháp.
20. **Đăng & Chỉnh sửa truyện (+ Live Preview - ISSUE-017):** Khung tạo truyện đa thể loại, tải ảnh bìa trực tiếp từ máy với khung xem trước tỉ lệ 3:4 tức thì qua FileReader API.
21. **Soạn thảo chương với Layout Editor:** Giao diện tập trung toàn màn hình, thanh công cụ tối giản, tự động đếm số từ theo thời gian thực, tự động đề xuất số chương kế tiếp.
22. **Bảng Thống kê truyện của tác giả:** Thống kê chi tiết tổng lượt xem, số lượt bookmark, số bình luận; kèm biểu đồ trực quan lượt đọc 14 ngày gần nhất của truyện nổi bật.
23. **Sao lưu Google Drive (Drive Backup - ISSUE-001):** Tác giả xuất bản sao lưu toàn bộ chương truyện lên Google Drive cá nhân dưới định dạng `001 - <chương>.txt` chuẩn hóa qua Drive API v3.
24. **Nhận xu ủng hộ & Tặng quà tác giả (ISSUE-008):** Độc giả có thể gửi tặng xu ảo kèm lời nhắn động viên cho tác giả; tác giả xem số dư xu trong ví cá nhân.
25. **Theo dõi tác giả & Nhận thông báo (Follow & Notify):** Độc giả bấm Follow tác giả; hệ thống tự động bắn thông báo tức thì đến người theo dõi ngay khi tác giả xuất bản chương mới.

### Phân hệ 4: Chức năng cho Quản trị viên — Admin Dashboard (6 chức năng)
26. **Bảng điều khiển quản trị (Dashboard Analytics):** Thống kê tổng số truyện, số chương, số tài khoản, tổng lượt đọc, biểu đồ tăng trưởng 14 ngày và danh sách truyện hot nhất.
27. **Quản lý toàn bộ kho truyện (Xóa mềm):** Xem toàn bộ truyện trong hệ thống kể cả bản nháp; thực hiện gỡ truyện vi phạm hoặc khôi phục lại truyện đã gỡ mà không làm mất dữ liệu liên quan.
28. **Quản lý tài khoản người dùng:** Tra cứu thành viên, nâng/hạ quyền Quản trị viên, thực hiện khóa tài khoản kèm lý do vi phạm chi tiết và mở khóa tài khoản.
29. **Quản lý danh mục thể loại (Tags):** Thêm mới thể loại, sửa tên / slug thể loại, xóa thể loại, theo dõi số lượng truyện thuộc từng thể loại.
30. **Xử lý báo cáo vi phạm (Reports):** Tiếp nhận danh sách tố cáo từ độc giả, xem nội dung bị tố cáo, đánh dấu Đã xử lý hoặc Bỏ qua.
31. **Kiểm duyệt bình luận:** Xem danh sách bình luận mới nhất, ẩn bình luận vi phạm (vẫn lưu lại bằng chứng trong CSDL) hoặc gỡ bỏ hoàn toàn.

### Phân hệ 5: Phân hệ Nền tảng, Tích hợp & Dịch vụ Nâng cao (7 chức năng)
32. **Ví xu ảo & Giao dịch nguyên tử (ACID Wallet - ISSUE-008):** Hệ thống ví xu nội bộ, ghi nhật ký giao dịch chuyển xu không thể xảy ra tình trạng mất mát dữ liệu tài chính.
33. **Bộ RESTful JSON API chuẩn quốc tế (ISSUE-009):** Cung cấp các endpoints chuẩn hóa kèm CORS headers cho bên thứ ba hoặc ứng dụng di động trong tương lai.
34. **PWA & Khả năng Đọc ngoại tuyến (Offline Reader - ISSUE-010):** Đạt chuẩn Progressive Web App, cho phép cài đặt app ra màn hình chính, Service Worker lưu bộ nhớ đệm giúp đọc lại các chương cũ không cần internet.
35. **Bảo vệ Sliding Window & Chống Brute-force/Spam (ISSUE-003):** Cơ chế Rate Limiter luồng an toàn tự động khóa tạm thời các IP/tài khoản cố tình thử mật khẩu hoặc spam bình luận.
36. **Tự động dọn dẹp hệ thống ngầm (Background Cleaner - ISSUE-013):** Daemon thread dọn dẹp nhật ký lượt đọc `view_logs` quá hạn 90 ngày, đảm bảo CSDL luôn tinh gọn.
37. **SEO Động, Sitemap & OpenGraph (ISSUE-012):** Tự động sinh `sitemap.xml`, cung cấp `robots.txt` và các thẻ OpenGraph (`og:image`, `og:title`) khi chia sẻ link lên Facebook/Zalo/Twitter.
38. **Chia sẻ 1 chạm & Toast Notifications (ISSUE-018):** Sao chép đường dẫn truyện vào Clipboard tức thì kèm Toast thông báo nổi siêu mượt.

---

## 5. Hệ thống 18 Module Công nghệ Nâng cao (ISSUE-001 → ISSUE-018)


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

## 6. Thiết kế Cơ sở Dữ liệu (14 Bảng CSDL)

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

## 7. Bảo mật & Trợ năng (Security & Accessibility)

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

## 8. Hệ thống Kiểm thử Tự động (114 Tests)

Kiểm thử tự động thực thi qua công cụ JUnit 5 Console Standalone, độc lập môi trường mạng và CSDL:

```text
JUnit Platform Suite: 114 tests found, 114 successful, 0 failed.
Thời gian thực thi: ~1.85 giây (100% PASS).
```

### Các nhóm test case tiêu biểu:
- **`RecaptchaFilterTest` (8 tests):** Chặn bot tại 3 cửa nhạy cảm (Login, Register, Comment), cơ chế Fail-Open an toàn mạng.
- **`Báo cáo vi phạm / ReportTest` (13 tests):** Phân loại vi phạm, quản lý bằng chứng hình ảnh, liên kết sâu và chặn spam báo cáo.
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

## 9. Hướng dẫn Vận hành Nhanh

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
