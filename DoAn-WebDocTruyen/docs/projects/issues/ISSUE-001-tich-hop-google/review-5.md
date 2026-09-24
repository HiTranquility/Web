# 🔍 [Review 5] Tích hợp Google — Review Phase 5: Tác giả sao lưu truyện lên Google Drive cá nhân

> **Đích:** `docs/projects/issues/ISSUE-001-tich-hop-google/review-5.md`
>
> **Cách dùng:** Điền sau khi hoàn thành xong `phase-5.md`. Đối chiếu baseline kiểm thử, kiến trúc sao lưu Google Drive và Acceptance Criteria.

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | [phase-5.md](phase-5.md) |
| **Ngày review** | 2026-09-17 |
| **Người review** | Tech Lead / Security Reviewer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Phase 5 bao gồm:
1. `util/ChapterToTxt`:
   - Tách phần format truyện và chương từ `DownloadServlet` thành hàm dùng chung theo nguyên tắc DRY.
   - Hàm `formatChapterFileName(int chapterNo, String title)`: Tự động đánh số 3 chữ số chuẩn `001 - <tên chương>.txt`, khử các ký tự cấm `\ / : * ? " < > |`.
   - Hàm `formatChapter(Chapter c)`: Ghép từng chương với tiêu đề và phân cách chuẩn.
   - Hàm `formatStory(Story s, List<Chapter> chapters)`: Dùng chung cho người đọc tải trọn bộ `.txt` tại `DownloadServlet`.
2. `util/DriveClient`:
   - Giao tiếp trực tiếp với Google Drive REST API v3 qua Java `HttpURLConnection` thuần (không kéo thêm thư viện nặng).
   - Chỉ sử dụng phạm vi tối thiểu: scope `https://www.googleapis.com/auth/drive.file`.
   - Tự động tạo thư mục `DocTruyen/<Tên truyện>/` trên Drive người dùng nếu chưa có.
   - Cơ chế ghi đè thông minh (`uploadOrUpdateFile`): Tìm file cũ theo tên trong thư mục, nếu đã có thì gửi `PATCH` (hoặc POST qua `X-HTTP-Method-Override: PATCH`) để cập nhật nội dung thay vì sinh file trùng tên dạng `(1)`, `(2)`.
3. `controller/story/DriveBackupServlet`:
   - Map `@WebServlet("/drive")` và được bảo vệ bởi `AuthFilter`.
   - **Quy tắc bảo mật sở hữu**: Kiểm tra ngay dòng đầu tiên `if (story == null || story.getAuthorId() != currentUser.getId()) { response.sendError(403); return; }`. Ngăn chặn tuyệt đối việc người dùng này tải trộm truyện nháp hoặc chương chưa đăng của người khác về Drive của mình.
   - Hỗ trợ endpoint:
     - `GET /drive?action=chapters&storyId=...`: Trả JSON danh sách chương kèm tên file 3 chữ số. Truyện 0 chương trả thông báo tử tế `Truyện chưa có chương nào để sao lưu`.
     - `POST /drive?action=init&storyId=...`: Chuẩn bị thư mục trên Drive.
     - `POST /drive?action=backup`: Sao lưu từng chương đơn lẻ (để client cập nhật tiến độ `7/29`) hoặc toàn bộ truyện.
4. Giao diện & Client:
   - Nút **"☁️ Sao lưu Drive"** tại `views/user/story/mine.jsp` (danh sách truyện của tác giả) và `views/common/story/detail.jsp` (chỉ hiện khi người xem đúng là tác giả của truyện).
   - `assets/js/drive-backup.js`: Sử dụng Google Identity Services (GIS) token client xin quyền `drive.file`, mở modal tiến độ mượt mà hiển thị thanh tiến trình và thông báo từng chương.
   - CSS modal phong cách hiện đại tích hợp sẵn trong `assets/css/components.css`.
