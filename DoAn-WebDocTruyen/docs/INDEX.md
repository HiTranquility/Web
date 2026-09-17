# 📖 Web Đọc Truyện — Mục lục tài liệu

Bản đồ tài liệu của đồ án. Mở file này khi không nhớ nên tra ở đâu.

```text
docs/
├── INDEX.md                          ← bạn đang ở đây (mục lục trung tâm)
│
├── _private/                         ← 🔒 Ghi chú cá nhân (được .gitignore, chỉ lưu ở local)
│   └── README.md
│
├── requirements/                     ← ĐẶC TẢ YÊU CẦU & HỒ SƠ ĐỀ TÀI
│   ├── DANG-KY-DE-TAI.md             bản đăng ký đề tài — danh sách chức năng đề xuất
│   └── MO-TA-DO-AN.md                bản mô tả gửi giảng viên — tên đề tài, 16 chức năng
│
├── architecture/                     ← THIẾT KẾ KIẾN TRÚC & KỸ THUẬT
│   ├── cau-truc.md                   bản đồ dự án · phân tầng Controller / DAO / Model
│   ├── so-do.md                      ERD · luồng MVC · kiến trúc — 7 sơ đồ Mermaid
│   ├── ke-hoach-frontend.md          4 layout · giao diện các trang · mảnh tái dùng
│   └── ke-hoach-database.md          13 bảng CSDL · chuẩn hoá + phi chuẩn hoá
│
├── guides/                           ← HƯỚNG DẪN & GIẢI THÍCH CƠ CHẾ
│   ├── huong-dan-code.md             HƯỚNG DẪN CODE BẰNG TAY — luồng CSDL → DAO → Servlet → JSP từ A-Z
│   ├── giai-thich.md                 VÌ SAO nó chạy như vậy — 7 khu, giảng từ đầu
│   └── CHECKLIST.md                  danh sách tick kiểm tra trước khi nộp bài
│
├── standards/                        ← QUY ƯỚC — tra cứu khi đang viết code
│   ├── 01-CODING_CONVENTIONS.md      Java: đặt tên, contract 4 tầng, URL
│   ├── 02-VIEW_CONVENTIONS.md        JSP: layout, scope, attribute, EL, CSS
│   ├── 03-DATABASE_CONVENTIONS.md    SQL: đặt tên, kiểu dữ liệu, luật DAO
│   └── 04-GIT_CONVENTIONS.md         commit message, quy tắc branch
│
├── projects/                         ← QUẢN LÝ CÔNG VIỆC — ai đang làm gì, lỗi nào chưa sửa
│   ├── README.md                     luật: ISSUE hay bug · đánh số · giao việc
│   ├── issues/README.md              bảng theo dõi việc cần làm & các phase
│   └── bugs/README.md                bảng theo dõi lỗi + 3 mức độ nghiêm trọng
│
└── templates/                        ← KHUÔN MẪU — copy ra rồi điền
    ├── issue-template.md             khuôn một việc
    ├── phase-template.md             khuôn một đợt, khi việc lớn phải chia
    └── bug-template.md               khuôn một lỗi
```

---

## Tra nhanh — tôi đang phân vân về…

