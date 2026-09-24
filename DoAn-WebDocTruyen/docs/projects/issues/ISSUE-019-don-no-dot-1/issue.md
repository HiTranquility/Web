# 🏷️ ISSUE-019: Dọn nợ đợt 1 — ba chỗ doc báo xong mà code chưa xong

> **Đích:** `docs/projects/issues/ISSUE-019-don-no-dot-1/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-019 |
| **Người làm** | N3 — Giao diện & Hiệu năng |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày mở** | 2026-09-23 |
| **CASE liên quan** | Dọn nợ kỹ thuật sau đợt 1 (ISSUE-001…018) |
| **Đụng vào** | `assets/css/components.css` · `views/layout/parts/head.jsp` · `controller/common/StoryServlet.java` *(chỉ đặt attribute)* |

---

## 1. Làm cái gì, và vì sao (Goal)

Rà soát ngày 2026-09-23 đối chiếu **từng lời hứa trong doc** với **file thật**. Ba chỗ
lệch. Không chỗ nào làm web chạy sai — nên không mở bug — nhưng để nguyên thì doc nói dối,
và [luật số 3 ở `INDEX.md`](../../../INDEX.md) nói doc sai còn tệ hơn không có doc.

- **Hiện tại:**
  1. [`ISSUE-011`](../ISSUE-011-tinh-gon-css/issue.md) ✅ *"Tinh gọn CSS"* — mục tiêu gốc là
     cắt `components.css` 79 KB vì *"trang đăng nhập cũng phải tải cả CSS của lưới truyện"*.
     File hiện **88,6 KB**, tức **phình thêm ~10 KB**, và vẫn nạp vô điều kiện ở
     `head.jsp:50` trên **mọi** trang. Review chỉ mô tả lại cấu trúc 4 tầng vốn đã có từ
     CASE 00 và không đo lại kích thước — nên không ai thấy.
  2. [`ISSUE-012`](../ISSUE-012-seo-sitemap-opengraph/issue.md) ✅ có nhắc `canonical`.
     `grep -rn "canonical" src/main/webapp/WEB-INF/views/` → **không ra gì**. `og:url` cũng
     thiếu, trong khi `og:title`, `og:description`, `og:image` đều có.
  3. `.error-hero-card` được khai **hai lần** trong `components.css` (dòng 2220 và 2415) —
     dấu hiệu của việc dán thêm mà không tìm xem đã có chưa. Đây chính là cơ chế làm file
     phình ở mục 1.
- **Sau khi xong:** ba con số đo được, không phải ba câu mô tả.

> [`bug-002`](../../bugs/bug-002-web-xml-khai-trung-error-page/bug.md) *(khai trùng
> `<error-page>`)* phát hiện cùng đợt rà soát này nhưng **đã sửa xong rồi**, không nằm
> trong ISSUE này.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

Mọi tiêu chí phải **ra một con số**, không được ghi "đã tối ưu".

- [ ] `components.css` **≤ 60 KB**. Ghi số thật vào đây: trước `88,6 KB` → sau `______ KB`
- [ ] Mở trang `/auth?action=login`, tab Network → **tổng byte CSS tải về giảm ít nhất 30%**
      so với số đo ở §5. Ghi cả hai số.
- [ ] `grep -c "error-hero-card {" assets/css/components.css` → **1**, không phải 2
- [ ] `grep -c "canonical" views/layout/parts/head.jsp` → **1**
- [ ] Dán link một truyện vào <https://www.opengraph.xyz> → hiện đủ **ảnh bìa, tiêu đề,
      mô tả và URL**, không còn ô trắng
- [ ] Đi hết 6 trang chính *(chủ · kho · chi tiết · đọc chương · đăng nhập · quản trị)* ở
      **cả nền tối lẫn nền sáng** — **không trang nào vỡ giao diện**
- [ ] `scripts\test.ps1` — 101 test vẫn pass

---

## 3. Các bước

| # | Bước | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | **Đo trước đã.** Ghi kích thước từng file CSS và tổng byte CSS của 3 trang mẫu vào §5 | *(không sửa gì)* | ☐ |
| 2 | Tìm và gộp mọi khai báo trùng: `grep -n "^\.[a-z-]* {" components.css \| sort \| uniq -d` | `components.css` | ☐ |
| 3 | Chuyển khối CSS của trang **quản trị** sang `layout-admin.css` *(hiện đang nằm nhầm trong `components.css`)* | `components.css` · `layout-admin.css` | ☐ |
| 4 | Chuyển khối CSS **chỉ trang chi tiết truyện dùng** sang `page-detail.css` mới, nạp qua biến `pageCss` đã có sẵn | `components.css` · `assets/css/page-detail.css` *(mới)* · `StoryServlet` | ☐ |
| 5 | Thêm `<link rel="canonical">` và `<meta property="og:url">` vào `head.jsp` | `head.jsp` | ☐ |
| 6 | **Đo lại**, điền cột "sau" ở §5 | *(không sửa gì)* | ☐ |

### Cách dựng canonical cho đúng

```jsp
<%-- URL chuẩn của trang này. Thiếu nó thì "/story?action=detail&id=3" và
     "/story?action=detail&id=3&from=home" bị Google coi là HAI trang khác
     nhau có cùng nội dung — điểm SEO bị chia đôi. --%>
