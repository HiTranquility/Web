# 🐞 bug-002: `web.xml` khai hai lần cùng một `<error-page>` — hai hệ trang lỗi cùng tồn tại

> **Đích:** `docs/projects/bugs/bug-002-web-xml-khai-trung-error-page/bug.md` — bug là **thư mục**. · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | bug-002 |
| **Mức** | 🟡 Nhẹ |
| **Feature liên quan** | [`ISSUE-016`](../../issues/ISSUE-016-trang-loi-404-500/issue.md) |
| **Người sửa** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | ✅ Đã sửa |
| **Ngày phát hiện** | 2026-09-23 |
| **Nơi xảy ra** | `src/main/webapp/WEB-INF/web.xml` dòng 160–179 **và** 219–229 · `views/common/page/error404.jsp` · `views/error/404.jsp` |

---

## 1. Tái hiện thế nào

Đây là lỗi **đọc file mới thấy**, không phải lỗi bấm ra được — trên máy dev nó đang chạy
đúng. Cách tái hiện là mở file:

1. Mở `src/main/webapp/WEB-INF/web.xml`
2. Tìm `<error-code>404</error-code>` — **ra hai kết quả**, dòng 161 và dòng 220
3. Tìm `<exception-type>java.lang.Throwable</exception-type>` — cũng **ra hai kết quả**,
   dòng 177 và dòng 227

```xml
<!-- dòng 160: bộ CŨ, từ CASE 11 -->
<error-page>
    <error-code>404</error-code>
    <location>/WEB-INF/views/common/page/error404.jsp</location>
</error-page>
...
<!-- dòng 219: bộ MỚI, ISSUE-016 thêm vào, KHÔNG xoá bộ cũ -->
<error-page>
    <error-code>404</error-code>
    <location>/error?code=404</location>
</error-page>
```

**Thấy:** hai khai báo cùng mã lỗi, trỏ hai chỗ khác nhau. Cả hai bộ JSP đều còn trong
`views/` — `common/page/error404.jsp` (cũ) và `error/404.jsp` (mới).

**Đáng ra phải:** một mã lỗi khai đúng một lần.

> **Hiện tại nó vẫn chạy đúng — đã kiểm bằng trình duyệt thật.** Mở
> `http://localhost:8080/khong-co-trang-nay-dau` thì ra trang 404 **mới** của ISSUE-016
> (chữ "404" cỡ lớn, hai nút *Quay về Trang chủ* / *Khám phá Kho truyện*). Tomcat nhúng
> đang chọn khai báo **sau**. Đó chính là chỗ đáng lo: nó chạy đúng **do may**, không do
> mình chỉ định.

---

## 2. Chứng cứ

```bash
$ grep -c "<error-code>404</error-code>" src/main/webapp/WEB-INF/web.xml
2
$ grep -c "<exception-type>java.lang.Throwable</exception-type>" src/main/webapp/WEB-INF/web.xml
2
```

Cả hai bộ JSP đều còn nằm trong cây view:

```text
views/common/page/error404.jsp   ← bộ cũ, còn được web.xml dòng 162 trỏ tới
views/common/page/_error404.jsp
views/common/page/error403.jsp   ← 403 CHỈ có bộ cũ, ISSUE-016 không làm 403
views/common/page/_error403.jsp
views/common/page/error500.jsp
views/common/page/_error500.jsp
views/error/404.jsp              ← bộ mới của ISSUE-016
views/error/500.jsp
```

---

## 3. Nguyên nhân

- **Sai ở:** `web.xml:219–229`
- **Vì:** ISSUE-016 **thêm** bộ trang lỗi mới nhưng **không gỡ** bộ cũ. Không ai phát hiện
  vì kết quả nhìn bằng mắt vẫn đúng.

Vì sao nó vẫn chạy: đặc tả Servlet 3.1 §14.4 nói khai trùng `error-code` là **mô tả triển
khai không hợp lệ**, nhưng không bắt container phải làm gì. Tomcat chọn cách dễ tính —
ghi cảnh báo rồi dùng khai báo cuối. Container khác, hoặc bản Tomcat khác, được phép
**từ chối triển khai**.

Đây là rủi ro thật với đồ án này chứ không phải lo xa: máy dev chạy **Tomcat nhúng** qua
`tools/DevServer.java`, còn `README.md` bảo chạy trên **Tomcat 9 cài rời**. Hai môi
trường khác nhau, và môi trường lúc demo trước lớp là môi trường chưa ai thử.

---

## 4. Sửa thế nào

*(Đã làm xong ngày 2026-09-24. Bốn thay đổi, theo đúng thứ tự này — thứ tự quan trọng:
thêm 403 vào cửa mới **trước**, rồi mới xoá cửa cũ, để không có lúc nào 403 mất chỗ dựa.)*

