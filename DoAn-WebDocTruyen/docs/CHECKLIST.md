# ✅ Checklist trước khi nộp bài

Đi từ trên xuống. Mục nào cũng có **cách kiểm** cụ thể, không chỉ "nhớ làm".

---

## 1. Bảo mật — làm trước, đây là chỗ mất điểm nặng nhất

- [ ] **Không có mật khẩu nào trong code**
  ```bash
  git ls-files | xargs grep -ilE "password.*=.*['\"].{4,}" 2>/dev/null
  ```
  Chỉ được ra `db.properties.example` (mẫu) và `setup_user.sql` (có ghi chú
  "đổi mật khẩu"). Ra file `.java` nào là **phải sửa**.

- [ ] **`db.properties` chưa từng bị commit**
  ```bash
  git log --all --name-only | grep "db.properties$" | grep -v example
  ```
  Ra kết quả nghĩa là mật khẩu đã nằm trong lịch sử git → **đổi mật khẩu MySQL**,
  xoá file ở commit sau là không đủ.

- [ ] **Mọi truy vấn dùng `PreparedStatement`, không nối chuỗi SQL**
  ```bash
  grep -rn "executeQuery(\"\|\" + .* + \"" src/main/java/truyen/dao/
  ```

- [ ] **Dữ liệu người dùng nhập đều qua `<c:out>`**
  Kiểm tay: mở từng `.jsp`, tìm `${story.title}`, `${comment.content}`,
  `${user.displayName}` để trần → bọc `<c:out value="..."/>`.
  Test nhanh: đăng truyện tên `<b>ĐẬM</b>`. Hiện ra chữ đậm = **thủng XSS**.

- [ ] **Sửa truyện có kiểm tra quyền sở hữu**
  Đăng nhập tài khoản A, mở `/story?action=edit&id=<truyện của B>`.
  Phải ra **403**, không phải form sửa.

- [ ] **Mật khẩu lưu dạng băm, không phải chữ thường**
  ```sql
  SELECT username, password_hash FROM users LIMIT 3;
  ```
  Phải thấy chuỗi `$2a$10$...`, không phải `123456`.

---

## 2. Dọn code demo

- [ ] Xoá khối "Chi tiết kỹ thuật" trong `views/page/_error500.jsp`
      — nó phơi tên lớp, thông điệp lỗi cho người dùng
- [ ] Xoá `System.out.println` còn sót
  ```bash
  grep -rn "System.out.println" src/main/java/
  ```
- [ ] Xoá code chết, biến không dùng, `TODO` chưa làm
  ```bash
  grep -rn "TODO\|FIXME\|XXX" src/main/
  ```
- [ ] Xoá khối cảnh báo `dbError` ở `home.jsp` nếu database đã chạy ổn

---

## 3. Chạy thử toàn bộ luồng

Với **database sạch** (chạy lại `schema.sql` + `sample_data.sql`):

- [ ] Đăng ký tài khoản mới → đăng nhập được
- [ ] Đăng nhập sai mật khẩu → báo lỗi, **không** vào được
- [ ] Đăng truyện mới, thêm 2 chương
- [ ] Đọc chương, chuyển chương trước/sau
- [ ] Lọc truyện theo tag → đúng kết quả
- [ ] Bình luận, đánh dấu, tải `.txt`
- [ ] Đăng xuất → các trang cần đăng nhập đá về `/auth?action=login`
- [ ] Tài khoản admin: gỡ truyện, ban tài khoản
- [ ] Tài khoản **bị ban** → không đăng nhập được
- [ ] Gõ URL bịa → trang 404 của mình, không phải trang Tomcat
- [ ] Vào `/admin/user` bằng tài khoản thường → **403**

---

## 4. Tiếng Việt và hiển thị

- [ ] Đăng truyện tên `Đường Về Cố Hương 🌙` → hiện đúng, không ra `?????`
- [ ] Bình luận có emoji → lưu và hiện đúng
- [ ] Kiểm trong database:
  ```sql
  SELECT title FROM stories WHERE id = <id vừa tạo>;
  ```
- [ ] Thu nhỏ cửa sổ trình duyệt → layout không vỡ
- [ ] Trang lỗi ở URL nhiều cấp (`/a/b/c`) **vẫn có CSS**

---

## 5. Database

- [ ] `schema.sql` chạy được trên máy trắng, không lỗi
- [ ] `sample_data.sql` chạy **lại nhiều lần** vẫn được
- [ ] Dùng `utf8mb4`, không phải `utf8`
- [ ] App dùng tài khoản `truyen_app`, **không phải `root`**
  ```bash
  grep "db.username" src/main/resources/db.properties
  ```

---

## 6. Tài liệu

- [ ] `README.md` — hướng dẫn cài đặt chạy được với người chưa từng thấy dự án
- [ ] `docs/cau-truc.md` khớp với thư mục thật
- [ ] `docs/standards/` — code thật tuân theo, không mâu thuẫn
- [ ] Có ERD trong `docs/`
- [ ] Xoá doc nói về thứ chưa làm

---

## 7. Git

- [ ] Không còn thay đổi chưa commit
  ```bash
  git status --porcelain
  ```
- [ ] Đã push hết
  ```bash
  git status -sb | head -1     # phải KHÔNG có [ahead N]
  ```
- [ ] `git log --oneline` đọc hiểu được, không toàn `update` / `fix`
- [ ] Đánh thẻ bản nộp
  ```bash
  git tag -a nop-bai-cuoi-ky -m "Bản nộp đồ án cuối kỳ"
  git push origin nop-bai-cuoi-ky
  ```
- [ ] **Cân nhắc đổi repo sang private** trước khi nộp

---

## 8. Thử nghiệm cuối — máy trắng

Cách duy nhất chắc chắn: clone ra thư mục mới rồi làm theo đúng README.

```bash
cd /tmp && git clone https://github.com/HiTranquility/Web thu-nghiem
cd thu-nghiem/DoAn-WebDocTruyen
# làm theo README từng bước, không dùng trí nhớ
```

- [ ] Clone → chạy được, không phải sửa gì ngoài `db.properties`
- [ ] Không thiếu file nào (thứ bị `.gitignore` nhầm)

> Bước này hay lòi ra file quan trọng lỡ bị `.gitignore` chặn — và lúc đó
> thầy cô mới là người phát hiện thì đã muộn.
