# 🏷️ ISSUE-025: Nâng cấp báo cáo vi phạm — phân loại, ảnh bằng chứng, bấm thẳng tới nội dung

> **Đích:** `docs/projects/issues/ISSUE-025-nang-cap-bao-cao-vi-pham/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-025 |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | ✅ Xong |
| **Ngày mở** | 2026-09-24 |
| **CASE liên quan** | CASE 10 — Quản trị *(mở rộng hệ thống báo cáo)* |
| **Đụng vào** | `database/schema.sql` · `ReportServlet` · `AdminReportServlet` · `dao/ReportDAO` · `model/Report.java` · `util/UploadUtil` · `views/common/story/detail.jsp` · `views/_partials/_comment.jsp` · `views/admin/reports.jsp` · `assets/css/components.css` |

---

## 1. Làm cái gì, và vì sao (Goal)

Hệ thống báo cáo hiện chạy đúng nhưng **thiếu thông tin để admin xử lý được**. Ba chỗ,
đã kiểm từng chỗ trong code:

| # | Chỗ thiếu | Bằng chứng trong code |
|:-:|---|---|
| 1 | **Báo cáo bình luận không bấm vào được** | `views/admin/reports.jsp:63` — truyện thì có thẻ `<a>` *(dòng 57)*, còn bình luận chỉ hiện 80 ký tự trong `<span class="quote">`. Admin muốn xem bình luận đó nằm ở đâu phải **tự đi mò từng truyện**. Nguyên nhân gốc ở `ReportDAO.findAll()`: câu `LEFT JOIN comments c` không lấy `c.story_id`, nên **không có gì để dựng link**. |
| 2 | **Không gửi được ảnh bằng chứng** | `reports` không có cột nào chứa ảnh. Người báo cáo phải mô tả bằng lời trong 500 ký tự. Với ảnh bìa phản cảm hoặc bình luận đã bị sửa, lời kể không thay được ảnh chụp. |
| 3 | **Chỉ có một ô lý do tự do** | `detail.jsp:173` — đúng một `<input type="text" name="reason">` với gợi ý *"spam, nội dung cấm, đạo văn…"*. Không phân loại được, nên **không lọc được**, không đếm được loại nào nhiều, và không đặt được mức ưu tiên. |

- **Sau khi xong:** người báo cáo **chọn loại vi phạm** từ danh sách, viết thêm mô tả, và
  **đính kèm tối đa 3 ảnh**. Admin mở trang báo cáo là bấm thẳng tới đúng truyện hoặc đúng
  bình luận, xem ảnh ngay tại chỗ, và lọc theo loại vi phạm.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

**Phân loại vi phạm**

- [x] Form báo cáo có ô chọn loại. **Sửa so với thiết kế ban đầu:** ô mặc định là
      *"— Chọn loại vi phạm —"* (rỗng, `required`) chứ **không** phải "Khác" — bắt người
      ta chọn chủ động, không để họ vô tình dồn mọi thứ vào "Khác". `OTHER` vẫn là mặc
      định **ở CSDL**, dành cho dữ liệu cũ.
- [x] Không chọn loại → **không gửi được**, không ghi dòng nào.
- [x] Chọn *"Khác"* mà bỏ trống mô tả → bị chặn.
- [x] Trang admin có bộ lọc đủ **8 loại**, mỗi báo cáo hiện nhãn màu.
- [x] Ba loại nặng đánh dấu 🔴 và **nổi lên đầu** — hai báo cáo `ADULT` đứng trên `SPAM`
      trong nhóm PENDING.

**Ảnh bằng chứng**

- [x] Đính kèm nhiều ảnh — gửi thật 2 ảnh, cả hai vào CSDL và vào ổ đĩa *(kiểm 8 byte
      đầu: `89504e47` = PNG thật)*.
- [x] Gửi 4 ảnh → chặn, và **0 file rác** còn lại *(đếm trước, lưu sau — đúng thiết kế)*.
- [x] Ảnh > 2 MB → chặn, hiện *"Ảnh bằng chứng quá 2 MB. Chọn ảnh nhỏ hơn."*
      **Phải sửa mới đạt** — xem §6 *(lỗi 1)*: ban đầu ra 403 trống.
- [x] File chạy Windows *(2 byte đầu `MZ`)* đổi tên thành `virus.jpg` → **bị từ chối**,
      0 dòng CSDL, 0 file trên đĩa.
- [x] Trang admin hiện ảnh thu nhỏ, có link mở ảnh gốc.
- [x] 🔴 **Hàng rào ảnh tố cáo:** khách **403** · thành viên thường **403** ·
      lách `/uploads/x/../evidence/…` **403** · admin **200** `image/png`.
- [x] Gửi 4 lượt liên tiếp → **đúng 3 lượt lọt**, lượt thứ 4 bị chặn.

**Bấm thẳng tới nội dung**

- [x] Báo cáo **truyện** → link cũ vẫn chạy, không hỏng.
- [x] Báo cáo **bình luận** → `/story?action=detail&id=1#comment-48`; trang truyện có 7
      neo `id="comment-N"`, `scroll-margin-top: 88px` nên header dính không che.
