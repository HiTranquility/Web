# 🐞 bug-NNN: [Triệu chứng, viết như lúc kể cho người khác nghe]

> Khuôn này copy thành `docs/projects/bugs/bug-NNN-ten-loi-khong-dau.md`.
> Luật chung: [`projects/README.md`](../README.md).
> **Xoá dòng trích dẫn này sau khi copy.**
>
> **[GOTCHA]** Đường dẫn trong khuôn tính từ **chỗ file copy ra sẽ nằm**, không phải
> từ `templates/`. Mở khuôn ở đây mà bấm link thì hỏng — đúng như vậy, đừng "sửa".

## 📌 Meta

| | |
|---|---|
| **Mã** | bug-NNN |
| **Mức** | 🔴 Chặn / 🟠 Nặng / 🟡 Nhẹ — **chọn một** *(bảng giải nghĩa ở [`bugs/README.md`](README.md))* |
| **Người sửa** | *(để trống nếu chưa ai nhận)* |
| **Trạng thái** | 🆕 Mới / 🛠️ Đang sửa / ✅ Đã sửa / ⛔ Không sửa — **chọn một** |
| **Ngày phát hiện** | YYYY-MM-DD |
| **Nơi xảy ra** | `XxxServlet` · `XxxDAO` · `views/…` · `filter/…` — điền được bao nhiêu thì điền |

---

## 1. Tái hiện thế nào

*Phần quan trọng nhất. Viết trước, viết kỹ. Ai đọc cũng phải làm ra được đúng lỗi đó.*

1. Đăng nhập bằng tài khoản […] *(tài khoản mẫu mật khẩu `123456`)*
2. Vào […]
3. Bấm […]

**Thấy:** [cái sai đang xảy ra]
**Đáng ra phải:** [cái đúng]

> Lúc nào cũng lỗi hay thỉnh thoảng mới lỗi? **Ghi rõ.** Lỗi lúc được lúc không
> thường là do dữ liệu cũ hoặc hai request chạy chen nhau — biết trước đỡ mò lâu.

---

## 2. Chứng cứ

*Dán thứ máy in ra, đừng kể lại bằng lời.*

```text
[stack trace trong console Tomcat, hoặc câu SQL, hoặc lỗi đỏ ở Console của trình duyệt]
```

Ảnh chụp màn hình thì để `docs/projects/bugs/anh/bug-NNN-1.png` rồi nhúng vào đây.

---

## 3. Nguyên nhân

*Điền khi đã tìm ra. Chưa biết thì ghi "chưa tìm ra" — đừng đoán bừa rồi sửa nhầm chỗ.*

- **Sai ở:** `file.java:dòng`
- **Vì:** [giải thích một câu]

---

## 4. Sửa thế nào

- **Đã đổi:** [file nào, đổi gì]
- **Vì sao chọn cách này:** [nếu có cách khác mà bỏ qua thì nói lý do]

---

## 5. Kiểm lại

- [ ] Làm lại đúng các bước ở §1 — **không còn lỗi**
- [ ] Thử trường hợp ngược lại — thứ đang chạy đúng **vẫn chạy đúng**
- [ ] `mvn test` — 28 test cũ vẫn pass
- [ ] Lỗi này đáng có test riêng không? *(có → thêm, ghi tên test vào đây)*

> **[Bẫy]** Lỗi sai dữ liệu thì sửa code chưa đủ — dữ liệu hỏng đã nằm trong
> database rồi. Ghi rõ có cần chạy câu SQL dọn dẹp không, và đã chạy chưa.

---

## 6. Ghi chú

*Lỗi này có họ hàng với bug nào khác không? Chỗ nào còn ngờ ngợ? Để trống được.*
