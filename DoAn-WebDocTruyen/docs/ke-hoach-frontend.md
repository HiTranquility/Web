# Kế hoạch Frontend — Kiến trúc Layout và Trang

Tài liệu này trả lời ba câu:
1. **Bao nhiêu layout?** → 5 (4 bắt buộc + 1 tuỳ chọn)
2. **Bao nhiêu trang?** → 30 trang + 9 mảnh tái dùng
3. **Trang nào dùng layout nào, phục vụ chức năng nào?**

Kế hoạch database ở tài liệu riêng: [`ke-hoach-database.md`](ke-hoach-database.md).
Danh sách chức năng gốc: [`DANG-KY-DE-TAI.md`](DANG-KY-DE-TAI.md).

---

## Phần 1 — Kiến trúc Layout

### Vì sao chỉ 5 layout cho 30 trang

Layout sinh theo **KHUNG**, không sinh theo **TRANG**. Khung = thanh điều hướng,
chân trang, khối bao ngoài. Nội dung khác nhau là chuyện bình thường — đó là lý
do có nhiều *mảnh nội dung*, không phải nhiều *layout*.

### Bảng quyết định — hỏi lần lượt, dừng ở câu đầu tiên trả lời "có"

| # | Câu hỏi | → Layout |
|:-:|---------|----------|
| 1 | Người dùng đang **đọc truyện**, cần bỏ hết thứ gây phân tâm? | `reader` |
| 2 | Người dùng đang **soạn thảo chương dài**, cần bề ngang tối đa? | `editor` |
| 3 | Đây là trang **quản trị**, cần menu bên trái? | `admin` |
| 4 | Người dùng **chưa đăng nhập**, chỉ cần một ô nhập giữa màn hình? | `auth` |
| 5 | **Không câu nào ở trên** | **`main`** |

Câu 5 quan trọng nhất: mặc định là `main`, không phải "tạo layout mới cho chắc".

### Năm layout

| Layout | Số trang | Đặc điểm khung | Trạng thái |
|--------|:--------:|----------------|------------|
| `main` | **18** | nav trên + nội dung + footer đầy đủ | ✅ đã dựng |
| `auth` | **4** | không nav, thẻ card giữa màn hình, footer gọn | ✅ đã dựng |
| `reader` | **1** | thanh trên tối giản, cột hẹp 38em, font serif, không footer | ✅ đã dựng |
| `admin` | **6** | nav trên + sidebar trái + nội dung | ✅ đã dựng |
| `editor` | **1** | thanh công cụ lưu, vùng soạn thảo toàn màn hình | ⬜ tuỳ chọn |

### Cấu trúc từng layout

**`main` — khung chính**

```
┌──────────────────────────────────────────────┐
│  [Logo]  Trang chủ  Kho truyện  Nội quy      │  ← parts/nav.jsp
│                        [avatar] [Đăng xuất]  │     (dính khi cuộn, kính mờ)
├──────────────────────────────────────────────┤
│                                              │
│         ⟨ MẢNH NỘI DUNG ⟩                    │  ← jsp:include contentPage
│         max-width 1200px                     │
│                                              │
├──────────────────────────────────────────────┤
│  Về trang này │ Hướng dẫn │ Quy định         │  ← parts/footer.jsp
└──────────────────────────────────────────────┘
```

**`auth` — đăng nhập / đăng ký**

```
┌──────────────────────────────────────────────┐
│                                              │
│              [Logo ĐọcTruyện]                │
│         ┌────────────────────────┐           │
│         │  ⟨ MẢNH NỘI DUNG ⟩     │           │  ← card 400px, căn giữa
│         │  form đăng nhập        │              cả ngang lẫn dọc
│         └────────────────────────┘           │
│           Nội quy · Về trang chủ             │
│                                              │
└──────────────────────────────────────────────┘
```
Không nav — người chưa đăng nhập thì menu để làm gì.

**`reader` — đọc chương**

