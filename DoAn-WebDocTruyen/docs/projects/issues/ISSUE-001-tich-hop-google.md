# 🏷️ ISSUE-001: Tích hợp Google trọn gói — đăng nhập thật, chống bot, sao lưu lên Drive

## 📌 Meta

| | |
|---|---|
| **Mã** | ISSUE-001 |
| **Người làm** | Dev A — Auth & Security *(điều phối; phase 5 giao Dev B — xem bảng §3)* |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày mở** | 2026-09-17 |
| **CASE liên quan** | CASE 01 — Đăng ký / Đăng nhập / Đăng xuất *(mở rộng sau khi 11 CASE đã ✅)* |
| **Đụng vào** | `database/schema.sql` · `AuthServlet` · `UserServlet` · `StoryServlet` · `CommentServlet` · `UserDAO` · `IdentityDAO` *(mới)* · `GoogleTokenVerifier` *(mới)* · `filter/RecaptchaFilter` *(mới)* · `views/auth/login.jsp` · `views/auth/register.jsp` · `views/user/edit.jsp` · `views/story/detail.jsp` · `assets/js/firebase-auth.js` · `pom.xml` |

---

## 1. Làm cái gì, và vì sao

Nút **"Đăng nhập với Google"** đã có sẵn trên trang đăng nhập và đăng ký từ lâu, nhưng
phía sau nó không có gì thật: máy chủ tin thẳng cái email mà trình duyệt gửi lên, nên ai
cũng đăng nhập được vào tài khoản của người khác — đó là [`bug-001`](../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan.md),
mức 🔴. Việc này biến cái vỏ đó thành thứ chạy thật, rồi tận dụng luôn hệ sinh thái Google
cho hai chỗ khác mà đồ án đang hở: **bot spam** và **tác giả mất bản thảo**.

- **Hiện tại:**
  - Đăng nhập Google là giả — gõ email người khác vào là vào được tài khoản họ.
  - Một người muốn vừa dùng mật khẩu vừa dùng Google thì không được: `users` không có chỗ
    nào ghi "tài khoản này liên kết với Google nào", và `password_hash` là `NOT NULL` nên
    người chỉ có Google vẫn bị nhét một chuỗi băm rác.
  - Không có lớp chống bot nào. Form đăng ký, đăng nhập, bình luận đều mở, không giới hạn.
  - Tác giả viết 40 chương trong `chapters`. Máy hỏng hoặc lỡ tay xoá truyện là mất trắng —
    `DownloadServlet` có xuất `.txt` nhưng phải nhớ bấm tay từng truyện.
- **Sau khi xong:**
  - Bấm Google → popup Google thật → máy chủ **kiểm chữ ký trên `idToken`** rồi mới cấp phiên.
  - Một người có thể gắn/gỡ tài khoản Google vào tài khoản sẵn có, ở trang Sửa hồ sơ.
  - Bot bị chặn ở đăng ký, đăng nhập và bình luận bằng reCAPTCHA v3, người thật không phải
    nhìn thấy ô tick nào.
  - Tác giả bấm một nút là toàn bộ truyện được đẩy lên Google Drive của **chính họ**.

---

## 2. Xong là thế nào (tick được mới tính)

- [ ] Gọi `POST /auth?action=firebase-google` với `idToken` bịa → trả `success:false`,
      **không** cấp phiên. Gọi với `idToken` thật do Google cấp → vào được.
- [ ] Đăng nhập Google bằng một Gmail chưa từng có trong `users` → tạo tài khoản mới, hiện
      đúng tên và ảnh đại diện lấy từ Google.
- [ ] Đăng nhập Google bằng Gmail **trùng** với một tài khoản mật khẩu đã có → **không** tạo
      trùng, mà báo "Email này đã có tài khoản, đăng nhập bằng mật khẩu rồi gắn Google vào".
- [ ] Tài khoản ADMIN bấm Google → vẫn bị từ chối *(giữ nguyên hàng rào đang có)*.
- [ ] Vào `/user?action=edit` thấy dòng **"Tài khoản Google: đã gắn — &lt;email&gt; · [Gỡ]"**.
      Bấm Gỡ mà tài khoản **chưa có mật khẩu** → bị chặn, kèm lời nhắc đặt mật khẩu trước.
- [ ] Viết script gửi 50 lượt đăng ký liên tiếp → reCAPTCHA chặn từ lượt thứ vài; đăng ký
      bằng tay trên trình duyệt thật → **không thấy ô tick nào**, vẫn đăng ký được.
- [ ] Ở `/story?action=detail` của truyện mình, bấm **"Sao lưu lên Google Drive"** → sau
      khi cho phép, mở Drive thấy thư mục `DocTruyen/<tên truyện>/` chứa đủ các chương `.txt`.
- [ ] Không có khoá Google nào nằm trong file đã `git add`. Kiểm bằng
      `git ls-files | xargs grep -l "AIza"` → **không ra gì**.
- [ ] Rút mạng rồi mở trang đăng nhập → trang vẫn hiện bình thường, chỉ nút Google báo
      "không kết nối được Google". Không có màn hình trắng.
- [ ] Đã chạy thử trên trình duyệt thật, không chỉ biên dịch sạch

---

## 3. Chia đợt — 5 phase

Việc này **bắt buộc chia đợt**: nó sửa `database/schema.sql`, nó đụng đường đăng nhập, và
nó thêm một filter mới. Cả ba đều nằm trong bảng "phải tách" ở
[`projects/README.md`](../README.md).

