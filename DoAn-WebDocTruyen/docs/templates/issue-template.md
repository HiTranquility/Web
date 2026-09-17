# 🏷️ ISSUE-NNN: [Tên việc, viết như một câu ra lệnh]

> Khuôn này copy thành `docs/projects/issues/ISSUE-NNN-ten-viec-khong-dau.md`.
> Luật chung: [`projects/README.md`](../README.md).
> **Xoá dòng trích dẫn này sau khi copy.**
>
> **[GOTCHA]** Đường dẫn trong khuôn tính từ **chỗ file copy ra sẽ nằm**, không phải
> từ `templates/`. Mở khuôn ở đây mà bấm link thì hỏng — đúng như vậy, đừng "sửa".

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-NNN |
| **Người làm** | *(để trống nếu chưa ai nhận)* |
| **Trạng thái** | 📝 Chưa nhận / 🚧 Đang làm / ✅ Xong / ⛔ Bỏ — **chọn một, xoá phần còn lại** |
| **Ngày mở** | YYYY-MM-DD |
| **CASE liên quan** | CASE 0N — *(xem lộ trình 11 CASE ở [`README.md`](../../../README.md))* |
| **Đụng vào** | `XxxServlet` · `XxxDAO` · `views/page/xxx.jsp` — **ghi tên file thật** |

---

## 1. Làm cái gì, và vì sao

*Hai ba câu. Người đọc là bạn cùng nhóm chưa từng mở phần này.*

- **Hiện tại:** [đang thiếu gì / đang bất tiện chỗ nào]
- **Sau khi xong:** [người dùng làm được gì mà giờ chưa làm được]

---

## 2. Xong là thế nào (tick được mới tính)

*Viết thành thứ **bấm thử được**, không viết cảm tính. "Giao diện đẹp hơn" không
kiểm được; "Bấm Báo cáo hiện hộp thoại, gửi xong thấy thông báo xanh" thì được.*

- [ ] …
- [ ] …
- [ ] Đã chạy thử trên trình duyệt thật, không chỉ biên dịch sạch

---

## 3. Các bước

*Chia nhỏ tới mức mỗi bước làm xong trong một lần ngồi.*

| # | Bước | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | [vd: thêm cột `report_count` vào bảng `stories`] | `database/schema.sql` | ☐ |
| 2 | [vd: viết `ReportDAO.insert()`] | `dao/ReportDAO.java` | ☐ |
| 3 | [vd: thêm `action=report` vào servlet] | `controller/StoryServlet.java` | ☐ |
| 4 | [vd: nút + hộp thoại ở trang chi tiết] | `views/page/detail.jsp` | ☐ |

> Có đụng **database** thì bước sửa `schema.sql` phải đứng đầu, và phải báo cả
> nhóm — người khác đang chạy schema cũ sẽ lỗi ngay khi pull về.

---

## 4. Quy ước phải theo

*Ghi **đúng số mục** cần đọc. Đừng viết "đọc standards đi" — sẽ không ai đọc.*

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên class/method, contract 4 tầng, URL | [`01-CODING §1 §2 §5`](../../standards/01-CODING_CONVENTIONS.md) |
| Attribute, scope, layout, `${}` vs `<c:out>` | [`02-VIEW §3 §4 §5`](../../standards/02-VIEW_CONVENTIONS.md) |
| Đặt tên bảng/cột, kiểu dữ liệu, khoá ngoại, luật DAO | [`03-DATABASE §2 §4`](../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit message, nhánh | [`04-GIT §1 §2`](../../standards/04-GIT_CONVENTIONS.md) |

*(Xoá dòng nào không liên quan.)*

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅. Bỏ trống = chưa xong, dù code đã viết.*

- **Bấm thử:** [đi từ trang nào, bấm gì, thấy gì]
- **Thử trường hợp xấu:** [bỏ trống ô bắt buộc · nhập chữ vào ô số · chưa đăng nhập mà gọi thẳng URL]
- **Chạy lại test:** `mvn test` — [số test pass / lỗi gì]

---

## 6. Ghi chú

*Chỗ mắc, thứ cố tình chưa làm, thứ người sau cần biết. Để trống được.*
