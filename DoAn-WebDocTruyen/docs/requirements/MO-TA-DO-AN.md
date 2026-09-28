# ĐỌC TRUYỆN ONLINE — Nền tảng đọc và chia sẻ truyện cộng đồng

**Đồ án cuối kỳ — Môn Lập trình Web**

| | |
|---|---|
| **Tên đồ án** | Xây dựng website đọc truyện trực tuyến có quản lý nội dung và phân quyền người dùng |
| **Tên ngắn** | ĐọcTruyện — Nền tảng đọc truyện cộng đồng |
| **Công nghệ** | Java Servlet 3.1 · JSP + JSTL · MySQL 8 · Apache Tomcat 9 |
| **Kiến trúc** | MVC Model 2 (Model – View – Controller), server-rendered |
| **Quy mô** | **77 lớp Java** · **55 file JSP** (36 trang, 5 layouts, 14 partials) · **14 bảng dữ liệu** · **114 bài Unit Test** |

---

## 1. Mô tả đề tài

Website cho phép người dùng **đọc truyện miễn phí** và **tự đăng truyện của mình**.
Mỗi truyện gồm nhiều chương, được phân loại theo thể loại để người đọc dễ tìm.
Người dùng có thể đánh dấu truyện đang đọc dở, bình luận trao đổi, và tải truyện
về máy. Quản trị viên có công cụ kiểm duyệt nội dung và xử lý tài khoản vi phạm.

Hệ thống có **nội quy cộng đồng** và **hướng dẫn sử dụng** rõ ràng, với cơ chế
thực thi: người dùng phải xác nhận đồng ý nội quy khi đăng ký, và quản trị viên
có quyền gỡ nội dung vi phạm.

---

## 2. Phân quyền — 3 nhóm người dùng

| Nhóm | Quyền hạn |
|------|-----------|
| **Khách** (chưa đăng nhập) | Xem kho truyện, lọc theo thể loại, tìm kiếm nội dung sâu, đọc chương, tải truyện, xem bảng xếp hạng |
| **Thành viên** | Toàn bộ quyền của Khách, cộng thêm: đăng truyện, quản lý truyện **của mình**, bình luận, đánh dấu truyện, đánh giá sao, theo dõi tác giả, tặng xu ủng hộ tác giả |
| **Quản trị viên** | Toàn bộ quyền của Thành viên, cộng thêm: xem bảng điều khiển thống kê, gỡ/khôi phục truyện bất kỳ, khoá/mở khoá tài khoản, quản lý thể loại, kiểm duyệt bình luận và xử lý báo cáo vi phạm |

Quyền được kiểm ở **hai tầng**: `Filter` chặn theo nhóm, và `Servlet` kiểm quyền
sở hữu từng bản ghi — đảm bảo người dùng A không sửa được truyện của người dùng B
kể cả khi tự sửa tham số trên URL.

---

## 3. Danh sách chức năng — 38 chức năng hoàn chỉnh

> 📌 *Chi tiết kiến trúc kỹ thuật, bản đồ 36 trang web và 18 modules nâng cao: Xem tài liệu trung tâm [`TONG-HOP-HE-THONG.md`](../TONG-HOP-HE-THONG.md).*

