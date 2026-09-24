# 🚀 ISSUE-022 — Phase 2: Giao diện đánh giá, nút Có ích, nhãn spoiler

> **Đích:** `docs/projects/issues/ISSUE-022-danh-gia-dai-spoiler/phase-2.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) |
| **Đợt** | Phase 2 / tổng 2 đợt |
| **Người làm** | N2 — Đọc & Nội dung |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `RatingServlet` · `StoryServlet` · `AdminCommentServlet` · `dao/ReviewDAO` · `views/common/story/detail.jsp` · `views/_partials/_review.jsp` *(mới)* · `views/admin/comments.jsp` · `assets/css/components.css` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** ô viết cảm nhận sau khi chấm sao · khối danh sách bài đánh giá ở trang
  chi tiết · nút 👍 Có ích · nhãn che spoiler · admin gỡ được bài vi phạm.
- **Để đợt sau:** không có. Đây là đợt cuối của ISSUE-022.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách:** đợt này **đổi trang `story/detail.jsp`** — trang đông người xem nhất của
web, và đang mang sẵn bốn thứ chạy đúng: mục lục chương, khối bình luận, chấm sao, và nút
chia sẻ của ISSUE-018. Chen một khối mới vào giữa đó là việc dễ làm vỡ thứ khác.

Nó cũng là đợt duy nhất có bề mặt **XSS mới**: một ô `textarea` 4000 ký tự. Để riêng thì
`git diff` của đợt này chỉ có một trang để soi kỹ.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [phase 1](phase-1.md) ✅ — cần hai bảng và `ReviewDAO`.
- **Xong đợt này mới mở khoá được:** không có.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | `StoryServlet.detail()` nạp thêm `reviews` và `myReview` vào request | `StoryServlet` | ☐ |
| 2 | `RatingServlet` thêm `action=review` *(gửi/sửa bài)* và `action=vote` | `RatingServlet` | ☐ |
| 3 | Mảnh `_review.jsp` — một bài đánh giá, dùng lại `_avatar.jsp` và `_rating-stars.jsp` đã có | `views/_partials/_review.jsp` | ☐ |
| 4 | Khối danh sách + ô viết, chèn vào `detail.jsp` **giữa** chấm sao và bình luận | `views/common/story/detail.jsp` | ☐ |
| 5 | CSS cho khối đánh giá và lớp che spoiler | `assets/css/components.css` | ☐ |
| 6 | Nhãn spoiler — che bằng CSS + một nút bấm, **không** cần gọi lại server *(§3.2)* | `_review.jsp` · `components.css` | ☐ |
| 7 | Tab **Đánh giá** ở trang quản trị bình luận, có nút Gỡ | `AdminCommentServlet` · `views/admin/comments.jsp` | ☐ |
| 8 | Test `ReviewFlowTest` — 5 ca ở §5 | `src/test/java/truyen/ReviewFlowTest.java` | ☐ |

### 3.1 Chỗ đặt khối đánh giá trong `detail.jsp`

```text
┌─ Thông tin truyện · tag · nút Đọc / Tải / Chia sẻ
├─ ★★★★☆ chấm sao          ← đang có
│   └─ [Viết cảm nhận ▾]    ← MỚI: chỉ hiện khi đã chấm sao
├─ Tab: [Mục lục] [Đánh giá (5)] [Bình luận (7)]   ← thêm MỘT tab, tab cũ giữ nguyên
│   └─ khối bài đánh giá, sắp theo Có ích          ← MỚI
└─ …
```

Trang đã có sẵn cơ chế tab *(Mục lục / Bình luận)*. **Thêm một tab vào cơ chế đó**, đừng
dựng khối rời phía dưới — dựng rời là trang dài thêm một màn hình và người ta không cuộn tới.

> **Ô viết chỉ hiện sau khi đã chấm sao.** Không phải để làm khó, mà vì bài đánh giá không
> lưu điểm sao riêng *(xem lý lẽ ở [phase 1 §3.1](phase-1.md))* — nó JOIN sang `ratings`
> để lấy. Chưa có dòng `ratings` thì bài đánh giá không có sao để hiện.

### 3.2 Nhãn spoiler — che ở giao diện, KHÔNG che ở máy chủ

Đây là chỗ dễ nhầm với chương VIP của [`ISSUE-020`](../ISSUE-020-mo-khoa-chuong-bang-xu/issue.md).
Hai việc **ngược nhau**, và nhầm lẫn theo hướng nào cũng sai:

| | Chương VIP (ISSUE-020) | Spoiler (việc này) |
|---|---|---|
| Nội dung có được vào HTML? | **Tuyệt đối không** | **Được** |
| Vì sao | Hàng rào **tiền**. Vào HTML là đọc chùa | Chỉ là **phép lịch sự**. Người xem tự chọn |
| Che bằng | Máy chủ không gửi | CSS `filter: blur()` + một nút |
| Bấm để xem | Gọi server, trừ xu | JS local, không gọi server |

Làm spoiler theo kiểu VIP *(gọi server mỗi lần bấm)* là tốn một request cho một việc không
cần bảo mật. Làm VIP theo kiểu spoiler *(gửi hết rồi che CSS)* là **thủng hàng rào tiền**.

```html
<div class="review-body is-spoiler">
    <button type="button" class="spoiler-reveal">
        Bài này có tiết lộ nội dung — bấm để xem
    </button>
    <div class="spoiler-content"><c:out value="${r.content}"/></div>
