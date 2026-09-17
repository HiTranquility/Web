# 🚀 ISSUE-001 — Phase 4: Google reCAPTCHA v3 chặn bot ở ba cửa

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [ISSUE-001](ISSUE-001-tich-hop-google.md) — *cùng thư mục* |
| **Đợt** | Phase 4 / tổng 5 đợt |
| **Người làm** | Dev A — Auth & Security |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `filter/RecaptchaFilter.java` *(mới)* · `util/RecaptchaVerifier.java` *(mới)* · `util/GoogleConfig.java` · `WEB-INF/web.xml` · `views/auth/login.jsp` · `views/auth/register.jsp` · `views/story/detail.jsp` · `assets/js/recaptcha.js` *(mới)* · `views/layout/parts/head.jsp` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** gắn reCAPTCHA v3 vào **đúng ba cửa** — đăng ký, đăng nhập, gửi bình
  luận. Máy chủ chấm điểm từng lượt gửi; điểm thấp thì từ chối. Người thật **không thấy ô
  tick nào**, không phải chọn ảnh xe buýt.
- **Để đợt sau:** không có gì. Đợt này độc lập với phase 5.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách của đợt này:** đợt này thêm một **filter mới** vào `web.xml` — đúng hàng
*"Có đụng `filter/`"* trong bảng của [`projects/README.md`](../README.md), cùng chiếu với
`AuthFilter` và `CsrfFilter`.

Filter nguy hiểm theo một kiểu riêng: nó chạy **trước mọi servlet**, nên một dòng sai ở đây
không làm hỏng một trang mà làm hỏng **cả site**. Kiểu hỏng kinh điển của đúng loại filter
này là *"Google không trả lời trong 3 giây → filter treo → toàn bộ web đứng"*. Nó không
xuất hiện lúc chạy thử trên máy mình, nó xuất hiện đúng lúc demo trước lớp mà wifi phòng
học kém. Vì vậy §3.1 có một luật cứng về timeout, và §5 có một ca **rút mạng** phải thử thật.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [Phase 2](ISSUE-001-tich-hop-google-phase-2.md) — dùng
  lại `GoogleConfig` và cơ chế đọc `google.properties` đã dựng ở đó.
- **Xong đợt này mới mở khoá được:** không có.

> Phase 4 và [phase 5](ISSUE-001-tich-hop-google-phase-5.md) **chạy song song được**. Hai
> người nhận hai đợt cùng lúc sau khi phase 2 ✅ — hai đợt này không đụng file nào của nhau.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Đăng ký site key + secret key ở <https://www.google.com/recaptcha/admin> *(chọn **v3**, domain `localhost`)* | *(ngoài repo)* | ☐ |
| 2 | Thêm `recaptcha.site.key` và `recaptcha.secret.key` vào `google.properties` **và** vào `google.properties.example` *(bản example để rỗng)* | `google.properties` · `.example` | ☐ |
| 3 | `util/RecaptchaVerifier.verify(token, action, ip)` → `double` điểm 0.0–1.0, gọi `https://www.google.com/recaptcha/api/siteverify`. **Timeout cứng 2 giây** | `util/RecaptchaVerifier.java` | ☐ |
| 4 | `filter/RecaptchaFilter` — chỉ chạy cho POST tới ba URL ở §3.1, các đường khác đi thẳng | `filter/RecaptchaFilter.java` | ☐ |
| 5 | Khai báo filter trong `web.xml`, đặt **sau `CsrfFilter`** — lý do ở §3.2 | `WEB-INF/web.xml` | ☐ |
| 6 | `assets/js/recaptcha.js` — lấy token lúc submit, nhét vào ô ẩn `g-recaptcha-token` | `assets/js/recaptcha.js` | ☐ |
| 7 | Nạp script reCAPTCHA **có điều kiện** ở `head.jsp`: `<c:if test="${recaptchaEnabled}">` | `views/layout/parts/head.jsp` | ☐ |
| 8 | Thêm ô ẩn vào ba form: đăng ký, đăng nhập, gửi bình luận | `login.jsp` · `register.jsp` · `story/detail.jsp` | ☐ |
| 9 | Test `RecaptchaFilterTest` — filter **bỏ qua** GET, bỏ qua URL ngoài danh sách, và **cho qua** khi tính năng tắt | `src/test/java/truyen/RecaptchaFilterTest.java` | ☐ |

### 3.1 Ba cửa, và luật timeout

| URL | Ngưỡng điểm | Dưới ngưỡng thì |
|---|:--:|---|
| `POST /auth` với `action=register` | **0.5** | Từ chối, hiện *"Không xác minh được bạn là người thật, thử lại sau."* |
| `POST /auth` với `action=login` | **0.3** | Từ chối *(ngưỡng thấp hơn — người thật gõ sai mật khẩu vài lần là chuyện bình thường, đừng khoá họ ra ngoài)* |
| `POST /comment` với `action=add` | **0.5** | Từ chối |

