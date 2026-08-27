# 🧱 Quy ước code Java — Servlet / DAO / Model

Áp dụng cho toàn bộ `src/main/java/truyen/`.
Cấu trúc thư mục xem [`../cau-truc.md`](../cau-truc.md).

---

## 1. Bảng tra nhanh đặt tên

| Loại | Quy tắc | Đúng | Sai |
|------|---------|------|-----|
| Package | thường, một từ, số ít | `truyen.dao` | `truyen.DAOs` |
| Model | Danh từ số ít, PascalCase | `Story` | `Stories`, `StoryModel` |
| DAO | `<Model>DAO` | `StoryDAO` | `StoryDao`, `StoryRepository` |
| Servlet | `<Việc>Servlet` | `StoryServlet` | `StoryController`, `Story` |
| Filter | `<Việc>Filter` | `AuthFilter` | `AuthenticationInterceptor` |
| Util | `<Việc>Util` | `PasswordUtil` | `PasswordHelper`, `Utils` |
| Method lấy 1 bản ghi | `findById`, `findBySlug` | `findById(int id)` | `getStory`, `selectOne` |
| Method lấy nhiều | `findAll`, `findLatest` | `findLatest(int limit)` | `getListStory` |
| Method ghi | `insert`, `update`, `delete` | `insert(Story s)` | `save`, `add` |
| Method đếm | `count<Gì>` | `countPublished()` | `getTotal` |
| Hằng số | `UPPER_SNAKE`, `static final` | `MAX_UPLOAD_SIZE` | `maxUploadSize` |
| Biến boolean | `is`/`has` + tính từ | `isCompleted` | `completed`, `flag` |

**Tiếng Anh cho tên code, tiếng Việt cho chú thích.** Đừng trộn
`layDanhSachTruyen()` với `findAll()` trong cùng dự án.

---

## 2. Contract 4 tầng — được và KHÔNG được làm gì

Đây là phần quan trọng nhất của cả file. Vi phạm một dòng ở đây là dự án bắt
đầu rối.

### `model/` — JavaBean thuần

| ✅ Được | ❌ Không được |
|---------|---------------|
| Field private + get/set | Bất kỳ câu SQL nào |
| Constructor rỗng + constructor đủ tham số | `import java.sql.*` |
| `implements Serializable` | `import javax.servlet.*` |
| Method tính toán đơn giản từ field có sẵn (`isCompleted()`, `getInitial()`) | Gọi DAO |

### `dao/` — chỉ nói chuyện với database

| ✅ Được | ❌ Không được |
|---------|---------------|
| `PreparedStatement`, `ResultSet` | `import javax.servlet.*` |
| Trả về Model hoặc `List<Model>` | `forward()`, `sendRedirect()` |
| **Ném** `SQLException` lên trên | **Nuốt** exception (`catch {}` rỗng) |
| Nhận tham số thường (`int`, `String`) | Nhận `HttpServletRequest` |

> DAO không biết gì về web. Phép thử: lớp này phải chạy được trong một
> `main()` bình thường, không cần server.

### `controller/` — chỉ điều phối

| ✅ Được | ❌ Không được |
|---------|---------------|
| `getParameter`, `setAttribute` | Viết câu SQL |
| Gọi DAO, kiểm tra dữ liệu, phân quyền | Sinh HTML (`out.println("<div>")`) |
| `forward` đúng **một lần** ở cuối method | `forward` ở giữa rồi chạy tiếp |
| `log(msg, e)` khi có lỗi | Để exception bay lên trình duyệt |

### `views/` — chỉ hiển thị

| ✅ Được | ❌ Không được |
|---------|---------------|
| EL: `${story.title}` | Scriptlet `<% ... %>` |
| JSTL: `<c:if>`, `<c:forEach>`, `<c:out>` | `import` lớp Java |
| Đọc attribute | Gọi DAO, truy vấn database |

**Phép thử tổng:** đổi MySQL sang PostgreSQL thì **chỉ** phải sửa trong `dao/`.
Nếu phải sửa chỗ khác, tầng đã bị rò.

---

## 3. Khuôn servlet chuẩn

Mọi servlet trong dự án viết theo đúng khuôn này. Chép khuôn, đổi nội dung.

```java
@WebServlet("/story")
public class StoryServlet extends HttpServlet {

    private StoryDAO storyDAO;

    // init() chạy MỘT lần — chỗ tạo DAO, không tạo trong doGet
    @Override
    public void init() throws ServletException {
        storyDAO = new StoryDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. LUÔN đặt encoding trước mọi getParameter()
        request.setCharacterEncoding("UTF-8");

        // 2. Đọc action, có giá trị mặc định
        String action = request.getParameter("action");
        if (action == null) action = "list";

        // 3. Rẽ nhánh — mỗi nhánh CHỈ gán url + setAttribute, KHÔNG tự forward
        String url;
        try {
            switch (action) {
                case "detail": url = detail(request); break;
                case "create": url = create(request); break;
                default:       url = list(request);   break;
            }
        } catch (SQLException e) {
            log("StoryServlet: lỗi truy vấn", e);   // stack trace vào log
            request.setAttribute("message", "Không tải được dữ liệu.");
            url = "/WEB-INF/views/page/_error.jsp";
        }

        // 4. Forward ĐÚNG MỘT LẦN, ở cuối
        request.setAttribute("contentPage", url);
        getServletContext()
                .getRequestDispatcher("/WEB-INF/views/layout/main.jsp")
                .forward(request, response);
    }

    // Mỗi action một method private, trả về đường dẫn mảnh nội dung
    private String list(HttpServletRequest request) throws SQLException {
        request.setAttribute("stories", storyDAO.findLatest(24));
        return "/WEB-INF/views/story/list.jsp";
    }
}
```

