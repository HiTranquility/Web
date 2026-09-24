# 🚀 ISSUE-022 — Phase 1: Hai bảng cho bài đánh giá và lượt bấm Có ích

> **Đích:** `docs/projects/issues/ISSUE-022-danh-gia-dai-spoiler/phase-1.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) |
| **Đợt** | Phase 1 / tổng 2 đợt |
| **Người làm** | N2 viết SQL · **N1 commit vào `schema.sql`** *(luật một cửa, xem [issues/README](../README.md))* |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `database/schema.sql` · `database/migration-022-reviews.sql` *(mới)* · `model/Review.java` *(mới)* · `dao/ReviewDAO.java` *(mới)* |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** hai bảng `reviews` và `review_votes`, model `Review`, và `ReviewDAO`
  với đủ phương thức. Xong đợt thì DAO chạy được bằng test — **web nhìn y hệt như cũ**.
- **Để đợt sau:** toàn bộ giao diện, nút Có ích, nhãn spoiler, gỡ bài ở trang admin.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách:** sửa `database/schema.sql` → luật ở [`projects/README`](../../README.md) bắt
đứng riêng và làm trước.

Riêng đợt này còn một lý do nữa: nó là đợt **hai người phải làm nối tiếp nhau**. N2 hiểu
nghiệp vụ đánh giá nên N2 viết câu SQL, nhưng `schema.sql` chỉ N1 được commit. Việc bàn
giao đó cần một file riêng để không ai tưởng mình đang chờ người kia.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:**
  [`ISSUE-020 phase 1`](../ISSUE-020-mo-khoa-chuong-bang-xu/phase-1.md) ✅ — **không phải vì
  nghiệp vụ liên quan gì tới nhau**, mà thuần tuý vì cả hai sửa `schema.sql`. Đợi N1 báo
  *"schema đã rảnh"* rồi mới bắt đầu.
- **Xong đợt này mới mở khoá được:** [phase 2](phase-2.md).

---

## 3. Việc trong đợt

| # | Task | Ai | Chạm vào | Xong |
|---|---|---|---|:---:|
| 1 | Viết hai khối `CREATE TABLE` theo mẫu §3.1, gửi N1 | N2 | *(bản nháp)* | ☐ |
| 2 | Dán vào `schema.sql` đúng chỗ, commit | **N1** | `database/schema.sql` | ☐ |
| 3 | `migration-022-reviews.sql` cho người đã có dữ liệu | **N1** | `database/` | ☐ |
| 4 | `model/Review.java` — JavaBean thuần | N2 | `model/` | ☐ |
| 5 | `ReviewDAO`: `findByStory` · `findByUserAndStory` · `upsert` · `vote` · `unvote` · `hide` | N2 | `dao/ReviewDAO.java` | ☐ |
| 6 | Test `ReviewDAOTest` — 5 ca ở §5 | N2 | `src/test/java/truyen/ReviewDAOTest.java` | ☐ |

### 3.1 Hai bảng phải viết như thế nào

Đặt **sau** bảng `ratings` *(bài đánh giá gắn liền với điểm sao)*:

```sql
-- =============================================================================
--  reviews — bài đánh giá dài, đi kèm điểm sao  (ISSUE-022)
-- =============================================================================
--  VÌ SAO KHÔNG THÊM CỘT content VÀO BẢNG ratings
--    ratings nằm trên đường NÓNG nhất của cả site: lưới kho truyện đọc
--    rating_sum / rating_count cho MỌI thẻ truyện, mỗi trang 24 thẻ. Nhét
--    thêm một cột TEXT, một cờ spoiler, một bộ đếm và một cột trạng thái vào
--    đó là làm nặng bảng mà 99% truy vấn không cần tới chúng.
--
--    Tách ra còn được một thứ quan trọng hơn: CHẤM SAO KHÔNG BẮT BUỘC VIẾT
--    CẢM NHẬN. Đa số người ta chỉ muốn bấm 5 sao rồi đi.
--
--  KHÔNG LƯU LẠI SỐ SAO Ở ĐÂY
--    Điểm sao đã nằm ở ratings.score. Chép sang đây là hai chỗ phải cùng
--    đúng, và chúng SẼ lệch: người ta sửa sao mà không sửa bài đánh giá.
--    Muốn hiện sao kèm bài thì JOIN sang ratings — hai bảng cùng khoá
--    (user_id, story_id) nên JOIN rất rẻ.
-- =============================================================================
CREATE TABLE reviews (
    id           INT AUTO_INCREMENT PRIMARY KEY,

    user_id      INT NOT NULL,
    story_id     INT NOT NULL,

    title        VARCHAR(150) NOT NULL,
    content      VARCHAR(4000) NOT NULL,

    -- Người viết tự tick. Bài có nhãn thì giao diện che mờ, bấm mới hiện.
    -- Đây là che CHO LỊCH SỰ, không phải hàng rào bảo mật — khác hẳn chương
    -- VIP ở ISSUE-020, nơi nội dung tuyệt đối không được vào HTML.
    has_spoiler  BOOLEAN NOT NULL DEFAULT FALSE,

    -- Đếm sẵn thay vì COUNT(*) mỗi lần tải trang. PHI CHUẨN HOÁ CÓ CHỦ Ý,
    -- cùng lý do với stories.rating_sum. Cái giá: review_votes và cột này
    -- phải cùng đúng -> ReviewDAO sửa cả hai trong MỘT transaction.
    helpful_count INT NOT NULL DEFAULT 0,

    -- Admin gỡ bài vi phạm thì đổi sang HIDDEN, không xoá hẳn — giữ bằng
    -- chứng khi xử lý tài khoản. Giống hệt cách comments đang làm.
    status       ENUM('VISIBLE','HIDDEN') NOT NULL DEFAULT 'VISIBLE',

    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                          ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id)  REFERENCES users(id)   ON DELETE CASCADE,
    FOREIGN KEY (story_id) REFERENCES stories(id) ON DELETE CASCADE,

    -- MỘT NGƯỜI MỘT BÀI CHO MỘT TRUYỆN. Gửi lần hai là SỬA bài cũ.
    -- Ràng buộc ở CSDL chứ không chỉ ở code: hai request song song thì code
    -- kiểm-trước-rồi-ghi vẫn lọt cả hai.
    UNIQUE KEY uq_review_user_story (user_id, story_id),

    -- Câu nóng nhất: "bài đánh giá của truyện này, hữu ích nhất trước".
    INDEX idx_review_story (story_id, helpful_count DESC)
) ENGINE=InnoDB;