```
┌──────────────────────────────────────────────┐
│  ← Kiếm Khí Trường Sinh    Ch.3 · ~5 phút    │  ← thanh tối giản, dính
├──────────────────────────────────────────────┤
│          ⟨ MẢNH NỘI DUNG ⟩                   │
│          cột 38em ≈ 70 ký tự/dòng            │  ← con số nghiên cứu
│          font Lora (serif), 1.12rem          │     về khả năng đọc
│          giãn dòng 1.95                      │
│                                              │
│      [← Ch.2]  [☰ Mục lục]  [Ch.4 →]         │
└──────────────────────────────────────────────┘
```
Không footer — đang đọc thì không ai muốn thấy chân trang.

**`admin` — quản trị**

```
┌──────────────────────────────────────────────┐
│  [Logo]  ...nav dùng chung với main...       │  ← TÁI DÙNG parts/nav.jsp
├───────────┬──────────────────────────────────┤
│ QUẢN TRỊ  │                                  │
│ 📊 Tổng   │     ⟨ MẢNH NỘI DUNG ⟩            │
│ 📚 Truyện │     bảng dữ liệu, cuộn ngang     │
│ 👤 T.khoản│     được trên màn hình hẹp       │
│ 🏷 Thể loại│                                 │
│ 💬 B.luận │                                  │
│ 🚩 Báo cáo│                                  │
├───────────┴──────────────────────────────────┤
│  ...footer dùng chung...                     │  ← TÁI DÙNG parts/footer.jsp
└──────────────────────────────────────────────┘
```
Sidebar dính khi cuộn — bảng quản trị thường dài.

**`editor` — soạn chương (tuỳ chọn)**

```
┌──────────────────────────────────────────────┐
│  ← Truyện X   Ch.5   [Nháp] [Lưu] [Đăng]     │
├──────────────────────────────────────────────┤
│                                              │
│          ⟨ Ô SOẠN THẢO ⟩                     │  ← toàn màn hình
│          font serif, giãn dòng rộng          │     giống trải nghiệm đọc
│                                              │
└──────────────────────────────────────────────┘
```

> **Khi nào `editor` mới đáng làm:** hiện tại form thêm chương dùng `main` và
> vẫn ổn. Chỉ tách layout riêng khi thêm những thứ này: tự lưu nháp, xem trước
> song song, đếm chữ, phím tắt. Chưa có mấy thứ đó thì `main` là đủ —
> **đừng tạo layout cho một cái ô textarea.**

### Bốn tầng CSS

```
base.css            biến màu, reset, typography          — MỌI trang
components.css      nút, thẻ, tag, form, bảng, panel     — MỌI trang
layout-{tên}.css    riêng của khung                      — 5 file
{pageCss}.css       riêng của MỘT trang (tuỳ chọn)       — hiếm dùng
```

Nạp trong `parts/head.jsp` theo đúng thứ tự — file sau ghi đè file trước.
Trang đăng nhập không phải tải CSS của thanh menu và lưới truyện.

---

## Phần 2 — Kiểm kê 30 trang

### A. Trang công khai — layout `main` (10 trang)

| # | Trang | URL | Chức năng phục vụ | Xong |
|:-:|-------|-----|-------------------|:----:|
| 1 | Trang chủ | `/` | Truyện mới cập nhật, xem nhiều nhất | ✅ |
| 2 | Kho truyện | `/story?action=list` | Duyệt, phân trang, lọc thể loại, tìm kiếm | ✅ |
| 3 | Chi tiết truyện | `/story?action=detail&id=` | Thông tin, mục lục, bình luận, đánh giá | ✅ |
| 4 | Trang tác giả | `/user?action=profile&id=` | Hồ sơ công khai, danh sách truyện, nút theo dõi | ⬜ |
| 5 | Bảng xếp hạng | `/rank?period=week` | Top truyện theo tuần/tháng | ⬜ |
| 6 | Hướng dẫn sử dụng | `/page?name=guide` | Hướng dẫn từng chức năng | ✅ |
| 7 | Nội quy cộng đồng | `/page?name=rules` | Quy định nội dung và ứng xử | ✅ |
| 8 | Lỗi 404 | *(container gọi)* | Không tìm thấy trang | ✅ |
| 9 | Lỗi 403 | *(container gọi)* | Không đủ quyền | ⬜ |
| 10 | Lỗi 500 | *(container gọi)* | Lỗi hệ thống | ✅ |

> Trang 2 gánh cả **duyệt, lọc, tìm kiếm** — không tách trang riêng cho kết quả
> tìm kiếm. Cùng một giao diện, chỉ khác tham số URL. Tách ra là nhân đôi code
> mà không được gì.

