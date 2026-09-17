# 🚀 ISSUE-001 — Phase 1: Chỗ ngồi trong cơ sở dữ liệu cho tài khoản Google

## 📌 Meta

| | |
|---|---|
| **Thuộc việc** | [ISSUE-001](ISSUE-001-tich-hop-google.md) — *cùng thư mục* |
| **Đợt** | Phase 1 / tổng 5 đợt |
| **Người làm** | Dev A — Auth & Security |
| **Trạng thái** | 📝 Chưa nhận |
| **Ngày bắt đầu** | *(điền lúc nhận)* |
| **Đụng vào** | `database/schema.sql` · `database/sample_data.sql` · `model/UserIdentity.java` *(mới)* · `dao/IdentityDAO.java` *(mới)* · `.gitignore` · `src/main/resources/google.properties.example` *(mới)* |

---

## 1. Đợt này làm tới đâu

- **Trong đợt này:** thêm bảng `user_identities`, cho `users.password_hash` nhận `NULL`,
  viết `UserIdentity` + `IdentityDAO` với đủ bốn phương thức, và dựng sẵn chỗ chứa khoá
  Google *(`google.properties`, đã `.gitignore`)*. Viết xong thì bảng có, DAO chạy được
  bằng test — **nhưng chưa ai gọi nó cả**.
- **Để đợt sau:** mọi thứ liên quan tới đăng nhập. `AuthServlet` không đụng đến ở đợt này,
  một dòng cũng không.

### Vì sao tách đợt riêng

> **[MUST] Chia đợt theo RỦI RO, không theo khối lượng.**

**Lý do tách của đợt này:** đợt này sửa `database/schema.sql`, và luật ở
[`projects/README.md`](../README.md) nói sửa schema **luôn đứng riêng một đợt và làm
trước**. Lý do rất cụ thể ở đây: `password_hash` đang là `NOT NULL` và **mọi** đường đăng
ký đều dựa vào điều đó. Nới nó ra là nới một ràng buộc mà `AuthServlet`, `UserDAO`,
`DemoData` đều ngầm tin tưởng. Nếu trộn việc này với code xác minh token của phase 2, thì
lúc đăng nhập hỏng sẽ không biết hỏng do câu SQL mới hay do chữ ký token — hai chỗ hoàn
toàn khác nhau, mất gấp đôi thời gian để tách ra.

Đợt này còn **cố ý không có code chạy được cho người dùng**. Đó là điểm mạnh chứ không
phải điểm yếu: hết đợt 1, pull về chạy web thì **mọi thứ y như cũ**, không có gì để hỏng.

---

## 2. Phụ thuộc

- **Phải xong trước mới làm được:** không có. Đây là đợt đầu tiên, bắt đầu được ngay.
- **Xong đợt này mới mở khoá được:** phase 2 → kéo theo phase 3, 4, 5.

> Đợt này sửa `schema.sql`. **Báo cả nhóm ngay khi commit** — Dev B, C, D đang chạy schema
> cũ, pull về mà không chạy lại SQL thì code của họ sẽ lỗi và họ sẽ tưởng do mình.

---

## 3. Việc trong đợt

| # | Task | Chạm vào | Xong |
|---|---|---|:---:|
| 1 | Thêm `google.properties` vào `.gitignore`, tạo `google.properties.example` với các khoá rỗng — **làm trước mọi thứ**, trước cả khi có khoá thật | `.gitignore` · `src/main/resources/google.properties.example` | ☐ |
| 2 | Viết bảng `user_identities` vào `schema.sql`, kèm chú thích từng cột như các bảng khác | `database/schema.sql` | ☐ |
| 3 | Đổi `users.password_hash` từ `NOT NULL` thành `NULL`, ghi chú **vì sao** ngay tại cột | `database/schema.sql` | ☐ |
| 4 | Viết script nâng cấp `database/migration-001-google.sql` cho ai đã có dữ liệu, không muốn nạp lại từ đầu | `database/migration-001-google.sql` *(mới)* | ☐ |
| 5 | `model/UserIdentity.java` — JavaBean thuần, chỉ getter/setter | `model/UserIdentity.java` | ☐ |
| 6 | `dao/IdentityDAO.java` — `findByProviderUid`, `findByUserId`, `insert`, `deleteByUserAndProvider` | `dao/IdentityDAO.java` | ☐ |
| 7 | `UserDAO.insert()` chấp nhận `passwordHash == null` mà không ném lỗi | `dao/UserDAO.java` | ☐ |
| 8 | Test `IdentityDAOTest` — chèn, tìm lại, xoá, và chèn trùng `(provider, provider_uid)` phải bật ra | `src/test/java/truyen/IdentityDAOTest.java` | ☐ |

