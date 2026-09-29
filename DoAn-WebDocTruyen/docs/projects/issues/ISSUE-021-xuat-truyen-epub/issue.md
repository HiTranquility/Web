# 🏷️ ISSUE-021: Xuất truyện ra EPUB — đọc được trên Kindle và Apple Books

> **Đích:** `docs/projects/issues/ISSUE-021-xuat-truyen-epub/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-021 |
| **Người làm** | N2 — Đọc & Nội dung |
| **Trạng thái** | ✅ Đã xong (2026-09-29) |
| **Ngày mở** | 2026-09-23 |
| **CASE liên quan** | CASE 09 — Tải truyện *(mở rộng)* |
| **Đụng vào** | `controller/common/DownloadServlet.java` · `util/EpubWriter.java` *(mới)* · `util/ChapterToTxt.java` *(chỉ đọc)* · `views/common/story/detail.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

CASE 09 đã có nút **Tải truyện `.txt`** và nó chạy tốt. Nhưng `.txt` là định dạng mất
thông tin: không có mục lục, không nhảy được giữa các chương, không nhớ vị trí đọc, và
Kindle lẫn Apple Books đều **không mở được** — hai thiết bị mà người đọc truyện dài hay
dùng nhất.

- **Hiện tại:** tải về một file `.txt` dài hàng trăm nghìn ký tự, mở bằng Notepad, cuộn tay
  đi tìm chương 37.
- **Sau khi xong:** cạnh nút `.txt` có thêm nút **EPUB**. File tải về có bìa, có mục lục
  bấm được, mỗi chương một trang, và thả vào Kindle là đọc được ngay.

**EPUB không phải định dạng khó.** Nó là một file **zip** đổi đuôi, bên trong là XHTML —
`java.util.zip` trong JDK làm được hết, **không cần thêm thư viện nào vào `pom.xml`**. Đây
là lý do chọn EPUB thay vì PDF: PDF phải kéo iText hoặc PDFBox về, nặng vài MB, và chữ
tiếng Việt có dấu trong PDF cần nhúng font mới không vỡ.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

- [ ] Trang chi tiết truyện có nút **📕 EPUB** ngay cạnh nút `.txt` hiện có.
- [ ] Bấm vào → tải về `ten-truyen.epub`, header `Content-Type: application/epub+zip`.
- [ ] Mở bằng **Calibre** → hiện đúng tên truyện, tên tác giả, và **ảnh bìa**.
- [ ] Mục lục có **đủ số chương**, bấm vào chương nào nhảy đúng chương đó.
- [ ] **Tiếng Việt có dấu hiển thị đúng** — không `Ch??ng 1`, không `Ch÷¬ng 1`.
- [ ] Đổi tên file thành `.zip` rồi giải nén → có đủ `mimetype`, `META-INF/container.xml`,
      `OEBPS/content.opf`, `OEBPS/toc.ncx`.
- [ ] Chạy qua <https://draft2digital.com/book/epubcheck/upload> → **0 lỗi**.
- [ ] Truyện **0 chương** → báo tử tế, không tải về file zip rỗng.
- [ ] Truyện **DRAFT hoặc DELETED** → người ngoài tải về **bị chặn**, tác giả tải được.
- [ ] Nút `.txt` cũ **vẫn chạy y như trước** — cùng số KB, cùng font đúng.
- [ ] `scripts\test.ps1` — không tụt so với 101 pass.

---

## 3. Các bước

| # | Bước | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Đo baseline: tải `.txt` một truyện 29 chương, ghi số KB và kiểm font *(§5)* | *(không sửa gì)* | ☐ |
| 2 | `EpubWriter.write(OutputStream, Story, List<Chapter>)` — dựng zip theo đúng thứ tự ở §3.1 | `util/EpubWriter.java` | ☐ |
| 3 | Escape XML cho tiêu đề và nội dung *(§3.2)* | `util/EpubWriter.java` | ☐ |
| 4 | `DownloadServlet` thêm nhánh `?format=epub`, mặc định vẫn là `txt` | `DownloadServlet` | ☐ |
| 5 | Nút EPUB ở trang chi tiết | `views/common/story/detail.jsp` | ☐ |
| 6 | Test `EpubWriterTest` — 5 ca ở §5 | `src/test/java/truyen/EpubWriterTest.java` | ☐ |

### 3.1 Cấu trúc file EPUB — và một cái bẫy