### B. Trang thành viên — layout `main` (8 trang)

| # | Trang | URL | Chức năng phục vụ | Xong |
|:-:|-------|-----|-------------------|:----:|
| 11 | Truyện đã lưu | `/bookmark?action=list` | Danh sách đánh dấu + tiến độ đọc | ✅ |
| 12 | Truyện của tôi | `/story?action=mine` | Danh sách truyện đã đăng, cả nháp | ✅ |
| 13 | Đăng / sửa truyện | `/story?action=create\|edit` | Form thông tin truyện + chọn thể loại | ✅ |
| 14 | Hồ sơ của tôi | `/user?action=me` | Xem thông tin cá nhân | ⬜ |
| 15 | Sửa hồ sơ | `/user?action=edit` | Đổi tên, avatar, bio, mật khẩu | ⬜ |
| 16 | Thống kê truyện của tôi | `/story?action=stats` | Lượt xem, số lưu, số bình luận | ⬜ |
| 17 | Đang theo dõi | `/follow?action=list` | Tác giả đang theo dõi | ⬜ |
| 18 | Thông báo | `/notification` | Chương mới của truyện đang theo dõi | ⬜ |

### C. Trang xác thực — layout `auth` (4 trang)

| # | Trang | URL | Chức năng phục vụ | Xong |
|:-:|-------|-----|-------------------|:----:|
| 19 | Đăng nhập | `/auth?action=login` | Đăng nhập | ✅ |
| 20 | Đăng ký | `/auth?action=register` | Đăng ký + xác nhận nội quy | ✅ |
| 21 | Quên mật khẩu | `/auth?action=forgot` | Gửi link đặt lại qua email | ⬜ |
| 22 | Đặt lại mật khẩu | `/auth?action=reset&token=` | Nhập mật khẩu mới | ⬜ |

### D. Trang đọc — layout `reader` (1 trang)

| # | Trang | URL | Chức năng phục vụ | Xong |
|:-:|-------|-----|-------------------|:----:|
| 23 | Đọc chương | `/chapter?action=read&id=` | Đọc, chuyển chương, tự ghi vị trí, tuỳ chỉnh cỡ chữ/nền | ✅ |

### E. Trang soạn thảo — layout `editor` hoặc `main` (1 trang)

| # | Trang | URL | Chức năng phục vụ | Xong |
|:-:|-------|-----|-------------------|:----:|
| 24 | Thêm / sửa chương | `/chapter?action=create\|edit` | Soạn nội dung chương | ✅ *(đang dùng `main`)* |

### F. Trang quản trị — layout `admin` (6 trang)

| # | Trang | URL | Chức năng phục vụ | Xong |
|:-:|-------|-----|-------------------|:----:|
| 25 | Bảng điều khiển | `/admin/dashboard` | Thống kê tổng quan | ⬜ |
| 26 | Quản lý truyện | `/admin/story` | Gỡ / khôi phục truyện | ✅ |
| 27 | Quản lý tài khoản | `/admin/user` | Khoá / mở khoá, đổi quyền | ✅ |
| 28 | Quản lý thể loại | `/admin/tag` | Thêm / sửa / xoá thể loại | ⬜ |
| 29 | Quản lý bình luận | `/admin/comment` | Ẩn bình luận vi phạm | ⬜ |
| 30 | Xử lý báo cáo | `/admin/report` | Duyệt báo cáo từ người dùng | ⬜ |

### Tổng kết

| | Đã dựng | Còn lại | Tổng |
|---|:---:|:---:|:---:|
| Trang | **13** | 17 | **30** |
| Layout | **4** | 1 | **5** |

---

## Phần 3 — Chín mảnh tái dùng

Mảnh (`_ten.jsp`) là khối giao diện xuất hiện ở **nhiều trang**. Tách ra để sửa
một chỗ, cả hệ thống đổi theo.

