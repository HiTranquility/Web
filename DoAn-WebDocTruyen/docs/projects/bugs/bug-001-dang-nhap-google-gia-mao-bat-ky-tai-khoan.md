# 🐞 bug-001: Bấm "Đăng nhập với Google", gõ email người khác vào là vào thẳng tài khoản của họ

## 📌 Meta

| | |
|---|---|
| **Mã** | bug-001 |
| **Mức** | 🔴 Chặn |
| **Người sửa** | Dev A — Auth & Security |
| **Trạng thái** | 🆕 Mới |
| **Ngày phát hiện** | 2026-09-17 |
| **Nơi xảy ra** | `controller/common/AuthServlet.java` (`firebaseGoogleLogin`, dòng 400–500) · `assets/js/firebase-auth.js` · `views/layout/auth.jsp` |

---

## 1. Tái hiện thế nào

**Luôn luôn lỗi**, không phải thỉnh thoảng. Không cần công cụ gì, làm bằng chuột trên trình duyệt thường.

1. Mở `/auth?action=login` ở chế độ ẩn danh — **không đăng nhập gì cả**.
2. Bấm nút **"Đăng nhập với Google"**.
   Khoá Firebase trong `firebase-auth.js` là khoá mẫu (`AIzaSyDemoKey…`), nên nhánh
   `isProdConfigured` là `false` → chương trình mở thẳng **modal chọn tài khoản demo**.
3. Trong modal, kéo xuống khối *"Đăng nhập tài khoản khác"*, gõ vào ô email:
   `mocmien@gmail.com` *(tài khoản thật trong `sample_data.sql`, tác giả của 3 truyện)*.
4. Bấm **"Đăng nhập tài khoản này"**.

**Thấy:** vào thẳng phiên của `mocmien`. Sửa được truyện của họ, xoá được chương, xoá
được bình luận của họ, đổi được email và ảnh đại diện của họ.

**Đáng ra phải:** máy chủ hỏi Google xem người bấm có thật sự sở hữu email đó không,
không sở hữu thì từ chối.

Gõ email **chưa có trong hệ thống** thì còn tệ hơn một bậc: máy chủ tự tạo tài khoản mới
và đăng nhập luôn — tạo tài khoản không cần mật khẩu, không cần xác minh gì.

---

## 2. Chứng cứ

Chỉ cần một dòng `curl`, không cần mở trình duyệt. `_csrf` lấy từ form đăng nhập của
chính phiên mình — `CsrfFilter` không cản được vì kẻ tấn công có phiên hợp lệ của **nó**:

```bash
curl -X POST 'http://localhost:8080/auth' \
  -b 'JSESSIONID=<phiên của chính bạn>' \
  -d 'action=firebase-google' \
  -d '_csrf=<token trong ô ẩn của trang login>' \
  -d 'email=mocmien@gmail.com' \
  -d 'displayName=Ke Tan Cong' \
  -d 'uid=bia-dat' \
  -d 'idToken=day-la-chuoi-vo-nghia' \
  -d 'ajax=1'
```

```json
{"success":true,"redirect":"/"}
```

`idToken` là **chuỗi bịa**. Máy chủ vẫn trả `success`.

Chỗ sai nằm gọn trong bốn dòng của `AuthServlet.java:408–418`:

```java
String email = trimOrEmpty(request.getParameter("email"));
...
String uid = trimOrEmpty(request.getParameter("uid"));
...
User user = userDAO.findByEmail(email);   // ← tin thẳng email do trình duyệt gửi lên
```

`idToken` được đọc ở `firebase-auth.js` và gửi lên, nhưng **`AuthServlet` không hề đọc
tham số `idToken`** — grep cả file không có chữ nào. Nó chỉ nằm đó cho giống thật.

---

## 3. Nguyên nhân

- **Sai ở:** `controller/common/AuthServlet.java:408–418`
- **Vì:** luồng đăng nhập Google được viết theo hướng *"trình duyệt nói tôi là ai thì tôi
  là người đó"*. Trong OAuth/OIDC, thứ duy nhất chứng minh danh tính là **chữ ký của
  Google trên `idToken`**, và chữ ký đó phải được **máy chủ** kiểm. Ở đây `idToken` được
  truyền lên rồi vứt đi, còn `email` — thứ người dùng gõ tay được — lại là căn cứ duy nhất.

Hai thứ khuếch đại hậu quả:

