# 🏷️ Issues — việc cần làm

Mỗi việc là **một file phẳng** ngay trong thư mục này:

```text
docs/projects/issues/
├── README.md                                    ← bạn đang ở đây (bảng theo dõi)
├── ISSUE-001-tich-hop-google.md                 ← việc lớn, chia 5 đợt
├── ISSUE-001-tich-hop-google-phase-1.md
├── ISSUE-001-tich-hop-google-phase-2.md
├── ISSUE-001-tich-hop-google-phase-3.md
├── ISSUE-001-tich-hop-google-phase-4.md
└── ISSUE-001-tich-hop-google-phase-5.md
```

Không có thư mục con, không chia theo tháng. File phase để **phẳng cạnh file gốc**,
cùng tiền tố `ISSUE-NNN-` nên vẫn nằm sát nhau khi sắp xếp.
Luật chung: [`../README.md`](../README.md).

---

## Mở một ISSUE mới

1. `git pull` — tránh trùng số với người khác.
2. Nhìn số lớn nhất trong thư mục, lấy số kế tiếp.
3. Copy [`../../templates/issue-template.md`](../../templates/issue-template.md)
   thành `ISSUE-NNN-ten-viec-khong-dau.md`, điền cho hết bảng Meta.
4. Thêm một dòng vào bảng dưới.
5. Commit cùng dòng ISSUE đó — đừng để file nằm riêng trên máy bạn.

**Việc lớn thì chia đợt:** copy [`../../templates/phase-template.md`](../../templates/phase-template.md)
thành `ISSUE-NNN-ten-viec-phase-1.md`. Chia theo **rủi ro** chứ không theo khối lượng —
có sửa `database/schema.sql` hoặc đụng `filter/` thì tách riêng; ngoài ra thì đừng chia.

---

## Bốn mảng — ai cầm cái gì

Chia theo **ranh giới file**, không chia theo "ai rảnh". Mục đích là hai người không bao
giờ mở cùng một `Servlet` trong cùng một buổi.

| Mảng | Ai *(điền tên thật vào đây, một lần)* | Cầm những file nào |
|---|---|---|
| **Dev A — Auth & Security** | `……………` | `AuthServlet` · `UserServlet` · `filter/*` · `util/PasswordUtil` · `util/Csrf*` · `dao/UserDAO` · `dao/PasswordResetDAO` |
| **Dev B — Reader & Story** | `……………` | `StoryServlet` · `ChapterServlet` · `CommentServlet` · `DownloadServlet` · `dao/StoryDAO` · `dao/ChapterDAO` · `dao/CommentDAO` · `views/story/*` · `views/chapter/*` |
| **Dev C — Admin & Data** | `……………` | `admin/*` · `RankServlet` · `dao/ViewLogDAO` · `dao/ReportDAO` · `dao/RatingDAO` · `database/*` · `views/admin/*` |
| **Dev D — Frontend & Platform** | `……………` | `assets/css/*` · `views/layout/*` · `views/_partials/*` · `views/page/*` · `pom.xml` · `scripts/*` · `src/test/*` |

> **Điền tên rồi thì thay luôn trong các file ISSUE.** Cột "Người làm" ở mỗi ISSUE đang ghi
> `Dev A — Auth & Security`; đổi thành tên thật, vì
> [luật giao việc](../README.md) nói ô đó phải là **một người cụ thể**, không phải một vai.

**Dev D không có ISSUE 🔴 nào** — cố ý. Mảng đó là chỗ nhận việc tối ưu, và người rảnh nhất
sẽ đỡ cho mảng đang tắc.

---

## Danh sách

Sắp theo **thứ tự nên làm**, không theo mã số. Ba mức ưu tiên:
🔴 **nợ bảo mật** *(chặn mọi thứ khác)* · 🟢 **tính năng mới** · 🔵 **tối ưu cái đang có**.

