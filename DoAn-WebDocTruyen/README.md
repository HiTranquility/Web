# Web Đọc Truyện — Đồ án cuối kỳ

Java Servlet + JSP + MySQL, kiến trúc MVC Model 2.
Server-rendered, không dùng framework frontend.

📖 **Tài liệu dự án:** Xem mục lục chi tiết tại [docs/INDEX.md](docs/INDEX.md).

---

## Lộ trình — 11 CASE

Làm tuần tự, mỗi case chạy được rồi mới sang case sau.

| CASE | Nội dung | Chạm vào | Xong |
|------|----------|----------|:----:|
| **00** | Khung dự án + layout + trang chủ | schema, CSS, HomeServlet | ✅ |
| **01** | Đăng ký / Đăng nhập / Đăng xuất | `AuthServlet`, `AuthFilter`, PBKDF2 | ✅ |
| **02** | Kho truyện — danh sách + phân trang | `StoryServlet?action=list` | ✅ |
| **03** | Tag + lọc truyện + tìm kiếm | `TagDAO`, lọc theo slug | ✅ |
| **04** | Chi tiết truyện + mục lục chương | `action=detail` | ✅ |
| **05** | Đăng / sửa truyện | `action=create\|edit`, **kiểm quyền sở hữu** | ✅ |
| **06** | Chương — đọc, thêm, sửa, xoá | `ChapterServlet`, `layout/reader.jsp` | ✅ |
| **07** | Bình luận | `CommentServlet` | ✅ |
| **08** | Đánh dấu + tự nhớ vị trí đọc | `BookmarkServlet` | ✅ |
| **09** | Tải truyện `.txt` | `DownloadServlet` — không trả HTML | ✅ |
| **10** | Quản trị — gỡ truyện, khoá tài khoản | `admin/*`, `AdminFilter`, `layout/admin.jsp` | ✅ |
| **11** | Hướng dẫn sử dụng + Nội quy | `PageServlet` + checkbox đồng ý | ✅ |

✅ **Dự án đã hoàn thiện trọn vẹn cả 11 CASE và mở rộng toàn diện:**
- Biên dịch sạch toàn bộ mã nguồn Java.
- Cơ sở dữ liệu `webdoctruyen`: 25 bảng (`schema.sql` + các `migration-*.sql`), có dữ liệu mẫu.
  Cài mới bằng `setup-db.ps1` đã được thử trên một MySQL trống.
- Vượt qua toàn bộ **175 bài kiểm thử tự động** (JUnit 5) — `scripts\test.ps1`.
- Các bộ lọc bảo mật (`EncodingFilter`, `CsrfFilter`, `AuthFilter`, `AdminFilter`) hoạt động chính xác, chặn các lỗ hổng phổ biến như SQLi, XSS, CSRF, Account Takeover và Path Traversal.

**Mở rộng sau 11 CASE:** ví xu & chương VIP, đánh giá 5 sao, điểm danh nhận quà, **truyện tranh**
(chương là dãy ảnh, ảnh chương VIP được kiểm quyền), **đánh dấu đoạn / trang** và **đọc tiếp đúng đoạn (`resumePara`)**, khối gợi ý truyện thống nhất, **Trạm kết thúc chương thông minh (Smart Chapter Hub)**, **Tag Pills lọc nhanh 6 chế độ**, **Nhật ký & nhịp đọc 7 ngày (Reading Insights)**, **Thước đo mật khẩu an toàn**, **PWA Offline Reading & đồng bộ `fetch keepalive`**, **Đọc bằng giọng nói (Text-to-Speech)** và **Âm thanh mưa rơi thư giãn (Web Audio API)**.

---

## Cài đặt

### Cách nhanh (khuyên dùng) — một lệnh

```bash
powershell -ExecutionPolicy Bypass -File scripts\setup-db.ps1
```

Script hỏi mật khẩu root MySQL **một lần**, rồi tự: tạo database → tạo tài khoản
`truyen_app` → nạp dữ liệu mẫu → **chạy mọi `database/migration-*.sql` theo thứ tự**
→ sinh `src/main/resources/db.properties`.

> ⚠️ Script **xoá và tạo lại** database `webdoctruyen`. Database đang có dữ liệu thật
> thì dùng cách "Nâng cấp database cũ" bên dưới.

