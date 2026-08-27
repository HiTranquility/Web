# temp/ — Khu vực tạm

**Đây KHÔNG phải đồ án.** Chỗ này chứa code học và demo tạm cho môn Lập trình Web.

Đồ án nằm ở thư mục khác: [`../DoAn-WebDocTruyen/`](../DoAn-WebDocTruyen/)

---

## Toàn cảnh thư mục `Downloads/Web/`

```
Downloads/Web/
├── DoAn-WebDocTruyen/      ← ĐỒ ÁN CUỐI KỲ (web đọc truyện)
│                              build riêng, docs riêng, database riêng
│
├── temp/                   ← BẠN ĐANG Ở ĐÂY — code học, demo tạm
│   ├── ch06-demo/             project chương 6, chạy độc lập (code + script)
│   └── docs/                  TẤT CẢ tài liệu gom ở đây
│
└── Week 1/                 ← code các chương trước + file slide gốc
    ├── ch02email/
    ├── ch05email/
    └── docs/
```

Ba khu này **hoàn toàn tách biệt**: không dùng chung code, không dùng chung
database, mỗi cái build riêng. Xoá `temp/` đi thì đồ án vẫn chạy bình thường.

---

## Có gì trong đây

### `ch06-demo/` — Chương 6: How to develop JSPs

Project độc lập, tự build tự chạy. Không liên quan gì tới đồ án.

**Chạy:**

```bash
cd temp\ch06-demo
powershell -ExecutionPolicy Bypass -File demo.ps1
```

Rồi mở <http://localhost:8080/temp/>

**Toàn bộ tài liệu gom trong [`docs/`](docs/):**

| File | Nội dung |
|------|----------|
| [`docs/chapter06-demo.md`](docs/chapter06-demo.md) | **Kịch bản demo 6 bước** — bấm gì, thấy gì, nói gì |
| [`docs/chapter06-mapping.md`](docs/chapter06-mapping.md) | Ánh xạ **35 slide → file nào, dòng nào** |
| [`docs/chapter06-project.md`](docs/chapter06-project.md) | Cấu trúc project, các lỗi đã vá |
| `ch06-demo/verify-mapping.ps1` | Kiểm tra số dòng trong bảng còn đúng không |

File slide gốc: `../Week 1/Chapter 06 slides.pptx`

---

## Lưu ý về cổng 8080

Đồ án và `ch06-demo` **cùng dùng cổng 8080**, nên chỉ chạy được một cái tại một
thời điểm. Chạy cái thứ hai sẽ báo `port 8080 already in use`.

Muốn chạy song song thì thêm `-Port`:

```bash
powershell -ExecutionPolicy Bypass -File demo.ps1 -Port 9090
```

Dừng server: **Ctrl+C** trong terminal đang chạy. Không được thì:

```bash
taskkill /F /IM java.exe
```

> Lệnh trên tắt **mọi** tiến trình Java trên máy. Đang chạy IntelliJ hay app Java
> nào khác thì nó tắt luôn.

---

## Khi nào xoá thư mục này

Xong môn thì xoá cả `temp/` được — nó không ảnh hưởng gì tới đồ án.

Trước khi nộp bài chương 6, nên dọn hai thứ trong `ch06-demo`:

1. Xoá `src/main/webapp/temp/` (trang demo bấm thử lỗi)
2. Xoá khối "Chi tiết kỹ thuật" trong `error_500.jsp` — nó phơi tên lớp và thông
   điệp lỗi cho người dùng xem

Chi tiết ở cuối [`docs/chapter06-demo.md`](docs/chapter06-demo.md).