```text
ten-truyen.epub  (là một file ZIP)
├── mimetype                    ← "application/epub+zip", KHÔNG nén, phải là mục ĐẦU TIÊN
├── META-INF/
│   └── container.xml           ← trỏ tới content.opf
└── OEBPS/
    ├── content.opf             ← tên truyện, tác giả, danh sách file, thứ tự đọc
    ├── toc.ncx                 ← mục lục bấm được
    ├── cover.jpg               ← ảnh bìa (bỏ qua nếu truyện không có)
    ├── style.css
    ├── chuong-001.xhtml
    └── chuong-002.xhtml …
```

> **[MUST] `mimetype` phải là mục đầu tiên trong zip và phải `ZipEntry.STORED`, không
> `DEFLATED`.** Đây là chỗ hỏng kinh điển của mọi lần tự viết EPUB. Đặc tả bắt như vậy để
> chương trình đọc nhận ra định dạng chỉ bằng 30 byte đầu file mà không phải giải nén.
> Làm sai thì Calibre vẫn mở được *(nó dễ tính)*, nhưng **Apple Books và epubcheck đều từ
> chối** — và mình sẽ tưởng là xong vì chỉ thử bằng Calibre.
>
> `STORED` thì phải tự đặt `setSize()`, `setCompressedSize()` và `setCrc()` trước khi ghi,
> không `ZipOutputStream` sẽ ném lỗi.

### 3.2 Nội dung chương phải escape XML, không phải escape HTML

XHTML nghiêm hơn HTML: `&`, `<`, `>` trong nội dung truyện mà không escape là **cả file
hỏng**, chương trình đọc báo lỗi cú pháp và không mở được — chứ không phải chỉ hiển thị
sai một chữ như trên web.

```java
// Trong truyện có "A & B", "<Lời tác giả>" là chuyện bình thường.
private static String xml(String s) {
    return s.replace("&", "&amp;")   // PHẢI đứng đầu, không thì escape chồng lên nhau
            .replace("<", "&lt;")
            .replace(">", "&gt;");
}
```

Mỗi đoạn văn bọc trong `<p>`, tách theo đúng cách `ChapterToTxt` đang tách — **dùng lại
hàm đó**, đừng viết lại cách tách đoạn lần thứ hai.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên `EpubWriter`, contract 4 tầng, URL `?format=` | [`01-CODING §1 §2 §5`](../../../standards/01-CODING_CONVENTIONS.md) |
| Nút tải ở trang chi tiết | [`02-VIEW §4`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Commit message | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Luật riêng:**

> **[NEVER] để `DownloadServlet` trả HTML.** Nó đã ghi rõ trong bảng 11 CASE ở
> [`README.md`](../../../../README.md): *"CASE 09 — `DownloadServlet` — không trả HTML"*.
> Lỗi giữa chừng thì `sendError`, **không** forward sang JSP — forward sau khi đã ghi byte
> vào response là file tải về dính một mẩu HTML ở đuôi và hỏng.
>
> Kiểm quyền **trước khi ghi byte đầu tiên**: truyện `DRAFT`/`DELETED` chỉ tác giả và admin
> tải được. Cùng hàng rào mà `?action=edit` đang dùng — đường mới thì phải dựng lại, nó
> không tự đi theo.

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅.*

| Đo | Trước | Sau |
|---|---|---|
| `.txt` truyện 29 chương — số KB | | |
| `.txt` mở bằng Notepad — tiếng Việt | | |
| `.epub` cùng truyện — số KB | — | |
| epubcheck | — | |
| `scripts\test.ps1` | 101 pass / 0 fail | |

- **Bấm thử:**
- **Thử trường hợp xấu:** 0 chương · truyện nháp · truyện đã gỡ · chương có ký tự `&` và `<`

---

## 6. Ghi chú

**Vì sao EPUB chứ không PDF.** PDF là định dạng **cố định khổ giấy** — đọc trên điện thoại
là phải phóng to kéo ngang, đúng thứ mà cả `layout-reader` của đồ án này đang cố tránh.
EPUB co giãn theo màn hình, người đọc tự chỉnh cỡ chữ. Cộng thêm chuyện PDF cần thư viện
ngoài và cần nhúng font cho tiếng Việt, trong khi EPUB chỉ cần `java.util.zip`.

**Thứ cố tình không làm:**

| Bỏ | Vì |
|---|---|
| MOBI | Amazon đã khai tử, Kindle đời mới nhận EPUB thẳng. |
| Gửi EPUB qua email tới Kindle | Cần SMTP và địa chỉ `@kindle.com` của người dùng — một tính năng riêng, không phải phần của việc xuất file. |
| Xuất từng chương lẻ ra EPUB | Một chương thì `.txt` là đủ. EPUB có ý nghĩa khi có mục lục để mà nhảy. |