| Phase | Nội dung | Người làm | Vì sao đứng riêng | Phải xong trước |
|:--:|---|---|---|---|
| [**1**](ISSUE-001-tich-hop-google-phase-1.md) | Schema: bảng `user_identities`, `password_hash` cho `NULL` | Dev A | Sửa `schema.sql` — **luôn đứng riêng và làm trước** | — |
| [**2**](ISSUE-001-tich-hop-google-phase-2.md) | Xác minh `idToken` ở máy chủ, mở lại nút Google | Dev A | Đụng đường đăng nhập — sai một dòng là thủng toàn site | phase 1 · [bug-001](../bugs/bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan.md) |
| [**3**](ISSUE-001-tich-hop-google-phase-3.md) | Gắn / gỡ tài khoản Google ở trang hồ sơ | Dev A | Đổi thứ đang chạy đúng (`user?action=edit`) | phase 2 |
| [**4**](ISSUE-001-tich-hop-google-phase-4.md) | reCAPTCHA v3 cho đăng ký · đăng nhập · bình luận | Dev A | Thêm `filter/` mới — cùng hàng rủi ro với `AuthFilter` | phase 2 |
| [**5**](ISSUE-001-tich-hop-google-phase-5.md) | Google Drive — tác giả sao lưu truyện | Dev B | Không đụng auth; làm song song được sau phase 2 | phase 2 |

> **Phase 1 và 2 là bắt buộc.** Phase 3–5 làm được tới đâu tính tới đó — dừng sau phase 2
> thì đồ án vẫn có một chức năng đăng nhập Google chạy thật và an toàn. Ghi rõ phần dừng
> lại ở §6, đừng để trống.

Phase 4 và 5 **không phụ thuộc nhau**, hai người làm song song được sau khi phase 2 ✅.

---

## 4. Quy ước phải theo

| Việc trong ISSUE này | Đọc |
|---|---|
| Đặt tên `GoogleTokenVerifier`, `IdentityDAO`, contract 4 tầng, URL `?action=` | [`01-CODING §1 §2 §5`](../../standards/01-CODING_CONVENTIONS.md) |
| Attribute `googleEnabled`, `linkedGoogle`, scope, `<c:out>` cho email lấy từ Google | [`02-VIEW §3 §4`](../../standards/02-VIEW_CONVENTIONS.md) |
| Đặt tên bảng `user_identities`, kiểu cột, khoá ngoại, luật DAO | [`03-DATABASE §2 §4`](../../standards/03-DATABASE_CONVENTIONS.md) |
| Commit message `ISSUE-001 phase-N — …`, nhánh | [`04-GIT §1 §2`](../../standards/04-GIT_CONVENTIONS.md) |

**Thêm một luật riêng cho việc này, không có trong `standards/`:**

> **[NEVER] để khoá Google trong file được commit.** Không trong `.java`, không trong
> `.jsp`, không trong `.js`, không trong `pom.xml`. Chỗ duy nhất là
> `src/main/resources/google.properties`, và file đó phải nằm trong `.gitignore` ngay ở
> commit đầu tiên của phase 1 — **trước** khi có khoá thật để mà lỡ tay. Kèm
> `google.properties.example` chứa key rỗng, giống cách `db.properties` đang làm.
>
> Vì sao nghiêm tới vậy: khoá đã vào lịch sử git thì xoá ở commit sau **không đủ** —
> giống hệt cảnh báo về `db.properties` ở [`CHECKLIST.md §1`](../../guides/CHECKLIST.md).

---

## 5. Đã kiểm thế nào

*Điền lúc chuyển sang ✅. Bỏ trống = chưa xong, dù code đã viết.*

- **Bấm thử:**
- **Thử trường hợp xấu:**
- **Chạy lại test:** `mvn test` —

---

## 6. Ghi chú

**Vì sao Firebase Auth chứ không tự viết OAuth 2.0 từ đầu.** Tự viết nghĩa là tự lo
`state` chống CSRF, tự đổi `code` lấy token, tự làm mới token, tự cache khoá công khai của
Google. Firebase gói hết phần đó lại và trả về một `idToken` chuẩn OIDC — phần việc còn
lại của mình đúng một câu: *kiểm chữ ký rồi đọc `email` và `sub` ra*. Vỏ Firebase SDK cũng
đã nằm sẵn trong `views/layout/auth.jsp:64–68`, không phải dựng lại.

**Thứ cố tình KHÔNG làm trong ISSUE này:**

| Bỏ | Vì |
|---|---|
| Đăng nhập Facebook / GitHub | Thêm nhà cung cấp thứ hai chỉ là thêm một dòng vào `user_identities.provider`. Làm khi có nhu cầu thật, không làm cho đủ bộ. |
| Gmail API để gửi mail | Gửi mail tách riêng thành **ISSUE-002** (SMTP thường), để nó không phải nằm chờ toàn bộ việc Google này xong. |
| Google Analytics | Trang `/story?action=stats` và `admin/dashboard` đã tự vẽ biểu đồ từ `view_logs`. Gắn thêm GA là đẩy dữ liệu người đọc sang bên thứ ba mà không đổi lại được gì cho đồ án. |
| Google One Tap | Đẹp, nhưng là một luồng đăng nhập **thứ hai** phải kiểm bảo mật riêng. Xong 5 phase rồi tính. |

**Cái bẫy lớn nhất của việc này** nằm ở phase 3, không nằm ở phase 2: khi một người chỉ có
Google, chưa từng đặt mật khẩu, mà bấm **Gỡ** liên kết Google ra — họ tự khoá mình ra ngoài
vĩnh viễn. Phase 3 §5 có đúng một dòng kiểm cho chuyện đó. Đừng bỏ.
