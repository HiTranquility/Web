# 💡 Giải thích từng khu — hiểu cho bằng được

File này **không phải quy ước** (quy ước ở [`standards/`](standards/)).
Nó giải thích **vì sao mọi thứ hoạt động như vậy** — đọc khi thấy mình đang làm
theo mà không hiểu tại sao.

Mỗi khu đi từ chuyện đơn giản nhất lên. Không nhảy cóc.

**Mục lục**
1. [4 scope — cái hộp nào](#khu-1--4-scope)
2. [Layout — ai lắp trang lại với nhau](#khu-2--layout)
3. [Một request đi qua những đâu](#khu-3--đường-đi-của-một-request)
4. [forward vs redirect](#khu-4--forward-vs-redirect)
5. [WEB-INF — vì sao giấu được](#khu-5--web-inf)
6. [EL tìm property kiểu gì](#khu-6--el-và-property)
7. [Filter — cái chặn trước cửa](#khu-7--filter)

---

## Khu 1 — 4 scope

### Bước 1: `setAttribute` thực chất là gì

```java
request.setAttribute("stories", danhSach);
```

Câu này **không có gì huyền bí**. Bên trong object `request` có một cái `Map`.
Dòng trên chính là:

```java
map.put("stories", danhSach);
```

Cất một object vào Map, đặt cho nó cái tên. Hết.

### Bước 2: Scope là **cái hộp nào**

Vấn đề: có **4 object khác nhau**, mỗi cái đều có một Map như vậy.

| Bạn viết | Bỏ vào Map của object |
|---|---|
| `pageContext.setAttribute(...)` | `PageContext` |
| `request.setAttribute(...)` | `HttpServletRequest` |
| `session.setAttribute(...)` | `HttpSession` |
| `application.setAttribute(...)` | `ServletContext` |

**"Scope" chỉ có nghĩa là: bạn chọn bỏ vào hộp nào.** Không phải loại dữ liệu,
không phải quyền hạn — chỉ là *hộp nào*.

### Bước 3: Vì sao cần 4 hộp — vì chúng bị vứt lúc khác nhau

```
8:00:00  Bạn gõ localhost:8080 rồi Enter
         → Tomcat TẠO MỚI một object request
         → servlet chạy, bỏ "stories" vào hộp request
         → JSP lấy "stories" ra, vẽ trang
         → gửi HTML về trình duyệt
         → Tomcat VỨT object request đó đi

8:00:05  Bạn bấm F5
         → Tomcat TẠO MỚI một object request khác, RỖNG TRƠN
         → "stories" hồi nãy? Không còn.
```

Hộp `request` bị vứt sau **mỗi lần** trả trang. Đó là toàn bộ lý do F5 làm mất
dữ liệu trong đó.

`session` khác: lần đầu bạn vào, Tomcat phát cho trình duyệt bạn một cookie tên
`JSESSIONID`. Mỗi lần bạn gửi request, cookie đó đi kèm, Tomcat nhìn vào và biết
"à, thằng này là người lúc nãy" rồi lấy đúng hộp `HttpSession` **của riêng bạn**.
Nên dữ liệu sống qua nhiều lần F5.

`application` tạo lúc server khởi động, vứt lúc server tắt. **Chỉ có một cái,
cả thế giới dùng chung.**

### Bước 4: Phép thử để phân biệt

| | Bấm F5 | Cửa sổ ẩn danh | Restart server |
|---|:---:|:---:|:---:|
| `page` | mất | mất | mất |
| `request` | mất | mất | mất |
| `session` | **CÒN** | mất | mất |
| `application` | **CÒN** | **CÒN** | mất |

Cửa sổ ẩn danh không mang cookie `JSESSIONID` của bạn → Tomcat coi là người lạ →
cấp hộp session mới. Đó là cách nhanh nhất để test dữ liệu đang ở `session` hay
`application`.

### Bước 5: `page` với `request` khác nhau chỗ nào

Nhìn bảng trên thì giống hệt. Khác biệt nằm chỗ khác:

**`request` sống qua `forward`, `page` thì không.**

```
HomeServlet  →forward→  layout/main.jsp  →include→  story/home.jsp
```

Ba file, nhưng **cùng một object `request`**. Nên `contentPage` servlet đặt vào
thì `main.jsp` đọc được.

Còn `page` thì mỗi file JSP có `PageContext` **riêng**. Đặt ở `main.jsp` thì
`home.jsp` không thấy.

Tóm: `page` = biến nháp trong đúng file này. `request` = dữ liệu đi từ servlet
sang JSP.

### Bước 6: Trong đồ án, cụ thể

| Hộp | Ví dụ thật | Tại sao hộp đó |
|---|---|---|
| `page` | biến đếm trong `<c:forEach>` | chỉ dùng trong đúng file đó |
| `request` | `stories`, `contentPage`, `message` | servlet đưa cho JSP, xong vứt |
| `session` | `currentUser` | phải nhớ bạn là ai qua nhiều trang |
| `application` | `uploadDir`, `pageSize` | cấu hình, ai cũng đọc như nhau |

Đây là lý do **đừng nhét kết quả tìm kiếm vào `session`**: session là của bạn
nhưng **dùng chung cho mọi tab**. Mở 2 tab tìm 2 thứ khác nhau → tab sau ghi đè
tab trước → tab đầu F5 ra kết quả của tab sau.

### Bước 7: "Thứ tự tìm kiếm" nghĩa là gì

Viết `${user}` mà **không nói rõ hộp nào** thì EL mở lần lượt:

```
page → request → session → application
```

Mở tới hộp nào thấy chữ `user` thì **lấy luôn, dừng tìm**.

Cái bẫy: bạn có `currentUser` ở session. Rồi ở trang chi tiết truyện lỡ đặt
`request.setAttribute("user", tacGia)`. Nếu cả hai cùng tên `user`:

```
${user}  →  lấy TÁC GIẢ (request), không phải người đăng nhập (session)
```

Trang hiện nhầm tên, **và không có lỗi nào báo**. Đó là lý do đồ án đặt tên
người đăng nhập là `currentUser` chứ không phải `user`.

Chỉ đích danh thì thêm tiền tố: `${sessionScope.currentUser}`.

---

## Khu 2 — Layout

### Bước 1: Vấn đề cần giải

25 trang, trang nào cũng có cùng thanh menu và chân trang. Chép 25 lần thì sửa
logo phải sửa 25 chỗ.

### Bước 2: Cách nghĩ sai (mà ai cũng nghĩ đầu tiên)

"Chắc phải có file `index.html` chứa khung, rồi nạp nội dung vào."

Đó là mô hình **React/Vue**: một `index.html` rỗng, JavaScript chạy **trên trình
duyệt** rồi tự lắp giao diện.

JSP **ngược lại**: server lắp xong HTML hoàn chỉnh rồi mới gửi. Trình duyệt nhận
về trang đã xong, không lắp gì thêm.

### Bước 3: Nhưng trực giác đó đúng — chỉ là nó chạy ở server

```
Servlet                    layout/main.jsp              story/home.jsp
   |                             |                            |
   |-- contentPage = home.jsp -->|                            |
   |-- forward tới main.jsp ---->|                            |
                                 |-- viết <html><head><nav>   |
                                 |-- <jsp:include> ---------->|
                                 |                            |-- viết phần ruột
                                 |<---------------------------|
                                 |-- viết <footer></html>
                                 |
                                 └─> gửi HTML hoàn chỉnh về trình duyệt
```

Trong servlet đúng 2 dòng:

```java
request.setAttribute("contentPage", "/WEB-INF/views/story/home.jsp");
forward("/WEB-INF/views/layout/main.jsp");
```

**Chú ý: forward tới LAYOUT, không phải tới trang nội dung.** Layout mới là thứ
được chạy; nó gọi trang nội dung vào giữa.

### Bước 4: Vì sao không dùng header.jsp + footer.jsp rời

Cách rời: mỗi trang tự `include` header rồi `include` footer.

Vấn đề: thẻ `<body>` **mở ở file này, đóng ở file kia**. Quên một `</div>` là vỡ
layout mà không lỗi nào báo, IDE cũng không kiểm giúp được.

Cách layout wrapper: thẻ mở và thẻ đóng nằm **cùng một file**. Trang nội dung
không cần biết gì về khung.

### Bước 5: Nhiều layout thì sao

Layout sinh theo **KHUNG**, không sinh theo **TRANG**.

25 trang nhưng chỉ 4 kiểu khung: `main` (8 trang), `auth` (2), `admin` (2),
`reader` (1). Bảng quyết định chọn layout ở
[`standards/02-VIEW_CONVENTIONS.md`](standards/02-VIEW_CONVENTIONS.md) §5.

---

## Khu 3 — Đường đi của một request

Theo dõi khi ai đó bấm vào một truyện:

```
1. Trình duyệt gửi:  GET /story?action=detail&id=5

2. Tomcat nhìn @WebServlet("/story")  →  gọi StoryServlet.doGet()

3. StoryServlet:
      đọc  action = "detail",  id = 5
      gọi  storyDAO.findById(5)
              │
              ├─ 4. StoryDAO mở kết nối MySQL
              │     chạy SELECT ... WHERE id = ?
              │     đọc ResultSet → dựng object Story
              │     đóng kết nối
              └─ trả về object Story

5. StoryServlet:
      request.setAttribute("story", story)
      request.setAttribute("contentPage", ".../story/detail.jsp")
      forward tới layout/main.jsp

6. main.jsp viết <html><head><nav>
      rồi <jsp:include> vào detail.jsp
              │
              └─ 7. detail.jsp đọc ${story.title}, viết ra HTML

8. main.jsp viết <footer></html>

9. Tomcat gửi HTML về trình duyệt, VỨT object request
```

**Ba điều rút ra:**

- **Bước 4 là chỗ DUY NHẤT chạm database.** Nên đổi MySQL sang PostgreSQL chỉ
  phải sửa trong `dao/`.
- **Bước 5 là chỗ DUY NHẤT nối hai thế giới**: DAO trả object Java, servlet cất
  vào request để JSP đọc được.
- **Bước 9 là lý do request scope chết sau mỗi lần tải trang.**

---

## Khu 4 — forward vs redirect

### Bước 1: Hai cách chuyển trang, khác nhau về bản chất

**forward** — chuyện xảy ra hoàn toàn **trong server**:

```
Trình duyệt ──request──> Tomcat ──> ServletA ──forward──> JSP ──> HTML
            <────────────────────── 1 lần đi về ──────────────────
```

**redirect** — server bảo trình duyệt "đi chỗ khác đi":

```
Trình duyệt ──request──> ServletA
            <──302 + Location──                       (lần 1)
Trình duyệt ──request tới địa chỉ mới──> ...
            <──HTML──                                 (lần 2)
```

### Bước 2: Mọi khác biệt đều suy ra từ đó

| | forward | redirect |
|---|---|---|
| Số lần đi về | 1 | 2 |
| Thanh địa chỉ | **không đổi** | **đổi** |
| Attribute trong request | **còn** | **mất** |
| Đi ra ngoài web khác | không | được |

Attribute mất khi redirect **không phải vì redirect "xoá" nó** — mà vì lần đi về
thứ hai là một object request hoàn toàn mới.

### Bước 3: Khi nào dùng cái nào

- **forward**: đưa dữ liệu cho JSP trong cùng app → mặc định của MVC
- **redirect**: sau khi POST đã **ghi** dữ liệu

Lý do thứ hai quan trọng: POST xong mà forward thì URL vẫn là URL của POST.
Người dùng bấm F5 → trình duyệt hỏi "gửi lại biểu mẫu?" → bấm OK là **đặt hàng
hai lần**. Kết thúc bằng redirect thì request cuối trong lịch sử là một GET vô
hại.

---

## Khu 5 — WEB-INF

### Bước 1: Nó là gì

`WEB-INF` là thư mục **Tomcat từ chối phục vụ cho trình duyệt**. Không phải quy
ước, không phải cấu hình — nó nằm trong đặc tả Servlet, mọi server đều làm vậy.

Gõ `localhost:8080/WEB-INF/views/story/home.jsp` → **404**, dù file có thật.

### Bước 2: Nhưng code vẫn đọc được

Tomcat chỉ chặn **truy cập từ ngoài vào**. Bên trong server thì thoải mái:

```java
getRequestDispatcher("/WEB-INF/views/layout/main.jsp").forward(...)   // chạy
```

### Bước 3: Vì sao để JSP vào đó

Sách để `thanks.jsp` ở gốc web. Nghĩa là ai cũng gõ thẳng `/thanks.jsp` được, và
thấy một trang vỡ với các ô trống — vì không đi qua servlet nên không có dữ liệu.

Bỏ vào `WEB-INF` thì **mọi lối vào đều buộc phải qua controller**. Đó chính là
điều MVC muốn, và nó được đảm bảo bởi server chứ không phải bởi kỷ luật của bạn.

### Bước 4: Còn dùng cho gì nữa

- `db.properties` — mật khẩu MySQL, tuyệt đối không để ngoài
- `EmailList.txt` — dữ liệu người dùng
- Ảnh bìa thì **không** — ảnh phải để ngoài vì trình duyệt cần tải trực tiếp

---

## Khu 6 — EL và property

### Bước 1: `${user.email}` không đọc field

Nó **gọi method** `getEmail()`.

EL lấy chữ sau dấu chấm, viết hoa chữ đầu, thêm `get` phía trước:

```
${user.email}      →  user.getEmail()
${user.firstName}  →  user.getFirstName()
${story.completed} →  story.isCompleted()     (boolean thì thử "is" trước)
```

### Bước 2: Hệ quả — tên property KHÔNG phải tên field

Đặt tên field là `email` mà method là `layEmail()` thì `${user.email}` **không
tìm thấy**. Tên property lấy từ **tên get method**.

Đó là lý do lớp model bắt buộc phải là JavaBean có đủ get method.

### Bước 3: Hai kiểu hỏng khác nhau

| Tình huống | Kết quả |
|---|---|
| `${user.email}` khi `user` là `null` | in ra **rỗng**, không lỗi |
| `${user.emailAddress}` khi không có `getEmailAddress()` | **exception → 500** |

Nhiều người nhớ nhầm là "EL không bao giờ lỗi". Không đúng — nó chỉ dễ tính với
`null`, còn gõ sai tên property thì sập ngay.

### Bước 4: `${}` không tự escape HTML

```jsp
<h1>${story.title}</h1>
```

Người dùng đặt tên truyện là `<script>alert(1)</script>` → đoạn script đó **chạy
thật** trên trình duyệt người khác. Đó là XSS.

`<c:out value="${story.title}"/>` thì escape sẵn.

**Luật: dữ liệu do NGƯỜI DÙNG nhập → luôn `<c:out>`.**

---

## Khu 7 — Filter

### Bước 1: Vấn đề

10 trang cần đăng nhập mới vào được. Chép đoạn kiểm tra session vào cả 10
servlet thì quên một chỗ là thủng.

### Bước 2: Filter đứng trước cửa

```
Trình duyệt ──> [AuthFilter] ──> StoryServlet ──> JSP
                     │
                     └── chưa đăng nhập? đá về /auth?action=login
                         servlet phía sau KHÔNG BAO GIỜ chạy
```

Filter chạy **trước** servlet. Khai bằng `@WebFilter("/admin/*")` là mọi URL
dưới `/admin/` đều phải qua nó.

### Bước 3: Vì sao mạnh hơn kiểm tra trong từng servlet

Bạn **không thể quên**. Thêm servlet mới dưới `/admin/` là nó tự động được bảo
vệ, không phải nhớ gì cả.

### Bước 4: Nhưng filter KHÔNG thay được kiểm tra quyền sở hữu

Filter chỉ biết "đã đăng nhập chưa", "có phải admin không". Nó **không biết**
truyện id=5 là của ai.

```java
// Filter đã cho qua vì bạn đã đăng nhập.
// Nhưng vẫn phải hỏi tiếp: truyện này có phải của bạn không?
if (story.getAuthorId() != currentUser.getId() && !laAdmin) {
    response.sendError(403);
    return;
}
```

Thiếu đoạn này thì sửa `?id=5` thành `?id=6` là sửa được truyện người khác.
Filter không cứu được, vì lúc đó bạn vẫn là người dùng hợp lệ.
