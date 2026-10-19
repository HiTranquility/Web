# 🏷️ ISSUE-026: Đồng bộ Modern Confirm Modal, gom CSS Modal và dọn ~430 dòng trùng lặp trong JSP

> **Đích:** `docs/projects/issues/ISSUE-026-dong-bo-modal-confirm-quicktopup-va-toi-uu-css/issue.md`
> **Luật chung:** [projects/README](../../README.md)

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-026 |
| **Người làm** | Phát |
| **Trạng thái** | ✅ Đã xong |
| **Ngày mở** | 2026-10-18 |
| **CASE liên quan** | CASE 05 · ISSUE-011 · ISSUE-024 |
| **Đụng vào** | `views/_partials/_confirm-modal.jsp` · `views/_partials/_quick-topup-modal.jsp` · `views/_partials/_vip-unlock-modal.jsp` · `assets/css/components.css` · `views/user/edit.jsp` |

---

## 1. Làm cái gì, và vì sao (Goal)

- **Hiện tại:** 
  1. Ba modal mới (`_confirm-modal.jsp`, `_quick-topup-modal.jsp`, `_vip-unlock-modal.jsp`) đang chứa hơn 430 dòng CSS trong các thẻ `<style>` nội tuyến (với nhiều `!important`), trong khi phần lớn thuộc tính đã được copy vào cuối `components.css`. Điều này làm phình to dung lượng HTML ở mọi trang nạp modal (`main.jsp`, `reader.jsp`, `admin.jsp`).
  2. Vẫn còn sót hộp thoại `window.confirm()` mặc định xấu xí của trình duyệt ở form Hủy liên kết Google ([`edit.jsp:133`](../../../../src/main/webapp/WEB-INF/views/user/edit.jsp#L133)).
  3. Logic lọc từ khoá cảnh báo nguy hiểm trong `_confirm-modal.jsp` phân biệt hoa thường và chưa nhận diện cả 2 dạng dấu tiếng Việt ("xoá" vs "xóa").
- **Sau khi xong:**
  1. Toàn bộ CSS modal nằm gọn gàng trong `components.css`, 3 file modal JSP sạch bóng không còn thẻ `<style>`, tải trang nhẹ hơn.
  2. 100% các hành động xoá/huỷ trên website dùng chung giao diện Modern Confirm Modal đồng bộ và đẹp mắt.

---

## 2. Xong là thế nào (Acceptance Criteria)

- [x] Di chuyển các style backdrop (`#appConfirmModalBackdrop`, `#quickTopupModalBackdrop`, `#vipUnlockModalBackdrop`) và animation còn thiếu vào `components.css`.
- [x] Xoá sạch thẻ `<style>...</style>` trong `_confirm-modal.jsp`, `_quick-topup-modal.jsp`, và `_vip-unlock-modal.jsp`.
- [x] Thay thế `onsubmit="return confirm(...)"` ở `edit.jsp:133` bằng `data-confirm="Bạn có chắc chắn muốn hủy liên kết tài khoản Google?" data-confirm-title="Xác nhận hủy liên kết" data-confirm-ok="Đồng ý hủy"`.
- [x] Cập nhật JavaScript trong `_confirm-modal.jsp` dùng `toLowerCase()` và hỗ trợ cả `xóa`, `xoá`, `gỡ`, `hủy`, `khoá`.
- [x] Kiểm tra giao diện trên trình duyệt: 3 modal mở đóng mượt mà, hiệu ứng blur nền đẹp mắt, hỗ trợ cả Dark mode và Light mode.

---

## 3. Các bước triển khai (Hướng dẫn cho Phát)

| # | Bước | Chạm vào |
|---|---|---|
| 1 | Bổ sung class backdrop và popup animation hoàn chỉnh vào `components.css` | `src/main/webapp/assets/css/components.css` |
| 2 | Xoá toàn bộ thẻ `<style>` ở cuối 3 file JSP modal | `src/main/webapp/WEB-INF/views/_partials/*.jsp` |
| 3 | Chuyển form huỷ liên kết Google sang `data-confirm` | `src/main/webapp/WEB-INF/views/user/edit.jsp` |
| 4 | Thêm `*.log` vào `.gitignore` và xoá file rác `hs_err_pid28388.log` | `.gitignore` |

---

## 4. ↩️ Kế hoạch quay lui (Rollback Plan)

- **Revert code:** `git checkout -- src/main/webapp/assets/css/components.css src/main/webapp/WEB-INF/views/`
- **Dữ liệu / CSDL:** Không đụng CSDL.
