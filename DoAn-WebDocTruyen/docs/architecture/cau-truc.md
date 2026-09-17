# Cấu trúc dự án — bản đồ toàn bộ

Tài liệu này trả lời đúng một câu hỏi: **file này bỏ vào đâu?**

Đọc xong thì không phải nghĩ về cấu trúc nữa. Cấu trúc bên dưới là **bản cuối**,
không đổi cho tới lúc nộp bài.

---

## Trước hết: Cấu trúc được định hình chặt chẽ

Toàn bộ dự án sau khi hoàn thiện đầy đủ cả 11 CASE và các chức năng nâng cao:

| Loại | Số file | Thư mục | Ghi chú |
|------|--------:|--------:|---------|
| Java | 58 | 7 | Phân tầng: `model`, `dao`, `controller` (`common`, `user`, `admin`), `filter`, `util` |
| JSP | 57 | 9 | Gồm 4 khung layout, các `parts/`, `_partials/` và các trang theo tính năng |
| CSS | 6 | 1 | Nạp tầng: `base.css` → `components.css` → `layout-*.css` |
| **Tổng** | **121** | **17** | |

**Layout thì đúng 4 cái.** Không phải 10, không phải 20.

Lý do: layout không sinh theo TRANG, nó sinh theo **KHUNG**. Hơn 30 trang nhưng chỉ
có 4 kiểu khung: `main` (trang chủ, kho truyện, thông tin truyện,...), `auth` (đăng nhập/đăng ký), `reader` (tối giản đọc chương), và `admin` (sidebar quản trị).

---

## Quy tắc DUY NHẤT: file này bỏ vào đâu

Hỏi một câu: **"Nó nói chuyện với ai?"**

| Nó nói chuyện với… | Bỏ vào | Ví dụ |
|--------------------|--------|-------|
| **Database** | `dao/` | `StoryDAO` |
| **Trình duyệt** (đọc request, trả response) | `controller/` | `StoryServlet` |
| **Không ai** — chỉ chứa dữ liệu | `model/` | `Story` |
| **Không ai** — chỉ là hàm tiện ích | `util/` | `SlugUtil` |
| **Chỉ vẽ HTML** | `views/` | `list.jsp` |
| **Chặn request trước khi vào controller** | `filter/` | `AuthFilter` |

Không có trường hợp thứ bảy. Nếu một file không rơi vào ô nào, gần như chắc
chắn nó đang làm **hai việc** — tách đôi ra.

---

## Java — Phân tầng rõ ràng theo vai trò & trách nhiệm

