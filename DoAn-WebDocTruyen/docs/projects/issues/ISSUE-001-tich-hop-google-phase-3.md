# 🚀 ISSUE-001 — Phase 3: Gắn và gỡ tài khoản Google ở trang hồ sơ

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [ISSUE-001](ISSUE-001-tich-hop-google.md) — *cùng thư mục* |
| **Đợt** | Phase 3 / tổng 5 đợt |
| **Người làm** | Dev A — Auth & Security |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `controller/user/UserServlet.java` · `dao/IdentityDAO.java` · `dao/UserDAO.java` · `views/user/edit.jsp` · `views/user/me.jsp` · `assets/js/firebase-auth.js` |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** ở `/user?action=edit` thêm một khối **"Đăng nhập bằng Google"** với
  hai trạng thái — *chưa gắn* thì có nút Gắn, *đã gắn* thì hiện email Google kèm nút Gỡ.
  Thêm luôn đường **đặt mật khẩu lần đầu** cho tài khoản chỉ có Google.
- **Để đợt sau:** reCAPTCHA (phase 4) · Drive (phase 5).

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách của đợt này:** đây là đợt **đổi một trang đang chạy đúng** —
`/user?action=edit` là form sửa hồ sơ, hiện đang hoạt động bình thường và không ai muốn nó
hỏng. Trộn vào phase 2 thì khi form sửa hồ sơ lỗi sẽ phải soi cả code xác minh token.

Còn một lý do nữa, quan trọng hơn: đợt này là nơi **duy nhất** trong cả ISSUE có thể khoá
vĩnh viễn một người ra khỏi tài khoản của họ (§5, ca xấu số 1). Lỗi đó không có màn hình
đỏ, không có stack trace — nó chỉ im lặng xảy ra và người dùng mất tài khoản. Loại lỗi đó
xứng đáng có một đợt riêng để soi kỹ, chứ không lẫn vào một `git diff` 600 dòng.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** [Phase 2](ISSUE-001-tich-hop-google-phase-2.md) — cần
  `GoogleTokenVerifier` chạy thật, vì nút "Gắn" dùng đúng luồng verify đó.
- **Xong đợt này mới mở khoá được:** không có. Phase 4 và 5 không chờ đợt này.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | `UserServlet` thêm `action=link-google` — nhận `idToken`, verify, `IdentityDAO.insert()` cho **người đang đăng nhập** | `controller/user/UserServlet.java` | ☐ |
| 2 | `UserServlet` thêm `action=unlink-google` — kèm **hàng rào** ở §3.1 | `controller/user/UserServlet.java` | ☐ |
| 3 | `UserServlet` thêm `action=set-password` — đặt mật khẩu lần đầu cho tài khoản `password_hash IS NULL` | `controller/user/UserServlet.java` | ☐ |
| 4 | `UserDAO.hasPassword(int userId)` → `boolean` *(đọc `password_hash IS NOT NULL`)* | `dao/UserDAO.java` | ☐ |
| 5 | Khối "Đăng nhập bằng Google" ở form sửa hồ sơ, hai trạng thái | `views/user/edit.jsp` | ☐ |
| 6 | Trang `me.jsp` hiện một dòng nhỏ "Đã gắn Google" cho người ta biết mà không phải vào form sửa | `views/user/me.jsp` | ☐ |
| 7 | JS: nút Gắn dùng lại `handleGoogleSignIn` nhưng gửi về `/user?action=link-google` | `assets/js/firebase-auth.js` | ☐ |
| 8 | Test `UnlinkGuardTest` — bốn tổ hợp (có/không mật khẩu × có/không Google) | `src/test/java/truyen/UnlinkGuardTest.java` | ☐ |

### 3.1 Hàng rào của `unlink-google` — đọc kỹ trước khi gõ

Người dùng chỉ được gỡ Google khi **sau khi gỡ vẫn còn ít nhất một đường vào**:

| Tài khoản có | Bấm Gỡ Google |
|---|---|
| Mật khẩu **và** Google | ✅ Cho gỡ — vẫn còn mật khẩu để vào |
| **Chỉ** Google, `password_hash IS NULL` | ⛔ **Chặn.** Hiện: *"Đặt mật khẩu trước khi gỡ Google, không thì bạn sẽ không còn cách nào đăng nhập."* kèm nút dẫn thẳng tới ô đặt mật khẩu |
| Chỉ mật khẩu | *(nút Gỡ không hiện ra)* |

