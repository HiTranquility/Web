# 📖 Web Đọc Truyện — Mục lục tài liệu

Bản đồ tài liệu của đồ án. Mở file này khi không nhớ nên tra ở đâu.

```text
docs/
├── INDEX.md                          ← bạn đang ở đây
├── MO-TA-DO-AN.md                    BẢN MÔ TẢ GỬI GIẢNG VIÊN — tên đề tài, 16 chức năng
├── cau-truc.md                       bản đồ 59 file · 13 thư mục · 4 layout
├── so-do.md                          ERD · luồng MVC · kiến trúc — 7 sơ đồ Mermaid
├── giai-thich.md                     VÌ SAO nó chạy như vậy — 7 khu, giảng từ đầu
├── CHECKLIST.md                      danh sách tick trước khi nộp bài
│
└── standards/                        QUY ƯỚC — tra khi đang code
    ├── 01-CODING_CONVENTIONS.md      Java: đặt tên, contract 4 tầng, URL
    ├── 02-VIEW_CONVENTIONS.md        JSP: layout, scope, attribute, EL, CSS
    ├── 03-DATABASE_CONVENTIONS.md    SQL: đặt tên, kiểu dữ liệu, luật DAO
    └── 04-GIT_CONVENTIONS.md         commit message, branch
```

---

## Tra nhanh — tôi đang phân vân về…

| Câu hỏi trong đầu | Mở file |
|-------------------|---------|
| **"Gửi cô bản mô tả đồ án"** | **[MO-TA-DO-AN.md](MO-TA-DO-AN.md)** |
| "File này bỏ vào thư mục nào?" | [cau-truc.md](cau-truc.md) |
| **"Cho tôi xem sơ đồ / ERD / luồng MVC"** | **[so-do.md](so-do.md)** |
| **"Cái này chạy kiểu gì? Sao lại thế?"** | **[giai-thich.md](giai-thich.md)** |
| "scope là gì, 4 cái khác nhau sao?" | [giai-thich.md](giai-thich.md) khu 1 |
| "layout lắp trang kiểu gì?" | [giai-thich.md](giai-thich.md) khu 2 |
| "forward khác redirect chỗ nào?" | [giai-thich.md](giai-thich.md) khu 4 |
| "Đặt tên class/method này sao?" | [01-CODING](standards/01-CODING_CONVENTIONS.md) §1 |
| "DAO có được forward không?" | [01-CODING](standards/01-CODING_CONVENTIONS.md) §2 |
| "URL của chức năng này là gì?" | [01-CODING](standards/01-CODING_CONVENTIONS.md) §5 |
| "Attribute này đặt tên gì, scope nào?" | [02-VIEW](standards/02-VIEW_CONVENTIONS.md) §3 |
| "Trang mới dùng layout nào?" | [02-VIEW](standards/02-VIEW_CONVENTIONS.md) §5 |
| "Dùng `${}` hay `<c:out>`?" | [02-VIEW](standards/02-VIEW_CONVENTIONS.md) §4 |
| "Cột này để `TEXT` hay `MEDIUMTEXT`?" | [03-DATABASE](standards/03-DATABASE_CONVENTIONS.md) §2 |
| "Khoá ngoại dùng CASCADE hay SET NULL?" | [03-DATABASE](standards/03-DATABASE_CONVENTIONS.md) §4 |
| "Sắp nộp bài rồi, cần dọn gì?" | [CHECKLIST.md](CHECKLIST.md) |

---

## Tài liệu ngoài `docs/`

| File | Nội dung |
|------|----------|
| [`../README.md`](../README.md) | Cài đặt, cách chạy, lộ trình 11 CASE |
| [`../database/schema.sql`](../database/schema.sql) | 7 bảng, chú thích từng cột |

---

## Quy tắc viết doc cho đồ án này

Ba điều, để `docs/` không phình thành thứ không ai đọc:

1. **Quy ước → `standards/`. Hướng dẫn làm → `README.md`.**
   Standard trả lời *"phải viết thế nào"*. README trả lời *"chạy thế nào"*.

2. **Không tạo file doc mới nếu chưa có 2 chỗ cần tra.**
   Một mẹo lẻ thì viết thành comment ngay trong code, đừng đẻ file mới.

3. **Doc nào mâu thuẫn với code thì code đúng — sửa doc ngay.**
   Doc sai còn tệ hơn không có doc, vì nó khiến bạn tin nhầm.

> Đồ án này cố ý **không** dùng hệ thống doc đầy đủ như IPLMS (13 loại doc,
> `reindex.sh`, thư mục theo tháng, ADR, postmortem). Quy mô ở đây là 59 file /
> một người / một kỳ — bê nguyên hệ thống đó sang thì thời gian viết doc vượt
> thời gian viết code, và phần lớn template sẽ không bao giờ đụng tới.
