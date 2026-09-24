# 🚀 ISSUE-020 — Phase 1: Chỗ ngồi trong CSDL cho chương VIP

> **Đích:** `docs/projects/issues/ISSUE-020-mo-khoa-chuong-bang-xu/phase-1.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) |
| **Đợt** | Phase 1 / tổng 2 đợt |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `database/schema.sql` · `database/migration-020-chapter-unlocks.sql` *(mới)* · `model/Chapter.java` · `model/ChapterUnlock.java` *(mới)* · `dao/UnlockDAO.java` *(mới)* · `dao/ChapterDAO.java` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** thêm bảng `chapter_unlocks`, thêm hai cột VIP vào `chapters`, viết
  `UnlockDAO` và cho `Chapter` mang thêm hai thuộc tính. Xong đợt thì bảng có, DAO chạy
  được bằng test — **nhưng chưa ai gọi nó, và web nhìn y hệt như cũ**.
- **Để đợt sau:** toàn bộ luồng mở khoá, hàng rào chặn đọc, và giao diện.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách:** đợt này sửa `database/schema.sql`, mà luật ở
[`projects/README`](../../README.md) nói sửa schema **luôn đứng riêng và làm trước**.

Ở đây còn một lý do riêng nặng hơn: đợt 2 đụng vào **tiền**. Khi xu bị trừ sai, câu hỏi
đầu tiên luôn là *"lỗi ở câu SQL hay ở logic tính?"*. Để schema ra một commit riêng đã
chạy test xong thì câu hỏi đó chỉ còn một nửa để tìm.

Đợt này **cố ý không có gì cho người dùng thấy**. Pull về chạy web là mọi thứ y như cũ —
đó là điểm mạnh, không có gì để hỏng.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** không có. Nhưng **ISSUE-022 phase 1 cũng sửa
  `schema.sql`** — hai đợt đó phải nối tiếp, không được song song. Đợt này đi trước.
- **Xong đợt này mới mở khoá được:** [phase 2](phase-2.md).

> Đợt này sửa `schema.sql`. **Báo cả nhóm ngay khi commit**, kèm đúng dòng lệnh họ cần gõ.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Thêm bảng `chapter_unlocks` vào `schema.sql`, chú thích từng cột như các bảng khác | `database/schema.sql` | ☐ |
| 2 | Thêm `is_vip` và `coin_price` vào bảng `chapters` | `database/schema.sql` | ☐ |
| 3 | Viết `migration-020-chapter-unlocks.sql` cho người đã có dữ liệu | `database/` | ☐ |
| 4 | `Chapter` thêm `vip` (boolean) và `coinPrice` (int) | `model/Chapter.java` | ☐ |
| 5 | `model/ChapterUnlock.java` — JavaBean thuần | `model/` | ☐ |
| 6 | `UnlockDAO`: `hasUnlocked(userId, chapterId)` · `insert(...)` · `countByUser(userId)` | `dao/UnlockDAO.java` | ☐ |
| 7 | `ChapterDAO` đọc thêm hai cột mới ở mọi câu `SELECT` đang có | `dao/ChapterDAO.java` | ☐ |
| 8 | Test `UnlockDAOTest` — chèn, tìm lại, và chèn trùng phải bị CSDL chặn | `src/test/java/truyen/UnlockDAOTest.java` | ☐ |

### Bảng phải viết như thế nào

Đặt **sau** `chapters` và **sau** `wallets` *(có khoá ngoại trỏ cả hai)*:

```sql
-- =============================================================================
--  chapter_unlocks — ai đã trả xu mở khoá chương nào  (ISSUE-020)
-- =============================================================================
--  MỞ MỘT LẦN, ĐỌC MÃI MÃI. Bảng này là tấm vé đã xé: có dòng nghĩa là người
--  đó đã trả tiền rồi, lần sau vào thẳng.
--
--  VÌ SAO KHÔNG SUY RA TỪ BẢNG transactions
--    Về lý thuyết cứ tìm trong transactions xem có giao dịch "mua chương 7"
--    chưa là biết. Nhưng transactions là SỔ KẾ TOÁN — nó ghi mọi thứ, gồm cả
--    tặng xu và admin cấp xu, và nó chỉ được phép GHI THÊM, không sửa.
--    Hỏi "người này đã mở chương này chưa" trên đó là quét cả sổ mỗi lần lật
--    một chương. Bảng riêng thì câu hỏi đó là một lần tra khoá chính.
--
--  GIÁ ĐƯỢC CHÉP LẠI VÀO ĐÂY, CÓ CHỦ Ý
--    price_paid là giá TẠI THỜI ĐIỂM MUA. Tác giả đổi giá chương sau đó thì
--    dòng cũ vẫn giữ đúng con số người ta đã trả. Trỏ ngược về
--    chapters.coin_price là lịch sử tự đổi theo — sổ sách như vậy là sai.
-- =============================================================================
CREATE TABLE chapter_unlocks (
    user_id     INT NOT NULL,
    chapter_id  INT NOT NULL,

    -- Giá đã trả, chép cứng. Xem ghi chú ở đầu bảng.
    price_paid  INT NOT NULL,

    unlocked_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Khoá chính kép: một người mở một chương đúng MỘT lần.
    -- Ràng buộc ở CSDL chứ không chỉ ở code: bấm hai lần thật nhanh thì code
    -- kiểm-trước-rồi-ghi vẫn lọt cả hai, CSDL thì không.
    PRIMARY KEY (user_id, chapter_id),

    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    FOREIGN KEY (chapter_id) REFERENCES chapters(id) ON DELETE CASCADE,

    -- "Chương này đã bán được bao nhiêu lượt" — cho trang thống kê tác giả.
    INDEX idx_unlock_chapter (chapter_id)
) ENGINE=InnoDB;
```

Và hai cột thêm vào `chapters`:

```sql
    -- CHƯƠNG VIP (ISSUE-020). Mặc định FALSE: mọi chương cũ vẫn miễn phí,
    -- không phải chạy câu UPDATE nào sau khi nâng cấp schema.
    is_vip     BOOLEAN NOT NULL DEFAULT FALSE,

    -- Giá xu. 0 khi is_vip = FALSE.
    --
    -- VÌ SAO KHÔNG GỘP THÀNH MỘT CỘT coin_price, coi 0 = miễn phí
    --   Vì "miễn phí" và "VIP nhưng đang mở khuyến mãi giá 0" là hai chuyện
    --   khác nhau, và gộp lại thì không phân biệt được. Hai cột đắt thêm một
    --   byte, rẻ hơn nhiều so với một buổi ngồi đoán ý nghĩa của số 0.
    coin_price INT NOT NULL DEFAULT 0,
