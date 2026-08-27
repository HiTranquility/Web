# Chapter 6 — How to develop JSPs · Bảng ánh xạ slide → code

Nguồn: `Chapter 06 slides.pptx` (Murach's Java Servlets/JSP, 3rd Ed.) — **35 slide**.
Code chạy được nằm ở [`../ch06-demo/`](../ch06-demo/).
Muốn trình bày cho cô: xem [kịch bản demo từng bước](chapter06-demo.md).

**Cách đọc bảng:** cột *Code?* cho biết slide đó có code cần viết hay không.
Slide chỉ là bảng tra, khái niệm, hay ảnh chụp màn hình thì ghi **—**.

---

## Tổng quan

| Loại slide | Số lượng |
|------------|---------:|
| ✅ Có code cần viết | 15 |
| 📷 Ảnh chụp — **là bản đặc tả, phải làm giống** | 3 (slide 29, 30, 34) |
| — Bảng tra / khái niệm | 17 |
| **Tổng** | **35** |

> **Đừng bỏ qua 3 slide ảnh chụp.** Slide không in code không có nghĩa là không
> phải làm gì. Slide 29 và 30 là ảnh chụp hai trang phải dựng ra; slide 34 chụp
> một lỗi cụ thể rất đáng học. Chi tiết ở cuối file.

---

## Bảng ánh xạ đầy đủ — kèm ĐƯỜNG DẪN và SỐ DÒNG

Mọi đường dẫn tính từ `temp/ch06-demo/`.
Cột **Ở đâu** ghi rõ file và dòng, mở ra là thấy ngay.

| Slide | Nội dung | Code? | Ở đâu |
|:-----:|----------|:-----:|-------|
| 1 | Tiêu đề chương | — | |
| 2 | Mục tiêu — Applied | — | |
| 3 | Mục tiêu — Knowledge | — | |
| **4** | Lớp User bean (phần 1) | ✅ | `src/main/java/murach/business/User.java`<br>**dòng 32** `class User implements Serializable`<br>**36–38** 3 field · **45** constructor rỗng · **52** constructor 3 tham số |
| **5** | Lớp User bean (get/set) | ✅ | cùng file, **dòng 65–87**<br>`getFirstName` 65 · `setFirstName` 69 · `getLastName` 73<br>`setLastName` 77 · `getEmail` 81 · `setEmail` 85 |
| 6 | 3 quy tắc JavaBean | — | *khái niệm* — chú thích trong `User.java` **dòng 5–30** |
| **7** | Hiển thị attribute | ✅ | `src/main/java/murach/email/EmailListServlet.java`<br>**dòng 81–83** `new GregorianCalendar()` → `setAttribute("currentYear", …)` |
| **8** | Hiển thị property | ✅ | cùng file, **dòng 50** `new User(...)` · **dòng 75** `setAttribute("user", user)` |
| 9 | Bảng 4 scope | — | *bảng tra* — chép lại bên dưới |
| 10 | Khái niệm EL | — | |
| **11** | taglib directive | ✅ | `src/main/webapp/index.jsp` **dòng 2**<br>`src/main/webapp/thanks.jsp` **dòng 2** |
| **12** | Thẻ `<c:if>` | ✅ | `src/main/webapp/index.jsp` **dòng 26–28** |
| 13 | Khái niệm JSTL | — | jar JSTL do `run.ps1` tự tải, xem **dòng 32–33** của `run.ps1` |
| 14 | Bảng 5 loại thẻ JSP | — | *bảng tra* — chép lại bên dưới |
| **15** | directive + scriptlet + expression | ✅ | `src/main/webapp/WEB-INF/includes/footer.jsp`<br>**dòng 2** page import · **16–19** scriptlet · **20** `<%= currentYear %>` |
| 16 | Khái niệm thẻ JSP | — | |
| **17** | 3 kiểu chú thích | ✅ | JSP comment: `index.jsp` **dòng 3–17**, `temp/index.jsp` **2–11**<br>chú thích 1 dòng: `index.jsp` **dòng 24** |
| 18 | Khái niệm chú thích | — | *bảng so sánh* bên dưới |
| **19** | Đọc bean bằng scriptlet | ✅ | *không dùng trong dự án* — code slide chép ở mục "Ba cách" bên dưới |
| **20** | Đọc bean bằng thẻ JSP chuẩn | ✅ | *không dùng trong dự án* — code slide chép ở mục "Ba cách" bên dưới |
| **21** | Đọc bean bằng EL | ✅ | **cách dự án dùng**<br>`index.jsp` **dòng 37, 40, 43** (thuộc tính `value=`)<br>`thanks.jsp` **dòng 40, 42, 44** (hiển thị) |
| 22 | So sánh EL vs thẻ chuẩn | — | |
| **23** | Cú pháp `<jsp:useBean>` | ✅ | *không dùng* — dự án dùng EL. Cú pháp chép bên dưới |
| **24** | `getProperty` / `setProperty` | ✅ | *không dùng* — cú pháp chép bên dưới |
| 25 | Khái niệm thẻ JSP chuẩn | — | |
| 26 | Bảng escape sequence | — | *bảng tra* — chép lại bên dưới |
| **27** | `header.html` + `footer.jsp` | ✅ | `src/main/webapp/WEB-INF/includes/header.html` (18 dòng)<br>`src/main/webapp/WEB-INF/includes/footer.jsp` (23 dòng) |
| **28** | JSP dùng cả hai include | ✅ | `src/main/webapp/index.jsp` — **dòng 18** mở, **dòng 50** đóng |
| 29 | Ảnh chụp `index.jsp` | 📷 | `src/main/webapp/index.jsp` — **ảnh là bản đặc tả giao diện** |
| 30 | Ảnh chụp `thanks.jsp` | 📷 | `src/main/webapp/thanks.jsp` — 4 phần bắt buộc: h1 **33**, 3 dòng info **39–44**, đoạn văn **47**, nút Return **56–59** |
| **31** | Include lúc **biên dịch** | ✅ | `error_404.jsp` **27 / 56** · `error_403.jsp` **25 / 46**<br>`error_500.jsp` **26 / 55** · `demo/index.jsp` **12 / 48** |
| **32** | Include lúc **chạy** | ✅ | `index.jsp` **18 / 50** · `thanks.jsp` **31 / 61** |
| 33 | Khái niệm include | — | bảng so sánh 2 kiểu ở cuối file |
| 34 | Ảnh chụp lỗi 500 của Tomcat | 📷 | **dựng lại ở** `src/main/webapp/demo/loi_el.jsp` **dòng 29** tạo bean, **dòng 49** gọi sai property |
| 35 | Lỗi JSP thường gặp: **404, 500** | — | `src/main/webapp/WEB-INF/web.xml`<br>404 → **dòng 18–21** · 500 (exception) → **34–37** · 500 (mã) → **41–44** |

### Ngoài slide — code thêm vào

| Việc | Ở đâu |
|------|-------|
| Trang lỗi **403** *(không có trong slide)* | `error_403.jsp` · khai trong `web.xml` **dòng 24–27** |
| Mở thẳng `/404` `/403` `/500` để demo | `src/main/java/murach/demo/ErrorTestServlet.java`<br>**dòng 30** `@WebServlet({"/404","/403","/500"})`<br>**55** `sendError(403)` · **58** `sendError(404)` · **68** `throw` |
| Trang demo bấm thử lỗi | `src/main/webapp/demo/index.jsp` |

> ⚠️ **Slide không hề nhắc tới 403.** Slide 35 chỉ liệt kê 404 và 500.
> File `error_403.jsp` là **thêm vào theo yêu cầu**, không có trong sách.

---

## Các bảng tra (slide không có code)

### Slide 9 — 4 scope EL tìm qua, theo thứ tự

EL tìm từ scope **nhỏ nhất** tới **lớn nhất**, gặp trước lấy trước:

| Scope | Object thật |
|-------|-------------|
| `page` | `PageContext` — chỉ trong trang hiện tại |
| `request` | `HttpServletRequest` — sống qua forward |
| `session` | `HttpSession` — sống qua nhiều request của một người |
| `application` | `ServletContext` — cả ứng dụng dùng chung |

Muốn chỉ đích danh: `${requestScope.user}`, `${sessionScope.user}`.

> Slide 10 dòng cuối: attribute ở `application` scope **không thread-safe** —
> đúng vấn đề biến instance của servlet ở chương 5.

### Slide 14 — 5 loại thẻ JSP

| Thẻ | Tên | Công dụng |
|-----|-----|-----------|
| `<%@ %>` | JSP directive | đặt điều kiện cho cả trang |
| `<% %>` | JSP scriptlet | chèn khối lệnh Java |
| `<%= %>` | JSP expression | in giá trị chuỗi của biểu thức |
| `<%-- --%>` | JSP comment | báo JSP engine bỏ qua |
| `<%! %>` | JSP declaration | khai báo biến instance và method |

> Slide 22: code mới **không dùng** 3 loại đầu nữa. Dùng EL + JSTL.
> Nhưng vẫn phải biết đọc, vì code cũ đầy thứ này.

### Slide 18 — 3 kiểu chú thích

| Kiểu | Có chạy? | Người dùng thấy? |
|------|:--------:|------------------|
| `<!-- -->` HTML | **CÓ** | **CÓ** — hiện trong View Source |
| `<%-- --%>` JSP | KHÔNG | không |
| `//` và `/* */` Java | KHÔNG | không |

> Hàng đầu là cái bẫy: chú thích HTML **vẫn chạy** code bên trong, và kết quả
> vẫn được gửi tới trình duyệt. Đừng dùng nó để "tạm tắt" code nhạy cảm.

### Slide 26 — Escape sequence

| Ký tự | Escape |
|-------|--------|
| `'` | `\'` |
| `"` | `\"` |
| `\` | `\\` |
| `<%` | `<\%` |
| `%>` | `%\>` |

> Mẹo: dùng nháy đôi bao ngoài khi giá trị có nháy đơn, và ngược lại —
> khỏi cần escape.

---

## Slide 19–21: ba cách đọc cùng một bean

Đây là phần quan trọng nhất của chương. Cùng một kết quả, ba kiểu viết:

**Cách 1 — scriptlet (slide 19).** Phải import, phải ép kiểu, phải tự kiểm tra null:

```jsp
<%@ page import="murach.business.User" %>
<%
    User user = (User) request.getAttribute("user");
    if (user == null) { user = new User(); }
%>
<span><%= user.getEmail() %></span>
```

**Cách 2 — thẻ JSP chuẩn (slide 20).** Không import, không ép kiểu:

```jsp
<jsp:useBean id="user" scope="request" class="murach.business.User"/>
<span><jsp:getProperty name="user" property="email"/></span>
```

**Cách 3 — EL (slide 21).** ← **dự án dùng cách này**

```jsp
<span>${user.email}</span>
```

**Slide 22 kết luận:** dùng EL cho code mới. Thẻ JSP chuẩn chỉ khi bảo trì code cũ.

Lý do "EL xử lý null tốt hơn": `${user.email}` khi `user` là null thì in ra
**rỗng**; còn `user.getEmail()` trong scriptlet là **NullPointerException**,
sập cả trang. Đó là vì sao cách 1 phải có `if (user == null)` còn cách 3 thì không.

---

## Slide 31 vs 32: chọn kiểu include nào

| | `<%@ include %>` (biên dịch) | `<c:import>` / `<jsp:include>` (chạy) |
|---|---|---|
| Thời điểm | lúc dịch JSP | mỗi lần có request |
| Kết quả | **một** servlet | nhiều request con |
| Tốc độ | nhanh hơn | chậm hơn chút |
| Thấy biến trang cha | **CÓ** | KHÔNG |
| Sửa file include | **phải dịch lại trang cha** | hiện ngay lần sau |
| Lấy từ server khác | không | `c:import` thì được |

**Dự án dùng cả hai, có lý do:**

- `index.jsp`, `thanks.jsp` → `<c:import>` (slide 32) — sửa header thấy ngay
- `error_*.jsp` → `<%@ include %>` (slide 31) — trang lỗi phải chắc chắn render
  được kể cả khi hệ thống đang hỏng; include tĩnh ít khâu có thể sai hơn

> **Cái bẫy của slide 33:** dùng `<%@ include %>` rồi sửa file header, F5 mà
> không thấy gì đổi — vì file cha không đổi nên Tomcat tưởng không cần dịch lại.
> **Cách xử lý: restart server.**

---

## Đối chiếu yêu cầu của cô → file

| Yêu cầu | File |
|---------|------|
| Lớp JavaBean | `src/main/java/murach/business/User.java` |
| JSP dùng EL đọc property bean | `index.jsp`, `thanks.jsp` |
| JSP dùng JSTL `<c:if>` | `index.jsp` |
| File include (header + footer) | `includes/header.html`, `includes/footer.jsp` |
| Include lúc biên dịch | `error_404.jsp`, `error_403.jsp`, `error_500.jsp` |
| Include lúc chạy | `index.jsp`, `thanks.jsp` |
| Trang lỗi 404 | `error_404.jsp` + khai trong `web.xml` |
| Trang lỗi 500 | `error_500.jsp` + khai trong `web.xml` |
| Trang lỗi 403 *(không có trong slide)* | `error_403.jsp` + khai trong `web.xml` |
| Servlet điều khiển | `murach/email/EmailListServlet.java` |
| Mở thẳng từng trang lỗi để demo | `murach/demo/ErrorTestServlet.java` |

---

## Cách chạy và mở các trang

```bash
powershell -ExecutionPolicy Bypass -File ch06jsp/run.ps1
```

| URL | Trang | Mã HTTP |
|-----|-------|:-------:|
| `localhost:8080` | Join our email list | 200 |
| `localhost:8080/404` | Không tìm thấy | **404** |
| `localhost:8080/403` | Không có quyền | **403** |
| `localhost:8080/500` | Lỗi máy chủ | **500** |
| `localhost:8080/bat-ky-gi` | 404 (URL không tồn tại) | **404** |

Ba URL lỗi trả **mã HTTP thật**, không phải 200 kèm nội dung trang lỗi —
kiểm chứng bằng tab Network của DevTools (F12).

---

## Slide 29, 30, 34 — ba ảnh chụp, và vì sao phải xem kỹ

Ba slide này không in dòng code nào, nhưng **không phải là slide bỏ qua được**.

### Slide 29 — `index.jsp` trông ra sao

Ảnh chụp cho biết chính xác trang phải có gì: tiêu đề, đoạn mô tả, ba ô nhập
(đã điền sẵn `jsmith@gmail.com` / `John` / `Smith`), nút **Join Now**, và dòng
copyright ở cuối do `footer.jsp` sinh ra.

URL trong ảnh là `localhost:8080/ch06email/` — sách đặt tên ứng dụng là
**ch06email**. Dự án này tên `ch06jsp` và deploy ở root nên URL là
`localhost:8080/`. Khác tên thôi, nội dung giống.

### Slide 30 — `thanks.jsp` trông ra sao

Đây là slide dễ làm thiếu nhất. Ảnh cho thấy trang phải có **đủ 4 phần**:

1. Tiêu đề "Thanks for joining our email list"
2. Ba dòng Email / First Name / Last Name
3. Đoạn văn *"To enter another email address, click on the Back button in your
   browser or the Return button shown below."*
4. **Nút Return** — là `<form>` có tham số ẩn `action=join`, không phải thẻ `<a>`

Phần 3 và 4 rất dễ bỏ sót vì slide không in code, chỉ có ảnh.

### Slide 34 — lỗi 500 mặc định của Tomcat, và nó dạy gì

Ảnh này **không phải** trang lỗi tự làm — nó là trang mặc định xấu xí của
Tomcat, để bạn thấy cái mình cần thay thế (và để tập đọc nó).

Lỗi cụ thể trong ảnh:

```
javax.el.PropertyNotFoundException:
Property 'emailAddress' not found on type murach.business.User
```

JSP viết `${user.emailAddress}` trong khi lớp `User` chỉ có `getEmail()`.

**Bài học:** tên property trong EL lấy từ **tên get method**, không phải tên
biến, cũng không phải tên cột database. `getEmail()` → `${user.email}`.

Và lưu ý sự khác biệt quan trọng:

| Tình huống | Kết quả |
|------------|---------|
| Bean là `null` | EL in ra **rỗng**, không lỗi |
| Bean có, nhưng **sai tên property** | **Ném exception → 500** |

Nhiều người nhớ nhầm là "EL không bao giờ lỗi". Không đúng — nó chỉ dễ tính với
`null`, còn gõ sai tên property thì sập ngay.

Ảnh cũng cho thấy trang lỗi mặc định phơi ra: phiên bản Tomcat, tên package,
số dòng, toàn bộ stack trace. Đó chính là lý do phải khai `<error-page>` trong
`web.xml` — xem `error_500.jsp`.
