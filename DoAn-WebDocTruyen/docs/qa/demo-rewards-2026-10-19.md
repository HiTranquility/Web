# Demo nhận thưởng trên MySQL local — 19/10/2026

Tài khoản thử: `demo_rewards_20261019`. Web: http://localhost:8080/.

Đăng ký, đăng nhập và nhận thưởng qua HTTP của ứng dụng đang nối MySQL thật. Không nạp DAO giả, không sửa ngày máy chủ, không đọc cấu hình bí mật hay file cơ sở dữ liệu. Thao tác chỉ áp dụng cho tài khoản thử mới tạo.

## Vị trí hiển thị

- Hồ sơ → Trạm Nhận Xu và Thành tích & Đặc quyền đều có **Xu hiện có trong ví**, **Thưởng đã nhận hôm nay**, **Tổng thưởng tích lũy** và ngày ghi nhận theo máy chủ DB.
- Tổng thưởng cộng số đã trả trong điểm danh, nhiệm vụ và thành tựu; không gồm 100 xu tặng khởi đầu, chuyển xu hay nạp xu. Chi tiêu không trừ tổng thưởng tích lũy.
- Trang thành tựu hiển thị số mục đủ điều kiện chưa nhận và tổng xu chờ nhận. Bộ lọc **Có thể nhận xu** giúp tìm các mục này.
- Ví → lịch sử giao dịch có đủ ba giao dịch đã nhận trong demo.

## Kết quả thực tế

| Thao tác | Cộng xu | Ví sau nhận | Thưởng hôm nay | Tổng thưởng tích lũy |
| --- | ---: | ---: | ---: | ---: |
| Mới đăng ký | 0 | 100 | 0 | 0 |
| Điểm danh ngày đầu | 5 | 105 | 5 | 5 |
| Thành tựu Trang Đầu Tiên | 2 | 107 | 7 | 7 |
| Nhiệm vụ Mọt sách siêng năng | 10 | 117 | 17 | 17 |

Chuẩn bị bằng cách lưu 5 truyện công khai để đạt Nhà Sưu Tầm, mở một chương miễn phí, chờ 31 giây rồi gửi tiến độ cuối chương qua endpoint đọc hiện hành. Không cấp huy hiệu bằng cách chèn/sửa dữ liệu DB.

Tài khoản có huy hiệu +2% thưởng ngày. Điểm danh ngày đầu: `round(5 × (1 + 2/100)) = 5`; nhiệm vụ đọc: `round(10 × (1 + 2/100)) = 10`. Phần tăng nhỏ được làm tròn theo quy tắc hiện có. Thành tựu Trang Đầu Tiên trả đúng 2 xu, không nhân huy hiệu.

Mỗi yêu cầu nhận đã được gửi lại: số dư và cả hai tổng thưởng không đổi. Các lần tải lại và đăng nhập bằng phiên mới vẫn trả 117 / 17 / 17. Nhãn điểm danh trên Hồ sơ hiển thị đã nhận 5 xu. Tổng thưởng và số dư trên Hồ sơ trùng trang thành tựu.

Giữ nguyên **Nhà Sưu Tầm: đủ điều kiện, chưa nhận 5 xu** để người dùng tự bấm thử. Nếu nhận thành công, ví dự kiến **117 → 122**, thưởng hôm nay và tổng thưởng **17 → 22** (nếu nhận cùng ngày, chưa có giao dịch khác). Thành tựu này chỉ nhận một lần, không lặp lại hằng ngày.

## Bằng chứng và giới hạn

Snapshot không chứa mật khẩu, cookie hay CSRF ở `build/rewards-demo/{prepare,checkin,milestone,daily,verify}.json`. Công cụ tái kiểm tra: `python tools/RealRewardsDemo.py verify`; lệnh `register` tạo tài khoản thử, các lệnh nhận thay đổi dữ liệu tài khoản thử.

Đã kiểm chứng HTML server trả về, JSON, số dư sau nhận, chống nhận lặp và dữ liệu đọc lại trong phiên đăng nhập mới. Chưa kiểm tra trực quan bằng trình duyệt: công cụ trình duyệt lỗi khởi tạo kernel assets. Không thay đổi ngày để giả lập chuỗi nhiều ngày.
