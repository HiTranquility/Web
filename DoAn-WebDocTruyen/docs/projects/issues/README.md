# 🏷️ Issues — việc cần làm

Mỗi việc là một **thư mục** chuyên biệt trong thư mục này:

```text
docs/projects/issues/
├── README.md                                    ← bạn đang ở đây (bảng theo dõi)
├── ISSUE-001-tich-hop-google/                   ← việc lớn, chia 5 đợt
│   ├── issue.md                                 ← mô tả tổng thể & checklist
│   ├── phase-1.md                               ← đợt 1: CSDL & model
│   ├── phase-2.md                               ← đợt 2: verify token
│   ├── phase-3.md                               ← đợt 3: gắn/gỡ hồ sơ
│   ├── phase-4.md                               ← đợt 4: reCAPTCHA
│   ├── phase-5.md                               ← đợt 5: Drive backup
│   ├── review-1.md                              ← nghiệm thu sau phase 1
│   ├── review-2.md                              ← nghiệm thu sau phase 2
│   ├── review-3.md                              ← nghiệm thu sau phase 3
│   ├── review-4.md                              ← nghiệm thu sau phase 4
│   └── review-5.md                              ← nghiệm thu sau phase 5
├── ISSUE-002-gui-email-that/ … ISSUE-018-chia-se-truyen/     (đợt 1 — đã xong)
└── ISSUE-019-don-no-dot-1/   … ISSUE-024-tach-js-khoi-jsp/   (đợt 2 — chưa nhận)
```

Mỗi issue là một thư mục `ISSUE-NNN-slug/` chứa `issue.md`, các file `phase-N.md` và `review-N.md`.
**[NEVER]** tạo file phẳng — không có chỗ đặt phase và review.
Luật chung: [`../README.md`](../README.md).

---

## Mở một ISSUE mới

1. `git pull` — tránh trùng số với người khác (hoặc chạy `powershell -ExecutionPolicy Bypass -File docs\reindex.ps1`).
2. Nhìn số lớn nhất trong thư mục, lấy số kế tiếp.
3. Tạo thư mục `ISSUE-NNN-ten-viec-khong-dau/`.
4. Copy [`../../templates/issue-template.md`](../../templates/issue-template.md) thành `issue.md`, điền cho hết bảng Meta.
5. Việc lớn thì chia đợt: copy [`../../templates/phase-template.md`](../../templates/phase-template.md) thành `phase-1.md`. Chia theo **rủi ro** chứ không theo khối lượng — có sửa `database/schema.sql` hoặc đụng `filter/` thì tách riêng; ngoài ra thì đừng chia.
6. Xong mỗi phase, copy [`../../templates/review-template.md`](../../templates/review-template.md) thành `review-N.md` đối chiếu baseline & Acceptance Criteria rồi mới mở phase kế.
7. Thêm một dòng vào bảng dưới.
8. Commit cùng dòng ISSUE đó — đừng để file nằm riêng trên máy bạn.

---

## Ba mảng — ai cầm cái gì

Chia theo **ranh giới file**, không chia theo "ai rảnh". Mục đích là hai người không bao
giờ mở cùng một `Servlet` trong cùng một buổi.

| Mảng | Ai *(điền tên thật vào đây, một lần)* | Cầm những file nào |
|---|---|---|
| **N1 — Nền tảng & Quản trị** | `……………` | `AuthServlet` · `UserServlet` · `WalletServlet` · `filter/*` · `admin/*` · `util/PasswordUtil` · `util/Csrf*` · `util/RateLimiter` · `dao/UserDAO` · `dao/WalletDAO` · `dao/ReportDAO` · `database/*` · `WEB-INF/web.xml` |
| **N2 — Đọc & Nội dung** | `……………` | `StoryServlet` · `ChapterServlet` · `CommentServlet` · `DownloadServlet` · `DriveBackupServlet` · `RankServlet` · `api/ApiServlet` · `dao/StoryDAO` · `dao/ChapterDAO` · `dao/CommentDAO` · `dao/RatingDAO` · `views/common/*` · `views/user/story/*` · `views/user/chapter/*` |
| **N3 — Giao diện & Hiệu năng** | `……………` | `assets/css/*` · `assets/js/*` · `views/layout/*` · `views/_partials/*` · `webapp/sw.js` · `webapp/manifest.json` · `pom.xml` · `scripts/*` · `src/test/*` |

