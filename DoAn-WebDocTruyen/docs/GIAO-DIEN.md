# 🎨 TỔNG HỢP TOÀN BỘ GIAO DIỆN HỆ THỐNG WEB ĐỌC TRUYỆN

> **Tài liệu đặc tả kiến trúc giao diện (Frontend Architecture), bản đồ 36 trang web JSP, 5 layouts, 14 view partials và các module nâng cấp UI/UX.**  
> *Được biên soạn và đối chiếu từ bản quy hoạch giao diện ban đầu đến phiên bản hoàn thiện hiện tại.*

---

## 📑 MỤC LỤC
1. [Đối chiếu: Kế hoạch Giao diện Ban đầu vs Thực tế Hiện tại](#1-đối-chiếu-kế-hoạch-giao-diện-ban-đầu-vs-thực-tế-hiện-tại)
2. [Kiến trúc Frontend & Hệ thống CSS 4 Tầng](#2-kiến-trúc-frontend--hệ-thống-css-4-tầng)
3. [Hệ thống 5 Khung Bố cục (Layout Architecture)](#3-hệ-thống-5-khung-bố-cục-layout-architecture)
4. [Bản đồ Chi tiết 36 Trang Web JSP](#4-bản-đồ-chi-tiết-36-trang-web-jsp)
5. [Danh mục 14 Mảnh Giao diện Tái sử dụng (_partials)](#5-danh-mục-14-mảnh-giao-diện-tái-sử-dụng-_partials)
6. [Các Điểm sáng & Cải tiến Giao diện Nâng cao Đã Thêm](#6-các-điểm-sáng--cải-tiến-giao-diện-nâng-cao-đã-thêm)
7. [Chuẩn Trợ năng (A11y) & Tương thích Di động 360px](#7-chuẩn-trợ-năng-a11y--tương-thích-di-động-360px)

---

## 1. Đối chiếu: Kế hoạch Giao diện Ban đầu vs Thực tế Hiện tại

Trước đây, đồ án khởi đầu với bản kế hoạch giao diện dự kiến trong `ke-hoach-frontend.md` (và file ghi chú `Giao Dien.docx`):
- Dự kiến: **30 trang**, **4 layouts**, **9 mảnh tái dùng**.
- Tiến độ ban đầu: Đạt 19/30 trang.

Hiện tại, sau khi hoàn thiện toàn bộ **18 ISSUE công nghệ cao**, hệ thống giao diện đã được mở rộng và hoàn thiện trọn vẹn:

| Thành phần giao diện | Kế hoạch ban đầu | Hệ thống thực tế hiện tại | Tăng trưởng / Nâng cấp |
|---|:---:|:---:|---|
| **Số lượng trang web (Views)** | 30 trang | **36 trang JSP hoàn chỉnh** | Thêm 6 trang mới (Thống kê tác giả, Tìm kiếm FULLTEXT, Lỗi 403, Raw view,...) |
| **Khung bố cục (Layouts)** | 4 layouts | **5 layouts chuẩn mực** | Bổ sung thêm Layout `editor.jsp` chuyên biệt cho sáng tác |
| **Mảnh tái dùng (Partials)** | 9 partials | **14 partials** | Thêm `_daily-chart.jsp`, `_stat-tile.jsp`, `_rating-stars.jsp`, `_empty.jsp`... |
| **Hệ thống CSS** | CSS đơn lẻ | **Kiến trúc CSS 4 tầng tinh gọn** | Tiết kiệm băng thông, nạp đúng CSS cần thiết theo khung |
| **Trải nghiệm di động** | Desktop thường | **Mobile-First chuẩn 360px** | Vùng chạm 44px WCAG 2.1, font scale mượt mà trên smartphone |
| **Môi trường đọc (Reader)** | 2 theme | **3 Theme (Dark / Light / Sepia)** | Tùy chỉnh font Serif/Sans, cỡ chữ linh hoạt, giãn dòng quang học |
| **Trợ năng (A11y)** | Cơ bản | **Đạt chuẩn WCAG 2.1** | Phím Tab `:focus-visible`, nút `.skip-link`, `prefers-reduced-motion` |
| **Khả năng ngoại tuyến** | Không | **PWA & Service Worker** | Cài đặt HomeScreen, chiến lược Cache-First đọc offline |

---

## 2. Kiến trúc Frontend & Hệ thống CSS 4 Tầng

Giao diện đồ án được xây dựng theo triết lý **Vanilla hiện đại** — không sử dụng các framework cồng kềnh (như Bootstrap hay Tailwind), giúp trang tải siêu nhanh và kiểm soát 100% từng pixel.

```text
webapp/assets/css/
├── base.css            ← TẦNG 1: Biến màu HSL, reset hiện đại, typography, biến kích thước
├── components.css      ← TẦNG 2: Nút bấm (btn), thẻ (card), badge, form, bảng, modal, toast
├── layout-*.css        ← TẦNG 3: Bố cục riêng của từng khung (main, auth, reader, admin, editor)
└── pages/ (pageCss)    ← TẦNG 4: Tuỳ biến riêng cho duy nhất 1 trang đặc thù (nếu có)
```

### Bảng màu Thương hiệu Chuẩn mực (Design Tokens trong `base.css`):
- **Gam màu chủ đạo (Primary):** Xanh mực học thuật và chàm hoàng hôn `#2563eb` / `#1d4ed8`
- **Màu nền sáng (Light Theme):** `#f8fafc` — `#ffffff` với viền kính mờ tinh tế
- **Màu nền tối (Dark Theme):** `#0f172a` — `#1e293b` (chống mỏi mắt tuyệt đối)
- **Màu nền giấy cũ (Sepia Theme):** `#fbf0d9` — sắc độ vàng dịu chuẩn sách giấy cổ điển

---

## 3. Hệ thống 5 Khung Bố cục (Layout Architecture)

Toàn bộ 36 trang web của hệ thống được bọc bởi **5 layout chuẩn mực** để đảm bảo tính nhất quán cao nhất:

### 1. `layout/main.jsp` — Khung chính ứng dụng
- **Cấu trúc:** Thanh điều hướng (Navbar kính mờ dính khi cuộn) + Khối vỏ nội dung (Shell 1200px) + Chân trang (Footer thương hiệu).
- **Phục vụ:** 20 trang công cộng, trang cá nhân thành viên, kho truyện, bảng xếp hạng.

### 2. `layout/auth.jsp` — Khung xác thực tối giản
- **Cấu trúc:** Không thanh menu điều hướng gây phân tâm, khối thẻ căn giữa màn hình (Card 420px), logo nhận diện thương hiệu trên cùng, liên kết hỗ trợ bên dưới.
- **Phục vụ:** 4 trang: Đăng nhập, Đăng ký, Quên mật khẩu, Đặt lại mật khẩu.

### 3. `layout/reader.jsp` — Khung đọc truyện chuyên sâu
- **Cấu trúc:** Thanh điều khiển đọc tối giản trên cùng (chọn theme, chỉnh cỡ chữ, mục lục nhanh), cột văn bản cố định **38em** (chuẩn khoa học thị giác giúp mắt không phải đảo xa), phông Lora có chân cao cấp, không footer.
- **Phục vụ:** Trang đọc chương truyện (`common/chapter/read.jsp`).

### 4. `layout/admin.jsp` — Khung bảng điều khiển quản trị
- **Cấu trúc:** Thanh tiêu đề quản trị + Menu điều hướng dạng cột bên trái (Sidebar dính khi cuộn) + Khu vực nội dung bảng dữ liệu cuộn ngang linh hoạt.
- **Phục vụ:** 6 trang quản trị: Dashboard thống kê, Quản lý truyện, Tài khoản, Thể loại, Bình luận, Báo cáo.

### 5. `layout/editor.jsp` — Khung soạn thảo chương truyện tập trung (Author Studio)
- **Cấu trúc:** Khung soạn thảo cao cấp (900px), thanh Author Toolbar tiện ích, ô soạn thảo min-height 60vh font Lora serif, chế độ toàn màn hình Zen Mode, tab Xem trước tức thì (Reader Preview), thanh thống kê từ/ký tự/thời gian đọc và tự động lưu nháp `localStorage`.
- **Phục vụ:** Trang thêm và sửa chương truyện (`user/chapter/form.jsp`).

---

## 4. Bản đồ Chi tiết 36 Trang Web JSP

### A. Nhóm Khám phá & Đọc truyện Công cộng (11 trang)

| # | Trang web | URL truy cập | Layout | Mảnh JSP nội dung | Điểm sáng giao diện |
|:-:|---|---|---|---|---|
| 1 | **Trang chủ** | `/` | `main` | `common/home.jsp` | Hero banner ấn tượng, slider truyện nổi bật, khối *"Tiếp tục đọc"* quay lại đọc ngay |
| 2 | **Kho truyện** | `/story?action=list` | `main` | `common/story/list.jsp` | Thanh lọc 10+ thể loại, chọn trạng thái Đang ra / Hoàn thành, lưới thẻ bìa 3:4 |
| 3 | **Tìm kiếm sâu nội dung** | `/story?action=search&q=...` | `main` | `common/story/search.jsp` | Tìm kiếm FULLTEXT, hiển thị trích đoạn ngữ cảnh kèm bôi đậm từ khóa `<mark>` |
| 4 | **Chi tiết truyện** | `/story?action=detail&id=...` | `main` | `common/story/detail.jsp` | Bìa sách lớn, đánh giá 5 sao, danh sách chương, bình luận lồng nhau, nút Tặng xu, Gợi ý truyện |
| 5 | **Đọc chương truyện** | `/chapter?action=read&id=...` | `reader` | `common/chapter/read.jsp` | 3 theme Sáng/Tối/Sepia, tăng giảm cỡ chữ, phím `←`/`→`, bình luận chân chương |
| 6 | **Xem nội dung thô (Raw)** | `/chapter?action=raw&id=...` | *Raw* | `common/chapter/raw.jsp` | Trả nội dung text thuần không viền, phục vụ tải nhanh hoặc cuộn vô tận |
| 7 | **Mục lục thả xuống** | `/chapter?action=toc&storyId=...` | *Raw* | `common/chapter/toc.jsp` | Menu danh sách chương dạng popup nạp bất đồng bộ không gây giật lag |
| 8 | **Bảng xếp hạng** | `/rank` hoặc `/rank?by=authors` | `main` | `common/rank.jsp` | Tab chuyển đổi BXH Truyện và BXH Tác giả xuất sắc, huy chương vàng/bạc/đồng |
| 9 | **Hướng dẫn sử dụng** | `/page?name=guide` | `main` | `common/page/guide.jsp` | Giao diện tài liệu trực quan, các khối ghi chú Alert rõ ràng |
| 10 | **Nội quy cộng đồng** | `/page?name=rules` | `main` | `common/page/rules.jsp` | Bảng quy ước ứng xử, phân cấp vi phạm và các mức xử lý minh bạch |
| 11 | **Hồ sơ tác giả công khai** | `/user?action=profile&id=...` | `main` | `user/profile.jsp` | Avatar lớn, bio, tổng lượt đọc, nút Theo dõi / Bỏ theo dõi, nút Tặng xu ủng hộ |

### B. Nhóm Xác thực & Bảo mật Tài khoản (4 trang)

| # | Trang web | URL truy cập | Layout | Mảnh JSP nội dung | Điểm sáng giao diện |
|:-:|---|---|---|---|---|
| 12 | **Đăng nhập** | `/auth?action=login` | `auth` | `auth/login.jsp` | Đăng nhập mật khẩu + Đăng nhập Google One-Tap/OIDC qua Firebase |
| 13 | **Đăng ký thành viên** | `/auth?action=register` | `auth` | `auth/register.jsp` | Form đăng ký tinh gọn, tích hợp Google reCAPTCHA v3 và checkbox đồng ý nội quy |
| 14 | **Quên mật khẩu** | `/auth?action=forgot` | `auth` | `auth/forgot.jsp` | Ô nhập email nhận liên kết đặt lại mật khẩu với giao diện gửi thư SMTP thật |
| 15 | **Đặt lại mật khẩu** | `/auth?action=reset&token=...` | `auth` | `auth/reset.jsp` | Form nhập mật khẩu mới với thanh đo độ mạnh mật khẩu và token bảo mật |

### C. Nhóm Thành viên & Độc giả Cá nhân (8 trang)

| # | Trang web | URL truy cập | Layout | Mảnh JSP nội dung | Điểm sáng giao diện |
|:-:|---|---|---|---|---|
| 16 | **Trung tâm cá nhân & Ví xu** | `/user?action=me` | `main` | `user/me.jsp` | Thẻ hồ sơ tổng quan, huy hiệu vai trò, số dư Ví xu ảo và liên kết mạng xã hội |
| 17 | **Chỉnh sửa thông tin hồ sơ** | `/user?action=edit` | `main` | `user/edit.jsp` | Đổi tên, tải avatar, cập nhật bio, cơ chế Unlink Guard gỡ liên kết Google an toàn |
| 18 | **Đổi mật khẩu tài khoản** | `/user?action=password` | `main` | `user/edit.jsp` | Form đổi mật khẩu băm PBKDF2 với xác thực mật khẩu cũ |
| 19 | **Truyện đã lưu (Bookmarks)** | `/bookmark?action=list` | `main` | `user/bookmarks.jsp` | Danh sách truyện đánh dấu dạng thẻ ngang, nút "Đọc tiếp" nhảy tới đúng chương dở |
| 20 | **Lịch sử đọc truyện** | `/history` | `main` | `user/history.jsp` | Dòng thời gian các chương đã đọc, nút xoá từng mục hoặc làm sạch lịch sử |
| 21 | **Tác giả đang theo dõi** | `/follow?action=list` | `main` | `user/following.jsp` | Danh sách các tác giả đã bấm follow kèm số truyện mới nhất |
| 22 | **Hộp thông báo cá nhân** | `/notification?action=list` | `main` | `user/notifications.jsp` | Danh sách thông báo chương mới và phản hồi bình luận, đánh dấu đã đọc |
| 23 | **Gửi báo cáo vi phạm** | `/report?action=create` | `main` | `user/report.jsp` | Form tố cáo phân 8 nhóm vi phạm, cho phép đính kèm ảnh bằng chứng trực quan |

### D. Nhóm Tác giả & Sáng tác — Author Studio (4 trang)

| # | Trang web | URL truy cập | Layout | Mảnh JSP nội dung | Điểm sáng giao diện |
|:-:|---|---|---|---|---|
| 24 | **Tủ truyện của tôi** | `/story?action=mine` | `main` | `user/story/mine.jsp` | Bảng quản lý truyện sáng tác, chuyển đổi trạng thái Bản nháp / Công khai |
| 25 | **Đăng & Chỉnh sửa truyện** | `/story?action=create\|edit`| `main` | `user/story/form.jsp` | Chọn đa thể loại (kèm đếm số lượng), **Live Slug Preview**, đếm ký tự tiêu đề (0/200) & giới thiệu (0/5000), **Live Preview ảnh bìa 3:4** tức thì |
| 26 | **Thống kê truyện tác giả** | `/story?action=stats` | `main` | `user/story/stats.jsp` | 3 ô chỉ số (view, bookmark, comment) + **Biểu đồ cột 14 ngày (Daily Chart)** |
| 27 | **Soạn thảo chương truyện** | `/chapter?action=create\|edit`| `editor` | `user/chapter/form.jsp` | **Author Toolbar** (B, I, S, Thoại —, Hoa thị ❖, Trích dẫn, Lời nhắn, Dọn dẹp, Thụt lề), **Tab Xem trước (Reader Preview)**, **Zen Mode**, **Auto-save Nháp**, **Live Stats (từ, ký tự, đoạn, phút đọc)** |

### E. Nhóm Quản trị viên — Admin Dashboard (6 trang)

| # | Trang web | URL truy cập | Layout | Mảnh JSP nội dung | Điểm sáng giao diện |
|:-:|---|---|---|---|---|
| 28 | **Bảng điều khiển quản trị** | `/admin/dashboard` | `admin` | `admin/dashboard.jsp` | Thống kê tổng số truyện, người dùng, lượt xem, biểu đồ tăng trưởng 14 ngày |
| 29 | **Quản lý toàn bộ kho truyện** | `/admin/story` | `admin` | `admin/stories.jsp` | Danh sách toàn bộ truyện, bộ lọc trạng thái, thao tác gỡ/khôi phục xóa mềm |
| 30 | **Quản lý tài khoản người dùng** | `/admin/user` | `admin` | `admin/users.jsp` | Danh sách thành viên, nâng/hạ quyền Admin, modal khóa tài khoản kèm lý do |
| 31 | **Quản lý thể loại (Tags)** | `/admin/tag` | `admin` | `admin/tags.jsp` | Thêm thể loại mới, chỉnh sửa slug, đếm số lượng tác phẩm thuộc từng thể loại |
| 32 | **Xử lý báo cáo vi phạm** | `/admin/report` | `admin` | `admin/reports.jsp` | Danh sách tố cáo từ người dùng, xem ảnh bằng chứng, duyệt hoặc bỏ qua |
| 33 | **Kiểm duyệt bình luận** | `/admin/comment` | `admin` | `admin/comments.jsp` | Bảng bình luận toàn site, nút ẩn bình luận phản cảm giữ lại chứng cứ CSDL |

### F. Nhóm Trang Báo lỗi Tùy biến — Custom Error Pages (3 trang)

| # | Trang web | URL truy cập | Layout | Mảnh JSP nội dung | Điểm sáng giao diện |
|:-:|---|---|---|---|---|
| 34 | **Báo lỗi 403 (Forbidden)** | `/error?code=403` | `main` | `error/403.jsp` | Thông báo không đủ quyền truy cập, nút điều hướng về trang chủ thân thiện |
| 35 | **Báo lỗi 404 (Not Found)** | `/error?code=404` | `main` | `error/404.jsp` | Giao diện đồng bộ phong cách truyện, hướng dẫn tìm lại tác phẩm |
| 36 | **Báo lỗi 500 (Server Error)** | `/error?code=500` | `main` | `error/500.jsp` | Che giấu stack trace nhạy cảm của máy chủ, hiển thị thông báo lịch sự |

---

## 5. Danh mục 14 Mảnh Giao diện Tái sử dụng (_partials)

Hệ thống sử dụng các mảnh giao diện nhỏ trong thư mục `WEB-INF/views/_partials/` để tái sử dụng trên nhiều trang khác nhau:

1. **`_card.jsp`**: Thẻ truyện hiển thị ảnh bìa tỉ lệ 3:4, tiêu đề cắt dòng thông minh, tên tác giả, nhãn thể loại và lượt xem.
2. **`_story-row.jsp`**: Hàng truyện hiển thị theo chiều ngang (ảnh bìa nhỏ + tiêu đề + thống kê), dùng cho trang Tủ truyện, Bookmarks, Lịch sử.
3. **`_chapter-list.jsp`**: Khung danh sách mục lục các chương truyện có phân trang hoặc hiển thị cuộn.
4. **`_comment.jsp`**: Khung một bình luận đơn lẻ: Avatar người dùng, tên tác giả (kèm huy hiệu tác giả), thời gian, nút trả lời lồng nhau, nút thả tim và nút xoá.
5. **`_avatar.jsp`**: Thành phần hiển thị avatar bo tròn, tự động tạo fallback chữ cái đầu trên nền màu nếu người dùng chưa có ảnh đại diện.
6. **`_cover.jsp`**: Khung ảnh bìa sách với tỉ lệ chuẩn 3:4, hiệu ứng bo góc và bóng đổ mềm (soft shadow).
7. **`_rating-stars.jsp`**: Cụm 5 ngôi sao hiển thị điểm trung bình và hỗ trợ tương tác rê chuột/chấm điểm trực tiếp.
8. **`_daily-chart.jsp`**: Biểu đồ cột mini (Bar chart) vẽ bằng CSS thuần, hiển thị biến thiên lượt xem trong 14 ngày gần nhất.
9. **`_stat-tile.jsp`**: Khối ô thống kê chỉ số (Icon nổi bật + Số liệu lớn + Nhãn giải thích), dùng ở trang Dashboard và Stats.
10. **`_pagination.jsp`**: Thanh điều hướng phân trang thông minh: tự động tính trang đầu, trang cuối, dấu ba chấm `...` và nút Trước/Sau.
11. **`_tag-filter.jsp`**: Dãy nút bấm dạng chip (Tag chips) hiển thị danh sách thể loại với trạng thái active được highlight.
12. **`_empty.jsp`**: Khung thông báo trạng thái rỗng (Empty state) khi không có truyện/bình luận, kèm biểu tượng và nút kêu gọi hành động.
13. **`_block.jsp`**: Mảnh phân tách đoạn văn bản nội dung chương truyện.
14. **`head.jsp`, `nav.jsp`, `footer.jsp`**: Các mảnh thành phần khung cố định trong `layout/parts/`.

---

## 6. Các Điểm sáng & Cải tiến Giao diện Nâng cao Đã Thêm

So với bản thiết kế sơ khai, đồ án đã được tích hợp thêm **9 module giao diện cao cấp**:

1. **Live Preview Ảnh Bìa 3:4 Tức thì (ISSUE-017):**
   - Khi tác giả chọn file ảnh bìa từ máy tính, FileReader API JavaScript lập tức nạp và render khung xem trước tỉ lệ 3:4 chuẩn bìa sách ngay trên màn hình trước khi nhấn nút Lưu.
2. **Tiện ích Chia sẻ Truyện 1 Chạm & Toast Notifications (ISSUE-018):**
   - Nút chia sẻ tích hợp Clipboard API sao chép link truyện vào bộ nhớ tạm thời chỉ bằng 1 cú click chuột, đồng thời kích hoạt Toast thông báo nổi siêu mượt ở góc màn hình.
3. **Modal Tặng Xu Ảo & Ví Cá Nhân (ISSUE-008):**
   - Hộp thoại Modal bật lên trực tiếp tại trang Chi tiết truyện và Hồ sơ tác giả, cho phép độc giả chọn mức xu (10, 50, 100 xu...) và gửi lời chúc động viên tác giả không cần chuyển trang.
4. **Biểu đồ Cột Lượt Đọc 14 Ngày (Daily Chart):**
   - Biểu đồ tăng trưởng trực quan được xây dựng hoàn toàn bằng CSS flexbox và phần trăm chiều cao cột, không phụ thuộc vào bất kỳ thư viện JS bên ngoài nào (như Chart.js), đảm bảo tốc độ tải trang tức thì.
5. **Trình Soạn Thảo Layout Editor Độc Lập:**
   - Cung cấp trải nghiệm viết truyện không phân tâm, bộ đếm số từ theo thời gian thực giúp tác giả theo dõi độ dài chương.
6. **Form Báo Cáo Vi Phạm Nâng Cấp (ISSUE-025):**
   - Giao diện nộp tố cáo phân rõ 8 nhóm hành vi vi phạm, cho phép chọn và xem trước nhiều tệp ảnh chụp bằng chứng từ máy.
7. **PWA HomeScreen & Offline Reading (ISSUE-010):**
   - Giao diện có khả năng "Cài đặt ứng dụng" lên màn hình điện thoại (HomeScreen App), biểu tượng ứng dụng và màn hình chờ (Splash screen) chuẩn Web App Manifest.
8. **Hiệu ứng Kính Mờ & Dark Mode Đẳng Cấp:**
   - Sử dụng `backdrop-filter: blur(12px)` cho thanh menu điều hướng dính, tạo cảm giác hiện đại và cao cấp giống các nền tảng đọc truyện quốc tế.
9. **Nút Nổi Chuyển Nhanh Xuống Bình Luận Kiểu Chat AI (Floating Comment Button):**
   - Cố định ở góc dưới bên phải màn hình đọc truyện (`#reader-fab-comment`), nút hình tròn nổi bật với gradient Amber ấm áp, animation nhịp thở nhẹ nhàng, icon mũi tên xuống kèm badge chat `💬` và tooltip nổi. Khi bấm sẽ cuộn mượt (smooth scroll) thẳng xuống khu vực bình luận `#comments` và tự động focus vào khung nhập nội dung.
10. **Bộ Công Cụ Soạn Thảo Tác Giả & Không Gian Viết Zen Mode (Author Studio Toolbar):**
   - Thanh công cụ chuyên dụng cho tác giả viết tiểu thuyết/truyện:
     - Nhóm văn học: Nút `— Thoại` (chèn gạch đầu dòng thoại chuẩn), `❖ Phân đoạn` (hoa thị ngắt cảnh), `❝ Trích dẫn` (suy nghĩ/thơ), `📝 Lời nhắn` (khối ghi chú tác giả).
     - Nhóm định dạng & chuẩn hóa: `B` (Đậm), `I` (Nghiêng), `S` (Gạch ngang), `🧹 Làm sạch` (xoá khoảng trắng thừa/dòng trống kép), `⇥ Thụt lề` (thụt đầu dòng chuẩn 4 spaces).
     - Tab chuyển đổi `✏️ Soạn thảo` và `👁️ Xem trước (Reader Preview)`: Tái hiện chuẩn xác font Lora Serif, cỡ chữ, line-height và lề đoạn văn y như độc giả nhìn thấy trên trang đọc.
     - Thanh thống kê thời gian thực ở chân trang: Số từ, số ký tự, số đoạn, thời gian đọc ước tính (`~ X phút đọc`) và badge đánh giá độ dài chương.
     - Chế độ toàn màn hình không phân tâm (**Zen Mode ⛶**): Loại bỏ hoàn toàn menu và chân trang, chỉ còn không gian viết chữ thuần khiết.
     - Cơ chế tự động lưu nháp an toàn (**Auto-save localStorage**): Tự động lưu sau mỗi lần gõ kèm hộp cảnh báo khôi phục nháp nếu vô tình tắt trình duyệt.
     - Phím tắt tiện lợi: `Ctrl+B`, `Ctrl+I`, `Ctrl+S` (lưu form nhanh).
11. **Hệ Thống Live Validation & Character Counters Trực Quan:**
   - **Form Thêm/Sửa truyện:** Bộ đếm ký tự tiêu đề thời gian thực (`0/200 ký tự`), bộ đếm giới thiệu (`0/5.000 ký tự`), **Live Slug Preview** (tự động chuyển tiếng Việt sang đường dẫn thân thiện `/truyen/...` ngay khi gõ), bộ đếm số lượng thể loại đã tích chọn (`· Đã chọn X thể loại`).
   - **Client & Server Validation đa tầng:** Chặn gửi form nếu tiêu đề/nội dung chỉ toàn dấu cách trắng, cảnh báo kích thước ảnh bìa vượt quá 2MB ngay khi chọn tệp, xác thực token CSRF và kiểm tra quyền sở hữu tác giả ở backend.
12. **Bộ Parse Định Dạng Văn Học Chuẩn Mực Trên Trang Đọc (Reader Typography):**
   - Khi tác giả sử dụng các ký hiệu Markdown văn học (`**đậm**`, `*nghiêng*`, `~~gạch~~`, `* * *`, `>`, `—`), trang đọc (`layout/reader.jsp`) tự động chuyển đổi an toàn thành các thẻ HTML tao nhã:
     - `* * *` hoặc `❖ ❖ ❖` thành phân đoạn `.divider` căn giữa với đường kẻ cánh hai bên màu hổ phách.
     - Dòng trích dẫn `>` thành thẻ `<blockquote>` viền trái Amber sang trọng.
     - Lời thoại `—` thành `.dialogue` có thụt lề chuẩn sách in.
     - Lời nhắn tác giả `[Tác giả: ...]` thành `.author-note` viền nét đứt kèm icon ✍️.
   - Cơ chế an toàn 100% trước XSS: Server escape toàn bộ ký tự nguy hiểm bằng `<c:out>` trước, Client chỉ parse cú pháp văn học. Áp dụng đồng bộ cho cả chế độ đọc đơn chương và đọc liên tục (Continuous Infinite Reading).
13. **Bảng Quản Trị & Duyệt Truyện Nâng Cao (Admin Story Moderation):**
   - Hệ thống tab lọc trạng thái trực quan: Tất cả, Bản nháp / Chờ duyệt (`DRAFT`), Đã xuất bản (`PUBLISHED`), Đã gỡ (`DELETED`) kèm huy hiệu đếm số lượng thời gian thực.
   - Thanh Quick Filter tìm kiếm tức thì theo tên truyện hoặc tác giả mà không cần tải lại trang.
   - Nút `✓ Duyệt đăng` 1-chạm nổi bật màu ngọc lục bảo (Emerald Green) giúp Admin xét duyệt truyện lên kệ chỉ với 1 click.
14. **Bàn Xử Lý Báo Cáo Vi Phạm Thông Minh (Admin Report Desk):**
   - Phân loại rõ 8 nhóm vi phạm chuẩn mực (Đạo văn, Đồi trụy, Bạo lực, Quấy rối, Thù địch, Lừa đảo, Sai thể loại, Khác), ưu tiên hiển thị 3 nhóm nghiêm trọng lên đầu danh sách.
   - Hỗ trợ xem ảnh bằng chứng thumbnail lazy load, bấm để phóng to toàn màn hình.
   - Thao tác xử lý 1-chạm: "Gỡ nội dung & Đóng báo cáo" hoặc "Bác bỏ báo cáo".
15. **Đọc Truyện Bằng Giọng Nói Tiếng Việt (Text-to-Speech 🎧 Audio Player):**
   - Tích hợp trực tiếp **Web Speech API** chuẩn trình duyệt không cần thư viện ngoài.
   - Thanh điều khiển Audio Bar dạng Floating Pill thiết kế Glassmorphism sang trọng: Phát / Tạm dừng (▶/⏸), Dừng hẳn (⏹), Tua đoạn trước/tiếp (⏮/⏭), đổi tốc độ đọc linh hoạt (`0.8x`, `1.0x`, `1.25x`, `1.5x`).
   - Tự động nhận diện Voice tiếng Việt (`vi-VN`), highlight đoạn văn bản đang đọc với đường viền hổ phách và tự động cuộn màn hình theo nhịp đọc.
16. **Âm Thanh Mưa Rơi Thư Giãn (Ambient Rain Sound Generator 🌧️):**
   - Sinh tiếng mưa rơi tự nhiên 100% bằng **Web Audio API** (tạo tín hiệu Pink Noise qua bộ lọc Biquad Lowpass filter 850Hz).
   - Không tốn băng thông, không cần tải file mp3, hoạt động offline mượt mà chỉ với 1 click vào nút `🌧️` trên thanh công cụ.
17. **Chế Độ In Ấn & Xuất Sách Ebook Chuẩn Mực (Print-Friendly CSS 🖨️):**
   - Bấm nút `🖨️` hoặc phím tắt `Ctrl + P`, toàn bộ thanh công cụ, menu, bình luận, nút nổi tự động được ẩn đi.
   - Trình bày truyện với font chữ Georgia Serif chuẩn mực, màu mực đen trên nền giấy trắng, ngắt trang thông minh (orphans/widows) sẵn sàng xuất ra file PDF hoặc in trực tiếp.
18. **Cụm Nút Điều Hướng Nổi Floating Dock & Thanh Tiến Độ Đọc (Reading Progress Bar):**
   - Cụm dock tròn 2 nút xếp chồng lên nhau ở góc phải dưới (`#reader-fab-dock`):
     - Nút Mũi tên lên (`↑`): Kính mờ sang trọng, tự động hiện khi cuộn qua 200px, bấm vào cuộn mượt về đầu trang.
     - Nút Mũi tên xuống (`↓` 💬): Gradient cam Amber ấm áp, bấm vào cuộn thẳng xuống khu vực bình luận và trỏ focus vào ô nhập.
   - Thanh tiến độ đọc mỏng (3px) chạy ngang mép đỉnh màn hình theo tỉ lệ % cuộn trang thực tế (`0%` $\rightarrow$ `100%`).
19. **Bộ Lọc Nhanh Chương Mục Lục & Ước Tính Thời Gian Đọc Tự Động:**
   - **Trang Chi tiết truyện:** Ô tìm kiếm tức thì `🔍 Tìm nhanh chương...` trong thanh công cụ mục lục, gõ số hoặc tên chương để lọc danh sách ngay lập tức không cần chuyển trang.
   - **Trang Đọc chương:** Tự động đếm số từ và ước tính thời gian đọc hiển thị dưới tiêu đề (`⏱️ ~X phút đọc (Y từ)`), áp dụng đồng bộ cho cả chương mở đầu lẫn các chương cuộn tiếp theo.
20. **Hệ Thống Điểm Nhấn Thị Giác & Viền Phát Sáng Môi Trường (Visual Polish & Ambient Glow Suite):**
   - **Thanh Header:** Bổ sung viền ánh sáng hổ phách gradient siêu mỏng ở đáy (`.site-header`) cùng hiệu ứng xoay nhẹ và vầng sáng nổi bật cho biểu tượng logo (`.brand:hover .brand-mark`).
   - **Thẻ Truyện 3D & Chiều Sâu:** Thẻ truyện áp dụng hiệu ứng nâng 3D (`translateY(-6px)`) kết hợp bóng đổ mềm môi trường (`box-shadow: 0 20px 42px rgba(0,0,0,0.52), 0 0 24px rgba(240,134,58,0.16)`) cùng lớp phủ shading mềm mại dưới chân ảnh bìa.
   - **Huy Hiệu & Nút Bấm:** Huy hiệu trạng thái (`Hoàn thành`, `Đang ra`) được nâng cấp màu gradient sống động kèm quầng sáng màu chuyên biệt (colored glow); nút bấm chính có hiệu ứng phản chiếu ánh sáng và phản hồi xúc giác khi tương tác.
21. **Chế Độ Đọc Tập Trung Tuyệt Đối (Zen / Focus Mode 🧘) & Bảng Phím Tắt Trợ Năng (`?`):**
   - **Zen Mode:** Bấm phím `Z` hoặc icon `🧘` trên thanh công cụ, toàn bộ header, nút nổi, thanh điều hướng và bình luận tự động ẩn đi để người đọc đắm chìm hoàn toàn vào con chữ. Di chuột lên đỉnh màn hình hoặc bấm `Z`/`Esc` để thoát chế độ.
   - **Bảng Phím Tắt Toàn Diện (`?` / `⌨`):** Hộp thoại modal hiển thị trực quan các phím tắt nhanh (`←`/`→`: chuyển chương, `T`: mục lục, `F`: toàn màn hình, `Z`: Zen mode, `S`: đọc giọng nói TTS, `M`: âm thanh mưa, `Esc`: đóng).
22. **Thẻ Gợi Ý "Tiếp Tục Đọc" (Recent Reading Resume Card):**
   - Tự động ghi nhớ chương truyện và tiến độ đọc gần nhất vào `localStorage` của trình duyệt.
   - Khi độc giả quay lại Trang chủ, một thẻ nổi bật sẽ xuất hiện ngay đầu trang hiển thị tên truyện, chương đang dở cùng nút **"Đọc tiếp →"** 1-click không cần lục lại lịch sử.
23. **Bộ Lọc Tìm Kiếm Tức Thời Trong Quản Trị Báo Cáo (Admin Reports Quick Filter):**
   - Thêm ô tìm kiếm trực tiếp `🔍 Lọc nhanh theo nội dung, lý do hoặc người báo cáo...` trong trang [admin/reports.jsp](file:///c:/Users/Admin/Downloads/Web/DoAn-WebDocTruyen/src/main/webapp/WEB-INF/views/admin/reports.jsp), lọc kết quả tức thì không cần tải lại trang.




---

## 7. Chuẩn Trợ năng (A11y) & Tương thích Di động 360px

Dự án áp dụng trọn vẹn các tiêu chuẩn trợ năng quốc tế **WCAG 2.1 Level AA** (ISSUE-015):
- **Phím tắt điều hướng:** Người đọc có thể dùng phím mũi tên trái `←` và mũi tên phải `→` để lùi/tiến chương truyện.
- **Vùng chạm cảm ứng di động:** Toàn bộ nút bấm, biểu tượng tương tác, liên kết trên di động đều có kích thước tối thiểu **44x44 pixel** để người dùng chạm ngón tay không bị bấm nhầm.
- **Hỗ trợ độc giả dùng bàn phím:** Đường viền `:focus-visible` màu thương hiệu rõ ràng khi bấm phím Tab; nút `.skip-link` ẩn ở đầu trang giúp người khiếm thị bỏ qua menu để nhảy thẳng vào nội dung chính.
- **Tôn trọng chế độ giảm chuyển động:** Khi hệ điều hành của người dùng bật tùy chọn `prefers-reduced-motion: reduce`, toàn bộ hiệu ứng chuyển cảnh và chuyển động trên web sẽ tự động được tắt để tránh gây chóng mặt.
