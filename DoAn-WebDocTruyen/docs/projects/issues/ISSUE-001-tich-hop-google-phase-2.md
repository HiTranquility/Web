# 🚀 ISSUE-001 — Phase 2: Máy chủ tự kiểm chữ ký Google, rồi mới mở lại nút đăng nhập

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [ISSUE-001](ISSUE-001-tich-hop-google.md) — *cùng thư mục* |
| **Đợt** | Phase 2 / tổng 5 đợt |
| **Người làm** | Dev A — Auth & Security |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `controller/common/AuthServlet.java` · `util/GoogleTokenVerifier.java` *(mới)* · `util/GoogleConfig.java` *(mới)* · `dao/IdentityDAO.java` · `assets/js/firebase-auth.js` · `views/auth/login.jsp` · `views/auth/register.jsp` · `views/layout/auth.jsp` · `pom.xml` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** đăng ký dự án trên Google Cloud / Firebase Console lấy khoá thật; thêm
  thư viện xác minh token vào `pom.xml`; viết `GoogleTokenVerifier`; viết lại
  `AuthServlet.firebaseGoogleLogin()` để **đọc `sub` và `email` ra từ token đã kiểm chữ
  ký**, không đọc từ tham số nữa; bật lại nút Google.
- **Để đợt sau:** gắn/gỡ liên kết ở trang hồ sơ (phase 3) · reCAPTCHA (phase 4) ·
  Drive (phase 5). Đợt này chỉ lo **một** câu hỏi: người bấm nút có thật sự là chủ tài
  khoản Google đó không.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách của đợt này:** đây là đợt đụng thẳng vào **đường đăng nhập** — đúng hàng
"sai một dòng là thủng bảo mật toàn site" trong bảng của
[`projects/README.md`](../README.md). Nó cũng là đợt duy nhất **đổi thứ đang chạy**
(`firebaseGoogleLogin` đã có code, chỉ là code sai). Trộn nó với phase 3 (sửa trang hồ sơ)
thì khi đăng nhập hỏng sẽ phải đoán giữa hai vùng; để riêng thì `git diff` của đợt này chỉ
có đúng một luồng để soi.

Đợt này còn có một đặc điểm không đợt nào khác có: **nó phụ thuộc vào thao tác ngoài
repo** — vào Google Cloud Console bấm chuột. Việc đó không `git diff` được, không test tự
động được, nên nó cần một danh sách kiểm riêng (§3.1). Trộn vào đợt khác là chắc chắn quên.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:**
  - [Phase 1](ISSUE-001-tich-hop-google-phase-1.md) — cần bảng `user_identities` và
    `IdentityDAO` để có chỗ ghi `sub`.
  - [bug-001](../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan.md) ✅ — modal demo
    và ô nhập email tự do phải **biến mất** trước. Không thì đợt này vừa viết code đúng ở
    một đường, vừa để hở một đường khác.
- **Xong đợt này mới mở khoá được:** phase 3, phase 4, phase 5 — cả ba.

> Đợt này chặn ba đợt sau. Nhận rồi thì **nói cho Dev B biết ngày dự kiến xong**, vì Dev B
> đang ngồi chờ để bắt đầu phase 5.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Dựng dự án trên Firebase Console, bật Google Sign-In, lấy Web config + Project ID — xem danh sách §3.1 | *(ngoài repo)* | ☐ |
| 2 | Điền khoá thật vào `src/main/resources/google.properties` *(file đã `.gitignore` từ phase 1)* | `google.properties` | ☐ |
| 3 | `util/GoogleConfig.java` — đọc `google.properties` một lần lúc khởi động, giống cách `DBConnection` đọc `db.properties` | `util/GoogleConfig.java` | ☐ |
| 4 | Thêm `com.google.firebase:firebase-admin` vào `pom.xml` | `pom.xml` | ☐ |
| 5 | `util/GoogleTokenVerifier.verify(String idToken)` → trả `GoogleUser{sub, email, name, picture}` hoặc `null`. **Không** ném exception ra ngoài | `util/GoogleTokenVerifier.java` | ☐ |
| 6 | Viết lại `AuthServlet.firebaseGoogleLogin()` theo đúng thứ tự ở §3.2 | `controller/common/AuthServlet.java` | ☐ |
| 7 | Bỏ `firebaseConfig` cứng trong JS; để `auth.jsp` in config ra từ `GoogleConfig` *(chỉ phần công khai được)* | `assets/js/firebase-auth.js` · `views/layout/auth.jsp` | ☐ |
| 8 | Bật lại nút Google: `googleEnabled = GoogleConfig.isEnabled()`, JSP bọc `<c:if>` | `AuthServlet` · `login.jsp` · `register.jsp` | ☐ |
| 9 | Test `GoogleTokenVerifierTest` — token rỗng, token bịa, token hết hạn, token của dự án khác → **cả bốn đều trả `null`** | `src/test/java/truyen/GoogleTokenVerifierTest.java` | ☐ |

