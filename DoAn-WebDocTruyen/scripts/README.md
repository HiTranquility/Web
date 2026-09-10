# scripts/ — Script chạy dự án

Cả ba script đều tự lùi về thư mục gốc dự án, nên gọi từ đâu cũng được.

---

## 1. Cài đặt database — chạy MỘT LẦN

```bash
powershell -ExecutionPolicy Bypass -File scripts\setup-db.ps1
```

Làm 4 việc:

| Bước | Việc |
|:----:|------|
| 1 | Tạo database `webdoctruyen` + 13 bảng |
| 2 | Tạo tài khoản MySQL `truyen_app` cho ứng dụng |
| 3 | Nạp dữ liệu mẫu (9 truyện, 29 chương, 6 tài khoản) |
| 4 | Sinh `src/main/resources/db.properties` |

**Bạn chỉ gõ mật khẩu MySQL `root` khi được hỏi.** Gõ thẳng vào `mysql`,
script không đọc, không lưu, không nhìn thấy mật khẩu đó.

Mật khẩu cho tài khoản `truyen_app` do script tự sinh ngẫu nhiên. Muốn tự đặt:

```bash
powershell -ExecutionPolicy Bypass -File scripts\setup-db.ps1 -AppPassword "MatKhauCuaBan"
```

Chạy lại script bao nhiêu lần cũng được — nó xoá dữ liệu cũ rồi nạp lại từ đầu.

---

## 2. Bổ sung index — chỉ khi database đã cài TỪ TRƯỚC

```bash
powershell -ExecutionPolicy Bypass -File scripts	hem-index.ps1
```

**Cài mới thì bỏ qua mục này** — `schema.sql` đã có đủ index.

Cần đến nó khi database đã tạo từ trước rồi `schema.sql` mới thêm index:
MySQL không tự moc thêm index vào bảng đã tồn tại, và cũng không có cơ chế
"đồng bộ lại" nào. Script so với `information_schema` rồi chỉ tạo phần còn
thiếu, nên **chạy lại bao nhiêu lần cũng được**.

Phải dùng `root`: tài khoản `truyen_app` **cố ý** chỉ có
`SELECT / INSERT / UPDATE / DELETE` — không có `INDEX`, không có `ALTER`,
không có `DROP`. Web chạy đủ với bấy nhiêu, và nếu có lỗ hổng SQL injection
nào lọt lưới thì kẻ tấn công cũng không xoá nổi bảng nào. Đổi lại, việc đổi
cấu trúc bảng phải làm bằng root — đúng ý đồ.

Mật khẩu root bạn gõ thẳng vào ô nhập, script không hiện ra màn hình và không
để lại trong lịch sử lệnh.

---

## 3. Build và chạy web

```bash
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```

→ <http://localhost:8080/>

Lần đầu chạy tải các thư viện về `.libs/` (~14 MB), lần sau chạy ngay.

Đổi cổng: `scripts\run.ps1 -Port 9090`
Dừng: **Ctrl+C** trong terminal đang chạy.

---

## Thứ tự lần đầu

```bash
powershell -ExecutionPolicy Bypass -File scripts\setup-db.ps1
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```

Sau đó mở <http://localhost:8080/> và đăng nhập:

| Tài khoản | Mật khẩu | Vai trò |
|-----------|----------|---------|
| `admin` | `admin123` | Quản trị viên |
| `mocmien` | `123456` | Tác giả |
| `thuytien` | `123456` | Độc giả |
| `spammer` | `123456` | Đã bị khoá — thử để xem cơ chế chặn |

---

## Khi gặp lỗi

| Hiện tượng | Cách xử lý |
|------------|------------|
| `Khong tim thay mysql.exe` | Cài MySQL 8, hoặc thêm thư mục `bin` của MySQL vào PATH |
| `Access denied for user 'root'` | Gõ sai mật khẩu root. Chạy lại script |
| `port 8080 already in use` | Dự án khác đang chiếm cổng → `scripts\run.ps1 -Port 9090` |
| Sửa `.java` mà không thấy đổi | Ctrl+C rồi chạy lại `run.ps1` — file `.java` phải biên dịch lại |
| Sửa `.jsp` mà không thấy đổi | F5 là đủ. Vẫn không đổi thì restart |