### A. Nhóm chức năng đọc truyện & khám phá (10 chức năng)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 1 | **Trang chủ** | Hiển thị truyện mới cập nhật, truyện hot nhiều lượt xem, và khối "Tiếp tục đọc" ghi nhớ vị trí đọc dở |
| 2 | **Kho truyện** | Danh sách toàn bộ truyện, lọc theo 10+ thể loại, tình trạng đang ra/hoàn thành, phân trang chuẩn |
| 3 | **Tìm kiếm sâu trong chương (ISSUE-005)** | Chỉ mục FULLTEXT MySQL, trích ngữ cảnh từ khóa kèm bôi đậm `<mark>` |
| 4 | **Xem chi tiết truyện** | Thông tin truyện, thể loại, mục lục chương, bình luận, nút tặng xu ủng hộ, gợi ý truyện tương đồng |
| 5 | **Đọc chương truyện** | Giao diện đọc chuyên biệt (Reader layout): ẩn menu, cột 38em chuẩn quang học, điều hướng phím mũi tên `←`/`→` |
| 6 | **Tùy biến môi trường đọc** | Đổi màu giao diện Sáng / Tối / Giấy Sepia, tăng giảm cỡ chữ linh hoạt, giãn dòng, đổi font Serif/Sans |
| 7 | **Tự động lưu tiến độ đọc** | Ghi nhận chương đọc dở và % vị trí cuộn trang tự động vào CSDL và LocalStorage |
| 8 | **Tải truyện Offline (.txt)** | Xuất toàn bộ nội dung truyện ra tệp văn bản `.txt` để đọc ngoại tuyến |
| 9 | **Bảng xếp hạng (ISSUE-006)** | Bảng vàng vinh danh truyện hot theo lượt xem/đánh giá và bảng xếp hạng tác giả xuất sắc |
| 10 | **Trang thông tin & Trợ năng** | Hướng dẫn sử dụng, nội quy cộng đồng, hỗ trợ A11y, Skip-link và chuẩn Mobile 360px (ISSUE-015) |

### B. Nhóm chức năng thành viên & cộng đồng (8 chức năng)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 11 | **Quản lý tài khoản & Google OIDC** | Đăng ký, đăng nhập mật khẩu PBKDF2 muối ngẫu nhiên + Đăng nhập Google an toàn qua Firebase (ISSUE-001) |
| 12 | **Hồ sơ cá nhân & Unlink Guard** | Cập nhật avatar, bio, tên hiển thị. Chốt chặn Unlink Guard yêu cầu có mật khẩu mới cho gỡ Google |
| 13 | **Khôi phục mật khẩu qua Email (ISSUE-002)** | Gửi link đặt lại mật khẩu với token an toàn qua JavaMail SMTP |
| 14 | **Đánh dấu truyện (Bookmarks)** | Lưu truyện đọc sau kèm nút "Đọc tiếp" thông minh đưa thẳng đến chương đang dở |
| 15 | **Lịch sử đọc truyện** | Theo dõi toàn bộ lịch sử các chương đã đọc theo dòng thời gian |
| 16 | **Bình luận đa cấp & Chân chương (ISSUE-004)** | Thảo luận ở chi tiết truyện và dưới chân từng chương đọc; trả lời lồng nhau, thả tim tương tác |
| 17 | **Đánh giá truyện (Rating)** | Chấm sao từ 1 đến 5 sao với ràng buộc chống trùng lặp đánh giá |
| 18 | **Báo cáo vi phạm (Report)** | Gửi phản ánh nội dung truyện hoặc bình luận vi phạm tới quản trị viên |

### C. Nhóm chức năng tác giả & sáng tác (7 chức năng)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 19 | **Tủ truyện của tôi** | Quản lý danh sách truyện do mình sáng tác, chuyển trạng thái Bản nháp / Công khai |
| 20 | **Đăng và sửa truyện** | Tạo truyện, chọn thể loại, tải ảnh bìa kèm **Live Preview 3:4** tức thì bằng FileReader API (ISSUE-017) |
| 21 | **Soạn thảo chương (Editor Layout)** | Khung soạn chuyên biệt toàn màn hình, đếm số từ trực tiếp, tự đề xuất số chương kế tiếp |
| 22 | **Thống kê truyện tác giả** | Thống kê tổng lượt xem, bookmark, bình luận kèm biểu đồ lượt đọc 14 ngày của truyện hot nhất |
| 23 | **Sao lưu Google Drive (ISSUE-001)** | Xuất và sao lưu toàn bộ chương truyện sang Google Drive cá nhân chuẩn định dạng text qua API v3 |
| 24 | **Nhận xu ủng hộ (ISSUE-008)** | Độc giả gửi tặng xu ảo kèm lời chúc; tác giả nhận xu trong ví cá nhân |
| 25 | **Theo dõi & Bắn thông báo** | Độc giả Follow tác giả; hệ thống tự động bắn thông báo khi có chương mới xuất bản |