### Cách làm tay

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p < database/setup_user.sql        # nhớ đổi mật khẩu ở dòng IDENTIFIED BY
mysql -u root -p webdoctruyen < database/sample_data.sql
```

Rồi chạy **từng** file `database/migration-*.sql` theo thứ tự số (001 → 029):

```bash
mysql -u root -p webdoctruyen < database/migration-001-google.sql
```

`schema.sql` **chưa có** chương VIP, đánh giá, điểm danh… — bỏ bước migration là mở
chương nào cũng lỗi. Cuối cùng chép `db.properties.example` thành `db.properties` và
điền mật khẩu `truyen_app` (file này đã nằm trong `.gitignore`).

> Không dùng `root` cho ứng dụng: `root` có toàn quyền trên mọi database của máy.

### Nâng cấp database cũ

Chỉ cần chạy lại **các migration** (không chạy `schema.sql` — file đó xoá database).
Mọi migration đều chạy lại được nhiều lần mà không hỏng dữ liệu.

### Tài khoản mẫu

| Đăng nhập | Mật khẩu | Vai trò |
|-----------|----------|---------|
| `admin` | `admin123` | Quản trị viên |
| `mocmien`, `haiduong`, `kiemvu` | `123456` | Tác giả |
| `thuytien` | `123456` | Độc giả |
| `spammer` | `123456` | Đã bị khoá |

### Chạy


```bash
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```

→ <http://localhost:8080/>

Chưa cài database web vẫn chạy ở chế độ xem giao diện (dữ liệu giả), không báo lỗi.

> Tomcat **9**, không phải 10+. Dự án dùng `javax.servlet`, Tomcat 10 đổi sang `jakarta.servlet`.

---

## Cấu trúc

```
DoAn-WebDocTruyen/
├── database/
│   ├── schema.sql              bảng gốc, có chú thích từng cột
│   ├── migration-*.sql         các nâng cấp sau (VIP, đánh giá, truyện tranh…) — chạy theo thứ tự
│   ├── setup_user.sql          tạo tài khoản MySQL riêng cho app
│   └── sample_data.sql         dữ liệu demo
├── docs/                       ERD, use case, báo cáo
└── src/main/
    ├── java/truyen/
    │   ├── model/              JavaBean thuần — chỉ dữ liệu, không SQL
    │   ├── dao/                truy vấn database — không đụng request/response
    │   ├── controller/         servlet — điều phối, không SQL, không HTML
    │   ├── filter/             AuthFilter, AdminFilter
    │   └── util/               DBConnection, PasswordUtil, SlugUtil
    ├── resources/
    │   └── db.properties       ← mật khẩu DB, KHÔNG commit
    └── webapp/
        ├── assets/css/         base + components + layout-* (xem cuối file)
        └── WEB-INF/
            ├── web.xml
            └── views/          TẤT CẢ JSP nằm trong WEB-INF
                ├── layout/     main.jsp, auth.jsp + parts/
                ├── story/      home.jsp, _card.jsp, list.jsp, detail.jsp
                ├── auth/  chapter/  user/  admin/  page/
