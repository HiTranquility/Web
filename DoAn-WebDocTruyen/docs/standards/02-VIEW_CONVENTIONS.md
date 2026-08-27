# 🎨 Quy ước View — JSP / EL / Scope / CSS

Áp dụng cho `src/main/webapp/`.
Cấu trúc thư mục xem [`../cau-truc.md`](../cau-truc.md).

---

## 1. Bảng tra nhanh đặt tên file view

| Tên | Là gì | Có `<html>`? | Ai gọi nó |
|-----|-------|:------------:|-----------|
| `list.jsp` | **mảnh nội dung** — phần ruột của trang | ❌ | servlet trỏ `contentPage` vào |
| `_card.jsp` | **mảnh nhỏ** tái dùng nhiều nơi | ❌ | `<c:forEach>` trong mảnh khác |
| `main.jsp` | **khung (layout)** — dựng HTML đầy đủ | ✅ | servlet `forward` tới |
| `head.jsp` `nav.jsp` | **bộ phận của khung** | ❌ | `<%@ include %>` trong layout |
| `error404.jsp` | **trang phóng** 3 dòng | ❌ | Tomcat forward thẳng tới |
| `_error404.jsp` | nội dung thật của trang lỗi | ❌ | trang phóng ở trên |

**Dấu `_` ở đầu = mảnh, không mở thẳng được.** Nhìn tên là biết loại, khỏi mở ra xem.

Chỉ trang lỗi mới cần cặp phóng/mảnh, vì Tomcat gọi thẳng nó chứ không qua
servlet nào — nên không ai đặt hộ `contentPage`.

---

## 2. Bốn scope — cái nào dùng khi nào

EL tìm từ scope **nhỏ nhất** tới **lớn nhất**, gặp trước lấy trước:

```
page  →  request  →  session  →  application
```

| Scope | Sống được bao lâu | Dùng cho | Trong đồ án này |
|-------|-------------------|----------|-----------------|
| `page` | trong **một** file JSP | biến tạm của riêng trang | `<c:set>` trong vòng lặp |
| `request` | **một** request, sống qua forward | dữ liệu servlet gửi cho JSP | **99% trường hợp** |
| `session` | nhiều request của **một** người | ai đang đăng nhập | chỉ `currentUser` |
| `application` | cả app, **mọi** người dùng chung | cấu hình đọc-một-lần | `uploadDir`, `pageSize` |

### Ba luật bắt buộc

**Luật 1 — mặc định là `request`.**
Dữ liệu servlet gửi cho JSP luôn để ở `request`. Nó tự biến mất sau khi trả
trang, không rò sang request sau.

**Luật 2 — `session` chỉ cho `currentUser`.**
Nhét truyện đang xem, kết quả tìm kiếm, bộ lọc vào session là bug chờ ngày nổ:
mở 2 tab là hai tab đè lên nhau, còn dữ liệu thì ở lì tới khi hết phiên.

**Luật 3 — `application` chỉ cho thứ KHÔNG BAO GIỜ bị ghi sau khi khởi động.**
Cả app dùng chung một object, **không thread-safe**. Đọc cấu hình thì được;
ghi vào thì hai người dùng cùng lúc sẽ giẫm lên nhau.

```jsp
${user}                 <%-- tìm lần lượt 4 scope, lấy cái gặp đầu tiên --%>
${requestScope.user}    <%-- chỉ định đích danh — dùng khi sợ trùng tên --%>
${sessionScope.currentUser}
```

Trùng tên ở hai scope là chuyện xảy ra thật, và rất khó tìm vì không có lỗi nào.
Nghi ngờ thì chỉ đích danh.

---

## 3. Sổ đăng ký attribute — bảng chốt

**Đây là bảng quan trọng nhất của cả file.** Trước khi đặt một attribute mới,
tra bảng này. Có tên rồi thì dùng lại, đừng đẻ tên mới.

> Không có bảng này thì chỗ đặt `msg`, chỗ `message`, chỗ `errorMsg` — và JSP
> im lặng in ra rỗng, không báo lỗi gì cả.

### Của layout — mọi servlet đều có thể đặt

| Tên | Kiểu | Scope | Bắt buộc? | Ai đặt | Ai đọc |
|-----|------|-------|:---------:|--------|--------|
| `contentPage` | String | request | **✅ có** | mọi servlet | `layout/*.jsp` |
| `pageTitle` | String | request | không | servlet | `parts/head.jsp` |
| `activeNav` | String | request | không | servlet | `parts/nav.jsp` |
| `layoutCss` | String | request | tự động | chính layout | `parts/head.jsp` |
| `pageCss` | String | request | không | servlet | `parts/head.jsp` |