| Mã | Việc | Đụng vào | Người làm | Trạng thái |
|---|---|---|---|---|
| **[ISSUE-001](ISSUE-001-tich-hop-google.md)** 🔴 | **Tích hợp Google trọn gói** — 5 đợt: schema · xác minh `idToken` · gắn/gỡ hồ sơ · reCAPTCHA v3 · sao lưu Drive | `schema.sql` · `AuthServlet` · `UserServlet` · `filter/RecaptchaFilter` · `views/auth/*` | Dev A *(phase 5: Dev B)* | 📝 Chưa nhận |
| **ISSUE-002** 🔴 | **Gửi email thật cho quên mật khẩu** — hiện `AuthServlet:335` in thẳng link đặt lại ra màn hình, ai gõ email người khác cũng đọc được. Thêm SMTP + hàng đợi gửi | `AuthServlet` · `util/MailSender` *(mới)* · `views/auth/forgot.jsp` · `pom.xml` | Dev A | 📝 Chưa nhận |
| **ISSUE-003** 🔴 | **Chặn dò mật khẩu và spam** — không có giới hạn số lần thử đăng nhập, không có khoảng nghỉ giữa hai bình luận. Đếm theo IP + theo tài khoản | `AuthServlet` · `CommentServlet` · `util/RateLimiter` *(mới)* | Dev A | 📝 Chưa nhận |
| **ISSUE-004** 🟢 | **Bình luận theo từng chương** — `schema.sql` đã chừa sẵn đường *(chú thích bảng `comments`)*: thêm `chapter_id` cho `NULL`. Chia **2 đợt** vì có sửa schema | `schema.sql` · `CommentDAO` · `CommentServlet` · `views/chapter/read.jsp` | Dev B | 📝 Chưa nhận |
| **ISSUE-005** 🟢 | **Tìm trong nội dung chương** — `FULLTEXT ft_chapter_text` đã tạo trong schema nhưng **chưa trang nào dùng**. Thêm lọc nhiều tag cùng lúc, lọc theo số chương, theo điểm sao | `StoryServlet` · `ChapterDAO` · `views/story/search.jsp` | Dev B | 📝 Chưa nhận |
| **ISSUE-006** 🟢 | **Trang tác giả công khai** — hiện `follows` có rồi nhưng không có trang nào giới thiệu một tác giả. Thêm trang + bảng xếp hạng tác giả | `UserServlet` · `StoryDAO` · `views/user/profile.jsp` · `views/story/rank.jsp` | Dev B | 📝 Chưa nhận |
| **ISSUE-007** 🟢 | **Gợi ý truyện** — "cùng thể loại" từ `story_tags`, "người đọc truyện này cũng đọc" từ `view_logs`. Không cần thư viện ngoài, hai câu SQL | `StoryDAO` · `views/story/detail.jsp` · `views/_partials/_story-row.jsp` | Dev C | 📝 Chưa nhận |
| **ISSUE-008** 🟢 | **Ủng hộ tác giả bằng xu ảo** — bảng `wallets` + `transactions`, không đụng tiền thật. Chia **3 đợt**, đợt 1 là schema | `schema.sql` · `WalletServlet` *(mới)* · `dao/WalletDAO` *(mới)* · `views/user/me.jsp` | Dev C | 📝 Chưa nhận |
| **ISSUE-009** 🟢 | **API JSON** — `/api/stories`, `/api/story/{slug}`, `/api/chapter/{id}`. Chỉ đọc, không ghi. Dùng lại DAO sẵn có, không viết SQL mới | `controller/api/*` *(mới)* · `util/JsonWriter` *(mới)* | Dev C | 📝 Chưa nhận |
| **ISSUE-010** 🟢 | **PWA — đọc offline** — `manifest.json` + service worker cache chương đã mở. Cài về màn hình chính điện thoại | `webapp/manifest.json` *(mới)* · `webapp/sw.js` *(mới)* · `views/layout/parts/head.jsp` | Dev D | 📝 Chưa nhận |
| **ISSUE-011** 🔵 | **Cắt `components.css` 79 KB** — mọi trang đều tải trọn 79 KB kể cả trang đăng nhập chỉ dùng vài phần trăm. Chia theo tầng đã có sẵn trong `head.jsp` | `assets/css/components.css` · `views/layout/parts/head.jsp` | Dev D | 📝 Chưa nhận |
| **ISSUE-012** 🔵 | **SEO** — `head.jsp` **không có** thẻ `og:`, `description`, `canonical`; `webapp/` không có `robots.txt` lẫn `sitemap.xml`. Dán link truyện lên Facebook giờ ra ô trắng | `views/layout/parts/head.jsp` · `SitemapServlet` *(mới)* · `webapp/robots.txt` *(mới)* | Dev D | 📝 Chưa nhận |
| **ISSUE-013** 🔵 | **Dọn `view_logs` định kỳ** — `schema.sql` đã ghi rõ *"bảng này sẽ lớn nhất hệ thống"* và *"hệ thống thật sẽ dọn định kỳ"*, nhưng chưa có gì dọn. Thêm gộp theo ngày + xoá dòng cũ hơn 90 ngày | `ViewLogDAO` · `util/AppListener` · `database/*` | Dev C | 📝 Chưa nhận |
| **ISSUE-014** 🔵 | **Nâng test từ 28 lên phủ DAO và Servlet** — 28 test hiện tại **chỉ kiểm hàm thuần** *(`SlugUtil`, `PasswordUtil`, tách đoạn, lịch sử đọc)*. Chưa có test nào chạm DAO hay Servlet, tức là chưa test gì về quyền | `src/test/java/truyen/*` · `pom.xml` | Dev D | 📝 Chưa nhận |
| **ISSUE-015** 🔵 | **Kiểm trên điện thoại thật + trợ năng** — đi hết 31 trang ở màn hình 360px, kiểm tương phản màu, thứ tự Tab, `alt` cho ảnh bìa | `assets/css/*` · `views/_partials/*` | Dev D | 📝 Chưa nhận |

