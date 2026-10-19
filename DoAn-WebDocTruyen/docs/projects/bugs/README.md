# 🐞 Bugs — lỗi đang nợ

Mỗi lỗi là **một thư mục** chuyên biệt trong thư mục này:

```text
docs/projects/bugs/
├── README.md                                                        ← bạn đang ở đây (bảng theo dõi)
├── bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan/
│   └── bug.md                                                       ← chi tiết lỗi, tái hiện, nguyên nhân
└── bug-002-web-xml-khai-trung-error-page/
    └── bug.md
```

Luật chung + cách phân biệt ISSUE với bug: [`../README.md`](../README.md).

---

## Báo một lỗi mới

1. `git pull`, lấy số kế tiếp (hoặc chạy `powershell -ExecutionPolicy Bypass -File docs\reindex.ps1`).
2. Tạo thư mục `bug-NNN-ten-loi-khong-dau/`.
3. Copy [`../../templates/bug-template.md`](../../templates/bug-template.md) thành `bug.md` bên trong thư mục vừa tạo.
4. **Viết các bước tái hiện trước tiên.** Lỗi không tái hiện được thì không sửa được — phần còn lại của file điền sau cũng kịp.
5. Thêm dòng vào bảng danh sách dưới đây.

> Sửa được ngay trong hai phút thì cứ sửa rồi commit, **không cần mở file bug**.
> Bảng này để theo dõi thứ *chưa sửa được ngay* — nợ thì mới cần sổ.

---

## Danh sách lỗi

| Mã | Lỗi | Mức | Người sửa | Trạng thái |
|---|---|:---:|---|---|
| **[bug-001](bug-001-dang-nhap-google-gia-mao-bat-ky-tai-khoan/bug.md)** | Bấm "Đăng nhập với Google", gõ email người khác là vào thẳng tài khoản họ | 🔴 Chặn | Dev A — Auth & Security | ✅ Đã sửa |
| **[bug-002](bug-002-web-xml-khai-trung-error-page/bug.md)** | `web.xml` khai hai lần cùng `<error-page>` 404 và `Throwable` — hai hệ trang lỗi cùng tồn tại, hiện chạy đúng **do may** | 🟡 Nhẹ | N1 — Nền tảng & Quản trị | ✅ Đã sửa |
| **[bug-003](bug-003-admin-filter-lo-hong-phan-quyen-editor/bug.md)** | `AdminFilter` kiểm tra sai điều kiện, để `EDITOR` lọt vào xem và sửa `/admin/wallet`, `/admin/user` | 🔴 Chặn | Phát | ✅ Đã sửa |
| **[bug-004](bug-004-mat-quyen-rbac-khi-cap-nhat-ho-so-hoac-vip/bug.md)** | Cập nhật hồ sơ hoặc gia hạn VIP làm mất sạch vai trò và quyền RBAC trong phiên cho đến khi relogin | 🔴 Chặn | Phát | ✅ Đã sửa |
| **[bug-005](bug-005-moderator-bi-chan-403-khi-an-binh-luan-trang-doc/bug.md)** | `CommentServlet` văng lỗi 403 Forbidden khi `MODERATOR` bấm ẩn bình luận vi phạm trực tiếp trên trang đọc | 🟠 Nặng | Phát | ✅ Đã sửa |

### Mức độ

| Mức | Nghĩa | Ví dụ |
|---|---|---|
| 🔴 **Chặn** | Không dùng được, hoặc hở bảo mật | Đăng nhập hỏng · `AuthFilter` cho khách vào trang admin |
| 🟠 **Nặng** | Một chức năng sai kết quả | Xoá nhầm bình luận khác · phân trang nhảy cóc |
| 🟡 **Nhẹ** | Xấu hoặc khó chịu, vẫn dùng được | CSS vỡ ở màn hình hẹp · chữ sai chính tả |

> 🔴 Chặn thì **sửa trước mọi ISSUE đang mở**, kể cả đang làm dở.