### Bảng phải viết ra như thế nào

Chép nguyên khối này vào `schema.sql`, đặt **sau** bảng `users` *(vì có khoá ngoại trỏ về
`users`)* và **trước** `password_resets`:

```sql
-- =============================================================================
--  user_identities — tài khoản này đăng nhập được bằng những đường nào
-- =============================================================================
--  MỘT NGƯỜI, NHIỀU ĐƯỜNG VÀO. Cùng một tài khoản có thể vào bằng mật khẩu,
--  bằng Google, và sau này bằng đường khác nữa.
--
--  VÌ SAO KHÔNG NHÉT google_sub THÀNH MỘT CỘT TRONG users
--    Nhét cột thì mỗi nhà cung cấp mới là một cột mới, và 99% số dòng để rỗng.
--    Thêm Facebook là thêm facebook_id, thêm GitHub là thêm github_id — bảng
--    users phình ra vì thứ chẳng liên quan gì tới người dùng.
--    Bảng nối thì thêm nhà cung cấp chỉ là thêm một giá trị vào ENUM.
--
--  provider_uid LÀ sub CỦA GOOGLE, KHÔNG PHẢI EMAIL.
--    Người ta đổi được email trong tài khoản Google của họ. sub thì không đổi
--    bao giờ. Khoá theo email nghĩa là hôm nào họ đổi email là mất tài khoản.
--    Email vẫn lưu, nhưng chỉ để HIỂN THỊ, không dùng để tra.
-- =============================================================================
CREATE TABLE user_identities (
    id           INT AUTO_INCREMENT PRIMARY KEY,

    user_id      INT NOT NULL,

    -- Hiện chỉ có GOOGLE. ENUM để thêm nhà cung cấp sau chỉ là sửa một dòng.
    provider     ENUM('GOOGLE') NOT NULL,

    -- Định danh do nhà cung cấp cấp. Với Google là trường `sub` trong idToken.
    -- 255 vì Google không cam kết độ dài, chỉ cam kết không quá 255.
    provider_uid VARCHAR(255) NOT NULL,

    -- Email lúc gắn. CHỈ ĐỂ HIỂN THỊ ở trang hồ sơ — không tra cứu theo cột này.
    email        VARCHAR(150),

    created_at   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    -- Một tài khoản Google chỉ gắn được vào ĐÚNG MỘT tài khoản trên web.
    -- Ràng buộc ở CSDL chứ không chỉ ở code: hai request gửi cùng lúc thì
    -- code kiểm trước-rồi-chèn vẫn lọt cả hai, CSDL thì không.
    UNIQUE KEY uq_identity_provider (provider, provider_uid),

    -- Một tài khoản cũng chỉ gắn được MỘT tài khoản Google, không phải ba.
    UNIQUE KEY uq_identity_user (user_id, provider)
) ENGINE=InnoDB;
```

Và sửa cột trong bảng `users`:

```sql
    -- Cho phép NULL từ ISSUE-001 phase 1: tài khoản chỉ đăng nhập bằng Google
    -- thì KHÔNG CÓ mật khẩu để mà băm.
    --
    -- Cách cũ là nhét một chuỗi băm ngẫu nhiên vào cho đủ cột NOT NULL. Nhìn
    -- thì chạy, nhưng nó nói dối: hàng đó trông như "có mật khẩu" trong khi
    -- không ai trên đời biết mật khẩu đó. Đến lúc làm nút "Gỡ liên kết Google"
    -- ở phase 3 thì không phân biệt được ai gỡ được ai không.
    --
    -- NULL nói đúng sự thật: tài khoản này chưa có mật khẩu.
    password_hash VARCHAR(255) NULL,
```

