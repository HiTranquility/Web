# Chapter 6 — How to develop JSPs

Code của chương 6, *Murach's Java Servlets and JSP* (3rd Ed.).

📋 **[DEMO.md](DEMO.md) — kịch bản demo từng bước**: bật server ra sao, bấm gì,
nói gì, kèm checklist. Đọc file này nếu bạn sắp trình bày.

🗺️ [../docs/chapter06-mapping.md](../docs/chapter06-mapping.md) — bảng ánh xạ
**slide nào → code nào** (slide nào không có code cũng ghi rõ).

---

## Cách chạy

**Để demo** — chạy cái này, nó in sẵn danh sách URL rồi mới khởi động server:

```bash
powershell -ExecutionPolicy Bypass -File demo.ps1
```

Rồi mở **<http://localhost:8080/temp/>** — trang demo có nút bấm cho từng lỗi.

**Chạy thường:**

```bash
powershell -ExecutionPolicy Bypass -File run.ps1
```

Lần đầu chạy sẽ tự tải 7 file jar (Tomcat + JSTL) về `.libs/`, mất khoảng 30 giây.
Những lần sau chạy ngay.

Muốn đổi cổng: `.\run.ps1 -Port 9090`

Dừng: bấm **Ctrl+C** trong terminal đang chạy.

---

## Các trang mở được

App deploy ở **root**, nên URL gọn, không có tên ứng dụng phía sau cổng.

| Mở URL này | Ra trang gì | Mã HTTP |
|------------|-------------|:-------:|
| `localhost:8080` | Join our email list — trang chính | 200 |
| `localhost:8080/404` | Trang lỗi Không tìm thấy | **404** |
| `localhost:8080/403` | Trang lỗi Không có quyền | **403** |
| `localhost:8080/500` | Trang lỗi Máy chủ | **500** |
| `localhost:8080/bat-ky-gi` | Cũng ra trang 404 (URL không tồn tại) | **404** |
| `localhost:8080/temp/` | **Trang demo** — nút bấm cho từng lỗi | 200 |
| `localhost:8080/temp/loi_el.jsp` | Lỗi EL sai tên property (slide 34) | **500** |

Điền form ở trang chính rồi bấm **Join Now** → sang `thanks.jsp`.
Bỏ trống một ô rồi bấm → quay lại form kèm thông báo lỗi, **chữ đã gõ vẫn còn**.

> **Lưu ý về mã HTTP.** Ba URL `/404`, `/403`, `/500` trả về **mã lỗi thật**,
> không phải 200 kèm nội dung trang lỗi. Mở tab Network trong DevTools (F12) là
> thấy. Đây là lý do chúng đi qua `ErrorTestServlet` chứ không trỏ thẳng vào
> file `.jsp` — trỏ thẳng thì trình duyệt nhận 200, mã sai.

---

## Cấu trúc

```
ch06jsp/
├── run.ps1                              build + chạy
├── tools/DevServer.java                 embedded Tomcat (chỉ dùng lúc dev)
└── src/main/
    ├── java/murach/
    │   ├── business/User.java           slide 4-5  — JavaBean
    │   └── email/
    │       ├── EmailListServlet.java    slide 7-8  — servlet điều khiển
    │       └── ErrorTestServlet.java    mở /404 /403 /500
    └── webapp/
        ├── index.jsp                    slide 28   — trang chính
        ├── thanks.jsp                   slide 30   — trang xác nhận
        ├── error_404.jsp                slide 35
        ├── error_403.jsp                (KHÔNG có trong slide — thêm theo đề)
        ├── error_500.jsp                slide 35
        ├── temp/                        FOLDER DEMO — xoá đi app vẫn chạy
        │   ├── index.jsp                menu bấm thử từng lỗi
        │   └── loi_el.jsp               dựng lại đúng lỗi slide 34
        ├── styles/main.css
        └── WEB-INF/
            ├── web.xml                  khai <error-page>
            └── includes/                slide 27 — ĐỂ TRONG WEB-INF
                ├── header.html
                └── footer.jsp
```

---

## Chương 6 dạy gì, và code nào thể hiện