> **Điền tên rồi thì thay luôn trong các file ISSUE.** Cột "Người làm" ở mỗi ISSUE đang ghi
> tên vai (`Dev A`, `N1`…); đổi thành tên thật, vì
> [luật giao việc](../README.md) nói ô đó phải là **một người cụ thể**, không phải một vai.

### Vì sao gộp từ bốn mảng xuống ba

Đợt 1 (ISSUE-001…018) chia bốn vai. Nhóm giờ có **ba người**, nên gộp lại — nhưng gộp
theo đường nào thì quan trọng hơn là gộp:

| Gộp | Vì |
|---|---|
| `Dev A (Auth)` + `Dev C (Admin & Data)` → **N1** | Hai vai này vốn đã dùng chung `dao/UserDAO` và `database/schema.sql`. Tách ra là hai người sửa `schema.sql` trong cùng một tuần — đúng thứ cả đợt 1 phải né. Gộp lại thì **chỉ một người được đụng schema**, hết xung đột. |
| `Dev B` → **N2**, nhận thêm `ApiServlet` | API chỉ đọc lại đúng `StoryDAO` / `ChapterDAO` mà N2 đang cầm. Giao cho người khác là họ phải hỏi N2 mọi lần đổi DAO. |
| `Dev D` → **N3**, giữ nguyên | Mảng này không chạm `Servlet` nào, nên nó chưa bao giờ giẫm chân ai. Giữ nguyên là lựa chọn rẻ nhất. |

**N3 không có ISSUE 🔴 nào** — cố ý. Mảng đó là chỗ nhận việc tối ưu, và người rảnh nhất
sẽ đỡ cho mảng đang tắc.

> **[Bẫy] `database/schema.sql` thuộc về N1, không ai khác.** Đợt 2 có hai việc cần sửa
> schema (ISSUE-020 và ISSUE-022), mà một việc nằm ở N1 còn một việc nằm ở N2. Luật:
> **N2 viết câu SQL, N1 là người commit vào `schema.sql`** — một cửa vào duy nhất.

---

## Danh sách

Sắp theo **thứ tự nên làm**, không theo mã số. Ba mức ưu tiên:
🔴 **nợ bảo mật** *(chặn mọi thứ khác)* · 🟢 **tính năng mới** · 🔵 **tối ưu cái đang có**.

