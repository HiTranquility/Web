# Cấu trúc dự án — bản đồ toàn bộ

Tài liệu này trả lời đúng một câu hỏi: **file này bỏ vào đâu?**

Đọc xong thì không phải nghĩ về cấu trúc nữa. Cấu trúc bên dưới là **bản cuối**,
không đổi cho tới lúc nộp bài.

---

## Trước hết: nó KHÔNG phình ra vô hạn

Đây là toàn bộ dự án khi làm xong **cả 11 CASE**:

| Loại | Số file | Thư mục | File nhiều nhất trong 1 thư mục |
|------|--------:|--------:|--------------------------------:|
| Java | 27 | 5 | 10 (`controller/`) |
| JSP | 26 | 7 | 6 (`page/`) |
| CSS | 6 | 1 | 6 |
| **Tổng** | **59** | **13** | |

**Layout thì đúng 4 cái.** Không phải 10, không phải 20.

Lý do: layout không sinh theo TRANG, nó sinh theo **KHUNG**. 25 trang nhưng chỉ
có 4 kiểu khung. Trang chủ, kho truyện, chi tiết truyện, bookmark, hướng dẫn,
nội quy, 2 trang lỗi — **8 trang dùng chung 1 layout `main`**.

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

## Java — 27 file, 5 thư mục

```
src/main/java/truyen/
├── model/          6 file — JavaBean thuần, chỉ get/set
│   ├── User.java        Story.java       Chapter.java
│   └── Tag.java         Comment.java     Bookmark.java
│
├── dao/            6 file — chỉ SQL, không đụng request/response
│   ├── UserDAO.java     StoryDAO.java    ChapterDAO.java
│   └── TagDAO.java      CommentDAO.java  BookmarkDAO.java
│
├── controller/     10 file — chỉ điều phối, không SQL, không HTML
│   ├── HomeServlet.java        /            trang chủ
│   ├── AuthServlet.java        /auth        đăng nhập, đăng ký, đăng xuất
│   ├── StoryServlet.java       /story       list, detail, create, edit, delete
│   ├── ChapterServlet.java     /chapter     read, create, edit, delete
│   ├── CommentServlet.java     /comment     add, delete
│   ├── BookmarkServlet.java    /bookmark    add, remove, list
│   ├── DownloadServlet.java    /download    xuất .txt (không trả HTML)
│   ├── PageServlet.java        /page        hướng dẫn, nội quy
│   └── admin/
│       ├── AdminStoryServlet.java   /admin/story
│       └── AdminUserServlet.java    /admin/user
│
├── filter/         2 file
│   ├── AuthFilter.java      chặn khách chưa đăng nhập
│   └── AdminFilter.java     chặn thành viên thường vào /admin/*
│
└── util/           3 file
    ├── DBConnection.java    mở kết nối MySQL
    ├── PasswordUtil.java    băm và kiểm tra mật khẩu
    └── SlugUtil.java        "Tiên Hiệp Kỳ" -> "tien-hiep-ky"
```

**`controller/` có 10 file là nhiều nhất, và nó dừng ở đó.** Vì mỗi *thực thể*
một servlet, mà chỉ có 6 thực thể. Thêm chức năng = thêm nhánh `?action=` trong
servlet có sẵn, **không** thêm file.

> Đó là lý do dùng `?action=` thay vì mỗi thao tác một servlet. Nếu tách,
> 6 thực thể × 4 thao tác = **24 servlet** thay vì 10.

---

## Views — 26 file, 7 thư mục

```
WEB-INF/views/
├── layout/         KHUNG — 4 cái, hết
│   ├── main.jsp        nav + nội dung + footer     (8 trang dùng)
│   ├── auth.jsp        card giữa màn hình, không nav (2 trang)
│   ├── reader.jsp      tối giản, để đọc            (1 trang)
│   ├── admin.jsp       có sidebar                  (2 trang)
│   └── parts/          mảnh dùng chung GIỮA các layout
│       ├── head.jsp    thẻ <head>, nạp CSS
│       ├── nav.jsp     thanh menu trên
│       ├── footer.jsp  chân trang
│       └── sidebar.jsp menu bên (chỉ layout admin)
│
├── story/    home.jsp  list.jsp  detail.jsp  form.jsp  _card.jsp
├── chapter/  read.jsp  form.jsp
├── auth/     login.jsp  register.jsp
├── user/     bookmarks.jsp
├── admin/    stories.jsp  users.jsp
└── page/     guide.jsp  rules.jsp
             error404.jsp  _error404.jsp
             error500.jsp  _error500.jsp
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