- **Đổi 1 — thêm 403 vào `ErrorServlet` trước tiên.** Tạo `views/error/403.jsp`, nội dung
  lấy nguyên từ `_error403.jsp` cũ. Dùng lại các lớp `.error-hero-*` đã có trong
  `components.css`, **không** viết style nội tuyến như `error/404.jsp` đang làm — để đổi
  giao diện trang lỗi chỉ phải sửa một chỗ.
- **Đổi 2 — `ErrorServlet` trả đúng mã HTTP.** Thêm `resp.setStatus(statusCode)` trước
  `forward`. Trước đó `/error?code=404` gõ tay trả **200 OK** kèm một trang viết "404" —
  Google đọc header chứ không đọc chữ, nên đó là *soft 404*: máy tìm kiếm sẽ đi lập chỉ
  mục cho trang lỗi. Đây là lỗi thứ hai, tìm ra trong lúc sửa lỗi thứ nhất.
- **Đổi 3 — gỡ khối `<error-page>` cũ** ở `web.xml:160–179`, và khai lại đủ bốn mục
  *(403 · 404 · 500 · `Throwable`)* ở một chỗ duy nhất, mỗi mã đúng một lần.
- **Đổi 4 — xoá 6 JSP chết** sau khi `grep -rn "common/page/error" src/` xác nhận không
  còn ai trỏ tới: `error403/404/500.jsp` và `_error403/404/500.jsp`.

**Vì sao giữ 403 chứ không xoá luôn:** ISSUE-016 chỉ làm 404 và 500, nên 403 là **lý do
duy nhất** khối cũ còn tồn tại. Xoá thẳng là mất trang 403 mà
[`CHECKLIST.md §1`](../../../guides/CHECKLIST.md) có dặn kiểm *("vào `/admin/user` bằng
tài khoản thường → 403")*.

**Vì sao không để nguyên cho lành:** để nguyên nghĩa là mỗi lần sửa trang lỗi phải nhớ
*"đang sửa bộ nào trong hai bộ"* — và nhớ nhầm thì sửa xong chạy thử vẫn thấy y như cũ,
mất cả buổi mới hiểu tại sao. Đây đúng là thứ mà
[`CHECKLIST.md §2`](../../../guides/CHECKLIST.md) gọi là *"xoá code chết"*.

**Dữ liệu hỏng:** không có. Đây là lỗi cấu hình, không đụng CSDL.

---

## 5. Kiểm lại

- [x] `grep -c "<error-code>404" web.xml` → **1** *(403, 500 cũng mỗi cái 1)*
- [x] `grep -c "java.lang.Throwable" web.xml` → **1**
- [x] Mở URL bịa → ra trang 404 **mới**, **giống hệt trước khi sửa** — đây là điểm mấu
      chốt: sửa xong người dùng không được thấy gì khác
- [x] Vào `/admin/dashboard` bằng tài khoản `mocmien` → **ra trang 403 mới**, giữ nguyên
      thanh menu, hiện nút *"Đổi tài khoản"* vì đang đăng nhập
- [x] **Mã HTTP đúng ở header**, không chỉ đúng trên màn hình:

      | URL | Mã trả về |
      |---|:--:|
      | `/khong-co-trang-nay` | 404 |
      | `/error?code=404` | 404 *(trước khi sửa: **200**)* |
      | `/error?code=403` | 403 |
      | `/` | 200 |

- [x] `scripts\test.ps1` — **101 pass / 0 fail** *(97 cũ + 4 test mới trong
      `ErrorServletTest`: ca 403, ca mã HTTP, ca thiếu mã, ca mã rác)*
- [ ] **Còn nợ:** chạy thử trên **Tomcat 9 cài rời**, không chỉ Tomcat nhúng. Đây là điều
      kiện duy nhất chứng minh dứt điểm — nhưng nó cũng là thứ *không thể sai* sau khi đã
      hết khai trùng, nên không chặn việc đóng bug.

---

## 6. Ghi chú

Bug này thuộc loại *"doc và code nói hai chuyện"* — cùng họ với hai chỗ khác phát hiện
trong cùng đợt rà soát ngày 2026-09-23, chưa mở bug riêng vì không phải lỗi chạy sai:

| Chỗ | Nói gì | Thực tế |
|---|---|---|
| [`ISSUE-011`](../../issues/ISSUE-011-tinh-gon-css/issue.md) ✅ *"Tinh gọn CSS"* | cắt `components.css` 79 KB | File **đã tăng lên 88,6 KB** và vẫn nạp trên **mọi** trang. Review chỉ mô tả lại cấu trúc 4 tầng vốn có từ CASE 00, không đo lại kích thước. Mục tiêu gốc *chưa đạt*. |
| [`ISSUE-012`](../../issues/ISSUE-012-seo-sitemap-opengraph/issue.md) ✅ nhắc `canonical` | có thẻ canonical | `grep -rn "canonical" src/main/webapp/WEB-INF/views/` → **không ra gì**. `og:url` cũng thiếu. |

Cả hai nên vào một ISSUE dọn dẹp riêng, không sửa lén ở đây.
