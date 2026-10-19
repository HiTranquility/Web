# 🚀 ISSUE-030: Chuẩn Hoá Modal Tặng Xu & Nâng Tầm Gamification UX

- **Mã định danh:** `ISSUE-030`
- **Mức độ:** 🟢 Giao diện / Tương tác UI/UX & Bảo vệ tài sản người dùng
- **Người thực hiện:** **Phát**
- **Ngày phát hiện & xử lý:** 2026-10-19
- **Trạng thái:** ✅ Đã hoàn thành (Verified PASS)

---

## 1. Mục tiêu & Vấn đề xử lý
1. **Modal Tặng Xu Tác Giả (`_tip-modal.jsp`):**
   - Vẫn đang sử dụng cách tiếp cận cũ với `style="display:none;"` và can thiệp style DOM thủ công thay vì bộ khung Design System chuẩn (`.app-modal-backdrop`, `.app-modal-card`, `.is-open`).
   - Chưa hỗ trợ phím `Escape` để thoát nhanh modal.
   - Nút `🎁 Gửi Tặng ... Xu` gửi ngay AJAX trừ tiền mà không hề có bước xác nhận (confirmation dialog), dễ gây mất xu oan nếu người dùng bấm nhầm.
2. **Trạm Nhận Xu Gamification (`user/me.jsp`):**
   - Form Điểm danh hàng ngày (`#form-daily-checkin`) và các nút Nhận thưởng nhiệm vụ (`.form-claim-quest`) submit POST truyền thống làm giật và reload toàn bộ trang.
   - Thiếu cơ chế debounce vô hiệu hoá nút tức thời; khi người dùng click nhanh liên tiếp (double click / spam), nhiều request gửi đồng thời có thể gây lỗi thông báo hoặc tải nặng server.

---

## 2. Giải pháp triển khai

1. **Chuẩn hoá Design System cho `_tip-modal.jsp`:**
   - Sử dụng thẻ `.app-modal-backdrop` và `.app-modal-card`, điều khiển đóng mở mượt mà thông qua class `.is-open`.
   - Bổ sung sự kiện phím `Escape` để đóng modal tiện dụng.
   - Tích hợp `window.showConfirmModal` ("Bạn có chắc chắn muốn gửi tặng [amount] xu cho tác giả [tên] không?") trước khi thực hiện trừ xu, bảo vệ tài sản của độc giả.
   - Cập nhật số dư xu trên toàn bộ trang và thanh header (`.coin-badge-val`, `.dropdown-coin-tag`) tức thời kèm hiệu ứng pháo hoa `window.launchCelebration('coins')`.

2. **Cập nhật CSS trong `components.css`:**
   - Bổ sung `#tipModalBackdrop` và `.tip-modal-card` vào nhóm CSS `.app-modal-backdrop` toàn cục.

3. **Backend `UserServlet.java`:**
   - Trả về payload JSON chi tiết cho các request AJAX: `success`, `reward`, `message`, `newBalance`.

4. **Nâng cấp Gamification AJAX & Debounce trên `user/me.jsp`:**
   - Bắt sự kiện submit của `#form-daily-checkin` và `.form-claim-quest`.
   - Vô hiệu hoá nút ngay khi click (hiện `⏳ Đang xử lý...`) để chống race condition / double submit.
   - Gửi yêu cầu qua `fetch` với `X-Requested-With: XMLHttpRequest` và CSRF token.
   - Khi thành công: bắn pháo hoa xu, cập nhật số dư trên thanh navbar, đổi nút sang huy hiệu "✅ Đã nhận" mà không cần reload trang.

---

## 3. Kết quả kiểm thử
- Đã bổ sung integration tests 4.14 và 4.15 trong `scripts/verify_full_system.py`.
- 181/181 unit tests PASS, 47/47 integration tests PASS (100%).