```

### Ba quy tắc giữ cho dự án không rối

1. **JSP nằm trong `WEB-INF/views/`.** Tomcat chặn truy cập trực tiếp, nên mọi
   đường vào đều buộc phải qua controller. Đây là khác biệt chính so với cách
   sách để JSP ở gốc web.

2. **Một servlet cho một thực thể, rẽ nhánh bằng `?action=`.**
   `/story?action=list|detail|create|edit|delete` — 5 servlet thay vì 20.

3. **Mỗi tầng chỉ làm việc của mình.** DAO không forward. Controller không viết
   SQL. JSP không chứa Java. Phép thử: đổi từ MySQL sang PostgreSQL chỉ phải sửa
   trong `dao/`.

---

## Quyết định thiết kế đáng chú ý

| Chọn | Thay vì | Vì sao |
|------|---------|--------|
| Không có bảng `authors` | bảng riêng cho tác giả | "Tác giả" là *quan hệ* (`stories.author_id`), không phải loại người. Cùng tài khoản vừa viết vừa đọc |
| Xoá mềm (`status='DELETED'`) | `DELETE FROM stories` | Bình luận và bookmark trỏ tới truyện sẽ mồ côi |
| Ban user nhưng giữ truyện | ẩn luôn truyện | Độc giả đang đọc dở không bị mất |
| Bảng nối `story_tags` | cột `tags` chuỗi ngăn phẩy | Lọc bằng `LIKE '%...%'` chậm và khớp nhầm |
| Nội dung chương trong DB | lưu ra file | Không phải quản lý đường dẫn, deploy lại không mất |
| `MEDIUMTEXT` cho nội dung | `TEXT` | `TEXT` chỉ 64 KB, chương dài tiếng Việt vượt và bị **cắt cụt âm thầm** |
| `utf8mb4` | `utf8` | `utf8` của MySQL chỉ 3 byte, không chứa được emoji |

---

## Hai lỗi bảo mật phải tránh — đã phòng sẵn trong CASE 00

**SQL injection** → mọi truy vấn dùng `PreparedStatement` với dấu `?`,
không nối chuỗi. Xem chú thích trong `StoryDAO.findLatest()`.

**XSS** → mọi chỗ in dữ liệu người dùng nhập đều dùng `<c:out value="..."/>`
chứ không phải `${...}`. EL **không** tự escape HTML — đây là lỗ hổng có thật
trong code gốc của sách.

---

## 📚 Tài liệu

👉 **[docs/INDEX.md](docs/INDEX.md)** — mục lục, có bảng "đang phân vân X → mở file nào"

| File | Khi nào mở |
|------|-----------|
| [docs/requirements/DANG-KY-DE-TAI.md](docs/requirements/DANG-KY-DE-TAI.md) | **bản đăng ký đề tài** — danh sách chức năng đề xuất theo mẫu của cô |
| [docs/requirements/MO-TA-DO-AN.md](docs/requirements/MO-TA-DO-AN.md) | **bản mô tả gửi giảng viên** — tên đề tài, 16 chức năng, tài khoản demo |
| [docs/architecture/ke-hoach-frontend.md](docs/architecture/ke-hoach-frontend.md) | **4 layout · giao diện các trang · mảnh tái dùng** — bản đồ giao diện |
| [docs/architecture/ke-hoach-database.md](docs/architecture/ke-hoach-database.md) | **13 bảng** — chuẩn hoá, phi chuẩn hoá, index |
| [docs/architecture/so-do.md](docs/architecture/so-do.md) | **ERD, luồng MVC, kiến trúc** — 7 sơ đồ Mermaid |
| [docs/architecture/cau-truc.md](docs/architecture/cau-truc.md) | **bản đồ phân tầng Java / JSP / CSS** — "file này bỏ vào thư mục nào?" |
| [docs/guides/giai-thich.md](docs/guides/giai-thich.md) | **"cái này chạy kiểu gì, sao lại thế?"** — giảng từ đầu, 7 khu |
| [docs/guides/CHECKLIST.md](docs/guides/CHECKLIST.md) | **bảng kiểm tra trước khi nộp bài** |
| [docs/standards/01-CODING_CONVENTIONS.md](docs/standards/01-CODING_CONVENTIONS.md) | đặt tên Java, contract 4 tầng, bảng tra URL |
| [docs/standards/02-VIEW_CONVENTIONS.md](docs/standards/02-VIEW_CONVENTIONS.md) | layout, scope, **sổ đăng ký attribute**, EL, CSS |
| [docs/standards/03-DATABASE_CONVENTIONS.md](docs/standards/03-DATABASE_CONVENTIONS.md) | đặt tên bảng, kiểu dữ liệu, luật DAO |
| [docs/standards/04-GIT_CONVENTIONS.md](docs/standards/04-GIT_CONVENTIONS.md) | commit message, quy tắc branch |
| [docs/projects/README.md](docs/projects/README.md) | quản lý công việc (ISSUE, bug) và phân công |

Tóm tắt cấu trúc: cả dự án khi xong 11 CASE là **59 file, 13 thư mục, 4 layout**.
Không phình thêm. Thêm tính năng = thêm 1 mảnh JSP + 1 nhánh `?action=`,
không thêm thư mục, không thêm layout.

---

## Layout và CSS — cách chia

### Không có `index.html` nào cả

Đây là chỗ dễ hiểu nhầm nhất. Trong React/Vue có **một** file `index.html` rỗng,
JavaScript chạy trên trình duyệt rồi tự dựng giao diện và đổi layout.

JSP **ngược lại hoàn toàn**: server dựng sẵn HTML hoàn chỉnh cho từng request
rồi gửi đi. Trình duyệt nhận về trang đã xong, không phải lắp gì thêm. Nên không
có file `index.html` nào chứa khung chung.

Nhưng trực giác "phải có chỗ nào đó chứa khung rồi nạp nội dung vào" thì **đúng** —
chỉ khác là nó chạy ở **server**, và nó là file `.jsp`.

### Layout hoạt động thế nào

```
Servlet                          layout/main.jsp              story/home.jsp
   |                                   |                            |
   |-- contentPage = home.jsp -------->|                            |
   |-- forward tới main.jsp ---------->|                            |
                                       |-- dựng <html><head><nav>   |
                                       |-- <jsp:include> ---------->|
                                       |                            |-- chỉ phần ruột
                                       |<---------------------------|
                                       |-- <footer></html>
