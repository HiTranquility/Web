# ⚡ Mẹo Nhớ Thần Tốc: Frontend (JSP/JSTL) ➔ Backend (Servlet/Context) ➔ DAO (JDBC)

> **Cẩm nang "bỏ túi" chống lú khi code Java Web (Servlet & JSP).**  
> Dành cho những lúc: *"Ủa thẻ này viết sao ta?", "Lấy param hay attribute?", "Sao đổ mảng không lên?", "JDBC đếm từ 0 hay 1?"*

---

## 📑 Mục Lục Nhanh
1. [Frontend: Giải mã "@p", Thẻ JSTL & Đổ Mảng](#-phần-1-frontend-mẹo-nhớ-thẻ-tag--đổ-mảng)
2. [Backend: Phân biệt Scopes, Context & Param vs Attr](#-phần-2-backend-mẹo-nhớ-context-scope--parameter-vs-attribute)
3. [Tầng DAO: Bộ 3 Tam Giác Vàng JDBC & Template Ăn Liền](#-phần-3-tầng-dao-mẹo-nhớ-jdbc--4-khuôn-crud)
4. [Bảng Tra Cứu Tốc Hành 30 Giây (Bẫy Hay Gặp)](#-phần-4-bảng-tra-cứu-tốc-hành-30-giây)

---

## 🎨 PHẦN 1: Frontend — Mẹo Nhớ Thẻ Tag & Đổ Mảng

### 1.1. Giải mã vụ hay bị lú: "@p gì đó" thực chất là gì?

Bạn hay nhớ mang máng `@p` vì có **4 thứ dễ lẫn lộn** sau đây:

| Bạn đang nghĩ tới | Cú pháp chính xác | Ý nghĩa & Khi nào dùng? |
|---|---|---|
| **Chỉ thị JSP Directive** (có chữ `@`) | `<%@ page ... %>`<br><%@ taglib ... %>`<br><%@ include ... %>` | **Cấu hình trang**: Cứ có `@` là khai báo ở đầu file JSP (nhập tiếng Việt UTF-8, gắn thư viện `c:`, `fmt:`). |
| **Lấy param từ URL** (`?p=...`) | `${param.p}` hoặc `${param.id}` | **Móc trực tiếp tham số trên URL**: Ví dụ link `story?p=2&id=5` thì gọi `${param.p}` ra số `2` mà không cần Servlet nạp attribute! |
| **Tên biến lặp `p`** | `<c:forEach var="p" items="${...}">` | **Tên biến tự đặt**: Khi lặp danh sách (như product, post, page), người ta hay gán `var="p"`, sau đó gọi `${p.name}`, `${p.title}`. |
| **Đường dẫn gốc Context Path** | `${pageContext.request.contextPath}` | **Link gốc web**: Tránh bị gãy link ảnh/CSS khi đổi cổng hay đổi tên app. |

---

### 1.2. Mẹo nhớ thẻ Đổ Mảng `<c:forEach>` (Không bao giờ quên)

```jsp
<c:forEach items="${stories}" var="story" varStatus="loop">
    <tr>
        <td>#${loop.count}</td>
        <td><c:out value="${story.title}"/></td>
    </tr>
</c:forEach>
```

#### 💡 Khẩu quyết 3 chữ vàng:
1. **`items` (Số NHIỀU - Có chữ `s`)**: Cái **GIỎ HÀNG / THÙNG CHỨA** từ Servlet gửi sang (ví dụ: `${stories}`, `${chapters}`, `${list}`). Phải có dấu `${}`.
2. **`var` (Số ÍT - Không có `s`)**: **TỪNG MÓN ĐỒ** móc ra từ giỏ để đem đi vẽ (ví dụ: `story`, `ch`, `item`). **Tuyệt đối KHÔNG có dấu `${}`**.
3. **`varStatus` (Camera giám sát / Người chấm công)**:
   - `loop.count` ➔ Bắt đầu từ **1** (Dùng làm Số Thứ Tự STT cho người xem).
   - `loop.index` ➔ Bắt đầu từ **0** (Dùng khi tính toán theo chỉ số mảng Java).
   - `loop.first` ➔ `true` nếu là dòng đầu tiên.
   - `loop.last`  ➔ `true` nếu là dòng cuối cùng.

---

### 1.3. Mẹo nhớ các thẻ Rẽ Nhánh (`c:if` vs `c:choose`)

#### A. Rẽ nhánh đơn: `<c:if>`
*Mẹo nhớ: Muốn xem kết quả thì phải làm bài **`test`**.*
```jsp
<c:if test="${not empty stories}">
    <p>Có ${stories.size()} truyện trong danh sách.</p>
</c:if>

<c:if test="${currentUser.role eq 'ADMIN'}">
    <span class="badge">Quản trị viên</span>
</c:if>
```
> **Lưu ý phép so sánh trong EL:** Dùng chữ cho đỡ lỗi HTML:
> - Bằng: `eq` hoặc `==`
> - Khác: `ne` hoặc `!=`
> - Rỗng: `empty` / `not empty`

#### B. Rẽ nhánh Nếu — Thì — Ngược lại: `<c:choose>`
*Mẹo nhớ: Giống hệt `switch - case - default` trong Java/C++:*
```jsp
<c:choose>
    <c:when test="${story.vip}">
        <span class="vip">Truyện VIP (Khóa)</span>
    </c:when>
    <c:when test="${story.views > 10000}">
        <span class="hot">Truyện Hot</span>
    </c:when>
    <c:otherwise>
        <span>Truyện Thường</span>
    </c:otherwise>
</c:choose>
```
- `<c:choose>` = Mở ngoặc switch.
- `<c:when test="...">` = `case (điều kiện):`
- `<c:otherwise>` = `default:` (nếu các cái trên đều sai).

---

### 1.4. In dữ liệu: Khi nào dùng `${...}` vs `<c:out>`?

| Cách viết | Ví dụ | Khi nào dùng? | Lý do |
|---|---|---|---|
| `<c:out value="${...}"/>` | `<c:out value="${comment.content}"/>` | **Dữ liệu người dùng nhập** (tên, bình luận, mô tả) | **Áo giáp chống XSS**: Tự biến `<script>` thành ký tự an toàn. |
| `${...}` | `${pageContext.request.contextPath}/home`<br>`${story.id}` | Link hệ thống, URL, số ID, class CSS | Nhanh, gọn, không bị mã hóa dấu `/` hay `?`. |

---

### 1.5. Thẻ làm đẹp số & ngày tháng (`fmt:`)

Cần khai báo ở đầu file: `<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>`

```jsp
<%-- Định dạng số lượt xem có dấu phẩy: 1,500,000 --%>
👁️ <fmt:formatNumber value="${story.views}" type="number" />

<%-- Định dạng tiền tệ / Xu: 50,000 --%>
💰 <fmt:formatNumber value="${wallet.balance}" pattern="#,###" /> Xu

<%-- Định dạng ngày giờ: 07/10/2026 18:30 --%>
🕒 <fmt:formatDate value="${story.createdAt}" pattern="dd/MM/yyyy HH:mm" />
```

---

## ☕ PHẦN 2: Backend — Mẹo Nhớ Context, Scope & Parameter vs Attribute

### 2.1. Khẩu quyết bất hủ: "Parameter vs Attribute"

Rất nhiều bạn bị lú chỗ này:
> **Khách gửi lên = PARAMETER (luôn là chuỗi String)**  
> **Server ném qua lại = ATTRIBUTE (đối tượng Object tuỳ ý)**

```text
[Trình duyệt] ──── (Form / URL) ────> request.getParameter("id")    (Hứng String từ Khách)
                                             │
                                             ▼ [Servlet xử lý]
                                             │
[Trang JSP]  <── (Nội bộ Server) <─── request.setAttribute("story", s) (Ném Object cho JSP)
```

| Tiêu chí | `request.getParameter("key")` | `request.setAttribute("key", value)` |
|---|---|---|
| **Ai tạo ra?** | Người dùng (Gõ form, bấm link `?id=123`) | Lập trình viên Backend gán trong code Java |
| **Kiểu dữ liệu** | **Luôn luôn là `String`** (hoặc `null`) | **Là `Object`** (Model, List, Map, boolean...) |
| **Cách dùng ở JSP** | `${param.key}` | `${key}` |
| **Lấy nhiều ô cùng tên (Checkbox)** | Dùng `request.getParameterValues("tagIds")` ➔ Trả về `String[]` | Không cần, set `List<Tag>` sang luôn |

---

### 2.2. "Lấy từ Context" — Bản chất là gì?

Trong Servlet bạn hay nghe "lấy context", "context path". Nhớ 2 hàm này:

1. **`request.getContextPath()`**:
   - Trả về: Tên gốc của website (ví dụ `""` hoặc `"/WebDocTruyen"`).
   - Dùng để: Viết URL tuyệt đối, chống lỗi nhảy sai đường dẫn:
     ```java
     response.sendRedirect(request.getContextPath() + "/story?action=list");
     ```
   - Bên JSP tương ứng là: `${pageContext.request.contextPath}`.

2. **`getServletContext()`**:
   - Trả về: Đối tượng `ServletContext` (Kho tổng của cả Web Server).
   - Dùng để: Đọc thư mục gốc lưu file upload, hoặc lưu dữ liệu toàn hệ thống:
     ```java
     String uploadDir = getServletContext().getRealPath("/uploads");
     ```

---

### 2.3. 4 chiếc hộp "Scope" — Ai sống lâu hơn ai?

Khi gọi `.setAttribute("key", val)`, bạn đang chọn **bỏ vào chiếc hộp nào**:

| Tên Hộp (Scope) | Đối tượng Java | Khi nào bị vứt thùng rác? | Phù hợp lưu cái gì? |
|---|---|---|---|
| **1. Page** | `pageContext` | Đọc xong trang JSP đó là vứt | Biến tạm tính toán trong nội bộ 1 file JSP. |
| **2. Request** | `request` | Trả HTML về trình duyệt xong là vứt (F5 là mất) | Danh sách truyện, thông tin chi tiết, kết quả tìm kiếm. |
| **3. Session** | `session` | Tắt trình duyệt hoặc quá hạn (F5 **KHÔNG** mất) | User đăng nhập (`currentUser`), giỏ hàng, flash message. |
| **4. Application** | `getServletContext()` | Server tắt hẳn mới mất (Cả thế giới dùng chung) | Cấu hình website, bộ đếm tổng lượt truy cập toàn site. |

#### 💡 Mẹo test phân biệt Scope siêu nhanh:
- **F5 lại trang:** `request` mất, `session` còn.
- **Mở tab Ẩn danh (Incognito):** `session` mất (vì là phiên của khách mới).
- **Restart lại Tomcat:** `application` mới mất.

---

### 2.4. Mẹo nhớ Session: `getSession()` vs `getSession(false)`

```java
// 1. Khi ĐĂNG NHẬP THÀNH CÔNG: Cần tạo mới session để lưu
HttpSession session = request.getSession(); // Tự tạo session nếu chưa có
session.setAttribute("currentUser", user);

// 2. Khi KIỂM TRA ĐĂNG NHẬP: Không được tự tiện tạo session rác
HttpSession session = request.getSession(false); // false = nếu chưa có thì trả về null
if (session != null && session.getAttribute("currentUser") != null) {
    // Đã đăng nhập
}

// 3. Khi ĐĂNG XUẤT: Huỷ hoàn toàn session
HttpSession session = request.getSession(false);
if (session != null) {
    session.invalidate(); // Xoá sạch hộp session
}
```

---

### 2.5. `forward` vs `sendRedirect` — Khẩu quyết sống còn

> **"GET thì Forward — POST thì Redirect"** (Mô hình PRG: Post-Redirect-Get)

```java
// ĐỌC DỮ LIỆU (doGet) ➔ FORWARD (Đi ngầm nội bộ, URL giữ nguyên, request còn nguyên)
request.setAttribute("story", story);
getServletContext()
    .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
    .forward(request, response);

// GHI DỮ LIỆU XONG (doPost) ➔ REDIRECT (Đá sang URL mới, F5 không bị gửi lặp lại form)
response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + id);
return; // Nhớ return ngay sau khi redirect!
```

---

## 🗄️ PHẦN 3: Tầng DAO — Mẹo Nhớ JDBC & 4 Khuôn CRUD

### 3.1. Bộ 3 Tam Giác Vàng JDBC

```text
Connection (Đường dây kết nối)
    │
    ▼
PreparedStatement (Nòng súng nạp câu SQL có dấu ?)
    │
    ▼
ResultSet (Bảng dữ liệu kết quả hứng về từ SELECT)
```

---

### 3.2. Ba cái bẫy "kinh điển" ở tầng DAO

1. **Dấu `?` trong JDBC đếm từ số 1 (KHÔNG PHẢI 0!)**:
   ```java
   String sql = "SELECT * FROM stories WHERE id = ? AND author_id = ?";
   ps.setInt(1, id);        // Dấu ? thứ nhất -> số 1
   ps.setInt(2, authorId);  // Dấu ? thứ hai  -> số 2
   ```
2. **`executeQuery()` vs `executeUpdate()`**:
   - `ps.executeQuery()`: Chỉ dùng cho lệnh **`SELECT`** ➔ Trả về bảng `ResultSet`.
   - `ps.executeUpdate()`: Dùng cho **`INSERT`, `UPDATE`, `DELETE`** ➔ Trả về số nguyên `int` (số dòng bị tác động).
3. **`try-with-resources`**: Luôn khai báo `Connection`, `PreparedStatement`, `ResultSet` trong dấu ngoặc đơn `try (...)` để Java tự động đóng cổng kết nối, **chống nghẽn CSDL (Connection leak)**.

---

### 3.3. 4 Mẫu DAO chuẩn "ăn liền" (Copy & Điền)

#### Mẫu 1: Lấy danh sách (Query List - `findAll` / `findLatest`)
```java
public List<Story> findAll() throws SQLException {
    List<Story> list = new ArrayList<>();
    String sql = "SELECT id, title, slug, views FROM stories ORDER BY id DESC";

    try (Connection con = DBConnection.get();
         PreparedStatement ps = con.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            Story s = new Story();
            s.setId(rs.getInt("id"));
            s.setTitle(rs.getString("title"));
            s.setSlug(rs.getString("slug"));
            s.setViews(rs.getInt("views"));
            list.add(s);
        }
    }
    return list;
}
```

#### Mẫu 2: Lấy 1 đối tượng theo ID (`findById`)
```java
public Story findById(int id) throws SQLException {
    String sql = "SELECT id, title, description FROM stories WHERE id = ?";

    try (Connection con = DBConnection.get();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, id); // Nhớ: số 1!

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) { // 1 bản ghi thì dùng if, không cần while
                Story s = new Story();
                s.setId(rs.getInt("id"));
                s.setTitle(rs.getString("title"));
                s.setDescription(rs.getString("description"));
                return s;
            }
        }
    }
    return null; // Không tìm thấy trả về null
}
```

#### Mẫu 3: Thêm mới bản ghi (`insert`)
```java
public boolean insert(Story story) throws SQLException {
    String sql = "INSERT INTO stories (title, slug, author_id) VALUES (?, ?, ?)";

    try (Connection con = DBConnection.get();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, story.getTitle());
        ps.setString(2, story.getSlug());
        ps.setInt(3, story.getAuthorId());

        return ps.executeUpdate() > 0; // > 0 tức là chèn thành công ít nhất 1 dòng
    }
}
```

#### Mẫu 4: Sửa / Xóa bản ghi (`update` / `delete`)
```java
public boolean updateStatus(int id, String status) throws SQLException {
    String sql = "UPDATE stories SET status = ? WHERE id = ?";

    try (Connection con = DBConnection.get();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setString(1, status);
        ps.setInt(2, id);

        return ps.executeUpdate() > 0;
    }
}
```

---

## 🚀 PHẦN 4: Bảng Tra Cứu Tốc Hành 30 Giây

| Tình huống code | Hay viết nhầm (SAI) | Cách viết đúng (CHUẨN) |
|---|---|---|
| Đổ mảng JSP | `<c:forEach items="stories" var="${s}">` | `<c:forEach items="${stories}" var="s">` |
| Đếm số thứ tự | `${loop.index}` *(bắt đầu từ 0)* | `${loop.count}` *(bắt đầu từ 1)* |
| Kiểm tra rỗng | `<c:if test="${stories != null}">` | `<c:if test="${not empty stories}">` |
| Lấy ô checkbox | `request.getParameter("tagIds")` *(chỉ lấy 1)* | `request.getParameterValues("tagIds")` *(lấy String[])* |
| Nhận số nguyên | `int id = request.getParameter("id");` | `int id = Integer.parseInt(request.getParameter("id"));` |
| Nhảy trang sau POST | `request.getRequestDispatcher(...).forward()` | `response.sendRedirect(request.getContextPath() + ...)` |
| Điền dấu `?` JDBC | `ps.setString(0, name)` *(lỗi Invalid Index)* | `ps.setString(1, name)` *(bắt đầu từ 1)* |
| Chạy câu INSERT | `ResultSet rs = ps.executeQuery()` *(sai ngữ cảnh)* | `int rows = ps.executeUpdate()` |
| Lấy link an toàn | `<a href="/home">` *(dễ gãy link khi đổi context)* | `<a href="${pageContext.request.contextPath}/home">` |

---

> 📖 **Liên kết tra cứu chuyên sâu trong dự án:**
> - Xem luồng dữ liệu minh họa bằng sơ đồ tuần tự: [`visual-flow-code-tinh-nang-moi.md`](visual-flow-code-tinh-nang-moi.md)
> - Xem giải thích chi tiết cơ chế hoạt động: [`giai-thich.md`](giai-thich.md)
> - Xem hướng dẫn code thực chiến chi tiết: [`huong-dan-code.md`](huong-dan-code.md)
