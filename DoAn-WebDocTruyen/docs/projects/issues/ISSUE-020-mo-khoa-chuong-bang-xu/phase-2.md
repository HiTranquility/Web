# 🚀 ISSUE-020 — Phase 2: Luồng mở khoá và hàng rào chặn đọc

> **Đích:** `docs/projects/issues/ISSUE-020-mo-khoa-chuong-bang-xu/phase-2.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) |
| **Đợt** | Phase 2 / tổng 2 đợt |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `ChapterServlet` · `WalletServlet` · `DownloadServlet` · `dao/WalletDAO` · `dao/UnlockDAO` · `views/user/chapter/form.jsp` · `views/common/chapter/toc.jsp` · `views/common/chapter/read.jsp` · `views/common/chapter/_locked.jsp` *(mới)* · `assets/css/layout-reader.css` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** tác giả đặt chương VIP kèm giá · mục lục hiện 🔒 · độc giả bị chặn ở
  chương chưa trả · nút Mở khoá trừ xu trong **một** transaction · `.txt` không kèm chương
  chưa mở.
- **Để đợt sau:** không có. Đây là đợt cuối của ISSUE-020.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách:** đợt này **đổi thứ đang chạy đúng** — đường đọc chương, thứ mà mọi người
dùng của web đều đi qua mỗi ngày. Nó cũng là đợt duy nhất đụng vào **số dư ví**. Trộn với
phase 1 thì `git diff` gộp schema với logic tiền, và khi xu trừ sai sẽ phải soi cả hai.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [phase 1](phase-1.md) ✅ — cần bảng và `UnlockDAO`.
- **Xong đợt này mới mở khoá được:** [`ISSUE-023`](../ISSUE-023-admin-vi-xu-nhat-ky/issue.md)
  có mục *"doanh thu theo chương"* — cần dữ liệu từ đợt này mới có gì mà hiện.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Form chương: ô tick **Chương VIP** + ô **giá xu**, bỏ tick thì vô hiệu ô giá | `views/user/chapter/form.jsp` | ☐ |
| 2 | `ChapterServlet` lưu hai trường mới — **kiểm giá ở máy chủ**, không tin form *(§3.2)* | `ChapterServlet` | ☐ |
| 3 | **Hàng rào đọc** trong `ChapterServlet.read()` theo đúng thứ tự ở §3.1 | `ChapterServlet` | ☐ |
| 4 | Mảnh `_locked.jsp` — hiện giá, số dư, nút Mở khoá hoặc lời nhắc đăng nhập | `views/common/chapter/_locked.jsp` | ☐ |
| 5 | `WalletServlet` thêm `action=unlock` — **một transaction** cho cả ba việc *(§3.3)* | `WalletServlet` · `dao/WalletDAO` | ☐ |
| 6 | Mục lục hiện 🔒 + giá ở chương VIP chưa mở | `views/common/chapter/toc.jsp` | ☐ |
| 7 | `DownloadServlet` **bỏ qua** chương chưa mở khoá của người đang tải | `DownloadServlet` | ☐ |
| 8 | Test `UnlockFlowTest` — 6 ca ở §5 | `src/test/java/truyen/UnlockFlowTest.java` | ☐ |

### 3.1 Hàng rào đọc — dừng ở chỗ đầu tiên đúng

```text
1. chương không phải VIP                 -> CHO ĐỌC
2. chưa đăng nhập                        -> _locked.jsp, mời đăng nhập
3. người đọc LÀ tác giả của truyện        -> CHO ĐỌC   (không bắt tự mua của mình)
4. người đọc là ADMIN                    -> CHO ĐỌC   (để còn kiểm duyệt được)
5. UnlockDAO.hasUnlocked(userId, chId)   -> CHO ĐỌC
6. còn lại                               -> _locked.jsp, hiện giá và số dư
```

> **[NEVER] nạp nội dung chương rồi mới ẩn bằng CSS.** Nội dung phải **không bao giờ đi
> vào HTML** ở nhánh 6. Ẩn bằng CSS thì bấm Ctrl+U là đọc miễn phí cả chương — hàng rào
> nhìn thì có mà thực tế không tồn tại. Tiêu chí nghiệm thu ở
> [`issue.md §2`](issue.md) có đúng một dòng kiểm chuyện này, đừng bỏ.

### 3.2 Giá do ai quyết

| Nơi | Được tin? |
|---|---|
| `chapters.coin_price` đọc từ CSDL | ✅ Đây là nguồn duy nhất |
| `request.getParameter("price")` lúc **mở khoá** | ⛔ **Không.** Sửa thành `0` là đọc chùa |
| `request.getParameter("coinPrice")` lúc **tác giả lưu chương** | ✅ nhưng phải chặn: số nguyên, `1..10000`, và người lưu đúng là tác giả |

Đây là cùng một nguyên tắc đã phải đi vá ở
[`bug-001`](../../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan/bug.md): **thứ
gì trình duyệt gửi lên thì người dùng sửa được.**

### 3.3 Ba việc, một transaction

```java
// WalletDAO.unlockChapter(...) — khung, không phải code hoàn chỉnh
conn.setAutoCommit(false);
try {
    // 1. Trừ ví người đọc. Điều kiện balance >= ? nằm TRONG câu UPDATE:
    //    kiểm trước rồi trừ sau thì hai request song song đều thấy đủ tiền.
    int rows = updateBalance(conn, readerId, -price, /* requireAtLeast */ price);
    if (rows == 0) { conn.rollback(); return InsufficientFunds; }

    // 2. Cộng ví tác giả.
    updateBalance(conn, authorId, +price, 0);

    // 3. Ghi vé và ghi sổ. Khoá chính kép của chapter_unlocks là thứ chặn
    //    lượt bấm thứ hai — bắt SQLIntegrityConstraintViolationException
    //    ở đây và coi như "đã mở rồi", KHÔNG phải lỗi.
    insertUnlock(conn, readerId, chapterId, price);
    insertTransaction(conn, readerId, authorId, price, "UNLOCK_CHAPTER");

    conn.commit();
} catch (SQLException e) {
    conn.rollback();
    throw e;
} finally {
    conn.setAutoCommit(true);
}
```

> **Vì sao `balance >= ?` phải nằm trong `UPDATE`.** Bấm hai lần thật nhanh với số dư vừa
> đủ một lần: hai request cùng đọc "còn 50 xu", cùng thấy đủ, cùng trừ → số dư thành **−50**.
> Đưa điều kiện vào `UPDATE ... WHERE balance >= ?` thì CSDL khoá dòng, request thứ hai
> trả `rows = 0` và tự rollback.

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `scripts\test.ps1` | *(điền)* |
| Biên dịch | *(điền)* |
| Đọc một chương bất kỳ | *(điền)* |
| Tải `.txt` một truyện | *(điền: mấy KB, tiếng Việt đúng không)* |
| `SELECT id, balance FROM wallets;` | *(chép nguyên bảng ra đây — cuối đợt so lại)* |

**[GOTCHA]** Dòng cuối bắt buộc. Đợt này đụng số dư; không chép số dư trước thì lúc nghi
ngờ xu bị trừ sai sẽ **không có gì để so**.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `scripts\test.ps1` — không tụt, có `UnlockFlowTest`
- [ ] **Bằng tay — đường chính:**
  1. Đăng nhập `mocmien`, đặt chương 3 của truyện mình thành VIP giá 50 xu
  2. Đăng xuất, đăng nhập `thuytien`, admin cấp cho ví 100 xu
  3. Mở mục lục → chương 3 có 🔒 và ghi "50 xu"
  4. Bấm vào → màn hình chặn, **Ctrl+U không thấy một chữ nội dung nào**
  5. Bấm Mở khoá → hiện chương ngay; ví còn **50**; ví `mocmien` **+50**
  6. Tải lại trang chương đó → vào thẳng, ví **vẫn 50**
- [ ] **Thử trường hợp xấu — bảy ca:**
  1. Số dư 30, giá 50 → báo thiếu 20 xu, **ví không đổi**, `transactions` **không có dòng mới**
  2. Bấm Mở khoá **hai lần liên tiếp thật nhanh** → chỉ **một** dòng `chapter_unlocks`, trừ **một** lần
  3. Sửa `price` trong request thành `0` rồi gửi → **vẫn trừ 50**
  4. Gõ thẳng `/chapter?action=read&id=<chương VIP>` → **vẫn bị chặn**
  5. `mocmien` đọc chương VIP của chính mình → vào thẳng, **không trừ xu**
  6. `admin` đọc chương VIP bất kỳ → vào thẳng
  7. `thuytien` tải `.txt` truyện đó → file **không chứa** chương chưa mở
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** đọc chương **thường** · chuyển chương trước/sau ·
      nhớ vị trí đọc · bình luận chương · tặng xu tác giả *(chức năng cũ của ISSUE-008)*
- [ ] So lại bảng `wallets` với Baseline §4: **tổng xu toàn hệ thống không đổi** — xu chỉ
      chuyển từ ví này sang ví kia, không được sinh ra hay mất đi

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Commit: `ISSUE-020 phase-2 — mo khoa chuong bang xu, hang rao doc va giao dich`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Đây là đợt cuối → quay lại [`issue.md`](issue.md) điền §5 và đổi Trạng thái ✅
- [ ] Thêm một mục vào [`page/guide.jsp`](../../../../src/main/webapp/WEB-INF/views/common/page/guide.jsp)
      và một dòng vào [`page/rules.jsp`](../../../../src/main/webapp/WEB-INF/views/common/page/rules.jsp)
      — nói rõ **xu là ảo, không quy đổi tiền thật**
- [ ] Thêm chức năng mới vào bảng ở [`MO-TA-DO-AN.md`](../../../requirements/MO-TA-DO-AN.md) §3

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