| Mã | Việc | Đụng vào | Người làm | Trạng thái |
|---|---|---|---|---|
| **[ISSUE-001](ISSUE-001-tich-hop-google/issue.md)** 🔴 | **Tích hợp Google trọn gói** — 5 đợt: schema · xác minh `idToken` · gắn/gỡ hồ sơ · reCAPTCHA v3 · sao lưu Drive | `schema.sql` · `AuthServlet` · `UserServlet` · `DriveBackupServlet` · `filter/RecaptchaFilter` · `views/auth/*` | Dev A *(phase 5: Dev B)* | ✅ Đã giải quyết xong |
| **[ISSUE-002](ISSUE-002-gui-email-that/issue.md)** 🔴 | **Gửi email thật cho quên mật khẩu** — xóa bỏ lộ link ra màn hình, tích hợp SMTP (JavaMail) bất đồng bộ với hàng đợi gửi và chế độ Dev an toàn | `AuthServlet` · `util/MailSender` *(mới)* · `views/auth/forgot.jsp` · `pom.xml` | Dev A | ✅ Xong |
| **[ISSUE-003](ISSUE-003-chan-do-mat-khau-va-spam/issue.md)** 🔴 | **Chặn dò mật khẩu và spam** — giới hạn 5 lần thử sai trong 15 phút, cooldown 20s giữa các lượt bình luận, thread-safe in-memory sliding window | `AuthServlet` · `CommentServlet` · `util/RateLimiter` *(mới)* · `views/layout/main.jsp` | Dev A | ✅ Xong |
| **[ISSUE-004](ISSUE-004-binh-luan-theo-chuong/issue.md)** 🟢 | **Bình luận theo từng chương** — thêm cột `chapter_id`, nạp thảo luận dưới chân chương đọc, trả lời và thả tim mượt mà | `schema.sql` · `CommentDAO` · `CommentServlet` · `ChapterServlet` · `views/chapter/read.jsp` · `_comment.jsp` | Dev B | ✅ Xong |
| **[ISSUE-005](ISSUE-005-tim-trong-noi-dung-chuong/issue.md)** 🟢 | **Tìm trong nội dung chương** — `FULLTEXT ft_chapter_text` đã tạo trong schema, tìm kiếm sâu trong chương truyện, lọc đa thể loại, theo số chương, điểm sao | `StoryServlet` · `ChapterDAO` · `views/common/story/search.jsp` | Dev B | ✅ Xong |
| **[ISSUE-006](ISSUE-006-trang-tac-gia-cong-khai/issue.md)** 🟢 | **Trang tác giả công khai** — bảng xếp hạng tác giả nổi bật theo tổng lượt xem và followers (`/rank?by=authors`), liên kết tác giả thông suốt từ thẻ truyện | `UserServlet` · `UserDAO` · `RankServlet` · `views/user/profile.jsp` · `views/common/rank.jsp` · `_card.jsp` | Dev B | ✅ Xong |
| **[ISSUE-007](ISSUE-007-goi-y-truyen/issue.md)** 🟢 | **Gợi ý truyện** — "cùng thể loại" từ `story_tags`, "người đọc truyện này cũng đọc" (Collaborative Filtering) từ `view_logs` và `bookmarks` | `StoryDAO` · `StoryServlet` · `views/story/detail.jsp` | Dev C | ✅ Xong |
| **[ISSUE-008](ISSUE-008-ung-ho-tac-gia-xu-ao/issue.md)** 🟢 | **Ủng hộ tác giả bằng xu ảo** — bảng `wallets` + `transactions`, không đụng tiền thật. Ví xu cá nhân, tặng hoa/thưởng xu cho tác giả | `schema.sql` · `WalletServlet` *(mới)* · `dao/WalletDAO` *(mới)* · `views/user/me.jsp` | Dev C | ✅ Xong |
| **[ISSUE-009](ISSUE-009-api-json/issue.md)** 🟢 | **API JSON** — `/api/stories`, `/api/story/{slug}`, `/api/chapter/{id}`. Trả về JSON chuẩn RESTful cho bên thứ ba và mobile | `controller/api/*` *(mới)* · `util/JsonWriter` *(mới)* | Dev C | ✅ Xong |
| **[ISSUE-010](ISSUE-010-pwa-doc-offline/issue.md)** 🟢 | **PWA — đọc offline** — `manifest.json` + service worker cache chương đã mở. Cài về màn hình chính điện thoại | `webapp/manifest.json` *(mới)* · `webapp/sw.js` *(mới)* · `views/layout/parts/head.jsp` | Dev D | ✅ Xong |
| **[ISSUE-011](ISSUE-011-tinh-gon-css/issue.md)** 🔵 | **Cắt `components.css` 79 KB** — Tinh gọn & phân tầng 4 lớp (Base, Components, Layout, Page). Nạp có điều kiện qua `head.jsp` | `assets/css/components.css` · `views/layout/parts/head.jsp` | Dev D | ✅ Xong |
| **[ISSUE-012](ISSUE-012-seo-sitemap-opengraph/issue.md)** 🔵 | **SEO, OpenGraph, Sitemap & Robots** — Thẻ `og:`, `description`, `canonical`; `webapp/robots.txt` lẫn `SitemapServlet` (`/sitemap.xml`) | `views/layout/parts/head.jsp` · `SitemapServlet` *(mới)* · `webapp/robots.txt` *(mới)* | Dev D | ✅ Xong |
| **[ISSUE-013](ISSUE-013-don-view-logs-dinh-ky/issue.md)** 🔵 | **Dọn `view_logs` định kỳ** — `schema.sql` đã ghi rõ *"bảng này sẽ lớn nhất hệ thống"*. Thêm gộp theo ngày + xoá dòng cũ hơn 90 ngày | `ViewLogDAO` · `util/AppListener` · `database/*` | Dev C | ✅ Xong |
| **[ISSUE-014](ISSUE-014-nang-phu-test-dao-servlet/issue.md)** 🔵 | **Nâng test phủ DAO và Servlet** — Nâng từ 28 bài test thuần lên 97 bài test phủ DAO và Servlet nghiệp vụ | `src/test/java/truyen/*` · `pom.xml` | Dev D | ✅ Xong |
| **[ISSUE-015](ISSUE-015-mobile-tro-nang-a11y/issue.md)** 🔵 | **Kiểm trên điện thoại thật + trợ năng** — Chuẩn hóa 360px mobile, vùng chạm tối thiểu 44px (WCAG 2.1), skip-link và prefers-reduced-motion | `assets/css/*` · `views/_partials/*` | Dev D | ✅ Xong |
| **[ISSUE-016](ISSUE-016-trang-loi-404-500/issue.md)** 🟢 | **Trang báo lỗi 404 & 500 tùy biến** — Bắt chặn lỗi tập trung, giao diện phong cách truyện, bảo mật máy chủ | `web.xml` · `controller/common/ErrorServlet.java` · `views/error/*` | Dev D | ✅ Xong |
| **[ISSUE-017](ISSUE-017-live-preview-anh-bia-truyen/issue.md)** 🟢 | **Tải ảnh bìa truyện từ máy tính & Xem trước trực tiếp** — Live preview FileReader, khung chuẩn 3:4 | `views/user/story/form.jsp` · `assets/css/components.css` | Dev D | ✅ Xong |
| **[ISSUE-018](ISSUE-018-chia-se-truyen/issue.md)** 🟢 | **Tiện ích Chia sẻ truyện & Sao chép liên kết nhanh** — Nút chia sẻ 1 chạm, copy clipboard kèm Toast notification | `views/common/story/detail.jsp` · `assets/css/components.css` | Dev D | ✅ Xong |

