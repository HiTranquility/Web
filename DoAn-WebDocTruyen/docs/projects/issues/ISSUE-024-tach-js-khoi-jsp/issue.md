# 🏷️ ISSUE-024: Tách JavaScript ra khỏi JSP — 188 dòng script gửi lại ở mọi trang

> **Đích:** `docs/projects/issues/ISSUE-024-tach-js-khoi-jsp/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-024 |
| **Người làm** | N3 — Giao diện & Hiệu năng |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày mở** | 2026-09-23 |
| **CASE liên quan** | Tối ưu hiệu năng frontend |
| **Đụng vào** | `views/layout/parts/nav.jsp` · `views/layout/parts/head.jsp` · `assets/js/nav.js` *(mới)* · `views/common/story/detail.jsp` · `views/common/chapter/read.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

`parts/nav.jsp` mang **188 dòng `<script>` nội tuyến**: nút đổi nền sáng/tối, menu thả
xuống của người dùng, và toàn bộ live search autocomplete. `nav.jsp` nằm trong layout
`main` và layout `admin`, tức là **gần như mọi trang**.

- **Hiện tại:** mỗi lần tải bất kỳ trang nào, ~6 KB JavaScript **y hệt nhau** lại được gửi
  xuống lần nữa. Trình duyệt **không cache được một dòng nào** — script nội tuyến là một
  phần của HTML, mà HTML thì không cache. Đi 10 trang là tải lại 10 lần cùng một đoạn mã.
- **Sau khi xong:** một file `assets/js/nav.js`, tải **một lần**, cache từ đó về sau.

Còn ba cái lợi nữa, không phải về tốc độ:

| Lợi | Cụ thể |
|---|---|
| **Sửa được tử tế** | Trong `.js` thì trình soạn thảo báo lỗi cú pháp và tự động định dạng. Trong `<script>` giữa JSP thì nó chỉ là chuỗi ký tự — gõ sai một dấu ngoặc là **trang trắng lúc chạy**, không phải lỗi lúc biên dịch. |
| **Hết trộn hai ngôn ngữ** | Script hiện đang nhúng `${pageContext.request.contextPath}` **ngay giữa** chuỗi JavaScript. JSP chạy ở máy chủ, JS chạy ở trình duyệt — trộn vào nhau là chỗ sinh lỗi kỳ quặc nhất của cả dự án. |
| **Mở đường cho CSP** | Muốn đặt `Content-Security-Policy` chặn XSS thì phải bỏ được `'unsafe-inline'`. Còn script nội tuyến thì không bao giờ bỏ được. |

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] `grep -c "<script>" views/layout/parts/nav.jsp` → **0**.
- [ ] `assets/js/nav.js` tồn tại, nạp từ `head.jsp` với thuộc tính **`defer`**.
- [ ] Tab Network: `nav.js` tải ở lần đầu, các lần sau là **`304`** hoặc **`from disk cache`**.
- [ ] Tổng byte HTML của trang chủ **giảm ít nhất 5 KB**. Ghi số thật: trước `___` → sau `___`.
- [ ] **Bốn chức năng chạy y như cũ**, kiểm bằng tay từng cái:
  1. Nút 🌙 đổi nền sáng/tối, **và nhớ lựa chọn** sau khi tải lại trang
  2. Menu người dùng mở/đóng, bấm ra ngoài thì đóng, **Esc** cũng đóng
  3. Live search: gõ 2 ký tự hiện gợi ý, **↑ ↓** di chuyển, **Enter** chọn, **Esc** đóng
  4. Chuông thông báo hiện đúng số chưa đọc
- [ ] **Không nháy sáng/tối (FOUC)** khi tải trang ở chế độ sáng — đoạn script chống FOUC
      ở `head.jsp` **phải giữ nguyên nội tuyến** *(§3.2)*.
- [ ] Console trình duyệt **không có lỗi đỏ** ở cả 6 trang chính.
- [ ] Chạy được khi deploy dưới context path khác `/` *(§3.1)*.
- [ ] `scripts\test.ps1` — không tụt so với 101 pass.

---

## 3. Các bước

| # | Bước | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Đo baseline: byte HTML của 3 trang, và quay màn hình 4 chức năng đang chạy *(§5)* | *(không sửa gì)* | ☐ |
| 2 | Chuyển `contextPath` từ JSP sang `data-` attribute trên `<body>` *(§3.1)* | `layout/main.jsp` · `layout/admin.jsp` | ☐ |
| 3 | Cắt nguyên khối script sang `assets/js/nav.js`, đọc contextPath từ `data-` | `nav.jsp` → `assets/js/nav.js` | ☐ |
| 4 | Nạp `nav.js` với `defer` trong `head.jsp` | `head.jsp` | ☐ |
| 5 | **Giữ nguyên** script chống FOUC nội tuyến ở `head.jsp` *(§3.2)* | `head.jsp` | ☐ |
| 6 | Làm tương tự với script nội tuyến ở `detail.jsp` và `read.jsp` nếu có | `detail.jsp` · `read.jsp` | ☐ |
| 7 | Đo lại, điền cột "sau" | *(không sửa gì)* | ☐ |

### 3.1 Truyền `contextPath` sang JS thế nào cho đúng

