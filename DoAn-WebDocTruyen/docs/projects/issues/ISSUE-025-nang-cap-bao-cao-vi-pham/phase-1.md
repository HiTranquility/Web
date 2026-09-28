# 🚀 ISSUE-025 — Phase 1: Cột phân loại, bảng ảnh bằng chứng, và câu JOIN lấy `story_id`

> **Đích:** `docs/projects/issues/ISSUE-025-nang-cap-bao-cao-vi-pham/phase-1.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) |
| **Đợt** | Phase 1 / tổng 2 đợt |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | ✅ Xong |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `database/schema.sql` · `database/migration-025-report-upgrade.sql` *(mới)* · `model/Report.java` · `model/ReportEvidence.java` *(mới)* · `dao/ReportDAO.java` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** thêm cột `category` vào `reports`, thêm bảng `report_evidence`, cho
  `Report` mang thêm ba thuộc tính, và **sửa câu `findAll()` để lấy `c.story_id`** — mảnh
  ghép còn thiếu để dựng được link tới bình luận.
- **Để đợt sau:** form chọn loại, tải ảnh, giao diện admin, link sâu.

Xong đợt này thì DAO trả về đủ dữ liệu, test chạy được — **nhưng trang báo cáo nhìn y hệt
như cũ**, vì chưa ai hiển thị mấy trường mới.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách:** sửa `database/schema.sql` → luật ở [`projects/README`](../../README.md) bắt
đứng riêng và làm trước.

Riêng đợt này còn thêm một chỗ dễ vỡ: `ReportDAO.findAll()` là câu SQL **đã phức tạp sẵn**
— nó `LEFT JOIN` hai bảng khác nhau tuỳ `target_type`, cộng một `COALESCE`, cộng
`ORDER BY` có điều kiện. Sửa nó chung với code upload file thì lúc trang báo cáo trống
trơn sẽ không biết tại câu SQL hay tại chỗ khác.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:**
  [`ISSUE-022 phase 1`](../ISSUE-022-danh-gia-dai-spoiler/phase-1.md) ✅ — **không phải vì
  nghiệp vụ liên quan**, thuần tuý vì xếp hàng sửa `schema.sql`. Thứ tự: ISSUE-020 phase 1
  → ISSUE-022 phase 1 → đợt này.
- **Xong đợt này mới mở khoá được:** [phase 2](phase-2.md).

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Thêm cột `category` vào `reports` *(§3.1)* | `database/schema.sql` | ☐ |
| 2 | Thêm bảng `report_evidence` *(§3.2)* | `database/schema.sql` | ☐ |
| 3 | `migration-025-report-upgrade.sql` cho người đã có dữ liệu | `database/` | ☐ |
| 4 | `Report` thêm `category`, `storyId`, `evidences` | `model/Report.java` | ☐ |
| 5 | `model/ReportEvidence.java` — JavaBean thuần | `model/` | ☐ |
| 6 | **Sửa `findAll()` lấy thêm `c.story_id` và `r.category`** *(§3.3)* | `dao/ReportDAO.java` | ☐ |
| 7 | `ReportDAO` thêm `insertEvidence` · `findEvidence` · `countEvidence` | `dao/ReportDAO.java` | ☐ |
| 8 | `ReportDAO.insert()` nhận thêm tham số `category` | `dao/ReportDAO.java` | ☐ |
| 9 | Test `ReportDAOTest` — 6 ca ở §5 | `src/test/java/truyen/ReportDAOTest.java` | ☐ |

### 3.1 Cột `category`

```sql
    -- PHÂN LOẠI VI PHẠM (ISSUE-025). Tám loại, xem bảng ở issue.md §3.1.
    --
    -- MẶC ĐỊNH 'OTHER' — đây là điều kiện để nâng cấp không phải sửa tay:
    -- mọi báo cáo CŨ (chỉ có ô reason tự do) tự rơi vào "Khác", đọc vẫn
    -- đúng nghĩa, và không dòng nào phải chạy UPDATE.
    --
    -- ENUM chứ không phải bảng tags riêng: một báo cáo có ĐÚNG MỘT loại.
    -- Quan hệ một-một thì cột là đúng, bảng nối là thừa. Thêm loại thứ chín
    -- sau này chỉ là sửa một dòng ENUM — cùng lý lẽ với cột target_type
    -- ngay phía trên.
    category ENUM('SPAM','ADULT','VIOLENCE','PRIVACY',
                  'PLAGIARISM','WRONG_INFO','HARASSMENT','OTHER')
             NOT NULL DEFAULT 'OTHER',
