# ĐỌC TRUYỆN ONLINE — Nền tảng đọc và chia sẻ truyện cộng đồng

**Đồ án cuối kỳ — Môn Lập trình Web**

| | |
|---|---|
| **Tên đồ án** | Xây dựng website đọc truyện trực tuyến có quản lý nội dung và phân quyền người dùng |
| **Tên ngắn** | ĐọcTruyện — Nền tảng đọc truyện cộng đồng |
| **Công nghệ** | Java Servlet 3.1 · JSP + JSTL · MySQL 8 · Apache Tomcat 9 |
| **Kiến trúc** | MVC Model 2 (Model – View – Controller), server-rendered |
| **Quy mô** | 27 lớp Java · 26 trang JSP · 7 bảng dữ liệu |

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
| **Khách** (chưa đăng nhập) | Xem kho truyện, lọc theo thể loại, tìm kiếm, đọc chương, tải truyện |
| **Thành viên** | Toàn bộ quyền của Khách, cộng thêm: đăng truyện, quản lý truyện **của mình**, bình luận, đánh dấu truyện |
| **Quản trị viên** | Toàn bộ quyền của Thành viên, cộng thêm: gỡ/khôi phục truyện bất kỳ, khoá/mở khoá tài khoản |

Quyền được kiểm ở **hai tầng**: `Filter` chặn theo nhóm, và `Servlet` kiểm quyền
sở hữu từng bản ghi — đảm bảo người dùng A không sửa được truyện của người dùng B
kể cả khi tự sửa tham số trên URL.

---

## 3. Danh sách chức năng — 16 chức năng

### A. Nhóm chức năng đọc truyện (không cần đăng nhập)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 1 | **Trang chủ** | Hiển thị truyện mới cập nhật và truyện có nhiều lượt xem nhất |
| 2 | **Kho truyện** | Danh sách toàn bộ truyện, có phân trang |
| 3 | **Lọc theo thể loại** | Lọc truyện theo 10 thể loại, hiển thị số truyện mỗi thể loại |
| 4 | **Tìm kiếm** | Tìm truyện theo tên; kết quả có thể chia sẻ qua đường dẫn |
| 5 | **Xem chi tiết truyện** | Thông tin truyện, thể loại, mục lục chương, bình luận |
| 6 | **Đọc chương** | Giao diện đọc riêng: bỏ menu, chữ lớn, phông chữ có chân, độ rộng tối ưu cho việc đọc lâu |
| 7 | **Tải truyện** | Xuất toàn bộ truyện ra tệp `.txt` để đọc ngoại tuyến |

### B. Nhóm chức năng thành viên (cần đăng nhập)

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 8 | **Quản lý tài khoản** | Đăng ký, đăng nhập, đăng xuất. Mật khẩu được băm PBKDF2 trước khi lưu |
| 9 | **Đăng và sửa truyện** | Tạo truyện, chọn thể loại, lưu nháp hoặc công khai. Chỉ tác giả sửa được truyện của mình |
| 10 | **Quản lý chương** | Thêm, sửa, xoá chương. Hệ thống tự đề xuất số chương kế tiếp |
| 11 | **Bình luận** | Bình luận truyện; tự gỡ được bình luận của mình |
| 12 | **Đánh dấu truyện** | Lưu truyện để đọc sau. Hệ thống **tự ghi nhớ vị trí đọc** — mở lại có nút "Đọc tiếp" đưa đúng chương đang dở |

### C. Nhóm chức năng quản trị

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 13 | **Quản lý truyện** | Xem toàn bộ truyện kể cả bản nháp; gỡ truyện vi phạm và khôi phục lại |
| 14 | **Quản lý tài khoản** | Xem danh sách tài khoản; khoá/mở khoá kèm lý do |

### D. Trang thông tin

| # | Chức năng | Mô tả |
|:-:|-----------|-------|
| 15 | **Hướng dẫn sử dụng** | Hướng dẫn từng chức năng cho người dùng mới |
| 16 | **Nội quy cộng đồng** | Quy định nội dung, ứng xử, và mức xử lý vi phạm |

---

## 4. Cơ sở dữ liệu — 7 bảng

| Bảng | Vai trò |
|------|---------|
| `users` | Tài khoản. Một bảng chung cho cả độc giả và tác giả |
| `stories` | Truyện |
| `chapters` | Chương truyện |
| `tags` | Thể loại |
| `story_tags` | Bảng nối truyện ↔ thể loại (quan hệ nhiều–nhiều) |
| `comments` | Bình luận |
| `bookmarks` | Đánh dấu truyện và vị trí đọc |

**Sơ đồ ERD** và các sơ đồ luồng xử lý: xem `docs/so-do.md`.

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

**Bảo mật mật khẩu.** Băm PBKDF2 với 120.000 vòng lặp và chuỗi ngẫu nhiên riêng
cho từng tài khoản. So sánh chuỗi băm bằng thuật toán thời gian cố định.

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