| Ý chính | Xem ở |
|---------|-------|
| JavaBean: constructor rỗng + get/set + Serializable | `User.java` |
| EL đọc property: `${user.email}` | `index.jsp`, `thanks.jsp` |
| JSTL `<c:if>` hiện thông báo lỗi | `index.jsp` |
| Include lúc **chạy**: `<c:import>` | `index.jsp`, `thanks.jsp` |
| Include lúc **biên dịch**: `<%@ include %>` | 3 file `error_*.jsp` |
| Scriptlet kiểu cũ `<% %>` và `<%= %>` | `includes/footer.jsp` |
| Trang lỗi tuỳ biến | `web.xml` + 3 file `error_*.jsp` |

Dùng **cả hai** kiểu include là cố ý, để đối chiếu:
`<c:import>` cho trang thường (sửa header thấy ngay khi F5),
`<%@ include %>` cho trang lỗi (ít khâu có thể sai khi hệ thống đang hỏng).

`includes/footer.jsp` cố ý viết bằng scriptlet đúng như slide 27, để bạn thấy
cách viết cũ mà chương 6 đang so sánh. Slide 22 kết luận: code mới thì dùng EL.

---

## Ba lỗi hay gặp

**Sửa `header.html` mà F5 không thấy đổi.**
Trang lỗi dùng `<%@ include %>` — include lúc biên dịch. File cha không đổi nên
Tomcat tưởng không cần dịch lại. → **Restart server.** (Slide 33)

**Trang trắng, hoặc hiện nguyên chữ `<c:if>` ra màn hình.**
Thiếu dòng `taglib` ở đầu file, hoặc thiếu jar JSTL. → Kiểm tra dòng đầu file
`.jsp` và thư mục `.libs/`.

**Sửa file `.java` mà không thấy tác dụng.**
File `.jsp` sửa là thấy ngay, nhưng `.java` phải biên dịch lại.
→ Ctrl+C rồi chạy lại `run.ps1`.

---

## Cổng đang dùng trong thư mục Web

| Dự án | Cổng | URL |
|-------|-----:|-----|
| `ch02email` | 8080 | `localhost:8080/ch02email/` |
| `ch05email` | 8081 | `localhost:8081/ch05email/` |
| **`ch06jsp`** | **8080** | **`localhost:8080/`** |
| `DoAn-WebDocTruyen` | 8080 | `localhost:8080/webdoctruyen/` |

Ba dự án cùng dùng 8080 nên **chỉ chạy được một cái tại một thời điểm**.
Muốn chạy song song thì thêm `-Port`, ví dụ `.\run.ps1 -Port 9090`.

---

## Ba lỗi về includes đã sửa

Ba lỗi này đều **có thật, đã kiểm chứng bằng curl và ảnh chụp**, không phải lo xa.

### 1. File include mở được bằng URL

Trước: `/includes/header.html` trả **200** kèm nửa trang HTML hỏng
(`<html><body><div>` không có thẻ đóng). `/includes/footer.jsp` trả về mấy thẻ
đóng lạc lõng.

Sửa: chuyển cả hai vào `/WEB-INF/includes/`. Tomcat chặn mọi truy cập trực tiếp
vào `WEB-INF`, nên giờ trả **404**. Include từ trong code vẫn chạy bình thường.

### 2. CSS vỡ ở URL nhiều cấp

`header.html` viết `href="styles/main.css"` — đường dẫn **tương đối**.

Ở `/khong-co` thì trình duyệt tìm `/styles/main.css` → đúng.
Nhưng ở `/truyen/khong-co` nó tìm `/truyen/styles/main.css` → **404**, và trang
lỗi hiện ra trần trụi không có định dạng nào.

Sửa: đổi thành `/styles/main.css` (có dấu `/` đầu — tuyệt đối từ gốc).

> Cách này đúng vì app deploy ở **root**. Nếu sau này deploy kèm tên ứng dụng
> (`/ch06jsp/`) thì phải đổi `header.html` thành `.jsp` và dùng
> `${pageContext.request.contextPath}` — file `.html` không chạy được EL.

### 3. Không có chỗ nào demo lỗi 500

Sửa: thêm folder `temp/` với `loi_el.jsp` dựng lại **đúng lỗi ở slide 34**:

```jsp
<%
    User user = new User("John", "Smith", "jsmith@gmail.com");
    request.setAttribute("user", user);
%>
Email address: ${user.emailAddress}   <%-- User chỉ có getEmail() --%>
```

Kết quả khớp y hệt ảnh slide 34:

```
javax.el.PropertyNotFoundException:
Property [emailAddress] not found on type [murach.business.User]
```

Khác một điểm: sách hiện trang lỗi mặc định xấu xí của Tomcat, còn ở đây rơi
vào `error_500.jsp` tự làm.