```

Và một index cho bộ lọc mới ở trang admin:

```sql
    -- Trang xử lý lọc theo loại, và ba loại nặng phải nổi lên đầu.
    INDEX idx_report_category (category, status, created_at),
```

### 3.2 Bảng `report_evidence`

```sql
-- =============================================================================
--  report_evidence — ảnh bằng chứng kèm theo báo cáo  (ISSUE-025)
-- =============================================================================
--  VÌ SAO BẢNG RIÊNG, KHÔNG PHẢI MỘT CỘT evidence_url TRÊN reports
--    Một cột chỉ chứa được một ảnh, mà ba ảnh là nhu cầu thật: ảnh chụp
--    trang truyện + ảnh chụp đúng chỗ vi phạm + ảnh bản gốc khi tố đạo văn.
--    Nhét ba đường dẫn ngăn phẩy vào một VARCHAR là đúng cái sai mà bảng
--    story_tags đã cảnh báo ("đừng làm kiểu tags = 'a,b'"): không đếm được,
--    không xoá lẻ được, và phải tự tách chuỗi ở tầng Java.
--
--  KHÔNG LƯU ẢNH VÀO CSDL, CHỈ LƯU ĐƯỜNG DẪN
--    Khác với nội dung chương (lưu thẳng MEDIUMTEXT, xem ghi chú bảng
--    chapters). Lý do ngược lại: ảnh là dữ liệu nhị phân vài trăm KB, đọc ra
--    là đọc nguyên khối, và trình duyệt cache được nếu nó là một file có URL
--    riêng. Nhét BLOB vào đây là mỗi lần mở trang admin kéo về vài MB.
--
--  ĐƯỜNG DẪN LUÔN BẮT ĐẦU BẰNG "evidence/"
--    Đây KHÔNG phải quy ước cho đẹp mà là hàng rào: UploadedFileServlet ở
--    phase 2 nhìn tiền tố này để biết phải chặn người không phải admin.
--    Xem phase-2 §3.2.
-- =============================================================================
CREATE TABLE report_evidence (
    id         INT AUTO_INCREMENT PRIMARY KEY,

    report_id  INT NOT NULL,

    -- Ví dụ: "evidence/2026/09/a1b2c3d4.jpg". Tương đối với thư mục uploads.
    file_path  VARCHAR(255) NOT NULL,

    -- Cỡ file lúc tải lên, để trang admin hiện và để đối soát khi dọn ổ đĩa.
    file_size  INT NOT NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Xoá báo cáo thì ảnh của nó đi theo. Lưu ý: CASCADE chỉ xoá DÒNG, không
    -- xoá FILE trên ổ đĩa — phase 2 phải tự xoá file, xem phase-2 §3.4.
    FOREIGN KEY (report_id) REFERENCES reports(id) ON DELETE CASCADE,

    INDEX idx_evidence_report (report_id)
) ENGINE=InnoDB;
```

> **[Bẫy] Giới hạn "tối đa 3 ảnh" KHÔNG khai được ở CSDL.** MySQL không có ràng buộc kiểu
> *"nhiều nhất 3 dòng con"*. Nó phải được ép ở tầng Java **trong cùng một transaction** với
> lúc chèn — kiểm `COUNT(*)` trước rồi chèn sau ở hai câu rời là hai request song song lọt
> cả hai. Đây là khác biệt có thật so với các bảng khác trong dự án, nơi khoá chính kép lo
> hộ. Ghi rõ vào `ReportDAO.insertEvidence()`.

### 3.3 Sửa câu `findAll()` — mảnh ghép thiếu

Câu hiện tại *(`ReportDAO.java:52–58`)* lấy được tiêu đề nhưng **không lấy `story_id` của
bình luận**, nên `reports.jsp` không có gì để dựng link:

```sql
-- TRƯỚC
SELECT r.*, u.display_name, u.username,
       COALESCE(s.title, LEFT(c.content, 80)) AS target_title
FROM reports r
JOIN users u        ON u.id = r.reporter_id
LEFT JOIN stories  s ON r.target_type = 'STORY'   AND s.id = r.target_id
LEFT JOIN comments c ON r.target_type = 'COMMENT' AND c.id = r.target_id
```

```sql
-- SAU: thêm đúng một biểu thức
SELECT r.*, u.display_name, u.username,
       COALESCE(s.title, LEFT(c.content, 80)) AS target_title,
       COALESCE(s.id, c.story_id)             AS link_story_id   -- ← MỚI
