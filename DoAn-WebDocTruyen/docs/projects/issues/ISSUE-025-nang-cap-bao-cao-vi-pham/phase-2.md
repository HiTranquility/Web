# 🚀 ISSUE-025 — Phase 2: Form chọn loại, tải ảnh bằng chứng, và link bấm thẳng tới nội dung

> **Đích:** `docs/projects/issues/ISSUE-025-nang-cap-bao-cao-vi-pham/phase-2.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) |
| **Đợt** | Phase 2 / tổng 2 đợt |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | ✅ Xong |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `ReportServlet` · `AdminReportServlet` · `UploadedFileServlet` · `util/UploadUtil` · `util/RateLimiter` · `dao/ReportDAO` · `views/common/story/detail.jsp` · `views/_partials/_comment.jsp` · `views/admin/reports.jsp` · `assets/css/components.css` · `assets/js/report-form.js` *(mới)* |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** ô chọn loại vi phạm trong form · tải tối đa 3 ảnh bằng chứng · trang
  admin hiện nhãn loại, bộ lọc, và ảnh thu nhỏ · bấm thẳng tới đúng bình luận bị báo cáo ·
  chặn người lạ xem ảnh tố cáo.
- **Để đợt sau:** không có. Đây là đợt cuối của ISSUE-025.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách:** đợt này mở một **đường nhận file từ người dùng** — bề mặt tấn công mới, và
là loại nguy hiểm nhất trong ba loại mà dự án đang có *(nhập chữ · nhập ảnh bìa · nhập ảnh
tố cáo)*. Ảnh bìa truyện thì chỉ tác giả tải và ai cũng được xem; ảnh tố cáo thì **ai
đăng nhập cũng tải được** và **không phải ai cũng được xem**. Hai điều đó cộng lại là bốn
hàng rào ở §3.2 và §3.3.

Nó cũng **sửa `UploadedFileServlet`** — một servlet đang phục vụ đúng cho mọi ảnh bìa của
cả site. Sai một điều kiện ở đó là ảnh bìa biến mất toàn trang.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [phase 1](phase-1.md) ✅ — cần cột `category`, bảng
  `report_evidence`, và `link_story_id` trong câu JOIN.
- **Xong đợt này mới mở khoá được:** không có. Đây là đợt cuối.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | `RateLimiter` thêm `isReportSpam(userId)` / `recordReport(userId)` theo khuôn `isCommentSpam` đang có | `util/RateLimiter.java` | ☐ |
| 2 | `UploadUtil.saveEvidence(Part, ctx)` — lưu vào `uploads/evidence/YYYY/MM/`, dùng lại phần sniff và giới hạn 2 MB sẵn có | `util/UploadUtil.java` | ☐ |
| 3 | `ReportServlet` nhận `multipart/form-data`, đọc `category`, lặp tối đa 3 `Part` *(§3.1)* | `ReportServlet` | ☐ |
| 4 | **`UploadedFileServlet` chặn `evidence/` với người không phải admin** *(§3.2)* | `UploadedFileServlet` | ☐ |
| 5 | Form báo cáo truyện: ô chọn loại + ô chọn ảnh + xem trước | `views/common/story/detail.jsp` · `assets/js/report-form.js` | ☐ |
| 6 | Form báo cáo bình luận: **chỉ 6 loại**, bỏ `PLAGIARISM` và `WRONG_INFO` | `views/_partials/_comment.jsp` | ☐ |
| 7 | Trang admin: nhãn loại có màu · bộ lọc theo loại · ảnh thu nhỏ bấm phóng to | `AdminReportServlet` · `views/admin/reports.jsp` | ☐ |
| 8 | **Link sâu tới bình luận** + neo `id="comment-N"` ở nơi hiển thị bình luận *(§3.3)* | `views/admin/reports.jsp` · `views/_partials/_comment.jsp` | ☐ |
| 9 | Xoá file trên ổ đĩa khi báo cáo bị xoá *(§3.4)* | `dao/ReportDAO` · `AdminReportServlet` | ☐ |
| 10 | Test `ReportUploadTest` — 7 ca ở §5 | `src/test/java/truyen/ReportUploadTest.java` | ☐ |

### 3.1 `ReportServlet` — thứ tự kiểm, dừng ở chỗ đầu tiên sai

```text
1. chưa đăng nhập                        -> 403            (AuthFilter lo, vẫn phải kiểm)
2. CSRF sai                              -> 403            (CsrfFilter lo)
3. RateLimiter.isReportSpam(userId)      -> báo "gửi quá nhanh", DỪNG
4. category rỗng / không thuộc 8 giá trị -> báo lỗi, DỪNG
5. category == OTHER mà reason rỗng      -> báo lỗi, DỪNG
6. đếm số Part ảnh > 3                   -> báo lỗi, DỪNG  ← ĐẾM TRƯỚC, LƯU SAU
7. với từng ảnh: UploadUtil.saveEvidence  (tự chặn >2MB và file không phải ảnh)
8. ghi reports + report_evidence trong MỘT transaction
9. RateLimiter.recordReport(userId)
```

> **[MUST] Bước 6 phải đếm TRƯỚC khi lưu file nào.** Lưu dần rồi mới phát hiện thừa là đã
> có 3 file rác nằm trên ổ đĩa mà không dòng CSDL nào trỏ tới — **không ai dọn được nữa**,
> vì không biết file nào là rác.
>
> Cùng lý do, bước 8 phải là **một transaction**: ghi ảnh mà rớt lúc ghi `reports` thì lại
> ra file mồ côi. Nếu transaction rollback thì **phải xoá luôn file vừa lưu** — CSDL
> rollback được, ổ đĩa thì không.

### 3.2 Ảnh tố cáo không được để ai cũng xem

`UploadedFileServlet` hiện map `/uploads/*` và trả **mọi** file trong thư mục upload cho
**bất kỳ ai**. Đúng cho ảnh bìa truyện — ảnh bìa vốn để khoe. **Sai hoàn toàn** cho ảnh tố
cáo: nó có thể là ảnh chụp tin nhắn riêng, thông tin cá nhân của người bị tố, hoặc chính
nội dung phản cảm đang bị báo cáo. Ai đoán được tên file là xem được hết.

```java
// UploadedFileServlet — thêm NGAY SAU đoạn chống path traversal đang có,
// và TRƯỚC khi ghi byte đầu tiên ra response.
String rel = root.relativize(target).toString().replace('\\', '/');
if (rel.startsWith("evidence/")) {
    User me = ServletHelper.currentUser(request);
    if (me == null || !me.isAdmin()) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return;
    }
}
```

> **[NEVER] dựa vào chuyện "tên file ngẫu nhiên nên không ai đoán được".** Đó là bảo mật
> bằng cách giấu, và nó hỏng ngay khi một admin dán link ảnh vào chat nhóm, hoặc khi
> `Referer` rò ra ngoài. Hàng rào phải là một câu `if` ở máy chủ.
>
> Đoạn kiểm này đặt **sau** phần chống path traversal đang có *(`UploadedFileServlet:51–61`)*,
> không phải trước — phải chuẩn hoá đường dẫn xong mới so tiền tố được, không thì
> `/uploads/x/../evidence/a.jpg` lách qua.

### 3.3 Bấm thẳng tới bình luận bị báo cáo

Phase 1 đã cho `Report` mang `storyId`. Giờ dựng link:

```jsp
<c:choose>
    <%-- Nội dung đã bị xoá hẳn: cả hai LEFT JOIN đều hụt -> storyId = 0 --%>
    <c:when test="${r.storyId == 0}">
        <span class="quote is-gone">Nội dung không còn tồn tại</span>
    </c:when>

    <c:when test="${r.story}">
        <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${r.storyId}">
            <c:out value="${r.targetTitle}"/></a>
    </c:when>

    <c:otherwise>
        <%-- Bình luận: mở đúng truyện rồi nhảy tới đúng khối bình luận. --%>
        <a href="${pageContext.request.contextPath}/story?action=detail&amp;id=${r.storyId}#comment-${r.targetId}">
            <span class="quote"><c:out value="${r.targetTitle}"/>…</span>
        </a>
    </c:otherwise>
</c:choose>
```

Để dấu `#comment-N` có chỗ mà nhảy tới, `_partials/_comment.jsp` phải có neo:

```jsp
<article class="comment" id="comment-${cm.id}">
```

Và một chút CSS để admin nhìn phát thấy ngay mình đang tìm cái nào:

```css
/* Bình luận được trỏ tới từ link báo cáo — nháy sáng vài giây rồi tắt. */
.comment:target {
    outline: 2px solid var(--ember);
    background: var(--ember-ghost);
    scroll-margin-top: 80px;   /* header sticky cao 68px, chừa chỗ kẻo bị che */
}
```

> **`scroll-margin-top` là dòng dễ quên nhất ở đây.** Không có nó thì trình duyệt cuộn
> đúng tới bình luận, nhưng thanh header dính *(`position: sticky`, cao 68px)* **che mất**
> — admin thấy trang nhảy rồi tưởng link hỏng.

### 3.4 CSDL xoá dòng, ổ đĩa thì không

`report_evidence` có `ON DELETE CASCADE`, nên xoá một báo cáo là dòng ảnh biến mất. Nhưng
**file trên ổ đĩa vẫn nằm đó mãi mãi**. Trước khi xoá báo cáo, đọc danh sách `file_path`
ra, xoá dòng, rồi mới xoá file.

Thứ tự đó có chủ ý: xoá file trước mà CSDL lỗi thì dòng còn nhưng ảnh mất — trang admin
hiện ảnh vỡ. Xoá dòng trước mà file lỗi thì chỉ còn file rác, ghi log là đủ.

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `scripts\test.ps1` | *(điền)* |
| Biên dịch | *(điền)* |
| Mở `/admin/report` | *(điền: mấy báo cáo, link truyện bấm được không)* |
| Gửi báo cáo từ trang truyện | *(điền)* |
| Gửi báo cáo một bình luận | *(điền)* |
| **Mở một trang truyện có ảnh bìa** | *(điền — ảnh bìa hiện đúng không)* |
| Dung lượng thư mục `uploads/` | *(điền MB — cuối đợt so lại xem có file rác không)* |

**[GOTCHA]** Dòng *"ảnh bìa"* bắt buộc: đợt này sửa `UploadedFileServlet`, servlet đang
phục vụ **mọi** ảnh bìa của cả site. Một điều kiện sai là ảnh bìa mất sạch toàn trang, và
đó là thứ dễ không nhận ra nếu chỉ kiểm mỗi trang báo cáo.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `scripts\test.ps1` — không tụt, có `ReportUploadTest`
- [ ] **Bằng tay — đường chính:**
  1. Đăng nhập `thuytien`, mở một truyện, bấm 🚩 **Báo cáo vi phạm**
  2. Chọn loại **"Đạo văn"**, viết mô tả, đính **2 ảnh** → thấy xem trước cả 2 → Gửi
  3. Đăng nhập `admin`, mở `/admin/report` → báo cáo hiện **nhãn "Đạo văn"** màu vàng, có
     **2 ảnh thu nhỏ**; bấm ảnh → phóng to
  4. Bấm tên truyện → mở đúng trang truyện
  5. Báo cáo một **bình luận**, rồi ở trang admin bấm vào đoạn trích → mở đúng truyện,
     **cuộn tới đúng bình luận**, bình luận được tô sáng **và không bị header che**
  6. Lọc theo loại **"Nội dung người lớn"** → chỉ còn báo cáo loại đó
- [ ] **Thử trường hợp xấu — tám ca:**
  1. 🔴 Đăng nhập `mocmien` *(không phải admin)*, dán thẳng URL một ảnh bằng chứng →
     **403**. Đăng xuất rồi dán lại → **403**
  2. Chọn 4 ảnh → chặn, báo "Tối đa 3 ảnh", và `uploads/evidence/` **không có file mới nào**
  3. Ảnh **3 MB** → chặn, báo rõ giới hạn 2 MB, **không lưu**
  4. Đổi tên `virus.exe` thành `anh.jpg` rồi gửi → **từ chối**
  5. Không chọn loại → không gửi được
  6. Chọn **"Khác"** mà bỏ trống mô tả → không gửi được
  7. Gửi **5 báo cáo trong 1 phút** → chặn từ cái thứ 4
  8. Báo cáo một bình luận rồi **xoá hẳn bình luận đó** trong CSDL → trang admin hiện
     *"Nội dung không còn tồn tại"*, **không** ra link chết, **không** lỗi 500
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:**
      **ảnh bìa truyện vẫn hiện ở trang chủ, kho truyện và trang chi tiết** *(quan trọng
      nhất — xem §4)* · tải ảnh bìa mới khi sửa truyện · đánh dấu báo cáo đã xử lý · bỏ qua
      báo cáo · lọc theo trạng thái
- [ ] So lại dung lượng `uploads/` với §4: tăng **đúng bằng** tổng ảnh vừa tải, **không có
      file mồ côi** nào

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Commit: `ISSUE-025 phase-2 — phan loai vi pham, anh bang chung, link toi noi dung`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Đây là đợt cuối → quay lại [`issue.md`](issue.md) điền §5, đổi Trạng thái ✅
- [ ] Thêm một mục vào [`page/rules.jsp`](../../../../src/main/webapp/WEB-INF/views/common/page/rules.jsp):
      liệt kê 8 loại vi phạm — người ta phải biết cái gì bị cấm thì mới báo cáo đúng loại
- [ ] Thêm một dòng vào [`page/guide.jsp`](../../../../src/main/webapp/WEB-INF/views/common/page/guide.jsp):
      cách gửi báo cáo kèm ảnh

---

## 7. Còn vướng / chưa làm

Form, upload, hàng rào 403 và link sâu — xong 2026-09-24.

Một thứ **cố tình chưa làm**: đường xoá báo cáo kèm dọn file trên đĩa.
`ReportDAO.findEvidencePaths()` và `UploadUtil.deleteEvidence()` đã viết sẵn,
nhưng **không có chỗ nào trong web xoá báo cáo** — admin chỉ đổi trạng thái.
Viết thêm một luồng xoá chưa ai gọi là đẻ code chết. Khi nào có nút xoá thật
thì hai hàm đó đã nằm sẵn đó.