```

Trong servlet đúng 2 dòng:

```java
request.setAttribute("contentPage", "/WEB-INF/views/story/home.jsp");
forward("/WEB-INF/views/layout/main.jsp");
```

Đổi sang khung khác chỉ cần đổi dòng thứ hai thành `layout/auth.jsp` —
**mảnh nội dung không phải sửa gì**.

### Vì sao bỏ cách header.jsp + footer.jsp

| | header/footer rời | layout wrapper |
|---|---|---|
| Thẻ `<body>` | mở ở file này, đóng ở file kia | **cùng một file** |
| Trang quên include footer | vỡ layout, không báo lỗi | không xảy ra được |
| Thêm layout thứ 3, 4 | phải nhớ đúng cặp header–footer | thêm 1 file, xong |
| Trang nội dung | phải biết mình dùng khung nào | không cần biết gì |

### Cấu trúc views

```
WEB-INF/views/
├── layout/
│   ├── main.jsp          khung chính: nav + nội dung + footer
│   ├── auth.jsp          khung đăng nhập: card giữa màn hình, không nav
│   └── parts/            mảnh dùng chung GIỮA các layout
│       ├── head.jsp      thẻ <head>, nạp CSS theo tầng
│       ├── nav.jsp       thanh menu
│       └── footer.jsp    chân trang
│
├── story/    home.jsp  list.jsp  detail.jsp  form.jsp  _card.jsp
├── auth/     login.jsp  register.jsp
├── chapter/  read.jsp  form.jsp
├── user/     bookmarks.jsp
├── admin/    stories.jsp  users.jsp
└── page/     guide.jsp  rules.jsp  error404.jsp  error500.jsp
```

**Quy ước đặt tên:**

| Dạng | Nghĩa |
|------|-------|
| `home.jsp` | mảnh nội dung — không có `<html>`, servlet trỏ `contentPage` vào |
| `_card.jsp` | mảnh nhỏ tái dùng nhiều nơi, gạch dưới ở đầu |
| `error404.jsp` | trang phóng 3 dòng — đặt `contentPage` rồi gọi layout |
| `_error404.jsp` | nội dung thật của trang lỗi |

Trang lỗi cần cặp phóng/nội dung vì Tomcat forward **thẳng** tới nó, không đi
qua servlet nào, nên không ai đặt hộ `contentPage`.

### CSS chia 4 tầng

```
assets/css/
├── base.css          biến màu, reset, typography, body   — MỌI trang
├── components.css    nút, thẻ, tag, form, bảng, panel    — MỌI trang
├── layout-main.css   header, nav, hero, footer           — layout main
└── layout-auth.css   card giữa màn hình                  — layout auth
```

Nạp trong `parts/head.jsp`, **thứ tự quan trọng** vì file sau ghi đè file trước:

```jsp
base.css  →  components.css  →  ${layoutCss}.css  →  ${pageCss}.css
```

`pageCss` là tuỳ chọn, cho trang nào có CSS riêng (trang đọc chương ở CASE 06
sẽ cần).

**Lợi ích cụ thể:** trang đăng nhập chỉ tải `base + components + layout-auth`,
không phải tải CSS của thanh menu và lưới truyện mà nó không dùng.

### Thêm một layout mới cần làm gì

1. Tạo `layout/reader.jsp` — chép `main.jsp` rồi sửa
2. Tạo `assets/css/layout-reader.css`
3. Trong layout mới đặt `<c:set var="layoutCss" value="layout-reader"/>`
4. Servlet forward tới layout đó

Không phải đụng vào bất kỳ mảnh nội dung nào.