<c:set var="_canonical" value="${not empty pageCanonical
        ? pageCanonical
        : pageContext.request.requestURL}"/>
<link rel="canonical" href="<c:out value='${_canonical}'/>">
<meta property="og:url" content="<c:out value='${_canonical}'/>">
```

`requestURL` **không** gồm query string — đó là điều mình muốn cho hầu hết trang. Riêng
trang chi tiết truyện thì `id` là phần định danh không bỏ được, nên `StoryServlet.detail()`
đặt thêm `request.setAttribute("pageCanonical", ...)` cho đúng.

> **[NEVER] dùng `requestURL` cho trang có phân trang.** `/story?action=list&page=2` mà
> canonical trỏ về `/story?action=list` thì Google bỏ qua toàn bộ trang 2 trở đi.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Thứ tự nạp CSS, biến `layoutCss` / `pageCss`, `<c:out>` | [`02-VIEW §4 §5`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Đặt attribute `pageCanonical` ở servlet | [`02-VIEW §3`](../../../standards/02-VIEW_CONVENTIONS.md) · [`01-CODING §2`](../../../standards/01-CODING_CONVENTIONS.md) |
| Commit message | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Một luật riêng cho việc này:**

> **Cắt CSS thì phải mở từng trang xem bằng mắt, không tin `grep`.** Một lớp trông như chỉ
> dùng ở một chỗ rất hay được `_partials/` dùng lại ngầm. Bỏ nhầm là trang vỡ, mà CSS vỡ
> thì **không có thông báo lỗi nào** — build vẫn sạch, test vẫn pass.
>
> Cách an toàn: **chuyển chỗ** (cắt khối CSS dán sang file layout tương ứng), **không xoá
> thẳng**. Chuyển chỗ mà sai thì trang vẫn còn style, chỉ là tải thừa. Xoá mà sai thì mất hẳn.

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅. Bỏ trống = chưa xong, dù code đã viết.*

| Đo | Trước | Sau |
|---|---|---|
| `components.css` | 88,6 KB | |
| Tổng byte CSS — trang đăng nhập | | |
| Tổng byte CSS — trang đọc chương | | |
| Tổng byte CSS — trang chủ | | |
| `scripts\test.ps1` | 101 pass / 0 fail | |

- **Bấm thử:**
- **Thử trường hợp xấu:** nền sáng · màn hình 360px · trang quản trị · trang lỗi 403/404

---

## 6. Ghi chú

**Vì sao đặt ngưỡng 60 KB chứ không phải "càng nhỏ càng tốt".** `components.css` là
Design System dùng chung thật — nút, thẻ, form, bảng, dropdown, toast đều nằm đó và **mọi**
trang đều cần. Ép nó xuống 20 KB nghĩa là băm nhỏ rồi mỗi trang tải 5 file, đổi một request
lớn lấy năm request nhỏ — thường là lỗ. 60 KB là mức mà phần *thật sự dùng chung* vừa đủ
chỗ, còn phần đặc thù của admin và trang chi tiết đã ra ngoài.

**Đừng gộp việc này với ISSUE-024.** Cả hai đều là N3 và đều là "dọn dẹp", nhưng một cái
đụng CSS còn một cái đụng JS. Trộn vào thì lúc trang vỡ không biết tại file nào.
