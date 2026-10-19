# [ISSUE-029] Tối Ưu Tương Tác Giao Diện, Modal Xác Nhận & Bảo Vệ Thao Tác Tránh Mất Xu/Dữ Liệu

- **Người thực hiện:** **Phát**
- **Thời gian xử lý:** 2026-10-18
- **Mức độ:** 🟢 Giao diện & Trải nghiệm người dùng (UX / UI Security Hardening)
- **Trạng thái:** ✅ Đã hoàn thành

---

## 1. Vấn đề phát hiện trong tương tác giao diện

1. **Hiển thị Modal trước khi kiểm tra tính hợp lệ của Form (`_confirm-modal.jsp`):**
   - Khi một form có các trường `required`, `min`, `max` nhưng nút submit mang `data-confirm`, việc click nút submit kích hoạt ngay hộp thoại xác nhận ("Bạn có chắc chắn muốn...") dù người dùng chưa điền thông tin. Sau khi bấm "Đồng ý", form mới hiển thị lỗi validation của trình duyệt, gây khó chịu cho người dùng.
2. **Thiếu hỗ trợ phím Escape trên các Modal:**
   - Người dùng bấm phím `Escape` trên bàn phím nhưng Modal xác nhận (`#appConfirmModalBackdrop`), Nạp xu nhanh (`#quickTopupModalBackdrop`), Mở khoá VIP (`#vipUnlockModalBackdrop`) và Điều chỉnh ví (`#adjustModal`) không tự đóng.
3. **Modal Điều chỉnh ví Admin (`admin/wallet.jsp`) dùng CSS inline cứng và thiếu xác nhận:**
   - Chưa dùng chung các class `.app-modal-backdrop` và `.app-modal-card` của Design System.
   - Bấm vào nền đen (backdrop) không đóng modal; nút submit không có `data-confirm` khiến admin dễ bấm nhầm điều chỉnh hàng ngàn xu vào tài khoản người dùng mà không có bước xác nhận lại.
4. **Trừ xu trực tiếp khi bấm Kích hoạt VIP (`user/me.jsp`):**
   - Nút `btn-vip-upgrade` gửi POST ngay lập tức trừ 60 / 150 / 500 xu trong ví của độc giả mà không có hộp thoại xác nhận.
5. **Xoá sạch toàn bộ lịch sử đọc (`user/history.jsp`):**
   - Nút `Xoá toàn bộ lịch sử` trong vùng nguy hiểm thực hiện ngay khi bấm, tiềm ẩn nguy cơ vô tình làm mất dữ liệu đọc dở.
6. **Kiểm duyệt bình luận trên trang đọc (`_comment.jsp`):**
   - Quyền gỡ bình luận trên giao diện chỉ kiểm tra `currentUser.admin`, khiến `MODERATOR` có quyền `MODERATE_COMMENTS` không nhìn thấy nút "gỡ" trực tiếp trên trang đọc; các nút "gỡ" cũng thiếu modal xác nhận trước khi xoá.

---

## 2. Giải pháp thực hiện

1. **Nâng cấp `_confirm-modal.jsp`:**
   - Bổ sung kiểm tra `form.checkValidity()` trước khi kích hoạt `showConfirmModal()`. Nếu form chưa hợp lệ, gọi `form.reportValidity()` để trình duyệt chỉ rõ trường cần nhập thay vì hiện hộp thoại xác nhận rỗng.
   - Thêm sự kiện phím `Escape` để đóng modal tức thời.
2. **Bổ sung phím Escape cho các modal khác:**
   - Thêm listener bàn phím cho `_quick-topup-modal.jsp`, `_vip-unlock-modal.jsp` và `admin/wallet.jsp`.
3. **Chuẩn hoá Modal điều chỉnh ví trong `admin/wallet.jsp`:**
   - Chuyển toàn bộ khung sang `.app-modal-backdrop` và `.app-modal-card`.
   - Bổ sung nút đóng `&times;`, cho phép click backdrop để đóng, và gắn `data-confirm` bảo vệ nút submit.
4. **Bảo vệ ví và dữ liệu với `data-confirm`:**
   - Thêm xác nhận trên `user/me.jsp` (Gói VIP).
   - Thêm xác nhận trên `user/history.jsp` (Xoá toàn bộ lịch sử).
   - Thêm xác nhận và quyền `MODERATE_COMMENTS` trên `_comment.jsp` (Gỡ bình luận và câu trả lời).
   - Thêm xác nhận trên `admin/comments.jsp` (Ẩn / Bỏ ẩn bình luận).

---

## 3. Các tệp đã thay đổi

| STT | Tệp tin | Thay đổi chính |
|:---:|---|---|
| 1 | [`src/main/webapp/WEB-INF/views/_partials/_confirm-modal.jsp`](../../../src/main/webapp/WEB-INF/views/_partials/_confirm-modal.jsp) | Kiểm tra `checkValidity()` trước khi hiện modal, thêm lắng nghe phím `Escape` |
| 2 | [`src/main/webapp/WEB-INF/views/_partials/_quick-topup-modal.jsp`](../../../src/main/webapp/WEB-INF/views/_partials/_quick-topup-modal.jsp) | Hỗ trợ đóng bằng phím `Escape` |
| 3 | [`src/main/webapp/WEB-INF/views/_partials/_vip-unlock-modal.jsp`](../../../src/main/webapp/WEB-INF/views/_partials/_vip-unlock-modal.jsp) | Hỗ trợ đóng bằng phím `Escape` |
| 4 | [`src/main/webapp/WEB-INF/views/admin/wallet.jsp`](../../../src/main/webapp/WEB-INF/views/admin/wallet.jsp) | Chuẩn hoá `adjustModal`, click backdrop dismiss, phím Escape, thêm `data-confirm` |
| 5 | [`src/main/webapp/WEB-INF/views/user/me.jsp`](../../../src/main/webapp/WEB-INF/views/user/me.jsp) | Thêm `data-confirm` xác nhận nâng cấp gói VIP |
| 6 | [`src/main/webapp/WEB-INF/views/user/history.jsp`](../../../src/main/webapp/WEB-INF/views/user/history.jsp) | Thêm `data-confirm` xác nhận xoá sạch lịch sử |
| 7 | [`src/main/webapp/WEB-INF/views/_partials/_comment.jsp`](../../../src/main/webapp/WEB-INF/views/_partials/_comment.jsp) | Đồng bộ quyền `MODERATE_COMMENTS` hiển thị nút gỡ, thêm `data-confirm` |
| 8 | [`src/main/webapp/WEB-INF/views/admin/comments.jsp`](../../../src/main/webapp/WEB-INF/views/admin/comments.jsp) | Thêm `data-confirm` cho nút Ẩn/Bỏ ẩn bình luận |
| 9 | [`scripts/verify_full_system.py`](../../../scripts/verify_full_system.py) | Bổ sung bài kiểm tra tự động 4.10 và 4.11 cho ISSUE-029 |

---

## 4. Kết quả kiểm thử

- **181/181** bài unit test chạy thành công không có lỗi.
- **43/43** bài kiểm thử tự động toàn hệ thống (bao gồm cả các ca kiểm thử bảo vệ giao diện mới) đều PASS 100%.