**Bốn luật rút ra:**

1. `setCharacterEncoding("UTF-8")` **trước** `getParameter()` đầu tiên — sau đó
   là vô tác dụng, và tiếng Việt hỏng mà không báo lỗi.
2. Mỗi action một method private → `doGet` luôn ngắn, đọc là hiểu luồng.
3. Nhánh chỉ **gán** `url`, không tự forward → chắc chắn forward đúng 1 lần.
4. `switch` phải có `default` → URL bịa không làm sập trang.

---

## 4. Kiểm tra quyền — luật bắt buộc

Kiểm tra **vai trò** là chưa đủ. Phải kiểm tra **quyền sở hữu**.

```java
// ❌ SAI — user nào đăng nhập cũng sửa được truyện của người khác
if (currentUser != null) { storyDAO.update(story); }

// ✅ ĐÚNG — phải là chủ truyện, hoặc admin
Story story = storyDAO.findById(id);
if (story == null) {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);      // 404
    return;
}
boolean laChu   = story.getAuthorId() == currentUser.getId();
boolean laAdmin = "ADMIN".equals(currentUser.getRole());
if (!laChu && !laAdmin) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);      // 403
    return;
}
storyDAO.update(story);
```

> Không có đoạn này thì chỉ cần sửa `?id=5` thành `?id=6` trên thanh địa chỉ là
> sửa được truyện người khác. Đây là lỗi bảo mật hay gặp nhất ở đồ án sinh viên.

**Dùng `sendError()` chứ không phải `setStatus()`** — chỉ `sendError` mới kích
hoạt `<error-page>` trong `web.xml`.

---

## 5. Bảng tra URL — toàn bộ đường dẫn của app

Đặt tên URL lệch nhau là nguồn bug khó chịu. Bảng này là bản chốt.

| URL | Servlet | Action | Ai vào được |
|-----|---------|--------|-------------|
| `/` | `HomeServlet` | — | tất cả |
| `/story?action=list` | `StoryServlet` | `list` | tất cả |
| `/story?action=detail&id=` | | `detail` | tất cả |
| `/story?action=create` | | `create` | thành viên |
| `/story?action=edit&id=` | | `edit` | **chủ truyện** / admin |
| `/story?action=delete&id=` | | `delete` | **chủ truyện** / admin |
| `/chapter?action=read&id=` | `ChapterServlet` | `read` | tất cả |
| `/chapter?action=create&storyId=` | | `create` | **chủ truyện** |
| `/chapter?action=edit&id=` | | `edit` | **chủ truyện** |
| `/comment?action=add` | `CommentServlet` | `add` | thành viên |
| `/comment?action=delete&id=` | | `delete` | **người viết** / admin |
| `/bookmark?action=add&storyId=` | `BookmarkServlet` | `add` | thành viên |
| `/bookmark?action=list` | | `list` | thành viên |
| `/download?storyId=` | `DownloadServlet` | — | tất cả |
| `/auth?action=login` | `AuthServlet` | `login` | khách |
| `/auth?action=register` | | `register` | khách |
| `/auth?action=logout` | | `logout` | thành viên |
| `/page?name=guide` | `PageServlet` | — | tất cả |
| `/page?name=rules` | | — | tất cả |
| `/admin/story` | `AdminStoryServlet` | | **admin** |
| `/admin/user` | `AdminUserServlet` | | **admin** |

**Luật đặt URL:**

- Danh từ **số ít**, thường: `/story` không phải `/stories` hay `/Story`
- Thao tác nằm ở `?action=`, **không** ở đường dẫn: `/story?action=edit`
  chứ không phải `/story/edit`
- Khu quản trị bắt đầu bằng `/admin/` → `AdminFilter` chặn bằng `/admin/*`
- Tham số id: `id` cho chính thực thể đó, `<tên>Id` cho thực thể khác
  (`?id=5` là chương số 5, `?storyId=5` là chương của truyện 5)

---

## 6. Xử lý lỗi và ghi log

| Tình huống | Làm gì |
|------------|--------|
| Lỗi truy vấn database | `log("<Servlet>: mô tả", e)` + hiện thông báo tử tế |
| Không tìm thấy bản ghi | `response.sendError(404)` |
| Không đủ quyền | `response.sendError(403)` |
| Dữ liệu nhập sai | đặt `message`, forward **về lại form**, giữ chữ đã gõ |

```java
log("StoryServlet: không lưu được truyện id=" + id, e);
```

Luôn kèm **tên servlet** và **biến liên quan** — không thì log đầy dòng vô nghĩa.

**Không bao giờ** `catch (Exception e) {}` rỗng. Lỗi biến mất không dấu vết là
cách chắc chắn nhất để mất nguyên buổi đi tìm một bug lẽ ra đã tự khai báo.

---

## 7. Ba thứ tuyệt đối tránh

| Đừng | Vì sao |
|------|--------|
| Biến instance có thể **thay đổi** trong servlet | Tomcat tạo **một** instance dùng cho mọi thread → mất dữ liệu ngầm, không có exception. (DAO làm field thì **được**, vì nó không có trạng thái bị ghi) |
| Nối chuỗi SQL: `"... WHERE id=" + id` | SQL injection. Luôn dùng `?` + `setInt/setString` |
| `out.println("<div>...")` trong servlet | HTML thuộc về JSP. Trừ `DownloadServlet` — nó trả `.txt`, không trả HTML |
