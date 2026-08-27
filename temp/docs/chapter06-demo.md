# Kịch bản demo — Chapter 6

Hướng dẫn bấm từng bước để trình bày đủ mọi trang. Làm theo đúng thứ tự, mất
khoảng **5 phút**.

Bảng ánh xạ slide → code: [`chapter06-mapping.md`](chapter06-mapping.md)

---

## Bước 0 — Khởi động

Mở PowerShell, vào thư mục `ch06jsp`, chạy:

```bash
powershell -ExecutionPolicy Bypass -File demo.ps1
```

Chờ tới khi terminal hiện dòng:

```
  Chapter 6 JSP running at http://localhost:8080/
  Press Ctrl+C to stop.
```

**Lần đầu chạy** mất thêm ~30 giây để tải 7 file jar về `.libs/`.
Lần sau chạy thẳng.

> ⚠️ **Đừng tắt terminal này.** Nó chính là server. Tắt là web sập.
> Mở trình duyệt ở cửa sổ khác.

> Nếu báo lỗi *"port 8080 already in use"*: đang có dự án khác chiếm cổng.
> Chạy `.\demo.ps1 -Port 9090` rồi thay 8080 thành 9090 ở mọi URL bên dưới.

Mở trình duyệt vào **<http://localhost:8080/temp/>** — trang demo có sẵn nút bấm
cho mọi bước bên dưới.

**Bật DevTools ngay từ đầu:** bấm **F12**, chọn tab **Network**. Cần nó ở bước 4
để chứng minh mã HTTP thật.

---

## Bước 1 — Trang chính, và cơ chế include

Mở **<http://localhost:8080/>**

Thấy: tiêu đề *"Join our email list"*, 3 ô nhập trống, nút **Join Now**, và dòng
copyright ở cuối.

**Nói:** trang này khớp ảnh chụp ở **slide 29**. Phần đầu và phần cuối không nằm
trong `index.jsp` — chúng được include từ `WEB-INF/includes/header.html` và
`footer.jsp` bằng `<c:import>` (**slide 32**, include lúc chạy).

Năm trong dòng copyright do `footer.jsp` **tính lúc chạy** bằng scriptlet
`<%= currentYear %>` — đúng code **slide 27**. Đó là lý do nó phải là `.jsp`
chứ không phải `.html`.

---

## Bước 2 — Kiểm tra dữ liệu (slide 12)

Ở trang chính, điền:

| Ô | Giá trị |
|---|---------|
| Email | `jsmith@gmail.com` |
| First Name | `John` |
| Last Name | *(để trống)* |

Bấm **Join Now**.

Thấy: quay lại chính trang đó, có thêm dòng đỏ **"Vui lòng điền đủ cả ba ô."**,
và **hai ô đã điền vẫn còn chữ**, chỉ ô Last Name trống.

**Nói:** dòng thông báo là thẻ `<c:if test="${message != null}">` của **slide 12**.
Hai ô giữ được chữ là nhờ `value="${user.email}"` — servlet forward luôn object
`User` về lại trang, nên người dùng không phải gõ lại từ đầu.

Lần đầu vào trang thì `user` chưa tồn tại, EL in ra rỗng nên ô trống — **EL xử
lý null tốt hơn scriptlet**, đúng như **slide 22**.

---

## Bước 3 — Trang cảm ơn (slide 30)

Điền nốt **Last Name** = `Smith`, bấm **Join Now**.

Thấy đủ **4 phần** đúng như ảnh slide 30:

1. Tiêu đề *"Thanks for joining our email list"*
2. Ba dòng Email / First Name / Last Name
3. Đoạn *"To enter another email address, click on the Back button…"*
4. Nút **Return**

**Nói:** ba dòng thông tin đọc bằng EL `${user.email}` — **slide 21**. Trang này
dùng **chung** header và footer với trang trước, nên giao diện đồng nhất; sửa
`header.html` một lần là cả hai trang đổi theo (**slide 33**).

Bấm **Return** → quay lại form trống. Nút này là `<form method="get">` mang tham
số ẩn `action=join`, không phải thẻ `<a>`.

---

## Bước 4 — Ba trang lỗi

> Đây là phần cần **tab Network của DevTools**. Mở F12 trước khi bấm.

Về **<http://localhost:8080/temp/>** rồi bấm lần lượt:

| Bấm | Trang hiện ra | Cột Status trong Network |
|-----|---------------|:------------------------:|
| **Gọi thẳng /404** | 404 — Không tìm thấy trang | **404** |
| **Gõ URL bịa** | cũng trang 404 | **404** |
| **Gọi /403** | 403 — Không có quyền truy cập | **403** |
| **Exception cố ý ném ra** | 500 — Lỗi máy chủ | **500** |

**Nói — đây là chỗ ăn điểm:** ba URL đó trả về **mã HTTP thật**, không phải 200
kèm nội dung trang lỗi. Chỉ vào cột Status trong Network để chứng minh.

Lý do: chúng đi qua `ErrorTestServlet` gọi `response.sendError()`, chứ không trỏ
thẳng vào file `.jsp`. Trỏ thẳng thì trình duyệt nhận **200 OK** — nhìn giống
nhưng mã sai, và công cụ tìm kiếm sẽ tưởng trang đó tồn tại.