5. Kiểm thử tự động:
   - `src/test/java/truyen/ChapterToTxtTest.java`: 7 test cases bao phủ tên file 3 chữ số, ký tự cấm, định dạng đơn/toàn bộ truyện, truyện 0 chương.
   - `src/test/java/truyen/DriveBackupServletTest.java`: 5 test cases bao phủ chặn 403 khi không phải tác giả, đá về đăng nhập khi chưa có phiên, xử lý truyện 0 chương, trích xuất ID Drive JSON.

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước phase 5 | Sau phase 5 | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 52 pass / 0 fail | **64 pass / 0 fail** | ✅ |
| Biên dịch code (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Bấm **Tải truyện `.txt`** ở truyện 29 chương | ~0.15s, file ~120 KB | ~0.15s, file ~120 KB | ✅ |
| Mở file `.txt` bằng Notepad | Chuẩn UTF-8 có dấu | Chuẩn UTF-8 có dấu 100% | ✅ |
| Kiểm tra quyền sở hữu truyện (Ca 1: `haiduong` gọi truyện của `mocmien`) | — | Trả đúng HTTP 403 Forbidden | ✅ |
| Chưa đăng nhập gọi `/drive` (Ca 5) | — | Chuyển hướng về `/auth?action=login` | ✅ |
| Sao lưu truyện 0 chương (Ca 3) | — | Báo tử tế "Truyện chưa có chương nào" | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria Phase 5

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Chỉ dùng scope `drive.file`, không bao giờ dùng `drive` | ✅ | `drive-backup.js` và `DriveClient` chỉ xin quyền `https://www.googleapis.com/auth/drive.file` |
| Kiểm tra quyền sở hữu truyện nghiêm ngặt | ✅ | `DriveBackupServlet` trả 403 nếu `story.getAuthorId() != currentUser.getId()` |
| Định dạng thư mục `DocTruyen/<tên truyện>/` | ✅ | `DriveClient.getOrCreateStoryFolder` tự tạo thư mục gốc và thư mục truyện |
| Tên file đánh số 3 chữ số `001 - <tên chương>.txt` | ✅ | `ChapterToTxt.formatChapterFileName` sinh chuẩn `001`, `029`, `105` |
| Ghi đè file nếu sao lưu lần hai (không sinh `(1)`, `(2)`) | ✅ | `DriveClient.uploadOrUpdateFile` tìm file cùng tên và gửi update |
| Không khóa trình duyệt khi truyện dài | ✅ | `drive-backup.js` gọi từng chương một và hiện tiến độ `7/29` |
| Nút hiển thị đúng ở `mine.jsp` và `detail.jsp` | ✅ | Chỉ hiện cho tác giả truyện |
| Tách code khỏi `DownloadServlet` không làm vỡ chức năng cũ | ✅ | Dùng chung `ChapterToTxt.formatStory`, tải `.txt` vẫn giữ nguyên 100% định dạng |
| Bộ test tự động bổ sung (12 ca mới) | ✅ | 64/64 tests toàn hệ thống đều xanh trong 1.8 giây |

---

## 🐞 Lỗi phát hiện & Đã xử lý

| # | Mức độ | Mô tả | Vị trí / File | Cách xử lý |
|---|---|---|---|---|
| 1 | Thấp | Tránh protocol exception khi gọi PATCH trên HttpURLConnection | `DriveClient.java` | Dùng chuẩn Google REST API `X-HTTP-Method-Override: PATCH` kèm phương thức POST |
| 2 | Nhẹ | Cảnh báo URL constructor deprecation trên Java 20+ | `DriveClient.java` | Chuyển sang `URI.create(urlStr).toURL()` để mã nguồn sạch và tương thích lâu dài |

---

## 📋 Kết luận toàn bộ ISSUE-001

Toàn bộ **5 Phase** của `ISSUE-001` đã được hoàn thành trọn vẹn:
- **Phase 1**: Mở rộng CSDL với bảng `user_identities`, hỗ trợ tài khoản Google không cần mật khẩu.
- **Phase 2**: Đăng nhập Google thật với Firebase Web SDK & OIDC server verification, vá triệt để lỗ hổng `bug-001`.
- **Phase 3**: Gắn / gỡ tài khoản Google trong trang hồ sơ với Lockout Guard (bảo vệ chống mất quyền truy cập).
- **Phase 4**: Google reCAPTCHA v3 chặn bot tại 3 cửa nhạy cảm với timeout 2s và cơ chế fail-open có chủ ý.
- **Phase 5**: Tác giả sao lưu toàn bộ chương truyện lên Google Drive cá nhân theo cấu trúc thư mục sạch và tên file chuẩn 3 chữ số.