- [x] Bình luận thuộc truyện đã gỡ → admin **200** *(kèm dải cảnh báo)*, thành viên
      **404**, khách **404**. **Phải sửa mới đạt** — xem §6 *(lỗi 2)*: ban đầu admin
      cũng nhận 404, link trong trang báo cáo là link chết.
- [x] Nội dung đã bị xoá hẳn → hiện *"Nội dung không còn tồn tại"*. Gặp thật: hai báo cáo
      mẫu trỏ vào comment id 1 và 2, **hai id đó không tồn tại** trong bảng `comments`
      *(lệch có sẵn của dữ liệu mẫu, không phải do ISSUE này gây ra)*.

**Chung**

- [x] 5 báo cáo cũ tự rơi vào `OTHER` sau migration, hiện bình thường.
- [x] `scripts\test.ps1` — **114 pass / 0 fail** *(101 cũ + 13 ca mới)*.

---

## 3. Chia đợt — 2 phase

| Phase | Nội dung | Vì sao đứng riêng |
|:--:|---|---|
| [**1**](phase-1.md) | Schema: cột `category`, bảng `report_evidence`, và `story_id` cho câu JOIN | Sửa `schema.sql` — **luôn đứng riêng và làm trước** |
| [**2**](phase-2.md) | Form chọn loại · tải ảnh · trang admin · link sâu tới bình luận | Thêm một đường **nhận file từ người dùng** — bề mặt tấn công mới |

> **[MUST] Phase 1 phải xếp hàng với hai đợt schema khác của đợt 2.** Thứ tự đã chốt:
> [`ISSUE-020 phase 1`](../ISSUE-020-mo-khoa-chuong-bang-xu/phase-1.md) →
> [`ISSUE-022 phase 1`](../ISSUE-022-danh-gia-dai-spoiler/phase-1.md) → **ISSUE-025 phase 1**.
> Cả ba đều sửa `database/schema.sql`, và luật ở [`issues/README`](../README.md) là **chỉ
> N1 được commit vào file đó**.

### 3.1 Tám loại vi phạm

Chọn tám loại, **không nhiều hơn**. Danh sách dài quá thì người ta chọn bừa cái đầu tiên —
và một danh sách bị chọn bừa còn tệ hơn không phân loại, vì nó tạo ra số liệu sai mà nhìn
như số liệu thật.

| Mã ENUM | Nhãn hiển thị | Mức | Áp dụng cho |
|---|---|:--:|---|
| `SPAM` | Spam, quảng cáo, rác | 🟡 | truyện · bình luận |
| `ADULT` | Nội dung người lớn, khiêu dâm | 🔴 | truyện · bình luận |
| `VIOLENCE` | Bạo lực, thù ghét, kích động | 🔴 | truyện · bình luận |
| `PRIVACY` | Lộ thông tin cá nhân người khác | 🔴 | truyện · bình luận |
| `PLAGIARISM` | Đạo văn, đăng lại không xin phép | 🟡 | truyện |
| `WRONG_INFO` | Sai thể loại, sai mô tả, tiêu đề gây hiểu nhầm | 🟡 | truyện |
| `HARASSMENT` | Quấy rối, xúc phạm người dùng khác | 🟡 | bình luận |
| `OTHER` | Khác *(bắt buộc mô tả)* | 🟡 | truyện · bình luận |