```java
// UserServlet.unlinkGoogle() — dòng đầu tiên của thân hàm, trước mọi thứ khác
if (!userDAO.hasPassword(currentUser.getId())) {
    request.setAttribute("error",
        "Hãy đặt mật khẩu trước khi gỡ liên kết Google — "
      + "gỡ bây giờ thì bạn sẽ không còn cách nào đăng nhập lại.");
    return "/WEB-INF/views/user/edit.jsp";
}
```

> **[NEVER] chỉ ẩn nút Gỡ ở JSP rồi coi là xong.** Ẩn ở giao diện chặn được người dùng
> bình thường, không chặn được ai gõ thẳng `/user?action=unlink-google` lên thanh địa chỉ.
> Đây đúng là bài học của chính đồ án này ở [`CHECKLIST.md §1`](../../guides/CHECKLIST.md) —
> *"đăng nhập A, mở URL sửa truyện của B, phải ra 403"*. Cùng một nguyên tắc: **hàng rào ở
> máy chủ là hàng rào thật, giao diện chỉ là lịch sự.**

### 3.2 Ca oái oăm: gắn một Google đã gắn ở tài khoản khác

`user_identities` có `UNIQUE (provider, provider_uid)` từ phase 1 nên CSDL sẽ ném
`SQLIntegrityConstraintViolationException`. **Bắt lấy nó**, đừng để lọt ra trang 500 —
hiện: *"Tài khoản Google này đã được gắn vào một tài khoản khác."*

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `mvn test` | *(điền)* |
| `mvn -q compile` | *(điền)* |
| Mở `/user?action=edit`, đổi tên hiển thị rồi lưu | *(điền — lưu được không, hiện đúng không)* |
| Mở `/user?action=edit`, đổi ảnh đại diện | *(điền)* |

**[GOTCHA]** Hai dòng cuối là **thứ đang chạy đúng** mà đợt này có nguy cơ làm hỏng. Đo
trước, không thì lúc form lưu hỏng sẽ không biết là mình gây ra hay vốn đã vậy.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `mvn test` — không tụt, có `UnlinkGuardTest` (4 ca)
- [ ] **Bằng tay — đường chính:**
  1. Đăng nhập `mocmien` / `123456` *(tài khoản chỉ có mật khẩu)*
  2. Vào `/user?action=edit` → thấy **"Chưa gắn tài khoản Google · [Gắn ngay]"**
  3. Bấm Gắn, chọn Gmail → trang tải lại, hiện **"Đã gắn — &lt;email&gt; · [Gỡ]"**
  4. Đăng xuất. Bấm **Đăng nhập với Google** bằng đúng Gmail đó → vào thẳng tài khoản
     `mocmien`, **không** tạo tài khoản mới
  5. Kiểm: `SELECT COUNT(*) FROM user_identities WHERE user_id = 2;` → **1**
- [ ] **Thử trường hợp xấu — cả năm ca:**
  1. 🔴 **Ca quan trọng nhất:** tạo tài khoản mới **hoàn toàn bằng Google**
     *(`password_hash IS NULL`)*, rồi bấm Gỡ → **bị chặn**, hiện lời nhắc đặt mật khẩu.
     Sau đó đặt mật khẩu → bấm Gỡ lại → **gỡ được**. Đăng xuất, đăng nhập bằng mật khẩu
     vừa đặt → **vào được**
  2. Gõ thẳng `/user?action=unlink-google` lên thanh địa chỉ với tài khoản chưa có mật
     khẩu → **vẫn bị chặn** *(không phải chỉ ẩn nút)*
  3. Gắn một Gmail đã gắn ở tài khoản khác → báo lỗi tử tế, **không** ra trang 500
  4. Chưa đăng nhập mà gọi `/user?action=link-google` → đá về trang đăng nhập
     *(`AuthFilter` lo, nhưng phải kiểm là nó có lo thật)*
  5. Gửi `link-google` với `idToken` bịa → từ chối
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** đổi tên hiển thị · đổi ảnh đại diện · đổi email ·
      đổi mật khẩu — **bốn thứ này ở cùng một form**, đừng chỉ kiểm khối mới

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở bảng Baseline đo lại **bằng hoặc tốt hơn**
- [ ] Đã commit: `ISSUE-001 phase-3 — gan/go tai khoan Google o trang ho so`
- [ ] **Ghi rõ phần CHƯA làm được** ở §7 và trong commit message
- [ ] Thêm một mục vào [`docs/page/guide.jsp`](../../../src/main/webapp/WEB-INF/views/page/guide.jsp):
      *"Gắn tài khoản Google để đăng nhập nhanh hơn"* — chức năng không ai biết thì bằng
      không có

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
