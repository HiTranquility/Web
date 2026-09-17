# 🛠️ Hướng dẫn code thực chiến từ A-Z — Tự tay viết tính năng

Tài liệu này là cẩm nang **"cầm tay chỉ việc"** dành cho mọi thành viên trong nhóm khi code tính năng mới: từ viết câu query trong DAO, bắt request & session trong Servlet, tới hiển thị mảng dữ liệu trên JSP bằng JSTL/EL.

---

## 🗺️ Toàn cảnh một luồng dữ liệu (Full Lifecycle)

Mỗi khi người dùng bấm chuột hoặc gửi một form, dữ liệu đi qua đúng 4 trạm:

```text
[Trình duyệt]
      │  gửi HTTP Request (GET / POST)
      ▼
[1. Filter]          Kiểm tra bảng mã (UTF-8) → Token CSRF → Quyền đăng nhập (Auth)
      │
      ▼
[2. Controller]      Servlet đọc tham số, lấy user từ Session, gọi DAO trong try-catch
      │
      ▼
[3. DAO & DB]        Mở kết nối → PreparedStatement với dấu ? → Map ResultSet sang Model
      │
      ▲  trả dữ liệu (Model / List<Model>) về Servlet
      │
[4. Servlet → View]  Servlet nạp vào requestScope → Forward sang Layout → JSP render HTML
```

---

## 1. Tầng DAO — Thao tác CSDL bằng tay

### Quy tắc bất di bất dịch:
1. **Luôn dùng `PreparedStatement` với dấu `?`** — tuyệt đối KHÔNG nối chuỗi SQL (phòng chống SQL Injection).
2. **Luôn dùng `try-with-resources`** để Connection, PreparedStatement, ResultSet tự động đóng, không rò rỉ kết nối.
3. **Ném `throws SQLException` lên Servlet** — không được nuốt lỗi bằng `catch {}` rỗng ở DAO.

---

### Mẫu 1: Lấy danh sách bản ghi (Query List)

```java
public List<Story> findLatest(int limit) throws SQLException {
    List<Story> list = new ArrayList<>();
    String sql = "SELECT id, title, slug, cover_url, views, created_at "
               + "FROM stories WHERE status = 'PUBLISHED' "
               + "ORDER BY created_at DESC LIMIT ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, limit);

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Story s = new Story();
                s.setId(rs.getInt("id"));
                s.setTitle(rs.getString("title"));
                s.setSlug(rs.getString("slug"));
                s.setCoverUrl(rs.getString("cover_url"));
                s.setViews(rs.getInt("views"));
                s.setCreatedAt(rs.getTimestamp("created_at"));
                list.add(s);
            }
        }
    }
    return list;
}
```

---

### Mẫu 2: Lấy đúng 1 bản ghi theo ID / Khoá (Query Single Object)

```java
public Story findById(int id) throws SQLException {
    String sql = "SELECT id, title, slug, author_id, description, status "
               + "FROM stories WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setInt(1, id);

        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Story s = new Story();
                s.setId(rs.getInt("id"));
                s.setTitle(rs.getString("title"));
                s.setSlug(rs.getString("slug"));
                s.setAuthorId(rs.getInt("author_id"));
                s.setDescription(rs.getString("description"));
                s.setStatus(rs.getString("status"));
                return s;
            }
        }
    }
    return null; // Không tìm thấy thì trả về null
}
```

---

### Mẫu 3: Thêm mới bản ghi & lấy ID tự sinh (Insert & Generated Keys)

```java
public int insert(Story story) throws SQLException {
    String sql = "INSERT INTO stories (title, slug, author_id, description, status) "
               + "VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

        ps.setString(1, story.getTitle());
        ps.setString(2, story.getSlug());
        ps.setInt(3, story.getAuthorId());
        ps.setString(4, story.getDescription());
        ps.setString(5, story.getStatus());

        ps.executeUpdate();

        // Lấy ID vừa sinh tự động
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                int generatedId = rs.getInt(1);
                story.setId(generatedId);
                return generatedId;
            }
        }
    }
    return 0;
}
```

---

### Mẫu 4: Cập nhật / Xoá bản ghi (Update / Delete)

