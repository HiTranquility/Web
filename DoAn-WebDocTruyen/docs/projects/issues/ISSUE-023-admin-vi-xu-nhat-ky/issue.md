# 🏷️ ISSUE-023: Admin — trang Ví xu và Nhật ký thao tác

> **Đích:** `docs/projects/issues/ISSUE-023-admin-vi-xu-nhat-ky/issue.md` · **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-023 |
| **Người làm** | N1 — Nền tảng & Quản trị |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày mở** | 2026-09-23 |
| **CASE liên quan** | CASE 10 — Quản trị *(mở rộng)* · [`ISSUE-008`](../ISSUE-008-ung-ho-tac-gia-xu-ao/issue.md) |
| **Đụng vào** | `database/schema.sql` · `admin/AdminWalletServlet` *(mới)* · `admin/AdminAuditServlet` *(mới)* · `dao/AuditDAO` *(mới)* · `dao/WalletDAO` · `AdminStoryServlet` · `AdminUserServlet` · `AdminCommentServlet` · `views/admin/wallet.jsp` *(mới)* · `views/admin/audit.jsp` *(mới)* · `views/layout/admin.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

Hai lỗ hổng **giám sát** — không phải lỗ hổng bảo mật, mà là chỗ admin không nhìn thấy gì:

- **Hiện tại:**
  1. [`ISSUE-008`](../ISSUE-008-ung-ho-tac-gia-xu-ao/issue.md) đưa **tiền ảo** vào hệ thống.
     Sidebar quản trị có đúng 6 mục — *Tổng quan · Truyện · Tài khoản · Thể loại · Bình luận
     · Báo cáo* — và **không mục nào** nhìn thấy được một giao dịch xu nào. Bảng
     `transactions` đang tồn tại mà không có màn hình nào đọc nó. Ai đó tặng nhầm 10.000 xu
     thì không có chỗ nào để phát hiện, và cũng không có chỗ nào để sửa.
  2. Admin gỡ truyện, khoá tài khoản, ẩn bình luận — **không chỗ nào ghi lại ai làm, lúc
     nào, vì sao**. Nhóm có ba người, sắp tới có thể có hai admin. Lúc một truyện biến mất
     thì câu hỏi *"ai gỡ?"* hiện không trả lời được.
- **Sau khi xong:** sidebar có thêm **💰 Ví xu** và **📜 Nhật ký**. Ví xu xem được số dư
  mọi tài khoản, dòng tiền, và admin cấp/thu xu **có ghi lý do**. Nhật ký ghi lại mọi thao
  tác quản trị, chỉ đọc, không xoá được.

---

## 2. Tiêu chí nghiệm thu (Acceptance Criteria)

**Trang Ví xu**

- [ ] `/admin/wallet` liệt kê mọi ví: tên tài khoản, số dư, tổng đã nhận, tổng đã tiêu —
      có phân trang và ô tìm theo tên.
- [ ] Bốn ô thống kê ở đầu trang: **tổng xu đang lưu hành · số giao dịch · xu đã tặng ·
      xu đã tiêu mở khoá** *(ô cuối chỉ có nếu [`ISSUE-020`](../ISSUE-020-mo-khoa-chuong-bang-xu/issue.md) đã xong)*.
- [ ] Bảng giao dịch gần nhất: ai → ai, bao nhiêu, loại gì, lúc nào. Lọc được theo loại.
- [ ] Admin **cấp xu** cho một tài khoản — **bắt buộc điền lý do**, bỏ trống thì không gửi được.
- [ ] Admin **thu hồi xu** — không cho số dư xuống dưới 0.
- [ ] Mỗi lần cấp/thu sinh **một** dòng `transactions` **và một** dòng nhật ký.
- [ ] **Tổng xu toàn hệ thống** hiện ở đầu trang phải **khớp** với `SELECT SUM(balance) FROM wallets`.

**Trang Nhật ký**

- [ ] `/admin/audit` liệt kê thao tác mới nhất trước: ai · làm gì · lên đối tượng nào · lý do · lúc nào.
- [ ] Ghi được ít nhất **6 loại**: gỡ truyện · khôi phục truyện · khoá tài khoản · mở khoá
      tài khoản · ẩn bình luận · cấp/thu xu.
- [ ] Lọc theo admin, theo loại thao tác, theo khoảng ngày.
- [ ] **Không có nút xoá ở bất kỳ đâu.** Gọi thẳng URL kiểu `?action=delete` cũng **không
      có route nào nhận** *(§3.2)*.
- [ ] Gỡ một truyện ở `/admin/story` → nhật ký xuất hiện dòng mới **ngay**, ghi đúng tên admin.

**Chung**

- [ ] Tài khoản **không phải admin** gọi `/admin/wallet` hoặc `/admin/audit` → **403**.
- [ ] `scripts\test.ps1` — không tụt so với 101 pass.

---

## 3. Các bước

| # | Bước | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Bảng `audit_logs` vào `schema.sql` + migration *(§3.1)* | `database/` | ☐ |
| 2 | `AuditDAO.log(adminId, action, targetType, targetId, reason)` + `find(...)` có lọc | `dao/AuditDAO.java` | ☐ |
| 3 | Gọi `AuditDAO.log()` ở **6 chỗ** đang có thao tác quản trị | `AdminStoryServlet` · `AdminUserServlet` · `AdminCommentServlet` | ☐ |
| 4 | `AdminAuditServlet` + `views/admin/audit.jsp` — chỉ đọc | `admin/` · `views/admin/` | ☐ |
| 5 | `WalletDAO` thêm `findAllWallets(...)`, `summary()`, `adminAdjust(...)` | `dao/WalletDAO.java` | ☐ |
| 6 | `AdminWalletServlet` + `views/admin/wallet.jsp` | `admin/` · `views/admin/` | ☐ |
| 7 | Thêm 2 mục vào sidebar quản trị | `views/layout/admin.jsp` | ☐ |
| 8 | Test `AuditDAOTest` + `AdminWalletTest` | `src/test/java/truyen/` | ☐ |

> **Việc này KHÔNG chia đợt** dù có sửa `schema.sql`, vì bảng `audit_logs` là bảng **mới
> hoàn toàn**, không sửa bảng nào đang chạy và không có khoá ngoại nào trỏ ngược vào nó.
> Rủi ro thấp hơn hẳn ISSUE-020 và ISSUE-022 — luật ở [`projects/README`](../README.md)
> nói rõ *"đa số việc KHÔNG cần chia đợt"*.
>
> Nhưng **vẫn phải làm bước 1 trước và commit riêng**, và vẫn phải **báo cả nhóm**.

### 3.1 Bảng `audit_logs`

```sql
-- =============================================================================
--  audit_logs — nhật ký thao tác quản trị  (ISSUE-023)
-- =============================================================================
--  CHỈ GHI THÊM, KHÔNG SỬA, KHÔNG XOÁ. Sổ mà sửa được thì không còn là sổ.
--  Không có DAO nào viết UPDATE hay DELETE lên bảng này — xem §3.2.
--
--  target_id KHÔNG có khoá ngoại, CÓ CHỦ Ý: nó trỏ sang stories, users hoặc
--  comments tuỳ dòng. Cùng một đánh đổi đã ghi ở bảng reports, và ở đây còn
--  hợp lý hơn: khoá ngoại có ON DELETE CASCADE sẽ XOÁ MẤT nhật ký khi đối
--  tượng bị xoá — đúng lúc người ta cần tra nhất.
-- =============================================================================
CREATE TABLE audit_logs (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    -- Admin đã thực hiện. SET NULL chứ không CASCADE: xoá tài khoản admin cũ
    -- thì nhật ký VẪN CÒN, chỉ mất tên người. Mất tên còn hơn mất cả dòng.
    admin_id    INT NULL,

    action      ENUM('DELETE_STORY','RESTORE_STORY',
                     'BAN_USER','UNBAN_USER',
                     'HIDE_COMMENT',
                     'GRANT_COIN','REVOKE_COIN') NOT NULL,

    target_type ENUM('STORY','USER','COMMENT','WALLET') NOT NULL,
    target_id   INT NOT NULL,

    -- Ảnh chụp tên đối tượng lúc thao tác. Truyện bị xoá hẳn sau này thì
    -- nhật ký vẫn đọc được là "đã gỡ truyện Kiếm Khí Trường Sinh", không
    -- phải "đã gỡ truyện #37".
    target_label VARCHAR(200),

    reason      VARCHAR(500),

    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE SET NULL,

    -- Câu nóng nhất: "thao tác gần đây nhất".
    INDEX idx_audit_time (created_at DESC),
    INDEX idx_audit_admin (admin_id, created_at DESC)
) ENGINE=InnoDB;
```

### 3.2 "Không xoá được" phải là thật, không phải là thiếu nút

> **[NEVER] chỉ bỏ nút Xoá ở JSP rồi coi là sổ không sửa được.** Bỏ nút chặn được người
> dùng bình thường, không chặn được ai gõ URL. Cùng bài học với
> [`ISSUE-001 phase 3`](../ISSUE-001-tich-hop-google/phase-3.md) và với
> [`CHECKLIST §1`](../../../guides/CHECKLIST.md).
>
> Cách làm cho thật:
> 1. `AdminAuditServlet` **chỉ có `doGet`**, không có `doPost`. Không có route nào để gọi.
> 2. `AuditDAO` **không có** phương thức `delete` hay `update`. Không viết ra thì không gọi nhầm.
> 3. Tốt nhất: tài khoản MySQL của ứng dụng chỉ được `INSERT, SELECT` trên `audit_logs` —
>    xem [`database/grant.sql`](../../../../database/grant.sql). Tầng này là tầng duy nhất
>    một lỗi lập trình không phá qua được.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên `AuditDAO`, `AdminWalletServlet`, URL `/admin/*` | [`01-CODING §1 §2 §5`](../../../standards/01-CODING_CONVENTIONS.md) |
| Layout `admin`, attribute, `<c:out>` cho lý do và tên | [`02-VIEW §3 §4 §5`](../../../standards/02-VIEW_CONVENTIONS.md) |
| Bảng `audit_logs`, ENUM, khoá ngoại `SET NULL` | [`03-DATABASE §2 §4`](../../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit message | [`04-GIT §2`](../../../standards/04-GIT_CONVENTIONS.md) |

**Luật riêng:**

> **[MUST] Cấp/thu xu và ghi nhật ký nằm trong MỘT transaction.** Cấp xong mà nhật ký
> không ghi là có xu từ trên trời rơi xuống và không ai giải thích được. Cùng khuôn mẫu
> với [`ISSUE-020 phase 2 §3.3`](../ISSUE-020-mo-khoa-chuong-bang-xu/phase-2.md).

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅.*

- **Bấm thử:**
- **Thử trường hợp xấu:** tài khoản thường gọi `/admin/wallet` · thu xu nhiều hơn số dư ·
  cấp xu không điền lý do · gọi `POST /admin/audit`
- **Chạy lại test:** `scripts\test.ps1` —

---

## 6. Ghi chú

**Vì sao nhật ký đáng làm ở một đồ án học phần.** Nghe như thứ chỉ hệ thống thật mới cần.
Nhưng đây là việc **rẻ nhất trong đợt 2** — một bảng, một DAO, sáu lời gọi — mà lúc bảo vệ
thì nó là thứ kể được: *"nhóm em có hai admin, và mọi thao tác gỡ nội dung đều truy ngược
được người làm"*. Nó cũng là câu trả lời sẵn cho câu hỏi *"nếu admin lạm quyền thì sao?"*.

**Thứ cố tình không làm:**

| Bỏ | Vì |
|---|---|
| Ghi nhật ký cả thao tác của người dùng thường | Bảng sẽ phình bằng `view_logs`, mà 99% dòng không ai đọc. Nhật ký chỉ có ý nghĩa cho hành động **có quyền lực**. |
| Khôi phục từ nhật ký *(undo)* | Muốn undo thì phải lưu cả trạng thái trước — thành một hệ phiên bản, to hơn cả ISSUE này. Xoá mềm đã cho khôi phục truyện rồi. |
| Xuất nhật ký ra CSV | Làm khi có người thật sự cần. |
| Cảnh báo khi admin thao tác bất thường | Cần ngưỡng, cần nơi gửi cảnh báo — một việc riêng. |