`activeNav` chỉ nhận: `home` · `browse` · `rules` · `admin`

### Của nghiệp vụ

| Tên | Kiểu | Scope | Ai đặt | Ý nghĩa |
|-----|------|-------|--------|---------|
| `currentUser` | `User` | **session** | `AuthServlet` | người đang đăng nhập, `null` = khách |
| `message` | String | request | servlet | thông báo lỗi / thành công |
| `story` | `Story` | request | `StoryServlet` | truyện đang xem |
| `stories` | `List<Story>` | request | `StoryServlet` | danh sách truyện |
| `chapter` | `Chapter` | request | `ChapterServlet` | chương đang đọc |
| `chapters` | `List<Chapter>` | request | `ChapterServlet` | mục lục chương |
| `tags` | `List<Tag>` | request | `TagServlet` | danh sách thể loại |
| `comments` | `List<Comment>` | request | `CommentServlet` | bình luận của truyện |
| `bookmarks` | `List<Bookmark>` | request | `BookmarkServlet` | truyện đã đánh dấu |
| `latest` | `List<Story>` | request | `HomeServlet` | truyện mới cập nhật (trang chủ) |
| `popular` | `List<Story>` | request | `HomeServlet` | truyện xem nhiều (trang chủ) |
| `totalStories` | int | request | `HomeServlet` | tổng số truyện đã công khai |
| `dbError` | String | request | `HomeServlet` | database chưa sẵn sàng |

### Luật đặt tên

| Luật | Ví dụ |
|------|-------|
| Một object → **số ít** | `story` |
| Danh sách → **số nhiều** | `stories` |
| Trùng tên biến trong servlet | `request.setAttribute("story", story)` |
| Thông báo cho người dùng → luôn là `message` | không `msg`, không `error` |
| Không đặt trống rỗng, để `null` | xem luật dưới |

**`message` phải là `null` khi không có gì để báo — không phải `""`.**
Vì JSP kiểm `<c:if test="${not empty message}">`. Đặt `""` thì điều kiện vẫn
đúng và trang hiện một dòng trống không ai hiểu vì sao.

---

## 4. EL — `${}` hay `<c:out>`

**Luật một câu: dữ liệu do NGƯỜI DÙNG nhập → luôn `<c:out>`.**

| Dữ liệu | Dùng | Vì sao |
|---------|------|--------|
| Tên truyện, bình luận, tên hiển thị | `<c:out value="${story.title}"/>` | **người dùng nhập → phải escape** |
| Số đếm, id, giá trị mình tự tính | `${story.viewCount}` | không thể chứa HTML |
| Chuỗi mình viết cứng trong code | `${pageTitle}` | mình kiểm soát |

```jsp
<%-- ❌ SAI — người dùng đặt tên truyện là <script>alert(1)</script> --%>
<h1>${story.title}</h1>

<%-- ✅ ĐÚNG --%>
<h1><c:out value="${story.title}"/></h1>
```

**EL `${}` KHÔNG tự escape HTML.** Đây là lỗ hổng XSS có thật trong code gốc
của sách — đã dựng lại và chứng minh được ở dự án chương 5.

### Ba cái bẫy EL

| Bẫy | Đúng |
|-----|------|
| `${user.class}` — `class` là từ khoá | `${user["class"]}` |
| `${a > b}` — dấu `>` bị hiểu là đóng thẻ | `${a gt b}` (`lt` `ge` `le` `ne`) |
| Muốn in ra chữ `${...}` cho người xem | `${'$'}{...}` hoặc `&#36;{...}` |

### Bean `null` vs sai tên property — khác nhau

| Tình huống | Kết quả |
|------------|---------|
| `${user.email}` khi `user` là `null` | in ra **rỗng**, không lỗi |
| `${user.emailAddress}` khi `User` không có `getEmailAddress()` | **exception → 500** |

Tên property lấy từ **tên get method**: `getEmail()` → `${user.email}`.
Không phải tên field, không phải tên cột database.

---

## 5. Chọn layout — bảng quyết định

Hỏi lần lượt, **dừng ở câu đầu tiên trả lời "có"**:

| # | Câu hỏi | → dùng |
|:-:|---------|--------|
| 1 | Có thanh nav như trang chủ? | `main` |
| 2 | Chưa đăng nhập, cần ô nhập giữa màn hình? | `auth` |
| 3 | Có menu bên trái của quản trị? | `admin` |
| 4 | Toàn màn hình, bỏ hết thứ gây phân tâm để đọc? | `reader` |
| 5 | **Không câu nào ở trên** | **`main`** |