Ba cửa này, **không thêm**. Không gắn vào đọc chương, không gắn vào tìm kiếm, không gắn
vào bấm sao — những chỗ đó không có gì để bot chiếm, mà gắn vào thì mỗi lượt xem trang là
một lượt gọi sang Google.

> **[MUST] Google không trả lời trong 2 giây thì CHO QUA, không chặn.**
>
> ```java
> conn.setConnectTimeout(2000);
> conn.setReadTimeout(2000);
> // ...
> catch (IOException e) {
>     log("reCAPTCHA khong ket noi duoc, cho qua: " + e.getMessage());
>     chain.doFilter(req, res);   // fail-open, CÓ CHỦ Ý
>     return;
> }
> ```
>
> Đây là một lựa chọn đánh đổi, và nó đi **ngược** với phản xạ bảo mật thông thường
> ("không chắc thì chặn"). Lý do: hậu quả hai bên không cân nhau. Chặn nhầm khi mạng lỗi
> = **không ai đăng nhập được, cả site chết**. Cho qua nhầm khi mạng lỗi = vài con bot lọt
> trong vài giây mạng chập. Với một đồ án sẽ được demo trên wifi phòng học, vế đầu là thứ
> phải tránh bằng mọi giá. Ghi hẳn comment `// fail-open, CÓ CHỦ Ý` vào code — không thì
> người sau sẽ tưởng là thiếu sót và "sửa" nó thành fail-closed.

### 3.2 Vì sao `RecaptchaFilter` đứng SAU `CsrfFilter` trong `web.xml`

Thứ tự filter là thứ tự khai báo `<filter-mapping>` trong `web.xml`, không phải thứ tự
`<filter>`. Đặt sau `CsrfFilter` vì: request thiếu CSRF token là request rác, nên loại nó
ra bằng một phép so chuỗi **trong bộ nhớ** trước, rồi mới tốn một lượt gọi mạng sang
Google cho những request còn lại. Đặt ngược lại thì mỗi request rác cũng ăn một lượt gọi
mạng — và đó chính là thứ kẻ tấn công dùng để làm site chậm.

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `mvn test` | *(điền)* |
| `mvn -q compile` | *(điền)* |
| Thời gian tải `/auth?action=login` *(tab Network của trình duyệt)* | *(điền số ms)* |
| Thời gian POST đăng nhập | *(điền số ms)* |
| Đăng ký · đăng nhập · bình luận | *(cả ba chạy?)* |

**[GOTCHA]** Hai dòng thời gian là bắt buộc ở đợt này. Filter này thêm một lượt gọi mạng
vào mỗi lần POST. Không đo trước thì lúc có người kêu *"web chậm hẳn"* sẽ không cãi được
bằng số, chỉ cãi được bằng cảm giác.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `mvn test` — không tụt, có `RecaptchaFilterTest`
- [ ] **Bằng tay — đường chính:**
  1. Đăng ký một tài khoản mới bằng tay trên trình duyệt thật
  2. Kết quả mong đợi: **không thấy ô tick nào, không phải chọn ảnh**, đăng ký xong bình thường
  3. Xem log Tomcat: có dòng ghi điểm reCAPTCHA, điểm phải **≥ 0.7** *(người thật thường 0.9)*
- [ ] **Thử trường hợp xấu — cả năm ca:**
  1. 🔴 **Rút mạng** *(hoặc chặn `google.com` trong hosts)* rồi đăng nhập → **vẫn đăng nhập
     được**, log ghi "không kết nối được, cho qua". Ca này **bắt buộc thử thật**, không
     được suy luận là chắc chạy
  2. `curl -X POST` đăng ký không kèm `g-recaptcha-token` → **bị từ chối**
  3. Gửi lại đúng một token đã dùng rồi → bị từ chối *(token reCAPTCHA dùng một lần)*
  4. Để trang đăng nhập mở **hơn 2 phút** rồi mới bấm → token hết hạn. Phải hiện lời nhắc
     tải lại trang, **không** phải lỗi 500 *(đây là ca người dùng thật hay gặp nhất)*
  5. Xoá `recaptcha.secret.key` khỏi `google.properties`, khởi động lại → tính năng **tự
     tắt**, cả site chạy bình thường, script Google không được nạp
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** POST ở các đường **không** nằm trong ba cửa —
      đăng truyện · thêm chương · chấm sao · báo cáo · gỡ truyện ở trang admin. Cả năm phải
      **không** bị filter đụng vào
- [ ] Đo lại hai dòng thời gian ở §4 — tăng bao nhiêu ms, ghi số vào đây: ______

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở bảng Baseline đo lại **bằng hoặc tốt hơn** *(riêng thời gian POST được phép
      tăng — nhưng phải **ghi con số thật** ra, không được lờ đi)*
- [ ] Đã commit: `ISSUE-001 phase-4 — them reCAPTCHA v3 cho dang ky, dang nhap, binh luan`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Thêm `RecaptchaFilter` vào bảng filter trong [`docs/architecture/cau-truc.md`](../../architecture/cau-truc.md)
      và [`so-do.md`](../../architecture/so-do.md) — sơ đồ thiếu một filter là sơ đồ sai

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