### Bộ 18 ISSUE hoàn thiện toàn diện dự án

Lộ trình 18 issue này bao phủ trọn vẹn mọi khía cạnh: Bảo mật, Trải nghiệm độc giả, Trải nghiệm tác giả, API, PWA, SEO và Tối ưu hiệu năng.

---

## Đợt 2 — đề xuất, chia cho ba người

Rà soát ngày **2026-09-23** *(chạy site thật trên trình duyệt, đọc lại code và doc)*:
97/97 test pass, 18 ISSUE đợt 1 đã xong. Sáu việc dưới đây là thứ **còn thật sự thiếu**,
**hai việc mỗi người**, không việc nào đụng file của việc kia.

| Mã | Việc | Đụng vào | Mảng | Trạng thái |
|---|---|---|---|---|
| **[ISSUE-019](ISSUE-019-don-no-dot-1/issue.md)** 🔵 | **Dọn nợ đợt 1** — ba chỗ *doc nói xong mà code chưa xong*: (1) `components.css` **đã phình 79 → 88,6 KB** và vẫn nạp trên mọi trang — cắt thật rồi **đo lại bằng số**; (2) thêm `canonical` + `og:url` mà ISSUE-012 nhắc nhưng không có; (3) `.error-hero-card` khai **hai lần** trong cùng một file CSS. *(Phần [`bug-002`](../bugs/bug-002-web-xml-khai-trung-error-page/bug.md) đã sửa xong 2026-09-24, không còn trong ISSUE này.)* | `assets/css/components.css` · `views/layout/parts/head.jsp` · `StoryServlet` | **N3** | 📝 Chưa nhận |
| **[ISSUE-020](ISSUE-020-mo-khoa-chuong-bang-xu/issue.md)** 🟢 | **Mở khoá chương bằng xu** — ví xu ở ISSUE-008 hiện chỉ tặng được tác giả, tiêu xong không để làm gì. Cho tác giả đặt chương VIP, độc giả trả xu mở khoá vĩnh viễn. **Chia 2 đợt**, đợt 1 là schema | `schema.sql` · `WalletServlet` · `ChapterServlet` · `dao/WalletDAO` · `dao/ChapterDAO` | **N1** | 📝 Chưa nhận |
| **[ISSUE-021](ISSUE-021-xuat-truyen-epub/issue.md)** 🟢 | **Xuất truyện ra EPUB** — `.txt` hiện tại mất hết chương mục và không mở được bằng Kindle / Apple Books. EPUB chỉ là zip + XHTML, dùng lại thẳng `util/ChapterToTxt` | `DownloadServlet` · `util/EpubWriter` *(mới)* · `views/common/story/detail.jsp` | **N2** | 📝 Chưa nhận |
| **[ISSUE-022](ISSUE-022-danh-gia-dai-spoiler/issue.md)** 🟢 | **Đánh giá dài + nhãn spoiler** — hiện chỉ chấm được 1–5 sao, không viết được một dòng cảm nhận nào. Thêm bài đánh giá có tiêu đề, nội dung, nút *"Có ích"* và nhãn che spoiler. **Chia 2 đợt**, đợt 1 là schema | `schema.sql` · `dao/RatingDAO` · `RatingServlet` · `views/common/story/detail.jsp` | **N2** | 📝 Chưa nhận |
| **[ISSUE-023](ISSUE-023-admin-vi-xu-nhat-ky/issue.md)** 🟢 | **Admin: trang Ví xu + Nhật ký thao tác** — sidebar quản trị hiện có 6 mục và **không mục nào nhìn thấy được giao dịch xu**; cũng không có chỗ nào ghi lại *ai đã gỡ truyện của ai, lúc nào* | `admin/AdminWalletServlet` *(mới)* · `admin/AdminAuditServlet` *(mới)* · `schema.sql` · `views/admin/*` · `views/layout/admin.jsp` | **N1** | 📝 Chưa nhận |
| **[ISSUE-024](ISSUE-024-tach-js-khoi-jsp/issue.md)** 🔵 | **Tách JS ra khỏi JSP** — `parts/nav.jsp` đang mang **188 dòng `<script>` nội tuyến** (theme toggle, dropdown, live search), gửi lại nguyên xi ở **mọi** trang và trình duyệt **không cache được dòng nào** | `views/layout/parts/nav.jsp` · `assets/js/nav.js` *(mới)* · `views/layout/parts/head.jsp` | **N3** | 📝 Chưa nhận |

