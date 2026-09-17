# 🚀 ISSUE-001 — Phase 5: Tác giả sao lưu truyện lên Google Drive của chính mình

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [ISSUE-001](ISSUE-001-tich-hop-google.md) — *cùng thư mục* |
| **Đợt** | Phase 5 / tổng 5 đợt |
| **Người làm** | Dev B — Reader & Story |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `controller/common/DownloadServlet.java` · `controller/story/DriveBackupServlet.java` *(mới)* · `util/DriveClient.java` *(mới)* · `dao/ChapterDAO.java` *(chỉ đọc)* · `views/story/mine.jsp` · `views/story/detail.jsp` · `assets/js/drive-backup.js` *(mới)* |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** tác giả mở truyện **của mình**, bấm **"Sao lưu lên Google Drive"**,
  cho phép quyền một lần, và toàn bộ chương được đẩy lên Drive **của họ** thành
  `DocTruyen/<tên truyện>/001 - <tên chương>.txt`. Bấm lại lần sau thì ghi đè, không đẻ
  bản sao.
- **Để đợt sau:** không có. Đây là đợt cuối của ISSUE-001.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách của đợt này:** thành thật mà nói, đợt này **không nằm trong bảng bốn hàng
"phải tách"** — nó không sửa schema, không đụng `filter/`, không đổi thứ đang chạy. Nó
tách vì hàng thứ tư: **hai người làm nối tiếp nhau**. Dev B không đụng được vào việc này
trước khi Dev A xong phase 2, và khi đã phải chờ thì việc phải có file riêng để Dev B biết
đích xác mình được giao cái gì, bắt đầu từ đâu.

Nếu cuối cùng vẫn là một người làm cả năm đợt, thì gộp phase 5 vào phase 4 cũng được —
**ghi lý do gộp vào §7**, đừng lặng lẽ gộp.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [Phase 2](ISSUE-001-tich-hop-google-phase-2.md) — cần
  dự án Google Cloud và `GoogleConfig` đã có.
- **Xong đợt này mới mở khoá được:** không có. Đây là đợt cuối.

> **Dev B đang chờ Dev A.** Dev A xong phase 2 thì **nhắn ngay**, đừng để Dev B tự đoán
> bằng cách nhìn git log.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Ở Google Cloud Console bật **Google Drive API**, thêm scope `drive.file` vào màn hình đồng ý | *(ngoài repo)* | ☐ |
| 2 | Tách phần ghép chương thành `.txt` ra khỏi `DownloadServlet` thành một hàm dùng chung — **hai chỗ cùng gọi, không chép đôi** | `controller/common/DownloadServlet.java` | ☐ |
| 3 | `util/DriveClient` — tạo thư mục nếu chưa có, tải file lên, ghi đè nếu trùng tên | `util/DriveClient.java` | ☐ |
| 4 | `DriveBackupServlet` với `action=backup` — **kiểm quyền sở hữu truyện trước tiên** *(§3.1)* | `controller/story/DriveBackupServlet.java` | ☐ |
| 5 | Nút **"Sao lưu lên Drive"** ở `story/mine.jsp` *(danh sách truyện của tôi)* và ở `story/detail.jsp` khi người xem là tác giả | `mine.jsp` · `detail.jsp` | ☐ |
| 6 | JS xin `access_token` với scope `drive.file`, gửi về servlet, hiện tiến độ *"đang tải 7/29 chương…"* | `assets/js/drive-backup.js` | ☐ |
| 7 | Test `ChapterToTxtTest` — hàm ghép chương ra đúng định dạng, đúng thứ tự, tên file có số 3 chữ số | `src/test/java/truyen/ChapterToTxtTest.java` | ☐ |

### 3.1 Hai luật của đợt này

**Luật 1 — `drive.file`, không bao giờ `drive`.**

| Scope | Cho phép | Dùng ở đây? |
|---|---|---|
| `drive.file` | Chỉ những file **do chính ứng dụng này tạo ra** | ✅ |
| `drive` | **Toàn bộ** Drive của người ta — ảnh, hoá đơn, luận văn | ⛔ **Không.** |

Một web đọc truyện xin quyền đọc toàn bộ Drive là xin thứ nó không có lý do gì để cần. Với
`drive.file` thì kể cả web này có bị chiếm, kẻ chiếm cũng chỉ thấy đúng mấy file truyện do
nó tạo. Đây cũng là câu hỏi giám khảo rất dễ hỏi lúc bảo vệ — trả lời được là điểm.

**Luật 2 — kiểm quyền sở hữu, giống hệt `?action=edit`.**

