# 🏷️ ISSUE-022: Đánh giá dài + nhãn spoiler — chấm sao xong còn được nói lý do

> **Đích:** `docs/projects/issues/ISSUE-022-danh-gia-dai-spoiler/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-022 |
| **Người làm** | N2 — Đọc & Nội dung |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày mở** | 2026-09-23 |
| **CASE liên quan** | Mở rộng chức năng chấm sao *(bảng `ratings`)* |
| **Đụng vào** | `database/schema.sql` · `dao/RatingDAO` · `dao/ReviewDAO` *(mới)* · `RatingServlet` · `AdminCommentServlet` · `views/common/story/detail.jsp` · `views/_partials/_review.jsp` *(mới)* |

---

## 1. Làm cái gì, và vì sao (Goal)

Bảng `ratings` hiện chỉ có một cột `score` từ 1 đến 5. Người đọc chấm 2 sao được, nhưng
**không nói được vì sao 2 sao** — và người đọc tiếp theo nhìn con số trung bình `3.7` thì
không biết nó đến từ đâu.

- **Hiện tại:** chấm sao là một thao tác câm. Ai muốn nói gì phải viết xuống ô bình luận,
  lẫn giữa những dòng *"hóng chương mới"*.
- **Sau khi xong:** chấm sao xong hiện ô viết cảm nhận có tiêu đề và nội dung. Bài đánh giá
  hiện thành khối riêng phía trên bình luận, sắp theo lượt bấm **Có ích**. Ai viết nội dung
  tiết lộ tình tiết thì tick **Có spoiler** — khối đó bị che mờ, bấm mới hiện.

**Vì sao nhãn spoiler là phần bắt buộc chứ không phải phần thêm cho vui.** Đánh giá một
cuốn truyện mà không được nhắc tới tình tiết thì chỉ còn viết được những câu chung chung.
Có nhãn che thì người viết nói thẳng được, mà người chưa đọc vẫn an toàn. Không có nhãn,
mục đánh giá sẽ thành chỗ nguy hiểm và người ta tự né không đọc.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Ở trang chi tiết, sau khi chấm sao thì hiện ô **"Viết cảm nhận"** với tiêu đề + nội dung.
- [ ] Gửi xong → bài đánh giá hiện ngay, kèm tên, ảnh đại diện, số sao **đã chấm**, và ngày.
- [ ] **Một người một bài đánh giá cho một truyện.** Gửi lần hai là **sửa** bài cũ, không
      đẻ bài mới. Kiểm bằng `SELECT COUNT(*) FROM reviews WHERE user_id=? AND story_id=?` → **1**.
- [ ] Số sao trong bài đánh giá **luôn khớp** với `ratings` — sửa sao thì bài đánh giá đổi theo.
- [ ] Tick **Có spoiler** → khối bị che, hiện *"Bài này có tiết lộ nội dung — bấm để xem"*.
      Bấm mới hiện. **Kiểm Ctrl+U: nội dung spoiler được phép nằm trong HTML** *(đây là che
      cho lịch sự, không phải hàng rào bảo mật — khác hẳn chương VIP ở ISSUE-020)*.
- [ ] Nút **👍 Có ích** — mỗi người bấm một lần cho một bài, bấm lại là bỏ.
- [ ] Danh sách sắp theo lượt Có ích giảm dần, bằng nhau thì mới nhất trước.
- [ ] Nhập `<b>đậm</b>` vào nội dung → hiện ra **đúng chuỗi đó**, không thành chữ đậm.
- [ ] Admin gỡ được bài đánh giá vi phạm *(xoá mềm, giống bình luận)*.
- [ ] Chưa đăng nhập → thấy các bài đánh giá nhưng **không** thấy ô viết.
- [ ] `scripts\test.ps1` — không tụt so với 101 pass.

---

## 3. Chia đợt — 2 phase

| Phase | Nội dung | Vì sao đứng riêng |
|:--:|---|---|
| [**1**](phase-1.md) | Schema: bảng `reviews` và `review_votes`, DAO | Sửa `schema.sql` — **luôn đứng riêng và làm trước** |
| [**2**](phase-2.md) | Giao diện, nút Có ích, nhãn spoiler, gỡ bài ở trang admin | Đổi trang chi tiết — thứ đang chạy đúng và đông người xem nhất |

> **[MUST] Phase 1 phải làm SAU [`ISSUE-020 phase 1`](../ISSUE-020-mo-khoa-chuong-bang-xu/phase-1.md).**
> Cả hai đều sửa `database/schema.sql`. Hai người sửa cùng lúc thì merge conflict trong
> file SQL còn dễ giải, nhưng hai máy chạy hai bản schema lệch nhau thì mất cả buổi mới
> hiểu tại sao code của mình lỗi trên máy người kia.
>
> Và theo luật ở [`issues/README`](../README.md): **N2 viết câu SQL, N1 là người commit vào
> `schema.sql`** — một cửa vào duy nhất.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên `ReviewDAO`, contract 4 tầng, URL `?action=review` | [`01-CODING §1 §2 §5`](../../../standards/01-CODING_CONVENTIONS.md) |
| Attribute `reviews`, `myReview`, **`<c:out>` cho mọi chữ người dùng nhập** | [`02-VIEW §3 §4`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Bảng `reviews`, `review_votes`, khoá ngoại, xoá mềm | [`03-DATABASE §2 §4`](../../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit `ISSUE-022 phase-N — …` | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Luật riêng:**

> **[MUST] Nội dung bài đánh giá là chữ người dùng nhập — in bằng `<c:out>`, không bao giờ
> bằng `${}`.** Đây là ô nhập dài nhất mà ISSUE này thêm vào, tức là bề mặt XSS lớn nhất.
> [`CHECKLIST.md §1`](../../../guides/CHECKLIST.md) có sẵn phép thử: viết một bài đánh giá
> tên `<b>ĐẬM</b>` — hiện ra chữ đậm là **thủng XSS**.

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅.*

- **Bấm thử:**
- **Thử trường hợp xấu:**
- **Chạy lại test:** `scripts\test.ps1` —

---

## 6. Ghi chú

**Vì sao bảng `reviews` riêng, không thêm cột `content` vào `ratings`.** Nghe thì thêm một
cột là xong. Nhưng `ratings` có khoá chính kép `(user_id, story_id)` và đang được
`rating_sum` / `rating_count` bám vào rất chặt — mọi lần chấm sao đều chạy một transaction
đụng hai bảng. Nhét thêm nội dung dài, cờ spoiler, số lượt Có ích và trạng thái ẩn/hiện vào
đó là làm nặng một bảng đang nằm trên đường nóng nhất *(lưới kho truyện đọc `rating_sum`
cho **mọi** thẻ truyện)*.

Bảng riêng thì: chấm sao vẫn nhẹ như cũ, và **chấm sao không bắt buộc phải viết cảm nhận** —
đa số người ta chỉ muốn bấm 5 sao rồi đi.

**Thứ cố tình không làm:**

| Bỏ | Vì |
|---|---|
| Trả lời dưới bài đánh giá | Đã có mục bình luận ngay bên dưới cho việc trao đổi. Thêm một luồng thảo luận thứ hai trên cùng một trang là hai chỗ phải kiểm duyệt. |
| Chấm theo nhiều tiêu chí *(cốt truyện / văn phong / nhân vật)* | Ba con số thì người ta lười chấm, và trung bình của ba số vô nghĩa hơn một số. |
| Báo cáo bài đánh giá vi phạm | Bảng `reports` có `target_type` là ENUM, thêm `'REVIEW'` là xong — nhưng đó là việc của `ReportServlet`, để dành khi có nhu cầu thật. |
