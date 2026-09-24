# 🏷️ ISSUE-020: Mở khoá chương bằng xu — cho ví xu có chỗ để tiêu

> **Đích:** `docs/projects/issues/ISSUE-020-mo-khoa-chuong-bang-xu/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-020 |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày mở** | 2026-09-23 |
| **CASE liên quan** | Mở rộng [`ISSUE-008`](../ISSUE-008-ung-ho-tac-gia-xu-ao/issue.md) — Ví xu ảo |
| **Đụng vào** | `database/schema.sql` · `WalletServlet` · `ChapterServlet` · `dao/WalletDAO` · `dao/ChapterDAO` · `dao/UnlockDAO` *(mới)* · `views/user/chapter/form.jsp` · `views/common/chapter/toc.jsp` · `views/common/chapter/read.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

[`ISSUE-008`](../ISSUE-008-ung-ho-tac-gia-xu-ao/issue.md) đã dựng xong ví xu và hai bảng
`wallets` / `transactions`. Nhưng xu hiện chỉ có **một** đường ra: tặng tác giả. Tặng xong
là hết — người tặng không nhận lại gì, nên sau vài lần thì không ai tặng nữa và cái ví trở
thành một con số nằm im trong trang hồ sơ.

- **Hiện tại:** tác giả viết 40 chương, đăng miễn phí hết, không có cách nào cho độc giả
  *trả lại* gì ngoài một nút tặng mang tính hảo tâm.
- **Sau khi xong:** tác giả đánh dấu một chương là **VIP** kèm giá xu. Độc giả trả xu để mở
  khoá **vĩnh viễn** chương đó, xu chuyển thẳng vào ví tác giả. Đây là vòng tuần hoàn khép
  kín: có chỗ tiêu xu thì mới có lý do nạp xu, và tác giả có lý do viết tiếp.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Ở form thêm/sửa chương có ô **"Chương VIP"** + ô **giá xu**. Bỏ tick thì giá bị vô hiệu.
- [ ] Mục lục truyện hiện biểu tượng 🔒 kèm giá ở các chương VIP **chưa mở khoá**, và mở
      bình thường ở chương đã mở.
- [ ] Độc giả mở chương VIP chưa trả → thấy màn hình chặn với giá và số dư, **không thấy
      một chữ nào của nội dung chương** *(kể cả trong HTML — bấm Ctrl+U kiểm)*.
- [ ] Bấm **Mở khoá** → trừ đúng số xu ở ví người đọc, **cộng đúng** vào ví tác giả, ghi
      một dòng `transactions`, và hiện ngay nội dung chương.
- [ ] Mở lại chương đó **lần thứ hai, thứ ba** → vào thẳng, **không trừ xu lần nữa**.
- [ ] Số dư không đủ → báo *"Bạn cần thêm N xu"*, **không trừ gì**, không tạo giao dịch.
- [ ] **Tác giả đọc chương VIP của chính mình** → vào thẳng, không bị đòi xu.
- [ ] **Admin** đọc chương VIP bất kỳ → vào thẳng *(để còn kiểm duyệt được)*.
- [ ] Gọi thẳng `/chapter?action=read&id=<chương VIP>` bằng URL → **vẫn bị chặn**.
- [ ] Gọi `/download?storyId=<truyện có chương VIP>` → file `.txt` **không chứa** chương
      chưa mở khoá.
- [ ] Bấm nút Mở khoá **hai lần thật nhanh** → chỉ trừ xu **một lần**.
- [ ] `scripts\test.ps1` — không tụt so với 101 pass.

---

## 3. Chia đợt — 2 phase

| Phase | Nội dung | Vì sao đứng riêng |
|:--:|---|---|
| [**1**](phase-1.md) | Schema: bảng `chapter_unlocks`, hai cột VIP trên `chapters` | Sửa `schema.sql` — **luôn đứng riêng và làm trước** |
| [**2**](phase-2.md) | Luồng mở khoá, hàng rào ở `ChapterServlet`, giao diện | Đụng đường đọc chương — thứ đang chạy đúng cho mọi người |

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên `UnlockDAO`, contract 4 tầng, URL `?action=unlock` | [`01-CODING §1 §2 §5`](../../../standards/01-CODING_CONVENTIONS.md) |
| Attribute `isLocked`, `unlockPrice`, `<c:out>` | [`02-VIEW §3 §4`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Bảng `chapter_unlocks`, kiểu cột, khoá ngoại, **transaction** | [`03-DATABASE §2 §4`](../../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit `ISSUE-020 phase-N — …` | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Luật riêng — tiền phải đúng, không được "gần đúng":**

> **[MUST] Trừ ví người đọc, cộng ví tác giả và ghi `transactions` nằm trong MỘT
> transaction SQL.** Ba câu lệnh rời nhau thì mất điện giữa chừng là xu bốc hơi — trừ của
> người này mà không tới người kia, và **không có cách nào biết** đã mất bao nhiêu.
> `RatingDAO` trong repo này đã có sẵn khuôn mẫu đúng, chép theo *(xem chú thích cột
> `rating_sum` trong `schema.sql`)*.
>
> **[NEVER] tin giá xu do trình duyệt gửi lên.** Giá phải đọc từ `chapters.coin_price`
> trong CSDL ngay trong câu `UPDATE`. Gửi `price` lên form rồi trừ theo nó là để người ta
> sửa thành `0` — đúng loại lỗi *"tin client"* đã phải đi vá ở
> [`bug-001`](../../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan/bug.md).

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅.*

- **Bấm thử:**
- **Thử trường hợp xấu:**
- **Chạy lại test:** `scripts\test.ps1` —

---

## 6. Ghi chú

**Đây là xu ảo, không phải tiền thật — và phải giữ nguyên như vậy.** Không nạp bằng thẻ,
không rút ra, không quy đổi. Gắn cổng thanh toán vào là đồ án chạm tới tiền thật: phải lo
hoàn tiền, hoá đơn, tranh chấp và pháp lý — không có thứ nào trong số đó nằm trong đề bài.
Xu do admin cấp hoặc do hệ thống thưởng, và nói rõ điều đó ở
[`page/rules.jsp`](../../../../src/main/webapp/WEB-INF/views/common/page/rules.jsp).

**Thứ cố tình không làm:**

| Bỏ | Vì |
|---|---|
| Mở khoá cả truyện một lần | Phải nghĩ tiếp: truyện ra thêm chương VIP sau đó thì sao? Mở khoá từng chương không có câu hỏi đó. |
| Giảm giá / mã khuyến mãi | Thêm một tầng tính giá nữa vào chỗ đang đụng tiền. Làm sau nếu thật sự cần. |
| Hoàn xu | Hoàn thì phải khoá lại chương đã đọc — vô nghĩa, người ta đọc xong rồi. |
