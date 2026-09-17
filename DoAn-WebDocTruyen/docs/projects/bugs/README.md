# 🐞 Bugs — lỗi đang nợ

Mỗi lỗi là **một file phẳng** ngay trong thư mục này:

```text
docs/projects/bugs/
├── README.md                          ← bạn đang ở đây (bảng theo dõi)
└── bug-001-xoa-nham-binh-luan.md
```

Luật chung + cách phân biệt ISSUE với bug: [`../README.md`](../README.md).

---

## Báo một lỗi

1. `git pull`, lấy số kế tiếp.
2. Copy [`../../templates/bug-template.md`](../../templates/bug-template.md)
   thành `bug-NNN-ten-loi-khong-dau.md`.
3. **Viết các bước tái hiện trước tiên.** Lỗi không tái hiện được thì không sửa
   được — phần còn lại của file điền sau cũng kịp.
4. Thêm dòng vào bảng dưới.

> Sửa được ngay trong hai phút thì cứ sửa rồi commit, **không cần mở file bug**.
> Bảng này để theo dõi thứ *chưa sửa được ngay* — nợ thì mới cần sổ.

---

## Danh sách

| Mã | Lỗi | Mức | Người sửa | Trạng thái |
|---|---|---|---|---|
| *(chưa có lỗi nào được ghi)* | | | | |

### Mức độ

| Mức | Nghĩa | Ví dụ |
|---|---|---|
| 🔴 **Chặn** | Không dùng được, hoặc hở bảo mật | Đăng nhập hỏng · `AuthFilter` cho khách vào trang admin |
| 🟠 **Nặng** | Một chức năng sai kết quả | Xoá nhầm bình luận khác · phân trang nhảy cóc |
| 🟡 **Nhẹ** | Xấu hoặc khó chịu, vẫn dùng được | CSS vỡ ở màn hình hẹp · chữ sai chính tả |

🔴 thì **sửa trước mọi ISSUE đang mở**, kể cả đang làm dở.