</div>
```

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `scripts\test.ps1` | *(điền)* |
| Biên dịch | *(điền)* |
| Mở `/story?action=detail&id=1` — thời gian tải | *(điền số ms, tab Network)* |
| Bấm tab Mục lục / Bình luận | *(cả hai đổi đúng?)* |
| Chấm sao · gửi bình luận · bấm Chia sẻ | *(cả ba chạy?)* |

**[GOTCHA]** Ba dòng cuối là **thứ đang chạy đúng** mà đợt này có nguy cơ làm hỏng — cả
bốn thứ đó nằm trên cùng một trang với khối mới.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `scripts\test.ps1` — không tụt, có `ReviewFlowTest`
- [ ] **Bằng tay — đường chính:**
  1. Đăng nhập `thuytien`, mở một truyện, chấm **4 sao**
  2. Ô **Viết cảm nhận** hiện ra → điền tiêu đề + nội dung → Gửi
  3. Bài hiện ngay trong tab **Đánh giá**, kèm ảnh đại diện và **4 sao**
  4. Sửa điểm thành **2 sao** → tải lại → bài đánh giá hiện **2 sao** *(JOIN đúng)*
  5. Gửi lại bài lần hai → **sửa bài cũ**;
     `SELECT COUNT(*) FROM reviews WHERE user_id=5` → **1**
  6. Đăng nhập tài khoản khác, bấm 👍 → `helpful_count` = 1; bấm lại → về 0
- [ ] **Thử trường hợp xấu — sáu ca:**
  1. 🔴 Nội dung `<b>ĐẬM</b>` → hiện ra **đúng chuỗi đó**, không thành chữ đậm
     *(phép thử XSS ở [`CHECKLIST §1`](../../../guides/CHECKLIST.md))*
  2. Tiêu đề rỗng · nội dung rỗng → báo lỗi tử tế, **không** ghi vào CSDL
  3. Nội dung **quá 4000 ký tự** → chặn ở cả trình duyệt và máy chủ
  4. Chưa đăng nhập → thấy danh sách, **không** thấy ô viết; gọi thẳng
     `/rating?action=review` → bị đá về đăng nhập
  5. Bấm 👍 **hai lần thật nhanh** → `helpful_count` chỉ đổi **một** bậc
  6. Bài có spoiler → mặc định che; **Ctrl+U thấy nội dung là ĐÚNG** *(xem §3.2 — đây
     không phải lỗi)*
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** tab Mục lục · tab Bình luận · chấm sao ·
      trả lời bình luận · nút Chia sẻ · nút Tải `.txt` — **sáu thứ, cùng một trang**
- [ ] Đo lại thời gian tải trang chi tiết, so với §4. Tăng bao nhiêu ms: ______

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở Baseline đo lại **bằng hoặc tốt hơn** *(thời gian tải được phép tăng, nhưng
      phải **ghi số thật** ra, không lờ đi)*
- [ ] Commit: `ISSUE-022 phase-2 — giao dien danh gia, nut Co ich, nhan spoiler`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Đây là đợt cuối → quay lại [`issue.md`](issue.md) điền §5, đổi Trạng thái ✅
- [ ] Thêm một dòng vào [`page/rules.jsp`](../../../../src/main/webapp/WEB-INF/views/common/page/rules.jsp):
      bài đánh giá cũng chịu nội quy như bình luận

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
