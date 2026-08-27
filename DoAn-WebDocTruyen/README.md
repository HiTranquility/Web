# Web Đọc Truyện — Đồ án cuối kỳ

Java Servlet + JSP + MySQL, kiến trúc MVC Model 2.
Server-rendered, không dùng framework frontend.

---

## Lộ trình — 11 CASE

Làm tuần tự, mỗi case chạy được rồi mới sang case sau.

| CASE | Nội dung | Chạm vào | Xong |
|------|----------|----------|:----:|
| **00** | **Khung dự án + layout + trang chủ** | schema, CSS, header/footer, HomeServlet | ✅ |
| 01 | Đăng ký / Đăng nhập / Đăng xuất | `AuthServlet`, `AuthFilter`, session, băm mật khẩu | ⚠️ |
| 02 | Kho truyện — danh sách + phân trang | `StoryServlet?action=list` | ☐ |
| 03 | Tag + lọc truyện theo thể loại | `TagDAO`, lọc nhiều tag | ☐ |
| 04 | Chi tiết truyện | `StoryServlet?action=detail` | ☐ |
| 05 | Đăng / sửa truyện | `action=create\|edit`, **kiểm tra quyền sở hữu** | ☐ |
| 06 | Chương — đọc, thêm, sửa | `ChapterServlet`, giao diện đọc | ☐ |
| 07 | Bình luận | `CommentServlet` | ☐ |
| 08 | Đánh dấu (bookmark) | `BookmarkServlet` | ☐ |
| 09 | Tải truyện `.txt` | `DownloadServlet` — không trả HTML | ☐ |
| 10 | Trang quản trị — gỡ truyện, ban tài khoản | `admin/*`, `AdminFilter` | ☐ |
| 11 | Hướng dẫn sử dụng + Nội quy | `PageServlet`, checkbox đồng ý lúc đăng ký | ☐ |

⚠️ **CASE 01 — code xong, CHƯA test được đầy đủ.** Trang đăng nhập/đăng ký hiện
đúng, `AuthFilter` chặn đúng (đã kiểm bằng curl). Nhưng luồng đăng nhập thật cần
database — chạy 3 bước ở mục Cài đặt rồi thử lại thì mới tick được ✅.

Nếu thiếu thời gian, cắt theo thứ tự: CASE 09 → CASE 08 → ảnh bìa.
**Đừng cắt CASE 03 và CASE 10** — tag/lọc và phân quyền là hai thứ được nhìn vào đầu tiên.

---

## Cài đặt

### 1. Tạo database

```bash
mysql -u root -p < database/schema.sql
```

### 2. Tạo tài khoản riêng cho app

Mở `database/setup_user.sql`, **đổi mật khẩu** ở dòng `IDENTIFIED BY`, rồi:

```bash
mysql -u root -p < database/setup_user.sql
```

> Không dùng `root` cho ứng dụng: `root` có toàn quyền trên mọi database của máy.

### 3. Khai báo mật khẩu cho app

```bash
cp src/main/resources/db.properties.example src/main/resources/db.properties
```

Mở `db.properties` điền mật khẩu vừa đặt. File này đã nằm trong `.gitignore`.

### 4. Nạp dữ liệu mẫu (tuỳ chọn, nên làm để có cái mà nhìn)

```bash
mysql -u root -p webdoctruyen < database/sample_data.sql
```

7 truyện, 10 thể loại, 5 tài khoản. Mật khẩu tài khoản mẫu đều là `123456`.

### 5. Chạy

```bash
powershell -ExecutionPolicy Bypass -File run.ps1
```

→ <http://localhost:8080/webdoctruyen/>

Chưa làm bước 1–3 web vẫn chạy: trang chủ sẽ hiện hướng dẫn cài đặt thay vì báo lỗi.

> Tomcat **9**, không phải 10+. Dự án dùng `javax.servlet`, Tomcat 10 đổi sang `jakarta.servlet`.

---

## Cấu trúc

```
DoAn-WebDocTruyen/
├── database/
│   ├── schema.sql              7 bảng, có chú thích từng cột
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
| [docs/so-do.md](docs/so-do.md) | **ERD, luồng MVC, kiến trúc** — 7 sơ đồ Mermaid |
| [docs/giai-thich.md](docs/giai-thich.md) | **"cái này chạy kiểu gì, sao lại thế?"** — giảng từ đầu, 7 khu |
| [docs/cau-truc.md](docs/cau-truc.md) | "file này bỏ vào thư mục nào?" |
| [docs/standards/01-CODING_CONVENTIONS.md](docs/standards/01-CODING_CONVENTIONS.md) | đặt tên Java, contract 4 tầng, bảng tra URL |
| [docs/standards/02-VIEW_CONVENTIONS.md](docs/standards/02-VIEW_CONVENTIONS.md) | layout, scope, **sổ đăng ký attribute**, EL, CSS |
| [docs/standards/03-DATABASE_CONVENTIONS.md](docs/standards/03-DATABASE_CONVENTIONS.md) | đặt tên bảng, kiểu dữ liệu, luật DAO |
| [docs/standards/04-GIT_CONVENTIONS.md](docs/standards/04-GIT_CONVENTIONS.md) | commit message, không commit gì |
| [docs/CHECKLIST.md](docs/CHECKLIST.md) | sắp nộp bài |

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