> Cột **Đụng vào** ghi tên file thật (`CommentServlet`, `views/page/detail.jsp`),
> không ghi chung chung kiểu *"phần bình luận"* — để hai người nhìn phát biết có
> giẫm chân nhau không.

### Thứ tự làm — đọc ngang, không đọc dọc

```text
TUẦN 1   bug-001  →  ISSUE-001 phase 1  →  phase 2        (Dev A, một mình, không ai chen)
         ISSUE-011, ISSUE-012                             (Dev D, song song, không đụng nhau)
         ISSUE-007                                        (Dev C, song song)

TUẦN 2   ISSUE-001 phase 3, 4   +   ISSUE-002             (Dev A)
         ISSUE-001 phase 5      →   ISSUE-004             (Dev B)
         ISSUE-013  →  ISSUE-008 đợt 1 (schema)           (Dev C)
         ISSUE-014                                        (Dev D)

TUẦN 3+  ISSUE-003 (Dev A) · ISSUE-005, 006 (Dev B) · ISSUE-008, 009 (Dev C) · ISSUE-010, 015 (Dev D)
```

Hai chỗ **bắt buộc nối tiếp**, không song song được:

- `bug-001` → `ISSUE-001 phase 1` → `phase 2`. Cả ba đều đụng đường đăng nhập.
- Mọi đợt **sửa `schema.sql`** *(ISSUE-001 phase 1 · ISSUE-004 đợt 1 · ISSUE-008 đợt 1)*
  phải **cách nhau ra**, đừng để hai người sửa `schema.sql` trong cùng một ngày. Merge
  conflict trong file SQL thì dễ giải, nhưng hai người chạy hai bản schema lệch nhau trên
  hai máy thì mất cả buổi mới hiểu tại sao code của mình lỗi trên máy người kia.

### Chỉ 15 ISSUE, không nhiều hơn

Danh sách này **cố ý dừng** ở đây. Bốn thứ đã cân nhắc rồi bỏ:

| Bỏ | Vì |
|---|---|
| Chuyển sang Spring Boot | Đề bài là **Servlet + JSP**. Đổi framework là làm lại đồ án, không phải nâng cấp nó. |
| Chuyển JSP sang React | Mất luôn điểm mạnh đang có: server-rendered, SEO tự nhiên, không cần build. Xem lý lẽ trong [`README.md` — "Không có `index.html` nào cả"](../../../README.md). |
| Dịch web sang tiếng Anh | Công sức lớn, không thêm điểm kỹ thuật nào. |
| Đưa lên máy chủ thật | Đáng làm, nhưng là việc *sau khi* xong 🔴 — đưa lỗ hổng ở `bug-001` lên Internet thì không còn là bài tập nữa. |

Thấy thiếu thật thì thêm — nhưng **thêm vì đang vướng**, không phải vì "cho đủ bộ".