| Thứ khuếch đại | Vì |
|---|---|
| Modal demo có ô **nhập email tự do** (`firebase-auth.js:275–290`) | Không cần biết `curl`, không cần mở DevTools. Ai mở web cũng khai thác được bằng chuột. |
| Nhánh `user == null` **tự tạo tài khoản** (`AuthServlet.java:420–446`) | Biến lỗ hổng đăng nhập thành cả lỗ hổng tạo tài khoản rác hàng loạt. |

Đoạn chặn ADMIN ở `AuthServlet.java:454` có chắn được đường chiếm tài khoản quản trị —
nhưng nó chỉ bịt **một** tài khoản. Toàn bộ thành viên còn lại vẫn mở toang, và tác giả
mới là người có thứ để mất (truyện, chương).

---

## 4. Sửa thế nào

bug này là **gỡ mìn**, không phải làm tính năng. Mục tiêu duy nhất: sau khi sửa, không
còn đường nào đăng nhập mà không qua Google thật. Làm xong trong một buổi.

- **Đổi 1 — chặn endpoint:** trong `firebaseGoogleLogin()`, trước mọi thứ khác, nếu
  `FirebaseConfig.isVerificationEnabled()` là `false` thì trả JSON
  `{"success":false,"message":"Đăng nhập Google tạm khoá, vui lòng dùng mật khẩu."}`
  rồi `return`. Mặc định là `false` cho tới khi ISSUE-001 phase 2 bật lên.
- **Đổi 2 — bỏ modal demo:** xoá `showGoogleAccountModal()` và mọi chỗ gọi nó trong
  `assets/js/firebase-auth.js`. Ba tài khoản bịa và ô nhập email tự do đi theo.
- **Đổi 3 — ẩn nút:** bọc khối `.btn-google-login` ở `auth/login.jsp` và
  `auth/register.jsp` trong `<c:if test="${googleEnabled}">`, cờ do `AuthServlet` đặt.
  Nút không bấm được thì không ai bấm nhầm rồi tưởng web hỏng.

**Vì sao chặn thay vì sửa cho đúng luôn:** sửa cho đúng nghĩa là thêm thư viện xác minh
`idToken`, đăng ký dự án Google Cloud, và thêm bảng lưu liên kết tài khoản — đó là
[`ISSUE-001`](../issues/ISSUE-001-tich-hop-google.md), năm đợt. Lỗ hổng 🔴 thì
[luật ở `bugs/README.md`](README.md) bảo **sửa trước mọi ISSUE đang mở** — nên bug này
chỉ làm phần chặn, phần làm-cho-đúng để ISSUE-001 phase 2 mở lại.

**Dữ liệu hỏng:** có thể đã có tài khoản rác do nhánh tự-tạo sinh ra lúc chạy thử. Kiểm:

```sql
SELECT id, username, email, created_at FROM users
WHERE password_hash LIKE '%' AND id NOT IN (1,2,3,4,5,6)
ORDER BY created_at DESC;
```

Thấy tài khoản không nhớ đã tạo thì xoá, hoặc nạp lại `sample_data.sql` cho sạch.

---

## 5. Kiểm lại

- [ ] Làm lại đúng bốn bước ở §1 — modal **không còn hiện ra**, nút Google không bấm được
- [ ] Chạy lại lệnh `curl` ở §2 — trả `success:false`, **không** cấp phiên
- [ ] Đăng nhập bằng mật khẩu thường (`mocmien` / `123456`) — **vẫn vào bình thường**
- [ ] Đăng ký tài khoản mới bằng form thường — **vẫn chạy**
- [ ] `mvn test` — 28 test cũ vẫn pass
- [ ] **Có thêm test:** `AuthServletGoogleTest` — gọi `firebase-google` với `idToken` bịa,
      khẳng định không có `session.getAttribute("currentUser")` sau lời gọi

---

## 6. Ghi chú

- Họ hàng gần: [`ISSUE-001`](../issues/ISSUE-001-tich-hop-google.md) — bug này là bước 0
  của nó. Phase 2 của ISSUE-001 **không được bắt đầu** trước khi bug này ✅.
- Khoá Firebase mẫu trong `firebase-auth.js` là khoá bịa nên không có gì để lộ. Nhưng khi
  phase 2 gắn khoá thật vào thì **tuyệt đối không** viết khoá vào file JS đã commit —
  xem cách làm ở phase 2 §3.
- `docs/requirements/MO-TA-DO-AN.md` §5 đang quảng cáo *"Chống Account Takeover"*. Câu đó hiện **sai**.
  Sửa doc ngay khi bug này ✅ — [luật số 3 ở `INDEX.md`](../../INDEX.md).