```java
public boolean update(Story story) throws SQLException {
    String sql = "UPDATE stories SET title = ?, description = ?, status = ? WHERE id = ?";

    try (Connection conn = DBConnection.getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {

        ps.setString(1, story.getTitle());
        ps.setString(2, story.getDescription());
        ps.setString(3, story.getStatus());
        ps.setInt(4, story.getId());

        int affectedRows = ps.executeUpdate();
        return affectedRows > 0; // true nếu có ít nhất 1 dòng được cập nhật
    }
}
```

---

## 2. Tầng Controller (Servlet) — Điều phối bằng tay

### Khởi tạo DAO ở `init()`
Luôn tạo instance của DAO trong method `init()` của Servlet, **không** tạo lại `new DAO()` trong từng request:

```java
@WebServlet("/comment")
public class CommentServlet extends HttpServlet {
    private CommentDAO commentDAO;

    @Override
    public void init() throws ServletException {
        commentDAO = new CommentDAO();
    }
}
```

---

### Phân biệt rõ ràng 4 hàm quan trọng nhất: getParameter, getParameterValues, getAttribute, getSession

| Hàm | Thuộc đối tượng | Chiều dữ liệu | Kiểu trả về | Khi nào dùng? |
|-----|-----------------|---------------|-------------|---------------|
| `request.getParameter("key")` | `HttpServletRequest` | **Client → Server** | `String` (hoặc `null`) | Lấy giá trị ô input text, radio, query string trên URL (`?id=1`) |
| `request.getParameterValues("key")` | `HttpServletRequest` | **Client → Server** | `String[]` (hoặc `null`) | **Lấy mảng** khi người dùng tick nhiều checkbox hoặc chọn multi-select cùng tên `key` |
| `request.setAttribute("key", obj)` | `HttpServletRequest` | **Controller → JSP** | `void` | Gửi dữ liệu (Model, List, Map...) cho trang JSP hiển thị bằng `${key}` |
| `request.getAttribute("key")` | `HttpServletRequest` | Nội bộ server | `Object` (cần ép kiểu) | Đọc lại dữ liệu đã set trước đó trong cùng một request |
| `request.getSession()` | `HttpServletRequest` | Quản lý phiên | `HttpSession` | Lấy hoặc tạo mới Session người dùng (lưu đăng nhập, giỏ hàng, flash message) |
| `request.getSession(false)` | `HttpServletRequest` | Quản lý phiên | `HttpSession` (hoặc `null`) | Chỉ lấy session **nếu đã tồn tại**, không tự tiện tạo mới |

---

### Cách lấy tham số Request an toàn bằng tay

#### 1. Lấy chuỗi đơn (String)
```java
String q = request.getParameter("q");
q = (q == null) ? "" : q.trim(); // Chống NullPointerException và xoá khoảng trắng thừa
```

#### 2. Lấy số nguyên (Integer)
```java
int page = 1;
try {
    String pageRaw = request.getParameter("page");
    if (pageRaw != null && !pageRaw.trim().isEmpty()) {
        page = Integer.parseInt(pageRaw.trim());
    }
} catch (NumberFormatException e) {
    page = 1; // Fallback giá trị mặc định, không để văng 500
}
```

#### 3. Lấy mảng dữ liệu từ Form (Checkbox / Multi-Select)
Khi trên JSP có nhiều checkbox cùng thuộc tính `name="tagIds"`:
```jsp
<input type="checkbox" name="tagIds" value="1"> Tiên Hiệp
<input type="checkbox" name="tagIds" value="2"> Kiếm Hiệp
<input type="checkbox" name="tagIds" value="3"> Huyền Huyễn
```
Trong Controller Servlet, dùng `getParameterValues`:
```java
String[] rawTagIds = request.getParameterValues("tagIds");
List<Integer> tagIds = new ArrayList<>();

if (rawTagIds != null) {
    for (String tagIdStr : rawTagIds) {
        try {
            tagIds.add(Integer.parseInt(tagIdStr.trim()));
        } catch (NumberFormatException ignored) {
            // Bỏ qua giá trị rác
        }
    }
}
// Giờ ta có List<Integer> tagIds an toàn để truyền vào DAO
```

