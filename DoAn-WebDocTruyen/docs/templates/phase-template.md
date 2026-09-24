# 🚀 ISSUE-NNN — Phase N: [Tên đợt]

> **Đích:** `docs/projects/issues/ISSUE-NNN-slug/phase-N.md` — nằm TRONG thư mục issue. · **Luật chung:** [projects/README](../../README.md)
>
> **[GOTCHA]** Đường dẫn trong khuôn tính từ **chỗ file copy ra sẽ nằm** (`docs/projects/issues/ISSUE-NNN-slug/`). Mở khuôn ở `templates/` mà bấm link thì hỏng — đúng như vậy, đừng "sửa".

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [issue.md](issue.md) — *cùng thư mục* |
| **Đợt** | Phase N / tổng M đợt |
| **Người làm** | *(một người thôi)* |
| **Trạng thái** | 📝 Chưa nhận / 🚧 Đang làm / ✅ Xong / ⛔ Bỏ — **chọn một** |
| **Ngày bắt đầu** | YYYY-MM-DD |
| **Đụng vào** | `XxxServlet` · `XxxDAO` · `views/...` — **ghi tên file thật** |

---

## 1. Đợt này làm tới đâu

*Một hai câu. Phần nào của ISSUE gốc nằm trong đợt này, phần nào để đợt sau.*

- **Trong đợt này:** …
- **Để đợt sau:** …

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

Không phải "việc dài quá nên cắt đôi cho dễ thở". Tách khi trộn vào thì **hỏng là không biết hỏng do đâu**:

| Tách ra khi | Vì |
|---|---|
| Có sửa `database/schema.sql` | Đổi schema mà lẫn với code mới → lỗi ra thì không rõ do bảng hay do câu SQL. Sửa schema **luôn đứng riêng một đợt và làm trước**. |
| Có đụng `filter/` (`AuthFilter`, `CsrfFilter`, `AdminFilter`) | Sai một dòng ở filter là **thủng bảo mật toàn site**, không phải hỏng một trang. |
| Có đổi thứ đang chạy đúng | Trộn "thêm mới" với "sửa cái cũ" → test đỏ không biết tại nhánh nào. |
| Hai người phải làm nối tiếp nhau | Người sau cần người trước xong hẳn mới bắt đầu được. |

Việc gọn, một mình, không đụng schema/filter thì **đừng chia đợt** — viết thẳng trong `issue.md` là đủ.

**Lý do tách của đợt này:** …

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [Phase / ISSUE nào, hoặc "không có"]
- **Xong đợt này mới mở khoá được:** [Phase / ISSUE nào, hoặc "không có"]

> Có ô nào không trống thì **nói cho người kia biết** — đừng để họ ngồi chờ mà không biết mình đang chờ ai.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | … | … | ☐ |
| 2 | … | … | ☐ |

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

> **[MUST] Điền số thật.** Ghi "OK" hay "chạy được" là vô dụng.

| Lệnh kiểm tra | Kết quả **trước** đợt này |
|---|---|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | [vd: 28 pass / 0 fail] |
| Biên dịch code (`scripts\run.ps1` hoặc `javac`) | [vd: biên dịch sạch, 0 lỗi] |
| Mở site chạy thử | [vd: trang chủ + đăng nhập + đọc chương đều bình thường] |

**[GOTCHA]** Không có baseline thì lúc xong đợt sẽ không phân biệt được **lỗi mình vừa gây ra** với **lỗi vốn đã có sẵn**. Đến lúc đó phải `git stash` để đo ngược lại — mất thời gian gấp đôi so với việc ghi nhận ngay bây giờ.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` — đo lại, **không được tụt** so với bảng §4
- [ ] **Bằng tay:** đi đúng luồng vừa sửa
  1. …
  2. …
  3. Kết quả mong đợi: …
- [ ] **Thử trường hợp xấu:** bỏ trống ô bắt buộc · chưa đăng nhập mà gọi thẳng URL · nhập chữ vào ô số
- [ ] **Thứ đang chạy đúng vẫn chạy đúng** — không chỉ kiểm phần mình vừa làm

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở bảng Baseline đo lại **bằng hoặc tốt hơn** trước đợt.
- [ ] Đã commit, dòng đầu ghi mã: `ISSUE-NNN phase-N — <mô tả ngắn>`, ghi rõ những gì **chưa** làm được (nếu có) — không im lặng bỏ qua.
- [ ] Viết file `review-N.md` đối chiếu baseline & Acceptance Criteria trước khi chuyển sang phase kế.
- [ ] Cập nhật bảng đợt ở [`issue.md`](issue.md) thành ✅.

---

## 7. Ghi chú

*Chỗ mắc, thứ cố tình chưa làm, thứ người sau cần biết. Để trống được.*