### D. Nhóm chức năng quản trị viên (6 chức năng)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 26 | **Bảng điều khiển quản trị** | Thống kê số lượng truyện, chương, người dùng, lượt xem, biểu đồ tăng trưởng 14 ngày |
| 27 | **Quản lý toàn bộ kho truyện** | Xem toàn bộ truyện kể cả bản nháp; gỡ truyện vi phạm và khôi phục lại (cơ chế xóa mềm) |
| 28 | **Quản lý tài khoản người dùng** | Xem danh sách tài khoản; phân quyền thành viên/admin; khoá/mở khoá kèm lý do chi tiết |
| 29 | **Quản lý thể loại (Tags)** | Thêm, sửa, xóa danh mục thể loại; theo dõi số lượng truyện theo từng thể loại |
| 30 | **Xử lý báo cáo vi phạm** | Tiếp nhận và xử lý danh sách báo cáo nội dung từ người dùng (duyệt / bỏ qua) |
| 31 | **Kiểm duyệt bình luận** | Xem bình luận toàn hệ thống; ẩn bình luận vi phạm giữ bằng chứng trong CSDL |

### E. Nhóm chức năng nền tảng & nâng cao (7 chức năng)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 32 | **Ví xu ảo & Giao dịch ACID (ISSUE-008)** | Quản lý ví xu và lịch sử giao dịch nguyên tử, chống thất thoát dữ liệu số dư |
| 33 | **RESTful JSON API (ISSUE-009)** | Cung cấp endpoints `/api/stories`, `/api/story/{id}`, `/api/chapter/{id}` chuẩn CORS |
| 34 | **PWA Đọc ngoại tuyến (ISSUE-010)** | Service Worker Cache-First và Web Manifest đạt chuẩn cài đặt App HomeScreen |
| 35 | **Chống Brute-force & Spam (ISSUE-003)** | Thuật toán Sliding Window luồng an toàn tự khóa IP/tài khoản thử sai và giãn cách bình luận |
| 36 | **Dọn dẹp hệ thống ngầm (ISSUE-013)** | Background thread AppListener tự dọn dẹp các dòng view logs quá hạn 90 ngày |
| 37 | **SEO Động & Sitemap XML (ISSUE-012)** | Tự động sinh `sitemap.xml`, `robots.txt` và thẻ OpenGraph khi chia sẻ mạng xã hội |
| 38 | **Chia sẻ 1 chạm (ISSUE-018)** | Copy link kèm Toast thông báo nổi tức thì và phím tắt chia sẻ Facebook/Twitter |

---

## 4. Cơ sở dữ liệu — 14 bảng

| Bảng | Vai trò |
|------|---------|
| `users` | Tài khoản. Một bảng chung cho cả độc giả, tác giả và quản trị viên |
| `user_identities` | Liên kết tài khoản mạng xã hội (Google OIDC UID) |
| `stories` | Danh sách truyện, thông tin tóm tắt và thống kê tổng hợp |
| `chapters` | Nội dung các chương truyện (`MEDIUMTEXT`, chỉ mục FULLTEXT) |
| `tags` | Danh mục thể loại truyện |
| `story_tags` | Bảng nối truyện ↔ thể loại (quan hệ nhiều–nhiều) |
| `bookmarks` | Đánh dấu truyện yêu thích và vị trí chương đọc dở |
| `comments` | Bình luận truyện và thảo luận theo từng chương |
| `ratings` | Chấm sao đánh giá từ 1 đến 5 sao |
| `follows` | Theo dõi tác giả yêu thích |
| `reports` | Báo cáo vi phạm nội dung truyện hoặc bình luận |
| `notifications` | Thông báo gửi tới người dùng |
| `view_logs` | Nhật ký lượt mở đọc truyện phục vụ thống kê |
| `wallets` & `transactions` | Ví xu ảo nội bộ và lịch sử chuyển xu tặng thưởng |