| Mảnh | Dùng ở trang | Nội dung | Xong |
|------|--------------|----------|:----:|
| `_card.jsp` | 1, 2, 4, 5, 12 | Thẻ truyện trong lưới | ✅ |
| `_story-row.jsp` | 11, 17, 26 | Truyện dạng hàng ngang (có ảnh bìa nhỏ) | ⬜ |
| `_pagination.jsp` | 2, 4, 5, 26, 27 | Thanh phân trang | ⬜ *(đang viết thẳng trong list.jsp)* |
| `_tag-filter.jsp` | 2, 4, 5 | Hàng nút lọc thể loại | ⬜ |
| `_comment.jsp` | 3, 29 | Một bình luận (avatar + nội dung + nút gỡ) | ⬜ |
| `_rating-stars.jsp` | 2, 3, 5 | Hiển thị / chấm sao | ⬜ |
| `_chapter-list.jsp` | 3, 12 | Mục lục chương | ⬜ |
| `_empty.jsp` | mọi trang danh sách | Trạng thái rỗng có thiết kế | ⬜ |
| `_stat-tile.jsp` | 16, 25 | Ô thống kê (số lớn + nhãn nhỏ) | ⬜ |

> **Quy tắc tách mảnh:** chỉ tách khi khối đó xuất hiện ở **ít nhất 2 trang**.
> Tách sớm quá thì có một đống file mỗi file 5 dòng, mở ra đọc mệt hơn là để
> nguyên tại chỗ.

---

## Phần 4 — Thứ tự làm frontend

Làm theo thứ tự này để lúc nào cũng có thứ chạy được để xem:

| Giai đoạn | Việc | Trang |
|:---------:|------|-------|
| **1** | Tách 4 mảnh dùng nhiều nhất | `_pagination` `_empty` `_comment` `_tag-filter` |
| **2** | Hoàn thiện nhóm công khai | 4, 5, 9 |
| **3** | Nhóm tài khoản cá nhân | 14, 15 |
| **4** | Chức năng cộng đồng | 17, 18 + `_rating-stars` |
| **5** | Hoàn thiện quản trị | 25, 28, 29, 30 |
| **6** | Đánh bóng | `editor` layout, tuỳ chỉnh cỡ chữ khi đọc |

Mỗi giai đoạn xong là **chạy được và xem được** — không có giai đoạn nào để lại
trang hỏng giữa chừng.

### Tiến độ

| Giai đoạn | Trạng thái |
|:---------:|------------|
| 1 | ✅ xong — 4 mảnh trong `views/_partials/` |
| 2 | ✅ xong — trang 4 (tác giả), 5 (xếp hạng), 9 (403) |
| 3–6 | chưa làm |

---

## Phần 4b — Chế độ xem giao diện (chưa cần MySQL)

Giai đoạn này là **làm giao diện**, nên web phải xem được ngay mà không phải cài
cơ sở dữ liệu trước. Đó là việc của `truyen/util/DemoData.java`.

**Cách hoạt động.** Mỗi hàm ĐỌC trong `dao/` mở đầu bằng đúng một dòng:

```java
if (!DBConnection.isReady()) return DemoData.xxx();
```

`isReady()` chỉ kiểm tra có `db.properties` hay không. Không có → lấy dữ liệu
trong RAM. Có → chạy SQL thật như bình thường.

**Vì sao đặt ở tầng `dao/` chứ không phải `controller/`.** Vì `dao/` đúng là
tầng chịu trách nhiệm "lấy dữ liệu ở đâu ra". Controller không được biết dữ liệu
là thật hay giả — nếu nó biết thì sau này gỡ dữ liệu giả sẽ phải sửa cả tầng
controller. Hiện tại gỡ chỉ cần xoá `DemoData.java` và xoá các dòng `if` đó.

**Cái gì xem được, cái gì chưa.**

| | Trạng thái |
|---|---|
| Xem mọi trang, lọc theo thể loại, tìm kiếm, phân trang | ✅ chạy |
| Đăng nhập bằng tài khoản mẫu | ✅ chạy (băm PBKDF2 thật) |
| Phân quyền: chủ truyện / admin / khách | ✅ chạy |
| Tải truyện `.txt` | ✅ chạy |
| Đăng truyện, sửa, bình luận, ban tài khoản | ❌ báo lỗi rõ ràng — **cần MySQL** |

Các lệnh GHI cố tình vẫn báo lỗi. Giả vờ lưu thành công rồi mất dữ liệu khi tắt
server thì tệ hơn nhiều so với nói thẳng là chưa lưu được.

