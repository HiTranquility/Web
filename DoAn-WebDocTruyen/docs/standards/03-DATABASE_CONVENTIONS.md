# 🗄️ Quy ước Database — Schema / DAO / SQL

Áp dụng cho `database/*.sql` và `src/main/java/truyen/dao/`.
Schema thật xem [`../../database/schema.sql`](../../database/schema.sql).

---

## 1. Bảng tra nhanh đặt tên

| Loại | Quy tắc | Đúng | Sai |
|------|---------|------|-----|
| Tên bảng | `snake_case`, **số nhiều** | `stories`, `story_tags` | `Story`, `tblStory` |
| Bảng nối N–N | `<bảng1>_<bảng2>` theo alphabet | `story_tags` | `tag_story`, `mapping` |
| Khoá chính | luôn là `id` | `id` | `story_id`, `ma_truyen` |
| Khoá ngoại | `<bảng số ít>_id` | `author_id`, `story_id` | `idStory`, `fk_story` |
| Cột thường | `snake_case` | `view_count`, `created_at` | `viewCount`, `SoLuotXem` |
| Cột boolean | `is_`/`has_` | `is_deleted` | `deleted`, `flag` |
| Thời gian | `created_at`, `updated_at` | | `ngay_tao`, `createDate` |
| Index | `idx_<bảng>_<cột>` | `idx_stories_status` | `index1` |
| Ràng buộc unique | `uq_<bảng>_<cột>` | `uq_story_chapter` | `unique1` |

**Database dùng `snake_case`, Java dùng `camelCase`.** Việc nối hai bên là của
method `mapRow()` trong DAO — đó là chỗ duy nhất biết cả hai cách đặt tên:

```java
s.setViewCount(rs.getInt("view_count"));   // camelCase  ←  snake_case
```

---

## 2. Bảng tra kiểu dữ liệu

| Cần lưu | Dùng | Đừng dùng | Vì sao |
|---------|------|-----------|--------|
| Tên, tiêu đề | `VARCHAR(200)` | `TEXT` | `VARCHAR` index được, `TEXT` thì không |
| Mô tả ngắn | `VARCHAR(1000)` | | đủ cho bình luận |
| Mô tả truyện | `TEXT` (64 KB) | | |
| **Nội dung chương** | **`MEDIUMTEXT`** (16 MB) | ❌ `TEXT` | chương dài tiếng Việt vượt 64 KB và MySQL **cắt cụt âm thầm** |
| Chuỗi băm mật khẩu | `VARCHAR(255)` | `VARCHAR(60)` | BCrypt là 60, nhưng đổi thuật toán sau này cần chỗ |
| Trạng thái cố định | `ENUM('A','B')` | `VARCHAR` | DB tự chặn giá trị sai |
| Số đếm | `INT` | `BIGINT` | INT tới 2 tỉ, quá đủ |
| Ngày giờ | `DATETIME` | `TIMESTAMP` | `TIMESTAMP` chỉ tới năm 2038 và tự đổi theo múi giờ |
| Tiền | `DECIMAL(10,2)` | ❌ `FLOAT` | số thực làm tròn sai |

### Hai thứ bắt buộc ở cấp database

```sql
CREATE DATABASE webdoctruyen
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

- **`utf8mb4` không phải `utf8`.** Bảng mã tên "utf8" của MySQL chỉ chứa 3 byte,
  **không đủ cho emoji**. Truyện và bình luận chắc chắn sẽ có emoji.
- **`utf8mb4_unicode_ci`** — so sánh không phân biệt hoa thường, sắp xếp đúng
  tiếng Việt có dấu.

Và trong chuỗi kết nối JDBC:

```
?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Ho_Chi_Minh
```

Thiếu `serverTimezone` thì MySQL 8 **báo lỗi ngay lúc kết nối**.

---

## 3. Luật viết DAO

### Khuôn chuẩn — chép rồi đổi nội dung

```java
private static final String SELECT_BASE =
    "SELECT s.id, s.title, s.view_count, u.username AS author_name "
  + "FROM stories s "
  + "JOIN users u ON u.id = s.author_id ";

