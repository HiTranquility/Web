# 🔍 [Review 1] Tích hợp Google — Review Phase 1: CSDL & Bảng User Identities

> **Đích:** `docs/projects/issues/ISSUE-001-tich-hop-google/review-1.md`
>
> **Cách dùng:** Điền sau khi Dev A hoàn thành xong `phase-1.md`. Đối chiếu baseline kiểm thử, schema migration và Acceptance Criteria.

## 📌 Meta

| | |
|---|---|
| **Issue gốc** | [issue.md](issue.md) |
| **Review cho** | [phase-1.md](phase-1.md) |
| **Ngày review** | 2026-09-17 |
| **Người review** | Tech Lead / Security Reviewer |
| **Kết luận** | ✅ Pass |

---

## 🎯 Phạm vi review

Phase 1 bao gồm:
1. Thêm cấu hình secret mẫu `google.properties.example` và đưa `google.properties` vào `.gitignore`.
2. Tạo bảng `user_identities` trong `schema.sql` và cho phép `users.password_hash` nhận giá trị `NULL`.
3. Cung cấp file migration CSDL `migration-001-google.sql`.
4. Tạo model `UserIdentity.java` và DAO `IdentityDAO.java` có đủ 4 phương thức CRUD cần thiết.
5. Kiểm tra test tự động không bị tụt lùi so với baseline ban đầu.

---

## 📸 Đối chiếu Baseline

| Lệnh kiểm tra | Trước phase (Gốc) | Sau phase 1 | Đạt? |
|---|---|---|:---:|
| `powershell -ExecutionPolicy Bypass -File scripts\test.ps1` | 28 pass / 0 fail | 32 pass / 0 fail | ✅ |
| Biên dịch code (`javac`) | Sạch, 0 lỗi | Sạch, 0 lỗi | ✅ |
| Khởi động ứng dụng web (`scripts\run.ps1`) | Trang chủ và đăng nhập bình thường | Không hồi quy, tương thích ngược | ✅ |

---

## ✅ Đối chiếu Acceptance Criteria Phase 1

| Tiêu chí | Đạt? | Ghi chú / Bằng chứng |
|---|:---:|---|
| Bảng `user_identities` có trong `schema.sql` với khóa ngoại trỏ sang `users` | ✅ | Đã thêm vào `schema.sql` sau bảng `users`, có `ON DELETE CASCADE` |
| `users.password_hash` chấp nhận giá trị `NULL` | ✅ | Cột đổi thành `VARCHAR(255) NULL`, `UserDAO.insert()` dùng `setNull` an toàn |
| `IdentityDAO` thao tác CRUD chuẩn JDBC, dùng PreparedStatement | ✅ | Đủ 4 phương thức: `findByProviderUid`, `findByUserId`, `insert`, `deleteByUserAndProvider` |
| File `google.properties` không bị `git status` theo dõi | ✅ | Đã khai báo trong cả `.gitignore` gốc và project `.gitignore` |
| Toàn bộ test cũ chạy pass, không hỏng hồi quy | ✅ | 32/32 tests pass (28 test gốc + 4 test mới cho UserIdentity và hasPassword) |

---

## 🐞 Lỗi phát hiện (nếu có)

| # | Mức độ | Mô tả | Vị trí / File | Đề xuất sửa |
|---|---|---|---|---|
| - | Không có | Tất cả tiêu chí Phase 1 đều đạt sạch | - | - |

---

## 📋 Action Items → Đưa vào Phase 2

- [x] **AI-1:** Chuyển giao các phương thức của `IdentityDAO` cho `AuthServlet` xử lý `idToken` ở Phase 2.
- [ ] **AI-2:** Tích hợp `GoogleTokenVerifier` kiểm tra chữ ký OIDC/Google OAuth ở Phase 2.

---

## 🏁 Kết luận (Verdict)

- **Có cho chuyển sang Phase 2 không?** Có (Đủ điều kiện chuyển Phase 2)
- **Ghi chú:** Schema CSDL, migration script, Model và DAO đã hoàn chỉnh, an toàn, không có lỗi biên dịch hay hồi quy. Sẵn sàng cho Phase 2: Xác minh `idToken` và xử lý đăng nhập Google ở máy chủ.
