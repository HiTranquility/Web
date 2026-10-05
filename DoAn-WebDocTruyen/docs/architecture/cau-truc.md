# 🏗️ CẤU TRÚC CODEBASE & QUY CHUẨN THIẾT KẾ HỆ THỐNG

> **Tài liệu chuẩn hóa cấu trúc thư mục, quy tắc đặt để file mã nguồn, bản đồ đối chiếu Sơ đồ ↔ Codebase (Sequence Tracing) và cẩm nang thay đổi UI/Logic cho đồ án ĐọcTruyện.**  
> Cập nhật: 2026-10-05 · **Phiên bản:** Hoàn thiện 25 Module Công nghệ Nâng cao · **Độ phủ test:** 126/126 Unit Tests PASS.

---

## 📑 MỤC LỤC
1. [Triết lý Kiến trúc & Quy tắc "Nó nói chuyện với ai?"](#1-triết-lý-kiến-trúc--quy-tắc-nó-nói-chuyện-với-ai)
2. [Bản đồ Toàn bộ Thư mục Mã nguồn (Project Directory Map)](#2-bản-đồ-toàn-bộ-thư-mục-mã-nguồn-project-directory-map)
3. [Vòng đời Xử lý Yêu cầu (Request Lifecycle Flow)](#3-vòng-đời-xử-lý-yêu-cầu-request-lifecycle-flow)
4. [Bản đồ Tra cứu Nhanh: Sơ đồ Tuần tự ↔ File Codebase](#4-bản-đồ-tra-cứu-nhanh-sơ-đồ-tuần-tự--file-codebase)
5. [Cẩm nang Thay đổi Giao diện (UI) Không Làm Vỡ Hệ thống](#5-cẩm-nang-thay-đổi-giao-diện-ui-không-làm-vỡ-hệ-thống)
6. [Quy trình 4 Bước Thêm hoặc Mở rộng Tính năng Nghiệp vụ](#6-quy-trình-4-bước-thêm-hoặc-mở-rộng-tính-năng-nghiệp-vụ)
7. [Các Nguyên tắc Vàng (Best Practices & Anti-patterns)](#7-các-nguyên-tắc-vàng-best-practices--anti-patterns)

---

## 1. Triết lý Kiến trúc & Quy tắc "Nó nói chuyện với ai?"

Dự án áp dụng mô hình chuẩn **MVC Model 2 (Model - View - Controller)** thuần Java, Server-side Rendering (SSR) không pha tạp thư viện cồng kềnh.

Để giữ codebase luôn ngăn nắp và không bị rối khi mở rộng, quy tắc quyết định duy nhất khi tạo file hoặc tìm kiếm code là: **"File này nói chuyện với ai?"**

| Nó nói chuyện với… | Bỏ vào tầng | Nhiệm vụ chính | Ví dụ điển hình |
|---|---|---|---|
| **Cơ sở dữ liệu (MySQL)** | `src/main/java/truyen/dao/` | 100% câu lệnh SQL qua PreparedStatement, HikariCP Connection Pool, không đụng request/response | `StoryDAO.java`, `UnlockDAO.java` |
| **Trình duyệt (Browser / Client)** | `src/main/java/truyen/controller/` | Tiếp nhận HTTP Request, đọc tham số, phân quyền, gọi DAO, gắn dữ liệu vào request và forward tới view | `ChapterServlet.java`, `WalletServlet.java` |
| **Không ai — chỉ là túi dữ liệu** | `src/main/java/truyen/model/` | JavaBean thuần (POJO), chứa trường dữ liệu, getters/setters, không chứa logic nghiệp vụ nặng | `Chapter.java`, `Review.java`, `DailyQuest.java` |
| **Không ai — chỉ là hàm thuật toán** | `src/main/java/truyen/util/` | Các hàm static xử lý chuỗi, mã hóa mật khẩu, kiểm tra bot, nén EPUB, cấu hình hệ thống | `PasswordUtil.java`, `EpubWriter.java` |
| **Chặn & tiền xử lý HTTP Request** | `src/main/java/truyen/filter/` | Lọc request trước khi đến Controller (UTF-8, CSRF, chống spam RateLimiter, phân quyền URL) | `AuthFilter.java`, `CsrfFilter.java` |
| **Người dùng nhìn thấy (Vẽ HTML)** | `src/main/webapp/WEB-INF/views/` | File JSP render giao diện, dùng EL/JSTL `<c:out>`, không chứa mã Java scriplet `<% %>` | `detail.jsp`, `read.jsp`, `form.jsp` |
| **Tài nguyên tĩnh (Static Assets)** | `src/main/webapp/assets/` | CSS phân tầng, Vanilla JS độc lập nạp defer, hình ảnh minh họa | `base.css`, `components.css`, `nav.js` |

> 💡 **Nguyên tắc phân định ranh giới:** Nếu một file làm cả 2 việc (vừa query SQL vừa in HTML, hoặc vừa xử lý request vừa tính toán thuật toán phức tạp), **hãy tách đôi ra ngay lập tức**.

---

## 2. Bản đồ Toàn bộ Thư mục Mã nguồn (Project Directory Map)

```text
DoAn-WebDocTruyen/
├── src/main/java/truyen/
│   ├── model/                  (15 Models — JavaBeans / DTOs khớp CSDL)
│   │   ├── User.java           Story.java          Chapter.java        Tag.java
│   │   ├── Comment.java        Bookmark.java       Rating.java         Follow.java
│   │   ├── Report.java         Notification.java   ReadHistory.java    Wallet.java
│   │   ├── Transaction.java    Review.java         DailyQuest.java
│   │
│   ├── dao/                    (17 DAOs — Truy vấn CSDL nguyên tử)
│   │   ├── UserDAO.java        StoryDAO.java       ChapterDAO.java     TagDAO.java
│   │   ├── CommentDAO.java     BookmarkDAO.java    RatingDAO.java      FollowDAO.java
│   │   ├── ReportDAO.java      NotificationDAO.java ViewLogDAO.java    PasswordResetDAO.java
│   │   ├── WalletDAO.java      UnlockDAO.java      ReviewDAO.java      GamificationDAO.java
│   │
│   ├── controller/             (19 Servlets — Điều hướng & Xử lý nghiệp vụ)
│   │   ├── common/             (Nhóm Công cộng & Đọc truyện)
│   │   │   ├── HomeServlet.java            Trang chủ (/)
│   │   │   ├── StoryServlet.java           Kho truyện, Chi tiết, Tìm kiếm, Đánh giá (/story)
│   │   │   ├── ChapterServlet.java         Đọc chương, Thêm/Sửa chương (/chapter)
│   │   │   ├── RankServlet.java            Bảng xếp hạng truyện & tác giả (/rank)
│   │   │   ├── PageServlet.java            Hướng dẫn, Nội quy (/page)
│   │   │   ├── DownloadServlet.java        Xuất tải truyện .txt (/download)
│   │   │   ├── SitemapServlet.java         SEO sitemap.xml (/sitemap.xml)
│   │   │   ├── ApiServlet.java             RESTful JSON API (/api/*)
│   │   │   └── ErrorServlet.java           Bắt lỗi tập trung 403, 404, 500 (/error)
│   │   │
│   │   ├── user/               (Nhóm Thành viên & Tác giả)
│   │   │   ├── AuthServlet.java            Đăng nhập, Đăng ký, Quên MK, Google OIDC (/auth)
│   │   │   ├── UserServlet.java            Hồ sơ, Đổi MK, Gamification Quests (/user)
│   │   │   ├── BookmarkServlet.java        Lưu truyện & Tiến độ đọc (/bookmark)
│   │   │   ├── HistoryServlet.java         Lịch sử đọc truyện (/history)
│   │   │   ├── FollowServlet.java          Theo dõi tác giả (/follow)
│   │   │   ├── NotificationServlet.java    Hộp thông báo cá nhân (/notification)
│   │   │   ├── CommentServlet.java         Bình luận đa cấp & Thả tim (/comment)
│   │   │   ├── ReportServlet.java          Báo cáo vi phạm nội dung (/report)
│   │   │   └── WalletServlet.java          Ví xu ảo & Mở khóa chương VIP (/wallet)
│   │   │
│   │   ├── story/              (Nhóm Tác vụ Đám mây)
│   │   │   └── DriveBackupServlet.java     Sao lưu Google Drive API v3 (/backup/drive)
│   │   │
│   │   └── admin/              (Nhóm Quản trị viên)
│   │       ├── AdminDashboardServlet.java  Thống kê tổng quan (/admin/dashboard)
│   │       ├── AdminStoryServlet.java      Duyệt, gỡ & khôi phục truyện (/admin/story)
│   │       ├── AdminUserServlet.java       Quản lý tài khoản, khóa vi phạm (/admin/user)
│   │       ├── AdminTagServlet.java        Quản lý thể loại truyện (/admin/tag)
│   │       ├── AdminCommentServlet.java    Kiểm duyệt bình luận (/admin/comment)
│   │       └── AdminReportServlet.java     Xử lý báo cáo & xem bằng chứng (/admin/report)
│   │
│   ├── filter/                 (6 Filters — Vòng tròn bảo mật đa lớp)
│   │   ├── EncodingFilter.java         Ép bảng mã UTF-8 toàn hệ thống
│   │   ├── CsrfFilter.java             Chống tấn công CSRF trên mọi request POST
│   │   ├── RecaptchaFilter.java        Chặn bot spam reCAPTCHA v3 ở cửa đăng ký/bình luận
│   │   ├── AuthFilter.java             Chặn khách chưa đăng nhập khi đọc chương/tải truyện
│   │   ├── AdminFilter.java            Bảo vệ khu vực quản trị viên `/admin/*`
│   │   └── NotificationFilter.java     Tự động nạp số lượng thông báo chưa đọc vào thanh menu
│   │
│   └── util/                   (15 Utilities — Thư viện tiện ích)
│       ├── DBConnection.java           HikariCP Connection Pool kết nối MySQL
│       ├── PasswordUtil.java           Băm mật khẩu PBKDF2WithHmacSHA256 kèm muối ngẫu nhiên
│       ├── SlugUtil.java               Chuẩn hóa tiêu đề thành URL slug không dấu
│       ├── CsrfUtil.java               Sinh & kiểm tra token bảo mật phiên
│       ├── RateLimiter.java            Thuật toán Sliding Window chặn brute-force & spam
│       ├── MailSender.java             Gửi email SMTP JavaMail thật & bất đồng bộ
│       ├── EpubWriter.java             Đóng gói file truyện chuẩn quốc tế EPUB 2.0
│       ├── UploadUtil.java             Xử lý tải ảnh bìa, kiểm soát dung lượng & định dạng
│       ├── GoogleConfig.java           Đọc cấu hình OIDC Client ID & Google Drive
│       ├── GoogleTokenVerifier.java    Xác thực chữ ký JWT idToken từ Google
│       ├── RecaptchaVerifier.java      Xác thực điểm số bot Google reCAPTCHA v3
│       ├── DriveClient.java            Giao tiếp Google Drive REST API v3
│       ├── ChapterToTxt.java           Chuyển đổi nội dung chương sang văn bản thuần
│       ├── ServletHelper.java          Đọc tham số an toàn, redirect, session helper
│       └── DemoData.java               Dữ liệu mẫu dự phòng khi chưa bật CSDL
│
├── src/main/webapp/
│   ├── WEB-INF/views/
│   │   ├── layouts/            (5 Khung Layout chuyên biệt)
│   │   │   ├── main.jsp        Khung chính (Nav + Nội dung + Footer)
│   │   │   ├── auth.jsp        Khung thẻ căn giữa màn hình (Đăng nhập/Đăng ký)
│   │   │   ├── reader.jsp      Khung đọc truyện chuẩn quang học (38em, Zen)
│   │   │   ├── editor.jsp      Khung soạn thảo toàn màn hình cho tác giả
│   │   │   └── admin.jsp       Khung bảng điều khiển có Sidebar quản trị
│   │   │
│   │   ├── _partials/          (14 Mảnh giao diện tái sử dụng)
│   │   │   ├── _nav.jsp        Thanh điều hướng kính mờ
│   │   │   ├── _footer.jsp     Chân trang thông tin & bản quyền
│   │   │   ├── _card.jsp       Thẻ truyện dạng lưới (Story Grid Card)
│   │   │   ├── _rating-stars.jsp Hiển thị sao đánh giá
│   │   │   ├── _pagination.jsp Thanh phân trang bảo toàn tham số
│   │   │   ├── _toast.jsp      Thông báo nổi Toast
│   │   │   └── ...             (Các mảnh thành phần khác)
│   │   │
│   │   ├── common/             (Giao diện công cộng: home, rank, story, chapter, page)
│   │   ├── user/               (Giao diện cá nhân, tủ sách, ví xu, soạn thảo chương)
│   │   ├── auth/               (Giao diện đăng nhập, đăng ký, quên mật khẩu)
│   │   ├── admin/              (Giao diện bảng điều khiển quản trị)
│   │   └── error/              (Giao diện trang lỗi 403, 404, 500)
│   │
│   └── assets/
│       ├── css/
│       │   ├── base.css        Design Tokens (HSL colors, dark/light theme, typography)
│       │   ├── components.css  Thành phần UI (Buttons, Cards, Badges, Modals, Forms)
│       │   ├── layout-main.css Bố cục khung chính
│       │   ├── layout-auth.css Bố cục khung đăng nhập
│       │   ├── layout-reader.css Bố cục trang đọc chương
│       │   ├── layout-admin.css Bố cục trang quản trị
│       │   └── ...             (Page CSS đặc thù: stats.css, rank.css)
│       ├── js/
│       │   ├── nav.js          Điều hướng, menu mobile, chuyển theme Dark/Light
│       │   ├── pwa.js          Đăng ký Service Worker & Cache ngoại tuyến
│       │   └── reader.js       Phím tắt đọc chương, thanh tiến độ cuộn
│       └── images/             (Ảnh bìa mẫu, logo, icons)
```

---

## 3. Vòng đời Xử lý Yêu cầu (Request Lifecycle Flow)

Mọi HTTP Request từ trình duyệt gửi về máy chủ Tomcat đều đi qua một chu trình khép kín, được kiểm soát qua 5 chốt chặn:

```text
[Trình duyệt (Browser)]
       │ (HTTP Request: GET/POST)
       ▼
 [1. EncodingFilter]     ───> Ép UTF-8 request & response (chống lỗi font tiếng Việt)
       │
 [2. CsrfFilter]         ───> Kiểm tra _csrf Token cho mọi POST/PUT/DELETE
       │
 [3. RecaptchaFilter]    ───> Kiểm tra điểm bot reCAPTCHA v3 tại form nhạy cảm
       │
 [4. Auth / AdminFilter] ───> Kiểm tra quyền đăng nhập (User) hoặc quyền Quản trị (Admin)
       │
 [5. Servlet Controller] ───> Đọc action parameter (?action=...)
       │                      ├── Validate tham số đầu vào
       │                      ├── Gọi DAO / Service thực hiện logic CSDL
       │                      └── Gán dữ liệu vào request.setAttribute(...)
       ▼
 [JSP Layout & Content]  ───> Layout nhúng Content Page qua <jsp:include>
       │                      └── Escape XSS qua <c:out value="..."/>
       ▼
[Trả về HTML cho Browser]
```

---

## 4. Bản đồ Tra cứu Nhanh: Sơ đồ Tuần tự ↔ File Codebase

Khi xem các Sơ đồ Tuần tự (Sequence Diagrams) hoặc muốn kiểm tra, chỉnh sửa từng luồng nghiệp vụ, hãy tra cứu theo bảng dưới đây:

| Luồng Nghiệp Vụ / Sơ Đồ | File Controller Phụ Trách | File DAO Thực Thi SQL | File JSP Giao Diện | File CSS Định Dạng |
|---|---|---|---|---|
| **Chương VIP & Paywall Mở Khóa** (`diagram_seq_vip.png`) | [ChapterServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/common/ChapterServlet.java)<br>[WalletServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/user/WalletServlet.java) | [UnlockDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/UnlockDAO.java)<br>[WalletDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/WalletDAO.java) | `common/chapter/read.jsp`<br>`common/story/detail.jsp`<br>`user/chapter/form.jsp` | `layout-reader.css`<br>`components.css` (`.vip-card`, `.badge-vip`) |
| **Đánh Giá & Cảnh Báo Spoiler** (`diagram_seq_review.png`) | [StoryServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/common/StoryServlet.java) | [ReviewDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/ReviewDAO.java) | `common/story/detail.jsp` | `components.css` (`.spoiler-box`, `.review-card`) |
| **Gamification & Nhiệm Vụ Ngày** (`diagram_seq_gamification.png`) | [UserServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/user/UserServlet.java) | [GamificationDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/GamificationDAO.java)<br>[WalletDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/WalletDAO.java) | `user/me.jsp` | `components.css` (`.quest-pill`, `.checkin-btn`) |
| **Bảng Xếp Hạng Truyện & Tác Giả** (`diagram_seq_rank.png`) | [RankServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/common/RankServlet.java) | [StoryDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/StoryDAO.java)<br>[UserDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/UserDAO.java) | `common/rank.jsp` | `assets/css/rank.css` |
| **Bắt Buộc Đăng Nhập khi Đọc** | [AuthFilter.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/filter/AuthFilter.java) | [UserDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/UserDAO.java) | `auth/login.jsp`<br>`common/chapter/read.jsp` | `layout-auth.css` |
| **Thông Báo Tác Giả & Cập Nhật** | [ChapterServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/common/ChapterServlet.java) | [NotificationDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/NotificationDAO.java)<br>[FollowDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/FollowDAO.java) | `user/notifications.jsp`<br>`user/chapter/form.jsp` | `components.css` (`.notif-item`, `.notif-new`) |
| **Soạn Thảo Văn Học (Studio)** | [ChapterServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/common/ChapterServlet.java) | [ChapterDAO.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/dao/ChapterDAO.java) | `user/chapter/form.jsp`<br>`layouts/editor.jsp` | `layout-editor.css` |
| **Xuất Bản Sách EPUB 2.0** | [StoryServlet.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/controller/common/StoryServlet.java) | [EpubWriter.java](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/java/truyen/util/EpubWriter.java) | `common/story/detail.jsp` | `components.css` |

---

## 5. Cẩm nang Thay đổi Giao diện (UI) Không Làm Vỡ Hệ thống

Khi muốn nâng cấp, chỉnh sửa màu sắc, phông chữ hoặc tái thiết kế giao diện cho đồ án:

### Bước 1: Thay đổi Bảng màu & Typography (Tokens)
- Mở file: `src/main/webapp/assets/css/base.css`.
- Chỉnh sửa các biến CSS trong `:root` (Dark Mode) và `[data-theme="light"]` (Light Mode):
  - `--bg`: Màu nền chính của ứng dụng.
  - `--bg-card`: Màu nền các thẻ card truyện / bảng biểu.
  - `--text-main`: Màu chữ chính.
  - `--text-mut`: Màu chữ phụ / chú thích mờ.
  - `--border`: Màu đường viền phân cách.
  - `--ember`: Màu nhấn thương hiệu nhận diện (đỏ cam hổ phách).

### Bước 2: Tùy biến Kiểu dáng Thành phần (Components)
- Mở file: `src/main/webapp/assets/css/components.css`.
- Mọi nút bấm, khung form, thẻ card, huy hiệu trạng thái đều tuân theo các class chuẩn:
  - Nút bấm: `.btn`, `.btn-primary`, `.btn-ghost`, `.btn-danger`, `.btn-sm`.
  - Thẻ truyện: `.story-card`, `.story-cover`, `.story-info`.
  - Huy hiệu: `.badge`, `.badge-vip`, `.badge-completed`.
- **Tuyệt đối không viết `style="..."` trực tiếp trong file JSP.** Hãy tạo class trong `components.css` để dễ bảo trì và chấm điểm đồ án.

### Bước 3: Tùy biến Bố cục Khung (Layouts)
- Muốn chỉnh sửa thanh điều hướng Header: sửa `src/main/webapp/WEB-INF/views/_partials/_nav.jsp` và `layout-main.css`.
- Muốn chỉnh sửa Chân trang Footer: sửa `src/main/webapp/WEB-INF/views/_partials/_footer.jsp`.
- Muốn chỉnh sửa Bố cục Đọc truyện (độ rộng cột, thanh cuộn): sửa `src/main/webapp/WEB-INF/views/layouts/reader.jsp` và `layout-reader.css`.

---

## 6. Quy trình 4 Bước Thêm hoặc Mở rộng Tính năng Nghiệp vụ

Khi cần thêm tính năng mới, tuân thủ nghiêm ngặt 4 bước tuần tự:

```text
[Bước 1: Model]      Tạo / Bổ sung JavaBean trong truyen.model (chỉ get/set)
        │
        ▼
[Bước 2: DAO]        Viết hàm truy vấn SQL trong truyen.dao (Dùng PreparedStatement)
        │
        ▼
[Bước 3: Controller] Thêm nhánh action trong truyen.controller (Đọc param, gọi DAO, gắn requestScope)
        │
        ▼
[Bước 4: View]       Tạo / Cập nhật file JSP trong WEB-INF/views/ (Hiển thị qua JSTL / EL)
```

**Ví dụ thực tế:** Thêm tính năng *"Ghim truyện yêu thích lên đầu hồ sơ"*
1. **Model:** Thêm cờ `boolean isPinned` trong `Story.java`.
2. **DAO:** Viết `pinStory(int storyId, boolean pin)` trong `StoryDAO.java`.
3. **Controller:** Thêm nhánh `case "pin":` trong `StoryServlet.java`, kiểm tra quyền sở hữu rồi gọi DAO.
4. **View:** Thêm nút bấm Ghim `📌` trong `user/story/mine.jsp`.

---

## 7. Các Nguyên tắc Vàng (Best Practices & Anti-patterns)

### ✅ Những điều PHẢI làm:
1. **Luôn dùng PreparedStatement:** Ràng buộc tham số qua dấu `?`, không bao giờ nối chuỗi SQL.
2. **Luôn Escape HTML ở JSP:** Sử dụng `<c:out value="${...}"/>` cho toàn bộ nội dung do người dùng nhập vào để ngăn chặn tuyệt đối XSS.
3. **Mô hình Post/Redirect/Get (PRG):** Sau khi thực hiện lệnh POST ghi dữ liệu, luôn dùng `response.sendRedirect(...)` để người dùng bấm F5 không bị gửi lại form.
4. **Phân quyền 2 lớp:** Filter chặn theo tiền tố URL (`/admin/*`, `/user/*`); Servlet kiểm tra quyền sở hữu cụ thể (`authorId == currentUser.id`).
5. **Chạy kiểm thử trước khi commit:** Chạy `scripts\test.ps1` để bảo đảm toàn bộ **126 tests** đều xanh.

### ❌ Những điều TUYỆT ĐỐI TRÁNH:
1. Không viết code Java (`<% ... %>`) trong file JSP.
2. Không gọi trực tiếp DAO từ trang JSP.
3. Không để lộ Exception Stack Trace ra màn hình người dùng (ErrorServlet đã bắt tập trung).
4. Không đẻ thêm layout khi chỉ thay đổi nội dung trang.
5. Không lạm dụng inline CSS trong mã HTML.