> **[Bẫy]** Đổi cột thành `NULL` thì mọi câu `INSERT INTO users` **liệt kê đủ cột** vẫn
> chạy như cũ — nhưng mọi chỗ **đọc** ra rồi gọi `.equals()` hay `.length()` trên
> `passwordHash` giờ có thể `NullPointerException`. Grep trước khi commit:
> `grep -rn "getPasswordHash" src/main/java/`

---

## 4. 📸 Baseline — đo TRƯỚC khi gõ dòng code đầu tiên

> **[MUST] Điền số thật.** Ghi "OK" hay "chạy được" là vô dụng.

| Lệnh | Kết quả **trước** đợt này |
|---|---|
| `mvn test` | *(dự kiến 28 pass / 0 fail — **đo lại, đừng chép**)* |
| `mvn -q compile` | *(điền)* |
| `SHOW TABLES;` trong `webdoctruyen` | *(dự kiến 13 bảng — đếm và ghi số)* |
| Mở site chạy thử | *(điền: đăng nhập `mocmien`/`123456`, đăng ký tài khoản mới — cả hai chạy?)* |

**[GOTCHA]** Không có baseline thì lúc xong đợt sẽ không phân biệt được **lỗi mình vừa gây
ra** với **lỗi vốn đã có sẵn**. Riêng đợt này, số bảng ở dòng thứ ba là số quan trọng
nhất: xong đợt phải là **14**, không phải 13 và cũng không phải 15.

---

## 5. Kiểm lại khi xong

- [ ] **Tự động:** `mvn test` — **không được tụt** so với bảng §4, và có thêm
      `IdentityDAOTest`
- [ ] **Nạp lại từ đầu chạy được:**
      `mysql -u root -p < database/schema.sql` rồi
      `mysql -u root -p webdoctruyen < database/sample_data.sql` — **không có lỗi đỏ nào**
- [ ] **Nâng cấp tại chỗ cũng chạy được:** trên một bản DB **cũ còn dữ liệu**, chạy
      `migration-001-google.sql` → `SHOW TABLES;` ra 14 bảng, dữ liệu cũ **còn nguyên**
- [ ] **Bằng tay:** đăng nhập `mocmien` / `123456` → vẫn vào được
- [ ] **Thử trường hợp xấu:**
  1. Chèn hai dòng `user_identities` cùng `(GOOGLE, 'sub-abc')` → CSDL phải **từ chối**
  2. Chèn `user_identities` với `user_id` không tồn tại → khoá ngoại phải **từ chối**
  3. `INSERT INTO users (username, email, password_hash, …) VALUES (…, NULL, …)` → **được**
  4. Xoá một `users` → dòng `user_identities` của họ **đi theo** (CASCADE)
- [ ] **Thứ đang chạy đúng vẫn chạy đúng:** đăng ký tài khoản mới bằng form thường, đăng
      truyện, bình luận, quên mật khẩu — **không** chỉ kiểm phần vừa làm
- [ ] `git ls-files | grep google.properties` → chỉ ra file `.example`, **không** ra file thật

---

## 6. Đóng đợt — đủ ba điều này mới được ✅

- [ ] Mọi số ở bảng Baseline đo lại **bằng hoặc tốt hơn** trước đợt
- [ ] Đã commit, dòng đầu ghi mã: `ISSUE-001 phase-1 — them bang user_identities, noi password_hash`
- [ ] **Ghi rõ phần CHƯA làm được**, nếu có — ở §7 và trong commit message

> Đợt này **có sửa `database/schema.sql`** → nhắn cả nhóm ngay khi commit, kèm đúng một
> dòng lệnh họ cần chạy:
> `mysql -u root -p webdoctruyen < database/migration-001-google.sql`
> Đừng chỉ nhắn "mình sửa schema nhé" — họ sẽ không biết phải gõ gì.

---

## 7. Còn vướng / chưa làm

*(để trống — điền lúc làm)*