| Câu hỏi trong đầu | Mở file |
|-------------------|---------|
| **"Tự tay code một tính năng từ DAO tới JSP"** | **[guides/huong-dan-code.md](guides/huong-dan-code.md)** |
| "Cách dùng getParameter, getSession, setAttribute?" | [guides/huong-dan-code.md](guides/huong-dan-code.md) §2 |
| "Cách viết try-catch, mở kết nối DAO?" | [guides/huong-dan-code.md](guides/huong-dan-code.md) §1 |
| "Cách lặp forEach, if/choose, in c:out trong JSP?" | [guides/huong-dan-code.md](guides/huong-dan-code.md) §3 |
| **"Nộp bản đăng ký đề tài"** | **[requirements/DANG-KY-DE-TAI.md](requirements/DANG-KY-DE-TAI.md)** |
| **"Gửi cô bản mô tả đồ án"** | **[requirements/MO-TA-DO-AN.md](requirements/MO-TA-DO-AN.md)** |
| "File này bỏ vào thư mục nào?" | [architecture/cau-truc.md](architecture/cau-truc.md) |
| **"Cần bao nhiêu layout, bao nhiêu trang?"** | **[architecture/ke-hoach-frontend.md](architecture/ke-hoach-frontend.md)** |
| **"Thiết kế bảng thế nào, chuẩn hoá ra sao?"** | **[architecture/ke-hoach-database.md](architecture/ke-hoach-database.md)** |
| **"Cho tôi xem sơ đồ / ERD / luồng MVC"** | **[architecture/so-do.md](architecture/so-do.md)** |
| **"Cái này chạy kiểu gì? Sao lại thế?"** | **[guides/giai-thich.md](guides/giai-thich.md)** |
| "scope là gì, 4 cái khác nhau sao?" | [guides/giai-thich.md](guides/giai-thich.md) khu 1 |
| "layout lắp trang kiểu gì?" | [guides/giai-thich.md](guides/giai-thich.md) khu 2 |
| "forward khác redirect chỗ nào?" | [guides/giai-thich.md](guides/giai-thich.md) khu 4 |
| "Đặt tên class/method này sao?" | [standards/01-CODING](standards/01-CODING_CONVENTIONS.md) §1 |
| "DAO có được forward không?" | [standards/01-CODING](standards/01-CODING_CONVENTIONS.md) §2 |
| "URL của chức năng này là gì?" | [standards/01-CODING](standards/01-CODING_CONVENTIONS.md) §5 |
| "Attribute này đặt tên gì, scope nào?" | [standards/02-VIEW](standards/02-VIEW_CONVENTIONS.md) §3 |
| "Trang mới dùng layout nào?" | [standards/02-VIEW](standards/02-VIEW_CONVENTIONS.md) §5 |
| "Dùng `${}` hay `<c:out>`?" | [standards/02-VIEW](standards/02-VIEW_CONVENTIONS.md) §4 |
| "Cột này để `TEXT` hay `MEDIUMTEXT`?" | [standards/03-DATABASE](standards/03-DATABASE_CONVENTIONS.md) §2 |
| "Khoá ngoại dùng CASCADE hay SET NULL?" | [standards/03-DATABASE](standards/03-DATABASE_CONVENTIONS.md) §4 |
| "Sắp nộp bài rồi, cần dọn gì?" | [guides/CHECKLIST.md](guides/CHECKLIST.md) |
| **"Giao việc này cho bạn kia thế nào?"** | **[projects/README.md](projects/README.md)** |
| "Việc mới thì mở ISSUE hay bug?" | [projects/README.md](projects/README.md) — "Chọn đúng một trong hai" |
| "Việc này lớn quá, có nên chia đợt?" | [projects/README.md](projects/README.md) — "Việc lớn thì chia đợt" |
| "Ai đang làm gì?" | [projects/issues/README.md](projects/issues/README.md) |
| "Còn lỗi nào chưa sửa?" | [projects/bugs/README.md](projects/bugs/README.md) |

---

## Tài liệu ngoài `docs/`

| File | Nội dung |
|------|----------|
| [`../README.md`](../README.md) | Cài đặt, cách chạy, lộ trình 11 CASE |
| [`../database/schema.sql`](../database/schema.sql) | 13 bảng, chú thích từng cột |

---

## Quy tắc viết doc cho đồ án này

Ba điều, để `docs/` không phình thành thứ không ai đọc:

1. **Quy ước → `standards/`. Hướng dẫn làm → `README.md`. Việc đang giao → `projects/`.**
   Standard trả lời *"phải viết thế nào"*, README trả lời *"chạy thế nào"*,
   `projects/` trả lời *"ai đang làm gì"*. Ba câu hỏi khác nhau, ba chỗ khác nhau.

2. **Không tạo file doc mới nếu chưa có 2 chỗ cần tra.**
   Một mẹo lẻ thì viết thành comment ngay trong code, đừng đẻ file mới.

3. **Doc nào mâu thuẫn với code thì code đúng — sửa doc ngay.**
   Doc sai còn tệ hơn không có doc, vì nó khiến bạn tin nhầm.

> **Vì sao `projects/` chỉ có hai loại doc, không phải mười bốn.**
> Đồ án này cố ý **không** bê nguyên bộ doc cấp production (ADR · postmortem ·
> spike · audit · usecase · thư mục theo tháng · script sinh mã số). Quy mô ở đây
> là 59 file, một kỳ — bê đủ bộ thì thời gian viết doc vượt thời gian viết code,
> và phần lớn khuôn sẽ không bao giờ đụng tới.
>
> `projects/` sinh ra **chỉ vì** đồ án chuyển từ một người sang nhiều người: hai
> người trở lên mà không ghi ai làm gì thì sẽ có hai người cùng sửa một `Servlet`.
> Đúng hai loại — **ISSUE** và **bug** — cộng khuôn **phase** cho việc lớn phải chia
> đợt. Thấy thiếu thật thì thêm, nhưng thêm vì đang vướng, không phải vì "cho đủ bộ".