**Sơ đồ ERD** và các sơ đồ luồng xử lý: xem [`docs/architecture/so-do.md`](../architecture/so-do.md).

---

## 5. Điểm kỹ thuật nổi bật

**Kiến trúc phân tầng rõ ràng.** Bốn tầng `model` / `dao` / `controller` / `view`
với ranh giới nghiêm ngặt: tầng truy cập dữ liệu không biết gì về web, tầng điều
khiển không chứa câu lệnh SQL, trang hiển thị không chứa mã Java.

**Hệ thống khung trang (layout).** Bốn khung dùng chung cho toàn bộ trang, mỗi
trang nội dung chỉ chứa phần ruột. Thay đổi giao diện chung chỉ sửa một chỗ.

**Chống SQL Injection.** Toàn bộ truy vấn dùng `PreparedStatement` với tham số
`?`, không nối chuỗi. Kể cả truy vấn dựng động cho bộ lọc cũng chỉ ghép khung
câu lệnh, giá trị luôn đi qua tham số.

**Chống XSS.** Mọi dữ liệu người dùng nhập đều được mã hoá ký tự đặc biệt trước
khi hiển thị bằng thẻ `<c:out>`.

**Bảo mật mật khẩu & Đăng nhập Google.** Mật khẩu được băm PBKDF2 với 120.000 vòng lặp
và chuỗi muối (salt) ngẫu nhiên riêng cho từng tài khoản. Đăng nhập Google xác thực trực
tiếp chữ ký số `idToken` qua Google OIDC máy chủ, tuyệt đối không tin client, chống giả
mạo và chống chiếm đoạt tài khoản (Anti-Account Takeover) cùng chống Session Fixation.

**Xoá mềm.** Truyện, bình luận và tài khoản khi bị gỡ chỉ đổi trạng thái, không
xoá khỏi cơ sở dữ liệu — khôi phục được và giữ dữ liệu liên quan không bị mồ côi.

**Hỗ trợ tiếng Việt đầy đủ.** Bảng mã `utf8mb4` hỗ trợ cả biểu tượng cảm xúc;
đường dẫn thân thiện tự sinh từ tiêu đề có dấu.

---

## 6. Tài khoản dùng thử

| Tên đăng nhập | Mật khẩu | Vai trò | Ghi chú |
|---------------|----------|---------|---------|
| `admin` | `admin123` | **Quản trị viên** | Vào được trang quản trị |
| `mocmien` | `123456` | Thành viên | Tác giả có 3 truyện |
| `haiduong` | `123456` | Thành viên | Tác giả có 2 truyện |
| `kiemvu` | `123456` | Thành viên | Tác giả có 3 truyện (1 bản nháp) |
| `thuytien` | `123456` | Thành viên | Độc giả, có 4 truyện đã lưu |
| `spammer` | `123456` | *Đã bị khoá* | Thử đăng nhập để xem cơ chế chặn |

Dữ liệu mẫu gồm **9 truyện · 29 chương · 10 thể loại · 19 bình luận · 8 lượt đánh dấu**.

---

## 7. Hướng dẫn cài đặt

Yêu cầu: **JDK 11+**, **MySQL 8**, **Apache Tomcat 9** (không dùng Tomcat 10 trở lên).

**Cách nhanh — một lệnh cài toàn bộ database:**

```bash
powershell -ExecutionPolicy Bypass -File scripts\setup-db.ps1
```

Script tạo database, tạo tài khoản MySQL cho ứng dụng, nạp dữ liệu mẫu và
sinh file cấu hình. Bạn chỉ cần gõ mật khẩu MySQL root khi được hỏi.

**Rồi chạy web:**

```bash
powershell -ExecutionPolicy Bypass -File scripts\run.ps1
```

Truy cập: <http://localhost:8080/>