```text
src/main/java/truyen/
├── model/          11 file — JavaBean thuần, chỉ get/set, khớp bảng CSDL & view object
│   ├── User.java        Story.java       Chapter.java
│   ├── Tag.java         Comment.java     Bookmark.java
│   ├── Rating.java      Follow.java      Report.java
│   └── Notification.java ReadHistory.java
│
├── dao/            12 file — chỉ xử lý SQL, Prepared Statement, không đụng request/response
│   ├── UserDAO.java     StoryDAO.java    ChapterDAO.java
│   ├── TagDAO.java      CommentDAO.java  BookmarkDAO.java
│   ├── RatingDAO.java   FollowDAO.java   ReportDAO.java
│   ├── NotificationDAO.java ViewLogDAO.java PasswordResetDAO.java
│
├── controller/     22 servlet — chia 3 package theo vai trò & quyền hạn (Role-based)
│   │
│   ├── common/     8 servlet — công khai, trang chủ, đọc truyện, xác thực, tải file
│   │   ├── HomeServlet.java          /            trang chủ
│   │   ├── AuthServlet.java          /auth        đăng nhập, đăng ký, đăng xuất, OAuth Google
│   │   ├── StoryServlet.java         /story       danh sách, chi tiết, tìm kiếm truyện
│   │   ├── ChapterServlet.java       /chapter     đọc chương, mục lục chương
│   │   ├── DownloadServlet.java      /download    tải truyện dạng .txt
│   │   ├── PageServlet.java          /page        hướng dẫn sử dụng, nội quy
│   │   ├── RankServlet.java          /rank        bảng xếp hạng lượt xem, đánh giá
│   │   └── UploadedFileServlet.java  /uploads/*   phục vụ ảnh upload an toàn
│   │
│   ├── user/       8 servlet — thành viên đã đăng nhập (bình luận, theo dõi, cá nhân)
│   │   ├── UserServlet.java          /user/*      hồ sơ cá nhân, đổi mật khẩu, avatar
│   │   ├── BookmarkServlet.java      /bookmark    đánh dấu & lưu vị trí chương đọc dở
│   │   ├── CommentServlet.java       /comment     gửi, xoá bình luận
│   │   ├── FollowServlet.java        /follow      theo dõi truyện yêu thích
│   │   ├── HistoryServlet.java       /history     lịch sử các chương đã đọc
│   │   ├── NotificationServlet.java  /notification thông báo hệ thống
│   │   ├── RatingServlet.java        /rating      chấm điểm sao truyện
│   │   └── ReportServlet.java        /report      báo cáo vi phạm nội dung
│   │
│   └── admin/      6 servlet — quản trị viên (AdminFilter bảo vệ)
│       ├── AdminDashboardServlet.java /admin/dashboard thống kê tổng quan
│       ├── AdminStoryServlet.java     /admin/story     quản lý, duyệt, gỡ truyện
│       ├── AdminUserServlet.java      /admin/user      quản lý, khoá tài khoản, cấp quyền
│       ├── AdminTagServlet.java       /admin/tag       thêm, sửa thể loại
│       ├── AdminCommentServlet.java   /admin/comment   kiểm duyệt bình luận
│       └── AdminReportServlet.java    /admin/report    xử lý đơn báo cáo
│
├── filter/         5 filter — bộ lọc bảo mật & tiền xử lý request
│   ├── EncodingFilter.java      ép chuẩn UTF-8 mọi request/response
│   ├── CsrfFilter.java          chặn tấn công CSRF trên mọi form POST
│   ├── AuthFilter.java          bảo vệ các URL yêu cầu đăng nhập (`/user/*`, `/bookmark`, ...)
│   ├── AdminFilter.java         bảo vệ vùng `/admin/*` chỉ dành cho role ADMIN
│   └── NotificationFilter.java  tự nạp số lượng thông báo chưa đọc vào request
│
└── util/           8 file — tiện ích dùng chung
    ├── DBConnection.java        mở kết nối MySQL qua Connection Pool
    ├── PasswordUtil.java        băm mật khẩu PBKDF2WithHmacSHA256 kèm muối ngẫu nhiên
    ├── SlugUtil.java            chuẩn hoá URL thân thiện (bỏ dấu tiếng Việt, ký tự lạ)
    ├── CsrfUtil.java            sinh và xác thực CSRF Token
    ├── UploadUtil.java          kiểm tra đuôi ảnh, chống path traversal khi tải lên
    ├── ServletHelper.java       tiện ích lấy tham số an toàn, redirect, trả JSON
    ├── DemoData.java            dữ liệu mẫu khi chạy thử nghiệm
    └── AppListener.java         khởi tạo ngữ cảnh ứng dụng khi server start
```

> **Tại sao tách `controller/` thành 3 package `common`, `user`, `admin`?**
> - **Rõ ràng quyền hạn:** Nhìn vào package biết ngay servlet cần mức quyền nào. `AdminFilter` chỉ cần soi URL `/admin/*`, `AuthFilter` quản lý các servlet trong `user/`.
> - **Dễ làm việc nhóm:** Nhóm làm giao diện người dùng không bị xung đột code với nhóm làm module quản trị.
> - **Mỗi servlet phụ trách một việc:** Giữ servlet gọn gàng, xử lý qua nhánh `?action=` cho các thao tác cùng thực thể.

---

## Views — Tổ chức theo 4 nhóm vai trò + 2 nhóm dùng chung

```text
WEB-INF/views/
├── layout/         KHUNG BỐ CỤC — 5 layout wrapper chính
│   ├── main.jsp        nav + nội dung + footer (trang chủ, kho truyện, thông tin,...)
│   ├── auth.jsp        card giữa màn hình, không nav (đăng nhập, đăng ký, quên mk)
│   ├── reader.jsp      tối giản, dải tiến độ, thanh công cụ đọc chương
│   ├── editor.jsp      giao diện soạn thảo chương truyện toàn màn hình
│   ├── admin.jsp       có sidebar quản trị bên trái
│   └── parts/          mảnh dùng chung giữa các layout (head.jsp, nav.jsp, footer.jsp)
│
├── _partials/      MẢNH GIAO DIỆN TÁI SỬ DỤNG — nhúng tĩnh bằng <%@ include %>
│   ├── _card.jsp          thẻ truyện dạng lưới (grid card)
│   ├── _cover.jsp         ảnh bìa truyện hoặc chữ cái đại diện
│   ├── _chapter-list.jsp  bảng danh sách chương phân trang
│   ├── _comment.jsp       khối bình luận lồng nhau
│   ├── _pagination.jsp    thanh chuyển trang giữ nguyên tham số lọc
│   ├── _rating-stars.jsp  hiển thị số sao đánh giá
│   ├── _stat-tile.jsp     khối số liệu thống kê
│   ├── _story-row.jsp     hàng truyện dạng danh sách ngang
│   ├── _tag-filter.jsp    bộ lọc thể loại truyện
│   └── _empty.jsp         trạng thái rỗng khi không có dữ liệu
│
├── auth/           XÁC THỰC — đi kèm layout/auth.jsp
│   ├── login.jsp       form đăng nhập + đăng nhập Google OAuth
│   ├── register.jsp    form đăng ký thành viên
│   ├── forgot.jsp      form quên mật khẩu
│   └── reset.jsp       form đặt lại mật khẩu mới
│
├── common/         TRANG CÔNG KHAI / CHUNG — ai cũng xem được
│   ├── home.jsp        trang chủ hệ thống (/)
│   ├── rank.jsp        bảng xếp hạng (/rank)
│   ├── story/          list.jsp (kho truyện), detail.jsp (chi tiết), search.jsp (tìm kiếm)
│   ├── chapter/        read.jsp (đọc chương), toc.jsp (mục lục thả xuống), raw.jsp, _block.jsp
│   └── page/           guide.jsp, rules.jsp, error403.jsp, error404.jsp, error500.jsp
│
├── user/           THÀNH VIÊN & TÁC GIẢ — yêu cầu đăng nhập
│   ├── profile.jsp     hồ sơ công khai
│   ├── me.jsp          trang cá nhân của tôi
│   ├── edit.jsp        chỉnh sửa hồ sơ, đổi mật khẩu
│   ├── bookmarks.jsp   tủ sách truyện đã đánh dấu
│   ├── history.jsp     lịch sử đọc
│   ├── following.jsp   truyện đang theo dõi
│   ├── notifications.jsp danh sách thông báo
│   ├── story/          form.jsp (đăng/sửa truyện), mine.jsp (quản lý), stats.jsp (thống kê)
│   └── chapter/        form.jsp (thêm/sửa chương)
│
└── admin/          QUẢN TRỊ VIÊN — chỉ dành cho role ADMIN
    ├── dashboard.jsp   thống kê tổng quan hệ thống
    ├── stories.jsp     quản lý, duyệt, gỡ truyện
    ├── users.jsp       quản lý tài khoản, phân quyền, khoá
    ├── tags.jsp        quản lý thể loại truyện
    ├── comments.jsp    kiểm duyệt bình luận
    └── reports.jsp     xử lý báo cáo vi phạm
```

### Quy ước tên file — nhìn tên biết loại

| Tên | Là gì | Ai gọi |
|-----|-------|--------|
| `list.jsp` | **mảnh nội dung** — không có `<html>` | servlet trỏ `contentPage` vào |
| `_card.jsp` | **mảnh nhỏ** tái dùng nhiều nơi | `<c:forEach>` trong mảnh khác |
| `main.jsp` | **khung** — có `<html>`, chèn mảnh vào giữa | servlet forward tới |
| `error404.jsp` | **trang phóng** 3 dòng | Tomcat forward thẳng tới |

Chỉ trang lỗi mới cần cặp phóng/mảnh, vì Tomcat gọi thẳng nó, không qua servlet
nào nên không ai đặt hộ `contentPage`.

---

## Khi nào được tạo layout MỚI

Đây là chỗ dễ đẻ ra 10 layout nhất. Quy tắc:

> **Layout mới chỉ khi KHUNG khác — không phải khi NỘI DUNG khác.**

Khung = thanh nav, chân trang, khối bao ngoài. Nội dung khác nhau là chuyện
bình thường, đó là lý do có nhiều mảnh nội dung chứ không phải nhiều layout.

### Bảng quyết định — hỏi lần lượt, dừng ở câu đầu tiên trả lời "có"

| # | Câu hỏi | Có → dùng |
|:-:|---------|-----------|
| 1 | Có thanh nav như trang chủ không? | `main` |
| 2 | Người chưa đăng nhập, cần ô nhập giữa màn hình? | `auth` |
| 3 | Có menu bên trái của quản trị? | `admin` |
| 4 | Toàn màn hình, bỏ hết thứ gây phân tâm để đọc? | `reader` |
| 5 | Không câu nào ở trên | **`main`** — đừng tạo mới |

Câu 5 quan trọng nhất. Mặc định là `main`, không phải "tạo cái mới cho chắc".

### Ví dụ áp dụng

| Trang mới | Layout | Vì sao |
|-----------|--------|--------|
| Trang cá nhân tác giả | `main` | vẫn nav + footer như thường |
| Kết quả tìm kiếm | `main` | y hệt kho truyện, chỉ khác dữ liệu |
| Quên mật khẩu | `auth` | chưa đăng nhập, một ô nhập |
| Thống kê lượt đọc | `admin` | nằm trong khu quản trị |
| Xem trước chương | `reader` | cần y hệt trải nghiệm đọc |

**5 trang mới, 0 layout mới.**

---

## CSS — 6 file, nạp theo tầng

```
assets/css/
├── base.css           biến màu, reset, typography     — MỌI trang
├── components.css     nút, thẻ, tag, form, bảng       — MỌI trang
├── layout-main.css    header, nav, hero, footer
├── layout-auth.css    card giữa màn hình
├── layout-reader.css  cỡ chữ đọc, chế độ giấy
└── layout-admin.css   sidebar, bảng quản trị
```

`parts/head.jsp` nạp theo thứ tự — file sau ghi đè file trước:

```
base.css → components.css → layout-{tên}.css → {pageCss}.css (tuỳ chọn)
```

Layout mới = thêm đúng **1** file `layout-*.css`. Không tách nhỏ hơn.

---

## Bốn thứ sẽ làm nó rối — đừng làm

| Đừng | Vì sao |
|------|--------|
| **Chia vừa theo tầng vừa theo tính năng** (`dao/story/StoryDAO.java`) | 6 DAO không cần thư mục con. Thêm một cấp là thêm một chỗ phải nghĩ |
| **Mỗi trang một layout** | 25 layout gần giống hệt nhau. Sửa logo phải sửa 25 chỗ |
| **Thêm tầng Service** khi controller còn mỏng | `Controller → Service → DAO` mà Service chỉ gọi xuyên qua thì nó là tầng thừa |
| **Mỗi trang một file CSS** | 25 file CSS, không biết class nào định nghĩa ở đâu |

---

## Cách thêm một tính năng — quy trình cố định

Ví dụ: **thêm chức năng tìm kiếm truyện**

1. `StoryDAO` — thêm method `search(String keyword)` ← **file có sẵn**
2. `StoryServlet` — thêm nhánh `else if (action.equals("search"))` ← **file có sẵn**
3. `views/story/search.jsp` — mảnh nội dung mới ← **file mới, 1 cái**
4. Servlet đặt `contentPage = ".../search.jsp"`, forward tới `layout/main.jsp`

**1 file mới. 0 thư mục mới. 0 layout mới.**

Gần như mọi tính năng đều theo đúng 4 bước này. Nếu bạn thấy mình sắp tạo thư
mục mới hay layout mới, dừng lại đọc lại bảng quyết định ở trên — thường là
không cần.