Câu 5 quan trọng nhất: mặc định là `main`, **không phải** "tạo layout mới cho chắc".

> **Layout mới chỉ khi KHUNG khác — không phải khi NỘI DUNG khác.**
> Khung = nav, footer, khối bao ngoài. Nội dung khác nhau là chuyện bình
> thường, đó là lý do có nhiều *mảnh*, không phải nhiều *layout*.

Servlet dùng layout:

```java
request.setAttribute("contentPage", "/WEB-INF/views/story/list.jsp");
getServletContext()
    .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
    .forward(request, response);
```

Đổi khung = đổi **một** dòng cuối. Mảnh nội dung không phải sửa gì.

---

## 6. Link — luôn có `contextPath`

```jsp
<%-- ❌ SAI — gãy khi deploy đổi tên ứng dụng --%>
<a href="/story?action=list">Kho truyện</a>
<link rel="stylesheet" href="assets/css/base.css">

<%-- ✅ ĐÚNG --%>
<a href="${pageContext.request.contextPath}/story?action=list">Kho truyện</a>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
```

| Kiểu | Kết quả | Vấn đề |
|------|---------|--------|
| `href="story?action=list"` | tương đối thư mục hiện tại | ở URL nhiều cấp là gãy |
| `href="/story?action=list"` | tuyệt đối từ gốc **server** | gãy khi app không ở root |
| `href="${pageContext.request.contextPath}/story..."` | tuyệt đối từ gốc **app** | ✅ luôn đúng |

> Đây là lỗi đã gặp thật ở dự án chương 6: CSS dùng đường dẫn tương đối, ở URL
> `/truyen/khong-co` trình duyệt đi tìm `/truyen/styles/main.css` → 404 → trang
> lỗi hiện ra trần trụi không có định dạng nào.

**Trong `.html` không dùng EL được.** File `.html` được gửi nguyên xi, `${...}`
in ra y nguyên. Cần `contextPath` thì file phải là `.jsp`.

Dấu `&` trong `href` phải viết `&amp;` — quy tắc HTML, không phải JSP.
Trình duyệt tự giải mã lại nên servlet không thấy khác gì.

---

## 7. CSS — 4 tầng, thứ tự cố định

```
assets/css/
├── base.css           biến màu, reset, typography     — MỌI trang
├── components.css     nút, thẻ, tag, form, bảng       — MỌI trang
├── layout-main.css    header, nav, hero, footer
├── layout-auth.css    card giữa màn hình
├── layout-reader.css  cỡ chữ đọc, chế độ giấy
└── layout-admin.css   sidebar, bảng quản trị
```

`parts/head.jsp` nạp theo thứ tự — **file sau ghi đè file trước**:

```
base.css → components.css → layout-{tên}.css → {pageCss}.css
```

### Luật đặt tên class

| Loại | Quy tắc | Ví dụ |
|------|---------|-------|
| Khối | danh từ, gạch ngang | `.story-card`, `.site-header` |
| Bộ phận trong khối | `<khối>-<bộ phận>` | `.story-title`, `.story-meta` |
| Trạng thái | tiền tố `is-` | `.is-active`, `.is-hidden` |
| Biến thể | `<gốc>-<biến thể>` | `.btn-primary`, `.btn-ghost` |
| Màu, khoảng cách | **luôn** dùng biến CSS | `var(--ember)` không `#f0863a` |

**Không viết mã màu thẳng vào file.** Mọi màu khai một lần trong `base.css`
dưới dạng `--tên`. Đổi tông màu thì sửa một chỗ.

**Layout mới = thêm đúng 1 file `layout-*.css`.** Không tách nhỏ hơn thế.
Mỗi trang một file CSS thì 25 file, và không ai biết class nào định nghĩa ở đâu.

---

## 8. Bốn thứ tuyệt đối tránh trong JSP

| Đừng | Thay bằng |
|------|-----------|
| Scriptlet `<% Java %>` | EL + JSTL. Servlet chuẩn bị sẵn dữ liệu |
| `${}` cho dữ liệu người dùng nhập | `<c:out value="${...}"/>` |
| Gọi DAO từ JSP | Servlet gọi, đặt vào request |
| Chú thích `<!-- -->` để "tạm tắt" code | `<%-- --%>` — chú thích HTML **vẫn chạy** code bên trong và kết quả vẫn tới trình duyệt |
