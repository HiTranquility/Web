# 🚀 ISSUE-031: Bộ Chọn Sao Trực Quan & Bình Chọn Đánh Giá Hữu Ích Không Tải Lại Trang

- **Mã định danh:** `ISSUE-031`
- **Mức độ:** 🟢 Giao diện / Tương tác UI/UX & Tối ưu hoá phản hồi người dùng
- **Người thực hiện:** **Phát**
- **Ngày phát hiện & xử lý:** 2026-10-19
- **Trạng thái:** ✅ Đã hoàn thành (Verified PASS)

---

## 1. Mục tiêu & Vấn đề xử lý
1. **Bộ chọn điểm sao đánh giá (`story/detail.jsp`):**
   - Form viết bài đánh giá tác phẩm (`#review-form-wrapper`) trước đây dùng thẻ `<select name="score">` dạng dropdown văn bản đơn điệu, kém trực quan và không mang lại trải nghiệm cảm xúc phong phú cho độc giả khi chấm điểm tác phẩm.
   - Khi gửi bài đánh giá chưa có cơ chế debounce; nếu người dùng double-click nút "Gửi đánh giá", form sẽ bị gửi nhiều lần liên tiếp.
2. **Bình chọn đánh giá hữu ích (`_review.jsp`):**
   - Nút "👍 Có ích" trong danh sách đánh giá submit form POST truyền thống, làm reload toàn bộ trang web và cuộn lên đầu, làm đứt mạch đọc của người dùng khi đang xem danh sách cảm nhận từ độc giả.
   - Chưa đồng bộ số lượng vote tức thì trên giao diện và chưa cập nhật trạng thái đã vote / hủy vote mượt mà.

---

## 2. Giải pháp triển khai

1. **Bộ chọn 5 sao trực quan (Interactive Star Rating Picker):**
   - Thay thế thẻ `<select>` bằng cụm `#interactive-star-picker` gồm 5 ngôi sao SVG/Unicode tương tác kết hợp nhãn giải thích trạng thái (`#star-label-text`).
   - Hỗ trợ hover chuột (kèm hiệu ứng phóng to `scale(1.22)` và phát sáng vàng amber), hiển thị nhãn đánh giá tương ứng theo thời gian thực (1 sao: Cần cải thiện, 2 sao: Tạm được, 3 sao: Khá ổn, 4 sao: Rất hay, 5 sao: Tuyệt tác).
   - Khi rê chuột ra ngoài, tự động khôi phục về điểm sao đã chọn hiện tại.
   - Tự động điền giá trị vào hidden input `score` để submit mượt mà với backend.

2. **Chống gửi lặp (Submit Debounce) cho bài đánh giá:**
   - Đặt ID `#story-review-form`, vô hiệu hoá nút gửi ngay khi kích hoạt kèm nhãn `⏳ Đang lưu đánh giá...` và timeout phục hồi an toàn sau 5 giây.

3. **Backend `ReviewDAO.java` & `RatingServlet.java`:**
   - Bổ sung hàm `getHelpfulCount(int reviewId)` trong `ReviewDAO.java` để lấy nhanh số lượt bình chọn mới nhất.
   - Cập nhật hàm `handleVote` trong `RatingServlet.java`: phát hiện header `X-Requested-With: XMLHttpRequest` và trả về JSON:
     `{"success":true,"voted":true/false,"count":<number>}`.
   - Thêm xử lý trả về mã `401 Unauthorized` kèm JSON `needLogin: true` nếu người dùng chưa đăng nhập khi bấm bình chọn qua AJAX.

4. **Bình chọn AJAX không tải lại trang trên `story/detail.jsp` & `_review.jsp`:**
   - Trong `_review.jsp`: bổ sung thuộc tính `data-voted` và thẻ `<span class="helpful-count">`.
   - Trong `detail.jsp`: bắt sự kiện submit của `.review-vote-form`, gọi AJAX `fetch`, cập nhật tức thời class nút (`.btn-primary` <-> `.btn-ghost`), hoán đổi số đếm và hiển thị toast phản hồi thân thiện.

---

## 3. Kết quả kiểm thử
- Bổ sung kiểm thử tự động 4.16 trong `scripts/verify_full_system.py`.
- 181/181 unit tests PASS, 49/49 integration tests PASS (100%).