public List<Story> findLatest(int limit) throws SQLException {
    String sql = SELECT_BASE
               + "WHERE s.status = 'PUBLISHED' "
               + "ORDER BY s.updated_at DESC LIMIT ?";

    List<Story> list = new ArrayList<>();
    try (Connection con = DBConnection.get();
         PreparedStatement ps = con.prepareStatement(sql)) {

        ps.setInt(1, limit);                    // KHÔNG nối chuỗi

        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapRow(rs));
        }
    }
    return list;
}
```

### Bốn luật

**1. Luôn `PreparedStatement` với `?`. Không bao giờ nối chuỗi.**

```java
"... WHERE title LIKE '%" + keyword + "%'"   // ❌ SQL injection
"... WHERE title LIKE ?"                      // ✅
```

Người dùng nhập `' OR '1'='1` là truy vấn trả về **cả bảng**. Nhập thứ khác còn
xoá được bảng. `PreparedStatement` gửi câu lệnh và dữ liệu qua **hai đường
riêng**, nên dữ liệu không bao giờ được hiểu là câu lệnh — đó mới là lý do nó
an toàn, không phải vì nó "lọc ký tự xấu".

**2. Luôn `try-with-resources`.**
`Connection`, `PreparedStatement`, `ResultSet` đều phải trả lại. Quên đóng thì
rò rỉ kết nối; sau vài chục request MySQL đạt `max_connections` và **cả web
chết**, với lỗi trông chẳng liên quan gì tới chỗ thật sự sai.

**3. `throws SQLException` — đừng nuốt.**
DAO không biết phải làm gì khi lỗi. Servlet mới biết (hiện thông báo, ghi log).

**4. Một method `mapRow()` dùng chung.**
Thêm cột mới thì sửa **một** chỗ. Không tách thì sẽ sót một vòng lặp nào đó.

### Tránh lỗi N+1

```java
// ❌ 20 truyện = 21 truy vấn
List<Story> stories = storyDAO.findAll();
for (Story s : stories) s.setAuthorName(userDAO.findById(s.getAuthorId()).getUsername());

// ✅ 1 truy vấn — JOIN lấy luôn
"SELECT s.*, u.username AS author_name FROM stories s JOIN users u ON u.id = s.author_id"
```

Đây là nguyên nhân phổ biến nhất khiến trang danh sách chậm.

---

## 4. Khoá ngoại — `CASCADE` hay `SET NULL`

Hỏi: **"Bản ghi con còn ý nghĩa gì không khi bản ghi cha biến mất?"**

| Trả lời | Dùng | Ví dụ trong đồ án |
|---------|------|-------------------|
| Không còn nghĩa gì | `ON DELETE CASCADE` | xoá truyện → xoá chương, bình luận, bookmark |
| Vẫn còn nghĩa, chỉ mất một thông tin | `ON DELETE SET NULL` | xoá chương → bookmark **vẫn còn**, chỉ mất vị trí đọc |
| Không được phép xoá cha | `ON DELETE RESTRICT` | (đồ án này không dùng) |

```sql
FOREIGN KEY (story_id)        REFERENCES stories(id)  ON DELETE CASCADE,
FOREIGN KEY (last_chapter_id) REFERENCES chapters(id) ON DELETE SET NULL
```

Cột dùng `SET NULL` **bắt buộc phải cho phép `NULL`** — không thì MySQL báo lỗi
ngay lúc tạo bảng.

---

## 5. Xoá mềm — luật của đồ án này

**Không dùng `DELETE FROM` cho truyện, người dùng, bình luận.** Đổi cột trạng thái:

| Bảng | Cột | Giá trị "đã gỡ" |
|------|-----|-----------------|
| `stories` | `status` | `DELETED` |
| `comments` | `status` | `HIDDEN` |
| `users` | `status` | `BANNED` |

Lý do:
- Xoá thật thì bình luận và bookmark trỏ tới nó thành mồ côi
- Admin gỡ nhầm còn khôi phục được
- Ban tài khoản mà giữ nguyên truyện → độc giả đang đọc dở không mất

**Hệ quả bắt buộc:** mọi truy vấn hiển thị phải có điều kiện lọc.

```sql
WHERE status = 'PUBLISHED'    -- không phải chỉ  WHERE 1=1
```

Quên một chỗ là truyện đã gỡ hiện lại trên trang công khai.

---

## 6. Ba file SQL, ba việc

| File | Việc | Chạy khi nào |
|------|------|--------------|
| `schema.sql` | tạo database + 7 bảng | **một lần**, lúc cài |
| `setup_user.sql` | tạo tài khoản MySQL riêng cho app | **một lần**, sau schema |
| `sample_data.sql` | dữ liệu demo | tuỳ chọn, chạy lại bao nhiêu lần cũng được |

`sample_data.sql` phải **chạy lại được nhiều lần** — mở đầu bằng `DELETE FROM`
theo thứ tự **bảng con trước bảng cha**, không thì vướng khoá ngoại.

**Không dùng `root` cho ứng dụng.** `root` có toàn quyền trên mọi database của
máy; một lỗ SQL injection là kẻ tấn công có quyền đó. Tài khoản `truyen_app`
chỉ được `SELECT, INSERT, UPDATE, DELETE` trên đúng `webdoctruyen`.

---

## 7. Ba thứ tuyệt đối tránh

| Đừng | Vì sao |
|------|--------|
| Lưu tag thành chuỗi ngăn phẩy `"tien-hiep,huyen-huyen"` | Lọc phải `LIKE '%tien-hiep%'` → chậm, không dùng index, và **khớp nhầm** (`tien-hiep` khớp cả `tien-hiep-hai-huoc`). Dùng bảng nối `story_tags` |
| Lưu mật khẩu dạng thường | Lộ database là lộ hết. Luôn băm (BCrypt) |
| `SELECT *` trong code | Thêm cột vào bảng là code ngầm đổi hành vi. Liệt kê cột rõ ràng |