### 3.1 Việc phải làm ngoài repo — tick từng dòng

Phần này không có trong code nên không ai review hộ được. Tick tay:

- [ ] Tạo dự án ở <https://console.firebase.google.com> *(tên gợi ý: `webdoctruyen`)*
- [ ] **Authentication → Sign-in method → Google → Enable**
- [ ] **Authentication → Settings → Authorized domains**: thêm `localhost`.
      *(Quên bước này thì popup mở ra rồi đóng ngay, báo `auth/unauthorized-domain` —
      lỗi tốn thời gian nhất của cả đợt.)*
- [ ] **Project settings → General**: chép `apiKey`, `authDomain`, `projectId` *(ba cái này
      công khai được, in ra HTML là bình thường)*
- [ ] **Project settings → Service accounts → Generate new private key** → tải file JSON
- [ ] File JSON đó đặt ở `src/main/resources/firebase-service-account.json` và
      **thêm vào `.gitignore` ngay lập tức**. ⚠️ File này là **chìa khoá toàn quyền** của
      dự án Firebase — nó khác hoàn toàn với `apiKey` ở dòng trên.
- [ ] `git status` — khẳng định không có file JSON nào trong danh sách chờ commit

### 3.2 `firebaseGoogleLogin()` phải chạy đúng thứ tự này

Thứ tự quan trọng hơn nội dung. Đọc như một danh sách chặn, dừng ở chỗ đầu tiên sai:

```text
1. Không phải POST                      -> 405, dừng
2. GoogleConfig.isEnabled() == false    -> JSON success:false, dừng
3. idToken rỗng                         -> JSON success:false, dừng
4. verifier.verify(idToken) == null     -> JSON success:false, dừng   ← CHẶN THẬT Ở ĐÂY
   -- từ dòng này trở xuống, KHÔNG đọc request.getParameter() nữa --
5. sub, email, name, picture := lấy TỪ KẾT QUẢ verify
6. identity := IdentityDAO.findByProviderUid(GOOGLE, sub)
   6a. có   -> user := UserDAO.findById(identity.userId)         (đăng nhập lần 2 trở đi)
   6b. chưa -> UserDAO.findByEmail(email)
       - ra user  -> JSON success:false, "Email đã có tài khoản, đăng nhập bằng mật
                     khẩu rồi vào Hồ sơ gắn Google."              (KHÔNG tự gắn — xem dưới)
       - null     -> tạo user mới (password_hash = NULL) + IdentityDAO.insert(...)
7. user.isAdmin()  -> JSON success:false   (giữ nguyên hàng rào cũ)
8. user.isBanned() -> JSON success:false + lý do
9. session.invalidate() rồi getSession(true)   ← chống Session Fixation
10. đặt currentUser vào phiên, JSON success:true
```

> **Vì sao bước 6b KHÔNG tự gắn Google vào tài khoản trùng email.** Tự gắn nghe tiện, và
> rất nhiều web làm vậy. Nhưng nó có nghĩa là: ai đăng ký được một Gmail trùng với email
> mà nạn nhân đã dùng ở web này là chiếm luôn tài khoản đó. Google có xác minh email thật,
> nên rủi ro thấp — nhưng "thấp" không phải "không", và đây đúng là loại lỗi vừa mới phải
> đi vá ở [bug-001](../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan.md). Bắt
> người ta đăng nhập bằng mật khẩu **một lần** rồi mới gắn thì không còn chỗ nào để lách.
> Luồng gắn đó là [phase 3](ISSUE-001-tich-hop-google-phase-3.md).

