# Thành tích, đặc quyền và uy tín tác giả

Trạng thái: chọn phương án B theo yêu cầu người dùng ngày 19/10/2026.

## Quyết định và thay đổi quy ước

Trang `/user?action=achievements` nằm dưới Hồ sơ. Tủ sách đọc truyện vẫn ở `/bookmark`. Trang thành tích gồm hai phần: huy hiệu tự động tạo đặc quyền và thành tựu bấm nhận xu một lần. Hồ sơ có đường dẫn và tổng % thưởng, không nhúng toàn bộ tủ huy hiệu.

Quy tắc này thay việc gộp đề xuất theo thứ tự nguồn cố định trong `StoryDAO.findRecommendations`. Xếp hạng thường, quyền quản trị và quyền đọc chương không được cấp thêm bởi huy hiệu.

## Các mức khởi đầu

| Thành tích | Điều kiện | % thưởng ngày | Điểm uy tín | Xu một lần |
| --- | --- | ---: | ---: | ---: |
| Cú Đêm | Đọc chương hợp lệ từ 23:00 đến trước 05:00 | 2 | 1 | 5 |
| Mọt Sách | 30 chương khác nhau | 3 | 1 | 15 |
| Đại Học Giả | 150 chương khác nhau | 5 | 2 | 30 |
| Mạnh Thường Quân | Tặng xu cho người khác, giao dịch có nhãn TIP | 2 | 1 | 5 |
| Nhà Phê Bình | Đánh giá từ 80 ký tự cho truyện công khai của người khác | 2 | 1 | 5 |
| Kỷ Lục Điểm Danh | Chuỗi 7 ngày | 4 | 1 | 20 |
| Nhà Sưu Tầm | Lưu 5 truyện khác nhau | 2 | 1 | 5 |
| Khởi Đầu Sáng Tác | Truyện công khai có chương đủ nội dung | 3 | 4 | 20 |
| Ngòi Bút Bền Bỉ | 10 chương đủ nội dung trong truyện công khai | 4 | 6 | 40 |
| Về Đích | Truyện công khai hoàn thành, có 10 chương đủ nội dung | 5 | 8 | 100 |
| Hội Viên Hoàng Gia | VIP còn thời hạn | 0 | 0 | 0 |

Chương đủ nội dung cho thành tích sáng tác: ít nhất 200 ký tự sau trim, hoặc ít nhất 3 trang ảnh. Đây là điều kiện tối thiểu, không phải đánh giá chất lượng văn học hay kiểm duyệt bản quyền.

Tổng huy hiệu cộng tối đa 20% thưởng ngày. Thưởng điểm danh = làm tròn(thưởng gốc × (hệ số cấp bậc/VIP hiện có + % huy hiệu / 100)). Nhiệm vụ khác dùng hệ số nền 1. Xu của thành tựu một lần không nhân thêm. Số xu thực nhận được lưu trong sổ cái; giao diện nhiệm vụ đã nhận hiển thị số đã trả.

VIP giữ nguyên thời hạn, chiết khấu và hệ số điểm danh hiện có; huy hiệu VIP không vĩnh viễn, không cấp xu và không tăng uy tín. Các huy hiệu còn lại được lưu vĩnh viễn sau khi đạt; nhận xu không phải điều kiện kích hoạt đặc quyền. Ví dụ bỏ lưu truyện sau khi đạt Nhà Sưu Tầm không xóa huy hiệu.

## Dữ liệu và tính nguyên tử

Hai bảng mới do DAO khởi tạo theo cơ chế hiện có của dự án:

- `user_achievements`: khóa chính `(user_id, achievement_key)`, ngày đạt, ngày nhận xu và số xu đã nhận. UPDATE có điều kiện `claimed_at IS NULL`, cộng ví và ghi giao dịch nằm chung transaction. Không nhận số xu từ client.
- `user_chapter_reads`: khóa chính `(user_id, chapter_id)`, lần đọc đầu/cuối và cờ đọc ban đêm. Đọc lại không nhân số chương. Lần đầu tạo bảng, giữ chương cuối đã biết từ `reading_progress`, không gán thời điểm hay lịch sử đọc ban đêm giả. Đây là chuyển đổi dữ liệu một lần; các lần tiến độ mới chưa đủ điều kiện không tự tăng số chương.

