# Đăng ký đề tài web

# Website Đọc Truyện Trực Tuyến — Các Chức Năng Đề Xuất

## Chức năng cho Người dùng (User functions)

- Đăng ký / Đăng nhập / Đăng xuất
- Quản lý hồ sơ: tên hiển thị, email, ảnh đại diện (avatar), giới thiệu bản thân (bio), mật khẩu
- Duyệt / Tìm kiếm / Lọc kho truyện
- Duyệt truyện theo trang (phân trang)
- Tìm kiếm truyện theo tên truyện / tên tác giả
- Lọc truyện theo thể loại / tình trạng (đang ra, đã hoàn thành) / độ phổ biến / thời gian cập nhật
- Xem chi tiết truyện: mô tả, ảnh bìa, tác giả, thể loại, số chương, lượt xem, danh sách chương
- Đọc chương với giao diện đọc chuyên biệt (ẩn thanh điều hướng, cỡ chữ lớn, phông chữ có chân, độ rộng cột tối ưu)
- Tuỳ chỉnh trải nghiệm đọc: cỡ chữ, giãn dòng, nền sáng / nền tối / nền giấy
- Chuyển chương trước / chương sau ngay trong trang đọc
- Tự động ghi nhớ vị trí đọc (đọc tới chương nào, không cần bấm gì)
- Đánh dấu / Bỏ đánh dấu truyện để đọc sau
- Xem danh sách truyện đã lưu kèm tiến độ đọc
- Tải truyện về máy dưới dạng tệp văn bản (.txt) để đọc ngoại tuyến
- Bình luận truyện
- Xoá bình luận của chính mình
- Đánh giá truyện (chấm sao)
- Theo dõi tác giả yêu thích
- Nhận thông báo khi truyện đang theo dõi có chương mới
- Báo cáo truyện / bình luận vi phạm nội quy
- Xem hướng dẫn sử dụng và nội quy cộng đồng

## Chức năng cho Tác giả (Author functions)

*Mọi thành viên đều có thể trở thành tác giả — không cần đăng ký riêng.*

- Đăng truyện mới: tiêu đề, mô tả, ảnh bìa, chọn thể loại
- Lưu bản nháp trước, công khai sau khi hoàn thiện
- Chỉnh sửa thông tin truyện của mình
- Gỡ truyện của mình
- Thêm chương mới (hệ thống tự đề xuất số chương kế tiếp)
- Chỉnh sửa / xoá chương
- Đánh dấu truyện đã hoàn thành
- Xem danh sách truyện của mình (bao gồm cả bản nháp)
- Xem thống kê truyện: lượt xem, số người đánh dấu, số bình luận

## Luồng tương tác đọc truyện (Reading interaction)

Vào trang chủ → Duyệt kho truyện hoặc lọc theo thể loại → Chọn truyện → Xem chi tiết truyện → Chọn chương → Đọc chương → Hệ thống tự ghi nhận vị trí đọc → Cập nhật cơ sở dữ liệu → Lượt xem tăng lên → Chuyển chương kế tiếp hoặc quay lại mục lục

## Luồng tương tác đăng truyện (Publishing interaction)

Đăng nhập → Vào mục "Truyện của tôi" → Đăng truyện mới → Nhập thông tin và chọn thể loại → Lưu bản nháp → Thêm chương → Kiểm tra lại nội dung → Chuyển trạng thái sang Công khai → Truyện xuất hiện trong kho truyện → Độc giả đọc và bình luận

## Chức năng cho Quản trị viên (Admin functions)

- Đăng nhập Quản trị viên (dùng chung cổng đăng nhập, phân biệt bằng vai trò)

**Quản lý người dùng:**
- Xem danh sách người dùng
- Khoá / Mở khoá tài khoản kèm lý do
- Thay đổi quyền (thành viên / quản trị viên)
- Xem số truyện mỗi tài khoản đã đăng

**Quản lý truyện:**
- Xem toàn bộ truyện, kể cả bản nháp và truyện đã gỡ
- Gỡ truyện vi phạm nội quy
- Khôi phục truyện đã gỡ nhầm
- Chỉnh sửa thông tin truyện bất kỳ

**Quản lý thể loại:**
- Thêm thể loại mới
- Đổi tên / xoá thể loại
- Xem số truyện thuộc mỗi thể loại

**Quản lý bình luận:**
- Xem danh sách bình luận
- Ẩn bình luận không phù hợp (giữ lại làm bằng chứng, không xoá hẳn)

**Xử lý báo cáo vi phạm:**
- Xem danh sách báo cáo từ người dùng
- Đánh dấu đã xử lý / bỏ qua

**Bảng điều khiển / Thống kê:**
- Tổng số truyện / chương / tài khoản
- Tổng lượt xem
- Truyện phổ biến nhất
- Thể loại được đọc nhiều nhất
- Số tài khoản mới theo ngày
- Số truyện đăng mới theo ngày

## Các tính năng tuỳ chọn, có thể thêm nếu thiếu

- Đánh giá bằng sao và xếp hạng truyện theo điểm trung bình
- Trả lời bình luận (bình luận lồng nhau)
- Danh sách đọc tự tạo (tủ truyện cá nhân, phân loại theo ý người dùng)
- Gợi ý truyện tương tự dựa trên thể loại
- Bảng xếp hạng theo tuần / tháng
- Tìm kiếm nâng cao trong nội dung chương
- Xuất truyện ra định dạng EPUB / PDF ngoài .txt
- Chế độ đọc ngoại tuyến (lưu chương vào trình duyệt)
- Đánh dấu chương đã đọc / chưa đọc
- Chia sẻ truyện lên mạng xã hội
- Email xác nhận khi đăng ký tài khoản
- Khôi phục mật khẩu qua email
- Đăng nhập bằng Google
- Hiển thị nhãn "Truyện mới" / "Hot" / "Hoàn thành"
- Đếm ngược tới chương mới nếu tác giả đặt lịch đăng
- Chống sao chép nội dung (chặn bôi đen, chuột phải)