> **Vì sao bước 9 phải có.** Không đổi ID phiên sau khi đăng nhập thì kẻ tấn công đưa nạn
> nhân một `JSESSIONID` biết trước, đợi nạn nhân đăng nhập, rồi dùng chính ID đó vào thẳng
> tài khoản họ — **Session Fixation**. Kiểm nhanh luồng đăng nhập mật khẩu hiện tại có làm
> việc này chưa; chưa thì sửa luôn ở đây và ghi vào §7.

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

> **[MUST] Điền số thật.**

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `mvn test` | *(điền — sau phase 1 phải nhiều hơn 28)* |
| `mvn -q compile` | *(điền)* |
| `curl` giả mạo ở [bug-001 §2](../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan.md) | *(phải đã là `success:false` — chưa phải thì bug-001 chưa xong, **dừng lại**)* |
| Đăng nhập mật khẩu `mocmien`/`123456` | *(điền)* |
| Kích thước file `.war` sau `mvn package` | *(điền — `firebase-admin` là thư viện nặng, sẽ tăng đáng kể)* |

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `mvn test` — không tụt, có thêm `GoogleTokenVerifierTest` (4 ca)
- [ ] **Bằng tay — đường chính:**
  1. Mở `/auth?action=login` ở cửa sổ ẩn danh, bấm **Đăng nhập với Google**
  2. Popup Google **thật** hiện ra, chọn một Gmail chưa từng dùng ở web này
  3. Kết quả mong đợi: về trang chủ, góc phải hiện tên và ảnh lấy từ Google
  4. Đăng xuất, bấm Google lại bằng đúng Gmail đó → vào lại **cùng tài khoản**, không tạo
     tài khoản thứ hai. Kiểm: `SELECT COUNT(*) FROM users WHERE email = '<gmail đó>';` → **1**
- [ ] **Thử trường hợp xấu — cả năm ca:**
  1. Chạy lại `curl` ở [bug-001 §2](../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan.md)
     với `idToken` bịa → `success:false`, **không** có phiên
  2. Gửi `idToken` **thật nhưng của dự án Firebase khác** → `success:false`
     *(đây là ca dễ bỏ sót nhất — chữ ký đúng, nhưng `aud` sai)*
  3. Bấm Google bằng Gmail **trùng** với `mocmien@gmail.com` → hiện lời nhắc đăng nhập bằng
     mật khẩu, **không** cấp phiên, **không** tạo dòng `user_identities`
  4. Bấm Google bằng Gmail của tài khoản đã bị ban → báo bị khoá kèm lý do
  5. Đổi `google.properties` thành khoá sai rồi khởi động lại → trang đăng nhập **vẫn mở
     bình thường**, chỉ nút Google ẩn hoặc báo lỗi. Không màn hình trắng, không 500
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** đăng nhập mật khẩu · đăng ký · quên mật khẩu ·
      đăng truyện · bình luận · trang admin
- [ ] `git ls-files | xargs grep -l "AIza"` → **không ra gì**
- [ ] `git status` → không có `firebase-service-account.json`

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở bảng Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Đã commit: `ISSUE-001 phase-2 — xac minh idToken o may chu, mo lai dang nhap Google`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Sửa [`docs/requirements/MO-TA-DO-AN.md`](../../requirements/MO-TA-DO-AN.md) §5 — câu "Chống Account Takeover"
      giờ mới đúng, và thêm một dòng về đăng nhập Google vào bảng 16 chức năng
- [ ] Nhắn cả nhóm: ai muốn chạy thử đăng nhập Google trên máy mình thì cần
      `google.properties` — **gửi qua chat riêng, không đẩy lên git**

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm. Nếu bỏ qua bước Session Fixation ở §3.2 bước 9 thì phải ghi ở
đây, đừng im lặng.)*