> **Ba loại 🔴 nổi lên đầu hàng đợi.** Không phải để cho đẹp: nội dung người lớn và lộ
> thông tin cá nhân là loại mà **mỗi giờ chậm là thêm người nhìn thấy**. Spam thì để tới
> chiều xử cũng không sao.
>
> Cột **Áp dụng cho** không phải trang trí — form báo cáo bình luận **không hiện**
> `PLAGIARISM` và `WRONG_INFO`, vì hai loại đó vô nghĩa với một dòng bình luận. Lọc danh
> sách theo ngữ cảnh là cách rẻ nhất để người ta chọn đúng.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên `ReportEvidence`, `EvidenceDAO`, URL `?action=` | [`01-CODING §1 §2 §5`](../../../standards/01-CODING_CONVENTIONS.md) |
| Attribute `categories`, `evidences`, **`<c:out>` cho mô tả người dùng nhập** | [`02-VIEW §3 §4`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Cột `category` ENUM, bảng `report_evidence`, khoá ngoại | [`03-DATABASE §2 §4`](../../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit `ISSUE-025 phase-N — …` | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Hai luật riêng — đây là chỗ nguy hiểm nhất của cả ISSUE:**

> **[MUST] Ảnh bằng chứng KHÔNG được để ai cũng xem.**
> `UploadedFileServlet` hiện phục vụ `/uploads/*` **công khai** — đúng cho ảnh bìa truyện,
> **sai hoàn toàn** cho ảnh tố cáo. Ảnh tố cáo có thể chứa ảnh chụp tin nhắn riêng, thông
> tin cá nhân của người bị tố, hoặc chính nội dung phản cảm đang bị báo cáo. Ai đoán được
> tên file là xem được hết.
>
> Cách làm: để ảnh bằng chứng ở thư mục con `uploads/evidence/`, và `UploadedFileServlet`
> kiểm **trước khi trả byte đầu tiên** — đường dẫn bắt đầu bằng `evidence/` thì chỉ admin
> mới được, còn lại **403**. Chi tiết ở [phase 2 §3.2](phase-2.md).

> **[MUST] Đây là đường cho người lạ đẩy file lên máy chủ — phải có giới hạn ở BỐN tầng.**
>
> | Tầng | Chặn gì |
> |---|---|
> | Số lượng | tối đa 3 ảnh một báo cáo |
> | Dung lượng | `UploadUtil.MAX_BYTES` = 2 MB mỗi ảnh |
> | Kiểu thật | `UploadUtil` đã sniff byte đầu file, **không tin đuôi file** |
> | Tần suất | `RateLimiter` — tối đa 3 báo cáo / phút / tài khoản |
>
> Thiếu tầng cuối thì một tài khoản viết vòng lặp gửi báo cáo là **đầy ổ đĩa máy chủ**.
> `RateLimiter` đã có sẵn từ [`ISSUE-003`](../ISSUE-003-chan-do-mat-khau-va-spam/issue.md),
> thêm một cặp `isReportSpam` / `recordReport` theo đúng khuôn `isCommentSpam` đang có.

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅. Bỏ trống = chưa xong, dù code đã viết.*

- **Bấm thử:**
- **Thử trường hợp xấu:**
- **Chạy lại test:** `scripts\test.ps1` —

---

## 6. Ghi chú

**Vì sao bảng `report_evidence` riêng chứ không phải cột `evidence_url` trên `reports`.**
Một cột thì chỉ chứa được một ảnh, và ba ảnh là nhu cầu thật *(ảnh chụp trang truyện + ảnh
chụp phần vi phạm + ảnh so sánh với bản gốc khi tố đạo văn)*. Nhét ba đường dẫn ngăn phẩy
vào một cột `VARCHAR` là đúng cái sai mà `schema.sql` đã cảnh báo ở bảng `story_tags`
*("đừng làm kiểu `tags = "tien-hiep,huyen-huyen"`")*. Bảng nối thì xoá một ảnh là xoá một
dòng, và đếm được ảnh bằng `COUNT(*)`.

**Vì sao `category` là cột trên `reports`, không phải bảng riêng.** Ngược lại với ảnh: một
báo cáo có **đúng một** loại. Quan hệ một–một thì cột là đúng, bảng nối là thừa. Dùng
`ENUM` để thêm loại sau chỉ là sửa một dòng — cùng lý lẽ với `target_type` đang có.

**Giữ nguyên cột `reason`.** Nó thành ô *mô tả thêm*, không bỏ. Báo cáo cũ đã có `reason`
mà không có `category` — phase 1 đặt mặc định `OTHER` cho chúng, nên dữ liệu cũ vẫn đọc
được và không phải sửa tay dòng nào.

**Thứ cố tình không làm:**

| Bỏ | Vì |
|---|---|
| Video hoặc PDF làm bằng chứng | File nặng gấp bội, và cần trình phát. Ảnh chụp màn hình đủ cho mọi loại vi phạm trong danh sách. |
| Tự động ẩn nội dung khi đủ N báo cáo | Nghe hay nhưng là **vũ khí**: vài tài khoản hùa nhau là gỡ được truyện của người khác mà không cần admin. Muốn làm thì phải chống lạm dụng trước — một ISSUE riêng. |
| Báo cáo người dùng *(không phải truyện/bình luận)* | `target_type` chỉ cần thêm `'USER'` vào ENUM là xong về mặt CSDL, nhưng xử lý một báo cáo *người* là chuyện khác hẳn *(cảnh cáo? khoá? bao lâu?)*. Làm khi có nhu cầu thật. |
| Gửi email báo cho người bị tố | Cần `MailSender` và cần nghĩ về chuyện tố cáo nặc danh. Ngoài phạm vi. |

---

## 6. Hai lỗi chỉ lộ ra khi kiểm tay

Cả hai đều **không** có test nào bắt được, và cả hai đều là loại "chặn thì có chặn, nhưng
chặn sai chỗ nên người dùng không hiểu gì".

### Lỗi 1 — ảnh quá cỡ trả 403 trống thay vì câu báo lỗi

Đặt `@MultipartConfig(maxFileSize = 2 MB)` cho khớp `UploadUtil.MAX_BYTES` nhìn thì gọn.
Thực tế gửi một PNG thật 2.6 MB thì người dùng nhận **trang 403 trống trơn**.

Dây chuyền: `CsrfFilter` chạy **trước** servlet và gọi `request.getParameter("_csrf")`.
Với request multipart, chỉ riêng lệnh đó đã buộc Tomcat phân tích cả thân request. File
vượt `maxFileSize` → Tomcat ném lỗi ngay tại đó → `getParameter()` trả `null` → filter
tưởng thiếu token CSRF → **403**. `ReportServlet` không bao giờ được chạy, nên câu
*"Ảnh bằng chứng quá 2 MB"* trong `UploadUtil` không bao giờ tới được ai.

**Sửa:** nâng `maxFileSize` lên 10 MB — để container làm hàng rào *chống đổ*, còn giới hạn
**người dùng thấy** thì để `UploadUtil` lo, nơi còn nói được câu tử tế.

> **Còn sót, chấp nhận:** file vượt cả 10 MB vẫn ra 403 trống. Đó là ca phá hoại chứ không
> phải người dùng lỡ tay, và quan trọng là Tomcat ngừng đọc chứ không nuốt hết vào RAM.

Sửa kèm: sáu thông báo lỗi của `ReportServlet` trước đó dùng attribute `flash`, mà layout
vẽ `flash` bằng khung **xanh** `panel-ok`. Người dùng thấy một khung xanh báo lỗi — trông
như đã gửi xong. Đổi hết sang `flashError` *(khung đỏ)*, đúng quy ước `UserServlet` đang dùng.

### Lỗi 2 — link tới truyện đã gỡ là link chết, kể cả với admin

`StoryServlet.detail()` trả 404 cho truyện `DELETED` **với tất cả mọi người**. Nghe thì
chặt chẽ, nhưng nó phá đúng việc admin cần làm: bình luận bị tố rất hay nằm trong truyện
vừa bị gỡ vì một báo cáo khác. Admin bấm từ trang báo cáo sang thì nhận 404 — không xem
được mình vừa gỡ cái gì, cũng không đọc được bình luận đang phải phân xử.

**Sửa:** truyện đã gỡ thì admin vào được *(kèm dải cảnh báo đỏ "Truyện này đã bị gỡ")*,
mọi người khác vẫn 404 y như cũ — không có cửa sau nào. Đồng thời **không cộng lượt xem**
cho truyện đã gỡ: chỉ admin vào được, và đó là thao tác quản trị chứ không phải đọc truyện.

| | trước | sau |
|---|:--:|:--:|
| admin | 404 | **200** + cảnh báo |
| thành viên | 404 | 404 |
| khách | 404 | 404 |
