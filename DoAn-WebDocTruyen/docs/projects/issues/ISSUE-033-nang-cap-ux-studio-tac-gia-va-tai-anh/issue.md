# 🚀 ISSUE-033: Nâng Cấp Toàn Diện UX Studio Tác Giả & Tải Ảnh Bìa, Trang Truyện Tranh

- **Mã định danh:** `ISSUE-033`
- **Mức độ:** 🟢 Giao diện / Tương tác UI/UX Tác giả & Tối ưu luồng xuất bản
- **Người thực hiện:** **Phát**
- **Ngày phát hiện & xử lý:** 2026-10-19
- **Trạng thái:** ✅ Đã hoàn thành (Verified PASS)

---

## 1. Mục tiêu & Vấn đề xử lý
1. **Trải nghiệm đăng / sửa truyện & ảnh bìa (`user/story/form.jsp`):**
   - Hộp xem trước bìa `#cover-preview-box` chỉ nhận file khi bấm nút chọn file truyền thống; nếu kéo thả ảnh trực tiếp từ máy tính (Explorer) vào thì trình duyệt mở ảnh ở tab mới thay vì nạp vào form.
   - Khi tác giả chọn nhầm một file ảnh nặng hoặc sai file từ máy, không có nút "✕ Hủy chọn file" để quay về ảnh ban đầu.
   - Chưa có submit debounce: Khi lưu truyện kèm ảnh bìa vài MB qua mạng chậm, người dùng bấm nhiều lần nút "Lưu truyện" sẽ gửi trùng lặp nhiều request.
   - Dùng popup `alert()` màu trắng cổ điển làm đơ giao diện khi validate dung lượng ảnh (> 2 MB) hoặc tiêu đề/mô tả rỗng/quá dài.
2. **Trải nghiệm chương truyện tranh (`user/chapter/form-comic.jsp`):**
   - Tác giả thường tải lên hàng loạt trang ảnh (scan). Khi tên file bị ngược thứ tự (từ dưới lên trên), tác giả phải bấm nút mũi tên `↑`/`↓` từng ảnh một hàng chục lần rất cực nhọc.
   - Thiếu nút "Xóa tất cả ảnh mới": Khi chọn nhầm một thư mục 50 trang, tác giả phải bấm nút `✕` 50 lần.
   - Dùng popup `alert()` khi kiểm tra số lượng trang ảnh.
3. **Trải nghiệm chương truyện chữ (`user/chapter/form.jsp`) & Backend Servlets:**
   - Chưa có submit debounce cho `#chapterForm` khi lưu chương chữ (`⏳ Đang lưu chương...`), dễ bị ấn đúp tạo chương trùng.
   - Dùng popup `alert()` khi validate tiêu đề và nội dung chương.
   - Thiếu Flash toast: Sau khi tạo/sửa/gỡ truyện hoặc tạo/sửa/xóa chương, các servlet redirect về trang chi tiết nhưng không có thông báo xác nhận thành công.

---

## 2. Giải pháp triển khai

1. **Bìa truyện (`story/form.jsp`):**
   - Tích hợp vùng kéo thả Drag & Drop trực tiếp vào ô xem trước bìa `#cover-preview-box` với overlay mờ hổ phách `#cover-drop-overlay` (`📥 Thả ảnh vào đây`).
   - Bổ sung nút `#btnRemoveCoverFile` ("✕ Hủy chọn file") để xóa tệp vừa chọn nếu chọn nhầm và khôi phục lại ảnh xem trước cũ.
   - Chuyển toàn bộ các thông báo lỗi sang Toast Design System thay vì `alert()`.
   - Bổ sung submit debounce cho `#storyForm`: vô hiệu hóa nút "Lưu truyện" ngay khi gửi, hiển thị `⏳ Đang lưu truyện...` kèm timeout an toàn 10s.

2. **Chương Truyện Tranh (`chapter/form-comic.jsp`):**
   - Bổ sung nút "🔄 Đảo ngược thứ tự" (`#btnReversePages`): một chạm đảo ngược toàn bộ thứ tự trang ảnh từ dưới lên trên và đánh lại số trang tức thì.
   - Bổ sung nút "🗑️ Xóa ảnh mới" (`#btnClearNewPages`): tự động hiển thị khi có ảnh mới tải lên, một chạm dọn sạch tất cả ảnh mới mà không ảnh hưởng tới các trang cũ đã xuất bản.
   - Chuyển `alert()` sang Toast Design System.
   - Bổ sung submit debounce cho `#btnSaveComic` với timeout an toàn 15s.

3. **Chương Truyện Chữ (`chapter/form.jsp`):**
   - Chuyển các thông báo `alert()` kiểm tra tiêu đề, số chương, nội dung sang Toast Design System.
   - Bổ sung submit debounce (`⏳ Đang lưu chương...`) với timeout an toàn 8s.

4. **Backend Servlets (`StoryServlet.java`, `ChapterServlet.java`):**
   - Bổ sung thông báo Flash toast trong session sau khi chuyển hướng (PRG pattern):
     - Tạo truyện mới: `🎉 Đã đăng truyện mới thành công!`
     - Sửa truyện: `✍️ Đã lưu thay đổi truyện thành công!`
     - Gỡ truyện: `🗑️ Đã gỡ truyện thành công!`
     - Tạo chương mới: `🎉 Đã xuất bản chương mới thành công!`
     - Sửa chương: `✍️ Đã cập nhật chương thành công!`
     - Xóa chương: `🗑️ Đã xoá chương thành công!`

---

## 3. Kết quả kiểm thử
- Bổ sung kiểm thử tự động 4.18 và 4.19 trong `scripts/verify_full_system.py`.
- 181/181 unit tests PASS, 51/51 integration tests PASS (100%).