**Tài khoản mẫu.**

| Tài khoản | Mật khẩu | Vai |
|---|---|---|
| `admin` | `admin123` | quản trị viên |
| `mocmien` | `123456` | tác giả — 3 truyện |
| `haiduong` | `123456` | tác giả — 2 truyện |
| `kiemvu` | `123456` | tác giả — 3 truyện |
| `thuytien` | `123456` | độc giả — 4 truyện đã lưu |
| `spammer` | `123456` | **bị khoá** — thử xem màn chặn |

**Chuyển sang dữ liệu thật:** chạy `scripts\setup-db.ps1`, tạo `db.properties`
từ `db.properties.example`. Không cần sửa dòng code nào — `isReady()` tự
chuyển nhánh.

---

## Phần 4c — Hai lỗi giao diện đã sửa trong giai đoạn này

**1. Thanh menu mất định dạng ở khu quản trị.**
`parts/nav.jsp` và `parts/footer.jsp` được 4 layout dùng chung, nhưng CSS của
chúng lại nằm trong `layout-main.css`. Layout admin không nạp file đó nên thanh
menu hiện ra trơ trụi. Đã chuyển toàn bộ phần khung chung sang `components.css`.

> **Quy tắc:** mảnh JSP dùng ở nhiều layout thì CSS phải ở `components.css`.
> Chỉ mảnh nào riêng một layout mới để trong `layout-<tên>.css`.

**2. Số lượt xem hiện thô `31200` thay vì `31.200`.**
`<fmt:formatNumber>` chọn cách viết số theo header `Accept-Language`. Không có
header đó thì JSTL **im lặng in số thô**, không báo lỗi gì. Trình duyệt luôn gửi
header nên lỗi này chỉ lộ ra khi test bằng `curl` — nhưng nó cũng có nghĩa là
máy cài tiếng Anh thấy `28,360` còn máy tiếng Việt thấy `28.360`.

Đã ghim trong `web.xml`:

```xml
<context-param>
    <param-name>javax.servlet.jsp.jstl.fmt.locale</param-name>
    <param-value>vi_VN</param-value>
</context-param>
```

Dùng `fmt.locale` (ghi đè) chứ không phải `fmt.fallbackLocale` (chỉ dùng khi
trình duyệt im lặng) — để ai xem cũng thấy một kiểu số.

---

## Phần 5 — Quy ước giao diện đã chốt

**Bảng màu Ink & Ember** — nền mực đen ngả xanh, điểm nhấn hổ phách ấm.
Chọn theo nội dung: web đọc truyện là chỗ ngồi lâu buổi tối.

| Vai trò | Biến CSS |
|---------|----------|
| Nền sâu nhất | `--ink-950` `#0b0c10` |
| Nền thẻ | `--ink-850` `#151822` |
| Chữ chính | `--text` `#e8eaf0` |
| Điểm nhấn | `--ember` `#f0863a` |
| Thành công | `--jade` `#2bb789` |
| Cảnh báo | `--rose` `#e5576f` |
| Nền đọc (chế độ giấy) | `--paper` `#f6f1e7` |

**Hai phông chữ, mỗi phông một việc:**

| Phông | Dùng cho | Vì sao |
|-------|----------|--------|
| Plus Jakarta Sans | giao diện: nút, menu, tiêu đề | sans đọc nhanh, gọn |
| **Lora (serif)** | **nội dung truyện** | chân chữ dẫn mắt chạy ngang dòng — đọc vài nghìn chữ đỡ mỏi hơn sans |

Đây là quyết định thiết kế quan trọng nhất của cả dự án: **chỉ ruột truyện mới
đổi phông**, giao diện vẫn sans.

**Quy ước đặt tên class:**

| Loại | Quy tắc | Ví dụ |
|------|---------|-------|
| Khối | danh từ, gạch ngang | `.story-card` |
| Bộ phận | `<khối>-<bộ phận>` | `.story-title` |
| Trạng thái | tiền tố `is-` | `.is-active` |
| Biến thể | `<gốc>-<biến thể>` | `.btn-primary` |
| Màu, khoảng cách | **luôn** dùng biến | `var(--ember)` |

Không viết mã màu thẳng vào file. Đổi tông màu chỉ sửa `base.css`.