---

### Làm việc với Session (`HttpSession`) bằng tay

```java
// 1. Kiểm tra người dùng đã đăng nhập chưa
HttpSession session = request.getSession(false); // false = không tạo mới nếu chưa có
User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

if (currentUser == null) {
    // Chưa đăng nhập -> đá về trang login
    response.sendRedirect(request.getContextPath() + "/auth?action=login");
    return;
}

// 2. Lưu đối tượng vào Session lúc Đăng nhập thành công
request.getSession().setAttribute("currentUser", user);

// 3. Huỷ Session khi Đăng xuất
HttpSession curSession = request.getSession(false);
if (curSession != null) {
    curSession.invalidate();
}
response.sendRedirect(request.getContextPath() + "/home");
return;
```

---

### Khung chuẩn `doGet()` — Nạp dữ liệu và Forward sang View (Hiển thị trang)

```java
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    String action = request.getParameter("action");
    if (action == null) action = "list";

    try {
        if ("detail".equals(action)) {
            int id = parseIntOr(request.getParameter("id"), 0);
            Story story = storyDAO.findById(id);

            if (story == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            // Gửi dữ liệu cho JSP qua setAttribute
            request.setAttribute("story", story);
            request.setAttribute("pageTitle", story.getTitle());
            request.setAttribute("contentPage", "/WEB-INF/views/common/story/detail.jsp");

        } else {
            // Mặc định là list
            List<Story> stories = storyDAO.findAll();
            request.setAttribute("stories", stories);
            request.setAttribute("pageTitle", "Kho truyện");
            request.setAttribute("contentPage", "/WEB-INF/views/common/story/list.jsp");
        }

    } catch (SQLException e) {
        log("Lỗi truy vấn CSDL trong StoryServlet", e);
        request.setAttribute("message", "Có lỗi xảy ra khi tải dữ liệu.");
        request.setAttribute("stories", Collections.emptyList()); // Đưa list rỗng, chống NPE ở JSP
        request.setAttribute("contentPage", "/WEB-INF/views/common/story/list.jsp");
    }

    // Luôn forward sang LAYOUT WRAPPER, layout sẽ nhúng contentPage vào giữa
    getServletContext()
        .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
        .forward(request, response);
}
```

---

### Khung chuẩn `doPost()` — Nhận form & Redirect (Mô hình PRG: Post-Redirect-Get)

> [!IMPORTANT]
> **Quy tắc vàng PRG:** Khi xử lý form POST thành công, **LUÔN DÙNG `sendRedirect`**, KHÔNG `forward`. Nếu `forward`, người dùng bấm F5 sẽ bị gửi lại dữ liệu lần 2 (trùng lặp đơn, trùng bình luận).

```java
@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {

    // 1. Kiểm tra đăng nhập
    User me = (User) request.getSession().getAttribute("currentUser");
    if (me == null) {
        response.sendRedirect(request.getContextPath() + "/auth?action=login");
        return;
    }

    // 2. Đọc và kiểm tra dữ liệu đầu vào
    String content = request.getParameter("content");
    content = (content == null) ? "" : content.trim();
    int storyId = parseIntOr(request.getParameter("storyId"), 0);

    if (content.isEmpty() || storyId <= 0) {
        // Dữ liệu lỗi: lưu thông báo tạm vào Session rồi redirect lại trang trước
        request.getSession().setAttribute("flashError", "Nội dung bình luận không được rỗng.");
        response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId);
        return;
    }

    // 3. Gọi DAO lưu CSDL
    try {
        Comment comment = new Comment();
        comment.setStoryId(storyId);
        comment.setUserId(me.getId());
        comment.setContent(content);

        commentDAO.insert(comment);

        // Thành công: thông báo và quay lại trang chi tiết truyện
        request.getSession().setAttribute("flashSuccess", "Đã gửi bình luận thành công!");
        response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId);

    } catch (SQLException e) {
        log("Lỗi lưu bình luận", e);
        request.getSession().setAttribute("flashError", "Lỗi hệ thống, vui lòng thử lại sau.");
        response.sendRedirect(request.getContextPath() + "/story?action=detail&id=" + storyId);
    }
}
```