Cả ba khai trong `WEB-INF/web.xml` bằng thẻ `<error-page>`.
403 **không có trong slide** — thêm theo yêu cầu đề bài.

---

## Bước 5 — Lỗi EL, dựng lại đúng slide 34

Bấm **"Lỗi EL sai tên property (đúng như slide 34)"**
(hoặc mở <http://localhost:8080/temp/loi_el.jsp>)

Thấy: trang **500**, phần "Chi tiết kỹ thuật" ghi:

```
Loại lỗi     class javax.el.PropertyNotFoundException
Thông điệp   Property [emailAddress] not found on type [murach.business.User]
```

**Nói:** đây đúng là lỗi trong ảnh chụp **slide 34**. JSP viết
`${user.emailAddress}` nhưng lớp `User` chỉ có `getEmail()`, nên tên property
đúng phải là `email`.

**Bài học:** tên property trong EL lấy từ **tên get method**, không phải tên
biến, cũng không phải tên cột database.

Và phân biệt hai trường hợp — nhiều người nhớ nhầm là "EL không bao giờ lỗi":

| Tình huống | Kết quả |
|------------|---------|
| Bean là `null` | EL in ra **rỗng**, không lỗi |
| Bean có, **sai tên property** | **Ném exception → 500** |

Trang `temp/loi_el.jsp` in cả ba trường hợp cạnh nhau để thấy rõ.

Khác sách một điểm đáng nói: slide 34 hiện trang mặc định xấu xí của Tomcat
(phơi ra phiên bản server, tên package, số dòng), còn ở đây rơi vào
`error_500.jsp` tự làm.

---

## Bước 6 — Hai lỗi về includes đã vá

Ở trang demo, mục **"File include có bị lộ không"**, bấm hai link:

| Bấm | Kết quả phải là |
|-----|:---------------:|
| `/WEB-INF/includes/header.html` | **404** |
| `/includes/header.html` (chỗ cũ) | **404** |

**Nói:** trước đây file include để ở `/includes/` nên gõ URL vào là xem được —
trả về 200 kèm nửa trang HTML hỏng. Chuyển vào `WEB-INF` thì Tomcat chặn hẳn,
nhưng include từ trong code vẫn chạy bình thường.

Rồi bấm **"URL nhiều cấp"** (`/truyen/sau/hon/nua`).

**Nói:** trang 404 hiện ra **có đầy đủ định dạng**. Trước khi vá thì trang này
trần trụi không CSS, vì `header.html` viết `href="styles/main.css"` (tương đối) —
ở URL nhiều cấp trình duyệt đi tìm `/truyen/sau/hon/styles/main.css` → 404.
Đổi thành `/styles/main.css` (tuyệt đối) là hết.

---

## Checklist — tick đủ là demo xong

- [ ] Trang chính hiện đúng, có header + footer từ include
- [ ] Năm trong footer là năm hiện tại (tính lúc chạy)
- [ ] Submit thiếu → hiện thông báo, **giữ lại chữ đã gõ**
- [ ] Submit đủ → trang cảm ơn có **đủ 4 phần**, kể cả nút Return
- [ ] Nút Return quay về form
- [ ] `/404` `/403` `/500` — Network hiện **đúng mã**, không phải 200
- [ ] URL bịa cũng ra trang 404
- [ ] `temp/loi_el.jsp` ném đúng `PropertyNotFoundException`
- [ ] File include trả 404 khi gõ URL trực tiếp
- [ ] Trang 404 ở URL nhiều cấp **vẫn có CSS**

---

## Dừng server

Bấm **Ctrl+C** trong terminal đang chạy.

Không tắt được thì:

```bash
taskkill /F /IM java.exe
```

---

## Nếu có trục trặc

| Hiện tượng | Nguyên nhân | Cách xử lý |
|------------|-------------|------------|
| `port 8080 already in use` | dự án khác đang chiếm cổng | `.\demo.ps1 -Port 9090` |
| Trang hiện nguyên chữ `<c:if>` | thiếu dòng `taglib` hoặc thiếu jar JSTL | kiểm tra dòng đầu file `.jsp` và thư mục `.libs/` |
| Sửa `.jsp` không thấy đổi | trang đó dùng `<%@ include %>` | **restart server** (slide 33) |
| Sửa `.java` không thấy đổi | file `.class` chưa biên dịch lại | Ctrl+C rồi chạy lại `demo.ps1` |
| Trang trắng, không CSS | đường dẫn CSS sai | kiểm tra `header.html` phải là `/styles/main.css` |

---

## Trước khi nộp bài

Hai thứ nên dọn:

1. **Xoá folder `temp/`** — chỉ dùng để demo, xoá đi app vẫn chạy đủ.
2. **Xoá khối "Chi tiết kỹ thuật" trong `error_500.jsp`** — nó phơi tên lớp và
   thông điệp lỗi cho người dùng. Lúc code thì cần, lúc chạy thật thì chi tiết
   thuộc về log file.