```

> **[Bẫy] `ChapterDAO` có nhiều câu `SELECT` liệt kê cột bằng tay.** Thêm cột vào bảng mà
> quên thêm vào câu `SELECT` thì `chapter.isVip()` trả `false` ở đúng chỗ đó — chương VIP
> hoá miễn phí, mà **không có lỗi nào bật ra**. Grep hết trước khi commit:
> `grep -n "SELECT" src/main/java/truyen/dao/ChapterDAO.java`

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `scripts\test.ps1` | *(dự kiến 101 pass / 0 fail — **đo lại, đừng chép**)* |
| Biên dịch | *(điền)* |
| `SHOW TABLES;` trong `webdoctruyen` | *(đếm và ghi số — xong đợt phải **nhiều hơn đúng 1**)* |
| `SELECT COUNT(*) FROM chapters;` | *(điền — xong đợt phải **y nguyên**)* |
| Mở đọc một chương bất kỳ | *(điền)* |

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `scripts\test.ps1` — không tụt, có thêm `UnlockDAOTest`
- [ ] **Nạp lại từ đầu chạy được:** `schema.sql` rồi `sample_data.sql` — không lỗi đỏ nào
- [ ] **Nâng cấp tại chỗ cũng chạy được:** chạy `migration-020-*.sql` trên bản DB cũ còn
      dữ liệu → số bảng +1, `chapters` **không mất dòng nào**, mọi chương `is_vip = FALSE`
- [ ] **Bằng tay:** đọc một chương bất kỳ → **vẫn bình thường**, không đòi xu *(đợt này
      chưa có luồng khoá)*
- [ ] **Thử trường hợp xấu:**
  1. Chèn hai dòng `chapter_unlocks` cùng `(user_id, chapter_id)` → CSDL **từ chối**
  2. Chèn với `chapter_id` không tồn tại → khoá ngoại **từ chối**
  3. Xoá một `chapters` → dòng unlock của nó **đi theo** (CASCADE)
  4. Xoá một `users` → unlock của họ **đi theo**
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** đọc chương · mục lục · tải `.txt` · thêm chương
      mới · sửa chương — **năm thứ này đều đi qua `ChapterDAO` vừa sửa**

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Commit: `ISSUE-020 phase-1 — them bang chapter_unlocks va hai cot VIP`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] **Báo cả nhóm**, kèm đúng dòng lệnh:
      `mysql -u root -p webdoctruyen < database/migration-020-chapter-unlocks.sql`
- [ ] Nhắn N2: *"schema đã rảnh, ISSUE-022 phase 1 bắt đầu được rồi"*

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