```java
// DriveBackupServlet — dòng đầu tiên, trước khi đọc một chương nào
Story story = storyDAO.findById(storyId);
if (story == null || story.getAuthorId() != currentUser.getId()) {
    response.sendError(HttpServletResponse.SC_FORBIDDEN);
    return;
}
```

Không có dòng này thì `/drive?action=backup&storyId=<truyện của người khác>` là một đường
tải trộm cả **bản nháp chưa đăng** của người ta về Drive mình. Cùng một lỗi mà
[`CHECKLIST.md §1`](../../guides/CHECKLIST.md) đã dặn kiểm cho `?action=edit` — đường mới thì
hàng rào phải dựng lại, nó không tự đi theo.

### 3.2 Truyện dài thì đừng chặn người dùng ngồi chờ

29 chương là 29 lượt gọi API. Làm tuần tự trong một request thì trình duyệt quay vòng cả
phút rồi rất có thể timeout. Cách xử lý, chọn một:

| Cách | Được | Mất |
|---|---|---|
| **JS gọi từng chương một, hiện tiến độ** ← *khuyến nghị* | Người dùng thấy `7/29`, biết là đang chạy; một chương lỗi không mất cả lượt | JS phức tạp hơn một chút |
| Servlet chạy hết trong một lần | Code đơn giản nhất | Treo cả phút, timeout là mất trắng, không biết dừng ở chương nào |
| `ExecutorService` chạy nền | Không treo | Phải có chỗ hỏi "xong chưa" — thêm hẳn một cơ chế mới cho một nút bấm |

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `mvn test` | *(điền)* |
| `mvn -q compile` | *(điền)* |
| Bấm **Tải truyện `.txt`** ở một truyện 29 chương | *(điền: mất mấy giây, file bao nhiêu KB)* |
| Mở file `.txt` đó bằng Notepad | *(điền: tiếng Việt có dấu **hiển thị đúng** không — đây là thứ hay vỡ nhất)* |

**[GOTCHA]** Hai dòng cuối là baseline thật sự của đợt này. Task 2 **tách code ra khỏi
`DownloadServlet`** — một chức năng đang chạy đúng. Không đo trước thì lúc file tải về bị
lỗi font sẽ không biết là mình vừa làm hỏng hay nó vốn đã vậy.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `mvn test` — không tụt, có `ChapterToTxtTest`
- [ ] **Bằng tay — đường chính:**
  1. Đăng nhập `mocmien` / `123456`, vào **Truyện của tôi**
  2. Bấm **Sao lưu lên Google Drive** ở một truyện có nhiều chương
  3. Màn hình xin quyền của Google hiện ra, ghi rõ *"xem và quản lý các file do ứng dụng
     này tạo"* — **không phải** "toàn bộ Drive". Nhìn đúng câu chữ đó, đây là cách kiểm
     scope bằng mắt
  4. Kết quả mong đợi: hiện tiến độ chạy tới hết, mở Drive thấy
     `DocTruyen/<tên truyện>/001 - ….txt` … đủ số chương
  5. Mở một file trên Drive → **tiếng Việt có dấu đúng**, không phải `Ch??ng 1`
  6. Bấm sao lưu **lần hai** → file bị ghi đè, **không** sinh `(1)`, `(2)`
- [ ] **Thử trường hợp xấu — cả năm ca:**
  1. 🔴 Đăng nhập `haiduong`, gọi thẳng `/drive?action=backup&storyId=<truyện của mocmien>`
     → **403**, Drive của `haiduong` **không** có gì
  2. Bấm Sao lưu rồi **bấm Huỷ** ở màn hình xin quyền → hiện lời nhắc tử tế, không treo,
     không lỗi 500
  3. Truyện **0 chương** → báo *"Truyện chưa có chương nào để sao lưu"*, không gửi request rỗng
  4. Rút mạng giữa chừng → hiện đã tải được bao nhiêu chương, bấm lại thì chạy tiếp được
  5. Chưa đăng nhập mà gọi `/drive?action=backup` → đá về trang đăng nhập
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** nút **Tải truyện `.txt`** cũ — tải về, mở lên,
      so với baseline §4. Cùng số KB, cùng font đúng. *(Task 2 đã sờ vào code của nó.)*

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở bảng Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Đã commit: `ISSUE-001 phase-5 — sao luu truyen len Google Drive`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Đây là đợt cuối → quay lại [`ISSUE-001`](ISSUE-001-tich-hop-google.md) điền **§5 Đã
      kiểm thế nào** và đổi **Trạng thái** thành ✅
- [ ] Thêm chức năng thứ 17 vào bảng ở [`docs/requirements/MO-TA-DO-AN.md`](../../requirements/MO-TA-DO-AN.md) §3

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm. Nếu gộp đợt này vào phase 4 vì một người làm cả, **ghi lý do ở
đây**.)*