Ghi nhận hoạt động mới sau khi người dùng có quyền đọc chương, ở trong phiên tối thiểu 30 giây và tiến độ tới ít nhất 80% đoạn/trang. Thời gian lấy ở server. Chương tải sẵn chưa được nhìn tới không được tính. Reader báo tiến độ định kỳ khi đang hiển thị; đây là tín hiệu hạn chế farm cơ bản, không phải hệ thống xác minh người thật.

Nhật ký lượt xem truyện cũ không được biến thành số chương đã đọc. Không suy diễn lịch sử đọc ban đêm hoặc nhiệm vụ hôm nay từ dữ liệu vị trí đọc cũ.

Các thao tác lưu truyện/chương, lưu tủ sách, đánh giá, tặng xu, điểm danh và tiến độ đọc cập nhật thành tích. Lỗi cập nhật đặc quyền không làm thông báo giao dịch trước đó thất bại sau khi giao dịch đã commit. Việc tính thưởng mới yêu cầu đọc được dữ liệu đặc quyền; không tự cấp thưởng giả khi lỗi CSDL.

Các giao dịch tặng xu mới được gắn tiền tố `[TIP]`. Mở khóa chương không có tiền tố này. Không đoán loại giao dịch lịch sử từ việc có `story_id`, vì cả mua chương và tặng xu đều có thể chứa trường đó; các giao dịch cũ nhập nhằng không tự cấp huy hiệu.

## Xếp đề xuất

Lấy nhóm ứng viên lớn hơn trước khi xếp, bỏ trùng và truyện đang xem. Điểm nguồn ban đầu: độc giả cùng đọc 100, cùng thể loại 90, phổ biến 65; giảm nhẹ theo thứ tự trong từng nguồn. Nhân với `(1 + min(10, tổng điểm uy tín) / 100)`. Trọng số sáng tác cao hơn đọc/gắn bó.

Giới hạn hai truyện của một tác giả trong một khối. Khối từ bốn truyện dành một vị trí cho truyện mới trong 30 ngày, cùng thể loại, của tác giả mới có một truyện công khai. Nếu không có ứng viên khám phá thì dùng khối đề xuất thường. Không bảo đảm lượt xem hay vị trí cố định cho tác giả có huy hiệu. Bảng xếp hạng chung không đổi. Khi không đủ tác giả khác nhau, khối có thể hiển thị ít hơn số lượng yêu cầu thay vì lấp đầy bằng một tác giả.

## Mở khóa chương truyện tranh

Form POST có CSRF, xác nhận dùng bộ modal chung và ID riêng theo chương để hoạt động cả khi reader nối thêm chương. Chương đang khóa không có URL ảnh trong HTML. Giá và ưu đãi tính lại từ server; không đọc giá do client truyền.

Giao dịch khóa người mua, kiểm lại vé đã có trước khi trừ, rồi trừ ví, cộng tác giả, lưu vé và ghi sổ cái trong cùng transaction. Gửi lại yêu cầu sau khi đã mua trả thành công mà không trừ lại. GET không mua chương. Sau chuyển hướng, trang đọc nạp ảnh và cấp quyền phát ảnh cho phiên. Chương giá 0 được coi là miễn phí. Chế độ không có CSDL không báo mở khóa thành công giả.

## Triển khai và kiểm chứng

Không thực thi DDL trên dữ liệu thật trong lượt phát triển này. Khi triển khai, tài khoản CSDL cần quyền tạo hai bảng mới trong lần khởi tạo. Không sửa hoặc đọc file `.sql`, `.env`, khóa bí mật hay cơ sở dữ liệu nằm trên đĩa.

Chạy `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/audit.ps1` hoàn toàn local, classpath không có cấu hình DB/Google/mail. Các ca JDBC mô phỏng kiểm tra nhận lặp, hoàn tiền khi ghi sổ lỗi, ví không đủ và mua lại chương. HTTP smoke dùng Tomcat, cookie thật và DAO giả để kiểm tra giá sau ưu đãi, form xác nhận, khóa trước khi mua và ảnh sau khi mua.

Lượt kiểm chứng cuối: **231/231 JUnit, 65/65 kiểm tra HTTP và 8/8 kiểm thử JavaScript đạt**. Log và HTML giả lập ở `build/audit/run-795bbb12b931442187aaef77914e0b40/`. Chưa thực thi việc khởi tạo/chuyển đổi dữ liệu hoặc giao dịch trên MySQL thật; chưa xác minh bố cục bằng trình duyệt vì công cụ trình duyệt của phiên gặp lỗi khởi tạo.