---

## 3. Tầng Giao diện (JSP) — Viết View bằng tay

### Khai báo chuẩn ở đầu file JSP

Mọi file JSP nội dung cần có 3 dòng này ở đầu file:
```jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
```

---

### In dữ liệu ra màn hình: `${...}` vs `<c:out>`

| Tình huống | Dùng cú pháp nào | Ví dụ |
|------------|------------------|-------|
| Dữ liệu người dùng nhập (Tên truyện, bình luận, mô tả) | Bắt buộc `<c:out>` (Chống tấn công XSS) | `<c:out value="${story.title}"/>` |
| Link, ContextPath, Tham số URL hệ thống | Dùng `${...}` | `${pageContext.request.contextPath}/story` |
| Giá trị số, boolean | Dùng `${...}` | `width: ${story.progressPercent}%;` |

---

### Điều kiện rẽ nhánh

#### Dạng 1: Kiểm tra đơn giản `<c:if>`
```jsp
<%-- Kiểm tra danh sách có phần tử hay không --%>
<c:if test="${not empty stories}">
    <p>Tìm thấy ${stories.size()} truyện.</p>
</c:if>

<%-- Kiểm tra điều kiện logic --%>
<c:if test="${currentUser.role eq 'ADMIN'}">
    <a href="${pageContext.request.contextPath}/admin/dashboard">Vào trang Quản trị</a>
</c:if>
```

#### Dạng 2: Rẽ nhánh Nếu — Thì — Ngược lại `<c:choose>`
```jsp
<c:choose>
    <c:when test="${not empty stories}">
        <%-- Có truyện thì vẽ danh sách --%>
        <div class="story-grid">
            ...
        </div>
    </c:when>
    <c:otherwise>
        <%-- Không có thì nhúng trạng thái rỗng --%>
        <%@ include file="/WEB-INF/views/_partials/_empty.jsp" %>
    </c:otherwise>
</c:choose>
```

---

### Duyệt mảng / Danh sách với `<c:forEach>`

```jsp
<div class="story-grid">
    <c:forEach var="story" items="${stories}" varStatus="loop">
        <div class="story-card">
            <%-- Số thứ tự (bắt đầu từ 1) --%>
            <span class="badge">#${loop.count}</span>

            <%-- In ảnh bìa --%>
            <img src="<c:out value="${story.coverUrl}"/>" alt="<c:out value="${story.title}"/>">

            <%-- In tiêu đề an toàn --%>
            <h3>
                <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${story.id}">
                    <c:out value="${story.title}"/>
                </a>
            </h3>

            <%-- In định dạng số (lượt xem) --%>
            <span class="views">
                👁️ <fmt:formatNumber value="${story.views}" type="number"/> lượt xem
            </span>
        </div>
    </c:forEach>
</div>
```

---

### Đưa dữ liệu mảng từ JSP vào JavaScript ở Frontend

Khi cần lấy dữ liệu từ Backend (`request.setAttribute("items", ...)`) bỏ vào **mảng JavaScript** để chạy slider, vẽ biểu đồ, hoặc lọc client:

#### Cách 1: Dùng thuộc tính `data-*` trên thẻ HTML (Chuẩn & an toàn nhất)
```jsp
<div class="chapter-item" 
     data-id="${chapter.id}" 
     data-num="${chapter.chapterNumber}" 
     data-title="<c:out value="${chapter.title}"/>">
</div>

<script>
    // Trong JS: Lấy toàn bộ danh sách bỏ vào mảng Javascript
    const chapterElements = document.querySelectorAll('.chapter-item');
    const chapterArray = Array.from(chapterElements).map(el => ({
        id: parseInt(el.dataset.id),
        num: parseFloat(el.dataset.num),
        title: el.dataset.title
    }));
    console.log("Mảng các chương:", chapterArray);
</script>
```

#### Cách 2: Khởi tạo trực tiếp mảng JavaScript bằng `<c:forEach>`
```jsp
<script>
    // Tạo mảng số hoặc mảng chuỗi trực tiếp từ JSTL
    const popularStoryIds = [
        <c:forEach var="story" items="${popular}" varStatus="status">
            ${story.id}${not status.last ? ',' : ''}
        </c:forEach>
    ];
    console.log("Mảng IDs truyện hot:", popularStoryIds);
</script>
```