Đây là chỗ duy nhất thật sự khó của việc này. Script hiện đang làm thế này:

```javascript
fetch('${pageContext.request.contextPath}/story?action=suggest&q=' + ...)
```

Cắt nguyên si sang `.js` thì `${...}` thành chuỗi ký tự — **live search chết ngay**, và
chết im lặng: không lỗi biên dịch, chỉ là fetch tới một URL vô nghĩa.

Cách đúng — để JSP ghi giá trị ra một chỗ mà JS đọc được:

```jsp
<%-- layout/main.jsp --%>
<body data-ctx="${pageContext.request.contextPath}">
```

```javascript
// assets/js/nav.js — đọc một lần ở đầu file
const CTX = document.body.dataset.ctx || '';
fetch(CTX + '/story?action=suggest&q=' + encodeURIComponent(q))
```

> **Vì sao không hardcode `''` cho gọn.** Vì lúc deploy lên Tomcat thật, ứng dụng rất có
> thể nằm dưới `/webdoctruyen` chứ không phải `/`. Hardcode thì trên máy dev chạy ngon, lên
> Tomcat là mọi lời gọi fetch **404** — và đó là lúc demo. Chính `README.md` đã nêu nguyên
> tắc này: *"contextPath: đổi tên lúc deploy vẫn chạy, không cần sửa link"*.

### 3.2 Một đoạn script PHẢI ở lại nội tuyến

`head.jsp` có đoạn chống nháy sáng/tối:

```javascript
var theme = localStorage.getItem('site_theme');
if (theme === 'light') document.documentElement.setAttribute('data-site-theme', 'light');
```

> **[NEVER] chuyển đoạn này ra file ngoài.** Nó phải chạy **trước khi trình duyệt vẽ pixel
> đầu tiên**. File ngoài — kể cả không có `defer` — vẫn phải chờ một vòng tải mạng, và
> trong vòng đó người dùng đã nhìn thấy nền tối loé lên rồi mới chuyển sang sáng. Đúng cái
> FOUC mà đoạn script này sinh ra để diệt.
>
> Đây là ngoại lệ **có lý do**, không phải chỗ bỏ sót. Ghi hẳn comment
> `// PHẢI nội tuyến — xem ISSUE-024 §3.2` vào `head.jsp`, không thì người sau sẽ "dọn nốt
> cho sạch" và FOUC quay lại.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên file trong `assets/js/`, thứ tự nạp | [`02-VIEW §4 §5`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Commit message | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Luật riêng:**

> **Đây là refactor thuần: đầu vào đổi, kết quả nhìn phải KHÔNG đổi một chút nào.**
> Cám dỗ lớn nhất khi cắt dán 188 dòng là "tiện tay viết lại cho đẹp". Đừng. Cắt nguyên
> si trước, chạy đủ 4 chức năng ở §2, commit. Muốn sửa gì thì **commit riêng sau đó** —
> không thì lúc live search hỏng sẽ không biết tại cắt dán hay tại viết lại.

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅.*

| Đo | Trước | Sau |
|---|---|---|
| Byte HTML — trang chủ | | |
| Byte HTML — trang chi tiết | | |
| Byte HTML — trang đọc chương | | |
| `nav.js` lần tải thứ hai | — | *(phải là 304 / disk cache)* |
| `grep -c "<script>" nav.jsp` | | *(phải là 0)* |
| `scripts\test.ps1` | 101 pass / 0 fail | |

- **Bấm thử — bốn chức năng ở §2:**
- **Thử trường hợp xấu:** tắt JS trong trình duyệt *(trang vẫn đọc được chứ?)* · deploy
  dưới context path `/webdoctruyen` · nền sáng tải lại trang xem có nháy không

---

## 6. Ghi chú

**Việc này không thêm tính năng nào cho người dùng.** Nó đổi lấy hai thứ: trang nhẹ hơn
vài KB, và 188 dòng mã chuyển từ chỗ **không sửa được tử tế** sang chỗ sửa được. Ưu tiên 🔵,
làm sau các việc 🟢 trong đợt 2.

**Đừng gộp với [`ISSUE-019`](../ISSUE-019-don-no-dot-1/issue.md).** Cả hai đều của N3 và
đều là dọn dẹp, nhưng một cái đụng CSS còn một cái đụng JS. Trộn vào thì lúc trang vỡ
không biết tại file nào — mà cả hai loại hỏng này đều **không có thông báo lỗi**: build
vẫn sạch, test vẫn pass, chỉ có trang là sai.

**Thứ cố tình không làm:**

| Bỏ | Vì |
|---|---|
| Gộp hết JS thành một bundle | Cần công cụ build. Đồ án này cố ý **không có bước build** cho frontend — xem lý lẽ ở [`README.md`](../../../../README.md). |
| Rút gọn (minify) | Cùng lý do. Vài KB không đáng đổi lấy một chuỗi công cụ. |
| Viết lại bằng framework | Xem *"Chuyển JSP sang React"* trong bảng **Bỏ** ở [`issues/README`](../README.md). |
| Đặt `Content-Security-Policy` luôn | Việc này chỉ **mở đường**. Bật CSP là một việc riêng, phải rà mọi `onclick=` còn sót trong JSP trước. |