### Thứ tự trong đợt 2

```text
LÀM TRƯỚC   ISSUE-019  (N3)   — dọn nợ trước khi chồng thêm; nhẹ, một buổi
            ISSUE-020 đợt 1   — schema, N1 làm MỘT MÌNH, không ai pull trong lúc này
            ISSUE-022 đợt 1   — schema, N1 commit hộ N2, LÀM SAU ISSUE-020 đợt 1

SONG SONG   ISSUE-020 đợt 2 (N1) · ISSUE-021 (N2) · ISSUE-024 (N3)
SAU ĐÓ      ISSUE-023 (N1)       · ISSUE-022 đợt 2 (N2)
```

Hai chỗ **không được song song**: hai đợt schema của ISSUE-020 và ISSUE-022 phải nối
tiếp nhau, vì cả hai đều sửa `schema.sql`.

> Sáu thư mục `ISSUE-NNN-slug/` đã dựng đủ, mỗi thư mục có `issue.md`; ISSUE-020 và
> ISSUE-022 có thêm `phase-1.md` + `phase-2.md`. Nhận việc nào thì điền tên thật vào ô
> **Người làm** của `issue.md`, đổi Trạng thái sang 🚧, và cập nhật dòng tương ứng ở bảng
> trên. Xong mỗi phase thì copy [`review-template.md`](../../templates/review-template.md)
> thành `review-N.md` **trước khi** mở phase kế.

| Bỏ | Vì |
|---|---|
| Chuyển sang Spring Boot | Đề bài là **Servlet + JSP**. Đổi framework là làm lại đồ án, không phải nâng cấp nó. |
| Chuyển JSP sang React | Mất luôn điểm mạnh đang có: server-rendered, SEO tự nhiên, không cần build. Xem lý lẽ trong [`README.md` — "Không có `index.html` nào cả"](../../../README.md). |
| Dịch web sang tiếng Anh | Công sức lớn, không thêm điểm kỹ thuật nào. |
| Đưa lên máy chủ thật | Đáng làm, nhưng là việc *sau khi* xong 🔴 — đưa lỗ hổng ở `bug-001` lên Internet thì không còn là bài tập nữa. |

Thấy thiếu thật thì thêm — nhưng **thêm vì đang vướng**, không phải vì "cho đủ bộ".