-- =============================================================================
--  review_votes — ai đã bấm "Có ích" cho bài nào  (ISSUE-022)
-- =============================================================================
--  Không có bảng này thì không chặn được một người bấm Có ích một trăm lần.
--  Khoá chính kép là thứ chặn, không phải câu if trong Java.
-- =============================================================================
CREATE TABLE review_votes (
    review_id  INT NOT NULL,
    user_id    INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (review_id, user_id),

    FOREIGN KEY (review_id) REFERENCES reviews(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)   REFERENCES users(id)   ON DELETE CASCADE
) ENGINE=InnoDB;
```

> **[Bẫy] `helpful_count` và `review_votes` là hai chỗ phải cùng đúng.** `ReviewDAO.vote()`
> chèn dòng vào `review_votes` **và** `UPDATE reviews SET helpful_count = helpful_count + 1`
> trong **một** transaction. Làm rời nhau là con số trên màn hình lệch dần với sự thật, và
> không ai phát hiện cho tới lúc ngồi đếm tay. Khuôn mẫu đúng đã có sẵn ở `RatingDAO`.

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `scripts\test.ps1` | *(điền — **đo lại, đừng chép**)* |
| Biên dịch | *(điền)* |
| `SHOW TABLES;` | *(đếm và ghi số — xong đợt phải **nhiều hơn đúng 2**)* |
| `SELECT COUNT(*) FROM ratings;` | *(điền — xong đợt phải **y nguyên**)* |
| Chấm sao một truyện trên web | *(điền — còn chạy không)* |

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `scripts\test.ps1` — không tụt, có `ReviewDAOTest`
- [ ] **Nạp lại từ đầu chạy được:** `schema.sql` rồi `sample_data.sql` — không lỗi đỏ
- [ ] **Nâng cấp tại chỗ:** chạy `migration-022-reviews.sql` trên DB cũ còn dữ liệu →
      số bảng **+2**, `ratings` **không mất dòng nào**
- [ ] **Bằng tay:** chấm sao một truyện → **vẫn chạy như cũ**, điểm trung bình đổi đúng
- [ ] **Thử trường hợp xấu — năm ca:**
  1. Chèn hai `reviews` cùng `(user_id, story_id)` → CSDL **từ chối**
  2. Chèn hai `review_votes` cùng `(review_id, user_id)` → CSDL **từ chối**
  3. Chèn `reviews` với `story_id` không tồn tại → khoá ngoại **từ chối**
  4. Xoá một `stories` → bài đánh giá của nó **đi theo**, và `review_votes` của bài đó
     **cũng đi theo** *(cascade hai tầng — kiểm thật, đừng đoán)*
  5. `ReviewDAO.vote()` gọi hai lần → `helpful_count` chỉ **+1**
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** chấm sao · đổi điểm sao · lưới kho truyện hiện
      đúng số sao · trang xếp hạng theo điểm

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Commit *(do N1 đẩy)*: `ISSUE-022 phase-1 — them bang reviews va review_votes`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] **Báo cả nhóm**, kèm dòng lệnh:
      `mysql -u root -p webdoctruyen < database/migration-022-reviews.sql`

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
