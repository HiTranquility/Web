# 🚀 ISSUE-032: Bộ Lọc & Jumper Nhảy Chương Nhanh Cùng Chống Spam Bình Luận

- **Mã định danh:** `ISSUE-032`
- **Mức độ:** 🟢 Giao diện / Tương tác UI/UX & Hiệu năng trải nghiệm người dùng
- **Người thực hiện:** **Phát**
- **Ngày phát hiện & xử lý:** 2026-10-19
- **Trạng thái:** ✅ Đã hoàn thành (Verified PASS)

---

## 1. Mục tiêu & Vấn đề xử lý
1. **Bộ lọc danh sách chương (`story/detail.jsp`):**
   - Ô tìm kiếm nhanh chương trước đây chỉ lọc ẩn/hiện đơn sơ bằng `display: none`.
   - Khi không tìm thấy chương nào phù hợp, danh sách bị trống trơn mà không hề có thông báo trạng thái trống (empty state notice), khiến người dùng tưởng giao diện bị đơ hoặc lỗi tải dữ liệu.
   - Thiếu nút xoá nhanh từ khoá (nút clear `&times;`) trong ô tìm kiếm.
   - Khi người dùng gõ số chương (ví dụ `Chương 10`) rồi nhấn phím `Enter`, trình duyệt không có phản ứng nhảy hay cuộn tới chương, buộc người dùng phải rê chuột tìm và bấm.
2. **Chống spam gửi trùng bình luận (`_comment.jsp`):**
   - Các form bình luận chính (`.comment-form`) và form phản hồi (`.reply-form`) chưa có debounce tự động. Khi mạng chập chờn hoặc người dùng click liên tục vào nút "Gửi", nhiều bình luận trùng lặp bị đẩy lên cơ sở dữ liệu.

---

## 2. Giải pháp triển khai

1. **Nâng cấp toàn diện ô tìm kiếm & Jumper nhảy chương:**
   - **Nút xoá nhanh (`#chapter-filter-clear`):** Hiện tự động khi có chữ và ẩn khi ô tìm kiếm rỗng. Bấm vào nút sẽ xóa trắng ô tìm kiếm, khôi phục lại toàn bộ danh sách chương và focus lại vào ô nhập.
   - **Thông báo trạng thái không tìm thấy (`#chapter-filter-empty`):** Hiển thị khối thông báo viền nét đứt tinh tế (`🔍 Không tìm thấy chương nào khớp với từ khóa tìm kiếm.`) nếu không có chương nào thỏa mãn điều kiện lọc.
   - **Jumper nhảy chương qua phím `Enter`:** Khi gõ từ khóa và nhấn `Enter`:
     - Tự động tìm chương đầu tiên đang hiển thị.
     - Cuộn mượt mà đưa chương đó vào trung tâm tầm nhìn (`scrollIntoView({ behavior: 'smooth', block: 'center' })`).
     - Kích hoạt hiệu ứng nhấp nháy làm nổi bật viền hổ phách vàng (`.chapter-pulse` qua `@keyframes chapterPulseAnim`).
     - Bắn thanh thông báo nhỏ Toast thông báo đã định vị thành công chương.

2. **Chống spam gửi bình luận toàn cục (Comment & Reply Submit Debounce):**
   - Trong `_comment.jsp`, đăng ký listener ủy quyền sự kiện `submit` trên toàn tài liệu (`window.__commentSubmitDebounceSetup`).
   - Tự động nhận diện mọi form `.comment-form` và `.reply-form`.
   - Vô hiệu hoá nút gửi ngay tức thì khi submit (`btn.disabled = true;`), đổi nhãn thành `⏳ Đang gửi...` và thiết lập timeout 5 giây tự động khôi phục an toàn.

---

## 3. Kết quả kiểm thử
- Bổ sung kiểm thử tự động 4.17 trong `scripts/verify_full_system.py`.
- 181/181 unit tests PASS, 49/49 integration tests PASS (100%).
