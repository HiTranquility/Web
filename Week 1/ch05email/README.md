# Chapter 5 — How to develop servlets

Toàn bộ code trong 58 slide của *Murach's Java Servlets and JSP* (3rd Ed.) chương 5,
viết thành **code chạy được, có chú thích tiếng Việt ngay trên từng đoạn**: dùng thế
nào, tại sao dùng như vậy, và chỗ nào dễ sai.

**Đọc code trước, chạy sau.** Mỗi file `.java` mở đầu bằng một khối chú thích nêu
VẤN ĐỀ mà case đó giải quyết, CÁCH DÙNG theo slide, và TẠI SAO lại làm như thế. Bên
trong thân method là chú thích cho từng dòng đáng chú ý. `web.xml` cũng được chú thích
từng thẻ. Các file `.jsp` có khối `<%-- --%>` giải thích phần cú pháp JSP/EL.

16 trang web đi kèm không phải phần chính — chúng chỉ để **chạy thử và nhìn thấy kết
quả** của đoạn code vừa đọc (ví dụ: CASE 15 bấm nút là thấy mất ~750.000 lượt tăng).

Nội dung slide đã bóc ra: [../docs/chapter05-notes.md](../docs/chapter05-notes.md).

## Đọc theo thứ tự nào

| Đọc | File | Vì sao đọc trước |
|-----|------|------------------|
| 1 | `murach/business/User.java` | JavaBean — 3 điều kiện, thiếu cái nào hỏng cái gì |
| 2 | `murach/data/UserIO.java` | tầng data access, vì sao tách ra lớp riêng |
| 3 | `murach/email/EmailListServlet.java` | servlet chính, gom 4 case lại một chỗ |
| 4 | `WEB-INF/web.xml` | mapping, init-param, error-page — chú thích từng thẻ |
| 5 | `murach/demo/*.java` | 13 case còn lại, mỗi file một chủ đề độc lập |

## Running it

```bash
powershell -ExecutionPolicy Bypass -File run.ps1 -Port 8081
```

Then open <http://localhost:8081/ch05email/> — the index lists all 16 cases.

Port 8081 keeps it clear of `ch02email`, which defaults to 8080. Other ways to run it
(IntelliJ/NetBeans with Tomcat 9, or a WAR into a real Tomcat) are identical to
chapter 2 — see [../ch02email/README.md](../ch02email/README.md).

> Tomcat **9**, not 10+. This is `javax.servlet`, not `jakarta.servlet`.

## The 16 cases

| # | Case | Slides | What the page proves |
|---|------|--------|----------------------|
| 01 | A servlet that returns HTML | 4–6 | one servlet answers GET and POST, page built by `PrintWriter` |
| 02 | Servlet mapping & URL patterns | 7–9 | `getServletPath()` vs `getPathInfo()` for `/mapping` and `/email/*` |
| 03 | The `@WebServlet` annotation | 10–11 | a servlet mapped with zero lines in `web.xml` |
| 04 | The HTTP GET method | 12–13 | all three ways to build a query string arrive identically; POST to a doGet-only servlet returns 405 |
| 05 | GET versus POST | 14–15 | the same value in the URL vs in the body |
| 06 | `getParameter` / `getParameterValues` | 16–17 | an unchecked box and an empty list both return **null**, not "off" and not `[]` |
| 07 | `ServletContext.getRealPath` | 18–20 | the real path on *your* machine; `/WEB-INF` is unreachable by URL |
| 08 | Request attributes | 21–22 | the cast, and that attributes really are gone on the next request |
| 09 | Forwarding a request | 23–24 | forward to HTML / JSP / servlet, attribute survives, address bar doesn't change |
| 10 | Redirecting a response | 25–26 | a real 302, the attribute lost, and a forward-vs-redirect table |
| 11 | Server-side data validation | 27–31 | the chapter's app: `${message}` and text boxes that keep what you typed |
| 12 | Initialization parameters | 35–40 | `context-param` vs `init-param`, and what the wrong scope returns |
| 13 | Custom error handling | 41–45 | a custom 404 page and three different exceptions caught by one `<error-page>` |
| 14 | The servlet lifecycle | 46–47 | a timestamped log of `init` / `service` / `doGet` / `doPost` as they happen |
| 15 | Instance variables aren't thread-safe | 48–50 | a stress test that loses ~700,000 of 1,000,000 increments |
| 16 | Console and log-file debugging | 52–58 | `System.out.println`, `log()`, and `log()` with a stack trace, all three visible |

## Layout

```
ch05email/src/main/
├── java/murach/
│   ├── business/User.java                 the JavaBean
│   ├── data/UserIO.java                   writes to the file named by the init-param
│   ├── email/
│   │   ├── TestServlet.java               case 01
│   │   └── EmailListServlet.java          case 11 (+ 07, 12, 16)
│   └── demo/                              one servlet per remaining case
└── webapp/
    ├── index.jsp                          the case index
    ├── join.jsp, thanks.jsp               case 11
    ├── error_404.jsp, error_java.jsp      case 13
    ├── demo/case01.jsp … case16.jsp       one page per case
    ├── styles/main.css
    └── WEB-INF/
        ├── web.xml                        mappings, init params, error pages
        └── jspf/header.jspf, footer.jspf  shared page chrome
```

Mappings are deliberately split: `web.xml` for the servlets whose configuration lives
there anyway (`EmailListServlet`, `InitParamServlet`) or that case 02 is about, and
`@WebServlet` for the rest. Both mechanisms run in the same application on purpose.

## Where things get written

- **`src/main/webapp/WEB-INF/EmailList.txt`** — the email list from case 11. The path
  comes from the `relativePathToFile` init parameter, resolved by `getRealPath()`. In
  a deployed WAR it lands inside the exploded webapp instead.
- **The terminal you started the server in** — case 16's console and log output.

## Differences from the slides

1. **`join.jsp`, not `index.jsp`.** The slides call the join page `index.jsp`; here
   `index.jsp` is the case index, so the join page is `join.jsp`. `EmailListServlet`
   forwards there instead. Nothing else about case 11 changes.
2. **`@WebInitParam`, not `@InitParam`.** Slide 37 abbreviates the annotation name;
   the real one is `@WebInitParam`. Noted on the case 12 page.
3. **`UserIO` is not shown in the slides** — only called (slide 56). The file-based
   implementation here is mine, matching the `add(user, path)` signature the slide uses.
4. **UTF-8 throughout**, as in chapter 2 — the book's code defaults to ISO-8859-1 and
   mangles non-ASCII names.
5. **`LifecycleServlet` overrides `service()`**, which slide 47 tells you not to do.
   It is the only way to *show* the call order, it calls `super.service(...)`, and the
   page says so in a warning box.