#### Cách 3: Gom mảng checkbox người dùng đã tick bằng JavaScript (Batch Action)
```html
<button id="btnDeleteSelected" type="button" class="btn btn-danger">Xoá các mục đã chọn</button>

<script>
    document.getElementById('btnDeleteSelected').addEventListener('click', function() {
        // Gom tất cả checkbox đang được check
        const checkedBoxes = document.querySelectorAll('input[name="selectedIds"]:checked');
        const ids = Array.from(checkedBoxes).map(cb => cb.value);

        if (ids.length === 0) {
            alert('Vui lòng chọn ít nhất 1 mục để thực hiện!');
            return;
        }

        if (confirm('Bạn có chắc muốn xoá ' + ids.length + ' mục này?')) {
            // Gửi mảng ids lên server qua form hoặc Fetch API
            console.log('Mảng ID gửi lên:', ids);
        }
    });
</script>
```

---

### Sử dụng các mảnh tái sử dụng (`_partials`)

Khi cần nhúng các mảnh nhỏ có sẵn trong dự án:

#### Nhúng thẻ truyện (`_card.jsp`):
Vòng lặp đặt biến là `story`, mảnh `_card.jsp` sẽ tự nhận diện biến `story`:
```jsp
<c:forEach var="story" items="${popular}">
    <%@ include file="/WEB-INF/views/_partials/_card.jsp" %>
</c:forEach>
```

#### Nhúng thanh phân trang (`_pagination.jsp`):
Trước khi include, đặt các tham số cần thiết vào `request`:
```jsp
<%-- Cấu hình phân trang --%>
<c:set var="pgBase" value="story?action=list" scope="request"/>
<c:set var="pgQuery" value="&tag=${currentTag}&sort=${sort}" scope="request"/>
<%@ include file="/WEB-INF/views/_partials/_pagination.jsp" %>
```

#### Form gửi POST có bảo vệ CSRF:
Mọi form `method="post"` trong dự án **bắt buộc** phải có thẻ ẩn token `_csrf`:
```jsp
<form action="${pageContext.request.contextPath}/comment" method="post">
    <%-- Token CSRF bắt buộc để không bị CsrfFilter chặn lỗi 403 --%>
    <input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">
    <input type="hidden" name="storyId" value="${story.id}">

    <textarea name="content" placeholder="Viết bình luận..." required></textarea>
    <button type="submit" class="btn btn-primary">Gửi bình luận</button>
</form>
```

---

## 4. Bảng tra nhanh các lỗi phổ biến & Cách khắc phục

| Hiện tượng | Nguyên nhân | Cách sửa |
|------------|-------------|----------|
| **Tiếng Việt bị lỗi ô vuông / ký tự lạ khi gửi form** | `setCharacterEncoding("UTF-8")` không chạy trước khi đọc tham số | Kiểm tra `EncodingFilter` đã ánh xạ `/*` trong `web.xml` |
| **Bấm nút submit bị trả về 403 Forbidden** | Thiếu token `_csrf` trong form | Thêm `<input type="hidden" name="_csrf" value="${sessionScope.csrfToken}">` |
| **Lỗi `NullPointerException` ở trang JSP** | Servlet trả về `null` thay vì `emptyList()`, hoặc model thiếu getter | Trong Servlet: `request.setAttribute("items", Collections.emptyList());` |
| **Bấm F5 trình duyệt hỏi "Confirm Form Resubmission"** | Controller dùng `forward` sau lệnh POST | Đổi thành `response.sendRedirect(...)` |
| **Không tìm thấy file JSP (404)** | Gõ sai đường dẫn trong `setAttribute("contentPage", ...)` | Kiểm tra lại đường dẫn: `/WEB-INF/views/common/...` hoặc `/WEB-INF/views/user/...` |
| **Trang hiển thị rỗng không có thanh nav / footer** | Servlet forward thẳng vào `list.jsp` thay vì `layout/main.jsp` | Đặt `contentPage` trỏ vào `list.jsp`, rồi forward vào `layout/main.jsp` |