FROM reports r
JOIN users u        ON u.id = r.reporter_id
LEFT JOIN stories  s ON r.target_type = 'STORY'   AND s.id = r.target_id
LEFT JOIN comments c ON r.target_type = 'COMMENT' AND c.id = r.target_id
```

`COALESCE(s.id, c.story_id)` trả về id truyện **trong cả hai trường hợp**: báo cáo truyện
thì là chính nó, báo cáo bình luận thì là truyện chứa bình luận đó. Phase 2 chỉ việc dựng
`/story?action=detail&id=${r.storyId}#comment-${r.targetId}`.

> **`link_story_id` có thể là `NULL`** khi nội dung bị báo cáo đã bị xoá hẳn — cả hai
> `LEFT JOIN` đều hụt. Đọc bằng `rs.getInt()` thì `NULL` ra `0`, không ném lỗi; phase 2
> lấy `0` làm tín hiệu để hiện *"Nội dung không còn tồn tại"* thay vì một link chết.

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

> **[MUST] Điền số thật.** Ghi "OK" là vô dụng.

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `scripts\test.ps1` | *(dự kiến 101 pass / 0 fail — **đo lại, đừng chép**)* |
| Biên dịch | *(điền)* |
| `SHOW TABLES;` | *(đếm và ghi — xong đợt phải **nhiều hơn đúng 1**)* |
| `SELECT COUNT(*) FROM reports;` | *(điền — xong đợt phải **y nguyên**)* |
| Mở `/admin/report` | *(điền: hiện được mấy báo cáo, có lỗi không)* |
| Gửi một báo cáo từ trang truyện | *(điền — còn chạy không)* |

**[GOTCHA]** Hai dòng cuối bắt buộc. Đợt này sửa câu SQL **đang phục vụ trang admin** —
sai một dấu phẩy là trang trống trơn mà không báo lỗi gì.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `scripts\test.ps1` — không tụt, có `ReportDAOTest`
- [ ] **Nạp lại từ đầu chạy được:** `schema.sql` rồi `sample_data.sql` — không lỗi đỏ
- [ ] **Nâng cấp tại chỗ:** chạy `migration-025-*.sql` trên DB cũ còn dữ liệu →
      số bảng **+1**, `reports` **không mất dòng nào**, mọi dòng cũ có `category = 'OTHER'`
- [ ] **Bằng tay:** mở `/admin/report` → **vẫn hiện đủ** số báo cáo như baseline §4
- [ ] **Thử trường hợp xấu — sáu ca:**
  1. Chèn `report_evidence` với `report_id` không tồn tại → khoá ngoại **từ chối**
  2. Xoá một `reports` → dòng `report_evidence` của nó **đi theo**
  3. Chèn `reports` với `category` không nằm trong 8 giá trị → CSDL **từ chối**
  4. `findAll()` trên một báo cáo **bình luận** → `link_story_id` ra **đúng id truyện chứa
     bình luận đó** *(đây là ca quan trọng nhất của cả đợt, kiểm bằng số thật)*
  5. `findAll()` trên báo cáo mà nội dung **đã bị xoá hẳn** → `link_story_id = 0`, **không
     ném lỗi**, dòng vẫn hiện
  6. `insertEvidence` lần thứ 4 cho cùng một báo cáo → **bị chặn ở tầng Java**
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** gửi báo cáo truyện · gửi báo cáo bình luận ·
      lọc theo trạng thái PENDING/RESOLVED/DISMISSED · đánh dấu đã xử lý · bỏ qua báo cáo —
      **năm thứ này đều đi qua `ReportDAO` vừa sửa**

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Commit: `ISSUE-025 phase-1 — them cot category, bang report_evidence, JOIN lay story_id`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] **Báo cả nhóm**, kèm đúng dòng lệnh:
      `mysql -u root -p webdoctruyen < database/migration-025-report-upgrade.sql`
- [ ] Nhắn: đây là đợt schema **cuối** của đợt 2 — sau đợt này `schema.sql` rảnh

---

## 7. Còn vướng / chưa làm

Schema, model và DAO — xong 2026-09-24.

Một thứ **cố tình chưa làm**: đường xoá báo cáo kèm dọn file trên đĩa.
`ReportDAO.findEvidencePaths()` và `UploadUtil.deleteEvidence()` đã viết sẵn,
nhưng **không có chỗ nào trong web xoá báo cáo** — admin chỉ đổi trạng thái.
Viết thêm một luồng xoá chưa ai gọi là đẻ code chết. Khi nào có nút xoá thật
thì hai hàm đó đã nằm sẵn đó.
