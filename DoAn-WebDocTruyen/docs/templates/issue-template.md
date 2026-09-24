# 🏷️ ISSUE-NNN: [Feature/Chore] Tên việc, viết như một câu ra lệnh

> **Đích:** `docs/projects/issues/ISSUE-NNN-slug/issue.md` — issue là **thư mục**: file này (`issue.md`) + `phase-N.md` + `review-N.md`. **[NEVER]** tạo file phẳng — không có chỗ đặt phase. · **Luật chung:** [projects/README](../../README.md)
>
> **[GOTCHA]** Đường dẫn trong khuôn tính từ **chỗ file copy ra sẽ nằm** (`docs/projects/issues/ISSUE-NNN-slug/`). Mở khuôn ở `templates/` mà bấm link thì hỏng — đúng như vậy, đừng "sửa".

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-NNN |
| **Người làm** | *(để trống nếu chưa ai nhận)* |
| **Trạng thái** | 📝 Chưa nhận / 🚧 Đang làm / ✅ Xong / ⛔ Bỏ — **chọn một** |
| **Ngày mở** | YYYY-MM-DD |
| **CASE liên quan** | CASE 0N — *(xem lộ trình 11 CASE ở [`README.md`](../../../../README.md))* |
| **Đụng vào** | `XxxServlet` · `XxxDAO` · `views/common/...` — **ghi tên file thật** |

---

## 1. Làm cái gì, và vì sao (Goal)

*Hai ba câu. Người đọc là bạn cùng nhóm chưa từng mở phần này.*

- **Hiện tại:** [đang thiếu gì / đang bất tiện chỗ nào]
- **Sau khi xong:** [người dùng làm được gì mà giờ chưa làm được]

---

## 2. Xong là thế nào (Acceptance Criteria)

*Viết thành thứ **bấm thử được**, không viết cảm tính. "Giao diện đẹp hơn" không kiểm được; "Bấm Báo cáo hiện hộp thoại, gửi xong thấy thông báo xanh" thì được.*

- [ ] …
- [ ] …
- [ ] Đã chạy thử trên trình duyệt thật, không chỉ biên dịch sạch

---

## 3. Các bước / Chia phase

*Nếu việc lớn, tách thành các file `phase-1.md`, `phase-2.md`... trong cùng thư mục này.*

| # | Bước / Phase | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | [vd: Phase 1 — thêm bảng `reports` vào CSDL] | `database/schema.sql` | ☐ |
| 2 | [vd: Phase 2 — viết `ReportDAO` + `ReportServlet`] | `dao/ReportDAO.java` · `controller/...` | ☐ |
| 3 | [vd: Phase 3 — nút + hộp thoại ở giao diện] | `views/common/...` | ☐ |

> Có đụng **database** thì bước sửa `schema.sql` phải đứng đầu và tách phase riêng — người khác đang chạy schema cũ sẽ lỗi ngay khi pull về.

---

## 4. ↩️ Kế hoạch quay lui (Rollback Plan)

*Trả lời trước khi làm: revert commit là đủ, hay có sửa schema / dữ liệu / file upload không tự quay lui được?*

- **Revert code:** `git revert <commit-hash>`
- **Dữ liệu / CSDL:** [Có cần script rollback SQL không? Nếu có, ghi rõ tên file script]

---

## 5. 🚫 Ngoài phạm vi (Non-goals)

*Những việc liên quan nhưng CỐ TÌNH không làm trong issue này — để tránh phình phạm vi khi làm phase.*

- Không làm: ...
- Không làm: ...

---

## 6. 🔐 Cửa kiểm soát an toàn (Security Gate)

*Bắt buộc trả lời trước khi code — không đụng thì ghi "Không đụng".*

- **Có thêm form nhận dữ liệu từ người dùng / endpoint mới?** [Validate ở Servlet nào? Đã kiểm tra CSRF token chưa?]
- **Có thao tác nhạy cảm (đổi mật khẩu, xóa dữ liệu, phân quyền)?** [Kiểm tra role ở đâu? Khách hay User thường có gọi trộm được không?]
- **Có nguy cơ rò rỉ secret / API key?** [Đã đưa file cấu hình vào `.gitignore` chưa?]

---

## 7. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên class/method, contract 4 tầng, URL | [`01-CODING §1 §2 §5`](../../../standards/01-CODING_CONVENTIONS.md) |
| Attribute, scope, layout, `${}` vs `<c:out>` | [`02-VIEW §3 §4 §5`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Đặt tên bảng/cột, kiểu dữ liệu, khoá ngoại, luật DAO | [`03-DATABASE §2 §4`](../../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit message, nhánh | [`04-GIT §1 §2`](../../../standards/04-GIT_CONVENTIONS.md) |

---

## 8. Đã kiểm thế nào

*Điền lúc chuyển sang ✅. Bỏ trống = chưa xong, dù code đã viết.*

- **Bấm thử:** [đi từ trang nào, bấm gì, thấy gì]
- **Thử trường hợp xấu:** [bỏ trống ô bắt buộc · nhập chữ vào ô số · chưa đăng nhập mà gọi thẳng URL]
- **Chạy lại test:** `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` — [kết quả pass/fail]

---

## 9. Ghi chú

*Chỗ mắc, thứ cố tình chưa làm, thứ người sau cần biết. Để trống được.*
